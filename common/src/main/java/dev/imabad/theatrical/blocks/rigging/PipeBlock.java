package dev.imabad.theatrical.blocks.rigging;

import dev.imabad.theatrical.api.Support;
import dev.imabad.theatrical.blocks.Blocks;
import dev.imabad.theatrical.blocks.HangableBlock;
import dev.imabad.theatrical.blocks.light.BaseLightBlock;
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
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class PipeBlock extends DirectionalBlock implements Support {

    public static final DirectionProperty FACING = DirectionalBlock.FACING;
    private final VoxelShape Z_BOX = Shapes.create(new AABB(0.35, 0.4, 0, 0.65, 0.6, 1));
    private final VoxelShape Z_BOX_DOWN = Shapes.create(new AABB(0, 0, 0, 1, 0.6, 1));
    private final VoxelShape X_BOX = Shapes.create(new AABB(0, 0.4, 0.4, 1, 0.6, 0.6));
    private final VoxelShape X_BOX_DOWN = Shapes.create(new AABB(0, 0, 0, 1, 0.6, 1));
    private final VoxelShape Y_BOX = Shapes.create(new AABB(0.4, 0, 0.4, 0.6, 1, 0.6));
    private final VoxelShape Y_BOX_SOUTH = Shapes.create(new AABB(0, 0, 0.4, 1, 1, 1));
    private final VoxelShape Y_BOX_NORTH = Shapes.create(new AABB(0, 0, 0, 1, 1, 0.6));
    private final VoxelShape Y_BOX_EAST = Shapes.create(new AABB(0.4, 0, 0, 1, 1, 1));
    private final VoxelShape Y_BOX_WEST = Shapes.create(new AABB(0, 0, 0, 0.6, 1, 1));

    public PipeBlock() {
        super(Properties.of()
            .requiresCorrectToolForDrops()
            .strength(3, 3)
            .noOcclusion()
            .isValidSpawn(Blocks::neverAllowSpawn)
            .mapColor(MapColor.METAL)
            .sound(SoundType.METAL));
        this.registerDefaultState(this.getStateDefinition().any().setValue(FACING, Direction.NORTH));
    }

    @Override
    public float[] getHookTransforms(LevelReader levelReader, BlockPos pos, Direction facing) {
        if(levelReader.getBlockState(pos).getValue(BaseLightBlock.HANG_DIRECTION) == Direction.UP){
            return new float[]{0, .5f, 0};
        }
        return new float[]{0, -0.35F, 0};
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if(state.getValue(FACING).getAxis() == Direction.Axis.Y){
            List<VoxelShape> shapes = new ArrayList<>();
            for(Direction dir : Direction.values()){
                if(dir.getAxis() == Direction.Axis.Y){
                    continue;
                }
                BlockState blockState = level.getBlockState(pos.relative(dir));
                if(blockState.getBlock() instanceof HangableBlock){
                   if(blockState.getValue(HangableBlock.HANG_DIRECTION) == dir.getOpposite()){
                       VoxelShape extensionShape = null;
                        switch(dir){
                            case SOUTH -> extensionShape = Y_BOX_SOUTH;
                            case NORTH -> extensionShape = Y_BOX_NORTH;
                            case EAST -> extensionShape = Y_BOX_EAST;
                            case WEST -> extensionShape = Y_BOX_WEST;
                        }
                        shapes.add(extensionShape);
                   }
                }
            }
            return Shapes.or(Y_BOX, shapes.toArray(new VoxelShape[0]));
        } else {
            if(level.getBlockState(pos.relative(Direction.DOWN)).getBlock() instanceof HangableBlock){
                VoxelShape ogShape = state.getValue(FACING).getAxis() == Direction.Axis.Z ? Z_BOX : X_BOX;
                VoxelShape extensionShape = state.getValue(FACING).getAxis() == Direction.Axis.Z ? Z_BOX_DOWN : X_BOX_DOWN;
                return Shapes.or(ogShape, extensionShape);
            }
        }
        return state.getValue(FACING).getAxis() == Direction.Axis.Z ? Z_BOX
                : state.getValue(FACING).getAxis() == Direction.Axis.Y ? Y_BOX : X_BOX;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getNearestLookingDirection().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if(!player.getItemInHand(hand).isEmpty()){
            Item item = player.getItemInHand(hand).getItem();
            if (item instanceof BlockItem blockItem) {
                if(blockItem.getBlock() instanceof HangableBlock hangableBlock){
                    BlockPos offset;
                    if(state.getValue(FACING).getAxis() == Direction.Axis.Y){
                        offset = pos.relative(player.getDirection().getOpposite());
                    } else {
                        offset = pos.relative(Direction.DOWN);
                    }
                    if(!level.getBlockState(offset).isAir()){
                        return InteractionResult.FAIL;
                    }
                    Direction hangDirection = Direction.UP;
                    if(state.getValue(FACING).getAxis() == Direction.Axis.Y){
                        hangDirection = player.getDirection();
                    }
                    level.setBlock(offset, hangableBlock.defaultBlockState()
                            .setValue(HangableBlock.FACING, player.getDirection())
                            .setValue(HangableBlock.HANGING, true)
                            .setValue(HangableBlock.HANG_DIRECTION, hangDirection), Block.UPDATE_CLIENTS);
                    if (!player.isCreative()) {
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
