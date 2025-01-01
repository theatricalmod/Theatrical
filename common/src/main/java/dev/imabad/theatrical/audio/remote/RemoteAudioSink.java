package dev.imabad.theatrical.audio.remote;

import dev.architectury.utils.GameInstance;
import dev.imabad.theatrical.audio.AudioBuffer;
import dev.imabad.theatrical.audio.AudioEngine;
import dev.imabad.theatrical.audio.AudioSink;
import dev.imabad.theatrical.net.sound.SpeakerAudioClient;
import dev.imabad.theatrical.util.DimensionBlockPos;
import net.minecraft.world.level.chunk.LevelChunk;

public class RemoteAudioSink implements AudioSink {

    private final DimensionBlockPos speakerPos;

    public RemoteAudioSink(DimensionBlockPos speakerPos) {
        this.speakerPos = speakerPos;
    }

    @Override
    public void write(AudioBuffer buffer) {
        if(buffer != null) {
            byte[] encodedFrame = AudioEngine.SINGLE_CHANNEL_ENCODER.encodeFrame(buffer.getSamples());
            LevelChunk chunk = GameInstance.getServer().getLevel(speakerPos.dimension()).getChunkAt(speakerPos.pos());
            new SpeakerAudioClient(speakerPos, encodedFrame)
                    .sendToChunkListeners(chunk);
        }
    }

}
