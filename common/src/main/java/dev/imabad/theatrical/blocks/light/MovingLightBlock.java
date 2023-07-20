package dev.imabad.theatrical.blocks.light;

import dev.imabad.theatrical.blockentities.BlockEntities;
import dev.imabad.theatrical.blockentities.interfaces.ArtNetInterfaceBlockEntity;
import dev.imabad.theatrical.blockentities.light.MovingLightBlockEntity;
import dev.imabad.theatrical.blocks.Blocks;
import dev.imabad.theatrical.client.gui.screen.ArtNetInterfaceScreen;
import dev.imabad.theatrical.client.gui.screen.MovingLightScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class MovingLightBlock extends BaseLightBlock{

    public static final BooleanProperty HANGING = BooleanProperty.create("hanging");

    public MovingLightBlock() {
        super(Properties.of(Material.HEAVY_METAL)
                .requiresCorrectToolForDrops()
                .strength(3, 3)
                .noOcclusion()
                .isValidSpawn(Blocks::neverAllowSpawn));
    }
    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new MovingLightBlockEntity(blockPos, blockState);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(HANGING);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        return super.getStateForPlacement(blockPlaceContext).setValue(HANGING,
                blockPlaceContext.getClickedFace() == Direction.DOWN ||
                        isHanging(blockPlaceContext.getLevel(), blockPlaceContext.getClickedPos()));
    }

    @Override
    public boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
        if(blockState.getValue(HANGING)){
            return isHanging(levelReader, blockPos);
        }
        return !levelReader.getBlockState(blockPos.below()).isAir();
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return level.isClientSide ? null : blockEntityType == BlockEntities.MOVING_LIGHT.get() ? MovingLightBlockEntity::tick : null;
    }

    @Override
    public VoxelShape getVisualShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if(context instanceof EntityCollisionContext entityCollisionContext && entityCollisionContext.getEntity() == null){
            return Shapes.empty();
        }
        return super.getVisualShape(state, level, pos, context);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if(level.isClientSide){
            MovingLightBlockEntity be = (MovingLightBlockEntity)level.getBlockEntity(pos);
            Minecraft.getInstance().setScreen(new MovingLightScreen(be));
        }
        return super.use(state, level, pos, player, hand, hit);
    }

}
