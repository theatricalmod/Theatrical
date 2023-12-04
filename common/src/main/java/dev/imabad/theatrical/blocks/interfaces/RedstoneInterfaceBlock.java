package dev.imabad.theatrical.blocks.interfaces;

import dev.imabad.theatrical.blockentities.interfaces.ArtNetInterfaceBlockEntity;
import dev.imabad.theatrical.blockentities.interfaces.RedstoneInterfaceBlockEntity;
import dev.imabad.theatrical.blocks.Blocks;
import dev.imabad.theatrical.client.gui.screen.ArtNetInterfaceScreen;
import dev.imabad.theatrical.client.gui.screen.RedstoneInterfaceScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class RedstoneInterfaceBlock  extends Block implements EntityBlock {
    public RedstoneInterfaceBlock() {
        super(Properties.of()
                .requiresCorrectToolForDrops()
                .strength(3, 3)
                .noOcclusion()
                .isValidSpawn(Blocks::neverAllowSpawn)
                .mapColor(MapColor.METAL)
                .sound(SoundType.METAL));
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RedstoneInterfaceBlockEntity(pos, state);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if(level.isClientSide){
            RedstoneInterfaceBlockEntity be = (RedstoneInterfaceBlockEntity)level.getBlockEntity(pos);
            Minecraft.getInstance().setScreen(new RedstoneInterfaceScreen(be));
        }
        return InteractionResult.SUCCESS;
    }


    @Override
    public int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        if(level.getBlockEntity(pos) instanceof RedstoneInterfaceBlockEntity be){
            return be.getRedstoneOutput();
        }
        return super.getSignal(state, level, pos, direction);
    }

    @Override
    public boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    public int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        if(level.getBlockEntity(pos) instanceof RedstoneInterfaceBlockEntity be){
            return be.getRedstoneOutput();
        }
        return super.getDirectSignal(state, level, pos, direction);
    }
}

