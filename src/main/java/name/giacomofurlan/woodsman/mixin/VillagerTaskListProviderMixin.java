package name.giacomofurlan.woodsman.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;

import name.giacomofurlan.woodsman.villager.ModVillagers;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.ai.brain.task.VillagerTaskListProvider;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.village.VillagerProfession;

@Mixin(value = VillagerTaskListProvider.class)
public class VillagerTaskListProviderMixin {
    // TODO create a list of tasks for Woodsman profession
    private static final ImmutableList<Pair<Integer, ? extends Task<? super VillagerEntity>>> woodsmanTaskList = ImmutableList.of();

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
