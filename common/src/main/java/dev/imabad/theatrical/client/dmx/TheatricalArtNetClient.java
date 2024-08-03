package dev.imabad.theatrical.client.dmx;

import ch.bildspur.artnet.*;
import ch.bildspur.artnet.events.ArtNetServerEventAdapter;
import ch.bildspur.artnet.packets.*;
import ch.bildspur.artnet.rdm.RDMCommandClass;
import ch.bildspur.artnet.rdm.RDMDeviceId;
import ch.bildspur.artnet.rdm.RDMPacket;
import ch.bildspur.artnet.rdm.RDMParameter;
import dev.imabad.theatrical.Constants;
import dev.imabad.theatrical.Theatrical;
import dev.imabad.theatrical.TheatricalClient;
import dev.imabad.theatrical.TheatricalExpectPlatform;
import dev.imabad.theatrical.api.Fixture;
import dev.imabad.theatrical.api.dmx.DMXPersonality;
import dev.imabad.theatrical.api.dmx.DMXSlot;
import dev.imabad.theatrical.config.TheatricalConfig;
import dev.imabad.theatrical.config.UniverseConfig;
import dev.imabad.theatrical.dmx.DMXDevice;
import dev.imabad.theatrical.fixtures.Fixtures;
import dev.imabad.theatrical.net.artnet.RDMUpdateConsumer;
import dev.imabad.theatrical.net.artnet.RequestConsumers;
import dev.imabad.theatrical.net.artnet.SendArtNetData;
import dev.imabad.theatrical.util.ByteUtils;
import io.netty.util.collection.IntObjectHashMap;
import io.netty.util.collection.IntObjectMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import org.jetbrains.annotations.Nullable;

import java.net.InetAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.stream.Collectors;

public class TheatricalArtNetClient extends ArtNetClient {
    private ArtNetManager manager;
    private InetAddress address;

    private final byte[] RDM_DEVICE_ID;
    private boolean isRunning = false;
    private long lastPacketMS = 0;
    private IntObjectMap<Map<RDMDeviceId, DMXDevice>> proxiedDevices;
    private boolean listChanged = false;
    private Queue<RDMPacket> queuedMessages;

    public TheatricalArtNetClient(InetAddress address, ArtNetManager manager) {
        super();
        this.manager = manager;
        this.address = address;
        RDM_DEVICE_ID = buildDeviceId();
        proxiedDevices = new IntObjectHashMap<>();
        queuedMessages = new ArrayDeque<>();
//        universes = TheatricalConfig.INSTANCE.CLIENT.universes.values().stream().filter((i) -> i.enabled).map(universeConfig -> universeConfig.getUniverse()).collect(Collectors.toList());
//        universes = new int[]{TheatricalConfig.INSTANCE.CLIENT.universe1,TheatricalConfig.INSTANCE.CLIENT.universe2,TheatricalConfig.INSTANCE.CLIENT.universe3,TheatricalConfig.INSTANCE.CLIENT.universe4};
        for (Map.Entry<Integer, UniverseConfig> integerUniverseConfigEntry : TheatricalConfig.INSTANCE.CLIENT.universes.entrySet()) {
            if(integerUniverseConfigEntry.getValue().enabled){
                new RequestConsumers(manager.getNetworkId(), integerUniverseConfigEntry.getKey()).sendToServer();
            }
        }
    }

    public void networkChange(){
        proxiedDevices.clear();
        queuedMessages.clear();
        for (Map.Entry<Integer, UniverseConfig> integerUniverseConfigEntry : TheatricalConfig.INSTANCE.CLIENT.universes.entrySet()) {
            if(integerUniverseConfigEntry.getValue().enabled){
                new RequestConsumers(manager.getNetworkId(), integerUniverseConfigEntry.getKey()).sendToServer();
            }
        }
    }

    public TheatricalArtNetClient(ArtNetBuffer inputBuffer) {
        super(inputBuffer);
        RDM_DEVICE_ID = buildDeviceId();
    }

    public TheatricalArtNetClient(ArtNetBuffer inputBuffer, int serverPort, int clientPort) {
        super(inputBuffer, serverPort, clientPort);
        RDM_DEVICE_ID = buildDeviceId();
    }

