package name.giacomofurlan.woodsman.villager.task.activator;

import java.util.Optional;

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
    TagKey<Item> tag;

    public MoveToItemOnGroundActivator(TagKey<Item> tag) {
        this.tag = tag;
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

        Optional<BlockPos> nearestItem = NearestElements.getNearestDroppedItemByTag(entity, jobSitePos.getPos(), WoodsmanWorkTask.OP_DISTANCE, WoodsmanWorkTask.OP_DISTANCE, true, this.tag);
        // Woodsman.LOGGER.info("Nearest dropped {} pos: {}", this.tag.toString(), nearestItem.isPresent() ? nearestItem.get().toShortString() : "null");

        if (nearestItem.isEmpty()) {
            return false;
        }

        BlockPos itemBlockPos = nearestItem.get();

        brain.remember(MemoryModuleType.WALK_TARGET, new WalkTarget(itemBlockPos, WoodsmanWorkTask.WALK_SPEED, 0));
        brain.remember(MemoryModuleType.LOOK_TARGET, new BlockPosLookTarget(itemBlockPos));

        return true;
    }

}
