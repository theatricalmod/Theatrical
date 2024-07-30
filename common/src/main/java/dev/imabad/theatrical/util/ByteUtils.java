package dev.imabad.theatrical.util;

import java.nio.ByteBuffer;

public class ByteUtils {
    private static ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);

    public static byte[] longToBytes(long x) {
        buffer.putLong(0, x);
        return buffer.array();
    }
    public static short calculateChecksum(byte[] data){
        int checksum = 0;
        for (byte b : data) {
            checksum += (b & 0xFF); // ensure b is treated as an unsigned byte
        }
        return (short) (checksum & 0xFFFF); // ensure the checksum is 16-bit
    }
}
