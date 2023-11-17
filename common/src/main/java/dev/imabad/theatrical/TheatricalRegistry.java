package dev.imabad.theatrical;

import dev.architectury.registry.registries.DeferredRegister;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

public class TheatricalRegistry {
    public static <T>DeferredRegister<T> register(ResourceKey<Registry<T>> registry) {
        return DeferredRegister.create(Theatrical.MOD_ID, registry);
    }
}