    private byte[] buildDeviceId(){
        byte[] deviceID =  Arrays.copyOfRange(ByteUtils.longToBytes(Minecraft.getInstance().getUser().getGameProfile().getId().getLeastSignificantBits()), 0, 4);
        ByteBuffer wrap = ByteBuffer.wrap(new byte[6]);
        wrap.putShort(Constants.MANUFACTURER_ID);
        wrap.put(deviceID);
        return wrap.array();
    }

    public boolean isSubscribedTo(int subnet, int universe){
        return TheatricalConfig.INSTANCE.CLIENT.universes.values()
                .stream().anyMatch(x -> x.enabled && x.subnet == subnet && x.universe == universe);
    }

    public int getNetworkUniverse(int subnet, int universe){
        Optional<Map.Entry<Integer, UniverseConfig>> first = TheatricalConfig.INSTANCE.CLIENT.universes.entrySet()
                .stream().filter(x -> x.getValue().enabled && x.getValue().subnet == subnet && x.getValue().universe == universe).findFirst();
        if(first.isPresent()){
            return first.get().getKey();
        }
        return -1;
    }

    public void refreshSubscriptions(){
        buildAndSetPollReply();
    }

    private Map<RDMDeviceId, DMXDevice> getProxyMap(short subnet, short universe){
        int key = hashKeyFromPair(subnet, universe);
        if(!proxiedDevices.containsKey(key)){
            proxiedDevices.put(key, new HashMap<>());
        }
        return proxiedDevices.get(key);
    }

    public void addDevice(short subnet, short universe, RDMDeviceId deviceId, DMXDevice dmxDevice){
        if(getProxyMap(subnet, universe).containsKey(deviceId)){
            return;
        }
        getProxyMap(subnet, universe).put(deviceId, dmxDevice);
        listChanged = true;
        sendTOD(subnet, universe);
        //queueProxiedDevicesUpdate();
    }
    public void updateDevice(short subnet, short universe, RDMDeviceId deviceId, DMXDevice dmxDevice){
        getProxyMap(subnet, universe).put(deviceId, dmxDevice);
    }

    public void clearDevices(){
        this.proxiedDevices.clear();
    }

    public void removeDevice(short subnet, short universe, RDMDeviceId deviceId){
        if(!getProxyMap(subnet, universe).containsKey(deviceId)){
            return;
        }
        getProxyMap(subnet, universe).remove(deviceId);
        listChanged = true;
        sendTOD(subnet, universe);
//        queueProxiedDevicesUpdate();
    }

    private void queueProxiedDevicesUpdate(){
//        RDMPacket getCommandResponse = new RDMPacket();
//        getCommandResponse.setParameter(RDMParameter.PROXIED_DEVICES_COUNT);
//        getCommandResponse.setParameterDataLength(0x03);
//        ch.bildspur.artnet.packets.ByteUtils byteBuffer = new ch.bildspur.artnet.packets.ByteUtils(new byte[0x03]);
//        byteBuffer.setInt16(proxiedDevices.size() & 0xFFFF, 0);
//        byteBuffer.setInt8(listChanged ? 1 : 0, 2);
//        getCommandResponse.setParameterData(byteBuffer.getBytes());
//        queuedMessages.add(getCommandResponse);
    }

    private void sendTOD(short subnet, short universe){
        ArtTodDataPacket replyPacket = new ArtTodDataPacket();
        replyPacket.setTotalDevices(proxiedDevices.size() + 1);
        replyPacket.setUniverse(subnet, universe);
        for (RDMDeviceId proxiedDevice : getProxyMap(subnet, universe).keySet()) {
            replyPacket.addDevice(proxiedDevice.toBytes());
        }
        getArtNetServer().broadcastPacket(replyPacket);
    }


    public static int hashKeyFromPair(short a, short b) {
        assert a >= 0;
        assert b >= 0;
        long sum = (long) a + (long) b;
        return (int) (sum * (sum + 1) / 2) + a;
    }

