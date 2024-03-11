package name.giacomofurlan.woodsman;

import name.giacomofurlan.woodsman.datagen.ModBlockTagsProvider;
import name.giacomofurlan.woodsman.datagen.ModItemTagsProvider;
import name.giacomofurlan.woodsman.datagen.ModPoiTagProvider;
import name.giacomofurlan.woodsman.datagen.ModRecipesProvider;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

public class WoodsmanDataGenerator implements DataGeneratorEntrypoint {
	@Override
	public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
		FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();

		pack.addProvider(ModItemTagsProvider::new);
		pack.addProvider(ModBlockTagsProvider::new);
		pack.addProvider(ModRecipesProvider::new);
		pack.addProvider(ModPoiTagProvider::new);
	}
}
