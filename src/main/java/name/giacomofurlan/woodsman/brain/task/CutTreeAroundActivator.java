package name.giacomofurlan.woodsman.brain.task;

import java.util.List;

import com.google.common.collect.ImmutableList;

import net.minecraft.block.BlockState;
import net.minecraft.entity.ai.brain.BlockPosLookTarget;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Cut the leaves and the logs over and next to the villager.
 * This resolves some path finding problems.
 */
public class CutTreeAroundActivator implements IActivator {

    @Override
    public boolean shouldRun(VillagerEntity entity) {
        return false;
    }

    @Override
    public boolean run(VillagerEntity entity) {
        World world = entity.getWorld();

        ImmutableList.Builder<BlockPos> leafBuilder = ImmutableList.<BlockPos>builder();

        Path currentPath = entity.getNavigation().getCurrentPath();
        if (currentPath != null && !currentPath.isFinished()) {
            BlockPos currentPathNextPos = currentPath.getCurrentNodePos();
            leafBuilder.addAll(List.of(currentPathNextPos, currentPathNextPos.up()));
        }

        ImmutableList<BlockPos> leafPos = leafBuilder
            .add(entity.getBlockPos().up().up())
            .build();

        Brain<VillagerEntity> brain = entity.getBrain();
        for (BlockPos pos : leafPos) {
            BlockState blockState = world.getBlockState(pos);
            if (blockState.isIn(BlockTags.LEAVES) || blockState.isIn(BlockTags.LOGS_THAT_BURN)) {
                world.breakBlock(pos, true);
                brain.remember(MemoryModuleType.LOOK_TARGET, new BlockPosLookTarget(pos));

            }
        }

        return false;
    }

}
