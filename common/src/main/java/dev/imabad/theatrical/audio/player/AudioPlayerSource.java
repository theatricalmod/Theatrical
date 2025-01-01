package dev.imabad.theatrical.audio.player;

import com.sedmelluq.discord.lavaplayer.format.AudioDataFormat;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame;
import com.sedmelluq.discord.lavaplayer.track.playback.MutableAudioFrame;
import dev.imabad.theatrical.audio.AudioBuffer;
import dev.imabad.theatrical.audio.AudioEngine;
import dev.imabad.theatrical.audio.AudioSource;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class AudioPlayerSource implements AudioSource {

    private AudioPlayer audioPlayer;
    private final ByteBuffer buffer;
    private final MutableAudioFrame frame;

    public AudioPlayerSource(AudioPlayer audioPlayer) {
        this.audioPlayer = audioPlayer;
        this.buffer = ByteBuffer.allocate(AudioEngine.CHANNELS * AudioEngine.FRAME_SIZE * 2);
        this.frame = new MutableAudioFrame();
        this.frame.setBuffer(buffer);
    }

    @Override
    public @Nullable AudioBuffer read() {
        if(frame == null) return null;
        return new AudioBuffer(frame.getData(), AudioEngine.SAMPLE_RATE, 1);
    }

    @Override
    public boolean isFinished() {
        if(audioPlayer.isPaused()){
            return true;
        }
        try {
            return !audioPlayer.provide(frame, 5, TimeUnit.MILLISECONDS);
        } catch (TimeoutException | InterruptedException e) {
            e.printStackTrace();
        }
        return true;
    }
}
