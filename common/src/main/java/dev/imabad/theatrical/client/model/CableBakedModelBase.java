package dev.imabad.theatrical.client.model;

import com.mojang.datafixers.util.Either;
import com.mojang.math.Quaternion;
import com.mojang.math.Transformation;
import dev.imabad.theatrical.api.CableType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class CableBakedModelBase implements BakedModel {

    public final TextureAtlasSprite particle;
    public final Map<Direction, Map<CableType, Map<Direction, List<BakedQuad>>>> directionToTypeToSide;
    public final Map<Direction, Map<CableType, Map<Direction, List<BakedQuad>>>> cornerPieces;

    private static Transformation mulBlockModelRotations(BlockModelRotation pos1, BlockModelRotation pos2){
        Quaternion rotation = pos1.getRotation().getLeftRotation();
        rotation.mul(pos2.getRotation().getLeftRotation());
        return new Transformation(null, rotation, null, null);
    }
    public CableBakedModelBase(CableModelBase cable, TextureAtlasSprite p, CableModelBase.ModelCallback c) {
        particle = p;
        directionToTypeToSide = new HashMap<>();
        cornerPieces = new HashMap<>();
        // Side to type to direction
        for (Direction facing : Direction.values()) {
            Map<CableType, Map<Direction, List<BakedQuad>>> facingMap = new HashMap<>();
            Map<CableType, Map<Direction, List<BakedQuad>>> cornerMap = new HashMap<>();
            for (CableType cableType : CableType.values()) {
                Map<Direction, List<BakedQuad>> typeMap = new HashMap<>();
                Map<Direction, List<BakedQuad>> cornerTypeMap = new HashMap<>();
                AbstractMap.SimpleEntry<String, ResourceLocation> entry = new AbstractMap.SimpleEntry<>("0", cableType.getTex());
                typeMap.put(Direction.NORTH, c.get(cable.modelNorth, Either.left(CableModelBase.FACE_ROTATIONS[facing.ordinal()]), entry));
                typeMap.put(Direction.SOUTH, c.get(cable.modelSouth, Either.left(CableModelBase.FACE_ROTATIONS[facing.ordinal()]), entry));
                typeMap.put(Direction.EAST, c.get(cable.modelEast, Either.left(CableModelBase.FACE_ROTATIONS[facing.ordinal()]), entry));
                typeMap.put(Direction.WEST, c.get(cable.modelWest, Either.left(CableModelBase.FACE_ROTATIONS[facing.ordinal()]), entry));
                cornerTypeMap.put(Direction.NORTH, c.get(cable.modelCenter, Either.right(mulBlockModelRotations(CableModelBase.FACE_ROTATIONS[facing.ordinal()], BlockModelRotation.X0_Y0)), entry));
                cornerTypeMap.put(Direction.SOUTH, c.get(cable.modelCenter, Either.right(mulBlockModelRotations(CableModelBase.FACE_ROTATIONS[facing.ordinal()],BlockModelRotation.X0_Y180)), entry));
                cornerTypeMap.put(Direction.EAST, c.get(cable.modelCenter, Either.right(mulBlockModelRotations(CableModelBase.FACE_ROTATIONS[facing.ordinal()],BlockModelRotation.X0_Y90)), entry));
                cornerTypeMap.put(Direction.WEST, c.get(cable.modelCenter, Either.right(mulBlockModelRotations(CableModelBase.FACE_ROTATIONS[facing.ordinal()],BlockModelRotation.X0_Y270)), entry));
                facingMap.put(cableType, typeMap);
                cornerMap.put(cableType, cornerTypeMap);
            }
            cornerPieces.put(facing, cornerMap);
            directionToTypeToSide.put(facing, facingMap);
        }
    }

    @Override
    public boolean useAmbientOcclusion() {
        return true;
    }

    @Override
    public boolean isGui3d() {
        return false;
    }

    @Override
    public boolean usesBlockLight() {
        return false;
    }

    @Override
    public boolean isCustomRenderer() {
        return false;
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        return particle;
    }

    @Override
    public ItemTransforms getTransforms() {
        return null;
    }

    @Override
    public ItemOverrides getOverrides() {
        return null;
    }
}
