package name.giacomofurlan.woodsman.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;

import name.giacomofurlan.woodsman.brain.WoodsmanWorkTask;
import name.giacomofurlan.woodsman.brain.task.LuberjackTask;
import name.giacomofurlan.woodsman.villager.ModVillagers;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.ai.brain.task.VillagerTaskListProvider;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.village.VillagerProfession;

@Mixin(value = VillagerTaskListProvider.class)
public class VillagerTaskListProviderMixin {
    private static final int SEARCH_RADIUS = 50;
    private static final float WALK_SPEED = 0.4f;

    private static final ImmutableList<Pair<Integer, ? extends Task<? super VillagerEntity>>> woodsmanTaskList = ImmutableList.of(
        Pair.of(4, new LuberjackTask(SEARCH_RADIUS, WALK_SPEED)),
        Pair.of(5, new WoodsmanWorkTask())
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
