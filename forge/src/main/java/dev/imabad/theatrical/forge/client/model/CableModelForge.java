package dev.imabad.theatrical.forge.client.model;

import dev.imabad.theatrical.client.model.CableModelBase;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import net.minecraftforge.client.model.geometry.IUnbakedGeometry;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public class CableModelForge extends CableModelBase implements IUnbakedGeometry<CableModelForge> {

    @Override
    public void resolveParents(Function<ResourceLocation, UnbakedModel> resolver) {
    }

    @Nullable
    @Override
    public BakedModel bake(
        ModelBaker baker,
        Function<Material, TextureAtlasSprite> spriteGetter,
        ModelState state,
        ResourceLocation location
    ) {
        return new CableBakedModelForge(
            this,
            spriteGetter.apply(particle),
            getModelCallback(baker, spriteGetter, state, location)
        );
    }

    @Override
    public BakedModel bake(
        IGeometryBakingContext context,
        ModelBaker baker,
        Function<Material, TextureAtlasSprite> spriteGetter,
        ModelState state,
        ItemOverrides overrides,
        ResourceLocation location
    ) {
        return bake(baker, spriteGetter, state, location);
    }
}
