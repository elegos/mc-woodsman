package name.giacomofurlan.woodsman.brain.task;

import java.util.Optional;

import name.giacomofurlan.woodsman.datagen.ModBlockTagsProvider;
import name.giacomofurlan.woodsman.util.WorldUtil;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
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

        if (isFollowingPath() && entity.isNavigating()) {
            return true;
        }

        SimpleInventory inventory = entity.getInventory();
        int inventorySize = inventory.size();
        int numStacks = 0;
        
        for (int i = 0; i < inventorySize; i++) {
            if (inventory.getStack(i) != ItemStack.EMPTY) {
                numStacks++;
            }
        }

        World world = entity.getWorld();

        Boolean chestWithinReach = WorldUtil.getBlockPos(Box.from(new BlockBox(entity.getBlockPos())))
            .stream()
            .anyMatch(pos -> world.getBlockState(pos).isIn(ModBlockTagsProvider.STORAGE_BLOCKS));

        Boolean needToDepositDueTooMuchTime = maxSecondsBetweenDeposits > 0
            && numStacks > 0
            && (server.getTicks() - lastDepositTick) > (maxSecondsBetweenDeposits * ticksPerSecond);

        Boolean canDepositWithinReach = chestWithinReach && (numStacks > 0);

        if (
            !needToDepositDueTooMuchTime
            && !needToDepositDueTooMuchTime
            && !canDepositWithinReach
            && (
                (depositIfNotFull && numStacks == 0)
                || (!depositIfNotFull && numStacks < inventorySize)
            )
        ) {
            return false;
        }

        if (walkRoutine(entity)) {
            return true;
        }

        
        BlockPos jobSite = brain.getOptionalMemory(MemoryModuleType.JOB_SITE).get().getPos();

        for (int distance = 1; distance < operativeDistance; distance++) {
            Optional<BlockPos> candidatePos = WorldUtil.cubicCoordinatesFromCenter(jobSite, distance)
                .stream()
                .filter((pos) -> {
                    if (!world.getBlockState(pos).isIn(ModBlockTagsProvider.STORAGE_BLOCKS)) {
                        return false;
                    }

                    // Filter full chests
                    Inventory candidateInventory = (Inventory) world.getBlockEntity(pos);
                    int numOccupiedStacks = 0;
                    for (int i = 0; i < inventorySize; i++) {
                        if (candidateInventory.getStack(i) != ItemStack.EMPTY) {
                            numOccupiedStacks++;
                        }
                    }

                    return numOccupiedStacks < candidateInventory.size();
                })
                .map(pos -> Optional.of(pos))
                .reduce(Optional.empty(), (accumulator, value) -> accumulator.isEmpty() || jobSite.getManhattanDistance(value.get()) < jobSite.getManhattanDistance(accumulator.get())  ? value : accumulator);
            
            if (candidatePos.isEmpty()) {
                continue;
            }

            Optional<BlockPos> targetPos = WorldUtil.getBlockPos(new Box(candidatePos.get()).expand(1))
                .stream()
                .filter(pos -> world.getBlockState(pos).isAir())
                .findFirst();

            if (targetPos.isEmpty()) {
                continue;
            }

            startWalking(entity, targetPos.get());
            if (!hasArrived(entity)) {
                return true;
            }

            Inventory inventoryBlock = (Inventory) world.getBlockEntity(candidatePos.get());

            villagerInventoryLoop:
            for (int i = 0; i < inventorySize; i++) {
                ItemStack stack = inventory.getStack(i);

                for (int slot = 0; i < inventoryBlock.size(); slot++) {
                    ItemStack blockStack = inventoryBlock.getStack(slot);
                    if (blockStack == ItemStack.EMPTY) {
                        inventoryBlock.setStack(slot, stack);
                        inventory.setStack(i, ItemStack.EMPTY);

                        continue villagerInventoryLoop;
                    } else if (blockStack.getItem().equals(stack.getItem()) && blockStack.getCount() < blockStack.getMaxCount()) {
                        int toTransfer = Math.min(stack.getCount(), blockStack.getMaxCount() - blockStack.getCount());
                        blockStack.setCount(toTransfer + blockStack.getCount());
                        if (toTransfer == stack.getCount()) {
                            inventory.setStack(i, ItemStack.EMPTY);

                            continue villagerInventoryLoop;
                        } else {
                            stack.setCount(stack.getCount() - toTransfer);
                        }
                    }
                }
                if (inventory.getStack(i) != ItemStack.EMPTY) {
                    inventoryBlock.setStack(i, inventory.getStack(i));
                    inventory.setStack(i, ItemStack.EMPTY);
                }
            }

            lastDepositTick = server.getTicks();
            
            return true;
        }

        // If the inventory is full, always return true (block other actions)
        return true;
    }

}
