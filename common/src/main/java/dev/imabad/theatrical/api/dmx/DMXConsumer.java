package dev.imabad.theatrical.api.dmx;

import ch.bildspur.artnet.rdm.RDMDeviceId;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public interface DMXConsumer extends BelongsToNetwork {

    int getChannelCount();

    int getChannelStart();

    int getUniverse();

    void consume(byte[] dmxValues);

    RDMDeviceId getDeviceId();

    int getDeviceTypeId();

    String getModelName();

    ResourceLocation getFixtureId();

    int getActivePersonality();

    UUID getNetworkId();

    void setStartAddress(int startAddress);
}
