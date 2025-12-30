package dev.imabad.theatrical.blocks;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import dev.imabad.theatrical.Theatrical;
import dev.imabad.theatrical.TheatricalRegistry;
import dev.imabad.theatrical.blocks.control.BasicLightingDeskBlock;
import dev.imabad.theatrical.blocks.interfaces.RedstoneInterfaceBlock;
import dev.imabad.theatrical.blocks.light.FresnelBlock;
import dev.imabad.theatrical.blocks.light.LEDPanelBlock;
import dev.imabad.theatrical.blocks.light.MovingLightBlock;
import dev.imabad.theatrical.blocks.light.MovingWashBlock;
import dev.imabad.theatrical.blocks.rigging.TankTrapBlock;
import dev.imabad.theatrical.blocks.rigging.TrussBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Function;
import java.util.function.Supplier;

public class Blocks {

    public static final DeferredRegister<Block> BLOCKS = TheatricalRegistry.get(Registries.BLOCK);
    public static final RegistrySupplier<Block> MOVING_LIGHT_BLOCK = register("moving_light", MovingLightBlock::new);
    public static final RegistrySupplier<Block> MOVING_WASH_BLOCK = register("moving_wash", MovingWashBlock::new);
    public static final RegistrySupplier<Block> PIPE_BLOCK = register("pipe", dev.imabad.theatrical.blocks.rigging.PipeBlock::new);
    public static final RegistrySupplier<Block> LED_FRESNEL = register("led_fresnel", FresnelBlock::new);
    public static final RegistrySupplier<RotatedPillarBlock> TRUSS_BLOCK = register("truss", TrussBlock::new);
    public static final RegistrySupplier<Block> REDSTONE_INTERFACE = register("redstone_interface", RedstoneInterfaceBlock::new);
    public static final RegistrySupplier<Block> TANK_TRAP = register("tank_trap", TankTrapBlock::new);
    public static final RegistrySupplier<Block> LED_PANEL = register("led_panel", LEDPanelBlock::new);
    public static final RegistrySupplier<Block> BASIC_LIGHTING_DESK = register("basic_lighting_desk", BasicLightingDeskBlock::new);

    private static <T extends Block> RegistrySupplier<T> register(String id, Function<BlockBehaviour.Properties, T> supplier) {
        return BLOCKS.register(id, () -> supplier.apply(BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, Theatrical.location(id)))));
    }

    public static Boolean neverAllowSpawn(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, EntityType<?> entity) {
        return false;
    }
}
