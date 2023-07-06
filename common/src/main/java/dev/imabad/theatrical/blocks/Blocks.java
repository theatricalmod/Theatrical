package dev.imabad.theatrical.blocks;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import dev.imabad.theatrical.TheatricalRegistry;
import dev.imabad.theatrical.blocks.interfaces.ArtNetInterfaceBlock;
import dev.imabad.theatrical.blocks.light.MovingLightBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.state.BlockState;

public class Blocks {

    public static final DeferredRegister<Block> BLOCKS = TheatricalRegistry.register(Registry.BLOCK_REGISTRY);
    public static final RegistrySupplier<Block> MOVING_LIGHT_BLOCK = BLOCKS.register("moving_light", MovingLightBlock::new);
    public static final RegistrySupplier<Block> PIPE_BLOCK = BLOCKS.register("pipe", dev.imabad.theatrical.blocks.rigging.PipeBlock::new);
    public static final RegistrySupplier<Block> ART_NET_INTERFACE = BLOCKS.register("artnet_interface", ArtNetInterfaceBlock::new);
    public static final RegistrySupplier<Block> CABLE = BLOCKS.register("cable", CableBlock::new);
    public static Boolean neverAllowSpawn(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, EntityType<?> entity) {
        return false;
    }
}
