package dev.imabad.theatrical.forge.compat.create;

import ch.bildspur.artnet.rdm.RDMDeviceId;
import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.content.trains.entity.CarriageContraption;
import dev.imabad.theatrical.Theatrical;
import dev.imabad.theatrical.api.dmx.BaseDMXData;
import dev.imabad.theatrical.api.dmx.DMXConsumer;
import dev.imabad.theatrical.net.compat.create.SendBEDataToContraption;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import java.util.Arrays;
import java.util.UUID;

public class DMXConsumerInContraption implements DMXConsumer {

    private BaseDMXData dmxData;
    private String modelName;
    private ResourceLocation fixtureId;
    private final BlockPos posInContraption;
    private Contraption contraption;
    private byte[] lastData;

    public DMXConsumerInContraption(CompoundTag blockEntityData,
                                    BlockPos posInContraption,
                                    Contraption contraption) {
        dmxData = BaseDMXData.fromCompoundTag(blockEntityData);
        this.posInContraption = posInContraption;
        this.contraption = contraption;
    }

    public void updateContraption(Contraption contraption) {
        if(this.contraption == contraption) return;
        this.contraption = contraption;
    }

    @Override
    public int getChannelCount() {
        return dmxData.getChannelCount();
    }

    @Override
    public int getChannelStart() {
        return dmxData.getChannelStart();
    }

    @Override
    public int getUniverse() {
        return dmxData.getUniverse();
    }

    @Override
    public void consume(byte[] dmxValues, boolean mapped) {
        int start = this.getChannelStart() > 0 ? this.getChannelStart() - 1 : 0;
        dmxValues = Arrays.copyOfRange(dmxValues, start,
                start + this.getChannelCount());
        if (dmxValues.length < dmxData.getChannelCount()) {
            return;
        }
        lastData = dmxValues;
        new SendBEDataToContraption(
                contraption.entity.getId(), posInContraption, dmxValues)
                .sendToChunkListeners(contraption.entity.level().getChunkAt(contraption.entity.blockPosition()));
    }

    @Override
    public RDMDeviceId getDeviceId() {
        return dmxData.getDeviceId();
    }

    @Override
    public int getDeviceTypeId() {
        return 0x01;
    }

    @Override
    public String getModelName() {
        return "";
    }

    @Override
    public ResourceLocation getFixtureId() {
        return new ResourceLocation(Theatrical.MOD_ID, "contraption");
    }

    @Override
    public int getActivePersonality() {
        return 0;
    }

    @Override
    public UUID getNetworkId() {
        return dmxData.getNetworkId();
    }

    @Override
    public String getTranslationKey() {
        return "";
    }

    @Override
    public void setStartAddress(int startAddress) {

    }

    @Override
    public void setNetworkId(UUID newNetworkId) {

    }

    public byte[] getLastData() {
        return lastData;
    }
}
