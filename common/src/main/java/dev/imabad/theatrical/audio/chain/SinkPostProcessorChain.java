package dev.imabad.theatrical.audio.chain;

import dev.imabad.theatrical.audio.AudioBuffer;
import dev.imabad.theatrical.audio.AudioProcessor;
import dev.imabad.theatrical.audio.AudioSink;
import dev.imabad.theatrical.audio.AudioSource;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class SinkPostProcessorChain implements AudioSink {
    private AudioSink actualSink;
    private final List<AudioProcessor> processors = new ArrayList<>();

    public SinkPostProcessorChain(AudioSink actualSink) {
        this.actualSink = actualSink;
    }

    public void setActualSink(AudioSink actualSink) {
        this.actualSink = actualSink;
    }

    public void addProcessor(AudioProcessor processor) {
        processors.add(processor);
    }

    @Override
    public void write(AudioBuffer buffer) {
        if(actualSink != null) {
            for (AudioProcessor processor : processors) {
                buffer = processor.process(buffer);
                if (buffer == null) {
                    break;
                }
            }
            actualSink.write(buffer);
        }
    }
}
