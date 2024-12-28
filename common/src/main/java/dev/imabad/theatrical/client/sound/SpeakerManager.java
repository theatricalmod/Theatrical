package dev.imabad.theatrical.client.sound;

import com.mojang.blaze3d.audio.Channel;
import net.minecraft.client.sounds.AudioStream;
import net.minecraft.client.sounds.SoundEngine;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SpeakerManager {

    private static final Map<UUID, Speaker> sounds = new ConcurrentHashMap<>();

    public static void onPlayStreaming(SoundEngine engine, Channel channel, AudioStream stream){
        if(!(stream instanceof OpusStreamedAudioStream opusStream)) return;

        opusStream.channel = channel;
        opusStream.executor = engine.executor;
    }

    public static Speaker getSound(UUID source){
        return sounds.computeIfAbsent(source, x -> new Speaker());
    }

    public static void stopSound(UUID source){
        Speaker remove = sounds.remove(source);
        if(remove != null) remove.stop();
    }

    public static void stopAll(){
        sounds.forEach((uuid, speaker) -> {
            speaker.stop();
        });
        sounds.clear();
    }

}
