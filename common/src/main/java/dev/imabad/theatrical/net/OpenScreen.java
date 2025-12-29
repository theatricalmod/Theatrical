package dev.imabad.theatrical.net;

import dev.architectury.networking.NetworkManager;
import dev.imabad.theatrical.Theatrical;
import dev.imabad.theatrical.TheatricalClient;
import dev.imabad.theatrical.TheatricalScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record OpenScreen(BlockPos pos, TheatricalScreen screen) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<OpenScreen> TYPE = new CustomPacketPayload.Type<>(Theatrical.location("open_screen"));

    public static final StreamCodec<RegistryFriendlyByteBuf, OpenScreen> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            OpenScreen::pos,
            TheatricalScreen.ID_STREAM_CODEC,
            OpenScreen::screen,
            OpenScreen::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(NetworkManager.PacketContext context) {
        context.queue(() -> TheatricalClient.handleOpenScreen(this));
    }
}
