package dev.imabad.theatrical.networks.members;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.imabad.theatrical.networks.TheatricalNetwork;
import dev.imabad.theatrical.networks.TheatricalNetworkData;
import dev.imabad.theatrical.networks.TheatricalNetworkMode;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public record NetworkMemberManager(HashSet<TheatricalNetworkMember> members) {

    public static final Codec<NetworkMemberManager> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                TheatricalNetworkMember.CODEC.listOf().fieldOf("members")
                        .xmap(HashSet::new, ArrayList::new).forGetter(NetworkMemberManager::members)
            )
            .apply(instance, NetworkMemberManager::new)
    );


    public NetworkMemberManager() {
        this(new HashSet<>());
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
