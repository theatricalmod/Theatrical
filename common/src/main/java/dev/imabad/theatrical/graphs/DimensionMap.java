package dev.imabad.theatrical.graphs;

import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

public class DimensionMap {

    List<ResourceKey<Level>> knownDimensions;

    public DimensionMap(){
        knownDimensions = new ArrayList<>();
    }

    public int map(ResourceKey<Level> dimension){
        int index = knownDimensions.indexOf(dimension);
        if(index == -1){
            index = knownDimensions.size();
            knownDimensions.add(dimension);
        }
        return index;
    }

    public ResourceKey<Level> map(int index){
        if(knownDimensions.size() <= index || index < 0)
            return Level.OVERWORLD;
        return knownDimensions.get(index);
    }

    public void write(CompoundTag tag){
        ListTag dimensions = new ListTag();
        for (ResourceKey<Level> knownDimension : knownDimensions) {
            CompoundTag dimensionTag = new CompoundTag();
            dimensionTag.putString("Id", knownDimension.location().toString());
            dimensions.add(dimensionTag);
        }
        tag.put("DimensionMap", dimensions);
    }

    public void toBuffer(FriendlyByteBuf buffer){
        buffer.writeInt(knownDimensions.size());
        for (ResourceKey<Level> knownDimension : knownDimensions) {
            buffer.writeResourceLocation(knownDimension.location());
        }
    }

    public static DimensionMap read(CompoundTag tag){
        DimensionMap map = new DimensionMap();
        ListTag dimensionMap = tag.getList("DimensionMap", Tag.TAG_COMPOUND);
        for (Tag t : dimensionMap) {
            CompoundTag compoundTag = (CompoundTag) t;
            map.knownDimensions.add(ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(compoundTag.getString("Id"))));
        }
        return map;
    }

    public static DimensionMap fromBuffer(FriendlyByteBuf buffer){
        DimensionMap dimensionMap = new DimensionMap();
        int length = buffer.readInt();
        for(int i = 0; i < length; i++){
            dimensionMap.knownDimensions.add(ResourceKey.create(Registry.DIMENSION_REGISTRY, buffer.readResourceLocation()));
        }
        return dimensionMap;
    }
}
