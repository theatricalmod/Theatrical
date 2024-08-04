package dev.imabad.theatrical.dmx;

import ch.bildspur.artnet.rdm.RDMDeviceId;
import dev.imabad.theatrical.api.dmx.DMXConsumer;
import dev.imabad.theatrical.net.artnet.NotifyConsumerChange;
import io.netty.util.collection.IntObjectHashMap;
import io.netty.util.collection.IntObjectMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class DMXNetwork {
    private final UUID id;
    private String name;
    private DMXNetworkMode mode;
    private final Set<DMXNetworkMember> members;
    private final IntObjectMap<Set<DMXConsumer>> universeToNodeMap = new IntObjectHashMap<>();
    private final Map<Integer, byte[]> universeDataMap = new HashMap<>();
    private final Set<ServerPlayer> knownSenders = new HashSet<>();
    public DMXNetwork(UUID id, String name, DMXNetworkMode mode, Set<DMXNetworkMember> members) {
        this.id = id;
        this.name = name;
        this.mode = mode;
        this.members = members;
    }
    public DMXNetwork(CompoundTag data){
        this.id = data.getUUID("id");
        this.name = data.getString("name");
        this.mode = DMXNetworkMode.valueOf(data.getString("mode"));
        this.members = new HashSet<>();
        ListTag membersList = data.getList("members", CompoundTag.TAG_COMPOUND);
        for (Tag tag : membersList) {
            CompoundTag member = (CompoundTag) tag;
            UUID player = member.getUUID("player");
            DMXNetworkMemberRole role = DMXNetworkMemberRole.valueOf(member.getString("role"));
            members.add(new DMXNetworkMember(player, role));
        }
    }
    public DMXNetwork(String name){
        this.id = UUID.randomUUID();
        this.name = name;
        this.mode = DMXNetworkMode.PRIVATE;
        this.members = new HashSet<>();
    }

    public CompoundTag save(){
        CompoundTag tag = new CompoundTag();
        tag.putUUID("id", id);
        tag.putString("mode", mode.toString());
        tag.putString("name", name);
        ListTag membersList = new ListTag();
        for (DMXNetworkMember member : members) {
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

    public DMXNetworkMode mode() {
        return mode;
    }

    public Set<DMXNetworkMember> members() {
        return members;
    }

    public byte[] getDmxData(int universe){
        if(!universeDataMap.containsKey(universe))
            universeDataMap.put(universe, new byte[512]);

        return universeDataMap.get(universe);
    }

    public void setDmxData(int universe, final byte[] dmxData)
    {
        universeDataMap.put(universe, dmxData);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (DMXNetwork) obj;
        return Objects.equals(this.id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "DMXNetwork[" +
                "id=" + id + ", " +
                "mode=" + mode + ", " +
                "members=" + members + ']';
    }

    public void addConsumer(DMXConsumer consumer){
        Set<DMXConsumer> universe = universeToNodeMap.computeIfAbsent(consumer.getUniverse(), (uni) -> new HashSet<>());
        universe.add(consumer);
        universeToNodeMap.put(consumer.getUniverse(), universe);
        new NotifyConsumerChange(consumer.getUniverse(),
                NotifyConsumerChange.ChangeType.ADD,
                new DMXDevice(
                        consumer.getDeviceId(), consumer.getChannelStart(), consumer.getChannelCount(),
                        consumer.getDeviceTypeId(), consumer.getActivePersonality(), consumer.getModelName(), consumer.getFixtureId()))
                .sendTo(knownSenders);
    }

    public void updateConsumer(DMXConsumer consumer){
        new NotifyConsumerChange(consumer.getUniverse(),
                NotifyConsumerChange.ChangeType.UPDATE,
                new DMXDevice(consumer.getDeviceId(), consumer.getChannelStart(), consumer.getChannelCount(),
                        consumer.getDeviceTypeId(),consumer.getActivePersonality(), consumer.getModelName(), consumer.getFixtureId()))
                .sendTo(knownSenders);
    }
    public void removeConsumer(DMXConsumer consumer){
        if(!universeToNodeMap.containsKey(consumer.getUniverse())){
            return;
        }
        Set<DMXConsumer> universe = universeToNodeMap.get(consumer.getUniverse());
        universe.remove(consumer);
        new NotifyConsumerChange(consumer.getUniverse(), NotifyConsumerChange.ChangeType.REMOVE,
                new DMXDevice(consumer.getDeviceId(), 0, 0,0,
                        0, "", new ResourceLocation("")))
                .sendTo(knownSenders);
    }
    @Nullable
    public Collection<DMXConsumer> getConsumers(int universe){
        if(universeToNodeMap.get(universe) != null) {
            return universeToNodeMap.get(universe);
        }
        return null;
    }

    public DMXConsumer getConsumer(int universe, RDMDeviceId deviceId){
        for (DMXConsumer blockPosDMXConsumerEntry : universeToNodeMap.get(universe)) {
            if(blockPosDMXConsumerEntry.getDeviceId().equals(deviceId)){
                return blockPosDMXConsumerEntry;
            }
        }
        return null;
    }

//    public Collection<DMXConsumer> getConsumersInRange(int universe, BlockPos fromPos, int radius){
//        Collection<DMXConsumer> consumers = new HashSet<>();
//        if(!universeToNodeMap.containsKey(universe) || universeToNodeMap.get(universe).isEmpty()){
//            return consumers;
//        }
//        for(Map.Entry<BlockPos, DMXConsumer> entry : universeToNodeMap.get(universe).entrySet()){
//            if(Math.sqrt(fromPos.distToCenterSqr(entry.getKey().getX(), entry.getKey().getY(), entry.getKey().getZ())) <= radius){
//                consumers.add(entry.getValue());
//            }
//        }
//        return consumers;
//    }

    public Set<Integer> getUniverses(){
        return universeToNodeMap.keySet();
    }

    public void addMember(UUID playerUUID, DMXNetworkMemberRole role){
        members.add(new DMXNetworkMember(playerUUID, role));
        DMXNetworkData.getInstance().setDirty();
    }
    public void removeMember(UUID playerUUID){
        DMXNetworkMember dmxNetworkMember = getDmxNetworkMember(playerUUID);
        members.remove(dmxNetworkMember);
        DMXNetworkData.getInstance().setDirty();
    }

    public boolean isMember(UUID playerUUID){
        return members.stream().anyMatch(x -> x.playerId().equals(playerUUID));
    }

    public boolean canSendDMX(UUID uuid) {
        DMXNetworkMember dmxNetworkMember = getDmxNetworkMember(uuid);
        if (dmxNetworkMember == null) return false;
        return dmxNetworkMember.role() == DMXNetworkMemberRole.SEND || dmxNetworkMember.role() == DMXNetworkMemberRole.ADMIN;
    }

    public boolean isAdmin(UUID uuid){
        DMXNetworkMember dmxNetworkMember = getDmxNetworkMember(uuid);
        if (dmxNetworkMember == null) return false;
        return dmxNetworkMember.role() == DMXNetworkMemberRole.ADMIN;
    }

    public void setMemberRole(UUID playerId, DMXNetworkMemberRole role){
        DMXNetworkMember dmxNetworkMember = getDmxNetworkMember(playerId);
        if(dmxNetworkMember != null) {
            dmxNetworkMember.setRole(role);
            DMXNetworkData.getInstance().setDirty();
        }
    }

    @Nullable
    private DMXNetworkMember getDmxNetworkMember(UUID uuid) {
        Optional<DMXNetworkMember> first = members.stream()
                .filter(dmxNetworkMember -> dmxNetworkMember.playerId().equals(uuid)).findFirst();
        return first.orElse(null);
    }

    public void setName(String name) {
        this.name = name;
        DMXNetworkData.getInstance().setDirty();
    }

    public void setMode(DMXNetworkMode mode) {
        this.mode = mode;
        DMXNetworkData.getInstance().setDirty();
    }
}
