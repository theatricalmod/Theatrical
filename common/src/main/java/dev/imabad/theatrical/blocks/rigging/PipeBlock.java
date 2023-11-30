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
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
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

public class PipeBlock extends HorizontalDirectionalBlock implements Support {

    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    private final VoxelShape Z_BOX = Shapes.create(new AABB(0.35, 0, 0, 0.65, 0.2, 1));
    private final VoxelShape X_BOX = Shapes.create(new AABB(0, 0, 0.4, 1, 0.2, 0.6));

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
        return new float[]{0, 0.19F, 0};
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(HorizontalDirectionalBlock.FACING).getAxis() == Direction.Axis.Z ? Z_BOX : X_BOX;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
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
