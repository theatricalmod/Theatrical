package dev.imabad.theatrical.net.sound;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseC2SMessage;
import dev.architectury.networking.simple.MessageType;
import dev.architectury.utils.GameInstance;
import dev.imabad.theatrical.api.network.audio.AudioChannelType;
import dev.imabad.theatrical.blockentities.sound.MixerBlockEntity;
import dev.imabad.theatrical.net.TheatricalNet;
import dev.imabad.theatrical.util.DimensionBlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;

public class ConfigureMixerPacket extends BaseC2SMessage {

    private DimensionBlockPos mixerPos;
    private int channelToChange;
    private AudioChannelType audioChannelType;
    private DimensionBlockPos newDevicePos;
    private int newDeviceChannel;

    public ConfigureMixerPacket(DimensionBlockPos mixerPos, int channelToChange,
                                AudioChannelType audioChannelType, DimensionBlockPos newDevicePos, int newDeviceChannel) {
        this.mixerPos = mixerPos;
        this.channelToChange = channelToChange;
        this.audioChannelType = audioChannelType;
        this.newDevicePos = newDevicePos;
        this.newDeviceChannel = newDeviceChannel;
    }

    public ConfigureMixerPacket(FriendlyByteBuf buffer) {
        mixerPos = DimensionBlockPos.decode(buffer);
        channelToChange = buffer.readInt();
        audioChannelType = buffer.readEnum(AudioChannelType.class);
        newDevicePos = DimensionBlockPos.decode(buffer);
        newDeviceChannel = buffer.readInt();
    }

    @Override
    public MessageType getType() {
        return TheatricalNet.CONFIGURE_MIXER;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        mixerPos.encode(buf);
        buf.writeInt(channelToChange);
        buf.writeEnum(audioChannelType);
        newDevicePos.encode(buf);
        buf.writeInt(newDeviceChannel);
    }

    @Override
    public void handle(NetworkManager.PacketContext context) {
        context.queue(() -> {
            ServerLevel serverLevel = GameInstance.getServer().getLevel(mixerPos.dimension());
            if(serverLevel != null){
                BlockEntity blockEntity = serverLevel.getBlockEntity(mixerPos.pos());
                if(blockEntity instanceof MixerBlockEntity mixer) {
                    mixer.updateMixerConfiguration(audioChannelType, channelToChange, newDevicePos, newDeviceChannel);
                }
            }
        });
    }
}
