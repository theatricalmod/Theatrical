package dev.imabad.theatrical.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.architectury.utils.GameInstance;
import dev.imabad.theatrical.blockentities.sound.MixerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.util.List;

public record DimensionBlockPos(ResourceKey<Level> dimension, BlockPos pos) {
    public static final Codec<DimensionBlockPos> CODEC = RecordCodecBuilder.create(i -> i.group(
            ResourceKey.codec(Registries.DIMENSION).fieldOf("channel").forGetter(DimensionBlockPos::dimension),
            BlockPos.CODEC.fieldOf("devicePos").forGetter(DimensionBlockPos::pos)
    ).apply(i, DimensionBlockPos::new));

    public static final Codec<List<DimensionBlockPos>> LIST_CODEC = CODEC.listOf();

    public static DimensionBlockPos of(ResourceKey<Level> dimension, BlockPos pos) {
        return new DimensionBlockPos(dimension, pos);
    }

    public static DimensionBlockPos decode(FriendlyByteBuf buffer) {
        return new DimensionBlockPos(buffer.readResourceKey(Registries.DIMENSION), buffer.readBlockPos());
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeResourceKey(dimension);
        buffer.writeBlockPos(pos);
    }

    public String toNiceString() {
        return dimension.location() + " - " + pos.toShortString();
    }
    @Override
    public String toString() {
        return dimension.location() + ";" + pos.asLong();
    }
    public static DimensionBlockPos fromString(String input){
        String[] split = input.split(";");
        String dimension = split[0];
        String pos = split[1];
        return new DimensionBlockPos(ResourceKey.create(Registries.DIMENSION, ResourceLocation.tryParse(dimension)), BlockPos.of(Long.parseLong(pos)));
    }
}
