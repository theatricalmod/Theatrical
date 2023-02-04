package dev.imabad.theatrical;

import dev.architectury.registry.registries.DeferredRegister;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.ArrayList;
import java.util.List;

public class TheatricalRegistry {
    public static <T>DeferredRegister<T> register(ResourceKey<Registry<T>> registry) {
        return DeferredRegister.create(Theatrical.MOD_ID, registry);
    }
}
