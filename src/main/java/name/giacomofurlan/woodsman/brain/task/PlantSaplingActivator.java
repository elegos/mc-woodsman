package name.giacomofurlan.woodsman.brain.task;

import java.util.HashMap;
import java.util.Optional;

import name.giacomofurlan.woodsman.util.WorldUtil;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

public class PlantSaplingActivator extends WalkableActivator {
    protected int searchRadius;

    public static final HashMap<Item, Block> SAPLINGS_MAP = new HashMap<>(){{
        put(Items.ACACIA_SAPLING, Blocks.ACACIA_SAPLING);
        put(Items.BIRCH_SAPLING, Blocks.BIRCH_SAPLING);
        put(Items.CHERRY_SAPLING, Blocks.CHERRY_SAPLING);
        put(Items.DARK_OAK_SAPLING, Blocks.DARK_OAK_SAPLING);
        put(Items.JUNGLE_SAPLING, Blocks.JUNGLE_SAPLING);
        put(Items.OAK_SAPLING, Blocks.OAK_SAPLING);
        put(Items.SPRUCE_SAPLING, Blocks.SPRUCE_SAPLING);
    }};

    public PlantSaplingActivator(int searchRadius, float walkSpeed) {
        super(walkSpeed);

        this.searchRadius = searchRadius;
    }

    @Override
    public boolean run(VillagerEntity entity, Brain<VillagerEntity> brain) {
        if (walkRoutine(entity)) {
            return true;
        }

        World world = entity.getWorld();

        SimpleInventory inventory = entity.getInventory();

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

        Optional<BlockPos> walkTarget = getWalkTarget();
        // walkTarget is set, and so we're arrived at the target (if not we'd have early returned at the beginning of the method)
        if (walkTarget.isPresent()) {
            world.setBlockState(walkTarget.get(), SAPLINGS_MAP.get(sapling.getItem()).getDefaultState());
            inventory.removeItem(sapling.getItem(), 1);
            stopWalking(entity);

            return true;
        }

        Optional<BlockPos> candidatePos = WorldUtil.getBlockPos(
            new Box(brain.getOptionalMemory(MemoryModuleType.JOB_SITE).get().getPos()).expand(searchRadius),
            true
        )
            .stream()
            .filter(pos -> {
                return world.getBlockState(pos).isAir()
                    && world.getStatesInBox(new Box(pos).expand(5, 0, 5))
                        .map(state -> state.isAir() || state.isIn(BlockTags.DIRT))
                        .reduce(true, (acc, val) -> acc && val)
                    && world.getBlockState(pos.down()).isIn(BlockTags.DIRT);
            }).findFirst();

        // no block where to plant
        if (candidatePos.isEmpty()) {
            return false;
        }

        startWalking(entity, candidatePos.get());

        return true;
    }
}
