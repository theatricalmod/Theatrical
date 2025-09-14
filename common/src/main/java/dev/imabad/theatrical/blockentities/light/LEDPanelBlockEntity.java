package dev.imabad.theatrical.blockentities.light;

import dev.imabad.theatrical.api.Fixture;
import dev.imabad.theatrical.blockentities.BlockEntities;
import dev.imabad.theatrical.fixtures.Fixtures;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Arrays;

public class LEDPanelBlockEntity extends BaseDMXConsumerLightBlockEntity {
    public LEDPanelBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(BlockEntities.LED_PANEL.get(), blockPos, blockState);
        setChannelCount(4);
    }

    @Override
    public Fixture getFixture() {
        return Fixtures.LED_PANEL.get();
    }

    @Override
    public void consume(byte[] dmxValues) {
        int start = this.getChannelStart() > 0 ? this.getChannelStart() - 1 : 0;
        byte[] ourValues = Arrays.copyOfRange(dmxValues, start,
                start+ this.getChannelCount());
        if(ourValues.length < 4){
            return;
        }
        if(this.storePrev()){
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
        boolean hasUpdated = false;
        int newIntensity = convertByteToInt(ourValues[0]);
        if(intensity != newIntensity){
            intensity = newIntensity;
            hasUpdated = true;
        }
        int newRed = convertByteToInt(ourValues[1]);
        if(red != newRed) {
            red = newRed;
            hasUpdated = true;
        }
        int newGreen = convertByteToInt(ourValues[2]);
        if(green != newGreen){
            green = newGreen;
            hasUpdated = true;
        }
        int newBlue = convertByteToInt(ourValues[3]);
        if(blue != newBlue){
            blue = newBlue;
            hasUpdated = true;
        }
        if(hasUpdated) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
            setChanged();
        }
    }

    @Override
    public int getDeviceTypeId() {
        return 0x03;
    }

    @Override
    public String getModelName() {
        return "LED Panel";
    }

    @Override
    public ResourceLocation getFixtureId() {
        return Fixtures.LED_PANEL.getId();
    }

    @Override
    public int getActivePersonality() {
        return 0;
    }

    public int convertByteToInt(byte val) {
        return Byte.toUnsignedInt(val);
    }

    @Override
    public float getMaxLightDistance() {
        return 1;
    }

    @Override
    public String getTranslationKey() {
        return "block.theatrical.led_panel";
    }
}
