package name.giacomofurlan.woodsman.villager.task.activator;

import java.util.Optional;

import name.giacomofurlan.woodsman.util.NearestElements;
import name.giacomofurlan.woodsman.villager.task.WoodsmanWorkTask;
import net.minecraft.entity.ai.brain.BlockPosLookTarget;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.WalkTarget;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.util.math.BlockPos;

public class MoveToTreeActivator implements IActivator {
    @Override
    public boolean run(VillagerEntity entity, Brain<VillagerEntity> brain) {
        if (entity.isNavigating()) {
            return false;
        }

        Optional<BlockPos> nearestTreeBlock = NearestElements.getNearestTree(entity, WoodsmanWorkTask.SEARCH_RADIUS, true);

        if (nearestTreeBlock.isEmpty()) {
            return false;
        }

        BlockPos treeBlockPos = nearestTreeBlock.get();

        // brain.remember(MemoryModuleType.WALK_TARGET, new WalkTarget(treeBlockPos, WoodsmanWorkTask.WALK_SPEED, 1));
        brain.remember(MemoryModuleType.LOOK_TARGET, new BlockPosLookTarget(treeBlockPos));

        if (treeBlockPos.getManhattanDistance(entity.getBlockPos()) <= WoodsmanWorkTask.MAX_INTERACTION_MANHATTAN_DISTANCE) {
            return false;
        }

        return true;
    }

}
