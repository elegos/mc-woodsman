package name.giacomofurlan.woodsman.brain.task;

import java.util.Optional;

import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public abstract class WalkableActivator implements IActivator {
    float walkSpeed;

    Vec3d lastKnownPos;


    public WalkableActivator(float walkSpeed) {
        this.walkSpeed = walkSpeed;
        this.lastKnownPos = null;
    }

    protected boolean walkRoutine(VillagerEntity entity) {
        EntityNavigation navigation = entity.getNavigation();
        Path currentPath = navigation.getCurrentPath();

        if (currentPath == null) {
            return false;
        }

        if (hasArrived(entity)) {
            lastKnownPos = null;

            return false;
        }

        if (lastKnownPos != null && lastKnownPos == entity.getPos()) {
            // Possibly stuck
            navigation.recalculatePath();
        }

        lastKnownPos = entity.getPos();

        if (!navigation.isFollowingPath()) {
            navigation.tick();
        }

        return true;
    }

    protected boolean startWalking(VillagerEntity entity, BlockPos pos) {
        Path path = entity.getNavigation().findPathTo(pos, 1);

        if (path != null && path.getEnd().getBlockPos().equals(entity.getBlockPos())) {
            return true;
        }

        return entity.getNavigation().startMovingAlong(path, walkSpeed);
    }

    public boolean hasArrived(VillagerEntity entity) {
        Path currentPath = entity.getNavigation().getCurrentPath();

        return currentPath == null
            || currentPath.isFinished()
            || currentPath.getEnd().getBlockPos().equals(entity.getBlockPos());
    }

    protected void stopWalking(VillagerEntity entity) {
        EntityNavigation navigation = entity.getNavigation();

        if (entity.isNavigating()) {
            navigation.stop();
        }
    }
    
    protected Optional<BlockPos> getWalkTarget(VillagerEntity entity) {
        BlockPos targetPos = entity.getNavigation().getTargetPos();

        return targetPos != null
            ? Optional.of(targetPos)
            : Optional.empty();
    }
}
