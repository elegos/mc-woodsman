package name.giacomofurlan.woodsman.villager.task.activator;

import java.util.List;
import java.util.Optional;

import name.giacomofurlan.woodsman.Woodsman;
import name.giacomofurlan.woodsman.util.NearestElements;
import name.giacomofurlan.woodsman.villager.task.WoodsmanWorkTask;
import net.minecraft.entity.ai.brain.BlockPosLookTarget;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.WalkTarget;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.item.Item;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;

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

        // Position of the chop block bound to
        GlobalPos jobSitePos = entity.getBrain().getOptionalRegisteredMemory(MemoryModuleType.JOB_SITE).get();

        Woodsman.LOGGER.info("Searching for items with tags: {}", this.tags);
        Optional<BlockPos> nearestItem = NearestElements.getNearestDroppedItemByTag(entity, jobSitePos.getPos(), searchRadius, WoodsmanWorkTask.OP_DISTANCE, true, this.tags);
        Woodsman.LOGGER.info("Nearest item: {}", nearestItem);

        if (nearestItem.isEmpty()) {
            return false;
        }

        BlockPos itemBlockPos = nearestItem.get();

        brain.remember(MemoryModuleType.WALK_TARGET, new WalkTarget(itemBlockPos, WoodsmanWorkTask.WALK_SPEED, 0));
        brain.remember(MemoryModuleType.LOOK_TARGET, new BlockPosLookTarget(itemBlockPos));

        return true;
    }

}
