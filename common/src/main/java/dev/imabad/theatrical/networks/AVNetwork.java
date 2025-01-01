package dev.imabad.theatrical.networks;

import dev.imabad.theatrical.networks.handlers.AVNetworkAudioHandler;
import dev.imabad.theatrical.networks.handlers.AVNetworkDMXHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class AVNetwork {
    private final UUID id;
    private String name;
    private AVNetworkMode mode;
    private final Set<AVNetworkMember> members;

    private final AVNetworkDMXHandler dmxHandler;
    private final AVNetworkAudioHandler audioHandler;

    public AVNetwork(UUID id, String name, AVNetworkMode mode, Set<AVNetworkMember> members) {
        this.id = id;
        this.name = name;
        this.mode = mode;
        this.members = members;
        dmxHandler = new AVNetworkDMXHandler();
        audioHandler = new AVNetworkAudioHandler();
    }

    public AVNetwork(CompoundTag data){
        this(data.getUUID("id"), data.getString("name"), AVNetworkMode.valueOf(data.getString("mode")),
                data.getList("members", CompoundTag.TAG_COMPOUND).stream().map(
                (tag) ->  new AVNetworkMember(((CompoundTag) tag).getUUID("player"), AVNetworkMemberRole.valueOf(((CompoundTag) tag).getString("role")))
        ).collect(Collectors.toSet()));
    }
    public AVNetwork(String name){
        this(UUID.randomUUID(), name, AVNetworkMode.PRIVATE, new HashSet<>());
    }

    public CompoundTag save(){
        CompoundTag tag = new CompoundTag();
        tag.putUUID("id", id);
        tag.putString("mode", mode.toString());
        tag.putString("name", name);
        ListTag membersList = new ListTag();
        for (AVNetworkMember member : members) {
            CompoundTag memberTag = new CompoundTag();
            memberTag.putUUID("player", member.playerId());
            memberTag.putString("role", member.role().toString());
            membersList.add(memberTag);
        }
        tag.put("members", membersList);
        return tag;
    }

    public String name(){ return name;}

    public UUID id() {
        return id;
    }

    public AVNetworkMode mode() {
        return mode;
    }

    public Set<AVNetworkMember> members() {
        return members;
    }

    public AVNetworkDMXHandler getDmxHandler() {
        return dmxHandler;
    }

    public AVNetworkAudioHandler getAudioHandler() {
        return audioHandler;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (AVNetwork) obj;
        return Objects.equals(this.id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "AVNetwork[" +
                "id=" + id + ", " +
                "mode=" + mode + ", " +
                "members=" + members + ']';
    }

    public void addMember(UUID playerUUID, AVNetworkMemberRole role){
        members.add(new AVNetworkMember(playerUUID, role));
        AVNetworkData.getInstance().setDirty();
    }
    public void removeMember(UUID playerUUID){
        AVNetworkMember AVNetworkMember = getDmxNetworkMember(playerUUID);
        members.remove(AVNetworkMember);
        AVNetworkData.getInstance().setDirty();
    }

    public boolean isMember(UUID playerUUID){
        return members.stream().anyMatch(x -> x.playerId().equals(playerUUID));
    }

    public boolean canSendDMX(UUID uuid) {
        AVNetworkMember AVNetworkMember = getDmxNetworkMember(uuid);
        if (AVNetworkMember == null) return false;
        return AVNetworkMember.role() == AVNetworkMemberRole.SEND || AVNetworkMember.role() == AVNetworkMemberRole.ADMIN;
    }

    public boolean isAdmin(UUID uuid){
        AVNetworkMember AVNetworkMember = getDmxNetworkMember(uuid);
        if (AVNetworkMember == null) return false;
        return AVNetworkMember.role() == AVNetworkMemberRole.ADMIN;
    }

    public void setMemberRole(UUID playerId, AVNetworkMemberRole role){
        AVNetworkMember AVNetworkMember = getDmxNetworkMember(playerId);
        if(AVNetworkMember != null) {
            AVNetworkMember.setRole(role);
            AVNetworkData.getInstance().setDirty();
        }
    }

    @Nullable
    private AVNetworkMember getDmxNetworkMember(UUID uuid) {
        Optional<AVNetworkMember> first = members.stream()
                .filter(AVNetworkMember -> AVNetworkMember.playerId().equals(uuid)).findFirst();
        return first.orElse(null);
    }

    public void setName(String name) {
        this.name = name;
        AVNetworkData.getInstance().setDirty();
    }

    public void setMode(AVNetworkMode mode) {
        this.mode = mode;
        AVNetworkData.getInstance().setDirty();
    }
}
