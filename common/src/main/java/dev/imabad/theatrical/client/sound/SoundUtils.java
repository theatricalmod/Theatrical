package dev.imabad.theatrical.client.sound;

public class SoundUtils {

    public static short[] floatsToShorts(float[] audioData){
        short[] shortAudioData = new short[audioData.length];
        for(int i = 0; i < audioData.length; i++){
            shortAudioData[i] = (short) Math.max(Math.min(audioData[i] * Short.MAX_VALUE, Short.MAX_VALUE - 1), -Short.MAX_VALUE);
        }
        return shortAudioData;
    }

    public static void ezSleep(int ms){
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e){}
    }

    public static byte[] convertShortArrayToByteArray(short[] shortArray) {
        int shortArrayLength = shortArray.length;
        byte[] byteArray = new byte[shortArrayLength * 2]; // Each short is 2 bytes

        for (int i = 0; i < shortArrayLength; i++) {
            short currentShort = shortArray[i];

            // Extract the bytes from the short
            byteArray[i * 2] = (byte) (currentShort & 0xFF); // Least significant byte
            byteArray[i * 2 + 1] = (byte) ((currentShort >> 8) & 0xFF); // Most significant byte
        }

        return byteArray;
    }
}
