package name.giacomofurlan.woodsman.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;

import name.giacomofurlan.woodsman.brain.task.LuberjackTask;
import name.giacomofurlan.woodsman.brain.task.PickupItemsTask;
import name.giacomofurlan.woodsman.brain.task.PlantSaplingTask;
import name.giacomofurlan.woodsman.villager.ModVillagers;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.ai.brain.task.VillagerTaskListProvider;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.village.VillagerProfession;

@Mixin(value = VillagerTaskListProvider.class)
public class VillagerTaskListProviderMixin {
    private static final int SEARCH_RADIUS = 50;
    private static final float WALK_SPEED = 0.4f;
    private static final List<TagKey<Item>> ITEM_TAGS_TO_PICK = List.of(ItemTags.LOGS_THAT_BURN, ItemTags.SAPLINGS);
    private static final List<Item> ITEMS_TO_PICK = List.of(Items.STICK, Items.APPLE);

    private static final ImmutableList<Pair<Integer, ? extends Task<? super VillagerEntity>>> woodsmanTaskList = ImmutableList.of(
        Pair.of(2, new PlantSaplingTask(SEARCH_RADIUS, WALK_SPEED)),
        Pair.of(3, new PickupItemsTask(SEARCH_RADIUS, WALK_SPEED, ITEM_TAGS_TO_PICK, ITEMS_TO_PICK)),
        Pair.of(4, new LuberjackTask(SEARCH_RADIUS, WALK_SPEED))
    );

    @Inject(method = "createWorkTasks", at = @At("INVOKE"), cancellable = true)
    private static void createWorkTasks(
        VillagerProfession profession,
        float speed,
        CallbackInfoReturnable<ImmutableList<Pair<Integer, ? extends Task<? super VillagerEntity>>>> cir
    ) {
        if (profession.id().equals(ModVillagers.WOODSMAN_PROFESSION_ID)) {
            cir.setReturnValue(VillagerTaskListProviderMixin.woodsmanTaskList);
        }
    }
}
