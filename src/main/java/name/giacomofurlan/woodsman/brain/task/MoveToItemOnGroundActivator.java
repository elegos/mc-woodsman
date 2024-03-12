package name.giacomofurlan.woodsman.brain.task;

import java.util.List;
import java.util.Optional;

import name.giacomofurlan.woodsman.brain.ModMemoryModuleType;
import name.giacomofurlan.woodsman.util.NearestElements;
import net.minecraft.entity.ai.brain.BlockPosLookTarget;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.WalkTarget;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.item.Item;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.math.BlockPos;

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

        Optional<BlockPos> nearestItem = NearestElements.getNearestDroppedItemByTag(entity, searchRadius, true, this.tags);

        if (nearestItem.isEmpty()) {
            return false;
        }

        BlockPos itemBlockPos = nearestItem.get();

        brain.remember(ModMemoryModuleType.LOOK_TARGET, new BlockPosLookTarget(itemBlockPos));

        return true;
    }

}