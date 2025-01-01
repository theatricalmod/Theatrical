package dev.imabad.theatrical.networks.handlers;

import dev.imabad.theatrical.api.network.audio.AudioNetworkDevice;
import dev.imabad.theatrical.util.DimensionBlockPos;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class AVNetworkAudioHandler {

    private final Map<DimensionBlockPos, AudioNetworkDevice> devices = new HashMap<>();

    public void addDevice(DimensionBlockPos pos, AudioNetworkDevice device) {
        devices.put(pos, device);
    }

    public void removeDevice(DimensionBlockPos pos) {
        devices.remove(pos);
    }

    public AudioNetworkDevice getDevice(DimensionBlockPos pos) {
        return devices.get(pos);
    }

    public Set<DimensionBlockPos> getDevices(){
        return devices.keySet();
    }
}
