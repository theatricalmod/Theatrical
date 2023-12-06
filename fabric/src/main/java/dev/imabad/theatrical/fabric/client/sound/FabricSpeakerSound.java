package dev.imabad.theatrical.fabric.client.sound;

import dev.imabad.theatrical.client.sound.OpusStreamedAudioStream;
import dev.imabad.theatrical.client.sound.SpeakerSound;
import net.minecraft.client.sounds.AudioStream;
import net.minecraft.client.sounds.SoundBufferLibrary;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

import java.util.concurrent.CompletableFuture;

public class FabricSpeakerSound extends SpeakerSound {
    public FabricSpeakerSound(ResourceLocation sound, OpusStreamedAudioStream stream, BlockPos pos) {
        super(sound, stream, pos);
    }

    @Override
    public CompletableFuture<AudioStream> getAudioStream(SoundBufferLibrary loader, ResourceLocation id, boolean repeatInstantly) {
        return stream != null ? CompletableFuture.completedFuture(stream) : loader.getStream(id, repeatInstantly);
    }
}
