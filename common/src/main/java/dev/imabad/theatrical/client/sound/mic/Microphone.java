package dev.imabad.theatrical.client.sound.mic;

import dev.imabad.theatrical.Theatrical;
import dev.imabad.theatrical.client.sound.SoundUtils;
import net.minecraft.client.sounds.SoundManager;
import org.lwjgl.openal.ALC11;
import org.lwjgl.openal.EXTFloat32;

public class Microphone {

    private final int sampleRate;
    private final String deviceName;
    private final int bufferSize;
    private long deviceId;
    private boolean started = false;

    public Microphone(int sampleRate, int bufferSize, String deviceName){
        this.sampleRate = sampleRate;
        this.bufferSize = bufferSize;
        this.deviceName = deviceName;
    }

    public void open(){
        if(isOpen()){
            return;
        }
        deviceId = openMic(deviceName);
    }

    public void close(){
        if(!isOpen()){
            return;
        }
        stop();
        ALC11.alcCaptureStop(deviceId);
        MicrophoneManager.checkAlcError(deviceId);
        deviceId = 0;
    }

    public void start(){
        if(!isOpen()){
            return;
        }
        if(started){
            return;
        }
        ALC11.alcCaptureStart(deviceId);
        MicrophoneManager.checkAlcError(deviceId);
        started = true;
    }

    public void stop(){
        if(!isOpen()){
            return;
        }
        if(!started){
            return;
        }
        ALC11.alcCaptureStop(deviceId);
        MicrophoneManager.checkAlcError(deviceId);
        started = false;

        // Clear remaining samples
        int available = captureSamplesAvailable();
        float[] buf = new float[available];
        ALC11.alcCaptureSamples(deviceId, buf, buf.length);
        MicrophoneManager.checkAlcError(deviceId);
    }

    public int captureSamplesAvailable(){
        int samples = ALC11.alcGetInteger(deviceId, ALC11.ALC_CAPTURE_SAMPLES);
        MicrophoneManager.checkAlcError(deviceId);
        return samples;
    }

    public short[] read(){
        int available = captureSamplesAvailable();
        if(bufferSize > available){
            throw new RuntimeException("Failed to read from runtime");
        }
        float[] buf = new float[bufferSize];
        ALC11.alcCaptureSamples(deviceId, buf, buf.length);
        MicrophoneManager.checkAlcError(deviceId);
        return SoundUtils.floatsToShorts(buf);
    }

    private long openMic(String deviceName){
        try {
            return openMicALC(deviceName);
        } catch (RuntimeException e){
            if(deviceName != null){
                Theatrical.LOGGER.error("Failed to open Microphone: {}", deviceName);
            }
            try {
                return openMicALC(MicrophoneManager.getDefaultMicrophone());
            } catch (RuntimeException ex){
                return openMicALC(null);
            }
        }
    }

    private long openMicALC(String deviceName){
        long deviceId = ALC11.alcCaptureOpenDevice(deviceName, sampleRate, EXTFloat32.AL_FORMAT_MONO_FLOAT32, bufferSize);
        if(deviceId == 0){
            throw new RuntimeException("Failed to open microphone");
        }
        return deviceId;
    }

    public boolean isStarted(){
        return started;
    }

    public boolean isOpen(){
        return deviceId != 0;
    }

}
