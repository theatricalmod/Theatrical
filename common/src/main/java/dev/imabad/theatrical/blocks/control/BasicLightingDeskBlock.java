package dev.imabad.theatrical.blocks.control;

import dev.imabad.theatrical.TheatricalScreen;
import dev.imabad.theatrical.blockentities.BlockEntities;
import dev.imabad.theatrical.blockentities.control.BasicLightingDeskBlockEntity;
import dev.imabad.theatrical.blocks.Blocks;
import dev.imabad.theatrical.networks.TheatricalNetwork;
import dev.imabad.theatrical.networks.TheatricalNetworkData;
import dev.imabad.theatrical.net.OpenScreen;
import dev.imabad.theatrical.util.UUIDUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class BasicLightingDeskBlock extends Block implements EntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    private static final VoxelShape SHAPE = Shapes.create(0, 0, 0, 16 / 16D, 3 / 16D, 16 / 16D);

    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    public BasicLightingDeskBlock() {
        super(Properties.of()
                .requiresCorrectToolForDrops()
                .strength(3, 3)
                .noOcclusion()
                .isValidSpawn(Blocks::neverAllowSpawn)
                .mapColor(MapColor.METAL)
                .sound(SoundType.METAL));

        this.registerDefaultState(this.getStateDefinition().any().setValue(FACING, Direction.NORTH).setValue(LIT, false));
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite()).setValue(LIT, context.getLevel().hasNeighborSignal(context.getClickedPos()));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
        builder.add(LIT);
    }


    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BasicLightingDeskBlockEntity(pos, state);
    }

//    @Override
//    public RenderShape getRenderShape(BlockState blockState) {
//        return RenderShape.ENTITYBLOCK_ANIMATED;
//    }

    @Override
    public float getShadeBrightness(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
        return 1.0F;
    }

    @Override
    public boolean propagatesSkylightDown(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
        return true;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return level.isClientSide ? null : blockEntityType == BlockEntities.BASIC_LIGHTING_DESK.get() ? BasicLightingDeskBlockEntity::tick : null;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if(!level.isClientSide){
            BasicLightingDeskBlockEntity be = (BasicLightingDeskBlockEntity) level.getBlockEntity(pos);
            if(be.getNetworkId() != UUIDUtil.NULL){
                TheatricalNetwork network = TheatricalNetworkData.getInstance(level.getServer().overworld()).getNetwork(be.getNetworkId());
                if(network != null && !network.members().isMember(player.getUUID())) {
                    return InteractionResult.FAIL;
                }
            }
            new OpenScreen(pos, TheatricalScreen.BASIC_LIGHTING_DESK).sendTo((ServerPlayer) player);
        }
        return InteractionResult.SUCCESS;
    }
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {
        if (!level.isClientSide) {
            boolean powered=level.hasNeighborSignal(pos);
            BasicLightingDeskBlockEntity be = (BasicLightingDeskBlockEntity) level.getBlockEntity(pos);
            if(powered && !state.getValue(LIT)){
                if(be.isRunMode()){
                    be.clickButton();
                }
                level.setBlock(pos, state.setValue(LIT, true), 3);
            }else if(!powered && state.getValue(LIT)){
                level.setBlock(pos, state.setValue(LIT, false), 3);
            }
        }
    }

}
