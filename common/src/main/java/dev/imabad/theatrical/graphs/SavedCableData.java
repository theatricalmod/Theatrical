package dev.imabad.theatrical.graphs;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SavedCableData extends SavedData {

    private Map<UUID, CableNetwork> cableNetworks = new HashMap<>();

    @Override
    public CompoundTag save(CompoundTag compoundTag) {
        ListTag networks = new ListTag();
        cableNetworks.forEach((id, network) -> {
            networks.add(network.write());
        });
        compoundTag.put("CableNetworks", networks);
        return compoundTag;
    }

    private static SavedCableData load(CompoundTag savedData){
        SavedCableData savedCableData = new SavedCableData();
        savedCableData.cableNetworks = new HashMap<>();

        savedData.getList("CableNetworks", Tag.TAG_COMPOUND).forEach((tag) -> {
            CableNetwork network = CableNetwork.load((CompoundTag) tag);
            savedCableData.cableNetworks.put(network.getId(), network);
        });
        return savedCableData;
    }

    public Map<UUID, CableNetwork> getCableNetworks() {
        return cableNetworks;
    }

    public static SavedCableData load(MinecraftServer server){
        return server.overworld()
                .getDataStorage()
                .computeIfAbsent(SavedCableData::load, SavedCableData::new, "theatrical_cables");
    }
}
