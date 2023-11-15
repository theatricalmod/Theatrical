package dev.imabad.theatrical.blocks;

import dev.imabad.theatrical.api.CableType;
import dev.imabad.theatrical.graphs.GlobalCableManager;
import dev.imabad.theatrical.graphs.api.Node;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.ticks.LevelTickAccess;

public abstract class NetworkNodeBlock extends Block implements Node {
    public NetworkNodeBlock(Properties properties) {
        super(properties);
    }

    public abstract CableType getAcceptedCableType(LevelAccessor level, BlockPos pos);

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        GlobalCableManager.onCableAdded(level, pos, state, getAcceptedCableType(level, pos));
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (level.isClientSide)
            return;
        LevelTickAccess<Block> blockTicks = level.getBlockTicks();
        if (!blockTicks.hasScheduledTick(pos, this))
            level.scheduleTick(pos, this, 1);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        GlobalCableManager.onCableRemoved(level, pos, state);
        super.onRemove(state, level, pos, newState, isMoving);
    }
}
