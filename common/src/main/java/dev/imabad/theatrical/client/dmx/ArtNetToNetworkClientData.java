package dev.imabad.theatrical.client.dmx;

import dev.imabad.theatrical.util.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.UUID;

public class ArtNetToNetworkClientData extends SavedData {

    private static ArtNetToNetworkClientData INSTANCE;
    private static final String KEY = "artnet_network_map";

    public static void unload(){
        INSTANCE = null;
    }

    private static final SavedData.Factory<ArtNetToNetworkClientData> factory = new Factory<>(
            ArtNetToNetworkClientData::new,
            ArtNetToNetworkClientData::read,
            null
    );
    public static ArtNetToNetworkClientData getInstance(Level level){
        if(INSTANCE == null){
            INSTANCE = level.getServer()
                    .overworld().getDataStorage().computeIfAbsent(factory, KEY);
        }
        return INSTANCE;
    }

    public static ArtNetToNetworkClientData read(CompoundTag tag) {
        ArtNetToNetworkClientData data = new ArtNetToNetworkClientData();
        if(tag.contains("networkId")) {
            data.setNetworkId(tag.getUUID("networkId"));
        }
        return data;
    }


    private UUID networkId = UUIDUtil.NULL;

    public UUID getNetworkId() {
        return networkId;
    }

    public void setNetworkId(UUID networkId) {
        this.networkId = networkId;
        setDirty(true);
    }

    @Override
    public CompoundTag save(CompoundTag compoundTag) {
        compoundTag.putUUID("networkId", networkId);
        return compoundTag;
    }
}
