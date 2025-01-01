package dev.imabad.theatrical.audio.remote;

import dev.imabad.theatrical.audio.AudioBuffer;
import dev.imabad.theatrical.audio.AudioSource;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class RemoteAudioSource implements AudioSource {
    private final BlockingQueue<AudioBuffer> incomingBuffers = new LinkedBlockingQueue<>();
    private volatile boolean finished = false;

    public void onAudioDataReceived(AudioBuffer buffer) {
        if (buffer == null) {
            finished = true;
        } else {
            incomingBuffers.offer(buffer);
        }
    }

    @Override
    public AudioBuffer read() {
        try {
            return incomingBuffers.poll(5, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e); // Hmm?
        }
    }

    @Override
    public boolean isFinished() {
        return finished;
    }
}
