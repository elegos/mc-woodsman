package name.giacomofurlan.woodsman.brain.task;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import name.giacomofurlan.woodsman.Woodsman;
import name.giacomofurlan.woodsman.util.WorldUtil;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class CutTreeActivator extends WalkableActivator {
    int searchRadius;

    List<BlockPos> currentLogsToCut;

    public CutTreeActivator(int searchRadius, float walkSpeed) {
        super(walkSpeed);

        this.currentLogsToCut = new ArrayList<>();

        this.searchRadius = searchRadius;
        this.walkSpeed = walkSpeed;
    }

    @Override
    public boolean run(VillagerEntity entity, Brain<VillagerEntity> brain) {
        if (currentLogsToCut.isEmpty()) {
            stopWalking(entity);
            Optional<List<BlockPos>> nearestTree = findNearestTree(entity, searchRadius);

            // No target tree, no tree found
            if (nearestTree.isEmpty()) {
                return false;
            }

            this.currentLogsToCut = new ArrayList<>(nearestTree.get());
        }

        if (!isFollowingPath() && !startWalking(entity, currentLogsToCut.get(0))) {
            // Can't find a path
            // TODO guard condition (to avoid freezing loops)

            Woodsman.LOGGER.debug("No available path for current target tree, removing its reference");
            currentLogsToCut.clear();

            return false;
        }

        if (walkRoutine(entity)) {
            return true;
        }

        // Reached the target tree (nearest position)
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
