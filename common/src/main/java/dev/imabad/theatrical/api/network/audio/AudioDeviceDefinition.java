package dev.imabad.theatrical.api.network.audio;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.imabad.theatrical.util.DimensionBlockPos;

import java.util.List;
import java.util.Map;

public record AudioDeviceDefinition(String name, List<AudioChannelDefinition> channelDefinitions) {
    public static final Codec<AudioDeviceDefinition> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.STRING.fieldOf("name").forGetter(AudioDeviceDefinition::name),
            AudioChannelDefinition.CODEC.listOf().fieldOf("channelDefinitions").forGetter(AudioDeviceDefinition::channelDefinitions)
    ).apply(i, AudioDeviceDefinition::new));
    public static final Codec<Map<DimensionBlockPos, AudioDeviceDefinition>> POS_MAP_CODEC =
            Codec.unboundedMap(Codec.STRING.xmap(DimensionBlockPos::fromString, DimensionBlockPos::toString), AudioDeviceDefinition.CODEC);
}
