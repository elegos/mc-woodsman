package name.giacomofurlan.woodsman.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import name.giacomofurlan.woodsman.villager.task.WoodsmanWorkTask;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.item.Item;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class NearestElements {
    public static int INTERACTION_MAHNATTAN_DISTANCE = 6;

    public static Optional<BlockPos> getNearestDroppedItemByTag(Entity entity, BlockPos opCenter, int searchRadius, int operativeDistance, boolean needsToBeReachable, TagKey<Item> tag) {
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

        mainLoop:
        for (int distance = 0; distance <= searchRadius; distance++) {
            for (BlockPos pos : cubicCoordinatesFromCenter(startX, startY, startZ, distance)) {
                // Within local search radius, but outside operative distance
                if (pos.getManhattanDistance(opCenter) > operativeDistance) {
                    break mainLoop;
                }

                if (isTaggedItemAtPosition(world, pos, tag)) {
                    if (candidate == null || pos.getSquaredDistance(startingPos) < candidate.getSquaredDistance(startingPos)) {
                        candidate = pos;
                    }
                }
            }

            Path pathToCandidate = candidate != null
                ? ((PathAwareEntity) entity).getNavigation().findPathTo(candidate.getX(), candidate.getY(), candidate.getZ(), 0)
                : null;

            if (
                candidate != null
                && (
                    !needsToBeReachable
                    || pathToCandidate != null && pathToCandidate.getManhattanDistanceFromTarget() == 0
                )
            ) {
                result = Optional.of(candidate);

                // Stop the search, as all the other trees will be more distant
                break;
            }
        }

        return result;
    }

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
        return getNearestTree(entity, opCenter, searchRadius, operativeDistance, needsToBeReachable, false);
    }

    public static Optional<BlockPos> getNearestTree(Entity entity, BlockPos opCenter, int searchRadius, int operativeDistance, boolean needsToBeReachable, boolean includeSaplings) {
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

        mainLoop:
        for (int distance = 0; distance <= searchRadius; distance++) {
            for (BlockPos pos : cubicCoordinatesFromCenter(startX, startY, startZ, distance)) {
                // Within local search radius, but outside operative distance
                if (pos.mutableCopy().setY(0).getManhattanDistance(opCenter.mutableCopy().setY(0)) > operativeDistance) {
                    break mainLoop;
                }

                if (isTreeAtPosition(world, pos) && (!includeSaplings || isTaggedBlockAtPosition(world, pos, BlockTags.SAPLINGS))) {
                    if (candidate == null || pos.getSquaredDistance(startingPos) < candidate.getSquaredDistance(startingPos)) {
                        candidate = pos;
                    }
                }
            }

            if (
                candidate != null
                // && (
                //     !needsToBeReachable
                //     || ((PathAwareEntity) entity).getNavigation().startMovingTo(candidate.getX(), candidate.getY(), candidate.getZ(), WoodsmanWorkTask.WALK_SPEED)
                // )
            ) {
                result = Optional.of(candidate);

                // Stop the search, as all the other trees will be more distant
                break;
            }
        }

        return result;
    }

    // Search for the nearest empty space from at least minSearchRadius mahnattan distance
    public static Optional<BlockPos> getNearestFreePos(Entity entity, BlockPos opCenter, int minSearchRadius, int operativeDistance, boolean needsToBeReachable) {
        Optional<BlockPos> result = Optional.empty();
        if (entity == null) {
            return result;
        }

        int searchRadius = opCenter.getManhattanDistance(entity.getBlockPos());

        Vec3d startingPos = entity.getPos();
        World world = entity.getWorld();

        int startX = (int)startingPos.getX();
        int startY = (int)startingPos.getY();
        int startZ = (int)startingPos.getZ();

        BlockPos candidate = null;

        for (int distance = minSearchRadius; distance <= searchRadius; distance++) {
            for (BlockPos pos : cubicCoordinatesFromCenter(startX, startY, startZ, distance)) {
                // Within local search radius, but outside operative distance
                if (pos.getManhattanDistance(opCenter) > operativeDistance) {
                    continue;
                }

                if (world.getBlockState(pos).isAir()) {
                    if (candidate == null || pos.getSquaredDistance(startingPos) < candidate.getSquaredDistance(startingPos)) {
                        candidate = pos;
                    }
                }
            }

            if (
                candidate != null
                && (
                    !needsToBeReachable
                    || ((PathAwareEntity) entity).getNavigation().startMovingTo(candidate.getX(), candidate.getY(), candidate.getZ(), WoodsmanWorkTask.WALK_SPEED)
                )
            ) {
                result = Optional.of(candidate);

                // Stop the search, as all the other trees will be more distant
                break;
            }
        }

        // TODO check if it is reachable by path

        return result;
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

    public static Optional<BlockPos> nearestTaggedBlockInReach(World world, BlockPos pos, TagKey<Block> tag) {
        Optional<BlockPos> result = Optional.empty();
        int resultManhattanDistance = Integer.MAX_VALUE;

        for (int radius = 0; radius <= INTERACTION_MAHNATTAN_DISTANCE; radius++) {
            List<BlockPos> blocks = cubicCoordinatesFromCenter(pos.getX(), pos.getY(), pos.getZ(), radius);
            for (BlockPos candidatePos : blocks) {
                if (candidatePos.getManhattanDistance(pos) > INTERACTION_MAHNATTAN_DISTANCE) {
                    continue;
                }

                if (!world.getBlockState(candidatePos).isIn(tag)) {
                    continue;
                }

                int distance = candidatePos.getManhattanDistance(pos);
                if (result.isEmpty() || distance < resultManhattanDistance) {
                    result = Optional.of(candidatePos);
                    resultManhattanDistance = distance;
                }
            }

            if (result.isPresent()) {
                return result;
            }
        }

        return result;
    }

    public static boolean isTreeAtPosition(World world, BlockPos pos) {
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

    public static boolean isTaggedBlockAtPosition(World world, BlockPos pos, TagKey<Block> tag) {
        return world.getBlockState(pos).isIn(tag);
    }

    public static boolean isTaggedItemAtPosition(World world, BlockPos pos, TagKey<Item> tag) {
        List<ItemEntity> items = world.getEntitiesByClass(
            ItemEntity.class,
            new Box(pos), (itemEntity) -> itemEntity.getStack().isIn(tag)
        );

        return items.size() > 0;
    }
}
