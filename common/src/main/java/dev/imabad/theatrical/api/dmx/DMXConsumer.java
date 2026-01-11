package dev.imabad.theatrical.api.dmx;

import ch.bildspur.artnet.rdm.RDMDeviceId;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public interface DMXConsumer extends BelongsToNetwork {

    int getChannelCount();

    int getChannelStart();

    int getUniverse();

    void consume(byte[] dmxValues, boolean mapped);

    default void consume(byte[] dmxValues){
        consume(dmxValues, false);
    }

    RDMDeviceId getDeviceId();

    int getDeviceTypeId();

    String getModelName();

    ResourceLocation getFixtureId();

    int getActivePersonality();

    UUID getNetworkId();

    String getTranslationKey();

    void setStartAddress(int startAddress);
}
