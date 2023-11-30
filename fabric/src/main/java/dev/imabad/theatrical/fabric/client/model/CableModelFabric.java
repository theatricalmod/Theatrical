package dev.imabad.theatrical.fabric.client.model;

import dev.imabad.theatrical.client.model.CableModelBase;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public class CableModelFabric extends CableModelBase {

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
        return new CableBakedModelFabric(
            this,
            spriteGetter.apply(particle),
            getModelCallback(baker, spriteGetter, state, location)
        );
    }
}
