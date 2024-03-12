package name.giacomofurlan.woodsman.brain.task;

import java.util.Optional;

import name.giacomofurlan.woodsman.brain.ModMemoryModuleType;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.LookTarget;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;

public class MoveToLookTargetActivator implements IActivator {
    protected int itemMinManhattanDistance;
    protected int blockToInteractWithMinManhattanDistance;
    protected int maxManhattanDistance;
    protected float walkSpeed;

    public MoveToLookTargetActivator(
        int itemMinManhattanDistance, int blockToInteractWithMinManhattanDistance, int maxManhattanDistance, float walkSpeed
    ) {
        this.itemMinManhattanDistance = itemMinManhattanDistance;
        this.blockToInteractWithMinManhattanDistance = blockToInteractWithMinManhattanDistance;

        this.maxManhattanDistance = maxManhattanDistance;
        this.walkSpeed = walkSpeed;
    }

    @Override
    public boolean run(VillagerEntity entity, Brain<VillagerEntity> brain) {
        if (entity.isNavigating()) {
            return true;
        }

        Optional<GlobalPos> jobSite = brain.getOptionalMemory(MemoryModuleType.JOB_SITE);
        Optional<LookTarget> lookTarget = brain.getOptionalMemory(ModMemoryModuleType.LOOK_TARGET);

        if (
            jobSite.isEmpty()
            || lookTarget.isEmpty()
        ) {
            return false;
        }

        // Path currentPath = entity.getNavigation().getCurrentPath();
        // if (currentPath != null && currentPath.getTarget().getManhattanDistance(entity.getBlockPos()) < maxManhattanDistance) {
        //     return false;
        // }

        BlockPos lookPos = lookTarget.get().getBlockPos();
        if (lookPos.getManhattanDistance(entity.getBlockPos()) < itemMinManhattanDistance) {
            return false;
        }

        Path pathToFollow = entity.getNavigation().findPathTo(lookPos, 1);
        if (
            pathToFollow == null
            || pathToFollow.getEnd().getBlockPos().getManhattanDistance(entity.getBlockPos()) <= 1
        ) {
            brain.forget(ModMemoryModuleType.LOOK_TARGET);
            brain.forget(MemoryModuleType.LOOK_TARGET);

            return false;
        }

        entity.getNavigation().startMovingAlong(pathToFollow, walkSpeed);

        return true;
    }

}
