package name.giacomofurlan.woodsman.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import name.giacomofurlan.woodsman.Woodsman;
import name.giacomofurlan.woodsman.datagen.ModBlockTagsProvider;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

public class WorldCache {
    protected static WorldCache instance;

    protected Map<String, Map<BlockPos, BlockState>> stateCache;

    public class CachedBlock {
        protected Optional<BlockState> blockState;

        public CachedBlock(Optional<BlockState> blockState) {
            this.blockState = blockState;
        }

        public boolean isIn(TagKey<Block> blockTags) {
            return blockState.isPresent() && blockState.get().isIn(blockTags);
        }

        public boolean isAir() {
            return blockState.isPresent() && blockState.get().isAir();
        }
    }

    public static WorldCache getInstance() {
        if (instance == null) {
            instance = new WorldCache();
        }
        return instance;
    }

    protected WorldCache() {
        stateCache = new HashMap<>();
    }

    protected String getDimensionKey(World world) {
        return world.getRegistryKey().getValue().toString();
    }
    
    /**
     * Caches all the log/leavesblocks within a specified radius of a center position in the world.
     *
     * @param  world   the world in which the blocks are located
     * @param  center  the center position around which to cache blocks
     * @param  radius  the radius within which to cache blocks
     */
    public void cacheCube(World world, BlockPos center, int radius) {
        String dimension = getDimensionKey(world);

        Woodsman.LOGGER.debug("Caching cube ({} :: {}) of radius {}", dimension, center.toShortString(), radius);
        WorldUtil.getBlockPos(WorldUtil.cubicBoxFromCenter(center, radius), true)
            .forEach((pos) -> {
                // Already cached
                if (stateCache.containsKey(dimension) && stateCache.get(dimension).containsKey(pos)) {
                    return;
                }

                updateCache(dimension, pos, world.getBlockState(pos), true);
            });
        Woodsman.LOGGER.debug("Caching cube ({} :: {}) of radius {} done", dimension, center.toShortString(), radius);
    }

    public void updateCache(String dimension, BlockPos pos, BlockState state, Boolean addIfMissing) {
        if (addIfMissing && !stateCache.containsKey(dimension)) {
            stateCache.put(dimension, new HashMap<>());
        }

        if (!stateCache.containsKey(dimension)) {
            return;
        }

        // -6 -59 -9
        Map<BlockPos, BlockState> dimensionCache = stateCache.get(dimension);
        if (!dimensionCache.containsKey(pos) && !addIfMissing) {
            return;
        }

        if (
            state.isAir()
            || state.isIn(BlockTags.DIRT)
            || state.isIn(BlockTags.LEAVES)
            || state.isIn(BlockTags.SAPLINGS)
            || state.isIn(BlockTags.LOGS_THAT_BURN)
            || state.isIn(ModBlockTagsProvider.STORAGE_BLOCKS)
        ) {
            dimensionCache.put(pos, state);
        } else {
            dimensionCache.remove(pos);
        }
    }

    public CachedBlock getCachedBlock(World world, BlockPos pos) {
        String dimension = getDimensionKey(world);

        if (stateCache.containsKey(dimension) && stateCache.get(dimension).containsKey(pos)) {
            return new CachedBlock(Optional.of(stateCache.get(dimension).get(pos)));
        }

        return new CachedBlock(Optional.empty());
    }

    public List<BlockState> getStatesInBox(World world, Box box) {
        String dimension = getDimensionKey(world);

        if (!stateCache.containsKey(dimension)) {
            return List.of();
        }

        Map<BlockPos, BlockState> cache = stateCache.get(dimension);

        return WorldUtil.getBlockPos(box)
            .parallelStream()
            .map(pos -> cache.get(pos))
            .filter(pos -> pos != null)
            .toList();
    }
}
