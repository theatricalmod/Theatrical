package dev.imabad.theatrical.blockentities.sound;

import dev.imabad.theatrical.api.network.audio.*;
import dev.imabad.theatrical.audio.AudioSink;
import dev.imabad.theatrical.audio.AudioSource;
import dev.imabad.theatrical.audio.remote.RemoteAudioSink;
import dev.imabad.theatrical.blockentities.BlockEntities;
import dev.imabad.theatrical.util.DimensionBlockPos;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.UUID;

public class SpeakerBlockEntity extends BaseAudioNetworkDeviceBlockEntity {

    @Override
    public AudioDeviceDefinition getDefinition() {
        return new AudioDeviceDefinition("Speaker",
                List.of(new AudioChannelDefinition("Main Output", 0, AudioChannelType.INPUT)));
    }

    @Override
    public AudioSink getSinkForChannel(int channel) {
        if(channel == 0){
            return new RemoteAudioSink(new DimensionBlockPos(level.dimension(), getBlockPos()));
        }
        return null;
    }

    @Override
    public AudioSource getSourceForChannel(int channel) {
        return null;
    }

    public SpeakerBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(BlockEntities.SPEAKER.get(), blockPos, blockState);
    }

    @Override
    public void write(CompoundTag compoundTag) {
        super.write(compoundTag);
    }

    @Override
    public void read(CompoundTag compoundTag) {
        super.read(compoundTag);
    }
}
