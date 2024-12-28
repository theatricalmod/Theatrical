package dev.imabad.theatrical.client.sound;

import com.mojang.blaze3d.audio.Channel;
import dev.imabad.theatrical.client.sound.mic.MicrophoneManager;
import net.labymod.opus.OpusCodec;
import net.minecraft.client.sounds.AudioStream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.BufferUtils;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.Executor;

public class OpusStreamedAudioStream implements AudioStream {
    private static final AudioFormat STEREO_16 = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, MicrophoneManager.SAMPLE_RATE, 16, 1, MicrophoneManager.FRAME_SIZE, MicrophoneManager.SAMPLE_RATE, false);

    private final OpusCodec opusCodec = OpusCodec.newBuilder()
            .withFrameSize(MicrophoneManager.FRAME_SIZE)
            .withChannels(1)
            .withSampleRate(MicrophoneManager.SAMPLE_RATE)
            .build();
    private PacketInputStream inputStream = new PacketInputStream(5);
    private AudioInputStream audioInputStream = new AudioInputStream(inputStream, STEREO_16, AudioSystem.NOT_SPECIFIED);
    private final int frameSize = STEREO_16.getFrameSize();
    private final byte[] frame = new byte[frameSize];

    @Nullable
    Channel channel;

    @Nullable
    Executor executor;

    @Override
    public @NotNull AudioFormat getFormat() {
        return STEREO_16;
    }

    void push(byte[] input) {
        byte[] bytes = opusCodec.decodeFrame(input);
        if(inputStream == null || inputStream.isClosed()){
            createStreams();
        }
        synchronized (this) {
            try {
                inputStream.writePacket(bytes);
            } catch (IOException e) {}
        }
    }

    private void createStreams(){
        inputStream = new PacketInputStream(5);
        audioInputStream = new AudioInputStream(inputStream, STEREO_16, AudioSystem.NOT_SPECIFIED);
    }

    @Override
    public ByteBuffer read(int size) throws IOException {
        // Create a ByteBuffer of the specified size
        ByteBuffer byteBuffer = BufferUtils.createByteBuffer(size);
        int bytesRead = 0, count = 0;
        // Loop to read data until the specified size is reached or the end of the input stream
        do {
            // Read the next chunk of data
            count = this.audioInputStream.read(frame);
            // Write the read data into the ByteBuffer
            if (count != -1) {
                byteBuffer.put(frame);
            }
        } while (count != -1 && (bytesRead += frameSize) < size);
        // Flip the ByteBuffer to prepare for reading
        byteBuffer.flip();
        // Return the ByteBuffer containing the read data
        return byteBuffer;
    }

    @Override
    public void close() throws IOException {
        audioInputStream.close();
    }
}
