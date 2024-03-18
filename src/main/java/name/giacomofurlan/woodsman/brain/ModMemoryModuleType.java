package name.giacomofurlan.woodsman.brain;

import java.util.List;
import java.util.Optional;

import com.mojang.serialization.Codec;

import name.giacomofurlan.woodsman.Woodsman;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class ModMemoryModuleType {
    public static MemoryModuleType<List<BlockPos>> TARGET_TREE = register("target_tree");
    public static MemoryModuleType<String> CURRENT_WOODSMAN_TASK = register("current_woodsman_task");

    protected static String getIdentifier(String id) {
        return new Identifier(Woodsman.MOD_ID, "memory." + id).toString();
    }

    private static <U> MemoryModuleType<U> register(String id) {
        return register(id, null);
    }

    private static <U> MemoryModuleType<U> register(String id, Codec<U> codec) {
        return Registry.register(
            Registries.MEMORY_MODULE_TYPE,
            getIdentifier(id),
            new MemoryModuleType<U>(codec != null ? Optional.of(codec) : Optional.empty())
        );
    }

    public static void registerMemoryModuleTypes() {
        Woodsman.LOGGER.info("Registering MemoryModuleTypes for {}", Woodsman.MOD_ID);
    }
}
