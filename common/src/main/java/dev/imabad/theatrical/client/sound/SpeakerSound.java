package dev.imabad.theatrical.client.sound;

import net.minecraft.client.resources.sounds.AbstractSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.AudioStream;
import net.minecraft.client.sounds.SoundBufferLibrary;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class SpeakerSound extends AbstractSoundInstance {

    protected @Nullable OpusStreamedAudioStream stream;

    protected SpeakerSound(ResourceLocation sound, OpusStreamedAudioStream stream, BlockPos pos) {
        super(sound, SoundSource.RECORDS, SoundInstance.createUnseededRandom());
        this.stream = stream;
        x = pos.getX();
        y = pos.getY();
        z = pos.getZ();
    }

    public CompletableFuture<AudioStream> getAudioStream(SoundBufferLibrary loader, ResourceLocation id, boolean repeatInstantly) {
        return stream != null ? CompletableFuture.completedFuture(stream) : loader.getStream(id, repeatInstantly);
    }


//    public CompletableFuture<AudioStream> getAudioStream(SoundBufferLibrary soundBuffers, ResourceLocation sound, boolean looping){
//        return stream != null ? CompletableFuture.completedFuture(stream) : soundBuffers.getStream(sound, looping);
//    }
//
    public AudioStream getStream(){
        return stream;
    }

}
