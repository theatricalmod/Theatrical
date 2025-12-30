package dev.imabad.theatrical.networks;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.imabad.theatrical.networks.members.NetworkMemberManager;
import dev.imabad.theatrical.networks.members.TheatricalNetworkMember;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;

import java.util.*;

public class TheatricalNetwork {
    private final UUID id;
    private String name;
    private TheatricalNetworkMode mode;
    private final NetworkMemberManager members;
    private final NetworkDMXManager dmx;

    public static final Codec<TheatricalNetwork> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    UUIDUtil.CODEC.fieldOf("id").forGetter(TheatricalNetwork::id),
                    Codec.STRING.fieldOf("name").forGetter(TheatricalNetwork::name),
                    TheatricalNetworkMode.CODEC.fieldOf("mode").forGetter(TheatricalNetwork::mode),
                    NetworkMemberManager.CODEC.fieldOf("members").forGetter(TheatricalNetwork::members)
            )
                    .apply(instance,  TheatricalNetwork::new)
    );

    public TheatricalNetwork(UUID id, String name, TheatricalNetworkMode mode, NetworkMemberManager members){
        this.id = id;
        this.name = name;
        this.mode = mode;
        this.members = members;
        this.dmx = new NetworkDMXManager();
    }

    public TheatricalNetwork(String name){
        this.id = UUID.randomUUID();
        this.name = name;
        this.mode = TheatricalNetworkMode.PRIVATE;
        this.members = new NetworkMemberManager();
        this.dmx = new NetworkDMXManager();
    }

    public String name(){ return name;}

    public UUID id() {
        return id;
    }

    public TheatricalNetworkMode mode() {
        return mode;
    }

    public NetworkMemberManager members() {
        return members;
    }

    public NetworkDMXManager dmx() { return dmx; }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (TheatricalNetwork) obj;
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

    public void setName(String name) {
        this.name = name;
        TheatricalNetworkData.getInstance().setDirty();
    }

    public void setMode(TheatricalNetworkMode mode) {
        this.mode = mode;
        TheatricalNetworkData.getInstance().setDirty();
    }
}
