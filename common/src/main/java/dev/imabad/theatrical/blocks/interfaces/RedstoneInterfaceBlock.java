package dev.imabad.theatrical.blocks.interfaces;

import dev.architectury.networking.NetworkManager;
import dev.imabad.theatrical.TheatricalScreen;
import dev.imabad.theatrical.blockentities.control.BasicLightingDeskBlockEntity;
import dev.imabad.theatrical.blockentities.interfaces.RedstoneInterfaceBlockEntity;
import dev.imabad.theatrical.blockentities.light.BaseDMXConsumerLightBlockEntity;
import dev.imabad.theatrical.blocks.Blocks;
import dev.imabad.theatrical.items.ConfigurationCardData;
import dev.imabad.theatrical.items.DataComponents;
import dev.imabad.theatrical.networks.TheatricalNetwork;
import dev.imabad.theatrical.networks.TheatricalNetworkData;
import dev.imabad.theatrical.items.Items;
import dev.imabad.theatrical.net.OpenScreen;
import dev.imabad.theatrical.util.UUIDUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class RedstoneInterfaceBlock  extends Block implements EntityBlock {
    public RedstoneInterfaceBlock(Properties properties) {
        super(properties.requiresCorrectToolForDrops()
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
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if(!level.isClientSide()) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof RedstoneInterfaceBlockEntity redstoneInterfaceBlockEntity) {
                if (!redstoneInterfaceBlockEntity.getNetworkId().equals(UUIDUtil.NULL)) {
                    TheatricalNetwork network = TheatricalNetworkData.getInstance(level.getServer().overworld()).getNetwork(redstoneInterfaceBlockEntity.getNetworkId());
                    if (network != null && !network.members().isMember(player.getUUID())) {
                        return InteractionResult.FAIL;
                    }
                }
                if (player.getItemInHand(hand).getItem() == Items.CONFIGURATION_CARD.get()) {
                    ItemStack itemInHand = player.getItemInHand(hand);
                    if(itemInHand.has(DataComponents.CONFIGURATION_CARD_DATA.get())){
                        ConfigurationCardData data = itemInHand.get(DataComponents.CONFIGURATION_CARD_DATA.get());
                        redstoneInterfaceBlockEntity.setNetworkId(data.network());
                        if (data.universeEnabled()) {
                            redstoneInterfaceBlockEntity.setUniverse(data.dmxUniverse());
                        }
                        if (data.addressEnabled()) {
                            redstoneInterfaceBlockEntity.setChannelStartPoint(data.dmxAddress());
                        }
                        if (data.autoIncrement()) {
                            data = data.increment(redstoneInterfaceBlockEntity.getChannelCount());
                            itemInHand.set(DataComponents.CONFIGURATION_CARD_DATA.get(), data);
                        }
                        TheatricalNetworkData instance = TheatricalNetworkData.getInstance(level.getServer().overworld());
                        player.displayClientMessage(Component.translatable("item.configurationcard.success",
                                instance.getNetwork(redstoneInterfaceBlockEntity.getNetworkId()).name(),
                                Integer.toString(redstoneInterfaceBlockEntity.getUniverse()),
                                Integer.toString(redstoneInterfaceBlockEntity.getChannelStart()),
                                Integer.toString(data.dmxAddress())), false);
                        return InteractionResult.SUCCESS;
                    }
                }
                return InteractionResult.PASS;
            }
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if(!level.isClientSide()){
            RedstoneInterfaceBlockEntity be = (RedstoneInterfaceBlockEntity) level.getBlockEntity(pos);
            if(be.getNetworkId() != UUIDUtil.NULL){
                TheatricalNetwork network = TheatricalNetworkData.getInstance(level.getServer().overworld()).getNetwork(be.getNetworkId());
                if(network != null && !network.members().isMember(player.getUUID())) {
                    return InteractionResult.FAIL;
                }
            }
            NetworkManager.sendToPlayer((ServerPlayer) player, new OpenScreen(pos, TheatricalScreen.GENERIC_DMX));
        }
        return super.useWithoutItem(state, level, pos, player, hitResult);
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

