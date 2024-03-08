package name.giacomofurlan.woodsman.block;

import name.giacomofurlan.woodsman.Woodsman;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;

public class ModBlocks {
    public static final Block CHOP_BLOCK = registerBlock(
        "chop_block_block",
        new ChopBlockBlock(
            FabricBlockSettings.copyOf(Blocks.AMETHYST_BLOCK)
                .sounds(BlockSoundGroup.WOOD)
                .requiresTool()
        )
    );

    private static Block registerBlock(String name, Block block) {
        registerBlockItem(name, block);

        return Registry.register(Registries.BLOCK, new Identifier(Woodsman.MOD_ID, name), block);
    }

    private static Item registerBlockItem(String name, Block block) {
        return Registry.register(
            Registries.ITEM,
            new Identifier(Woodsman.MOD_ID, name),
            new BlockItem(block, new FabricItemSettings())
        );
    }

    public static void registerModBlocks() {
        Woodsman.LOGGER.info("Registering ModBlocks for {}", Woodsman.MOD_ID);
    }
}
