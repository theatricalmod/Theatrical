package dev.imabad.theatrical.net.sound;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseC2SMessage;
import dev.architectury.networking.simple.MessageType;
import dev.imabad.theatrical.client.sound.SpeakerManager;
import dev.imabad.theatrical.net.TheatricalNet;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.UUID;

public class MicrophoneAudioPacket extends BaseC2SMessage {

    private UUID playerId;
    private BlockPos speakerPos;
    private byte[] data;

    public MicrophoneAudioPacket(UUID playerId, BlockPos speakerPos, byte[] data){
        this.playerId= playerId;
        this.speakerPos = speakerPos;
        this.data = data;
    }

    public MicrophoneAudioPacket(FriendlyByteBuf buf){
        playerId = buf.readUUID();
        speakerPos = buf.readBlockPos();
        data = new byte[buf.readableBytes()];
        buf.readBytes(data);
    }

    @Override
    public MessageType getType() {
        return TheatricalNet.PLAYER_AUDIO;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeUUID(playerId);
        buf.writeBlockPos(speakerPos);
        buf.writeBytes(data);
    }

    @Override
    public void handle(NetworkManager.PacketContext context) {
        context.queue(() -> {
            new SpeakerAudioClient(playerId, speakerPos, data).sendToChunkListeners(
                    context.getPlayer().level().getChunkAt(speakerPos));
        });
    }
}
