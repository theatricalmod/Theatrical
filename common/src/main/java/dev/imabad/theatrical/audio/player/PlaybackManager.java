package dev.imabad.theatrical.audio.player;

import com.sedmelluq.discord.lavaplayer.format.AudioDataFormat;
import com.sedmelluq.discord.lavaplayer.format.Pcm16AudioDataFormat;
import com.sedmelluq.discord.lavaplayer.format.StandardAudioDataFormats;
import com.sedmelluq.discord.lavaplayer.player.AudioConfiguration;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import dev.imabad.theatrical.audio.AudioEngine;
import dev.imabad.theatrical.util.DimensionBlockPos;

import java.util.HashMap;
import java.util.Map;

public class PlaybackManager {

    public static final PlaybackManager INSTANCE = new PlaybackManager();

    private AudioPlayerManager manager = new DefaultAudioPlayerManager();
    private Map<DimensionBlockPos, AudioPlayer> playerMap = new HashMap<>();

    public PlaybackManager(){
        TestLoadNatives.loadConnectorLibrary();
        AudioSourceManagers.registerRemoteSources(manager);
        manager.getConfiguration()
                .setOutputFormat(
                        new Pcm16AudioDataFormat(1, AudioEngine.SAMPLE_RATE, AudioEngine.FRAME_SIZE, false)
                );
    }

    public AudioPlayer getPlayer(DimensionBlockPos pos){
        return playerMap.computeIfAbsent(pos, (p) -> manager.createPlayer());
    }

    public AudioPlayerManager getManager() {
        return manager;
    }
}
