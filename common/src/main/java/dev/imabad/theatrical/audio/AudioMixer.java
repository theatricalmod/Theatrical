package dev.imabad.theatrical.audio;

import dev.imabad.theatrical.audio.chain.SourcePreProcessorChain;

import java.util.List;

public class AudioMixer implements AudioProcessor {

    private List<SourcePreProcessorChain> sourceChains;

    public AudioMixer(List<SourcePreProcessorChain> sourceChains) {
        this.sourceChains = sourceChains;
    }

    @Override
    public AudioBuffer process(AudioBuffer input) {
        byte[] mixed = null;
        int sampleRate = 48000;
        int channels = 2;

        for (SourcePreProcessorChain sourceChain : sourceChains) {
            if(!sourceChain.isFinished()){
                AudioBuffer buffer = sourceChain.processNextFrame();
                if(buffer != null){
                    if(mixed == null){
                        mixed = buffer.getSamples().clone();
                        sampleRate = buffer.getSampleRate();
                        channels = buffer.getChannels();
                    } else {
                        byte[] bufSamples = buffer.getSamples();
                        int len = Math.min(mixed.length, bufSamples.length);
                        for (int i = 0; i < len; i++) {
                            mixed[i] += bufSamples[i];
                        }
                    }
                }
            }
        }

        if(mixed == null){
            return null;
        }

        return new AudioBuffer(mixed, sampleRate, channels);
    }
}
