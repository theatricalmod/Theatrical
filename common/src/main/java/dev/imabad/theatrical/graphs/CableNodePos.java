package dev.imabad.theatrical.graphs;

import dev.imabad.theatrical.api.CableType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class CableNodePos extends BlockPos{

    ResourceKey<Level> dimension;
    public CableNodePos(int x, int y, int z){
        super(x, y, z);
    }
    public CableNodePos(double x, double y, double z){
        super(Math.round(x * 2), Math.round(y * 2), Math.round(z * 2));
    }
    public CableNodePos(Vec3 vec){this(vec.x, vec.y, vec.z);}
    public CableNodePos(BlockPos pos){
        this(pos.getX(), pos.getY(), pos.getZ());
    }

    public CableNodePos dimension(Level level){
        return dimension(level.dimension());
    }

    public CableNodePos dimension(ResourceKey<Level> dimension){
        this.dimension = dimension;
        return this;
    }

    public ResourceKey<Level> dimension(){
        return this.dimension;
    }

    public Collection<BlockPos> allAdjacent(){
        Set<BlockPos> adjacent = new HashSet<>();
        Vec3 vec3 = getLocation();
        double step = 1 / 8f; //TODO: Figure this out?!
        for(int x : new int[]{1, -1}){
            for(int y : new int[]{1, -1}){
                for(int z : new int[]{1, -1}){
                    adjacent.add(new BlockPos(vec3.add(x * step, y * step, z * step)));
                }
            }
        }
        return adjacent;
    }

    @Override
    public boolean equals(Object object) {
        return super.equals(object) && object instanceof CableNodePos cnp && Objects.equals(cnp.dimension, dimension);
    }

    public Vec3 getLocation() {
        return new Vec3(getX() / 2.0 , getY() / 2.0, getZ() / 2.0);
    }

    public CompoundTag write(DimensionMap dimensionMap){
        CompoundTag c = NbtUtils.writeBlockPos(new BlockPos(this));
        c.putInt("Dimension", dimensionMap.map(dimension));
        return c;
    }

    public void toBuf(FriendlyByteBuf buf, DimensionMap dimensionMap){
        buf.writeVarInt(getX());
        buf.writeShort(getY());
        buf.writeVarInt(getZ());
        buf.writeVarInt(dimensionMap.map(dimension));
    }

    public static CableNodePos read(CompoundTag location, DimensionMap dimensionMap) {
        CableNodePos pos = fromBlockPos(NbtUtils.readBlockPos(location));
        pos.dimension = dimensionMap.map(location.getInt("Dimension"));
        return pos;
    }

    private static CableNodePos fromBlockPos(BlockPos pos){
        return new CableNodePos(pos);
    }

    public static CableNodePos fromBuffer(FriendlyByteBuf buf, DimensionMap dimensionMap) {
        CableNodePos cableNodePos = fromBlockPos(new BlockPos(
                buf.readVarInt(),
                buf.readShort(),
                buf.readVarInt()
        ));
        cableNodePos.dimension = dimensionMap.map(buf.readVarInt());
        return cableNodePos;
    }

    public static class DiscoveredPosition extends CableNodePos {
        CableType cableTypeA;
        CableType cableTypeB;

        public DiscoveredPosition(int x, int y, int z) {
            super(x, y, z);
        }

        public DiscoveredPosition(ResourceKey<Level> dimension, Vec3 pos){
            super(pos);
            dimension(dimension);
        }

        public DiscoveredPosition typeA(CableType cableType){
            this.cableTypeA = cableType;
            return this;
        }

        public DiscoveredPosition typeB(CableType cableType){
            this.cableTypeB = cableType;
            return this;
        }

        public boolean isDifferentTypes(){
            return cableTypeA != cableTypeB;
        }

    }
}
