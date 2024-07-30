package dev.imabad.theatrical.client.dmx;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SavedClientNetworkManager {

    private static final SavedClientNetworkManager INSTANCE = new SavedClientNetworkManager();
    private static final Gson GSON = new Gson();

    public static SavedClientNetworkManager getInstance(){
        return INSTANCE;
    }

    private final Path savePath;
    private final Map<String, UUID> ipToNetworkIdMap = new HashMap<>();

    public SavedClientNetworkManager(){
        savePath = new File(Minecraft.getInstance().gameDirectory, "local/dmx_networks.json").toPath();
        if(Files.exists(savePath)){
            try (Reader reader = Files.newBufferedReader(savePath)) {
                JsonObject jsonObject = GSON.fromJson(reader, JsonObject.class);
                for (String s : jsonObject.keySet()) {
                    ipToNetworkIdMap.put(s, UUID.fromString(jsonObject.get(s).getAsString()));
                }
            } catch (Exception ignored){}
        }
    }

    public UUID getNetworkFromIP(String ip) {
        return ipToNetworkIdMap.get(ip);
    }

    private void save(){
        String json = GSON.toJson(ipToNetworkIdMap);
        try {
            Files.writeString(savePath, json);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveNetworkForIP(String ip, UUID networkId){
        ipToNetworkIdMap.put(ip, networkId);
        save();
    }

}
