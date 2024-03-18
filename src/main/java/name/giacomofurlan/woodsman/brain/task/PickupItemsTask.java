package name.giacomofurlan.woodsman.brain.task;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import name.giacomofurlan.woodsman.Woodsman;
import name.giacomofurlan.woodsman.brain.ModMemoryModuleType;
import name.giacomofurlan.woodsman.util.WorldCache;
import name.giacomofurlan.woodsman.util.WorldUtil;
import net.minecraft.entity.Entity.RemovalReason;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.task.VillagerWorkTask;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class PickupItemsTask extends VillagerWorkTask {
    protected int searchRadius;
    protected float walkSpeed;
    protected List<TagKey<Item>> itemTagsToPick;
    protected List<Item> itemsToPick;

    protected long lastCheckedTime;


    public PickupItemsTask(int searchRadius, float walkSpeed, List<TagKey<Item>> itemTagsToPick,
            List<Item> itemsToPick) {
        this.searchRadius = searchRadius;
        this.walkSpeed = walkSpeed;
        this.itemTagsToPick = itemTagsToPick;
        this.itemsToPick = itemsToPick;
    }

    @Override
    protected boolean shouldRun(ServerWorld serverWorld, VillagerEntity villagerEntity) {
        if (isInventoryFull(villagerEntity.getInventory())) {
            return false;
        }

        Brain<VillagerEntity> brain = villagerEntity.getBrain();
        Optional<String> currentTask = brain.getOptionalMemory(ModMemoryModuleType.CURRENT_WOODSMAN_TASK);

        if (currentTask.isPresent() && !currentTask.get().equals(PickupItemsTask.class.getName())) {
            return false;
        }

        if (serverWorld.getTime() - this.lastCheckedTime < 60L) {
            return false;
        }
        this.lastCheckedTime = serverWorld.getTime();

        Optional<BlockPos> nearestItemOnGroundPos = getNearestItemOnGroundPos(villagerEntity, searchRadius);
        if (nearestItemOnGroundPos.isEmpty()) {
            return false;
        }

        return true;
    }
    
    @Override
    protected boolean shouldKeepRunning(ServerWorld serverWorld, VillagerEntity villagerEntity, long l) {
        if (isInventoryFull(villagerEntity.getInventory())) {
            return false;
        }

        Optional<String> currentTask = villagerEntity.getBrain().getOptionalMemory(ModMemoryModuleType.CURRENT_WOODSMAN_TASK);

        return (currentTask.isPresent() && currentTask.get().equals(PickupItemsTask.class.getName()))
            && villagerEntity.isNavigating();
    }

    @Override
    protected void run(ServerWorld serverWorld, VillagerEntity villagerEntity, long l) {
        List<ItemEntity> itemsToPickNow = getItemsToPick(villagerEntity, 2);
        Brain<VillagerEntity> brain = villagerEntity.getBrain();
        EntityNavigation navigation = villagerEntity.getNavigation();

        // Pick the items around and return
        if (!itemsToPickNow.isEmpty()) {
            Woodsman.LOGGER.debug("Picking items around ({})", villagerEntity.getBlockPos().toShortString());
            navigation.stop();
            brain.forget(ModMemoryModuleType.CURRENT_WOODSMAN_TASK);

            SimpleInventory inventory = villagerEntity.getInventory();

            itemsToPickNow.stream().forEach(itemEntity -> {
                ItemStack change = inventory.addStack(itemEntity.getStack());
                if (change == ItemStack.EMPTY) {
                    itemEntity.remove(RemovalReason.DISCARDED);
                } else {
                    itemEntity.setStack(change);
                }
            });

            return;
        }

        if (villagerEntity.isNavigating()) {
            Woodsman.LOGGER.debug("PickupItemsTask: Navigating...");

            return;
        }

        Optional<BlockPos> nearestItemOnGroundPos = getNearestItemOnGroundPos(villagerEntity, searchRadius);
        // Item to pick not in reach: move to it
        if (!nearestItemOnGroundPos.isEmpty()) {
            Woodsman.LOGGER.debug("Moving to pick items around ({})", nearestItemOnGroundPos.get().toShortString());
            navigation.startMovingAlong(navigation.findPathTo(nearestItemOnGroundPos.get(), 1), walkSpeed);
            brain.remember(ModMemoryModuleType.CURRENT_WOODSMAN_TASK, PickupItemsTask.class.getName());

            return;
        }
    }

    protected Optional<BlockPos> getNearestItemOnGroundPos(VillagerEntity entity, int radius) {
        BlockPos entityPos = entity.getBlockPos();

        return getItemsToPick(entity, radius)
            .stream()
            .map(item -> item.getBlockPos())
            .sorted(Comparator.comparingInt(pos -> pos.getManhattanDistance(entityPos)))
            .findFirst();
    }

    protected List<ItemEntity> getItemsToPick(VillagerEntity entity, int radius) {
        return getItemsToPick(entity.getWorld(), entity.getBlockPos(), radius);
    }

    protected List<ItemEntity> getItemsToPick(World world, BlockPos entityPos, int radius) {
        WorldCache worldCache = WorldCache.getInstance();
        return world.getEntitiesByClass(
            ItemEntity.class,
            WorldUtil.cubicBoxFromCenter(entityPos, radius),
            (itemEntity) -> itemTagsToPick.stream().anyMatch(tag -> (
                itemEntity.getStack().isIn(tag)
                || itemsToPick.stream().anyMatch(item -> itemEntity.getStack().getItem().equals(item))
            ) && (
                // Hide items on leaves, which probably are not reachable in any case
                !worldCache.getCachedBlock(world, itemEntity.getBlockPos().down()).isIn(BlockTags.LEAVES))
            )
        );
    }

    public Boolean checkItemOnGround(ItemEntity itemEntity) {
        return itemTagsToPick.stream().anyMatch(tag -> itemEntity.getStack().isIn(tag))
            || itemsToPick.stream().anyMatch(item -> itemEntity.getStack().getItem().equals(item));
    }

    public Boolean isInventoryFull(SimpleInventory inventory) {
        for (int i = 0; i < inventory.size(); i++) {
            if (inventory.getStack(i).getItem() == Items.AIR) {
                return false;
            }
        }
        
        return true;
    }
}
