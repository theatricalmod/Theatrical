package dev.imabad.theatrical.audio;

import org.jetbrains.annotations.Nullable;

public interface AudioSource {

    @Nullable
    AudioBuffer read();

    boolean isFinished();

}
