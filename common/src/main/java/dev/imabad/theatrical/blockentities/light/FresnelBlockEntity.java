package dev.imabad.theatrical.blockentities.light;

import dev.imabad.theatrical.api.Fixture;
import dev.imabad.theatrical.api.FocusableFixture;
import dev.imabad.theatrical.blockentities.BlockEntities;
import dev.imabad.theatrical.blocks.light.BaseLightBlock;
import dev.imabad.theatrical.fixtures.Fixtures;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public class FresnelBlockEntity extends BaseDMXConsumerLightBlockEntity implements FocusableFixture {

    private Entity trackingEntity;
    public FresnelBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntities.LED_FRESNEL.get(), pos, state);
        setChannelCount(4);
    }
    @Override
    public Fixture getFixture() {
        return Fixtures.LED_FRESNEL.get();
    }

    @Override
    public void tick() {
        if(trackingEntity == null || level.isClientSide()) return;
        double distance = Math.sqrt(trackingEntity.distanceToSqr(getOwnerPos().getX(), trackingEntity.position().y(), getOwnerPos().getZ()));
        double height = getOwnerPos().getY() - trackingEntity.getEyeY();
        double someCalc = height / distance;
        int calculatedTilt = -(int) Math.toDegrees(Math.atan(someCalc));
        Direction facing = getBlockState().getValue(BaseLightBlock.FACING);
        double x = getOwnerPos().getX() - trackingEntity.position().x;
        double z = getOwnerPos().getZ() - trackingEntity.position().z;
        double calc = Math.atan2(x, z);
        int pan = (int) Math.toDegrees(calc);
        pan = pan - (int) facing.toYRot();
//        if (pan < -180) {
//            pan += 360;
//        } else if (pan > 180) {
//            pan -= 360;
//        }
        if(this.pan != pan || this.tilt != calculatedTilt){
            setPan(-pan);
            setTilt(calculatedTilt);
            markAsDirty();
        }
    }

    @Override
    public int getFocus() {
        return 1;
    }

    @Override
    public void consume(byte[] dmxValues, boolean mapped) {
        if(!mapped) {
            int start = this.getChannelStart() > 0 ? this.getChannelStart() - 1 : 0;
            dmxValues = Arrays.copyOfRange(dmxValues, start,
                    start + this.getChannelCount());
            if (dmxValues.length < getChannelCount()) {
                return;
            }
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
        if(hasUpdated && this.hasLevel() && !this.level.isClientSide) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
            setChanged();
        }
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

    @Override
    public String getTranslationKey() {
        return "block.theatrical.fresnel";
    }

    public int convertByteToInt(byte val) {
        return Byte.toUnsignedInt(val);
    }

    @Override
    public void setTrackingEntity(@Nullable Entity entity) {
        this.trackingEntity = entity;
    }

    @Override
    public @Nullable Entity getTrackingEntity() {
        return trackingEntity;
    }

    @Override
    public void lightTick() {
        super.lightTick();
    }
}