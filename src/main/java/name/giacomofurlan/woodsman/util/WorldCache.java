package name.giacomofurlan.woodsman.util;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import name.giacomofurlan.woodsman.Woodsman;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.World;

public class WorldCache {
    protected static Map<RegistryKey<World>, Map<BlockPos, List<BlockPos>>> treeCache = new HashMap<>();

    public static void cacheTrees(World world, BlockPos pos, int radius) {
        if (!treeCache.containsKey(world.getRegistryKey())) {
            treeCache.put(world.getRegistryKey(), new HashMap<>());
        }

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Woodsman.LOGGER.debug("Caching trees in {} for {} blocks radius from center ({})", world.getRegistryKey().getValue(), radius, pos.toShortString());
                Map<BlockPos, List<BlockPos>> dimensionCache = treeCache.get(world.getRegistryKey());

                dimensionCache.clear();

                WorldUtil.getBlockPos(WorldUtil.cubicBoxFromCenter(pos, radius))
                    .stream()
                    .filter(blockPos -> world.getBlockState(blockPos).isIn(BlockTags.LOGS_THAT_BURN))
                    .forEach(blockPos -> {
                        Optional<Set<BlockPos>> treeLogs = WorldUtil.getTreeLogsPosFromAnyLog(world, blockPos);
                        if (treeLogs.isEmpty()) {
                            return;
                        }

                        List<BlockPos> logsToCache = treeLogs.get().stream().sorted(Comparator.comparingDouble(logPos -> logPos.getY())).toList();
                        dimensionCache.put(logsToCache.get(0), logsToCache);
                    });
                
                Woodsman.LOGGER.debug("Cached trees in {} for {} blocks radius from center ({})", world.getRegistryKey().getValue(), radius, pos.toShortString());
            }
        };

        Thread thread = new Thread(runnable);

        thread.start();
    }

    public static Optional<List<BlockPos>> getNearestTreeFromCache(GlobalPos pos) {
        RegistryKey<World> dimension = pos.getDimension();
        if (!treeCache.containsKey(dimension)) {
            return Optional.empty();
        }

        BlockPos centerPos = pos.getPos();
        Optional<BlockPos> key = treeCache.get(dimension).keySet()
            .stream()
            .sorted(Comparator.comparingDouble(rootPos -> rootPos.getSquaredDistance(centerPos)))
            .findFirst();
        
        if (key.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(treeCache.get(dimension).get(key.get()));
    }
}
