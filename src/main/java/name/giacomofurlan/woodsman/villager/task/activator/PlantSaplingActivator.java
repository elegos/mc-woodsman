package name.giacomofurlan.woodsman.villager.task.activator;

import name.giacomofurlan.woodsman.util.NearestElements;
import name.giacomofurlan.woodsman.villager.task.WoodsmanWorkTask;
import net.minecraft.block.BlockState;
import net.minecraft.entity.ai.brain.BlockPosLookTarget;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.WalkTarget;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.World;

public class PlantSaplingActivator implements IActivator {
    @Override
    public boolean run(VillagerEntity villager, Brain<VillagerEntity> brain) {
        if (brain.getOptionalMemory(MemoryModuleType.WALK_TARGET).isPresent()) {
            return false;
        }

        World world = villager.getWorld();
        GlobalPos jobSitePos = villager.getBrain().getOptionalRegisteredMemory(MemoryModuleType.JOB_SITE).get();


        SimpleInventory inventory = villager.getInventory();

        ItemStack sapling = null;

        for (int slot = 0; slot < inventory.size(); slot++) {
            ItemStack stack = inventory.getStack(slot);

            if (stack.isIn(ItemTags.SAPLINGS)) {
                sapling = stack;
                break;
            }
        }

        if (sapling == null) {
            return false;
        }

        int startX = villager.getBlockX();
        int startY = villager.getBlockY();
        int startZ = villager.getBlockZ();

        for (int distance = 0; distance < WoodsmanWorkTask.SEARCH_RADIUS; distance++) {
            cubeLoop:
            for (BlockPos candidatePos : NearestElements.cubicCoordinatesFromCenter(startX, startY, startZ, distance)) {
                BlockState blockState = world.getBlockState(candidatePos);
                BlockPos lowerBlock = candidatePos.mutableCopy().add(0, -1, 0);

                if (
                    !world.getBlockState(lowerBlock).isIn(BlockTags.DIRT)
                    || !blockState.isAir()
                    || candidatePos.getManhattanDistance(jobSitePos.getPos()) > WoodsmanWorkTask.OP_DISTANCE
                ) {
                    continue;
                }

                // check whether there is a sapling or a tree nearby (radius of DISTANCE_BETWEEN_TREES)
                for (int dist = 1; dist < WoodsmanWorkTask.DISTANCE_BETWEEN_TREES; dist++) {
                    boolean hasNearbyTrees = NearestElements
                        .cubicCoordinatesFromCenter(candidatePos.getX(), candidatePos.getY(), candidatePos.getZ(), WoodsmanWorkTask.DISTANCE_BETWEEN_TREES)
                        .stream()
                        .map(nearbyPos -> world.getBlockState(nearbyPos).isIn(BlockTags.LOGS_THAT_BURN) || world.getBlockState(nearbyPos).isIn(BlockTags.SAPLINGS))
                        .reduce(false, (accumulator, value) -> accumulator || value);
                    
                    if (hasNearbyTrees) {
                        continue cubeLoop;
                    }
                }

                // Position is too far away to plant the seed, move to it
                if (candidatePos.getManhattanDistance(villager.getBlockPos()) > NearestElements.INTERACTION_MAHNATTAN_DISTANCE) {
                    brain.remember(MemoryModuleType.LOOK_TARGET, new BlockPosLookTarget(candidatePos));
                    brain.remember(MemoryModuleType.WALK_TARGET, new WalkTarget(candidatePos, WoodsmanWorkTask.WALK_SPEED, 1));

                    return true;
                }

                brain.remember(MemoryModuleType.LOOK_TARGET, new BlockPosLookTarget(candidatePos));
                world.setBlockState(candidatePos, WoodsmanWorkTask.SAPLINGS_MAP.get(sapling.getItem()).getDefaultState());

                inventory.removeItem(sapling.getItem(), 1);

                return true;
            }
        }

        return false;
    }
}
