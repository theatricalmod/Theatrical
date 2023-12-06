package dev.imabad.theatrical.client.sound.mic;

import dev.imabad.theatrical.Theatrical;
import dev.imabad.theatrical.client.sound.SoundUtils;
import dev.imabad.theatrical.net.sound.MicrophoneAudioPacket;
import dev.imabad.theatrical.net.sound.SpeakerAudioClient;
import net.labymod.opus.OpusCodec;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.UUID;

public class MicrophoneThread extends Thread {

    private final OpusCodec opusCodec;
    private final UUID soundID;
    private final BlockPos speakerPos;
    private boolean running;
    private Microphone mic;

    public MicrophoneThread(BlockPos speakerPos) {
        this.opusCodec = OpusCodec.newBuilder()
                .withFrameSize(MicrophoneManager.FRAME_SIZE)
                .withChannels(1)
                .withSampleRate(MicrophoneManager.SAMPLE_RATE)
                .build();
        this.running = true;
        this.soundID = Minecraft.getInstance().player.getUUID();
        this.speakerPos = speakerPos;
        setDaemon(true);
        setName("TheatricalMicrophoneCaptureThread");
    }

    @Override
    public void run() {
        Microphone mic = getMic();
        if(mic == null){
            return;
        }
        while(running){
            short[] audio = pollMic();
            if(audio == null){
                continue;
            }
            byte[] audioData = SoundUtils.convertShortArrayToByteArray(audio);
            byte[] encodedFrame = opusCodec.encodeFrame(audioData);
            try {
                Files.write(Path.of(Minecraft.getInstance().gameDirectory.getPath(), "opus_pre_pre"), encodedFrame, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            } catch (IOException e) {
                e.printStackTrace();
                //ignored
            }
            new MicrophoneAudioPacket(soundID, speakerPos, encodedFrame).sendToServer();
        }
    }

    private short[] pollMic() {
        Microphone mic = getMic();
        if(mic == null){
            throw new RuntimeException("Tried capturing audio when no microphone available");
        }
        if(!mic.isStarted()){
            mic.start();
        }
        if(mic.captureSamplesAvailable() < MicrophoneManager.FRAME_SIZE){
            SoundUtils.ezSleep(5);
            return null;
        }
        return mic.read();
    }

    private Microphone getMic(){
        if(!running){
            return null;
        }
        if(mic == null){
            try {
                mic = MicrophoneManager.createMicrophone();
            } catch(RuntimeException e){
                Theatrical.LOGGER.error("Failed to create microphone: {}", e.getMessage());
                running = false;
                return null;
            }
        }
        return mic;
    }

    public void close(){
        if(!running){
            return;
        }
        running = false;
        if(Thread.currentThread() != this){
            try {
                join(100);
            } catch (InterruptedException ex){}
        }

        mic.close();
        opusCodec.destroy();
    }
}
