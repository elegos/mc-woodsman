package name.giacomofurlan.woodsman.brain.task;

import java.util.Optional;

import name.giacomofurlan.woodsman.brain.ModMemoryModuleType;
import name.giacomofurlan.woodsman.brain.WoodsmanWorkTask;
import name.giacomofurlan.woodsman.util.NearestElements;
import net.minecraft.entity.ai.brain.BlockPosLookTarget;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;

public class MoveToTreeActivator implements IActivator {
    protected int searchRadius;
    protected int operativeRadius; // from the chop block

    public MoveToTreeActivator(int searchRadius, int operativeRadius) {
        this.searchRadius = searchRadius;
        this.operativeRadius = operativeRadius;
    }


    @Override
    public boolean run(VillagerEntity entity, Brain<VillagerEntity> brain) {
        if (entity.isNavigating()) {
            return false;
        }

        Optional<GlobalPos> jobSitePos = brain.getOptionalMemory(MemoryModuleType.JOB_SITE);
        if (jobSitePos.isEmpty()) {
            return false;
        }

        Optional<BlockPos> nearestTreeBlock = NearestElements.getNearestTree(entity, searchRadius, jobSitePos.get().getPos(), operativeRadius);
        // Optional<BlockPos> nearestTreeBlock = NearestElements.getNearestTree(entity, searchRadius, true);

        if (nearestTreeBlock.isEmpty()) {
            return false;
        }

        BlockPos treeBlockPos = nearestTreeBlock.get();

        brain.remember(MemoryModuleType.LOOK_TARGET, new BlockPosLookTarget(treeBlockPos));
        
        if (treeBlockPos.getManhattanDistance(entity.getBlockPos()) <= WoodsmanWorkTask.MAX_INTERACTION_MANHATTAN_DISTANCE) {
            brain.remember(ModMemoryModuleType.LOOK_TARGET, new BlockPosLookTarget(treeBlockPos));

            return true;
        }

        return false;
    }

}
