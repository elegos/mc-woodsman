package name.giacomofurlan.woodsman.event;

import java.util.Optional;

import name.giacomofurlan.woodsman.util.WorldCache;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TreeBlockCallback implements IBlockStateChangeCallback, IBlockBreakCallback {
    @Override
    public ActionResult onBlockBreak(Optional<Entity> entity, BlockEntity block) {
        BlockState state = block.getCachedState();
        World world = entity.isPresent() ? entity.get().getWorld() : null;

        if (state == null && world != null) {
            state = world.getBlockState(block.getPos());
        }

        updateCache(entity, block.getPos(), state);

        return ActionResult.PASS;
    }

    @Override
    public ActionResult onBlockStateChange(Optional<Entity> stateChanger, BlockPos pos, BlockState blockState) {
        updateCache(stateChanger, pos, blockState);

        return ActionResult.PASS;
    }

    protected void updateCache(Optional<Entity> entity, BlockPos pos, BlockState state) {
        World world = entity.isPresent() ? entity.get().getWorld() : null;

        WorldCache.getInstance().updateCache(world.getRegistryKey().getValue().toString(), pos, state, false);
    }
}
