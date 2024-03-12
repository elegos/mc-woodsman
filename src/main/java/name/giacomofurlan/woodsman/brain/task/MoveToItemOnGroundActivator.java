package name.giacomofurlan.woodsman.brain.task;

import java.util.List;
import java.util.Optional;

import name.giacomofurlan.woodsman.brain.ModMemoryModuleType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.ai.brain.BlockPosLookTarget;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.WalkTarget;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.item.Item;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

public class MoveToItemOnGroundActivator implements IActivator {
    List<TagKey<Item>> tags;
    int searchRadius;

    public MoveToItemOnGroundActivator(List<TagKey<Item>> tags, int searchRadius) {
        this.tags = tags;
        this.searchRadius = searchRadius;
    }

    @Override
    public boolean run(VillagerEntity entity, Brain<VillagerEntity> brain) {
         Optional<WalkTarget> currentTarget = brain.getOptionalMemory(MemoryModuleType.WALK_TARGET);

        // Another activity is in progress
        if (currentTarget.isPresent()) {
            return false;
        }

        Optional<BlockPos> nearestItem = entity.getWorld().getEntitiesByClass(
            ItemEntity.class,
            Box.enclosing(
                new BlockPos((int) entity.getX() - searchRadius, (int) entity.getY() - searchRadius, (int) entity.getZ() - searchRadius),
                new BlockPos((int) entity.getX() + searchRadius, (int) entity.getY() + searchRadius, (int) entity.getZ() + searchRadius)
            ),
            itemEntity -> tags.parallelStream().reduce(false, (acc, val) -> acc || itemEntity.getStack().isIn(val), (acc, val) -> acc || val)
        ).stream()
            .map(itemEntity -> itemEntity.getBlockPos())
            .reduce(
                (acc, val) ->  acc == null ? val : val.getSquaredDistance(entity.getBlockPos()) > acc.getSquaredDistance(entity.getBlockPos())
                    ? val
                    : acc
            );

        if (nearestItem.isEmpty()) {
            return false;
        }

        BlockPos itemBlockPos = nearestItem.get();

        brain.remember(ModMemoryModuleType.LOOK_TARGET, new BlockPosLookTarget(itemBlockPos));

        return true;
    }

}
