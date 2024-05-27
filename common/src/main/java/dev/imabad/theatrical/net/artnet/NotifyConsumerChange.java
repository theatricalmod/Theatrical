package dev.imabad.theatrical.net.artnet;

import ch.bildspur.artnet.rdm.RDMDeviceId;
import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseS2CMessage;
import dev.architectury.networking.simple.MessageType;
import dev.imabad.theatrical.TheatricalClient;
import dev.imabad.theatrical.dmx.DMXDevice;
import dev.imabad.theatrical.net.TheatricalNet;
import net.fabricmc.api.EnvType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

public class NotifyConsumerChange extends BaseS2CMessage {

    public enum ChangeType{
        ADD,
        UPDATE,
        REMOVE;
    }

    private int universe;
    private ChangeType changeType;
    private DMXDevice dmxDevice;

    public NotifyConsumerChange(int universe, ChangeType changeType, DMXDevice dmxDevice){
        this.universe = universe;
        this.changeType = changeType;
        this.dmxDevice = dmxDevice;
    }

    public NotifyConsumerChange(FriendlyByteBuf buf){
        universe = buf.readInt();
        changeType = ChangeType.valueOf(buf.readUtf());
        if(buf.readBoolean()) {
            dmxDevice = new DMXDevice(new RDMDeviceId(buf.readByteArray(6)), buf.readInt(),
                    buf.readInt(), buf.readInt(), buf.readInt(), buf.readUtf(), buf.readResourceLocation());
        }
    }

    @Override
    public MessageType getType() {
        return TheatricalNet.NOTIFY_CONSUMER_CHANGE;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeInt(universe);
        buf.writeUtf(changeType.name());
        buf.writeBoolean(dmxDevice != null);
        if(dmxDevice != null) {
            buf.writeByteArray(dmxDevice.getDeviceId().toBytes());
            buf.writeInt(dmxDevice.getDmxStartAddress());
            buf.writeInt(dmxDevice.getDmxChannelCount());
            buf.writeInt(dmxDevice.getDeviceTypeId());
            buf.writeInt(dmxDevice.getActivePersonality());
            buf.writeUtf(dmxDevice.getModelName());
            buf.writeResourceLocation(dmxDevice.getFixtureID());
        }
    }

    @Override
    public void handle(NetworkManager.PacketContext context) {
        if(context.getEnv() == EnvType.CLIENT){
            TheatricalClient.handleConsumerChange(this);
        }
    }

    public int getUniverse() {
        return universe;
    }

    public ChangeType getChangeType() {
        return changeType;
    }

    public DMXDevice getDmxDevice() {
        return dmxDevice;
    }
}
