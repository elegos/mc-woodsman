package name.giacomofurlan.woodsman.datagen;

import java.util.concurrent.CompletableFuture;

import name.giacomofurlan.woodsman.Woodsman;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider.BlockTagProvider;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public class ModBlockTagsProvider extends BlockTagProvider {
    public ModBlockTagsProvider(FabricDataOutput output, CompletableFuture<WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    public static final TagKey<Block> STORAGE_BLOCKS = TagKey.of(RegistryKeys.BLOCK, new Identifier(Woodsman.MOD_ID + ":storage_blocks"));

    @Override
    protected void configure(WrapperLookup arg) {
        getOrCreateTagBuilder(STORAGE_BLOCKS)
            .add(Blocks.BARREL)
            .add(Blocks.CHEST)
            .add(Blocks.ENDER_CHEST)
            .add(Blocks.SHULKER_BOX)
            .add(Blocks.TRAPPED_CHEST);
    }

}
