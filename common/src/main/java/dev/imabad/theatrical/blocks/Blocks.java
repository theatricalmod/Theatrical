package dev.imabad.theatrical.blocks;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import dev.imabad.theatrical.TheatricalRegistry;
import dev.imabad.theatrical.blocks.control.BasicLightingDeskBlock;
import dev.imabad.theatrical.blocks.interfaces.ArtNetInterfaceBlock;
import dev.imabad.theatrical.blocks.interfaces.RedstoneInterfaceBlock;
import dev.imabad.theatrical.blocks.light.FresnelBlock;
import dev.imabad.theatrical.blocks.light.LEDPanel2Block;
import dev.imabad.theatrical.blocks.light.RGBbarBlock;
import dev.imabad.theatrical.blocks.light.LEDPanelBlock;
import dev.imabad.theatrical.blocks.light.LEDfountainBlock;
import dev.imabad.theatrical.blocks.light.BigPanelBlock;
import dev.imabad.theatrical.blocks.light.BigPanel2Block;
import dev.imabad.theatrical.blocks.light.MovingLightBlock;
import dev.imabad.theatrical.blocks.light.MovingWashBlock;
import dev.imabad.theatrical.blocks.light.MovingVL2CBlock;
import dev.imabad.theatrical.blocks.light.MovingVL6Block;
import dev.imabad.theatrical.blocks.light.ParLedBlock;
import dev.imabad.theatrical.blocks.light.MovingBeamBlock;
import dev.imabad.theatrical.blocks.light.MovingScanBlock;
import dev.imabad.theatrical.blocks.rigging.TankTrapBlock;
import dev.imabad.theatrical.blocks.rigging.TrussBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;

public class Blocks {

    public static final DeferredRegister<Block> BLOCKS = TheatricalRegistry.get(Registries.BLOCK);
    public static final RegistrySupplier<Block> MOVING_LIGHT_BLOCK = BLOCKS.register("moving_light", MovingLightBlock::new);
    public static final RegistrySupplier<Block> MOVING_WASH_BLOCK = BLOCKS.register("moving_wash", MovingWashBlock::new);
    public static final RegistrySupplier<Block> MOVING_VL2C_BLOCK = BLOCKS.register("moving_vl2c", MovingVL2CBlock::new);
    public static final RegistrySupplier<Block> MOVING_VL6_BLOCK = BLOCKS.register("moving_vl6", MovingVL6Block::new);
    public static final RegistrySupplier<Block> MOVING_BEAM_BLOCK = BLOCKS.register("moving_beam", MovingBeamBlock::new);
    public static final RegistrySupplier<Block> MOVING_SCAN_BLOCK = BLOCKS.register("moving_scan", MovingScanBlock::new);
    public static final RegistrySupplier<Block> PIPE_BLOCK = BLOCKS.register("pipe", dev.imabad.theatrical.blocks.rigging.PipeBlock::new);
    public static final RegistrySupplier<Block> ART_NET_INTERFACE = BLOCKS.register("artnet_interface", ArtNetInterfaceBlock::new);
    public static final RegistrySupplier<Block> LED_FRESNEL = BLOCKS.register("led_fresnel", FresnelBlock::new);
    public static final RegistrySupplier<Block> LED_PANEL_2 = BLOCKS.register("led_panel_2", LEDPanel2Block::new);
    public static final RegistrySupplier<Block> PAR_LED = BLOCKS.register("par_led", ParLedBlock::new);
    public static final RegistrySupplier<Block> LED_FOUNTAIN = BLOCKS.register("led_fountain", LEDfountainBlock::new);
    public static final RegistrySupplier<Block> RGB_BAR = BLOCKS.register("rgb_bar", RGBbarBlock::new);
    public static final RegistrySupplier<Block> BIG_PANEL = BLOCKS.register("big_panel", BigPanelBlock::new);
    public static final RegistrySupplier<Block> BIG_PANEL2 = BLOCKS.register("big_panel2", BigPanel2Block::new);
    public static final RegistrySupplier<RotatedPillarBlock> TRUSS_BLOCK = BLOCKS.register("truss", TrussBlock::new);
    public static final RegistrySupplier<Block> REDSTONE_INTERFACE = BLOCKS.register("redstone_interface", RedstoneInterfaceBlock::new);
    public static final RegistrySupplier<Block> TANK_TRAP = BLOCKS.register("tank_trap", TankTrapBlock::new);
    public static final RegistrySupplier<Block> LED_PANEL = BLOCKS.register("led_panel", LEDPanelBlock::new);
    public static final RegistrySupplier<Block> BASIC_LIGHTING_DESK = BLOCKS.register("basic_lighting_desk", BasicLightingDeskBlock::new);
    public static Boolean neverAllowSpawn(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, EntityType<?> entity) {
        return false;
    }
}
