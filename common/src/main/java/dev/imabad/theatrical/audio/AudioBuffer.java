package dev.imabad.theatrical.audio;

public class AudioBuffer {

    private byte[] samples;
    private int sampleRate;
    private int channels;

    public AudioBuffer(byte[] samples, int sampleRate, int channels) {
        this.samples = samples;
        this.sampleRate = sampleRate;
        this.channels = channels;
    }

    public byte[] getSamples() {
        return samples;
    }

    public void setSamples(byte[] samples) {
        this.samples = samples;
    }

    public int getSampleRate() {
        return sampleRate;
    }

    public void setSampleRate(int sampleRate) {
        this.sampleRate = sampleRate;
    }

    public int getChannels() {
        return channels;
    }

    public void setChannels(int channels) {
        this.channels = channels;
    }
}
