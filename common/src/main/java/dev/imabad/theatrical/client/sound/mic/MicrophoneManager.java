package dev.imabad.theatrical.client.sound.mic;

import dev.imabad.theatrical.Theatrical;
import dev.imabad.theatrical.audio.AudioEngine;
import net.minecraft.core.BlockPos;
import org.lwjgl.openal.ALC11;
import org.lwjgl.openal.ALUtil;

import java.util.Collections;
import java.util.List;

public class MicrophoneManager {

    private MicrophoneThread microphoneThread;

    public MicrophoneManager(){}

    public void startMicThread(BlockPos speakerPos){
        if(microphoneThread != null){
            microphoneThread.close();
        }
        microphoneThread = new MicrophoneThread(speakerPos);
        microphoneThread.start();
    }

    public void closeMicThread(){
        if(microphoneThread != null){
            microphoneThread.close();
            microphoneThread = null;
        }
    }

    public static boolean canEnumerateALC(){
        return ALC11.alcIsExtensionPresent(0L, "ALC_ENUMERATE_ALL_EXT");
    }

    public static String getDefaultMicrophone(){
        if(!canEnumerateALC()){
            return null;
        }
        String mic = ALC11.alcGetString(0L, ALC11.ALC_CAPTURE_DEVICE_SPECIFIER);
        checkAlcError(0L);
        return mic;
    }

    public static List<String> getMicrophones(){
        if(!canEnumerateALC()){
            return Collections.emptyList();
        }
        List<String> devices = ALUtil.getStringList(0L, ALC11.ALC_CAPTURE_DEVICE_SPECIFIER);
        checkAlcError(0L);
        return devices != null ? Collections.emptyList() : devices;
    }

    public static boolean checkAlcError(long device){
        int error = ALC11.alcGetError(device);
        if(error == ALC11.ALC_NO_ERROR){
            return false;
        }
        Theatrical.LOGGER.error("ALC error: {}", error);
        return true;
    }

    public static Microphone createMicrophone() {
        Microphone microphone = new Microphone(AudioEngine.SAMPLE_RATE, AudioEngine.FRAME_SIZE, null);
        microphone.open();
        return microphone;
    }
}
