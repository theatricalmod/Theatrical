package dev.imabad.theatrical.blockentities.sound;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEvent;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventListener;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import dev.imabad.theatrical.api.network.audio.AudioChannelDefinition;
import dev.imabad.theatrical.api.network.audio.AudioChannelType;
import dev.imabad.theatrical.api.network.audio.AudioDeviceDefinition;
import dev.imabad.theatrical.audio.AudioSink;
import dev.imabad.theatrical.audio.AudioSource;
import dev.imabad.theatrical.audio.player.AudioPlayerSource;
import dev.imabad.theatrical.audio.player.PlaybackManager;
import dev.imabad.theatrical.blockentities.BlockEntities;
import dev.imabad.theatrical.util.DimensionBlockPos;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MusicPlayerBlockEntity extends BaseAudioNetworkDeviceBlockEntity implements AudioEventListener {

    private AudioPlayer audioPlayer;
    private AudioPlayerSource audioSource;

    public MusicPlayerBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(BlockEntities.MUSIC_PLAYER.get(), blockPos, blockState);
    }

    @Override
    public void setLevel(Level level) {
        super.setLevel(level);
        if(level != null && !level.isClientSide){
            audioPlayer = PlaybackManager.INSTANCE.getPlayer(DimensionBlockPos.of(level.dimension(), getBlockPos()));
            audioPlayer.addListener(this);
            audioSource = new AudioPlayerSource(audioPlayer);
        }
    }

    @Override
    public AudioDeviceDefinition getDefinition() {
        return new AudioDeviceDefinition("Music Player", List.of(
                new AudioChannelDefinition("Music Out L", 0, AudioChannelType.OUTPUT),
                new AudioChannelDefinition("Music Out R", 1, AudioChannelType.OUTPUT)));
    }

    @Override
    public @Nullable AudioSink getSinkForChannel(int channel) {
        return null;
    }

    @Override
    public @Nullable AudioSource getSourceForChannel(int channel) {
        if(channel == 0){
            return audioSource;
        }
        return null;
    }

    public void playTrack(){
        if(audioPlayer.isPaused()) {
            PlaybackManager.INSTANCE.getManager().loadItem("https://dist.creeper.host/Rushmeadfiles/lovetropics.wav", new AudioLoadResultHandler() {
                @Override
                public void trackLoaded(AudioTrack audioTrack) {
                    System.out.println("Loaded track");
                    audioPlayer.playTrack(audioTrack);
                    audioPlayer.setPaused(false);
                }

                @Override
                public void playlistLoaded(AudioPlaylist audioPlaylist) {

                }

                @Override
                public void noMatches() {

                }

                @Override
                public void loadFailed(FriendlyException e) {
                    e.printStackTrace();
                }
            });
        } else {
            audioPlayer.setPaused(true);
        }
    }

    @Override
    public void onEvent(AudioEvent audioEvent) {

    }
}
