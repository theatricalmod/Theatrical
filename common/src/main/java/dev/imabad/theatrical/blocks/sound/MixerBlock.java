package dev.imabad.theatrical.blocks.sound;

import com.mojang.serialization.Codec;
import dev.imabad.theatrical.TheatricalScreen;
import dev.imabad.theatrical.api.network.audio.AudioDeviceDefinition;
import dev.imabad.theatrical.blockentities.control.BasicLightingDeskBlockEntity;
import dev.imabad.theatrical.blockentities.sound.MixerBlockEntity;
import dev.imabad.theatrical.blocks.Blocks;
import dev.imabad.theatrical.items.Items;
import dev.imabad.theatrical.net.OpenScreen;
import dev.imabad.theatrical.networks.AVNetwork;
import dev.imabad.theatrical.networks.AVNetworkData;
import dev.imabad.theatrical.util.DimensionBlockPos;
import dev.imabad.theatrical.util.UUIDUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MixerBlock extends Block implements EntityBlock {
    public MixerBlock() {
        super(Properties.of()
                .requiresCorrectToolForDrops()
                .strength(3, 3)
                .noOcclusion()
                .isValidSpawn(Blocks::neverAllowSpawn)
                .mapColor(MapColor.METAL)
                .sound(SoundType.METAL));
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MixerBlockEntity(pos, state);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if(!level.isClientSide){
            MixerBlockEntity be = (MixerBlockEntity) level.getBlockEntity(pos);
            if(be != null) {
                if (player.getItemInHand(hand).getItem() == Items.CONFIGURATION_CARD.get()) {
                    ItemStack itemInHand = player.getItemInHand(hand);
                    CompoundTag tagData = itemInHand.getOrCreateTag();
                    be.setNetworkId(tagData.getUUID("network"));
                    AVNetworkData instance = AVNetworkData.getInstance(level.getServer().overworld());
                    player.sendSystemMessage(Component.translatable("item.configurationcard.success", instance.getNetwork(be.getNetworkId()).name()));
                    return InteractionResult.SUCCESS;
                } else {
                    be.refreshEngineConfig();
                    Tag extraData = new CompoundTag();
                    if (be.getNetworkId() != UUIDUtil.NULL) {
                        AVNetwork network = AVNetworkData.getInstance(level.getServer().overworld()).getNetwork(be.getNetworkId());
                        if (network != null && !network.isMember(player.getUUID())) {
                            return InteractionResult.FAIL;
                        }
                        if (network != null) {
                            Map<DimensionBlockPos, AudioDeviceDefinition> devices = new HashMap<>();
                            for (DimensionBlockPos device : network.getAudioHandler().getDevices()) {
                                devices.put(device, network.getAudioHandler().getDevice(device).getDefinition());
                            }
                            extraData = AudioDeviceDefinition.POS_MAP_CODEC.encodeStart(NbtOps.INSTANCE,
                                    devices).getOrThrow(true, System.out::println);
                        }
                    }
                    new OpenScreen(pos, TheatricalScreen.MIXER, extraData).sendTo((ServerPlayer) player);
                }
            }
        }
        return InteractionResult.SUCCESS;
    }
}
