package name.giacomofurlan.woodsman.villager;

import com.google.common.collect.ImmutableSet;

import name.giacomofurlan.woodsman.Woodsman;
import name.giacomofurlan.woodsman.block.ModBlocks;
import net.fabricmc.fabric.api.object.builder.v1.world.poi.PointOfInterestHelper;
import net.minecraft.block.Block;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.village.VillagerProfession;
import net.minecraft.world.poi.PointOfInterestType;

public class ModVillagers {
    public static final RegistryKey<PointOfInterestType> CHOP_BLOCK_POI_KEY = poiKey("chop_block_poi");
    public static final PointOfInterestType CHOP_BLOCK_POI = registerPoi("chop_block_poi", ModBlocks.CHOP_BLOCK);
    public static final VillagerProfession WOODSMAN = registerProfession("woodsman", CHOP_BLOCK_POI_KEY);

    private static VillagerProfession registerProfession(String name, RegistryKey<PointOfInterestType> type) {
        return Registry.register(
            Registries.VILLAGER_PROFESSION,
            new Identifier(Woodsman.MOD_ID, name),
            new VillagerProfession(
                name,
                entry -> entry.matchesKey(type),
                entry -> entry.matchesKey(type),
                ImmutableSet.of(),
                ImmutableSet.of(),
                SoundEvents.ENTITY_VILLAGER_WORK_BUTCHER
            )
        );
    }

    private static PointOfInterestType registerPoi(String name, Block block) {
        return PointOfInterestHelper.register(new Identifier(Woodsman.MOD_ID, name), 1, 1, block);
    }

    private static RegistryKey<PointOfInterestType> poiKey(String name) {
        return RegistryKey.of(RegistryKeys.POINT_OF_INTEREST_TYPE, new Identifier(Woodsman.MOD_ID, name));
    }

    public static void registerVillagers() {
        Woodsman.LOGGER.info("Registering Villagers {}", Woodsman.MOD_ID);
    }
}
