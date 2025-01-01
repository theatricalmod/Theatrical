package dev.imabad.theatrical.api.network.audio;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record AudioChannelDefinition(String name, int index, AudioChannelType channelType) {
    public static final Codec<AudioChannelDefinition> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.STRING.fieldOf("name").forGetter(AudioChannelDefinition::name),
            Codec.INT.fieldOf("index").forGetter(AudioChannelDefinition::index),
            AudioChannelType.CODEC.fieldOf("channelType").forGetter(AudioChannelDefinition::channelType)
    ).apply(i, AudioChannelDefinition::new));
}
