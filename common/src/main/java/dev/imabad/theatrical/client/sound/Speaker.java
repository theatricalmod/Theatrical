package dev.imabad.theatrical.client.sound;

import dev.imabad.theatrical.Theatrical;
import dev.imabad.theatrical.TheatricalExpectPlatform;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

public class Speaker {

    public static final ResourceLocation STREAM = new ResourceLocation(Theatrical.MOD_ID, "speaker.opus_stream");

    private OpusStreamedAudioStream opusStream;
    private SpeakerSound sound;

    Speaker(){}

    public synchronized void pushAudio(byte[] buffer){
        if(opusStream == null){
            opusStream = new OpusStreamedAudioStream();
        }
        boolean isExhausted = opusStream.isEmpty();
        opusStream.push(buffer);
        if(isExhausted && sound != null && sound.stream == opusStream && opusStream.channel != null && opusStream.executor != null){
            OpusStreamedAudioStream actualStream = sound.stream;
            opusStream.executor.execute(() -> {
                if(actualStream.channel != null){
                    if(!actualStream.channel.stopped()) actualStream.channel.pumpBuffers(1);
                }
            });
        }
    }

    public boolean isReady(){
        return opusStream.ready();
    }

    public void playAudio(BlockPos pos){
        SoundManager soundManager = Minecraft.getInstance().getSoundManager();
        if(sound != null && sound.stream != opusStream){
            soundManager.stop(sound);
            sound = null;
        }

        if(sound != null && !soundManager.isActive(sound)) sound = null;

        if(sound == null && opusStream != null){
            sound = TheatricalExpectPlatform.newSpeakerSound(STREAM, opusStream, pos);
            soundManager.play(sound);
        }
    }

    public void stop(){
        if(sound != null) Minecraft.getInstance().getSoundManager().stop(sound);

        opusStream = null;
        sound = null;
    }
}
