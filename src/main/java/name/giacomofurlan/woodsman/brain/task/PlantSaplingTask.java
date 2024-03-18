package name.giacomofurlan.woodsman.brain.task;

import java.util.HashMap;
import java.util.Optional;

import name.giacomofurlan.woodsman.Woodsman;
import name.giacomofurlan.woodsman.brain.ModMemoryModuleType;
import name.giacomofurlan.woodsman.util.WorldCache;
import name.giacomofurlan.woodsman.util.WorldUtil;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.VillagerWorkTask;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.World;

public class PlantSaplingTask extends VillagerWorkTask {
    public static final HashMap<Item, Block> SAPLINGS_MAP = new HashMap<>(){{
        put(Items.ACACIA_SAPLING, Blocks.ACACIA_SAPLING);
        put(Items.BIRCH_SAPLING, Blocks.BIRCH_SAPLING);
        put(Items.CHERRY_SAPLING, Blocks.CHERRY_SAPLING);
        put(Items.DARK_OAK_SAPLING, Blocks.DARK_OAK_SAPLING);
        put(Items.JUNGLE_SAPLING, Blocks.JUNGLE_SAPLING);
        put(Items.OAK_SAPLING, Blocks.OAK_SAPLING);
        put(Items.SPRUCE_SAPLING, Blocks.SPRUCE_SAPLING);
    }};

    protected int operativeRadius;
    protected float walkSpeed;

    protected long lastCheckedTime;

    public PlantSaplingTask(int operativeRadius, float walkSpeed) {
        super();

        this.operativeRadius = operativeRadius;
        this.walkSpeed = walkSpeed;
    }

    @Override
    protected boolean shouldRun(ServerWorld serverWorld, VillagerEntity villagerEntity) {
        Brain<VillagerEntity> brain = villagerEntity.getBrain();
        Optional<String> currentTask = brain.getOptionalMemory(ModMemoryModuleType.CURRENT_WOODSMAN_TASK);

        if (
            (currentTask.isPresent() && !currentTask.get().equals(getName()))
            || getNextSaplingStack(villagerEntity).isEmpty()
        ) {
            return false;
        }

        if (serverWorld.getTime() - this.lastCheckedTime < 60L) {
            return false;
        }
        this.lastCheckedTime = serverWorld.getTime();

        return true;
    }

    @Override
    protected boolean shouldKeepRunning(ServerWorld serverWorld, VillagerEntity villagerEntity, long l) {
        Brain<VillagerEntity> brain = villagerEntity.getBrain();
        Optional<String> currentTask = brain.getOptionalMemory(ModMemoryModuleType.CURRENT_WOODSMAN_TASK);

        return currentTask.isPresent()
            && currentTask.get().equals(getName())
            && getNextSaplingStack(villagerEntity).isPresent();
    }

    @Override
    protected void run(ServerWorld serverWorld, VillagerEntity villagerEntity, long l) {
        if (villagerEntity.isNavigating()) {
            Woodsman.LOGGER.debug("{}: Navigating...", getName());
            return;
        }

        Brain<VillagerEntity> brain = villagerEntity.getBrain();
        Optional<ItemStack> nextSapling = getNextSaplingStack(villagerEntity);
        if (nextSapling.isEmpty()) {
            brain.forget(ModMemoryModuleType.CURRENT_WOODSMAN_TASK);

            return;
        }

        ItemStack sapling = nextSapling.get();

        Optional<BlockPos> targetPosMemory = brain.getOptionalMemory(ModMemoryModuleType.SAPLING_TARGET_POS);
        if (targetPosMemory.isEmpty()) {
            Optional<GlobalPos> jobSite = brain.getOptionalMemory(MemoryModuleType.JOB_SITE);
            Optional<BlockPos> plantLocation = getNearestPlantLocation(sapling, villagerEntity.getWorld(), jobSite.get().getPos(), operativeRadius);

            if (plantLocation.isEmpty()) {
                Woodsman.LOGGER.debug("{}: Need to plant sapling, but no plant location was found", getName());

                // TODO manage this situation
                return;
            }

            targetPosMemory = plantLocation;
            brain.remember(ModMemoryModuleType.SAPLING_TARGET_POS, plantLocation.get());
        }

        if (targetPosMemory.get().getManhattanDistance(villagerEntity.getBlockPos()) < 5) {
            villagerEntity.getWorld().setBlockState(targetPosMemory.get(), SAPLINGS_MAP.get(sapling.getItem()).getDefaultState());
            villagerEntity.getInventory().removeItem(sapling.getItem(), 1);
            
            brain.forget(ModMemoryModuleType.CURRENT_WOODSMAN_TASK);
            brain.forget(ModMemoryModuleType.SAPLING_TARGET_POS);

            return;
        }

        EntityNavigation navigation = villagerEntity.getNavigation();
        navigation.startMovingAlong(navigation.findPathTo(targetPosMemory.get(), 1), walkSpeed);
    }

    private Optional<BlockPos> getNearestPlantLocation(ItemStack sapling, World world, BlockPos centerPos, int radius) {
        WorldCache cache = WorldCache.getInstance();

        // TODO special algorithm for dark oak sapling (2x2 planting)
        
        // It needs to be air
        // It neds to have dirt underneath
        // It needs to have no other saplings or logs around
        return WorldUtil.getBlockPos(WorldUtil.cubicBoxFromCenter(centerPos, radius), true)
            .stream()
            .filter(pos -> world.getBlockState(pos).isAir()
                && cache.getCachedBlock(world, pos.down()).isIn(BlockTags.DIRT)
                && !cache.getStatesInBox(world, WorldUtil.cubicBoxFromCenter(pos, 5))
                    .stream()
                    .anyMatch(posState -> posState.isIn(BlockTags.SAPLINGS) || posState.isIn(BlockTags.LOGS_THAT_BURN)))
            .findFirst();
    }

    protected Optional<ItemStack> getNextSaplingStack(VillagerEntity entity) {
        SimpleInventory inventory = entity.getInventory();

        for (int i = 0; i < inventory.size(); i++) {
            if (inventory.getStack(i).isIn(ItemTags.SAPLINGS)) {
                return Optional.of(inventory.getStack(i));
            }
        }

        return Optional.empty();
    }
}
