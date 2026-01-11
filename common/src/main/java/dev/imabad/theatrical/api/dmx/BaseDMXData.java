package dev.imabad.theatrical.api.dmx;

import ch.bildspur.artnet.rdm.RDMDeviceId;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;

import java.nio.ByteBuffer;
import java.util.UUID;

public class BaseDMXData {

    private int channelCount, channelStart, universe;
    private RDMDeviceId deviceId;
    private UUID networkId = dev.imabad.theatrical.util.UUIDUtil.NULL;

    public static final Codec<byte[]> BYTE_ARRAY = Codec.BYTE_BUFFER.xmap((buf) -> {
        if (buf.hasArray()) {
            return buf.array();
        }

        var bytes = new byte[buf.capacity()];
        buf.get(bytes);
        return bytes;
    }, ByteBuffer::wrap);

    public static final Codec<RDMDeviceId> DEVICE_ID_CODEC = BYTE_ARRAY.xmap(RDMDeviceId::new, RDMDeviceId::toBytes);

    public static final Codec<BaseDMXData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.CODEC.fieldOf("network").forGetter(BaseDMXData::getNetworkId),
            Codec.INT.fieldOf("channelCount").forGetter(BaseDMXData::getChannelCount),
            Codec.INT.fieldOf("channelStartPoint").forGetter(BaseDMXData::getChannelStart),
            Codec.INT.fieldOf("dmxUniverse").forGetter(BaseDMXData::getUniverse),
            DEVICE_ID_CODEC.fieldOf("deviceId").forGetter(BaseDMXData::getDeviceId)
    ).apply(instance, BaseDMXData::new));

    public static BaseDMXData fromCompoundTag(CompoundTag compoundTag) {
        if(compoundTag.contains("dmxData")) {
            compoundTag = compoundTag.getCompound("dmxData");
        }
        DataResult<Pair<BaseDMXData, Tag>> decode = BaseDMXData.CODEC.decode(NbtOps.INSTANCE, compoundTag);
        if(decode.result().isPresent()) {
            return decode.result().get().getFirst();
        } else {
            return new BaseDMXData();
        }
    }

    public BaseDMXData(UUID networkId, int channelCount, int channelStart, int universe, RDMDeviceId deviceId) {
        this.channelCount = channelCount;
        this.channelStart = channelStart;
        this.universe = universe;
        this.networkId = networkId;
        this.deviceId = deviceId;
    }

    public BaseDMXData(){
    }

    public int getChannelCount() {
        return channelCount;
    }

    public int getChannelStart() {
        return channelStart;
    }

    public int getUniverse() {
        return universe;
    }

    public UUID getNetworkId() {
        return networkId;
    }

    public RDMDeviceId getDeviceId() {
        return deviceId;
    }

    public void setChannelCount(int channelCount) {
        this.channelCount = channelCount;
    }

    public void setChannelStart(int channelStart) {
        this.channelStart = channelStart;
    }

    public void setUniverse(int universe) {
        this.universe = universe;
    }

    public void setDeviceId(RDMDeviceId deviceId) {
        this.deviceId = deviceId;
    }

    public void setNetworkId(UUID networkId) {
        this.networkId = networkId;
    }
}
