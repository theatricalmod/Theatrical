package dev.imabad.theatrical.registry;

import dev.architectury.registry.registries.Registrar;
import dev.architectury.registry.registries.Registries;
import dev.imabad.theatrical.Theatrical;
import dev.imabad.theatrical.api.Fixture;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Map;

public abstract class FixtureRegistry {

    public static final ResourceLocation REGISTRY_KEY = new ResourceLocation(Theatrical.MOD_ID, "fixtures");

    private static Registrar<Fixture> registry;

    public static void buildRegistry(){
        if (registry == null) {
            registry = Registries.get(Theatrical.MOD_ID)
                    .<Fixture>builder(REGISTRY_KEY).build();
        }
    }
    public static List<Fixture> entries(){
        return registry.entrySet().stream().<Fixture>map(Map.Entry::getValue).toList();
    }

}
