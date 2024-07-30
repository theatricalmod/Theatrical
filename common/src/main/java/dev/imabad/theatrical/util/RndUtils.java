package dev.imabad.theatrical.util;

import net.minecraft.util.RandomSource;

public class RndUtils {

    public static void nextBytes(RandomSource randomSource, byte[] bytes){
        for (int i = 0, len = bytes.length; i < len; )
            for (int rnd = randomSource.nextInt(),
                 n = Math.min(len - i, Integer.SIZE/Byte.SIZE);
                 n-- > 0; rnd >>= Byte.SIZE)
                bytes[i++] = (byte)rnd;
    }

}
