package dev.imabad.theatrical.net.sound;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseS2CMessage;
import dev.architectury.networking.simple.MessageType;
import dev.imabad.theatrical.TheatricalClient;
import dev.imabad.theatrical.client.sound.mic.MicrophoneManager;
import dev.imabad.theatrical.net.TheatricalNet;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

public class StartMicrophone extends BaseS2CMessage {

    private BlockPos speakerPos;
    private boolean start;

    public StartMicrophone(BlockPos speakerPos, boolean start) {
        this.speakerPos = speakerPos;
        this.start = start;
    }

    public StartMicrophone(FriendlyByteBuf buf){
        speakerPos = buf.readBlockPos();
        start = buf.readBoolean();
    }

    @Override
    public MessageType getType() {
        return TheatricalNet.START_MIC;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeBlockPos(speakerPos);
        buf.writeBoolean(start);
    }

    @Override
    public void handle(NetworkManager.PacketContext context) {
        if(start) {
            TheatricalClient.getMicrophoneManager().startMicThread(speakerPos);
        } else {
            TheatricalClient.getMicrophoneManager().closeMicThread();
        }
    }
}
