package name.giacomofurlan.woodsman.event;

import java.util.Optional;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.util.ActionResult;

public interface IBlockBreakCallback {
    Event<IBlockBreakCallback> EVENT = EventFactory.createArrayBacked(IBlockBreakCallback.class, (listeners) -> (entity, block) -> {
        for (IBlockBreakCallback listener : listeners) {
            ActionResult result = listener.onBlockBreak(entity, block);

            if (result != ActionResult.PASS) {
                return result;
            }
        }

        return ActionResult.PASS;
    });

    ActionResult onBlockBreak(Optional<Entity> entity, BlockEntity block);
}
