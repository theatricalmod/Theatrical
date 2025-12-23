package dev.imabad.theatrical.networks.members;

import dev.imabad.theatrical.networks.TheatricalNetworkData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public record NetworkMemberManager(Set<TheatricalNetworkMember> members) {

    public NetworkMemberManager(CompoundTag data) {
        this(new HashSet<>());
        ListTag membersList = data.getList("members", CompoundTag.TAG_COMPOUND);
        for (Tag tag : membersList) {
            CompoundTag member = (CompoundTag) tag;
            UUID player = member.getUUID("player");
            TheatricalNetworkMemberRole role = TheatricalNetworkMemberRole.valueOf(member.getString("role"));
            members.add(new TheatricalNetworkMember(player, role));
        }
    }

    public NetworkMemberManager() {
        this(new HashSet<>());
    }

    public CompoundTag save(CompoundTag output) {
        CompoundTag tag = new CompoundTag();
        ListTag membersList = new ListTag();
        for (TheatricalNetworkMember member : members) {
            CompoundTag memberTag = new CompoundTag();
            memberTag.putUUID("player", member.playerId());
            memberTag.putString("role", member.role().toString());
            membersList.add(memberTag);
        }
        output.put("members", membersList);
        return output;
    }

    public void addMember(UUID playerUUID, TheatricalNetworkMemberRole role) {
        members.add(new TheatricalNetworkMember(playerUUID, role));
        TheatricalNetworkData.getInstance().setDirty();
    }

    public void removeMember(UUID playerUUID) {
        TheatricalNetworkMember theatricalNetworkMember = getDmxNetworkMember(playerUUID);
        members.remove(theatricalNetworkMember);
        TheatricalNetworkData.getInstance().setDirty();
    }

    public boolean isMember(UUID playerUUID) {
        return members.stream().anyMatch(x -> x.playerId().equals(playerUUID));
    }

    public boolean canSendDMX(UUID uuid) {
        TheatricalNetworkMember theatricalNetworkMember = getDmxNetworkMember(uuid);
        if (theatricalNetworkMember == null) return false;
        return theatricalNetworkMember.role() == TheatricalNetworkMemberRole.SEND || theatricalNetworkMember.role() == TheatricalNetworkMemberRole.ADMIN;
    }

    public boolean isAdmin(UUID uuid) {
        TheatricalNetworkMember theatricalNetworkMember = getDmxNetworkMember(uuid);
        if (theatricalNetworkMember == null) return false;
        return theatricalNetworkMember.role() == TheatricalNetworkMemberRole.ADMIN;
    }

    public void setMemberRole(UUID playerId, TheatricalNetworkMemberRole role) {
        TheatricalNetworkMember theatricalNetworkMember = getDmxNetworkMember(playerId);
        if (theatricalNetworkMember != null) {
            theatricalNetworkMember.setRole(role);
            TheatricalNetworkData.getInstance().setDirty();
        }
    }

    @Nullable
    private TheatricalNetworkMember getDmxNetworkMember(UUID uuid) {
        Optional<TheatricalNetworkMember> first = members.stream()
                .filter(theatricalNetworkMember -> theatricalNetworkMember.playerId().equals(uuid)).findFirst();
        return first.orElse(null);
    }
}
