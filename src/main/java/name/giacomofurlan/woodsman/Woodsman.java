package name.giacomofurlan.woodsman;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import name.giacomofurlan.woodsman.block.ModBlocks;
import name.giacomofurlan.woodsman.brain.ModMemoryModuleType;
import name.giacomofurlan.woodsman.item.ModItemGroups;
import name.giacomofurlan.woodsman.item.ModItems;
import name.giacomofurlan.woodsman.villager.ModVillagers;
import net.fabricmc.api.ModInitializer;

public class Woodsman implements ModInitializer {
	public static final String MOD_ID = "gf-woodsman";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		ModItemGroups.registerItemGroups();
		ModItems.registerModItems();
		ModBlocks.registerModBlocks();
		ModVillagers.registerVillagers();
		ModMemoryModuleType.registerMemoryModuleTypes();
	}
}