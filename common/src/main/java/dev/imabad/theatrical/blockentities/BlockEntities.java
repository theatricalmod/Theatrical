package dev.imabad.theatrical.blockentities;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import dev.imabad.theatrical.TheatricalRegistry;
import dev.imabad.theatrical.blockentities.control.BasicLightingDeskBlockEntity;
import dev.imabad.theatrical.blockentities.interfaces.RedstoneInterfaceBlockEntity;
import dev.imabad.theatrical.blockentities.light.FresnelBlockEntity;
import dev.imabad.theatrical.blockentities.light.LEDPanelBlockEntity;
import dev.imabad.theatrical.blockentities.light.MovingLightBlockEntity;
import dev.imabad.theatrical.blockentities.light.MovingWashBlockEntity;
import dev.imabad.theatrical.blocks.Blocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.Set;

public class BlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = TheatricalRegistry.get(Registries.BLOCK_ENTITY_TYPE);
    public static final RegistrySupplier<BlockEntityType<MovingLightBlockEntity>> MOVING_LIGHT =
            BLOCK_ENTITIES.register("moving_light", () ->
                    new BlockEntityType<>(MovingLightBlockEntity::new, Set.of(Blocks.MOVING_LIGHT_BLOCK.get())));
    public static final RegistrySupplier<BlockEntityType<FresnelBlockEntity>> LED_FRESNEL = BLOCK_ENTITIES.register("led_fresnel", () -> new BlockEntityType<>(FresnelBlockEntity::new, Set.of(Blocks.LED_FRESNEL.get())));



    public static final RegistrySupplier<BlockEntityType<RedstoneInterfaceBlockEntity>> REDSTONE_INTERFACE = BLOCK_ENTITIES.register("redstone_interface", () -> new BlockEntityType<>(RedstoneInterfaceBlockEntity::new, Set.of(Blocks.REDSTONE_INTERFACE.get())));
    public static final RegistrySupplier<BlockEntityType<LEDPanelBlockEntity>> LED_PANEL = BLOCK_ENTITIES.register("led_panel", () -> new BlockEntityType<>(LEDPanelBlockEntity::new, Set.of(Blocks.LED_PANEL.get())));
    public static final RegistrySupplier<BlockEntityType<BasicLightingDeskBlockEntity>> BASIC_LIGHTING_DESK = BLOCK_ENTITIES.register("basic_lighting_desk", () -> new BlockEntityType<>(BasicLightingDeskBlockEntity::new, Set.of(Blocks.BASIC_LIGHTING_DESK.get())));
    public static final RegistrySupplier<BlockEntityType<MovingWashBlockEntity>> MOVING_WASH = BLOCK_ENTITIES.register("moving_wash", () -> new BlockEntityType<>(MovingWashBlockEntity::new, Set.of(Blocks.MOVING_WASH_BLOCK.get())));


}
