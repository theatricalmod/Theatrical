package dev.imabad.theatrical.blockentities.light;

import dev.imabad.theatrical.api.Fixture;
import dev.imabad.theatrical.blockentities.BlockEntities;
import dev.imabad.theatrical.blocks.light.MovingLightBlock;
import dev.imabad.theatrical.fixtures.Fixtures;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Arrays;

public class MovingLightBlockEntity extends BaseDMXConsumerLightBlockEntity {
    public MovingLightBlockEntity(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
        setChannelCount(7);
    }

    public MovingLightBlockEntity(BlockPos pos, BlockState state) {
        this(BlockEntities.MOVING_LIGHT.get(), pos, state);
    }
    @Override
    public Fixture getFixture() {
        return Fixtures.MOVING_LIGHT.get();
    }

    @Override
    public void consume(byte[] dmxValues) {
        int start = this.getChannelStart() > 0 ? this.getChannelStart() - 1 : 0;
        byte[] ourValues = Arrays.copyOfRange(dmxValues, start,
                start+ this.getChannelCount());
        if(ourValues.length < 7){
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
        int newFocus = convertByteToInt(ourValues[4]);
        if(focus != newFocus){
            focus = newFocus;
            hasUpdated = true;
        }
        int newPan = (int) ((convertByteToInt(ourValues[5]) * 360) / 255f) - 180;
        if(pan != newPan){
            pan = newPan;
            hasUpdated = true;
        }
        int newTilt = (int) ((convertByteToInt(ourValues[6]) * 180) / 255F) - 180;
        if(tilt != newTilt){
            tilt = newTilt;
            hasUpdated = true;
        }
        if(hasUpdated) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
            setChanged();
        }
    }

    @Override
    public int getDeviceTypeId() {
        return 0x01;
    }

    @Override
    public String getModelName() {
        return "Moving Head";
    }

    @Override
    public ResourceLocation getFixtureId() {
        return Fixtures.MOVING_LIGHT.getId();
    }

    @Override
    public int getActivePersonality() {
        return 0;
    }

    public int convertByteToInt(byte val) {
        return Byte.toUnsignedInt(val);
    }
    @Override
    public boolean isUpsideDown() {
        return getBlockState().getValue(MovingLightBlock.HANGING) && getBlockState().getValue(MovingLightBlock.HANG_DIRECTION) == Direction.UP;
    }

    @Override
    public int getBasePan() {
        return 0;
    }
}
