package dev.imabad.theatrical.net;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseS2CMessage;
import dev.architectury.networking.simple.MessageType;
import dev.imabad.theatrical.TheatricalClient;
import dev.imabad.theatrical.TheatricalScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.Nullable;

public class OpenScreen extends BaseS2CMessage {

    private BlockPos pos;
    private TheatricalScreen screen;
    private Tag extraData;

    public OpenScreen(BlockPos pos, TheatricalScreen screen){
        this(pos, screen, new CompoundTag());
    }

    public OpenScreen(BlockPos pos, TheatricalScreen screen, Tag extraData) {
        this.pos = pos;
        this.screen = screen;
        this.extraData = extraData;
    }

    public OpenScreen(FriendlyByteBuf buf){
        this.pos = buf.readBlockPos();
        this.screen = buf.readEnum(TheatricalScreen.class);
        this.extraData = buf.readNbt();
    }

    @Override
    public MessageType getType() {
        return TheatricalNet.OPEN_SCREEN;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeEnum(screen);
        buf.writeNbt(extraData);
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

    public Tag getExtraData() {
        return extraData;
    }
}
