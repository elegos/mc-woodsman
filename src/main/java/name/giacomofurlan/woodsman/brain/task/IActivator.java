package name.giacomofurlan.woodsman.brain.task;

import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.passive.VillagerEntity;

public interface IActivator {
    boolean run(VillagerEntity entity, Brain<VillagerEntity> brain);
}
