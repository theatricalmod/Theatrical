package dev.imabad.theatrical;

import dev.imabad.theatrical.net.artnet.NotifyConsumerChange;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;

import java.util.function.IntFunction;

public enum TheatricalScreen {

    GENERIC_DMX,
    BASIC_LIGHTING_DESK,
    FRESNEL,
    GENERIC_PAN_TILT;

    public static final IntFunction<TheatricalScreen> BY_ID =
            ByIdMap.continuous(
                    TheatricalScreen::ordinal,
                    TheatricalScreen.values(),
                    ByIdMap.OutOfBoundsStrategy.ZERO
            );

    public static final StreamCodec<ByteBuf, TheatricalScreen> ID_STREAM_CODEC =
            ByteBufCodecs.idMapper(TheatricalScreen.BY_ID, TheatricalScreen::ordinal);
}
