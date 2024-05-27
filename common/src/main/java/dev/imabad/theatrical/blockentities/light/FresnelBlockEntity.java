package dev.imabad.theatrical.blockentities.light;

import dev.imabad.theatrical.api.Fixture;
import dev.imabad.theatrical.blockentities.BlockEntities;
import dev.imabad.theatrical.blocks.light.MovingLightBlock;
import dev.imabad.theatrical.fixtures.Fixtures;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Arrays;

public class FresnelBlockEntity extends BaseDMXConsumerLightBlockEntity {

    public FresnelBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntities.LED_FRESNEL.get(), pos, state);
        setChannelCount(4);
    }
    @Override
    public Fixture getFixture() {
        return Fixtures.LED_FRESNEL.get();
    }

    @Override
    public int getFocus() {
        return 1;
    }

    @Override
    public void consume(byte[] dmxValues) {
        byte[] ourValues = Arrays.copyOfRange(dmxValues, this.getChannelStart(),
                this.getChannelStart() + this.getChannelCount());
        if(ourValues.length < 4){
            return;
        }
        if(this.storePrev()){
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
        intensity = convertByteToInt(ourValues[0]);
        red = convertByteToInt(ourValues[1]);
        green = convertByteToInt(ourValues[2]);
        blue = convertByteToInt(ourValues[3]);
        level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
    }

    @Override
    public int getDeviceTypeId() {
        return 0x02;
    }

    @Override
    public String getModelName() {
        return "LED Fresnel";
    }

    @Override
    public ResourceLocation getFixtureId() {
        return Fixtures.LED_FRESNEL.getId();
    }

    @Override
    public int getActivePersonality() {
        return 0;
    }

    public int convertByteToInt(byte val) {
        return Byte.toUnsignedInt(val);
    }

}