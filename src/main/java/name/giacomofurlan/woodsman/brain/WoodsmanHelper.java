package name.giacomofurlan.woodsman.brain;

import java.util.HashMap;

import net.minecraft.block.BlockState;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;

public class WoodsmanHelper {
    protected static final HashMap<VillagerEntity, WoodsmanHelper> cache = new HashMap<>();

    protected Pair<BlockPos, BlockState> currentTarget;

    protected WoodsmanHelper() {
        
    }

    public static WoodsmanHelper getInstance(VillagerEntity entity) {
        if (!cache.containsKey(entity)) {
            cache.put(entity, new WoodsmanHelper());
        }

        return cache.get(entity);
    }

    public static HashMap<VillagerEntity, WoodsmanHelper> getCache() {
        return cache;
    }

    public Pair<BlockPos, BlockState> getCurrentTarget() {
        return currentTarget;
    }

    public void setCurrentTarget(Pair<BlockPos, BlockState> currentTarget) {
        this.currentTarget = currentTarget;
    }
}
