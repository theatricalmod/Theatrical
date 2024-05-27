package dev.imabad.theatrical.net.artnet;

import ch.bildspur.artnet.rdm.RDMDeviceId;
import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseS2CMessage;
import dev.architectury.networking.simple.MessageType;
import dev.imabad.theatrical.TheatricalClient;
import dev.imabad.theatrical.dmx.DMXDevice;
import dev.imabad.theatrical.net.TheatricalNet;
import net.fabricmc.api.EnvType;
import net.minecraft.network.FriendlyByteBuf;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class ListConsumers extends BaseS2CMessage {

    private int universe;
    private List<DMXDevice> dmxDevices;

    public ListConsumers(int universe, List<DMXDevice> dmxDevices){
        this.universe = universe;
        this.dmxDevices = dmxDevices;
    }

    public ListConsumers(FriendlyByteBuf buf){
        universe = buf.readInt();
        int count = buf.readInt();
        dmxDevices = new ArrayList<>();
        for(int i = 0; i < count; i++){
            dmxDevices.add(new DMXDevice(new RDMDeviceId(buf.readByteArray(6)), buf.readInt(), buf.readInt(),
                    buf.readInt(), buf.readInt(), buf.readUtf(), buf.readResourceLocation()));
        }
    }

    @Override
    public MessageType getType() {
        return TheatricalNet.LIST_CONSUMERS;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeInt(universe);
        buf.writeInt(dmxDevices.size());
        for (DMXDevice dmxDevice : dmxDevices) {
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
            TheatricalClient.handleListConsumers(this);
        }
    }

    public int getUniverse() {
        return universe;
    }

    public List<DMXDevice> getDmxDevices() {
        return dmxDevices;
    }
}
