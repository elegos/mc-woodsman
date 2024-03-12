package name.giacomofurlan.woodsman.brain.task;

import java.util.List;

import net.minecraft.entity.Entity.RemovalReason;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

public class PickItemsOnTheGroundActivator implements IActivator {

    @Override
    public boolean run(VillagerEntity entity, Brain<VillagerEntity> brain) {
        World world = entity.getWorld();

        BlockPos entityPos = entity.getBlockPos();

        List<ItemEntity> collectibleItemsOnTheGround = world.getEntitiesByClass(
            ItemEntity.class,
            Box.enclosing(entityPos.mutableCopy().add(2, 4, 2), entityPos.mutableCopy().add(-3, 0, -3)),
            (itemEntity) -> itemEntity.getStack().isIn(ItemTags.SAPLINGS)
                || itemEntity.getStack().isIn(ItemTags.LOGS_THAT_BURN)
                || itemEntity.getStack().getItem().equals(Items.STICK)
        );

        for (ItemEntity item : collectibleItemsOnTheGround) {
            entity.getInventory().addStack(item.getStack()).toString();
            item.remove(RemovalReason.DISCARDED);
        }


        // This is a non-blocking activity
        return false;
    }

}
