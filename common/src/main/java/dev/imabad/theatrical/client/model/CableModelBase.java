package dev.imabad.theatrical.client.model;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Transformation;
import dev.imabad.theatrical.Theatrical;
import dev.imabad.theatrical.api.CableType;
import dev.imabad.theatrical.util.ClientUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;

@Environment(EnvType.CLIENT)
public abstract class CableModelBase implements UnbakedModel {

    public static final BlockModelRotation[] FACE_ROTATIONS = {
            BlockModelRotation.X0_Y0,
            BlockModelRotation.X180_Y0,
            BlockModelRotation.X90_Y180,
            BlockModelRotation.X90_Y0,
            BlockModelRotation.X90_Y90,
            BlockModelRotation.X90_Y270
    };

    public static final ResourceLocation modelNorth = new ResourceLocation(Theatrical.MOD_ID, "block/cable/cable_north");
    public static final ResourceLocation modelSouth =new ResourceLocation(Theatrical.MOD_ID, "block/cable/cable_south");
    public static final ResourceLocation modelEast = new ResourceLocation(Theatrical.MOD_ID, "block/cable/cable_east");
    public static final ResourceLocation modelWest = new ResourceLocation(Theatrical.MOD_ID, "block/cable/cable_west");
    public static final ResourceLocation modelCenter = new ResourceLocation(Theatrical.MOD_ID, "block/cable/cable_middle");

    public static Collection<ResourceLocation> getModelDependencies(){
        return List.of(modelNorth, modelSouth, modelEast, modelWest, modelCenter);
    }


    public final Material particle;
    public final Collection<Material> textures;

    public CableModelBase(){
        particle = new Material(TextureAtlas.LOCATION_BLOCKS, new ResourceLocation("minecraft:block/gray_concrete"));
        textures = new HashSet<>();
        for(CableType type : CableType.values()){
            textures.add(new Material(TextureAtlas.LOCATION_BLOCKS, type.getTex()));
        }
    }

    @Override
    public @NotNull Collection<ResourceLocation> getDependencies() {
        return getModelDependencies();
    }

    @Override
    public @NotNull Collection<Material> getMaterials(Function<ResourceLocation, UnbakedModel> modelGetter,
                                                      Set<Pair<String, String>> missingTextureErrors) {
        return textures;
    }

    public ModelCallback getModelCallback(ModelBakery modelBakery, Function<Material, TextureAtlasSprite> spriteGetter,
                                          ModelState transform, ResourceLocation location){
        return  (id, rotation, uvlock, retextures) -> {
            ImmutableMap.Builder<String, Material> builder = ImmutableMap.builder();

            for (Map.Entry<String, ResourceLocation> entry : retextures)
            {
                builder.put(new AbstractMap.SimpleEntry<>(entry.getKey(),
                        new Material(TextureAtlas.LOCATION_BLOCKS, entry.getValue())));
            }
            ImmutableMap<String, Material> builtRemapper = builder.build();
            UnbakedModel model = modelBakery.getModel(id);
            if(model instanceof BlockModel blockModel){
                builtRemapper.forEach((key, material) -> {
                    blockModel.textureMap.remove(key);
                    blockModel.textureMap.put(key, Either.left(material));
                });
                BasicModelState basicModelState;
                if(rotation.left().isPresent()){
                    basicModelState = new BasicModelState(rotation.left().get(), uvlock);
                } else{
                    basicModelState = new BasicModelState(rotation.right().get(), uvlock);
                }
                BakedModel bakedModel = blockModel.bake(modelBakery, spriteGetter, basicModelState, id);
                return ClientUtils.optimize(bakedModel.getQuads(null, null, null));
            }
            return Collections.emptyList();
        };
    }

    public interface ModelCallback
    {
        List<BakedQuad> get(ResourceLocation id, Either<BlockModelRotation, Transformation> rotation, boolean uvlock, Map.Entry<String, ResourceLocation>... retextures);

        default List<BakedQuad> get(ResourceLocation id, Either<BlockModelRotation, Transformation> rotation, Map.Entry<String, ResourceLocation>... retextures){
            return get(id, rotation, true, retextures);
        }
    }

    public static class BasicModelState implements ModelState {

        private final Transformation transformation;
        private final boolean uvLock;

        public BasicModelState(BlockModelRotation blockModelRotation, boolean uvLock) {
            this.transformation = blockModelRotation.getRotation();
            this.uvLock = uvLock;
        }

        public BasicModelState(Transformation transformation, boolean uvLock) {
            this.transformation = transformation;
            this.uvLock = uvLock;
        }

        @Override
        public @NotNull Transformation getRotation() {
            return transformation;
        }

        @Override
        public boolean isUvLocked() {
            return uvLock;
        }
    }
}
