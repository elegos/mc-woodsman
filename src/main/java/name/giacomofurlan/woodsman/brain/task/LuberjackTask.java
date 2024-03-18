package name.giacomofurlan.woodsman.brain.task;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import name.giacomofurlan.woodsman.Woodsman;
import name.giacomofurlan.woodsman.brain.ModMemoryModuleType;
import name.giacomofurlan.woodsman.util.WorldCache;
import name.giacomofurlan.woodsman.util.WorldUtil;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.task.VillagerWorkTask;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.World;

/**
 * Luberjack activity
 */
public class LuberjackTask extends VillagerWorkTask {
    protected int searchRadius;
    protected float walkSpeed;

    protected long lastCheckedTime;
    protected List<GlobalPos> bannedTreeList;

    public LuberjackTask(int searchRadius, float walkSpeed) {
        super();

        this.bannedTreeList = new ArrayList<>();

        this.searchRadius = searchRadius;
        this.walkSpeed = walkSpeed;
    }

    /**
     * The task should run at most every 3 seconds (~60 ticks) if there are still empty slots in the inventory.
     */
    @Override
    protected boolean shouldRun(ServerWorld serverWorld, VillagerEntity villagerEntity) {
        Optional<String> currentTask = villagerEntity.getBrain().getOptionalMemory(ModMemoryModuleType.CURRENT_WOODSMAN_TASK);
        if (currentTask.isPresent() && !currentTask.get().equals(getName())) {
            return false;
        }

        if (serverWorld.getTime() - this.lastCheckedTime < 60L) {
            return false;
        }
        this.lastCheckedTime = serverWorld.getTime();

        SimpleInventory inventory = villagerEntity.getInventory();
        int numOccupiedStacks = 0;

        for (int i = 0; i < inventory.size(); i++) {
            if (inventory.getStack(i).getItem() != Items.AIR) {
                numOccupiedStacks++;
            }
        }

        return numOccupiedStacks < inventory.size();
    }

    /**
     * The task should continue until the target tree is being cut
     */
    @Override
    protected boolean shouldKeepRunning(ServerWorld serverWorld, VillagerEntity villagerEntity, long l) {
        Optional<String> currentTask = villagerEntity.getBrain().getOptionalMemory(ModMemoryModuleType.CURRENT_WOODSMAN_TASK);
        Optional<List<BlockPos>> targetTree = villagerEntity.getBrain().getOptionalMemory(ModMemoryModuleType.TARGET_TREE);

        if (currentTask.isPresent() && !currentTask.get().equals(getName())) {
            return false;
        }

        if (targetTree.isPresent() && targetTree.get().isEmpty()) {
            villagerEntity.getBrain().forget(ModMemoryModuleType.CURRENT_WOODSMAN_TASK);

            return false;
        }

        return true;
    }

    @Override
    protected void run(ServerWorld serverWorld, VillagerEntity villagerEntity, long l) {
        Brain<VillagerEntity> brain = villagerEntity.getBrain();
        Optional<List<BlockPos>> targetTree = brain.getOptionalMemory(ModMemoryModuleType.TARGET_TREE);
        EntityNavigation navigation = villagerEntity.getNavigation();

        if (targetTree.isEmpty()) {
            navigation.stop();
            targetTree = findNearestTree(villagerEntity, searchRadius);

            if (targetTree.isEmpty()) {
                return;
            }

            brain.remember(ModMemoryModuleType.TARGET_TREE, targetTree.get());
        }

        brain.remember(ModMemoryModuleType.CURRENT_WOODSMAN_TASK, getName());

        BlockPos targetTreePos = targetTree.get().get(0);
        BlockPos villagerPos = villagerEntity.getBlockPos();
        Path currentPath = navigation.getCurrentPath();

        if (currentPath == null) {
            Woodsman.LOGGER.debug("Moving to target tree ({})...", targetTreePos.toShortString());
            navigation.startMovingTo(targetTreePos.getX(), targetTreePos.getY(), targetTreePos.getZ(), walkSpeed);

            return;
        } else if (villagerEntity.isNavigating()) {
            // Still not there yet
            Woodsman.LOGGER.debug("{}: Navigating...", getName());
            return;
        } else if (currentPath.getTarget().mutableCopy().setY(villagerPos.getY()).getManhattanDistance(villagerPos) > 1) {
            // Not navigating, but still not there yet
            Woodsman.LOGGER.debug("Not navigating, but still not there yet: obstruction ahead...");

            Optional<BlockPos> nextBlock = WorldUtil.findNextWalkableBlockToTarget(villagerPos, targetTreePos);
            if (nextBlock.isEmpty()) {
                Woodsman.LOGGER.debug("Can't reach the position, banning the tree root position...");

                bannedTreeList.add(GlobalPos.create(serverWorld.getRegistryKey(), targetTreePos));
                navigation.stop();

                return;
            }
            List.of(nextBlock.get(), nextBlock.get().up()).forEach(blockPos -> {
                if (WorldCache.getInstance().getCachedBlock(serverWorld, blockPos).isIn(BlockTags.LEAVES)) {
                    serverWorld.breakBlock(blockPos, true);
                }
            });

            navigation.startMovingTo(targetTreePos.getX(), targetTreePos.getY(), targetTreePos.getZ(), walkSpeed);

            return;
        }

        Woodsman.LOGGER.debug("Cutting target tree ({})...", targetTreePos.toShortString());
        villagerEntity.getWorld().breakBlock(targetTreePos, true);
        if (targetTree.get().size() == 1) {
            villagerEntity.getBrain().forget(ModMemoryModuleType.TARGET_TREE);
            villagerEntity.getBrain().forget(ModMemoryModuleType.CURRENT_WOODSMAN_TASK);
        } else {
            villagerEntity.getBrain().remember(ModMemoryModuleType.TARGET_TREE, targetTree.get().subList(1, targetTree.get().size()));
        }
    }

    protected Optional<List<BlockPos>> findNearestTree(VillagerEntity entity, int searchRadius) {
        World world = entity.getWorld();
        WorldCache worldCache = WorldCache.getInstance();
        BlockPos entityPos = entity.getBlockPos();

        List<BlockPos> logsWithinReach = WorldUtil.getBlockPos(WorldUtil.cubicBoxFromCenter(entityPos, searchRadius), true)
            .stream()
            .filter(pos -> !bannedTreeList.contains(GlobalPos.create(world.getRegistryKey(), pos)) && worldCache.getCachedBlock(world, pos).isIn(BlockTags.LOGS_THAT_BURN))
            .toList();

        for (BlockPos logPos : logsWithinReach) {
            Optional<Set<BlockPos>> tree = WorldUtil.getTreeLogsPosFromAnyLog(world, logPos);

            if (tree.isPresent()) {
                return Optional.of(tree.get().stream().sorted(Comparator.comparingDouble(pos -> pos.getY())).toList());
            }
        }

        return Optional.empty();
    }
}
