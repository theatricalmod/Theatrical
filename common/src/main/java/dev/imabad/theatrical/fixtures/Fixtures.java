package dev.imabad.theatrical.fixtures;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import dev.imabad.theatrical.TheatricalRegistry;
import dev.imabad.theatrical.api.Fixture;
import dev.imabad.theatrical.registry.FixtureRegistry;

public class Fixtures {

    public static final DeferredRegister<Fixture> FIXTURES = TheatricalRegistry.register(FixtureRegistry.REGISTRY_KEY);

    public static final RegistrySupplier<Fixture> MOVING_LIGHT = FIXTURES.register("moving_light", MovingLightFixture::new);

}
