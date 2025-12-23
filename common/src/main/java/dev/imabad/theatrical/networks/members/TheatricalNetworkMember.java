package dev.imabad.theatrical.networks.members;

import java.util.Objects;
import java.util.UUID;

public final class TheatricalNetworkMember {
    private final UUID playerId;
    private TheatricalNetworkMemberRole role;

    public TheatricalNetworkMember(UUID playerId, TheatricalNetworkMemberRole role) {
        this.playerId = playerId;
        this.role = role;
    }

    public UUID playerId() {
        return playerId;
    }

    public TheatricalNetworkMemberRole role() {
        return role;
    }

    public void setRole(TheatricalNetworkMemberRole role) {
        this.role = role;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (TheatricalNetworkMember) obj;
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
