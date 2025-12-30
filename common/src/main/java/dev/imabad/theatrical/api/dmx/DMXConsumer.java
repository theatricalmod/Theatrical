package dev.imabad.theatrical.api.dmx;

import ch.bildspur.artnet.rdm.RDMDeviceId;
import net.minecraft.resources.Identifier;

import java.util.UUID;

public interface DMXConsumer extends BelongsToNetwork {

    int getChannelCount();

    int getChannelStart();

    int getUniverse();

    void consume(byte[] dmxValues);

    RDMDeviceId getDeviceId();

    int getDeviceTypeId();

    String getModelName();

    Identifier getFixtureId();

    int getActivePersonality();

    UUID getNetworkId();

    String getTranslationKey();
}
