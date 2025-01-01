package dev.imabad.theatrical.audio.processors;

import dev.imabad.theatrical.audio.AudioBuffer;
import dev.imabad.theatrical.audio.AudioProcessor;

public class VolumeControl implements AudioProcessor {
    /**
     * The gain multiplier for the audio signal.
     * 1.0 = no change in volume
     * 0.5 = half the volume (~ -6 dB)
     * 2.0 = double the volume (~ +6 dB)
     */
    private float gain;

    public VolumeControl(float initialGain) {
        this.gain = initialGain;
    }

    public float getGain() {
        return gain;
    }

    public void setGain(float newGain) {
        this.gain = newGain;
    }

    @Override
    public AudioBuffer process(AudioBuffer input) {
        if (input == null) {
            return null;
        }

        byte[] samples = input.getSamples();
        for (int i = 0; i < samples.length; i += 2) {
            // Combine two bytes into a single 16-bit sample
            short sample = (short) ((samples[i] & 0xff) | (samples[i + 1] << 8));
            float scaled = sample * gain;

            // clamp scaled if needed
            if (scaled > Short.MAX_VALUE) scaled = Short.MAX_VALUE;
            if (scaled < Short.MIN_VALUE) scaled = Short.MIN_VALUE;

            sample = (short) scaled;

            // write sample back out as two bytes
            samples[i]   = (byte) (sample & 0xff);
            samples[i+1] = (byte) ((sample >> 8) & 0xff);
        }

        return input;
    }
}
