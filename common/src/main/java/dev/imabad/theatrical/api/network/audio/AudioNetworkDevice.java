package dev.imabad.theatrical.api.network.audio;

import dev.imabad.theatrical.api.network.BelongsToNetwork;
import dev.imabad.theatrical.audio.AudioSink;
import dev.imabad.theatrical.audio.AudioSource;
import org.jetbrains.annotations.Nullable;

public interface AudioNetworkDevice extends BelongsToNetwork {
    AudioDeviceDefinition getDefinition();

    @Nullable
    AudioSink getSinkForChannel(int channel);

    @Nullable
    AudioSource getSourceForChannel(int channel);
}
