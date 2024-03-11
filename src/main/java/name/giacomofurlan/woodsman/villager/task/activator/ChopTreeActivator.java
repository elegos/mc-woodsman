package name.giacomofurlan.woodsman.villager.task.activator;

import java.util.Optional;

import name.giacomofurlan.woodsman.util.NearestElements;
import net.minecraft.entity.ai.brain.BlockPosLookTarget;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ChopTreeActivator implements IActivator {

    @Override
    public boolean run(VillagerEntity entity, Brain<VillagerEntity> brain) {
        if (brain.getOptionalMemory(MemoryModuleType.WALK_TARGET).isPresent()) {
            return false;
        }

        World world = entity.getWorld();

        Optional<BlockPos> treeBlockInReach = NearestElements.nearestTaggedBlockInReach(world, entity.getBlockPos(), BlockTags.LOGS_THAT_BURN);

        if (treeBlockInReach.isEmpty()) {
            return false;
        }

        BlockPos treeLogPos = treeBlockInReach.get();
        brain.remember(MemoryModuleType.LOOK_TARGET, new BlockPosLookTarget(treeLogPos));

        world.breakBlock(treeLogPos, true, entity);

        return true;
    }

}
