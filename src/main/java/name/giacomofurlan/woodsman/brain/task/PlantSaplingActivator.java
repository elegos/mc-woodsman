package name.giacomofurlan.woodsman.brain.task;

import java.util.HashMap;
import java.util.Optional;

import name.giacomofurlan.woodsman.util.WorldCache;
import name.giacomofurlan.woodsman.util.WorldUtil;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
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

    public static final int TREE_DISTANCE = 6;

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
    public boolean shouldRun(VillagerEntity entity) {
        return false;
    }

    @Override
    public boolean run(VillagerEntity entity) {
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

        BlockPos villagerPos = entity.getBlockPos();
        Optional<BlockPos> plantablePos = WorldUtil.getBlockPos(WorldUtil.cubicBoxFromCenter(villagerPos, TREE_DISTANCE))
            .stream()
            .filter(pos -> isValidPlantPos(world, pos))
            .findAny();

        if (plantablePos.isPresent()) {
            plantSapling(entity, plantablePos.get(), sapling);

            return true;
        }

        Optional<BlockPos> candidatePos = WorldUtil.getBlockPos(
            new Box(entity.getBrain().getOptionalMemory(MemoryModuleType.JOB_SITE).get().getPos()).expand(searchRadius),
            true
        )
            .stream()
            .filter(pos -> isValidPlantPos(world, pos)).findFirst();

        // no block where to plant
        if (candidatePos.isEmpty()) {
            return false;
        }

        // BlockPos pos = candidatePos.get();

        // if (pos.getManhattanDistance(villagerPos) < 5) {
        //     plantSapling(entity, pos, sapling);

        //     return true;
        // }

        startWalking(entity, candidatePos.get());

        return true;
    }

    protected static boolean isValidPlantPos(World world, BlockPos pos) {
        WorldCache worldCache = WorldCache.getInstance();

        return worldCache.getCachedBlock(world, pos).isAir()
            && worldCache.getCachedBlock(world, pos.down()).isIn(BlockTags.DIRT)
            && worldCache.getStatesInBox(world, new Box(pos).expand(TREE_DISTANCE, 0, TREE_DISTANCE))
                .stream()
                .map(state -> !state.isIn(BlockTags.LOGS_THAT_BURN) && !state.isIn(BlockTags.SAPLINGS))
                .reduce(true, (acc, val) -> acc && val);
    }

    protected static void plantSapling(VillagerEntity entity, BlockPos pos, ItemStack sapling) {
        SimpleInventory inventory = entity.getInventory();
        World world = entity.getWorld();

        world.setBlockState(pos, SAPLINGS_MAP.get(sapling.getItem()).getDefaultState());
        inventory.removeItem(sapling.getItem(), 1);
    }
}
