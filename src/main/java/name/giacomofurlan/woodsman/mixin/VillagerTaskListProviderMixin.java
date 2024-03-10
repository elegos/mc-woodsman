package name.giacomofurlan.woodsman.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;

import name.giacomofurlan.woodsman.villager.ModVillagers;
import name.giacomofurlan.woodsman.villager.task.WoodsmanWorkTask;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.task.LookAtMobTask;
import net.minecraft.entity.ai.brain.task.RandomTask;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.ai.brain.task.VillagerTaskListProvider;
import net.minecraft.entity.ai.brain.task.WaitTask;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.village.VillagerProfession;

@Mixin(value = VillagerTaskListProvider.class)
public class VillagerTaskListProviderMixin {
    private static final ImmutableList<Pair<Integer, ? extends Task<? super VillagerEntity>>> woodsmanTaskList = ImmutableList.of(
        VillagerTaskListProviderMixin.createBusyFollowTask(),
        Pair.of(5, new WoodsmanWorkTask())
        // TODO create a list of tasks for Woodsman profession
    );

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static Pair<Integer, Task<LivingEntity>> createBusyFollowTask() {
        return Pair.of(5, new RandomTask(ImmutableList.of(Pair.of(LookAtMobTask.create(EntityType.VILLAGER, 8.0f), 2), Pair.of(LookAtMobTask.create(EntityType.PLAYER, 8.0f), 2), Pair.of(new WaitTask(30, 60), 8))));
    }

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

    // public static ImmutableList<Pair<Integer, ? extends Task<? super VillagerEntity>>> createWorkTasksa(VillagerProfession profession, float speed) {
    //     VillagerWorkTask villagerWorkTask = profession == VillagerProfession.FARMER ? new FarmerWorkTask() : new VillagerWorkTask();
    //     return ImmutableList.of(
    //         VillagerTaskListProvider.createBusyFollowTask(),
    //         Pair.of(5, new RandomTask(
    //             ImmutableList.of(
    //                 Pair.of(villagerWorkTask, 7),
    //                 Pair.of(GoToIfNearbyTask.create(MemoryModuleType.JOB_SITE, 0.4f, 4), 2),
    //                 Pair.of(GoToNearbyPositionTask.create(MemoryModuleType.JOB_SITE, 0.4f, 1, 10), 5),
    //                 Pair.of(GoToSecondaryPositionTask.create(MemoryModuleType.SECONDARY_JOB_SITE, speed, 1, 6, MemoryModuleType.JOB_SITE), 5),
    //                 Pair.of(new FarmerVillagerTask(), profession == VillagerProfession.FARMER ? 2 : 5),
    //                 Pair.of(new BoneMealTask(), profession == VillagerProfession.FARMER ? 4 : 7)))),
    //                 Pair.of(10, new HoldTradeOffersTask(400, 1600)),
    //                 Pair.of(10, FindInteractionTargetTask.create(EntityType.PLAYER, 4)),
    //                 Pair.of(2, VillagerWalkTowardsTask.create(MemoryModuleType.JOB_SITE, speed, 9, 100, 1200)),
    //                 Pair.of(3, new GiveGiftsToHeroTask(100)),
    //                 Pair.of(99, ScheduleActivityTask.create()
    //         ));
    // }
}
