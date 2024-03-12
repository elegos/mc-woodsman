package name.giacomofurlan.woodsman.brain.task;

import java.util.Optional;

import name.giacomofurlan.woodsman.brain.WoodsmanWorkTask;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.WalkTarget;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.util.math.GlobalPos;

public class ReturnToPOIActivator implements IActivator {

    @Override
    public boolean run(VillagerEntity entity, Brain<VillagerEntity> brain) {
        if (brain.getOptionalMemory(MemoryModuleType.WALK_TARGET).isPresent()) {
            return false;
        }

        Optional<GlobalPos> jobSite = brain.getOptionalMemory(MemoryModuleType.JOB_SITE);

        if (jobSite == null || entity.getBlockPos().getManhattanDistance(jobSite.get().getPos()) <= 2) {
            return false;
        }

        brain.remember(MemoryModuleType.WALK_TARGET, new WalkTarget(jobSite.get().getPos(), WoodsmanWorkTask.WALK_SPEED, 2));

        return true;
    }

}
