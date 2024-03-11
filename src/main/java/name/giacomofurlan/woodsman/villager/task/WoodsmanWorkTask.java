package name.giacomofurlan.woodsman.villager.task;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import com.google.common.collect.ImmutableList;

import name.giacomofurlan.woodsman.Woodsman;
import name.giacomofurlan.woodsman.villager.task.activator.ChopTreeActivator;
import name.giacomofurlan.woodsman.villager.task.activator.CutTreeAroundActivator;
import name.giacomofurlan.woodsman.villager.task.activator.DepositItemsInChestActivator;
import name.giacomofurlan.woodsman.villager.task.activator.IActivator;
import name.giacomofurlan.woodsman.villager.task.activator.MoveToItemOnGroundActivator;
import name.giacomofurlan.woodsman.villager.task.activator.MoveToTreeActivator;
import name.giacomofurlan.woodsman.villager.task.activator.PickItemsOnTheGroundActivator;
import name.giacomofurlan.woodsman.villager.task.activator.PlantSaplingActivator;
import name.giacomofurlan.woodsman.villager.task.activator.ReturnToPOIActivator;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.VillagerWorkTask;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.GlobalPos;

public class WoodsmanWorkTask extends VillagerWorkTask {
    public static final int CYCLE_SECONDS = 2;
    public static final int TICKS_PER_SECOND = 20; // roughly value, experimentally taken
    public static final int SEARCH_RADIUS = 30; // Radius from POI from which the villager should search for and plant new trees
    public static final int OP_DISTANCE = 100; // Distance from POI from which the villager should work within
    public static final float WALK_SPEED = 0.4f;
    public static final int DISTANCE_BETWEEN_TREES = 4;

    public static final HashMap<Item, Block> SAPLINGS_MAP = new HashMap<>(){{
        put(Items.ACACIA_SAPLING, Blocks.ACACIA_SAPLING);
        put(Items.BIRCH_SAPLING, Blocks.BIRCH_SAPLING);
        put(Items.CHERRY_SAPLING, Blocks.CHERRY_SAPLING);
        put(Items.DARK_OAK_SAPLING, Blocks.DARK_OAK_SAPLING);
        put(Items.JUNGLE_SAPLING, Blocks.JUNGLE_SAPLING);
        put(Items.OAK_SAPLING, Blocks.OAK_SAPLING);
        put(Items.SPRUCE_SAPLING, Blocks.SPRUCE_SAPLING);
    }};

    protected long lastCheckedTime = 0;

    public static ImmutableList<IActivator> PRIORITIZED_ACTIVATORS = ImmutableList.of(
        new DepositItemsInChestActivator(),

        new CutTreeAroundActivator(),
        
        new PlantSaplingActivator(),
        
        new PickItemsOnTheGroundActivator(),
        new MoveToItemOnGroundActivator(List.of(ItemTags.LOGS_THAT_BURN, ItemTags.SAPLINGS), SEARCH_RADIUS),
        
        new ChopTreeActivator(),
        new MoveToTreeActivator(),

        new ReturnToPOIActivator(),
        new DepositItemsInChestActivator(true)
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
            Woodsman.LOGGER.info(activator.getClass().getSimpleName());
            if (activator.run(entity, brain)) {
                Woodsman.LOGGER.info("Current action: {}", activator.getClass().getSimpleName());

                return;
            }
        }

        Woodsman.LOGGER.info("No activator was found");
    }
}