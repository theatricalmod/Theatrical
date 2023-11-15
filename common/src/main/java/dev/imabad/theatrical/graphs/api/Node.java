package dev.imabad.theatrical.graphs.api;

import dev.imabad.theatrical.graphs.CableNodePos;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public interface Node {

    Collection<CableNodePos.DiscoveredPosition> getConnected(@Nullable CableNodePos from, BlockGetter level, BlockPos pos, BlockState state);

    Collection<CableNodePos.DiscoveredPosition> getPossibleNodesForSide(Direction side, BlockGetter level, BlockPos pos);
}
