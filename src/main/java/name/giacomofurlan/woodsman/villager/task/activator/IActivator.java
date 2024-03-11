package name.giacomofurlan.woodsman.villager.task.activator;

import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.passive.VillagerEntity;

public interface IActivator {
    boolean run(VillagerEntity entity, Brain<VillagerEntity> brain);
}
