package name.giacomofurlan.woodsman.brain.task;

import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public abstract class WalkableActivator implements IActivator {
    float walkSpeed;

    Path currentPath;

    Vec3d lastKnownPos;


    public WalkableActivator(float walkSpeed) {
        this.walkSpeed = walkSpeed;
        this.lastKnownPos = null;
    }

    protected boolean walkRoutine(VillagerEntity entity) {
        if (currentPath == null) {
            return false;
        }

        if (currentPath.isFinished()) {
            return false;
        }

        if (lastKnownPos != null && lastKnownPos == entity.getPos()) {
            // Possibly stuck
            currentPath = entity.getNavigation().findPathTo(currentPath.getEnd().getBlockPos(), 0);
        }

        entity.getNavigation().startMovingAlong(currentPath, walkSpeed);

        return true;
    }

    protected boolean startWalking(VillagerEntity entity, BlockPos pos) {
        currentPath = entity.getNavigation().findPathTo(pos, 1);

        return currentPath != null;
    }

    protected void stopWalking(VillagerEntity entity) {
        if (entity.isNavigating()) {
            entity.getNavigation().stop();
        }

        currentPath = null;
    }

    protected boolean isFollowingPath() {
        return currentPath != null;
    }
    
}