    private void onPacketReceived(InetAddress sourceAddress, final ArtNetPacket packet) {
        switch(packet.getType()){
            case ART_OUTPUT: {
                if (getInputBuffer() == null)
                    return;

                ArtDmxPacket dmxPacket = (ArtDmxPacket) packet;
                int subnet = dmxPacket.getSubnetID();
                int universe = dmxPacket.getUniverseID();
                lastPacketMS = System.currentTimeMillis();
                getInputBuffer().setDmxData((short) subnet, (short) universe, dmxPacket.getDmxData());
                int networkUniverse = getNetworkUniverse(subnet, universe);
                if(networkUniverse != -1){
                    new SendArtNetData(manager.getNetworkId(), networkUniverse, dmxPacket.getDmxData()).sendToServer();
                }
                break;
            }
            case ART_TOD_REQUEST: {
                ArtTodRequestPacket requestPacket = (ArtTodRequestPacket) packet;
                sendTOD((short) requestPacket.getSubnetID(), (short)  requestPacket.getUniverseID());
                break;
            }
            case ART_TOD_CONTROL: {
                ArtTodControlPacket controlPacket = (ArtTodControlPacket) packet;
                if (controlPacket.isFlush()) {
                    sendTOD((short) controlPacket.getSubnetID(), (short)  controlPacket.getUniverseID());
                }
                break;
            }
            case ART_RDM:
                ArtRdmPacket artRdmPacket = (ArtRdmPacket) packet;
                RDMPacket rdmPacket = artRdmPacket.getRdmPacket();
                short universe = getUniverseFromPortAddress(artRdmPacket.getAddress());
                short subnet = getSubnetFromPortAddress(artRdmPacket.getAddress());
                RDMDeviceId destinationID = new RDMDeviceId(rdmPacket.getDestinationID());
                if(!getProxyMap(subnet, universe).containsKey(destinationID) && !Arrays.equals(rdmPacket.getDestinationID(), RDM_DEVICE_ID)){
                    return;
                }
                if(rdmPacket.getCommandClass() != null && rdmPacket.getParameter() != null) {
                    switch (rdmPacket.getCommandClass()) {
                        case GET_COMMAND:
                        {
                            RDMPacket getCommandResponse = new RDMPacket();
                            getCommandResponse.setDestinationID(rdmPacket.getSourceID());
                            getCommandResponse.setSourceID(rdmPacket.getDestinationID());
                            getCommandResponse.setTransactionID(rdmPacket.getTransactionID());
                            getCommandResponse.setPortID(0x00);
                            getCommandResponse.setMessageCount(queuedMessages.size());
                            getCommandResponse.setSubDevice((short) 0);
                            getCommandResponse.setCommandClass(RDMCommandClass.GET_COMMAND_RESPONSE);
                            switch (rdmPacket.getParameter()) {
                                case SUPPORTED_PARAMETERS:
                                    getCommandResponse.setParameter(RDMParameter.SUPPORTED_PARAMETERS);
                                    RDMParameter[] supportedParameters = RDMParameter.values();
                                    int dataLength = supportedParameters.length * 2;
                                    ByteBuffer supportedParameterIDs = ByteBuffer.wrap(new byte[dataLength]);
                                    for (RDMParameter supportedParameter : supportedParameters) {
                                        supportedParameterIDs.putShort((short) supportedParameter.getId());
                                    }
                                    getCommandResponse.setParameterDataLength(dataLength);
                                    getCommandResponse.setParameterData(supportedParameterIDs.array());
                                    break;
                                case DEVICE_INFO:
                                    getCommandResponse.setParameter(RDMParameter.DEVICE_INFO);
                                    ch.bildspur.artnet.packets.ByteUtils deviceInfoData = new ch.bildspur.artnet.packets.ByteUtils(new byte[0x13]);
                                    deviceInfoData.setInt8(0x01, 0);
                                    deviceInfoData.setInt8(0x00, 1);
                                    if (!Arrays.equals(rdmPacket.getDestinationID(), RDM_DEVICE_ID)) {
                                        DMXDevice dmxDevice = getProxyMap(subnet, universe).get(destinationID);
                                        Fixture fixture = Fixtures.FIXTURES.get(dmxDevice.getFixtureID());
                                        deviceInfoData.setInt16(dmxDevice.getDeviceTypeId(), 2);
                                        deviceInfoData.setInt16(0x01 >> 8, 4);
                                        deviceInfoData.setInt16(dmxDevice.getDmxChannelCount(), 10);
                                        deviceInfoData.setInt8(dmxDevice.getActivePersonality() + 1, 12);
                                        deviceInfoData.setInt8(fixture.getDMXPersonalities().size(), 13);
                                        deviceInfoData.setInt16(dmxDevice.getDmxStartAddress(), 14);
                                    } else {
                                        deviceInfoData.setInt16(0x0, 2);
                                        deviceInfoData.setInt16(0x08 >> 8, 4);
                                        deviceInfoData.setInt16(0, 10);
                                        deviceInfoData.setInt16(0xFFFF, 14);
                                    }
                                    ByteBuffer versionId = ByteBuffer.wrap(new byte[4]);
                                    versionId.putInt(1);
                                    deviceInfoData.setByteChunk(versionId.array(), 6);
                                    deviceInfoData.setInt16(0, 16);
                                    deviceInfoData.setInt8(0, 18);
                                    getCommandResponse.setParameterDataLength(0x13);
                                    getCommandResponse.setParameterData(deviceInfoData.getBytes());
                                    break;
                                case DMX_START_ADDRESS: {
                                    getCommandResponse.setParameter(RDMParameter.DMX_START_ADDRESS);
                                    ch.bildspur.artnet.packets.ByteUtils dmxStartAddress = new ch.bildspur.artnet.packets.ByteUtils(new byte[0x02]);
                                    if (!Arrays.equals(rdmPacket.getDestinationID(), RDM_DEVICE_ID)) {
                                        DMXDevice dmxDevice = getProxyMap(subnet, universe).get(destinationID);
                                        dmxStartAddress.setInt16(dmxDevice.getDmxStartAddress(), 0);
                                    } else {
                                        dmxStartAddress.setInt16(0, 0);
                                    }
                                    getCommandResponse.setParameterDataLength(dmxStartAddress.length);
                                    getCommandResponse.setParameterData(dmxStartAddress.getBytes());
                                    break;
                                }
                                case SOFTWARE_VERSION_LABEL:
                                    getCommandResponse.setParameter(RDMParameter.SOFTWARE_VERSION_LABEL);
                                    String modVersion = TheatricalExpectPlatform.getModVersion();
                                    if (modVersion.length() > 32) {
                                        modVersion = modVersion.substring(0, 32);
                                    }
                                    byte[] modVersionBytes = modVersion.getBytes();
                                    getCommandResponse.setParameterDataLength(modVersionBytes.length);
                                    getCommandResponse.setParameterData(modVersionBytes);
                                    break;
                                case IDENTIFY_DEVICE:
                                    getCommandResponse.setParameter(RDMParameter.IDENTIFY_DEVICE);
                                    getCommandResponse.setParameterDataLength(0);
                                    getCommandResponse.setParameterData(new byte[0]);
                                    break;
                                case MANUFACTURER_LABEL:
                                    getCommandResponse.setParameter(RDMParameter.MANUFACTURER_LABEL);
                                    byte[] manufacturerLabelBytes = "Theatrical".getBytes();
                                    getCommandResponse.setParameterDataLength(manufacturerLabelBytes.length);
                                    getCommandResponse.setParameterData(manufacturerLabelBytes);
                                    break;
                                case DEVICE_MODEL_DESCRIPTION:
                                    getCommandResponse.setParameter(RDMParameter.DEVICE_MODEL_DESCRIPTION);
                                    String deviceModel = "";
                                    if (!Arrays.equals(rdmPacket.getDestinationID(), RDM_DEVICE_ID)) {
                                        DMXDevice dmxDevice = getProxyMap(subnet, universe).get(destinationID);
                                        deviceModel = dmxDevice.getModelName();
                                    }
                                    if (deviceModel.length() > 32) {
                                        deviceModel = deviceModel.substring(0, 32);
                                    }
                                    byte[] deviceModelBytes = deviceModel.getBytes();
                                    getCommandResponse.setParameterDataLength(deviceModelBytes.length);
                                    getCommandResponse.setParameterData(deviceModelBytes);
                                    break;
                                case DMX_PERSONALITY: {
                                    getCommandResponse.setParameter(RDMParameter.DMX_PERSONALITY);
                                    ch.bildspur.artnet.packets.ByteUtils byteUtils = new ch.bildspur.artnet.packets.ByteUtils(new byte[2]);
                                    if (!Arrays.equals(rdmPacket.getDestinationID(), RDM_DEVICE_ID)) {
                                        DMXDevice dmxDevice = getProxyMap(subnet, universe).get(destinationID);
                                        Fixture fixture = Fixtures.FIXTURES.get(dmxDevice.getFixtureID());
                                        int activePersonality = dmxDevice.getActivePersonality() + 1;
                                        byteUtils.setInt8(activePersonality, 0);
                                        byteUtils.setInt8(fixture.getDMXPersonalities().size(), 1);
                                    }
                                    getCommandResponse.setParameterDataLength(byteUtils.length);
                                    getCommandResponse.setParameterData(byteUtils.getBytes());
                                    break;
                                }
                                case DMX_PERSONALITY_DESCRIPTION:{
                                    getCommandResponse.setParameter(RDMParameter.DMX_PERSONALITY_DESCRIPTION);
                                    String personalityText = "";
                                    int footprint = 0;
                                    if (!Arrays.equals(rdmPacket.getDestinationID(), RDM_DEVICE_ID)) {
                                        DMXDevice dmxDevice = getProxyMap(subnet, universe).get(destinationID);
                                        Fixture fixture = Fixtures.FIXTURES.get(dmxDevice.getFixtureID());
                                        personalityText = fixture.getDMXPersonalities()
                                                .get(dmxDevice.getActivePersonality()).getDescription();
                                        footprint = dmxDevice.getDmxChannelCount();
                                    }
                                    if (personalityText.length() > 32) {
                                        personalityText = personalityText.substring(0, 32);
                                    }
                                    byte[] personalityBytes = personalityText.getBytes();
                                    ch.bildspur.artnet.packets.ByteUtils byteUtils = new ch.bildspur.artnet.packets.ByteUtils(new byte[personalityBytes.length + 3]);
                                    byteUtils.setInt8(rdmPacket.getParameterData()[0], 0);
                                    byteUtils.setInt16(footprint, 1);
                                    byteUtils.setByteChunk(personalityBytes, 3);
                                    getCommandResponse.setParameterDataLength(byteUtils.length);
                                    getCommandResponse.setParameterData(byteUtils.getBytes());
                                    break;
                                }
                                case SLOT_INFO: {
                                    getCommandResponse.setParameter(RDMParameter.SLOT_INFO);
                                    ch.bildspur.artnet.packets.ByteUtils byteUtils;
                                    if(!Arrays.equals(rdmPacket.getDestinationID(), RDM_DEVICE_ID)) {
                                        DMXDevice dmxDevice = getProxyMap(subnet, universe).get(destinationID);
                                        Fixture fixture = Fixtures.FIXTURES.get(dmxDevice.getFixtureID());
                                        DMXPersonality activePersonality = fixture.getDMXPersonalities()
                                                .get(dmxDevice.getActivePersonality());
                                        byteUtils = new ch.bildspur.artnet.packets.ByteUtils(new byte[activePersonality.getSlots().size() * 5]);
                                        for (int i = 0; i < activePersonality.getSlots().size(); i++) {
                                            DMXSlot dmxSlot = activePersonality.getSlots().get(i);
                                            byteUtils.setInt16(i, (i * 5));
                                            byteUtils.setInt8(dmxSlot.slotType().getId(), (i * 5) + 2);
                                            byteUtils.setInt16(dmxSlot.slotID().getId(), (i * 5) + 3);
                                        }
                                    } else {
                                        byteUtils = new ch.bildspur.artnet.packets.ByteUtils(new byte[0]);
                                    }
                                    getCommandResponse.setParameterDataLength(byteUtils.length);
                                    getCommandResponse.setParameterData(byteUtils.getBytes());
                                    break;
                                }
                                case SLOT_DESCRIPTION: {
                                    getCommandResponse.setParameter(RDMParameter.SLOT_DESCRIPTION);
                                    ch.bildspur.artnet.packets.ByteUtils byteUtils;
                                    if(!Arrays.equals(rdmPacket.getDestinationID(), RDM_DEVICE_ID)) {
                                        DMXDevice dmxDevice = getProxyMap(subnet, universe).get(destinationID);
                                        Fixture fixture = Fixtures.FIXTURES.get(dmxDevice.getFixtureID());
                                        DMXPersonality activePersonality = fixture.getDMXPersonalities()
                                                .get(dmxDevice.getActivePersonality());
                                        int slotRequested = new ch.bildspur.artnet.packets.ByteUtils(rdmPacket.getParameterData()).getInt16(0);
                                        DMXSlot dmxSlot1 = activePersonality.getSlots().get(slotRequested);
                                        byte[] slotLabelBytes = dmxSlot1.label().getBytes();
                                        byteUtils = new ch.bildspur.artnet.packets.ByteUtils(new byte[2 + (slotLabelBytes.length)]);
                                        byteUtils.setInt16(slotRequested, 0);
                                        byteUtils.setByteChunk(slotLabelBytes, 2);
                                    } else {
                                        byteUtils = new ch.bildspur.artnet.packets.ByteUtils(new byte[0]);
                                    }
                                    getCommandResponse.setParameterDataLength(byteUtils.length);
                                    getCommandResponse.setParameterData(byteUtils.getBytes());
                                    break;
                                }
                                case DEFAULT_SLOT_VALUE: {
                                    getCommandResponse.setParameter(RDMParameter.DEFAULT_SLOT_VALUE);
                                    ch.bildspur.artnet.packets.ByteUtils byteUtils;
                                    if(!Arrays.equals(rdmPacket.getDestinationID(), RDM_DEVICE_ID)) {
                                        DMXDevice dmxDevice = getProxyMap(subnet, universe).get(destinationID);
                                        Fixture fixture = Fixtures.FIXTURES.get(dmxDevice.getFixtureID());
                                        DMXPersonality activePersonality = fixture.getDMXPersonalities()
                                                .get(dmxDevice.getActivePersonality());
                                        byteUtils = new ch.bildspur.artnet.packets.ByteUtils(new byte[activePersonality.getSlots().size() * 3]);
                                        for (int i = 0; i < activePersonality.getSlots().size(); i++) {
                                            byteUtils.setInt16(i, (i * 3));
                                            byteUtils.setInt8(0, (i * 3) + 2);
                                        }
                                    } else {
                                        byteUtils = new ch.bildspur.artnet.packets.ByteUtils(new byte[0]);
                                    }
                                    getCommandResponse.setParameterDataLength(byteUtils.length);
                                    getCommandResponse.setParameterData(byteUtils.getBytes());
                                    break;
                                }
//                                case PROXIED_DEVICES_COUNT:
//                                    getCommandResponse.setParameter(RDMParameter.PROXIED_DEVICES_COUNT);
//                                    getCommandResponse.setParameterDataLength(0x03);
//                                    ch.bildspur.artnet.packets.ByteUtils byteBuffer = new ch.bildspur.artnet.packets.ByteUtils(new byte[0x03]);
//                                    byteBuffer.setInt16(proxiedDevices.size() & 0xFFFF, 0);
//                                    byteBuffer.setInt8(listChanged ? 1 : 0, 2);
//                                    getCommandResponse.setParameterData(byteBuffer.getBytes());
//                                    listChanged = false;
//                                    break;
                                case QUEUED_MESSAGE:
                                    if(queuedMessages.isEmpty()){
                                        getCommandResponse.setParameter(RDMParameter.STATUS_MESSAGES);
                                        getCommandResponse.setParameterDataLength(0);
                                        break;
                                    }
                                    RDMPacket queuedMessage = queuedMessages.poll();
                                    getCommandResponse.setParameter(queuedMessage.getParameter());
                                    getCommandResponse.setParameterDataLength(queuedMessage.getParameterDataLength());
                                    getCommandResponse.setParameterData(queuedMessage.getParameterData());
                                    break;
                                default:
                                    return;
                            }
                            getCommandResponse.write();
                            ArtRdmPacket sendArtRdmPacket = new ArtRdmPacket();
                            sendArtRdmPacket.setRdmPacket(getCommandResponse);
                            sendArtRdmPacket.setNet(0);
                            sendArtRdmPacket.setAddress(0 << 4 | universe);
                            sendArtRdmPacket.write();
                            getArtNetServer().unicastPacket(sendArtRdmPacket, sourceAddress);
                            return;
                        }
                        case SET_COMMAND: {
                            RDMPacket setCommandResponse = new RDMPacket();
                            setCommandResponse.setDestinationID(rdmPacket.getSourceID());
                            setCommandResponse.setSourceID(rdmPacket.getDestinationID());
                            setCommandResponse.setTransactionID(rdmPacket.getTransactionID());
                            setCommandResponse.setPortID(0x00);
                            setCommandResponse.setMessageCount(queuedMessages.size());
                            setCommandResponse.setSubDevice((short) 0);
                            setCommandResponse.setCommandClass(RDMCommandClass.SET_COMMAND_RESPONSE);
                            DMXDevice targetDevice = getProxyMap(subnet, universe).get(new RDMDeviceId(rdmPacket.getDestinationID()));
                            if(targetDevice == null){
                                return;
                            }
                            switch (rdmPacket.getParameter()) {
                                case DMX_START_ADDRESS: {
                                    setCommandResponse.setParameter(RDMParameter.DMX_START_ADDRESS);
                                    ch.bildspur.artnet.packets.ByteUtils inData = new ch.bildspur.artnet.packets.ByteUtils(rdmPacket.getParameterData());
                                    int newAddress = inData.getInt16(0);
                                    new RDMUpdateConsumer(manager.getNetworkId(), universe, targetDevice.getDeviceId(), newAddress).sendToServer();
                                    break;
                                }
                                default:
                                    break;
                            }
                            setCommandResponse.write();
                            ArtRdmPacket sendArtRdmPacket = new ArtRdmPacket();
                            sendArtRdmPacket.setRdmPacket(setCommandResponse);
                            sendArtRdmPacket.setNet(0);
                            sendArtRdmPacket.setAddress(0 << 4 | universe);
                            sendArtRdmPacket.write();
                            getArtNetServer().unicastPacket(sendArtRdmPacket, sourceAddress);
                            return;
                        }
                    }
                }
                break;
        }
        // only store input data if buffer is created
    }

