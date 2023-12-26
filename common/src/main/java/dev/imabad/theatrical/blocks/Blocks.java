package dev.imabad.theatrical.blocks;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import dev.imabad.theatrical.TheatricalRegistry;
import dev.imabad.theatrical.blocks.interfaces.ArtNetInterfaceBlock;
import dev.imabad.theatrical.blocks.interfaces.RedstoneInterfaceBlock;
import dev.imabad.theatrical.blocks.light.FresnelBlock;
import dev.imabad.theatrical.blocks.light.LEDPanelBlock;
import dev.imabad.theatrical.blocks.light.MovingLightBlock;
import dev.imabad.theatrical.blocks.rigging.TrussBlock;
import dev.imabad.theatrical.blocks.rigging.TankTrapBlock;
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
    public static final RegistrySupplier<Block> PIPE_BLOCK = BLOCKS.register("pipe", dev.imabad.theatrical.blocks.rigging.PipeBlock::new);
    public static final RegistrySupplier<Block> ART_NET_INTERFACE = BLOCKS.register("artnet_interface", ArtNetInterfaceBlock::new);
    public static final RegistrySupplier<Block> LED_FRESNEL = BLOCKS.register("led_fresnel", FresnelBlock::new);
    public static final RegistrySupplier<RotatedPillarBlock> TRUSS_BLOCK = BLOCKS.register("truss", TrussBlock::new);
    public static final RegistrySupplier<Block> REDSTONE_INTERFACE = BLOCKS.register("redstone_interface", RedstoneInterfaceBlock::new);
    public static final RegistrySupplier<Block> TANK_TRAP = BLOCKS.register("tank_trap", TankTrapBlock::new);
    public static final RegistrySupplier<Block> LED_PANEL = BLOCKS.register("led_panel", LEDPanelBlock::new);
    public static Boolean neverAllowSpawn(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, EntityType<?> entity) {
        return false;
    }
}
