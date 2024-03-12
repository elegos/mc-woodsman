package name.giacomofurlan.woodsman.brain.task;

import java.util.Optional;

import com.google.common.collect.ImmutableList;

import name.giacomofurlan.woodsman.brain.ModMemoryModuleType;
import name.giacomofurlan.woodsman.util.WorldUtil;
import net.minecraft.block.BlockState;
import net.minecraft.entity.ai.brain.BlockPosLookTarget;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.LookTarget;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
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

        Optional<LookTarget> lookTarget = brain.getOptionalMemory(ModMemoryModuleType.LOOK_TARGET);
        if (lookTarget.isPresent()) {
            LookTarget target = lookTarget.get();

            if (world.getBlockState(target.getBlockPos()).isAir()) {
                leafPos = ImmutableList.copyOf(
                    WorldUtil.getBlockPos(
                            Box.enclosing(target.getBlockPos(), target.getBlockPos().mutableCopy().setY((int) entity.getY()))
                        ).stream()
                            .filter(pos -> world.getBlockState(pos).isIn(BlockTags.LEAVES) || world.getBlockState(pos).isIn(BlockTags.LOGS_THAT_BURN))
                            .toList()
                );
            }
        }

        leafPos = ImmutableList.<BlockPos>builder()
            .addAll(leafPos)
            .add(entity.getBlockPos().mutableCopy().add(0, 4, 0))
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
