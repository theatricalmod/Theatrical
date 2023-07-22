package dev.imabad.theatrical.fabric.client.model;

import dev.imabad.theatrical.client.model.CableModelBase;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public class CableModelFabric extends CableModelBase {

    @Nullable
    @Override
    public BakedModel bake(ModelBakery modelBakery, Function<Material, TextureAtlasSprite> spriteGetter, ModelState transform, ResourceLocation location) {
        return new CableBakedModelFabric(this, spriteGetter.apply(particle), getModelCallback(modelBakery, spriteGetter, transform, location));
    }

}
