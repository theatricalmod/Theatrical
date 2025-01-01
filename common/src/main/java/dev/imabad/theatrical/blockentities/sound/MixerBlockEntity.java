package dev.imabad.theatrical.blockentities.sound;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.imabad.theatrical.api.network.audio.AudioChannelDefinition;
import dev.imabad.theatrical.api.network.audio.AudioChannelType;
import dev.imabad.theatrical.api.network.audio.AudioDeviceDefinition;
import dev.imabad.theatrical.api.network.audio.AudioNetworkDevice;
import dev.imabad.theatrical.audio.AudioEngine;
import dev.imabad.theatrical.audio.AudioSink;
import dev.imabad.theatrical.audio.AudioSource;
import dev.imabad.theatrical.blockentities.BlockEntities;
import dev.imabad.theatrical.networks.AVNetwork;
import dev.imabad.theatrical.networks.AVNetworkData;
import dev.imabad.theatrical.networks.handlers.AVNetworkAudioHandler;
import dev.imabad.theatrical.util.DimensionBlockPos;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;

public class MixerBlockEntity extends BaseAudioNetworkDeviceBlockEntity {

    private final AudioEngine audioEngine;
    private final AudioDeviceDefinition definition;
    private MixerConfiguration mixerConfiguration;

    public MixerBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(BlockEntities.MIXER.get(), blockPos, blockState);
        audioEngine = new AudioEngine(16, 16, UUID.randomUUID());
        List<AudioChannelDefinition> audioChannelDefinitions = new ArrayList<>();
        for (int i = 0; i < 16; i++) {
            audioChannelDefinitions.add(new AudioChannelDefinition("Input " + (i + 1), i, AudioChannelType.INPUT));
            audioChannelDefinitions.add(new AudioChannelDefinition("Output " + (i + 1), i, AudioChannelType.OUTPUT));
        }
        definition = new AudioDeviceDefinition("Mixer", audioChannelDefinitions);
        mixerConfiguration  = new MixerConfiguration(new Int2ObjectOpenHashMap<>(

        ), new Int2ObjectOpenHashMap<>());
    }

    @Override
    public void write(CompoundTag compoundTag) {
        super.write(compoundTag);
        compoundTag.put("config", mixerConfiguration.write());
    }

    @Override
    public void read(CompoundTag compoundTag) {
        super.read(compoundTag);
        mixerConfiguration = MixerConfiguration.read(compoundTag.getCompound("config"));
        if(level != null && !level.isClientSide){
            configureAudioEngineFromMixerConfiguration();
        }
    }

    private void configureAudioEngineFromMixerConfiguration(){
        AVNetwork avNetwork = AVNetworkData.getInstance(level).getNetwork(getNetworkId());
        if(avNetwork == null){
            return;
        }
        AVNetworkAudioHandler audioHandler = avNetwork.getAudioHandler();
        for (Int2ObjectMap.Entry<AudioDeviceChannel> inputEntry : mixerConfiguration.inputs().int2ObjectEntrySet()) {
            AudioDeviceChannel audioDeviceChannel = inputEntry.getValue();
            AudioNetworkDevice networkDevice = audioHandler.getDevice(audioDeviceChannel.devicePos());
            if(networkDevice != null) {
                AudioSource newSource =
                        networkDevice.getSourceForChannel(audioDeviceChannel.channel());
                if (newSource != null) {
                    audioEngine.setChannelSource(inputEntry.getIntKey(), newSource);
                }
            }
        }
        for (Int2ObjectMap.Entry<AudioDeviceChannel> outputEntry : mixerConfiguration.outputs().int2ObjectEntrySet()) {
            AudioDeviceChannel audioDeviceChannel = outputEntry.getValue();
            AudioNetworkDevice networkDevice = audioHandler.getDevice(audioDeviceChannel.devicePos());
            if(networkDevice != null) {
                AudioSink newSink =
                        networkDevice.getSinkForChannel(audioDeviceChannel.channel());
                if (newSink != null) {
                    audioEngine.setChannelSink(outputEntry.getIntKey(), newSink);
                }
            }
        }
    }

    public void refreshEngineConfig(){
        configureAudioEngineFromMixerConfiguration();
    }

    public void updateMixerConfiguration(AudioChannelType type, int localChannel, DimensionBlockPos devicePos, int deviceChannel){
        Int2ObjectMap<MixerBlockEntity.AudioDeviceChannel> typeChannels;
        if (type == AudioChannelType.INPUT) {
            typeChannels = getMixerConfiguration().inputs();
        } else {
            typeChannels = getMixerConfiguration().outputs();
        }
        if (typeChannels.containsKey(localChannel)) {
            MixerBlockEntity.AudioDeviceChannel audioDeviceChannel = typeChannels.get(localChannel);
            audioDeviceChannel
                    .setDevicePos(devicePos);
            audioDeviceChannel.setChannel(deviceChannel);
        } else {
            typeChannels.put(localChannel, new MixerBlockEntity.
                    AudioDeviceChannel(devicePos, deviceChannel, 1));
        }
        setChanged();
        level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        configureAudioEngineFromMixerConfiguration();
    }

    @Override
    public void setLevel(Level level) {
        super.setLevel(level);
        if(level != null && !level.isClientSide) {
            audioEngine.start();
        }
    }

    @Override
    public void setRemoved() {
        if(level != null && !level.isClientSide) {
            audioEngine.stop();
        }
        super.setRemoved();
    }

    @Override
    public AudioDeviceDefinition getDefinition() {
        return definition;
    }

    @Override
    public AudioSink getSinkForChannel(int channel) {
        return null;
    }

    @Override
    public AudioSource getSourceForChannel(int channel) {
        return null;
    }

    public MixerConfiguration getMixerConfiguration() {
        return mixerConfiguration;
    }

    public void updateChannelVolume(AudioChannelType type, int channelToChange, float newVolume) {
        Int2ObjectMap<MixerBlockEntity.AudioDeviceChannel> typeChannels;
        if (type == AudioChannelType.INPUT) {
            typeChannels = getMixerConfiguration().inputs();
        } else {
            typeChannels = getMixerConfiguration().outputs();
        }
        if (typeChannels.containsKey(channelToChange)) {
            MixerBlockEntity.AudioDeviceChannel audioDeviceChannel = typeChannels.get(channelToChange);
            audioDeviceChannel.setGain(newVolume);
            audioEngine.getSourceChain(channelToChange).getVolumeControl().setGain(newVolume);
        } else {
            typeChannels.put(channelToChange, new MixerBlockEntity.
                    AudioDeviceChannel(null, -1, newVolume));
        }
        setChanged();
        level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
    }

    public static final class AudioDeviceChannel {
        public static final Codec<AudioDeviceChannel> CODEC = RecordCodecBuilder.create(i -> i.group(
                DimensionBlockPos.CODEC.fieldOf("devicePos").forGetter(AudioDeviceChannel::devicePos),
                Codec.INT.fieldOf("channel").forGetter(AudioDeviceChannel::channel),
                Codec.FLOAT.fieldOf("gain").forGetter(AudioDeviceChannel::getGain)
        ).apply(i, AudioDeviceChannel::new));
        private DimensionBlockPos devicePos;
        private int channel;
        private float gain = 1.0f;

        public AudioDeviceChannel(DimensionBlockPos devicePos, int channel, float gain) {
            this.devicePos = devicePos;
            this.channel = channel;
            this.gain = gain;
        }

        public static AudioDeviceChannel read(CompoundTag tag) {
            return CODEC.decode(NbtOps.INSTANCE, tag).result().get().getFirst();
        }

        public void write(CompoundTag tag) {
            CODEC.encode(this, NbtOps.INSTANCE, tag).result().get();
        }

        public DimensionBlockPos devicePos() {
            return devicePos;
        }

        public int channel() {
            return channel;
        }

        public float getGain() {
            return gain;
        }

        public void setDevicePos(DimensionBlockPos devicePos) {
            this.devicePos = devicePos;
        }

        public void setChannel(int channel) {
            this.channel = channel;
        }

        public void setGain(float gain) {
            this.gain = gain;
        }

        public String getNiceName(){
            return devicePos().dimension().toString() + " - " + devicePos.pos().toShortString()  + " - " + channel();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            var that = (AudioDeviceChannel) obj;
            return Objects.equals(this.devicePos, that.devicePos) &&
                    this.channel == that.channel;
        }

        @Override
        public int hashCode() {
            return Objects.hash(devicePos, channel);
        }

        @Override
        public String toString() {
            return "AudioDeviceChannel[" +
                    "devicePos=" + devicePos + ", " +
                    "channel=" + channel + ']';
        }

    }

    public record MixerConfiguration(Int2ObjectMap<AudioDeviceChannel> inputs,
                                     Int2ObjectMap<AudioDeviceChannel> outputs) {

        public static final Codec<MixerConfiguration> CODEC = RecordCodecBuilder.create(i -> i.group(
                    Codec.unboundedMap(Codec.STRING.xmap(Integer::parseInt, Object::toString), AudioDeviceChannel.CODEC)
                            .xmap(map -> (Int2ObjectMap<AudioDeviceChannel>) new Int2ObjectOpenHashMap<>(map), Function.identity())
                            .fieldOf("inputs").forGetter(MixerConfiguration::inputs),
                    Codec.unboundedMap(Codec.STRING.xmap(Integer::parseInt, Object::toString), AudioDeviceChannel.CODEC)
                            .xmap(map -> (Int2ObjectMap<AudioDeviceChannel>) new Int2ObjectOpenHashMap<>(map), Function.identity())
                            .fieldOf("outputs").forGetter(MixerConfiguration::outputs)
            ).apply(i, MixerConfiguration::new));

            public static MixerConfiguration read(CompoundTag tag) {
                return CODEC.decode(NbtOps.INSTANCE, tag).getOrThrow(false, System.out::println).getFirst();
            }

            public Tag write() {
                return CODEC.encodeStart(NbtOps.INSTANCE, this).getOrThrow(false, System.out::println);
            }
        }
}
