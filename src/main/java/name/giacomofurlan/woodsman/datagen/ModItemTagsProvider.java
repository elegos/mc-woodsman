package name.giacomofurlan.woodsman.datagen;

import java.util.concurrent.CompletableFuture;

import name.giacomofurlan.woodsman.Woodsman;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider.ItemTagProvider;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public class ModItemTagsProvider extends ItemTagProvider {
    public static final TagKey<Item> STRIPPED_LOGS = TagKey.of(RegistryKeys.ITEM, new Identifier(Woodsman.MOD_ID + ":stripped_logs"));
    
    public ModItemTagsProvider(FabricDataOutput output, CompletableFuture<WrapperLookup> completableFuture) {
        super(output, completableFuture);
    }

    @Override
    protected void configure(WrapperLookup arg) {
        getOrCreateTagBuilder(STRIPPED_LOGS)
            .add(Items.STRIPPED_ACACIA_LOG)
            .add(Items.STRIPPED_BIRCH_LOG)
            .add(Items.STRIPPED_CHERRY_LOG)
            .add(Items.STRIPPED_DARK_OAK_LOG)
            .add(Items.STRIPPED_JUNGLE_LOG)
            .add(Items.STRIPPED_MANGROVE_LOG)
            .add(Items.STRIPPED_OAK_LOG)
            .add(Items.STRIPPED_SPRUCE_LOG);
    }

}
