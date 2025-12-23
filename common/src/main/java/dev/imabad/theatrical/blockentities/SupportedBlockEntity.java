package dev.imabad.theatrical.blockentities;

import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;

public interface SupportedBlockEntity {
    Optional<BlockState> getSupportingStructure();
}
