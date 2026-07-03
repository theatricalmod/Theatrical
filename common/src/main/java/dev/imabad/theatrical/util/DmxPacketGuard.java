package dev.imabad.theatrical.util;

import dev.imabad.theatrical.blockentities.control.BasicLightingDeskBlockEntity;
import dev.imabad.theatrical.config.TheatricalConfig;
import dev.imabad.theatrical.networks.TheatricalNetwork;
import dev.imabad.theatrical.networks.TheatricalNetworkData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.UUID;

public final class DmxPacketGuard {

    public static final int DMX_CHANNELS_PER_UNIVERSE = 512;
    private static final int CONSOLE_FADER_COUNT = 12;

    private DmxPacketGuard() {
    }

    public static boolean isValidAddress(int address) {
        return address >= 1 && address <= DMX_CHANNELS_PER_UNIVERSE;
    }

    public static boolean isValidUniverse(int universe) {
        return universe >= 0;
    }

    public static boolean fitsInUniverse(int address, int footprint) {
        return isValidAddress(address) && address + footprint - 1 <= DMX_CHANNELS_PER_UNIVERSE;
    }

    public static boolean canReach(ServerPlayer player, BlockPos pos) {
        double radius = TheatricalConfig.INSTANCE.COMMON.wirelessDMXRadius;
        return player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= radius * radius;
    }

    public static boolean canConfigure(ServerPlayer player, TheatricalNetwork network) {
        return network.members().isMember(player.getUUID());
    }

    public static boolean canSendDmx(ServerPlayer player, TheatricalNetwork network) {
        return network.members().canSendDMX(player.getUUID());
    }

    @Nullable
    public static TheatricalNetwork resolveNetwork(ServerLevel level, UUID networkId) {
        if (level.getServer() == null) {
            return null;
        }
        return TheatricalNetworkData.getInstance(level.getServer().overworld()).getNetwork(networkId);
    }

    public static byte[] normalizeDmxBuffer(byte[] data) {
        if (data == null) {
            return new byte[0];
        }
        if (data.length <= DMX_CHANNELS_PER_UNIVERSE) {
            return data;
        }
        return Arrays.copyOf(data, DMX_CHANNELS_PER_UNIVERSE);
    }

    public static byte[] sliceDmxChannels(byte[] dmxValues, int channelStart, int channelCount) {
        if (dmxValues == null || channelCount <= 0) {
            return new byte[0];
        }
        int start = channelStart > 0 ? channelStart - 1 : 0;
        if (start >= dmxValues.length) {
            return new byte[0];
        }
        int end = Math.min(start + channelCount, dmxValues.length);
        return Arrays.copyOfRange(dmxValues, start, end);
    }

    public static boolean canControlConsole(ServerPlayer player, BasicLightingDeskBlockEntity desk) {
        if (!canReach(player, desk.getBlockPos())) {
            return false;
        }
        UUID networkId = desk.getNetworkId();
        if (networkId.equals(UUIDUtil.NULL)) {
            return true;
        }
        TheatricalNetwork network = resolveNetwork((ServerLevel) player.level(), networkId);
        return network != null && canSendDmx(player, network);
    }

    public static boolean isValidFaderIndex(int fader) {
        return fader >= 0 && fader < CONSOLE_FADER_COUNT;
    }

    public static boolean isValidFaderValue(int value) {
        return value >= 0 && value <= 255;
    }

    public static int clampPanTilt(int value) {
        return Math.max(-360, Math.min(360, value));
    }

    public static int clampNonNegative(int value) {
        return Math.max(0, value);
    }
}
