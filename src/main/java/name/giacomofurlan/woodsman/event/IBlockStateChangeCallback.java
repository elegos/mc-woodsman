package name.giacomofurlan.woodsman.event;

import java.util.Optional;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;

public interface IBlockStateChangeCallback {
    Event<IBlockStateChangeCallback> EVENT = EventFactory.createArrayBacked(IBlockStateChangeCallback.class, (listeners) -> (entity, blockPos, blockState) -> {
        for (IBlockStateChangeCallback listener : listeners) {
            ActionResult result = listener.onBlockStateChange(entity, blockPos, blockState);
            if (result != ActionResult.PASS) {
                return result;
            }
        }
        return ActionResult.PASS;
    });

    ActionResult onBlockStateChange(Optional<Entity> stateChanger, BlockPos pos, BlockState blockState);
}
