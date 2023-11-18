package dev.imabad.theatrical.registry;

import dev.architectury.registry.registries.Registrar;
import dev.imabad.theatrical.Theatrical;
import dev.imabad.theatrical.api.Fixture;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Map;

public abstract class FixtureRegistry {

    public static final ResourceKey<Registry<Fixture>> REGISTRY_KEY =
        ResourceKey.createRegistryKey(new ResourceLocation(Theatrical.MOD_ID, "fixtures"));

    private static Registrar<Fixture> registry;

    public static void buildRegistry(){
        if (registry == null) {
            registry = Theatrical.MANAGER.get().get(REGISTRY_KEY);
        }
    }
    public static List<Fixture> entries(){
        return registry.entrySet().stream().<Fixture>map(Map.Entry::getValue).toList();
    }

}
