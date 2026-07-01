package dev.imabad.theatrical.networks;

import dev.imabad.theatrical.api.dmx.DMXConsumer;
import dev.imabad.theatrical.util.DmxPacketGuard;
import net.minecraft.server.MinecraftServer;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Coalesces incoming Art-Net / DMX frames to one apply pass per server tick per universe.
 * Prevents main-thread stalls when clients relay ~40 Hz Art-Net packets.
 */
public final class ServerDmxBroker {

    private record UniverseKey(UUID networkId, int universe) {}

    private static final Map<UniverseKey, byte[]> pending = new HashMap<>();
    private static final Map<UniverseKey, byte[]> lastApplied = new HashMap<>();

    private ServerDmxBroker() {
    }

    public static void queue(UUID networkId, int universe, byte[] data) {
        if (data == null || data.length == 0) {
            return;
        }
        int length = Math.min(data.length, DmxPacketGuard.DMX_CHANNELS_PER_UNIVERSE);
        pending.put(new UniverseKey(networkId, universe), Arrays.copyOf(data, length));
    }

    public static void flush(MinecraftServer server) {
        if (pending.isEmpty() || server == null) {
            return;
        }
        Map<UniverseKey, byte[]> snapshot = new HashMap<>(pending);
        pending.clear();

        TheatricalNetworkData networkData = TheatricalNetworkData.getInstance(server.overworld());
        for (Map.Entry<UniverseKey, byte[]> entry : snapshot.entrySet()) {
            UniverseKey key = entry.getKey();
            TheatricalNetwork network = networkData.getNetwork(key.networkId());
            if (network == null) {
                continue;
            }
            byte[] normalized = DmxPacketGuard.normalizeDmxBuffer(entry.getValue());
            byte[] previous = lastApplied.get(key);
            if (previous != null && Arrays.equals(previous, normalized)) {
                continue;
            }
            lastApplied.put(key, normalized);

            Collection<DMXConsumer> consumers = network.dmx().getConsumers(key.universe());
            if (consumers == null || consumers.isEmpty()) {
                continue;
            }
            for (DMXConsumer consumer : consumers) {
                consumer.consume(normalized);
            }
        }
    }

    public static void clearNetwork(UUID networkId) {
        pending.keySet().removeIf(key -> key.networkId().equals(networkId));
        lastApplied.keySet().removeIf(key -> key.networkId().equals(networkId));
    }
}
