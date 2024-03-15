package name.giacomofurlan.woodsman.util;

import java.util.HashMap;
import java.util.Map;

import name.giacomofurlan.woodsman.Woodsman;
import net.minecraft.block.BlockState;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class WorldCache {
    protected static WorldCache instance;

    protected Boolean registeredToEvents = false;
    protected Map<String, Map<BlockPos, BlockState>> stateCache;

    public static WorldCache getInstance() {
        if (instance == null) {
            instance = new WorldCache();
        }
        return instance;
    }

    protected WorldCache() {
        stateCache = new HashMap<>();
    }
    
    /**
     * Caches all the log/leavesblocks within a specified radius of a center position in the world.
     *
     * @param  world   the world in which the blocks are located
     * @param  center  the center position around which to cache blocks
     * @param  radius  the radius within which to cache blocks
     */
    public void cacheCube(World world, BlockPos center, int radius) {
        String dimension = world.getRegistryKey().getValue().toString();

        Woodsman.LOGGER.debug("Caching cube ({} :: {}) of radius {}", dimension, center.toShortString(), radius);
        WorldUtil.cubicCoordinatesFromCenter(center, radius)
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

        Map<BlockPos, BlockState> dimensionCache = stateCache.get(dimension);
        if (state.isIn(BlockTags.LEAVES) || state.isIn(BlockTags.LOGS_THAT_BURN)) {
            dimensionCache.put(pos, state);
        } else if (dimensionCache.containsKey(pos)) {
            dimensionCache.remove(pos);
        }
    }
}
