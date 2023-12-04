package dev.imabad.theatrical.blocks.rigging;

import dev.imabad.theatrical.api.Support;
import dev.imabad.theatrical.blocks.Blocks;
import dev.imabad.theatrical.blocks.HangableBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.ticks.ScheduledTick;

public class TrussBlock extends RotatedPillarBlock implements SimpleWaterloggedBlock, Support {

    private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 16, 16);

    public TrussBlock() {
        super(Properties.of()
                .requiresCorrectToolForDrops()
                .strength(3, 3)
                .noOcclusion()
                .isValidSpawn(Blocks::neverAllowSpawn)
                .mapColor(MapColor.METAL)
                .sound(SoundType.METAL));
        this.registerDefaultState(this.getStateDefinition().any().setValue(AXIS, Direction.Axis.X).setValue(BlockStateProperties.WATERLOGGED, false));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        FluidState iFluidState = context.getLevel().getFluidState(context.getClickedPos());
        return this.defaultBlockState().setValue(AXIS, context.getClickedFace().getAxis())
                .setValue(BlockStateProperties.WATERLOGGED, iFluidState.getType() == Fluids.WATER);
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(BlockStateProperties.WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }


    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AXIS, BlockStateProperties.WATERLOGGED);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if(state.getValue(BlockStateProperties.WATERLOGGED)){
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }
    @Override
    public float[] getHookTransforms(LevelReader levelReader, BlockPos pos, Direction facing) {
        return new float[]{0, 0F, 0};
    }

    @Override
    public float getShadeBrightness(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
        return 1.0F;
    }

    @Override
    public boolean propagatesSkylightDown(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
        return true;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if(!player.getItemInHand(hand).isEmpty()){
            Item item = player.getItemInHand(hand).getItem();
            if (item instanceof BlockItem blockItem) {
                if(blockItem.getBlock() instanceof HangableBlock hangableBlock){
                    BlockPos down = pos.relative(Direction.DOWN);
                    if(!level.getBlockState(down).isAir()){
                        return InteractionResult.FAIL;
                    }
                    level.setBlock(down, hangableBlock.defaultBlockState().setValue(HangableBlock.FACING, player.getDirection()), Block.UPDATE_CLIENTS);
                    if(!player.isCreative()) {
                        if (player.getItemInHand(hand).getCount() > 1) {
                            player.getItemInHand(hand).setCount(player.getItemInHand(hand).getCount() - 1);
                        } else {
                            player.setItemInHand(hand, new ItemStack(Items.AIR));
                        }
                    }
                    return InteractionResult.CONSUME;
                }
            }
        }
        return super.use(state, level, pos, player, hand, hit);
    }
}
