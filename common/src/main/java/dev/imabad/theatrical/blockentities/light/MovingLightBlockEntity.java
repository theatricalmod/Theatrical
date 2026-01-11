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
import java.util.Optional;

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
    public void consume(byte[] dmxValues, boolean mapped) {
        if(!mapped) {
            int start = this.getChannelStart() > 0 ? this.getChannelStart() - 1 : 0;
            dmxValues = Arrays.copyOfRange(dmxValues, start,
                    start + this.getChannelCount());
        }
        if (dmxValues.length < 7) {
            return;
        }
        if(this.storePrev() && this.hasLevel() && !this.level.isClientSide){
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
        boolean hasUpdated = false;
        int newIntensity = convertByteToInt(dmxValues[0]);
        if(intensity != newIntensity){
            intensity = newIntensity;
            hasUpdated = true;
        }
        int newRed = convertByteToInt(dmxValues[1]);
        if(red != newRed) {
            red = newRed;
            hasUpdated = true;
        }
        int newGreen = convertByteToInt(dmxValues[2]);
        if(green != newGreen){
            green = newGreen;
            hasUpdated = true;
        }
        int newBlue = convertByteToInt(dmxValues[3]);
        if(blue != newBlue){
            blue = newBlue;
            hasUpdated = true;
        }
        int newFocus = convertByteToInt(dmxValues[4]);
        if(focus != newFocus){
            focus = newFocus;
            hasUpdated = true;
        }
        int newPan = (int) ((convertByteToInt(dmxValues[5]) * 360) / 255f) - 180;
        if(pan != newPan){
            pan = newPan;
            hasUpdated = true;
        }
        int newTilt = (int) ((convertByteToInt(dmxValues[6]) * 270) / 255F) - 225;
        if(tilt != newTilt){
            tilt = newTilt;
            hasUpdated = true;
        }
        if(hasUpdated && this.hasLevel() && !this.level.isClientSide) {
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

    @Override
    public String getTranslationKey() {
        return "block.theatrical.moving_light";
    }

}
