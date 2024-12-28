package dev.imabad.theatrical.net.sound;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseS2CMessage;
import dev.architectury.networking.simple.MessageType;
import dev.imabad.theatrical.client.sound.SpeakerManager;
import dev.imabad.theatrical.net.TheatricalNet;
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

    private UUID sourceId;
    private BlockPos pos;
    private byte[] data;

    public SpeakerAudioClient(FriendlyByteBuf buf){
        sourceId = buf.readUUID();
        pos = buf.readBlockPos();
        data = new byte[buf.readableBytes()];
        buf.readBytes(data);
    }

    public SpeakerAudioClient(UUID sourceId, BlockPos pos, byte[] data){
        this.sourceId = sourceId;
        this.pos = pos;
        this.data = data;
    }

    @Override
    public MessageType getType() {
        return TheatricalNet.SPEAKER_AUDIO;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeUUID(sourceId);
        buf.writeBlockPos(pos);
        buf.writeBytes(data);
    }

    @Override
    public void handle(NetworkManager.PacketContext context) {
        SpeakerManager.getSound(sourceId).pushAudio(data);
        SpeakerManager.getSound(sourceId).playAudio(pos);
    }
}
