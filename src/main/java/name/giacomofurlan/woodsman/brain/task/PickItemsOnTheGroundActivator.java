package name.giacomofurlan.woodsman.brain.task;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import net.minecraft.entity.Entity.RemovalReason;
import net.minecraft.entity.ItemEntity;
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
    public boolean shouldRun(VillagerEntity entity) {
        return false;
    }

    @Override
    public boolean run(VillagerEntity entity) {
        World world = entity.getWorld();
        BlockPos entityPos = entity.getBlockPos();

        // Pick any item during the course of action
        world.getEntitiesByClass(
            ItemEntity.class,
            getReachBox(entityPos),
            this::checkItemOnGround
        ).forEach(itemEntity -> {
            entity.getInventory().addStack(itemEntity.getStack()).toString();
            itemEntity.remove(RemovalReason.DISCARDED);
        });

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
            .filter(
                pos -> getReachBox(entity.getNavigation().findPathTo(pos, 0).getEnd().getBlockPos())
                    .contains((double) pos.getX(), (double) pos.getY(), (double) pos.getZ())
            )
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

    protected Box getReachBox(BlockPos pos) {
        return Box.enclosing(pos.down(3).west(2).north(2), pos.up(6).east(2).south(2));
    }
}
