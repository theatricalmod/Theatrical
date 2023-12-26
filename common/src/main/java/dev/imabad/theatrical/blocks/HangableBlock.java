package dev.imabad.theatrical.blocks;

import dev.imabad.theatrical.api.Support;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.ticks.ScheduledTick;
import org.jetbrains.annotations.Nullable;

public abstract class HangableBlock extends HorizontalDirectionalBlock {

    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    public static final BooleanProperty BROKEN = BooleanProperty.create("broken");
    public static final DirectionProperty HANG_DIRECTION = DirectionProperty.create("hang_direction");
    public static final BooleanProperty HANGING = BooleanProperty.create("hanging");

    protected HangableBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.getStateDefinition().any().setValue(FACING, Direction.NORTH)
                .setValue(BROKEN, false).setValue(HANG_DIRECTION, Direction.UP).setValue(HANGING, false));
    }
    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        return this.defaultBlockState().setValue(FACING, blockPlaceContext.getHorizontalDirection().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING).add(BROKEN).add(HANG_DIRECTION).add(HANGING);
    }

    @Override
    public boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
        return isHanging(levelReader, blockPos);
    }

    @Override
    public BlockState updateShape(BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2) {
        levelAccessor.getBlockTicks().schedule(new ScheduledTick<>(this, blockPos, 3, 0));
        return super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
    }

    public boolean isHanging(LevelReader levelReader, BlockPos pos){
        BlockState blockState = levelReader.getBlockState(pos);
        if(!blockState.isAir() && blockState.getValue(HANGING)){
            BlockPos offset = pos.relative(blockState.getValue(HANG_DIRECTION));
            return !levelReader.isEmptyBlock(offset) && levelReader.getBlockState(offset).getBlock() instanceof Support;
        }
        return false;
    }

    @Override
    public void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
        if(!blockState.getValue(BROKEN) && !canSurvive(blockState, serverLevel, blockPos)){
            //TODO: Make light fall.
        }
    }

    public abstract Direction getLightFacing(Direction hangDirection, Player placingPlayer);
}
