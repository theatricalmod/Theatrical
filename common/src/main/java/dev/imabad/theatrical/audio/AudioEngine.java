package dev.imabad.theatrical.audio;

import dev.imabad.theatrical.audio.chain.SinkPostProcessorChain;
import dev.imabad.theatrical.audio.chain.SourcePreProcessorChain;
import dev.imabad.theatrical.client.sound.mic.MicrophoneManager;
import net.labymod.opus.OpusCodec;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AudioEngine {

    public static int CHANNELS = 1;
    public static int SAMPLE_RATE = 48000;
    public static int RATE_MS = 40;
    public static int FRAME_SIZE = (SAMPLE_RATE / 1000) * RATE_MS;

    public static final OpusCodec SINGLE_CHANNEL_ENCODER = OpusCodec.newBuilder()
            .withFrameSize(FRAME_SIZE)
            .withChannels(1)
            .withSampleRate(SAMPLE_RATE)
            .build();

    private final int maxInputs;
    private final int maxOutputs;
    private final UUID engineId;

    public AudioEngine(int maxInputs, int maxOutputs, UUID engineId) {
        this.maxInputs = maxInputs;
        this.maxOutputs = maxOutputs;
        this.engineId = engineId;
        sources = new ArrayList<>();
        for (int i = 0; i < maxInputs; i++) {
            sources.add(new SourcePreProcessorChain(null));
        }
        sinks = new ArrayList<>();
        for (int i = 0; i < maxOutputs; i++) {
            sinks.add(new SinkPostProcessorChain(null));
        }
    }

    private List<SourcePreProcessorChain> sources;
    private List<AudioProcessor> processors = new ArrayList<>();
    private List<SinkPostProcessorChain> sinks;
    private AudioMixer mixer;
    private volatile boolean running = false;

    public void setChannelSource(int channel, AudioSource chainSource) {
        if(channel >= 0 && channel <= maxInputs) {
            sources.get(channel).setSource(chainSource);
        }
    }

    public SourcePreProcessorChain getSourceChain(int channel) {
        return sources.get(channel);
    }

    public void addProcessor(AudioProcessor processor) { processors.add(processor); }
    public void setChannelSink(int channel, AudioSink sink) {
        if(channel >= 0 && channel <= maxOutputs) {
            sinks.get(channel).setActualSink(sink);
        }
    }

    public UUID getEngineId() {
        return engineId;
    }

    public void start() {
        mixer = new AudioMixer(sources);
        running = true;
        Thread thread = new Thread(this::runEngine);
        thread.setName("Theatrical Audio Engine - " + engineId.toString());
        thread.setDaemon(true);
        thread.start();
    }

    private void runEngine() {
        while (running) {
            // 1. Create or fetch a "mixed" buffer from all sources (e.g. a Mixer).
            AudioBuffer mixedOutput = null;
            if(mixer != null) {
                mixedOutput = mixer.process(null);
            }

            // 2. Pass the buffer through additional processors
            for (int i = 1; i < processors.size(); i++) {
                if (mixedOutput == null) break;
                mixedOutput = processors.get(i).process(mixedOutput);
            }

            // 3. Write to sinks (which send back to relevant clients)
            for (AudioSink sink : sinks) {
                sink.write(mixedOutput);
            }

            // 4. Some small sleep to prevent spinning
            try {
                Thread.sleep(RATE_MS - 2);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public void stop() {
        running = false;
    }
}
