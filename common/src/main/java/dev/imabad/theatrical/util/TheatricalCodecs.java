package dev.imabad.theatrical.util;

import com.mojang.serialization.Codec;

import java.nio.ByteBuffer;

public interface TheatricalCodecs {
    Codec<byte[]> BYTE_ARRAY = Codec.BYTE_BUFFER.xmap((buf) -> {
        if (buf.hasArray()) {
            return buf.array();
        }

        var bytes = new byte[buf.capacity()];
        buf.get(bytes);
        return bytes;
    }, ByteBuffer::wrap);
}
