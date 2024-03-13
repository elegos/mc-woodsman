package name.giacomofurlan.woodsman.util;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.IntStream;

import net.minecraft.block.BlockState;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.world.World;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

public class WorldUtil {
    /**
     * @see WorldUtil.cubicCoordinatesFromCenter
     */
    public static List<BlockPos> cubicCoordinatesFromCenter(Vec3d center, int distance) {
        return cubicCoordinatesFromCenter(new Vec3i((int) center.getX(), (int) center.getY(), (int) center.getZ()), distance);
    }

    /**
     * Generates a list of BlockPos representing the coordinates of a cube centered at (center) with the given distance.
     *
     * @param  center   the center's coordinate
     * @param  distance the distance from the center to the edges of the cube
     * @return          the list of BlockPos representing the cube coordinates
     */
    public static List<BlockPos> cubicCoordinatesFromCenter(Vec3i center, int distance) {
        ArrayList<BlockPos> result = new ArrayList<>();
        if (distance == 0) {
            result.add(new BlockPos(center.getX(), center.getY(), center.getZ()));

            return result;
        }

        int minX = center.getX() - distance;
        int maxX = center.getX() + distance;

        int minY = center.getY() - distance;
        int maxY = center.getY() + distance;

        int minZ = center.getZ() - distance;
        int maxZ = center.getZ() + distance;

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

    /**
     * Generate the bounding box of a cube centered at (center) with the given distance.
     * @param center
     * @param distance
     * @return
     */
    public static Box cubicBoxFromCenter(Vec3i center, int distance) {
        int minX = center.getX() - distance;
        int minY = center.getY() - distance;
        int minZ = center.getZ() - distance;
        int maxX = center.getX() + distance;
        int maxY = center.getY() + distance;
        int maxZ = center.getZ() + distance;

        return new Box(minX, minY, minZ, maxX, maxY, maxZ);
    }

    protected static void sortIntegerListFromMiddleToEnds(List<Integer> list) {
        ArrayList<Integer> listCopy = new ArrayList<>(list);
        list.clear();
        while (!listCopy.isEmpty()) {
            int index = (int) Math.floor(listCopy.size() / 2);
            list.add(listCopy.get(index));
            listCopy.remove(index);
        }
    }

    public static List<BlockPos> getBlockPos(Box box, Boolean fromCenter) {
        List<BlockPos> result = new ArrayList<>();

        List<Integer> xList, yList, zList;

        xList = new ArrayList<>(IntStream.rangeClosed((int) box.getMin(Axis.X), (int)box.getMax(Axis.X)).boxed().toList());
        yList = new ArrayList<>(IntStream.rangeClosed((int) box.getMin(Axis.Y), (int)box.getMax(Axis.Y)).boxed().toList());
        zList = new ArrayList<>(IntStream.rangeClosed((int) box.getMin(Axis.Z), (int)box.getMax(Axis.Z)).boxed().toList());

        if (fromCenter) {
            sortIntegerListFromMiddleToEnds(xList);
            sortIntegerListFromMiddleToEnds(yList);
            sortIntegerListFromMiddleToEnds(zList);
        }

        for (int x : xList) {
            for (int y : yList) {
                for (int z : zList) {
                    result.add(new BlockPos(x, y, z));
                }
            }
        }

        return result;
    }

    public static List<BlockPos> getBlockPos(Box box) {
        return getBlockPos(box, false);

        // List<BlockPos> result = new ArrayList<>();

        // for (int x = (int) box.getMin(Axis.X); x < (int) box.getMax(Axis.X); x++) {
        //     for (int y = (int) box.getMin(Axis.Y); y < (int) box.getMax(Axis.Y); y++) {
        //         for (int z = (int) box.getMin(Axis.Z); z < (int) box.getMax(Axis.Z); z++) {
        //             result.add(new BlockPos(x, y, z));                    
        //         }
        //     }
        // }

        // return result;
    }

    public static int get2DManhattanDistance(BlockPos a, BlockPos b) {
        return a.mutableCopy().setY(0).getManhattanDistance(b.mutableCopy().setY(0));
    }

    /**
     * Retrieves the positions of all logs connected to the given log position, if any of them is connected to leaves.
     *
     * @param  world   the world in which the log is located
     * @param  logPos  the position of the log
     * @return         an Optional containing a Set of BlockPos if leaves are found, otherwise an empty Optional
     */
    public static Optional<Set<BlockPos>> getTreeLogsPosFromAnyLog(World world, BlockPos logPos) {
        Set<BlockPos> result = new HashSet<>();
        Set<BlockPos> visited = new HashSet<>();
        Deque<BlockPos> queue = new ArrayDeque<>();

        Boolean foundLeaves = false;

        // Add the initial log
        queue.offer(logPos);

        while (!queue.isEmpty()) {
            BlockPos currentPos = queue.removeFirst();
            visited.add(currentPos);

            BlockState state = world.getBlockState(currentPos);
            foundLeaves = foundLeaves || state.isIn(BlockTags.LEAVES);

            if (state.isIn(BlockTags.LOGS_THAT_BURN)) {
                result.add(currentPos);
                queue.addAll(
                    getBlockPos(new Box(currentPos).expand(1))
                        .stream()
                        .filter(pos -> !visited.contains(pos))
                        .toList()
                );
            }
        }

        return foundLeaves ? Optional.of(result) : Optional.empty();
    }
}
