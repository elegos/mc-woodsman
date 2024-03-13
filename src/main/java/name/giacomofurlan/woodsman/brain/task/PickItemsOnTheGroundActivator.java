package name.giacomofurlan.woodsman.brain.task;

import java.util.List;

import net.minecraft.entity.Entity.RemovalReason;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

public class PickItemsOnTheGroundActivator implements IActivator {
    protected List<TagKey<Item>> itemTagsToPick;
    protected List<Item> itemsToPick;

    public PickItemsOnTheGroundActivator(List<TagKey<Item>> itemTagsToPick, List<Item> itemsToPick) {
        this.itemTagsToPick = itemTagsToPick;
        this.itemsToPick = itemsToPick;
    }

    @Override
    public boolean run(VillagerEntity entity, Brain<VillagerEntity> brain) {
        World world = entity.getWorld();

        BlockPos entityPos = entity.getBlockPos();

        List<ItemEntity> collectibleItemsOnTheGround = world.getEntitiesByClass(
            ItemEntity.class,
            Box.enclosing(entityPos.mutableCopy().add(2, 4, 2), entityPos.mutableCopy().add(-3, 0, -3)),
            (itemEntity) -> itemTagsToPick.stream().anyMatch(tag -> itemEntity.getStack().isIn(tag))
                || itemsToPick.stream().anyMatch(item -> itemEntity.getStack().getItem().equals(item))
        );

        for (ItemEntity item : collectibleItemsOnTheGround) {
            entity.getInventory().addStack(item.getStack()).toString();
            item.remove(RemovalReason.DISCARDED);
        }


        // This is a non-blocking activity
        return false;
    }

}
