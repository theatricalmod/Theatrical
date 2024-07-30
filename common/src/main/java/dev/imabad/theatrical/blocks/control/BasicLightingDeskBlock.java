package dev.imabad.theatrical.blocks.control;

import dev.imabad.theatrical.TheatricalClient;
import dev.imabad.theatrical.blockentities.BlockEntities;
import dev.imabad.theatrical.blockentities.control.BasicLightingDeskBlockEntity;
import dev.imabad.theatrical.blockentities.light.FresnelBlockEntity;
import dev.imabad.theatrical.blockentities.light.LightCollisionContext;
import dev.imabad.theatrical.blocks.Blocks;
import dev.imabad.theatrical.client.gui.screen.BasicLightingDeskScreen;
import dev.imabad.theatrical.client.gui.screen.FresnelScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
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

    public BasicLightingDeskBlock() {
        super(Properties.of()
                .requiresCorrectToolForDrops()
                .strength(3, 3)
                .noOcclusion()
                .isValidSpawn(Blocks::neverAllowSpawn)
                .mapColor(MapColor.METAL)
                .sound(SoundType.METAL));
        this.registerDefaultState(this.getStateDefinition().any().setValue(FACING, Direction.NORTH));
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
    @Environment(EnvType.CLIENT)
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if(level.isClientSide){
            BasicLightingDeskBlockEntity be = (BasicLightingDeskBlockEntity) level.getBlockEntity(pos);
            Minecraft.getInstance().setScreen(new BasicLightingDeskScreen(be));
        }
        return InteractionResult.SUCCESS;
    }
}
