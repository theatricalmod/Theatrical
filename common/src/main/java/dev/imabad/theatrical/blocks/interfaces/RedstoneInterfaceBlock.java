package dev.imabad.theatrical.blocks.interfaces;

import dev.imabad.theatrical.TheatricalScreen;
import dev.imabad.theatrical.blockentities.interfaces.RedstoneInterfaceBlockEntity;
import dev.imabad.theatrical.blockentities.light.BaseDMXConsumerLightBlockEntity;
import dev.imabad.theatrical.blocks.Blocks;
import dev.imabad.theatrical.client.gui.screen.GenericDMXConfigurationScreen;
import dev.imabad.theatrical.dmx.DMXNetwork;
import dev.imabad.theatrical.dmx.DMXNetworkData;
import dev.imabad.theatrical.items.Items;
import dev.imabad.theatrical.net.OpenScreen;
import dev.imabad.theatrical.util.UUIDUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
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
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if(!level.isClientSide()) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof RedstoneInterfaceBlockEntity redstoneInterfaceBlockEntity) {
                if (!redstoneInterfaceBlockEntity.getNetworkId().equals(UUIDUtil.NULL)) {
                    DMXNetwork network = DMXNetworkData.getInstance(level.getServer().overworld()).getNetwork(redstoneInterfaceBlockEntity.getNetworkId());
                    if (network != null && !network.isMember(player.getUUID())) {
                        return InteractionResult.FAIL;
                    }
                }
                if (player.getItemInHand(hand).getItem() == Items.CONFIGURATION_CARD.get()) {
                    ItemStack itemInHand = player.getItemInHand(hand);
                    CompoundTag tagData = itemInHand.getOrCreateTag();
                    redstoneInterfaceBlockEntity.setNetworkId(tagData.getUUID("network"));
                    if (tagData.getBoolean("universeEnabled")) {
                        redstoneInterfaceBlockEntity.setUniverse(tagData.getInt("dmxUniverse"));
                    }
                    if (tagData.getBoolean("addressEnabled")) {
                        redstoneInterfaceBlockEntity.setChannelStartPoint(tagData.getInt("dmxAddress"));
                    }
                    if (tagData.getBoolean("autoIncrement")) {
                        tagData.putInt("dmxAddress", tagData.getInt("dmxAddress") + redstoneInterfaceBlockEntity.getChannelCount());
                    }
                    itemInHand.save(tagData);
                    DMXNetworkData instance = DMXNetworkData.getInstance(level.getServer().overworld());
                    player.sendSystemMessage(Component.translatable("item.configurationcard.success", instance.getNetwork(redstoneInterfaceBlockEntity.getNetworkId()).name(), Integer.toString(redstoneInterfaceBlockEntity.getUniverse()), Integer.toString(redstoneInterfaceBlockEntity.getChannelStart()), Integer.toString(tagData.getInt("dmxAddress"))));
                    return InteractionResult.SUCCESS;
                }
                new OpenScreen(pos, TheatricalScreen.GENERIC_DMX).sendTo((ServerPlayer) player);
            }
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

