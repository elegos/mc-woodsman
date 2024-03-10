package name.giacomofurlan.woodsman.villager.task;

import java.util.Optional;
import java.util.function.Function;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.ImmutableList;

import name.giacomofurlan.woodsman.Woodsman;
import name.giacomofurlan.woodsman.util.NearestElements;
import net.minecraft.block.BlockState;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.WalkTarget;
import net.minecraft.entity.ai.brain.task.VillagerWorkTask;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;

public class WoodsmanWorkTask extends VillagerWorkTask {
    private static final int CYCLE_SECONDS = 5;
    private static final int TICKS_PER_SECOND = 20; // roughly value, experimentally taken
    private static final int SEARCH_RADIUS = 20; // Radius from POI from which the villager should search for and plant new trees
    private static final int OP_DISTANCE = 30; // Distance from POI from which the villager should work (roughtly sqrt distance between POI and search radius)

    protected long lastCheckedTime = 0;

    public static ImmutableList<Function<Pair<VillagerEntity, Brain<VillagerEntity>>, Boolean>> PRIORITIZED_ADDITIONAL_WORK_TASK_ACTIVATORS = ImmutableList.of(
        WoodsmanWorkTask::moveToSapling,
        WoodsmanWorkTask::moveToTree
    );

    @Override
    protected boolean shouldRun(ServerWorld serverWorld, VillagerEntity villagerEntity) {
        long currentTime = serverWorld.getTime();

        if ((currentTime - lastCheckedTime) < (CYCLE_SECONDS * TICKS_PER_SECOND)) {
            return false;
        }

        // Why not, add entropy! (taken from original VillagerWorkTask class)
        if (serverWorld.random.nextInt(2) != 0) {
            return false;
        }

        lastCheckedTime = currentTime;
        GlobalPos jobSitePos = villagerEntity.getBrain().getOptionalRegisteredMemory(MemoryModuleType.JOB_SITE).get();

        return jobSitePos.getDimension() == serverWorld.getRegistryKey()
            && jobSitePos.getPos().isWithinDistance(villagerEntity.getPos(), OP_DISTANCE);
    }

    @Override
    protected void performAdditionalWork(ServerWorld world, VillagerEntity entity) {
        Brain<VillagerEntity> brain = entity.getBrain();
        Optional<GlobalPos> optJobSitePos = brain.getOptionalRegisteredMemory(MemoryModuleType.JOB_SITE);

        // POI-less
        if (optJobSitePos.isEmpty()) {
            return;
        }

        for (Function<Pair<VillagerEntity, Brain<VillagerEntity>>, Boolean> activator : PRIORITIZED_ADDITIONAL_WORK_TASK_ACTIVATORS) {
            if (activator.apply(Pair.of(entity, brain))) {
                break;
            }
        }
        
        GlobalPos globalPos = optJobSitePos.get();
        BlockState blockState = world.getBlockState(globalPos.getPos());
        
        Woodsman.LOGGER.info(blockState.toString());
    }

    protected static Boolean moveToSapling(Pair<VillagerEntity, Brain<VillagerEntity>> input) {
        Brain<VillagerEntity> brain = input.getRight();
        Optional<WalkTarget> currentTarget = brain.getOptionalMemory(MemoryModuleType.WALK_TARGET);

        // Another activity is in progress
        if (currentTarget.isPresent()) {
            return false;
        }

        // TODO

        return false;
    }

    protected static Boolean moveToTree(Pair<VillagerEntity, Brain<VillagerEntity>> input) {
        VillagerEntity villager = input.getLeft();
        Brain<VillagerEntity> brain = input.getRight();
        Optional<WalkTarget> currentTarget = brain.getOptionalMemory(MemoryModuleType.WALK_TARGET);

        // Another activity is in progress
        if (currentTarget.isPresent()) {
            return false;
        }

        // Position of the chop block bound to
        GlobalPos jobSitePos = villager.getBrain().getOptionalRegisteredMemory(MemoryModuleType.JOB_SITE).get();

        Optional<BlockPos> nearestTreeBlock = NearestElements.getNearestTree(villager, jobSitePos.getPos(), SEARCH_RADIUS, OP_DISTANCE, true);
        Woodsman.LOGGER.info("Nearest tree: {}", nearestTreeBlock.isPresent() ? nearestTreeBlock.get().toShortString() : "null");

        if (nearestTreeBlock.isEmpty()) {
            return false;
        }

        // TODO WIP

        return true;
    }
}
