package dev.imabad.theatrical.api.network.audio;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public enum AudioChannelType implements StringRepresentable {

    INPUT,
    OUTPUT;

    public static final Codec<AudioChannelType> CODEC = StringRepresentable.fromEnum(AudioChannelType::values);

    @Override
    public @NotNull String getSerializedName() {
        return name();
    }
}
