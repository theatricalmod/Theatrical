package dev.imabad.theatrical.fixtures;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import dev.imabad.theatrical.Theatrical;
import dev.imabad.theatrical.api.Fixture;
import dev.imabad.theatrical.registry.FixtureRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

public class Fixtures {

    public static final ResourceKey<Registry<Fixture>> FIXTURE_REGISTRY_KEY = ResourceKey.createRegistryKey(FixtureRegistry.REGISTRY_KEY);
    public static final DeferredRegister<Fixture> FIXTURES = DeferredRegister.create(Theatrical.MOD_ID, FIXTURE_REGISTRY_KEY);

    public static final RegistrySupplier<Fixture> MOVING_LIGHT = FIXTURES.register("moving_light", MovingLightFixture::new);

}
