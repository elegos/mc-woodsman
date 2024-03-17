package name.giacomofurlan.woodsman.brain.task;

import net.minecraft.entity.passive.VillagerEntity;

public interface IActivator {
    boolean shouldRun(VillagerEntity entity);

    boolean run(VillagerEntity entity);
}
