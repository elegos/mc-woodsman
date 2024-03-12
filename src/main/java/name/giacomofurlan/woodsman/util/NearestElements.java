package name.giacomofurlan.woodsman.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import name.giacomofurlan.woodsman.brain.WoodsmanWorkTask;
import net.minecraft.block.Block;
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

    public static Optional<BlockPos> getNearestDroppedItemByTag(Entity entity, int searchRadius, boolean needsToBeReachable, List<TagKey<Item>> tags) {
        if (entity == null) {
            return Optional.empty();
        }

        Vec3d startingPos = entity.getPos();
        World world = entity.getWorld();

        int startX = (int)startingPos.getX();
        int startY = (int)startingPos.getY();
        int startZ = (int)startingPos.getZ();

        List<BlockPos> foundItemPositions = world.getEntitiesByClass(
            ItemEntity.class,
            Box.enclosing(
                new BlockPos(startX - searchRadius, startY - searchRadius, startZ - searchRadius),
                new BlockPos(startX + searchRadius, startY + searchRadius, startZ + searchRadius)
            ),
            itemEntity -> tags.parallelStream().reduce(false, (acc, val) -> acc || itemEntity.getStack().isIn(val), (acc, val) -> acc || val)
        ).stream().map(itemEntity -> itemEntity.getBlockPos()).toList();

        if (foundItemPositions.size() == 0) {
            return Optional.empty();
        }

        for (int distance = 0; distance <= searchRadius; distance++) {
            try {
                Optional<BlockPos> optCandidate = cubicCoordinatesFromCenter(startX, startY, startZ, distance)
                    .parallelStream()
                    .filter(pos -> {
                        if (!foundItemPositions.contains(pos)) {
                            return false;
                        }

                        Path path = ((PathAwareEntity) entity).getNavigation().findPathTo(pos, 0);

                        return path != null && path.reachesTarget();
                    })
                    .findFirst();
                
                if (optCandidate.isPresent()) {
                    return optCandidate;
                }
            } catch (Exception ex) {
                // .findFirst() will raise NPE if the stream is empty
                continue;
            }
        }

        return Optional.empty();
    }

    /**
     * Attempt to find the nearest tree from the given entity.
     * @param entity the entity which is searching for the nearest tree.
     * @param searchRadius The search radius the entity searches within.
     * @param operativeDistance The operative (manhattan) distance from the opCenter the villager works within.
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
            Optional<BlockPos> optCandidate = cubicCoordinatesFromCenter(startX, startY, startZ, distance)
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

    public static boolean isTreeAtPosition(World world, BlockPos pos) {
        // TODO refactor

        return world.getBlockState(pos).isIn(BlockTags.LOGS_THAT_BURN);
    }

    public static boolean isTaggedBlockAtPosition(World world, BlockPos pos, TagKey<Block> tag) {
        return world.getBlockState(pos).isIn(tag);
    }

    public static boolean isTaggedItemAtPosition(World world, BlockPos pos, TagKey<Item> tag) {
        List<ItemEntity> items = world.getEntitiesByClass(
            ItemEntity.class,
            new Box(pos),
            (itemEntity) -> itemEntity.getStack().isIn(tag)
        );

        return items.size() > 0;
    }
}
