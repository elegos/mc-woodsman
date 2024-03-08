package name.giacomofurlan.woodsman.item;

import name.giacomofurlan.woodsman.Woodsman;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ModItemGroups {
    public static final ItemGroup WOODSMAN = Registry.register(
        Registries.ITEM_GROUP,
        new Identifier(Woodsman.MOD_ID, "chop_block"),
        FabricItemGroup.builder()
            .displayName(Text.translatable("itemgroup.woodsman"))
            .icon(() -> new ItemStack(ModItems.CHOP_BLOCK)).entries((displayContext, entries) -> {
                entries.add(ModItems.CHOP_BLOCK);
            }).build()
    );

    public static void registerItemGroups() {
        Woodsman.LOGGER.info("Registering Item Groups for {}", Woodsman.MOD_ID);
    }
}
