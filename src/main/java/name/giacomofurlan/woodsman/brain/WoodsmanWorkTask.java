package name.giacomofurlan.woodsman.brain;

import java.util.List;
import java.util.Optional;

import com.google.common.collect.ImmutableList;

import name.giacomofurlan.woodsman.Woodsman;
import name.giacomofurlan.woodsman.brain.task.CutTreeActivator;
import name.giacomofurlan.woodsman.brain.task.DepositItemsInChestActivator;
import name.giacomofurlan.woodsman.brain.task.IActivator;
import name.giacomofurlan.woodsman.brain.task.PickItemsOnTheGroundActivator;
import name.giacomofurlan.woodsman.brain.task.PlantSaplingActivator;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.VillagerWorkTask;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.GlobalPos;

public class WoodsmanWorkTask extends VillagerWorkTask {
    public static final int CYCLE_SECONDS = 2;
    public static final int DEPOSIT_INTERVAL_SECONDS = 300; // Seconds after which the woodsman will deposit items in the chest
    public static final int TICKS_PER_SECOND = 20; // roughly value, experimentally taken
    public static final int SEARCH_RADIUS = 50; // Radius from POI from which the villager should search for and plant new trees
    public static final int MAX_INTERACTION_MANHATTAN_DISTANCE = 16; // Maximum manhattan distance to interact with items
    public static final int OP_DISTANCE = 142; // Sqrt distance of a 200x200 area from the center of the POI
    public static final float WALK_SPEED = 0.4f;
    public static final int DISTANCE_BETWEEN_TREES = 4;

    protected long lastCheckedTime = 0;

    public static ImmutableList<IActivator> PRIORITIZED_ACTIVATORS = ImmutableList.of(
        new PlantSaplingActivator(SEARCH_RADIUS, WALK_SPEED),
        new DepositItemsInChestActivator(false, DEPOSIT_INTERVAL_SECONDS, TICKS_PER_SECOND, OP_DISTANCE, WALK_SPEED),
        new PickItemsOnTheGroundActivator(
            List.of(ItemTags.SAPLINGS, ItemTags.LOGS_THAT_BURN),
            List.of(Items.STICK, Items.APPLE),
            SEARCH_RADIUS,
            WALK_SPEED
        ),
        new CutTreeActivator(SEARCH_RADIUS, WALK_SPEED),

        new DepositItemsInChestActivator(true, DEPOSIT_INTERVAL_SECONDS, TICKS_PER_SECOND, OP_DISTANCE, WALK_SPEED)
        // new ReturnToPOIActivator()
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

        for (IActivator activator : PRIORITIZED_ACTIVATORS) {
            if (activator.run(entity, brain)) {
                Woodsman.LOGGER.debug("Current action: {}", activator.getClass().getSimpleName());

                return;
            }
        }

        Woodsman.LOGGER.debug("No activator was found");
    }
}
