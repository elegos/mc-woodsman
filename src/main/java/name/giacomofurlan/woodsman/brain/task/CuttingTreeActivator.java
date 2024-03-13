package name.giacomofurlan.woodsman.brain.task;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import name.giacomofurlan.woodsman.util.WorldUtil;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class CuttingTreeActivator implements IActivator {
    int searchRadius;
    float walkSpeed;

    List<BlockPos> currentLogsToCut;
    BlockPos targetCutPos;
    

    public CuttingTreeActivator(int searchRadius, float walkSpeed) {
        this.currentLogsToCut = new ArrayList<>();
        this.targetCutPos = null;

        this.searchRadius = searchRadius;
        this.walkSpeed = walkSpeed;
    }


    @Override
    public boolean run(VillagerEntity entity, Brain<VillagerEntity> brain) {
        // if (entity.isNavigating()) {
        //     return false;
        // }

        if (currentLogsToCut.isEmpty()) {
            Optional<List<BlockPos>> nearestTree = findNearestTree(entity, searchRadius);

            // No target tree, no tree found
            if (nearestTree.isEmpty()) {
                return false;
            }

            this.currentLogsToCut = new ArrayList<>(nearestTree.get());
        }

        if (targetCutPos == null) {
            Path pathToTree = entity.getNavigation().findPathTo(currentLogsToCut.get(0), 1);
            targetCutPos = pathToTree.getEnd().getBlockPos();
        }

        if (!targetCutPos.equals(entity.getBlockPos())) {
            entity.getNavigation().startMovingTo(targetCutPos.getX(), targetCutPos.getY(), targetCutPos.getZ(), walkSpeed);

            return true;
        }

        // Reached the target tree
        World world = entity.getWorld();
        Deque<BlockPos> queue = new ArrayDeque<>(currentLogsToCut);
        while (!queue.isEmpty()) {
            BlockPos targetPos = queue.removeFirst();
            
            if (!world.getBlockState(targetPos).isIn(BlockTags.LOGS_THAT_BURN)) {
                // Log is not in the world anymore
                currentLogsToCut.remove(targetPos);

                continue;
            }

            // Break the first log and return

            world.breakBlock(targetPos, true);
            currentLogsToCut.remove(targetPos);

            if (currentLogsToCut.isEmpty()) {
                targetCutPos = null;
            }
            
            return true;
        }

        return false;
    }

    protected Optional<List<BlockPos>> findNearestTree(VillagerEntity entity, int searchRadius) {
        World world = entity.getWorld();
        BlockPos entityPos = entity.getBlockPos();

        List<BlockPos> logsWithinReach = WorldUtil.getBlockPos(WorldUtil.cubicBoxFromCenter(entityPos, searchRadius), true)
            .stream()
            .filter(pos -> world.getBlockState(pos).isIn(BlockTags.LOGS_THAT_BURN))
            .limit(5)
            .toList();

        for (BlockPos logPos : logsWithinReach) {
            Optional<Set<BlockPos>> tree = WorldUtil.getTreeLogsPosFromAnyLog(world, logPos);

            if (tree.isPresent()) {
                return Optional.of(tree.get().stream().sorted(Comparator.comparingDouble(pos -> pos.getY())).toList());
            }
        }

        return Optional.empty();
    }
}
