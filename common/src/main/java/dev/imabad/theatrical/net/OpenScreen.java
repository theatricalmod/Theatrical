package dev.imabad.theatrical.net;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseS2CMessage;
import dev.architectury.networking.simple.MessageType;
import dev.imabad.theatrical.TheatricalClient;
import dev.imabad.theatrical.TheatricalScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

public class OpenScreen extends BaseS2CMessage {

    private final BlockPos pos;
    private final TheatricalScreen screen;

    public OpenScreen(BlockPos pos, TheatricalScreen screen) {
        this.pos = pos;
        this.screen = screen;
    }

    public OpenScreen(FriendlyByteBuf buf){
        this.pos = buf.readBlockPos();
        this.screen = buf.readEnum(TheatricalScreen.class);
    }

    @Override
    public MessageType getType() {
        return TheatricalNet.OPEN_SCREEN;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeEnum(screen);
    }

    @Override
    public void handle(NetworkManager.PacketContext context) {
        context.queue(() -> TheatricalClient.handleOpenScreen(this));
    }

    public BlockPos getPos() {
        return pos;
    }

    public TheatricalScreen getScreen() {
        return screen;
    }
}
