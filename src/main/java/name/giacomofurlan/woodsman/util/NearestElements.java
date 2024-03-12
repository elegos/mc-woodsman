package name.giacomofurlan.woodsman.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class NearestElements {
    public static int INTERACTION_MAHNATTAN_DISTANCE = 6;

    public static Optional<BlockPos> getNearestTree(Entity entity, int searchRadius, BlockPos opCenterPos, int maxOperativeDistance) {
        return getNearestTree(entity, searchRadius, opCenterPos, maxOperativeDistance, false);
    }

    public static Optional<BlockPos> getNearestTree(Entity entity, int searchRadius, BlockPos opCenterPos, int maxOperativeDistance, Boolean includeSaplings) {
        World world = entity.getWorld();
        BlockPos entityPos = entity.getBlockPos();
        Box searchBox = WorldUtil.cubicBoxFromCenter(entityPos, searchRadius);

        return WorldUtil.getBlockPos(searchBox)
            .stream()
            .filter(pos -> {
                // TODO search for leaves (actual tree and not house tree)
                if (pos.getSquaredDistance(opCenterPos) > maxOperativeDistance) {
                    return false;
                }

                BlockState blockState = world.getBlockState(pos);

                return blockState.isIn(BlockTags.LOGS_THAT_BURN) || (includeSaplings && blockState.isIn(BlockTags.SAPLINGS));
            })
            .sorted(Comparator.comparingDouble((BlockPos pos) -> pos.getSquaredDistance(entityPos)))
            .findFirst();
    }

    /**
     * Attempt to find the nearest tree from the given entity.
     * @param entity the entity which is searching for the nearest tree.
     * @param searchRadius The search radius the entity searches within.
     * @param needsToBeReachable if true, the candidate needs to be reachable from a path point of view.
     * @return 
     */
    public static Optional<BlockPos> getNearestTree(Entity entity, int searchRadius, boolean needsToBeReachable) {
        return getNearestTree(entity, searchRadius, needsToBeReachable, false);
    }

    public static Optional<BlockPos> getNearestTree(Entity entity, int searchRadius, boolean needsToBeReachable, boolean includeSaplings) {
        if (entity == null) {
            return Optional.empty();
        }

        Vec3d startingPos = entity.getPos();
        World world = entity.getWorld();

        int startX = (int)startingPos.getX();
        int startY = (int)startingPos.getY();
        int startZ = (int)startingPos.getZ();

        List<BlockPos> candidatePos = new ArrayList<>();
        for (int x = startX - searchRadius; x <= startX + searchRadius; x++) {
            for (int y = startY - searchRadius; y <= startY + searchRadius; y++) {
                for (int z = startZ - searchRadius; z <= startZ + searchRadius; z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    if (world.getBlockState(pos).isIn(BlockTags.LOGS_THAT_BURN)) {
                        candidatePos.add(pos);
                    }
                }
            }
        }

        for (int distance = 0; distance <= searchRadius; distance++) {
            Optional<BlockPos> optCandidate = WorldUtil.cubicCoordinatesFromCenter(startingPos, distance)
                .stream()
                .filter(pos -> {
                    if (!candidatePos.contains(pos)) {
                        return false;
                    }

                    // Path path = ((PathAwareEntity) entity).getNavigation().findPathTo(pos, 0, INTERACTION_MAHNATTAN_DISTANCE);

                    // return path != null && path.reachesTarget();

                    return true;
                })
                .findFirst();
            
            if (optCandidate.isPresent()) {
                return optCandidate;
            }
        }

        return Optional.empty();
    }
}
