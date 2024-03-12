package name.giacomofurlan.woodsman.brain.task;

import java.util.Optional;

import com.google.common.collect.ImmutableList;

import net.minecraft.block.BlockState;
import net.minecraft.entity.ai.brain.BlockPosLookTarget;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.LookTarget;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

/**
 * Cut the leaves and the logs over and next to the villager.
 * This resolves some path finding problems.
 */
public class CutTreeAroundActivator implements IActivator {

    @Override
    public boolean run(VillagerEntity entity, Brain<VillagerEntity> brain) {
        Direction facing = entity.getHorizontalFacing();
        World world = entity.getWorld();

        ImmutableList<BlockPos> leafPos = ImmutableList.of();

        Optional<LookTarget> lookTarget = brain.getOptionalMemory(MemoryModuleType.LOOK_TARGET);
        if (lookTarget.isPresent()) {
            leafPos = ImmutableList.of(lookTarget.get().getBlockPos().mutableCopy().add(0, -1, 0)); // get the block under the target (i.e. to let the sapling or log fall down)
        }

        leafPos = ImmutableList.<BlockPos>builder()
            .addAll(leafPos)
            .add(entity.getBlockPos().mutableCopy().add(0, 2, 0))
            .add(entity.getBlockPos().mutableCopy().add(facing.getOffsetX(), 2, facing.getOffsetZ()))
            .build();

        for (BlockPos pos : leafPos) {
            BlockState blockState = world.getBlockState(pos);
            if (blockState.isIn(BlockTags.LEAVES) || blockState.isIn(BlockTags.LOGS_THAT_BURN)) {
                world.breakBlock(pos, true);
                brain.remember(MemoryModuleType.LOOK_TARGET, new BlockPosLookTarget(pos));

                return true;
            }
        }

        return false;
    }

}
