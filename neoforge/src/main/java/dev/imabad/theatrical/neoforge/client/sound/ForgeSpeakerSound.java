package dev.imabad.theatrical.neoforge.client.sound;

import dev.imabad.theatrical.client.sound.OpusStreamedAudioStream;
import dev.imabad.theatrical.client.sound.SpeakerSound;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.sounds.AudioStream;
import net.minecraft.client.sounds.SoundBufferLibrary;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

import java.util.concurrent.CompletableFuture;

public class ForgeSpeakerSound extends SpeakerSound {
    public ForgeSpeakerSound(ResourceLocation sound, OpusStreamedAudioStream stream, BlockPos pos) {
        super(sound, stream, pos);
    }

    @Override
    public CompletableFuture<AudioStream> getStream(SoundBufferLibrary soundBuffers, Sound sound, boolean looping) {
        return getAudioStream(soundBuffers, sound.getPath(), looping);
    }
}
