package name.giacomofurlan.woodsman.brain.task;

import name.giacomofurlan.woodsman.brain.ModMemoryModuleType;
import name.giacomofurlan.woodsman.brain.WoodsmanWorkTask;
import name.giacomofurlan.woodsman.util.NearestElements;
import net.minecraft.entity.ai.brain.BlockPosLookTarget;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

public class PlantSaplingActivator implements IActivator {
    @Override
    public boolean run(VillagerEntity villager, Brain<VillagerEntity> brain) {
        if (brain.getOptionalMemory(MemoryModuleType.WALK_TARGET).isPresent()) {
            return false;
        }

        World world = villager.getWorld();

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
            for (BlockPos candidatePos : NearestElements.cubicCoordinatesFromCenter(startX, startY, startZ, distance)) {
                Boolean isValid = world.getStatesInBox(Box.enclosing(candidatePos.up().east().north(), candidatePos.down().west().south()))
                    .map(state -> state.isAir() || state.isIn(BlockTags.DIRT))
                    .reduce(true, (acc, val) -> acc && val);
                
                if (!isValid) {
                    continue;
                }

                // Can't plan in air or on dirt
                if (
                    !world.getBlockState(candidatePos).isAir()
                    || world.getBlockState(candidatePos.down()).isAir()
                ) {
                    continue;
                }

                // Position is too far away to plant the seed, move to it
                if (candidatePos.getManhattanDistance(villager.getBlockPos()) > NearestElements.INTERACTION_MAHNATTAN_DISTANCE) {
                    brain.remember(MemoryModuleType.LOOK_TARGET, new BlockPosLookTarget(candidatePos));
                    brain.remember(ModMemoryModuleType.LOOK_TARGET, new BlockPosLookTarget(candidatePos));

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
