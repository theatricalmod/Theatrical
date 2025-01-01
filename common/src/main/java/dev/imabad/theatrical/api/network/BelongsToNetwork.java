package dev.imabad.theatrical.api.network;

import java.util.UUID;

public interface BelongsToNetwork {

    UUID getNetworkId();

    void setNetworkId(UUID newNetworkId);
}
