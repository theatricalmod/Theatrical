package dev.imabad.theatrical.networks;

import java.util.Objects;
import java.util.UUID;

public final class AVNetworkMember {
    private final UUID playerId;
    private AVNetworkMemberRole role;

    public AVNetworkMember(UUID playerId, AVNetworkMemberRole role) {
        this.playerId = playerId;
        this.role = role;
    }

    public UUID playerId() {
        return playerId;
    }

    public AVNetworkMemberRole role() {
        return role;
    }

    public void setRole(AVNetworkMemberRole role) {
        this.role = role;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (AVNetworkMember) obj;
        return Objects.equals(this.playerId, that.playerId) &&
                Objects.equals(this.role, that.role);
    }

    @Override
    public int hashCode() {
        return Objects.hash(playerId, role);
    }

    @Override
    public String toString() {
        return "AVNetworkMember[" +
                "playerId=" + playerId + ", " +
                "role=" + role + ']';
    }

}
