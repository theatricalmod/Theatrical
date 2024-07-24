package dev.imabad.theatrical.dmx;

import java.util.UUID;

public record DMXNetworkMember(UUID playerId, DMXNetworkMemberRole role) {
}
