package name.giacomofurlan.woodsman.datagen;

import java.util.concurrent.CompletableFuture;

import name.giacomofurlan.woodsman.Woodsman;
import net.minecraft.data.DataOutput;
import net.minecraft.data.server.tag.TagProvider;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.registry.tag.PointOfInterestTypeTags;
import net.minecraft.util.Identifier;
import net.minecraft.world.poi.PointOfInterestType;

public class ModPoiTagProvider extends TagProvider<PointOfInterestType> {
    public ModPoiTagProvider(DataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookupFuture) {
        super(output, RegistryKeys.POINT_OF_INTEREST_TYPE, registryLookupFuture);
    }

    @Override
    protected void configure(WrapperLookup var1) {
        getOrCreateTagBuilder(PointOfInterestTypeTags.ACQUIRABLE_JOB_SITE)
            .addOptional(new Identifier(Woodsman.MOD_ID, "chop_block_poi"));
    }

}
