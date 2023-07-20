package dev.imabad.theatrical.dmx;

import ch.bildspur.artnet.ArtNetBuffer;
import ch.bildspur.artnet.ArtNetClient;
import ch.bildspur.artnet.ArtNetException;
import ch.bildspur.artnet.events.ArtNetServerEventAdapter;
import ch.bildspur.artnet.packets.ArtDmxPacket;
import ch.bildspur.artnet.packets.ArtNetPacket;

import java.net.InetAddress;
import java.net.SocketException;

import static ch.bildspur.artnet.packets.PacketType.ART_OUTPUT;

public class TheatricalArtNetClient extends ArtNetClient {

    private boolean isRunning = false;
    private long lastPacketMS = 0;

    public TheatricalArtNetClient() {
        super();
    }

    public TheatricalArtNetClient(ArtNetBuffer inputBuffer) {
        super(inputBuffer);
    }

    public TheatricalArtNetClient(ArtNetBuffer inputBuffer, int serverPort, int clientPort) {
        super(inputBuffer, serverPort, clientPort);
    }

    private void onPacketReceived(final ArtNetPacket packet) {
        // only store input data if buffer is created
        if (getInputBuffer() == null)
            return;

        if (packet.getType() != ART_OUTPUT)
            return;

        ArtDmxPacket dmxPacket = (ArtDmxPacket) packet;
        int subnet = dmxPacket.getSubnetID();
        int universe = dmxPacket.getUniverseID();
        lastPacketMS = System.currentTimeMillis();

        getInputBuffer().setDmxData((short) subnet, (short) universe, dmxPacket.getDmxData());
    }

    public boolean hasReceivedPacket(){
        return this.lastPacketMS > 0;
    }

    public long getLastPacketMS(){
        return lastPacketMS;
    }

    public void start(InetAddress networkInterfaceAddress) {
        if (isRunning)
            return;

        // reset buffer if present
        if (getInputBuffer() != null)
            getInputBuffer().clear();

        try {
            getArtNetServer().addListener(
                    new ArtNetServerEventAdapter() {
                        @Override
                        public void artNetPacketReceived(ArtNetPacket packet) {
                            onPacketReceived(packet);
                        }
                    });

            getArtNetServer().start(networkInterfaceAddress);

            isRunning = true;
        } catch (SocketException | ArtNetException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isRunning() {
        return this.isRunning;
    }

    @Override
    public void stop() {
        if (!isRunning)
            return;

        getArtNetServer().stop();

        isRunning = false;
    }
}
