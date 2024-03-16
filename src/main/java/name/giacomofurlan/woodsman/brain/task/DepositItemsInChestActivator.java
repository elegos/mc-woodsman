package name.giacomofurlan.woodsman.brain.task;

import java.util.Optional;

import name.giacomofurlan.woodsman.datagen.ModBlockTagsProvider;
import name.giacomofurlan.woodsman.util.WorldUtil;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.World;

public class DepositItemsInChestActivator extends WalkableActivator {
    protected Boolean depositIfNotFull;
    protected int maxSecondsBetweenDeposits;
    protected Integer lastDepositTick = null;
    protected Integer ticksPerSecond;
    protected Integer operativeDistance;

    public DepositItemsInChestActivator(
        Boolean depositIfNotFull,
        int maxSecondsBetweenDeposits,
        int ticksPerSecond,
        int operativeDistance,
        float walkSpeed
    ) {
        super(walkSpeed);

        this.maxSecondsBetweenDeposits = maxSecondsBetweenDeposits;
        this.depositIfNotFull = depositIfNotFull;
        this.ticksPerSecond = ticksPerSecond;
        this.operativeDistance = operativeDistance;
    }

    @Override
    public boolean run(VillagerEntity entity, Brain<VillagerEntity> brain) {
        MinecraftServer server = entity.getServer();

        if (lastDepositTick == null) {
            lastDepositTick = server.getTicks();
        }

        if (entity.getNavigation().isFollowingPath() && entity.isNavigating()) {
            return true;
        }

        SimpleInventory inventory = entity.getInventory();
        int inventorySize = inventory.size();
        int numStacks = 0;
        
        for (int i = 0; i < inventorySize; i++) {
            ItemStack itemStack = inventory.getStack(i);
            if (itemStack.isIn(ItemTags.SAPLINGS)) {
                inventorySize--;
            } else if (itemStack.getItem() != Items.AIR) {
                numStacks++;
            }
        }
        
        if (numStacks == 0) {
            return false;
        }

        
        World world = entity.getWorld();
        Optional<GlobalPos> jobPos = brain.getOptionalRegisteredMemory(MemoryModuleType.JOB_SITE);
        Box jobPosBox = new Box(jobPos.get().getPos()).expand(10);
        
        Boolean chestAroundChopBlock = WorldUtil.getBlockPos(jobPosBox)
            .stream()
            .anyMatch(pos -> world.getBlockState(pos).isIn(ModBlockTagsProvider.STORAGE_BLOCKS));
        
        if (!chestAroundChopBlock) {
            return false;
        }

        Boolean isFullOfItems = numStacks == inventorySize;

        Boolean needToDepositDueTooMuchTime = maxSecondsBetweenDeposits > 0
            && (server.getTicks() - lastDepositTick) > (maxSecondsBetweenDeposits * ticksPerSecond);

        if (!isFullOfItems && !(depositIfNotFull && needToDepositDueTooMuchTime)) {
            return false;
        }


        if (walkRoutine(entity)) {
            return true;
        }


        Optional<BlockPos> candidatePos = WorldUtil.getBlockPos(jobPosBox, true)
            .stream()
            .filter(pos -> world.getBlockState(pos).isIn(ModBlockTagsProvider.STORAGE_BLOCKS))
            .filter(pos -> ((LootableContainerBlockEntity) world.getBlockEntity(pos))
                        .containsAny(stack -> stack.getItem() == Items.AIR))
            .findFirst();
        
        if (candidatePos.isEmpty()) {
            return false;
        }

        Optional<BlockPos> targetPos = WorldUtil.getBlockPos(new Box(candidatePos.get()).expand(1))
            .stream()
            .filter(pos -> world.getBlockState(pos).isAir())
            .findFirst();

        if (targetPos.isEmpty()) {
            return false;
        }

        startWalking(entity, targetPos.get());
        if (!hasArrived(entity)) {
            return true;
        }

        LootableContainerBlockEntity inventoryBlock = (LootableContainerBlockEntity) world.getBlockEntity(candidatePos.get());

        for (int i = 0; i < inventorySize; i++) {
            ItemStack stack = inventory.getStack(i);

            if (stack.isIn(ItemTags.SAPLINGS) || stack.getItem() == Items.AIR) {
                continue;
            }

            int result = addItemStackToChest(inventoryBlock, stack);
            if (result == 0) {
                inventory.setStack(i, ItemStack.EMPTY);
            } else {
                stack.setCount(result);
            }
        }

        lastDepositTick = server.getTicks();
        
        return true;
    }

    /**
     * Adds an {@link ItemStack} to a chest block entity. Returns the number of items left that couldn't be added.
     *
     * @param chestEntity The chest block entity to add the {@link ItemStack} to
     * @param stack The {@link ItemStack} to add
     * @return The number of items left that couldn't be added
     */
    protected int addItemStackToChest(LootableContainerBlockEntity chestEntity, ItemStack stack) {
        if (chestEntity == null) {
            return -1;
        }

        for (int slot = 0; slot < chestEntity.size(); slot++) {
            ItemStack slotStack = chestEntity.getStack(slot);

            if (slotStack.isEmpty()) {
                chestEntity.setStack(slot, stack.copy());

                return 0;
            }

            if (ItemStack.areItemsEqual(stack, slotStack)) {
                int spaceLeftInSlot = Math.min(stack.getCount(), slotStack.getMaxCount() - slotStack.getCount());

                slotStack.increment(spaceLeftInSlot);
                stack.decrement(spaceLeftInSlot);

                if (stack.isEmpty()) {
                    return 0;
                }
            }
        }

        return stack.getCount();
    }

}
