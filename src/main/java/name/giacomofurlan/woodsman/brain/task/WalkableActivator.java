package name.giacomofurlan.woodsman.brain.task;

import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.util.math.BlockPos;

public abstract class WalkableActivator implements IActivator {
    float walkSpeed;

    Path currentPath;


    public WalkableActivator(float walkSpeed) {
        this.walkSpeed = walkSpeed;
    }

    protected boolean walkRoutine(VillagerEntity entity) {
        if (currentPath == null) {
            return false;
        }

        if (currentPath.isFinished()) {
            return false;
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
