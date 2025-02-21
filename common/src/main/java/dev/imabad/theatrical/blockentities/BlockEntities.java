package dev.imabad.theatrical.blockentities;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import dev.imabad.theatrical.TheatricalRegistry;
import dev.imabad.theatrical.blockentities.control.BasicLightingDeskBlockEntity;
import dev.imabad.theatrical.blockentities.interfaces.ArtNetInterfaceBlockEntity;
import dev.imabad.theatrical.blockentities.interfaces.RedstoneInterfaceBlockEntity;
import dev.imabad.theatrical.blockentities.light.FresnelBlockEntity;
import dev.imabad.theatrical.blockentities.light.LEDPanel2BlockEntity;
import dev.imabad.theatrical.blockentities.light.RGBbarBlockEntity;
import dev.imabad.theatrical.blockentities.light.LEDPanelBlockEntity;
import dev.imabad.theatrical.blockentities.light.ParLedBlockEntity;
import dev.imabad.theatrical.blockentities.light.BigPanelBlockEntity;
import dev.imabad.theatrical.blockentities.light.BigPanel2BlockEntity;
import dev.imabad.theatrical.blockentities.light.MovingLightBlockEntity;
import dev.imabad.theatrical.blockentities.light.MovingWashBlockEntity;
import dev.imabad.theatrical.blockentities.light.MovingVL2CBlockEntity;
import dev.imabad.theatrical.blockentities.light.MovingVL6BlockEntity;
import dev.imabad.theatrical.blockentities.light.MovingBeamBlockEntity;
import dev.imabad.theatrical.blockentities.light.MovingScanBlockEntity;
import dev.imabad.theatrical.blockentities.light.LEDfountainBlockEntity;
import dev.imabad.theatrical.blocks.Blocks;
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
    public static final RegistrySupplier<BlockEntityType<MovingWashBlockEntity>> MOVING_WASH = BLOCK_ENTITIES.register("moving_wash", () -> BlockEntityType.Builder.of(MovingWashBlockEntity::new, Blocks.MOVING_WASH_BLOCK.get()).build(null));
    public static final RegistrySupplier<BlockEntityType<MovingVL2CBlockEntity>> MOVING_VL2C = BLOCK_ENTITIES.register("moving_vl2c", () -> BlockEntityType.Builder.of(MovingVL2CBlockEntity::new, Blocks.MOVING_VL2C_BLOCK.get()).build(null));
    public static final RegistrySupplier<BlockEntityType<MovingVL6BlockEntity>> MOVING_VL6 = BLOCK_ENTITIES.register("moving_vl6", () -> BlockEntityType.Builder.of(MovingVL6BlockEntity::new, Blocks.MOVING_VL6_BLOCK.get()).build(null));
    public static final RegistrySupplier<BlockEntityType<MovingBeamBlockEntity>> MOVING_BEAM = BLOCK_ENTITIES.register("moving_beam", () -> BlockEntityType.Builder.of(MovingBeamBlockEntity::new, Blocks.MOVING_BEAM_BLOCK.get()).build(null));
    public static final RegistrySupplier<BlockEntityType<MovingScanBlockEntity>> MOVING_SCAN = BLOCK_ENTITIES.register("moving_scan", () -> BlockEntityType.Builder.of(MovingScanBlockEntity::new, Blocks.MOVING_SCAN_BLOCK.get()).build(null));
    public static final RegistrySupplier<BlockEntityType<RGBbarBlockEntity>> RGB_BAR = BLOCK_ENTITIES.register("rgb_bar", () -> BlockEntityType.Builder.of(RGBbarBlockEntity::new, Blocks.RGB_BAR.get()).build(null));
    public static final RegistrySupplier<BlockEntityType<LEDfountainBlockEntity>> LED_FOUNTAIN = BLOCK_ENTITIES.register("led_fountain", () -> BlockEntityType.Builder.of(LEDfountainBlockEntity::new, Blocks.LED_FOUNTAIN.get()).build(null));
    public static final RegistrySupplier<BlockEntityType<LEDPanel2BlockEntity>> LED_PANEL_2 = BLOCK_ENTITIES.register("led_panel_2", () -> BlockEntityType.Builder.of(LEDPanel2BlockEntity::new, Blocks.LED_PANEL_2.get()).build(null));
    public static final RegistrySupplier<BlockEntityType<BigPanelBlockEntity>> BIG_PANEL = BLOCK_ENTITIES.register("big_panel", () -> BlockEntityType.Builder.of(BigPanelBlockEntity::new, Blocks.BIG_PANEL.get()).build(null));
    public static final RegistrySupplier<BlockEntityType<BigPanel2BlockEntity>> BIG_PANEL2 = BLOCK_ENTITIES.register("big_panel2", () -> BlockEntityType.Builder.of(BigPanel2BlockEntity::new, Blocks.BIG_PANEL2.get()).build(null));
    public static final RegistrySupplier<BlockEntityType<ParLedBlockEntity>> PAR_LED = BLOCK_ENTITIES.register("par_led", () -> BlockEntityType.Builder.of(ParLedBlockEntity::new, Blocks.PAR_LED.get()).build(null));


}
