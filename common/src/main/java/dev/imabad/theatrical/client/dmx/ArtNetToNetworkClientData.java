package dev.imabad.theatrical.client.dmx;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.imabad.theatrical.networks.TheatricalNetwork;
import dev.imabad.theatrical.networks.TheatricalNetworkData;
import dev.imabad.theatrical.util.UUIDUtil;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.UUID;

public class ArtNetToNetworkClientData extends SavedData {

    private static ArtNetToNetworkClientData INSTANCE;
    private static final String KEY = "artnet_network_map";
    public static final Codec<ArtNetToNetworkClientData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            net.minecraft.core.UUIDUtil.CODEC.fieldOf("networkId")
                    .forGetter(ArtNetToNetworkClientData::getNetworkId)
    ).apply(instance, ArtNetToNetworkClientData::new));

    public static final SavedDataType<ArtNetToNetworkClientData> TYPE = new SavedDataType<>(
            KEY,
            ArtNetToNetworkClientData::new,
            ArtNetToNetworkClientData.CODEC,
            null
    );

    public static void unload(){
        INSTANCE = null;
    }

    public static ArtNetToNetworkClientData getInstance(Level level){
        if(INSTANCE == null){
            INSTANCE = level.getServer()
                    .overworld().getDataStorage().computeIfAbsent(TYPE);
        }
        return INSTANCE;
    }

    public ArtNetToNetworkClientData(UUID networkId) {
        this.networkId = networkId;
    }

    public ArtNetToNetworkClientData() {
    }

    private UUID networkId = UUIDUtil.NULL;

    public UUID getNetworkId() {
        return networkId;
    }

    public void setNetworkId(UUID networkId) {
        this.networkId = networkId;
        setDirty(true);
    }
}
