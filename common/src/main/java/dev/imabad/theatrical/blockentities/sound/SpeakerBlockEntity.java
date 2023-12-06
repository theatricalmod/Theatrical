package dev.imabad.theatrical.blockentities.sound;

import dev.imabad.theatrical.blockentities.BlockEntities;
import dev.imabad.theatrical.blockentities.ClientSyncBlockEntity;
import dev.imabad.theatrical.client.sound.OpusStreamedAudioStream;
import dev.imabad.theatrical.net.sound.SpeakerAudioClient;
import dev.imabad.theatrical.net.sound.StartMicrophone;
import net.labymod.opus.OpusCodec;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.UUID;

public class SpeakerBlockEntity extends ClientSyncBlockEntity {

    private OpusCodec codec;
    private UUID sourceId = UUID.randomUUID();

    public SpeakerBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(BlockEntities.SPEAKER.get(), blockPos, blockState);
        codec = OpusCodec.createDefault();
    }

    @Override
    public void write(CompoundTag compoundTag) {

    }

    @Override
    public void read(CompoundTag compoundTag) {

    }
    public static <T extends BlockEntity> void tick(Level level, BlockPos pos, BlockState state, T be) {
        SpeakerBlockEntity speaker = (SpeakerBlockEntity) be;
    }
    public void playAudio(){
        new StartMicrophone(getBlockPos(), true).sendToChunkListeners(level.getChunkAt(getBlockPos()));
    }
    public void stopAudio(){
        new StartMicrophone(getBlockPos(), false).sendToChunkListeners(level.getChunkAt(getBlockPos()));
    }

    @Override
    public void setLevel(Level level) {
        super.setLevel(level);
    }

    @Override
    public void setRemoved() {
        if(!level.isClientSide){
            stopAudio();
        }
        super.setRemoved();
    }
}
