package dev.imabad.theatrical.dmx;

import java.util.Objects;
import java.util.UUID;

public final class DMXNetworkMember {
    private final UUID playerId;
    private DMXNetworkMemberRole role;

    public DMXNetworkMember(UUID playerId, DMXNetworkMemberRole role) {
        this.playerId = playerId;
        this.role = role;
    }

    public UUID playerId() {
        return playerId;
    }

    public DMXNetworkMemberRole role() {
        return role;
    }

    public void setRole(DMXNetworkMemberRole role) {
        this.role = role;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (DMXNetworkMember) obj;
        return Objects.equals(this.playerId, that.playerId) &&
                Objects.equals(this.role, that.role);
    }

    @Override
    public int hashCode() {
        return Objects.hash(playerId, role);
    }

    @Override
    public String toString() {
        return "DMXNetworkMember[" +
                "playerId=" + playerId + ", " +
                "role=" + role + ']';
    }

}
