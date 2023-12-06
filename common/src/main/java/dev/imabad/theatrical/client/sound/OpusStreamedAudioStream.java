package dev.imabad.theatrical.client.sound;

import com.google.common.base.Preconditions;
import com.mojang.blaze3d.audio.Channel;
import dev.imabad.theatrical.client.sound.mic.MicrophoneManager;
import io.netty.buffer.ByteBuf;
import net.labymod.opus.OpusCodec;
import net.minecraft.client.Minecraft;
import net.minecraft.client.sounds.AudioStream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.BufferUtils;

import javax.sound.sampled.AudioFormat;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.InvalidMarkException;
import java.nio.ShortBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.Executor;

public class OpusStreamedAudioStream implements AudioStream {
    private static final AudioFormat STEREO_16 = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, MicrophoneManager.SAMPLE_RATE, 16, 1, 2, 48000, false);
    private final Queue<ByteBuffer> buffers = new ArrayDeque<>(2);
    private final OpusCodec opusCodec = OpusCodec.newBuilder()
            .withFrameSize(MicrophoneManager.FRAME_SIZE)
            .withChannels(1)
            .withSampleRate(MicrophoneManager.SAMPLE_RATE)
            .build();

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
        ByteBuffer wrap = ByteBuffer.wrap(bytes);
        synchronized (this) {
            buffers.add(wrap);
        }
    }

    @Override
    public ByteBuffer read(int size) throws IOException {
        var result = BufferUtils.createByteBuffer(size);
        while (result.hasRemaining()) {
            var head = buffers.peek();
            if (head == null) break;

            var toRead = Math.min(head.remaining(), result.remaining());
            result.put(result.position(), head, head.position(), toRead);
            result.position(result.position() + toRead);
            head.position(head.position() + toRead);

            if (head.hasRemaining()) break;
            buffers.remove();
        }

        result.flip();

        // This is naughty, but ensures we're not enqueuing empty buffers when the stream is exhausted.
        return result.remaining() == 0 ? null : result;
    }

    @Override
    public void close() throws IOException {
        buffers.clear();
    }

    public boolean ready(){
        return buffers.size() > 5;
    }

    public boolean isEmpty() {
        return buffers.isEmpty();
    }
    private byte[] shortToByteTwiddle(final short[] input) {
        final int len = input.length;
        final byte[] buffer = new byte[len * 2];
        for (int i = 0; i < len; i++) {
            buffer[(i * 2) + 1] = (byte) (input[i]);
            buffer[(i * 2)] = (byte) (input[i] >> 8);
        }
        return buffer;
    }
}
