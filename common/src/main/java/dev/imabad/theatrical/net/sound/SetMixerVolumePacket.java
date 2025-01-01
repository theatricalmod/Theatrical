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

public class SetMixerVolumePacket extends BaseC2SMessage {

    private DimensionBlockPos mixerPos;
    private AudioChannelType channelType;
    private int channelToChange;
    private float newVolume;

    public SetMixerVolumePacket(DimensionBlockPos mixerPos, AudioChannelType channelType, int channelToChange, float newVolume) {
        this.mixerPos = mixerPos;
        this.channelType = channelType;
        this.channelToChange = channelToChange;
        this.newVolume = newVolume;
    }

    public SetMixerVolumePacket(FriendlyByteBuf buffer) {
        mixerPos = DimensionBlockPos.decode(buffer);
        channelType = buffer.readEnum(AudioChannelType.class);
        channelToChange = buffer.readInt();
        newVolume = buffer.readFloat();
    }

    @Override
    public MessageType getType() {
        return TheatricalNet.SET_MIXER_VOLUME;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        mixerPos.encode(buf);
        buf.writeEnum(channelType);
        buf.writeInt(channelToChange);
        buf.writeFloat(newVolume);
    }

    @Override
    public void handle(NetworkManager.PacketContext context) {
        context.queue(() -> {
            ServerLevel serverLevel = GameInstance.getServer().getLevel(mixerPos.dimension());
            if(serverLevel != null){
                BlockEntity blockEntity = serverLevel.getBlockEntity(mixerPos.pos());
                if(blockEntity instanceof MixerBlockEntity mixer) {
                    mixer.updateChannelVolume(channelType, channelToChange, newVolume);
                }
            }
        });
    }
}
