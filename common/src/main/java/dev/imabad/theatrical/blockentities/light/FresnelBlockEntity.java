package dev.imabad.theatrical.blockentities.light;

import dev.imabad.theatrical.api.Fixture;
import dev.imabad.theatrical.blockentities.BlockEntities;
import dev.imabad.theatrical.blocks.light.MovingLightBlock;
import dev.imabad.theatrical.fixtures.Fixtures;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Arrays;

public class FresnelBlockEntity extends BaseDMXConsumerLightBlockEntity {
    public FresnelBlockEntity(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
        setChannelCount(4);
    }

    public FresnelBlockEntity(BlockPos pos, BlockState state) {
        this(BlockEntities.LED_FRESNEL.get(), pos, state);
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
    public int convertByteToInt(byte val) {
        return Byte.toUnsignedInt(val);
    }

}