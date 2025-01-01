package dev.imabad.theatrical.audio.processors;

import dev.imabad.theatrical.audio.AudioBuffer;
import dev.imabad.theatrical.audio.AudioProcessor;

public class VolumeProcessor implements AudioProcessor {
    private float lastRms = 0.0f;
    @Override
    public AudioBuffer process(AudioBuffer input) {
        if (input == null) {
            // No data, just return
            return null;
        }

        // Calculate RMS (Root Mean Square) across all samples
        byte[] samples = input.getSamples();
        if (samples.length == 0) {
            return input; // nothing to measure
        }

        double sumOfSquares = 0.0;
        for (float sample : samples) {
            sumOfSquares += sample * sample;
        }

        double meanSquare = sumOfSquares / samples.length;
        float rms = (float) Math.sqrt(meanSquare);

        // Store the latest measured RMS
        lastRms = rms;
        return input;
    }

    public float getAmplitude() {
        return lastRms;
    }

    public float getDB(){
        return amplitudeToDecibels(lastRms);
    }

    public static float amplitudeToDecibels(float amplitude) {
        if (amplitude <= 0.0f) {
            // Avoid log of zero or negative
            return Float.NEGATIVE_INFINITY;
        }
        return 20.0f * (float) Math.log10(amplitude);
    }
}
