package dev.imabad.theatrical.blockentities;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import dev.imabad.theatrical.TheatricalExpectPlatform;
import dev.imabad.theatrical.TheatricalRegistry;
import dev.imabad.theatrical.blockentities.control.BasicLightingDeskBlockEntity;
import dev.imabad.theatrical.blockentities.interfaces.ArtNetInterfaceBlockEntity;
import dev.imabad.theatrical.blockentities.interfaces.RedstoneInterfaceBlockEntity;
import dev.imabad.theatrical.blockentities.light.FresnelBlockEntity;
import dev.imabad.theatrical.blockentities.light.LEDPanelBlockEntity;
import dev.imabad.theatrical.blockentities.light.MovingLightBlockEntity;
import dev.imabad.theatrical.blocks.Blocks;
import dev.imabad.theatrical.blocks.control.BasicLightingDeskBlock;
import dev.imabad.theatrical.blocks.light.LEDPanelBlock;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class BlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = TheatricalRegistry.get(Registries.BLOCK_ENTITY_TYPE);
    public static final RegistrySupplier<BlockEntityType<MovingLightBlockEntity>> MOVING_LIGHT = BLOCK_ENTITIES.register("moving_light", () -> BlockEntityType.Builder.of(MovingLightBlockEntity::new, Blocks.MOVING_LIGHT_BLOCK.get()).build(null));
    public static final RegistrySupplier<BlockEntityType<ArtNetInterfaceBlockEntity>> ART_NET_INTERFACE = BLOCK_ENTITIES.register("artnet_interface", () -> BlockEntityType.Builder.of(ArtNetInterfaceBlockEntity::new, Blocks.ART_NET_INTERFACE.get()).build(null));
    public static final RegistrySupplier<BlockEntityType<FresnelBlockEntity>> LED_FRESNEL = BLOCK_ENTITIES.register("led_fresnel", () -> BlockEntityType.Builder.of(FresnelBlockEntity::new, Blocks.LED_FRESNEL.get()).build(null));

    public static final RegistrySupplier<BlockEntityType<RedstoneInterfaceBlockEntity>> REDSTONE_INTERFACE = BLOCK_ENTITIES.register("redstone_interface", () -> BlockEntityType.Builder.of(RedstoneInterfaceBlockEntity::new, Blocks.REDSTONE_INTERFACE.get()).build(null));
    public static final RegistrySupplier<BlockEntityType<LEDPanelBlockEntity>> LED_PANEL = BLOCK_ENTITIES.register("led_panel", () -> BlockEntityType.Builder.of(LEDPanelBlockEntity::new, Blocks.LED_PANEL.get()).build(null));
    public static final RegistrySupplier<BlockEntityType<BasicLightingDeskBlockEntity>> BASIC_LIGHTING_DESK = BLOCK_ENTITIES.register("basic_lighting_desk", () -> BlockEntityType.Builder.of(BasicLightingDeskBlockEntity::new, Blocks.BASIC_LIGHTING_DESK.get()).build(null));
}
