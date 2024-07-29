package dev.imabad.theatrical.api.dmx;

import java.util.UUID;

public interface BelongsToNetwork {

    UUID getNetworkId();

    void setNetworkId(UUID newNetworkId);
}
