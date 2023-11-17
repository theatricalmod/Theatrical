package dev.imabad.theatrical.blocks.interfaces;

import dev.imabad.theatrical.api.CableType;
import dev.imabad.theatrical.blockentities.interfaces.ArtNetInterfaceBlockEntity;
import dev.imabad.theatrical.blocks.Blocks;
import dev.imabad.theatrical.blocks.CableBlock;
import dev.imabad.theatrical.blocks.NetworkNodeBlock;
import dev.imabad.theatrical.client.gui.screen.ArtNetInterfaceScreen;
import dev.imabad.theatrical.graphs.CableNodePos;
import dev.imabad.theatrical.graphs.api.Node;
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
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class ArtNetInterfaceBlock extends NetworkNodeBlock implements EntityBlock, Node {
    public ArtNetInterfaceBlock() {
        super(Properties.of(Material.HEAVY_METAL)
                .requiresCorrectToolForDrops()
                .strength(3, 3)
                .noOcclusion()
                .isValidSpawn(Blocks::neverAllowSpawn));
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ArtNetInterfaceBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return level.isClientSide ? ArtNetInterfaceBlockEntity::tick : null;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if(level.isClientSide){
            ArtNetInterfaceBlockEntity be = (ArtNetInterfaceBlockEntity)level.getBlockEntity(pos);
            Minecraft.getInstance().setScreen(new ArtNetInterfaceScreen(be));
        }
        return super.use(state, level, pos, player, hand, hit);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if(!level.isClientSide){
            BlockEntity be = level.getBlockEntity(pos);
            if(be instanceof ArtNetInterfaceBlockEntity interfaceBlock && placer != null){
                interfaceBlock.setOwnerUUID(placer.getUUID());
            }
        }
        super.setPlacedBy(level, pos, state, placer, stack);
    }

    @Override
    public Collection<CableNodePos.DiscoveredPosition> getConnected(@Nullable CableNodePos from, BlockGetter level, BlockPos pos, BlockState state) {
        Set<CableNodePos.DiscoveredPosition> connected = new HashSet<>();
        // Grab center of the position provided
        Vec3 center = Vec3.atBottomCenterOf(pos);
        // Check if the tile is an actual cable
        for(Direction direction : Direction.values()){
            Vec3[] axes = CableBlock.DirectionAxes.fromDirection(direction).getAxes();
            Vec3 dirCenter = CableBlock.modifyCenter(center, direction);
            // Loop through the axis'
               for (Vec3 axe : axes) {
                // Add possible connection points from each axis to the list
                CableBlock.addToListIfConnected(from, connected,
                        (isPositive) -> level instanceof Level l ? l.dimension() : Level.OVERWORLD,
                        (isPositive, tPos) -> CableBlock.getCableType(level, new BlockPos(tPos)),
                        CableBlock.getOffsetPos(dirCenter, axe, false));
                CableBlock.addToListIfConnected(from, connected,
                        (isPositive) -> level instanceof Level l ? l.dimension() : Level.OVERWORLD,
                        (isPositive, tPos) -> CableBlock.getCableType(level, new BlockPos(tPos)),
                        CableBlock.getOffsetPos(dirCenter, axe, true));
            }
        }
        return connected;
    }

    @Override
    public Collection<CableNodePos.DiscoveredPosition> getPossibleNodesForSide(Direction side, BlockGetter level, BlockPos pos) {
        return getConnected(null, level, pos, level.getBlockState(pos));
    }

    @Override
    public CableType getAcceptedCableType(LevelAccessor level, BlockPos pos) {
        return CableType.BUNDLED;
    }
}
