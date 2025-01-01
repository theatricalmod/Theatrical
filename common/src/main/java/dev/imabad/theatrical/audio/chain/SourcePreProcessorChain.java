package dev.imabad.theatrical.audio.chain;

import dev.imabad.theatrical.audio.AudioBuffer;
import dev.imabad.theatrical.audio.AudioProcessor;
import dev.imabad.theatrical.audio.AudioSource;
import dev.imabad.theatrical.audio.processors.VolumeControl;
import dev.imabad.theatrical.audio.processors.VolumeProcessor;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class SourcePreProcessorChain {
    private AudioSource source;
    private final List<AudioProcessor> processors = new ArrayList<>();
    private final VolumeControl volumeControl = new VolumeControl(1.0f);
    private final VolumeProcessor volumeProcessor = new VolumeProcessor();

    public SourcePreProcessorChain(AudioSource source) {
        this.source = source;
        processors.add(volumeControl);
        processors.add(volumeProcessor);
    }

    public void addProcessor(AudioProcessor processor) {
        processors.add(processor);
    }

    public void setSource(AudioSource source) {
        this.source = source;
    }

    @Nullable
    public AudioBuffer processNextFrame(){
        if(source == null){
            return null;
        }
        AudioBuffer buffer = source.read();
        if(buffer == null){
            return null;
        }
        for (AudioProcessor processor : processors) {
            buffer = processor.process(buffer);
            if(buffer == null){
                break;
            }
        }
        return buffer;
    }

    public boolean isFinished() {
        return source == null || source.isFinished();
    }

    public VolumeProcessor getVolumeProcessor() {
        return volumeProcessor;
    }

    public VolumeControl getVolumeControl() {
        return volumeControl;
    }
}
