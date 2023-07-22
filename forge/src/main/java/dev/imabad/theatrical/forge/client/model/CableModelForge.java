package dev.imabad.theatrical.forge.client.model;

import com.mojang.datafixers.util.Pair;
import dev.imabad.theatrical.client.model.CableModelBase;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import net.minecraftforge.client.model.geometry.IUnbakedGeometry;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Set;
import java.util.function.Function;

public class CableModelForge extends CableModelBase implements IUnbakedGeometry<CableModelForge> {

    @Nullable
    @Override
    public BakedModel bake(ModelBakery modelBakery, Function<Material, TextureAtlasSprite> spriteGetter,
                           ModelState transform, ResourceLocation location) {
        return new CableBakedModelForge(this, spriteGetter.apply(particle),
                getModelCallback(modelBakery, spriteGetter, transform, location));
    }

    @Override
    public BakedModel bake(IGeometryBakingContext iGeometryBakingContext, ModelBakery modelBakery,
                           Function<Material, TextureAtlasSprite> spriteGetter, ModelState transform,
                           ItemOverrides arg3, ResourceLocation location) {
        return bake(modelBakery, spriteGetter, transform, location);
    }

    @Override
    public Collection<Material> getMaterials(IGeometryBakingContext iGeometryBakingContext, Function<ResourceLocation, UnbakedModel> modelGetter, Set<Pair<String, String>> missingTextureErrors) {
        return getMaterials(modelGetter, missingTextureErrors);
    }
}
