package name.giacomofurlan.woodsman.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import net.minecraft.entity.Entity;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class NearestElements {
    public static int INTERACTION_MAHNATTAN_DISTANCE = 6;

    /**
     * Attempt to find the nearest tree from the given entity.
     * @param entity the entity which is searching for the nearest tree.
     * @param searchRadius The search radius the entity searches within.
     * @param operativeDistance The operative (manhattan) distance from the opCenter the villager works within.
     * @param needsToBeReachable if true, the candidate needs to be reachable from a path point of view.
     * @return 
     */
    public static Optional<BlockPos> getNearestTree(Entity entity, int searchRadius, boolean needsToBeReachable) {
        return getNearestTree(entity, searchRadius, needsToBeReachable, false);
    }

    public static Optional<BlockPos> getNearestTree(Entity entity, int searchRadius, boolean needsToBeReachable, boolean includeSaplings) {
        if (entity == null) {
            return Optional.empty();
        }

        Vec3d startingPos = entity.getPos();
        World world = entity.getWorld();

        int startX = (int)startingPos.getX();
        int startY = (int)startingPos.getY();
        int startZ = (int)startingPos.getZ();

        List<BlockPos> candidatePos = new ArrayList<>();
        for (int x = startX - searchRadius; x <= startX + searchRadius; x++) {
            for (int y = startY - searchRadius; y <= startY + searchRadius; y++) {
                for (int z = startZ - searchRadius; z <= startZ + searchRadius; z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    if (world.getBlockState(pos).isIn(BlockTags.LOGS_THAT_BURN)) {
                        candidatePos.add(pos);
                    }
                }
            }
        }

        for (int distance = 0; distance <= searchRadius; distance++) {
            Optional<BlockPos> optCandidate = WorldUtil.cubicCoordinatesFromCenter(startingPos, distance)
                .stream()
                .filter(pos -> {
                    if (!candidatePos.contains(pos)) {
                        return false;
                    }

                    // Path path = ((PathAwareEntity) entity).getNavigation().findPathTo(pos, 0, INTERACTION_MAHNATTAN_DISTANCE);

                    // return path != null && path.reachesTarget();

                    return true;
                })
                .findFirst();
            
            if (optCandidate.isPresent()) {
                return optCandidate;
            }
        }

        return Optional.empty();
    }
}
