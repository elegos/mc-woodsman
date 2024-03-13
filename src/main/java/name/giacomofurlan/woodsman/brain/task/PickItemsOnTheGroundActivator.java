package name.giacomofurlan.woodsman.brain.task;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import net.minecraft.entity.Entity.RemovalReason;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.item.Item;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

public class PickItemsOnTheGroundActivator extends WalkableActivator {
    protected List<TagKey<Item>> itemTagsToPick;
    protected List<Item> itemsToPick;
    protected int searchRadius;

    public PickItemsOnTheGroundActivator(
        List<TagKey<Item>> itemTagsToPick, List<Item> itemsToPick, int searchRadius, float walkSpeed
    ) {
        super(walkSpeed);

        this.itemTagsToPick = itemTagsToPick;
        this.itemsToPick = itemsToPick;
        this.searchRadius = searchRadius;
    }

    @Override
    public boolean run(VillagerEntity entity, Brain<VillagerEntity> brain) {
        World world = entity.getWorld();
        BlockPos entityPos = entity.getBlockPos();

        // Pick any item during the course of action
        world.getEntitiesByClass(
            ItemEntity.class,
            Box.enclosing(entityPos.mutableCopy().add(2, 4, 2), entityPos.mutableCopy().add(-3, 0, -3)),
            this::checkItemOnGround
        ).forEach(itemEntity -> {
            entity.getInventory().addStack(itemEntity.getStack()).toString();
            itemEntity.remove(RemovalReason.DISCARDED);
        });

        // Moving to next item to collect
        if (walkRoutine(entity)) {
            return true;
        }

        // Search for the next item(s) to collect
        Optional<BlockPos> nearestItem = entity.getWorld().getEntitiesByClass(
            ItemEntity.class,
            Box.enclosing(
                new BlockPos((int) entity.getX() - searchRadius, (int) entity.getY() - searchRadius, (int) entity.getZ() - searchRadius),
                new BlockPos((int) entity.getX() + searchRadius, (int) entity.getY() + searchRadius, (int) entity.getZ() + searchRadius)
            ),
            this::checkItemOnGround
        ).stream()
            .map(itemEntity -> itemEntity.getBlockPos())
            .sorted(Comparator.comparingDouble(pos -> pos.getSquaredDistance(entityPos)))
            .findFirst();

        if (nearestItem.isEmpty()) {
            return false;
        }

        return startWalking(entity, nearestItem.get());
    }

    public Boolean checkItemOnGround(ItemEntity itemEntity) {
        return itemTagsToPick.stream().anyMatch(tag -> itemEntity.getStack().isIn(tag))
            || itemsToPick.stream().anyMatch(item -> itemEntity.getStack().getItem().equals(item));
    }

}