    private short getUniverseFromPortAddress(int portAddress)
    {
        return (short) (portAddress & 0xFF);
    }

    private short getSubnetFromPortAddress(int portAddress)
    {
        return (short) ((portAddress >> 8) & 0x0F);
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
                        public void artNetPacketReceived(InetAddress sourceAddress, ArtNetPacket packet) {
                            try {
                                onPacketReceived(sourceAddress, packet);
                            }
                            catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
            getArtNetServer().setBroadcastAddress("127.0.0.255");
            buildAndSetPollReply();
            getArtNetServer().start(networkInterfaceAddress);

            isRunning = true;
        } catch (SocketException | ArtNetException e) {
            e.printStackTrace();
        }
    }

    private void buildAndSetPollReply(){
        ArtPollReplyPacket defaultReplyPacket = new ArtPollReplyPacket();
        defaultReplyPacket.setIp(address);
        defaultReplyPacket.setLongName("Theatrical - " + Minecraft.getInstance().getUser().getGameProfile().getName());
        defaultReplyPacket.setShortName("Theatrical");
        defaultReplyPacket.setNodeStyle(NodeStyle.ST_NODE);
        defaultReplyPacket.setEstaManufacturerCode(0x7ff0);
        byte nodeStatus = (byte) 0;
        nodeStatus |= (byte) (3 << 6);
        nodeStatus |= 3 << 4;
        nodeStatus |= 0b00000010;
        defaultReplyPacket.setNodeStatus(nodeStatus);
        PortDescriptor[] ports = new PortDescriptor[4];
        int portCount = 0;
        for(int i = 0; i < 4; i++){
            ports[i] = new PortDescriptor(true, false, PortType.DMX512, (byte) 0 ,(byte) 0, 0, 0);
        }
        defaultReplyPacket.setNumPorts(portCount);
        defaultReplyPacket.setPorts(ports);
        defaultReplyPacket.setVersionInfo(1);
        defaultReplyPacket.setSubSwitch(0);
        defaultReplyPacket.setOemCode(0x0007);
        defaultReplyPacket.translateData();
        getArtNetServer().setDefaultReplyPacket(defaultReplyPacket);
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
