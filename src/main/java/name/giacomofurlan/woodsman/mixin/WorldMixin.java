package name.giacomofurlan.woodsman.mixin;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import name.giacomofurlan.woodsman.util.WorldCache;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Mixin(World.class)
public class WorldMixin {
    // BlockPos pos, BlockState state, int flags, int maxUpdateDepth
    @Inject(method = "setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;II)Z", at = @At("TAIL"), cancellable = true)
    private void setBlockState(BlockPos pos, BlockState state, int flags, int maxUpdateDepth, CallbackInfoReturnable<Boolean> cir) {
        WorldCache.getInstance().updateCache(((World)(Object) this).getRegistryKey().getValue().toString(), pos, state, true);
    }

    @Inject(method = "setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;)Z", at = @At("TAIL"), cancellable = true)
    private void setBlockState(BlockPos pos, BlockState state, CallbackInfoReturnable<Boolean> cir) {
        WorldCache.getInstance().updateCache(((World)(Object) this).getRegistryKey().getValue().toString(), pos, state, true);
    }

    @Inject(method = "breakBlock", at = @At("TAIL"), cancellable = true)
    private void breakBlock(BlockPos pos, boolean drop, @Nullable Entity breakingEntity, int maxUpdateDepth, CallbackInfoReturnable<Boolean> cir) {
        @SuppressWarnings("resource")
        World world = (World)(Object) this;

        WorldCache.getInstance().updateCache(world.getRegistryKey().getValue().toString(), pos, world.getBlockState(pos), true);
    }
}
