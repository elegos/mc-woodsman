package name.giacomofurlan.woodsman.datagen;

import name.giacomofurlan.woodsman.item.ModItems;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.data.server.recipe.RecipeExporter;
import net.minecraft.data.server.recipe.ShapedRecipeJsonBuilder;
import net.minecraft.item.Items;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.Registries;

public class ModRecipesProvider extends FabricRecipeProvider {
    
    public ModRecipesProvider(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generate(RecipeExporter exporter) {
        ShapedRecipeJsonBuilder.create(RecipeCategory.REDSTONE, ModItems.CHOP_BLOCK, 1)
            .pattern("A")
            .pattern("L")
            .input('A', Items.IRON_AXE)
            .input('L', ModItemTagsProvider.STRIPPED_LOGS)
            .criterion(hasItem(Items.IRON_AXE), conditionsFromItem(Items.IRON_AXE))
            .offerTo(exporter, Registries.ITEM.getId(ModItems.CHOP_BLOCK));
    }

}
