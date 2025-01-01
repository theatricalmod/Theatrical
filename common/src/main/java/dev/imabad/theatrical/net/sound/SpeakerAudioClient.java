package dev.imabad.theatrical.net.sound;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseS2CMessage;
import dev.architectury.networking.simple.MessageType;
import dev.imabad.theatrical.client.sound.SpeakerManager;
import dev.imabad.theatrical.net.TheatricalNet;
import dev.imabad.theatrical.util.DimensionBlockPos;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.UUID;

public class SpeakerAudioClient extends BaseS2CMessage {

    private DimensionBlockPos sourceId;
    private byte[] data;

    public SpeakerAudioClient(FriendlyByteBuf buf){
        sourceId = DimensionBlockPos.decode(buf);
        data = new byte[buf.readableBytes()];
        buf.readBytes(data);
    }

    public SpeakerAudioClient(DimensionBlockPos sourceId, byte[] data){
        this.sourceId = sourceId;
        this.data = data;
    }

    @Override
    public MessageType getType() {
        return TheatricalNet.SPEAKER_AUDIO;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        sourceId.encode(buf);
        buf.writeBytes(data);
    }

    @Override
    public void handle(NetworkManager.PacketContext context) {
        SpeakerManager.getSound(sourceId).pushAudio(data);
        SpeakerManager.getSound(sourceId).playAudio(sourceId.pos());
    }
}
