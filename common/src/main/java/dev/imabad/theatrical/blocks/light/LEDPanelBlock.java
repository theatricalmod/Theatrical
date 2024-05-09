package dev.imabad.theatrical.blocks.light;

import dev.imabad.theatrical.TheatricalClient;
import dev.imabad.theatrical.blockentities.BlockEntities;
import dev.imabad.theatrical.blockentities.light.FresnelBlockEntity;
import dev.imabad.theatrical.blockentities.light.LEDPanelBlockEntity;
import dev.imabad.theatrical.blocks.Blocks;
import dev.imabad.theatrical.client.gui.screen.FresnelScreen;
import dev.imabad.theatrical.client.gui.screen.GenericDMXConfigurationScreen;
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
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class LEDPanelBlock extends BaseLightBlock {

    public LEDPanelBlock() {
        super(Properties.of()
                .requiresCorrectToolForDrops()
                .strength(3, 3)
                .noOcclusion()
                .isValidSpawn(Blocks::neverAllowSpawn)
                .mapColor(MapColor.METAL)
                .sound(SoundType.METAL)
                .pushReaction(PushReaction.DESTROY));
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new LEDPanelBlockEntity(pos, state);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
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

    @Override
    public Direction getLightFacing(Direction hangDirection, Player placingPlayer) {
        return placingPlayer.getDirection().getOpposite();
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return blockEntityType == BlockEntities.LED_PANEL.get() ? LEDPanelBlockEntity::tick : null;
    }

    @Override
    public VoxelShape getVisualShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if(context instanceof EntityCollisionContext entityCollisionContext && entityCollisionContext.getEntity() == null){
            return Shapes.empty();
        }
        return super.getVisualShape(state, level, pos, context);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if(state.getValue(HANGING)){
            return Shapes.empty();
        }
        return super.getCollisionShape(state, level, pos, context);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if(level.isClientSide){
            if(player.isCrouching()){
                if(TheatricalClient.DEBUG_BLOCKS.contains(pos)){
                    TheatricalClient.DEBUG_BLOCKS.remove(pos);
                } else {
                    TheatricalClient.DEBUG_BLOCKS.add(pos);
                }
                return InteractionResult.SUCCESS;
            }
            LEDPanelBlockEntity be = (LEDPanelBlockEntity)level.getBlockEntity(pos);
            Minecraft.getInstance().setScreen(new GenericDMXConfigurationScreen<>(be, pos, "block.theatrical.led_panel"));
        }
        return InteractionResult.SUCCESS;
    }
}
