package dev.imabad.theatrical.mixin.client;

import com.mojang.blaze3d.audio.Channel;
import dev.imabad.theatrical.client.sound.SpeakerManager;
import dev.imabad.theatrical.client.sound.SpeakerSound;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.AudioStream;
import net.minecraft.client.sounds.SoundEngine;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SoundEngine.class)
public class SoundEngineMixin {
    @Nullable
    @Unique
    private static SoundEngine self;

    @Inject(method = "play", at = @At(value = "HEAD"))
    @SuppressWarnings("UnusedMethod")
    private void playSound(SoundInstance sound, CallbackInfo ci) {
        self = (SoundEngine) (Object) this;
    }

    @Inject(at = @At("TAIL"), method = {"lambda$play$8", "method_19755"})
    private void onStream(AudioStream audioStream, SoundInstance soundInstance, Channel channel, CallbackInfo ci){
        if(soundInstance instanceof SpeakerSound speakerSound) {
            SpeakerManager.onPlayStreaming(self, channel, speakerSound.getStream());
        }
    }
}
