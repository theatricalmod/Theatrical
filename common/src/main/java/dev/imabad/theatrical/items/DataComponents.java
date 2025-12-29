package dev.imabad.theatrical.items;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import dev.imabad.theatrical.TheatricalRegistry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;

public class DataComponents {

    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS = TheatricalRegistry.get(Registries.DATA_COMPONENT_TYPE);

    public static final RegistrySupplier<DataComponentType<ConfigurationCardData>> CONFIGURATION_CARD_DATA =
            DATA_COMPONENTS.register("configuration_card", () -> DataComponentType.<ConfigurationCardData>builder()
                    .persistent(ConfigurationCardData.CODEC)
                    .networkSynchronized(ConfigurationCardData.STREAM_CODEC)
                    .build());
}
