package name.giacomofurlan.woodsman.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class NearestElements {
    /**
     * Attempt to find the nearest tree from the given entity.
     * @param entity the entity which is searching for the nearest tree.
     * @param opCenter the center of operations. Search radius refers to the opCenter.
     * @param searchRadius The search radius the entity searches within.
     * @param operativeDistance The operative (manhattan) distance from the opCenter the villager works within.
     * @param needsToBeReachable if true, the candidate needs to be reachable from a path point of view.
     * @return 
     */
    public static Optional<BlockPos> getNearestTree(Entity entity, BlockPos opCenter, int searchRadius, int operativeDistance, boolean needsToBeReachable) {
        Optional<BlockPos> result = Optional.empty();
        if (entity == null) {
            return result;
        }

        Vec3d startingPos = entity.getPos();
        World world = entity.getWorld();

        int startX = (int)startingPos.getX();
        int startY = (int)startingPos.getY();
        int startZ = (int)startingPos.getZ();

        BlockPos candidate = null;

        for (int distance = 0; distance <= searchRadius; distance++) {
            for (BlockPos pos : cubicCoordinatesFromCenter(startX, startY, startZ, distance)) {
                // Within local search radius, but outside operative distance
                if (pos.getManhattanDistance(opCenter) > operativeDistance) {
                    continue;
                }

                if (isTreeAtPosition(world, pos)) {
                    if (candidate == null || pos.getSquaredDistance(startingPos) < candidate.getSquaredDistance(startingPos)) {
                        candidate = pos;
                    }
                }
            }

            if (candidate != null) {
                result = Optional.of(candidate);

                // Stop the search, as all the other trees will be more distant
                break;
            }
        }

        return result;
    }

    protected static boolean isTreeAtPosition(World world, BlockPos pos) {
        // TODO refactor
        int numLogsInColumn = 0;
        int numLeaves = 0;

        if (!world.getBlockState(pos).isIn(BlockTags.LOGS_THAT_BURN)) {
            return false;
        }

        // Search for the logs, in a vertical column
        for (int i = -3; i <= 3; i++) {
            BlockState blockState = world.getBlockState(pos.mutableCopy().add(0, i, 0));
            if (blockState.isIn(BlockTags.LOGS_THAT_BURN)) {
                numLogsInColumn += 1;
            }
        }

        return numLogsInColumn > 1;
    }

    public static List<BlockPos> cubicCoordinatesFromCenter(int centerX, int centerY, int centerZ, int distance) {
        ArrayList<BlockPos> result = new ArrayList<>();
        if (distance == 0) {
            result.add(new BlockPos(centerX, centerY, centerZ));

            return result;
        }

        int minX = centerX - distance;
        int maxX = centerX + distance;

        int minY = centerY - distance;
        int maxY = centerY + distance;

        int minZ = centerZ - distance;
        int maxZ = centerZ + distance;

        for (int x = minX; x <= maxX; x++) {
            // XY plane
            for (int y = minY; y <= maxY; y++) {
                result.add(new BlockPos(x, y, minZ));
                result.add(new BlockPos(x, y, maxZ));
            }
            // XZ plane
            for (int z = minZ; z <= maxZ; z++) {
                result.add(new BlockPos(x, minY, z));
                result.add(new BlockPos(x, maxY, z));
            }
        }

        // YZ plane
        for (int y = minY; y <= maxY; y++) {
            for (int z = minZ; z <= maxZ; z++) {
                result.add(new BlockPos(minX, y, z));
                result.add(new BlockPos(maxX, y, z));
            }
        }

        return result;
    }
}
