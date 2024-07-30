package dev.imabad.theatrical.dmx;

import ch.bildspur.artnet.rdm.RDMDeviceId;
import net.minecraft.resources.ResourceLocation;

public class DMXDevice {

    private RDMDeviceId deviceId;
    private int dmxStartAddress, dmxChannelCount, deviceTypeId, activePersonality;
    private String modelName;
    private ResourceLocation fixtureID;

    public DMXDevice(RDMDeviceId deviceId, int dmxStartAddress, int dmxChannelCount, int deviceTypeId, int activePersonality, String modelName, ResourceLocation fixtureID) {
        this.deviceId = deviceId;
        this.dmxStartAddress = dmxStartAddress;
        this.dmxChannelCount = dmxChannelCount;
        this.deviceTypeId = deviceTypeId;
        this.activePersonality = activePersonality;
        this.modelName = modelName;
        this.fixtureID = fixtureID;
    }

    public int getDmxStartAddress() {
        return dmxStartAddress;
    }

    public int getDmxChannelCount() {
        return dmxChannelCount;
    }

    public void setDmxStartAddress(int dmxStartAddress) {
        this.dmxStartAddress = dmxStartAddress;
    }

    public void setDmxChannelCount(int dmxChannelCount) {
        this.dmxChannelCount = dmxChannelCount;
    }

    public int getDeviceTypeId() {
        return deviceTypeId;
    }

    public void setDeviceTypeId(int deviceTypeId) {
        this.deviceTypeId = deviceTypeId;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public int getActivePersonality() {
        return activePersonality;
    }

    public ResourceLocation getFixtureID() {
        return fixtureID;
    }

    public void setActivePersonality(int activePersonality) {
        this.activePersonality = activePersonality;
    }

    public void setFixtureID(ResourceLocation fixtureID) {
        this.fixtureID = fixtureID;
    }

    public RDMDeviceId getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(RDMDeviceId deviceId) {
        this.deviceId = deviceId;
    }
}
