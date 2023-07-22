package dev.imabad.theatrical.fabric.client.model;

import dev.imabad.theatrical.Theatrical;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.model.ModelProviderContext;
import net.fabricmc.fabric.api.client.model.ModelProviderException;
import net.fabricmc.fabric.api.client.model.ModelResourceProvider;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class TheatricalFabricModelLoader implements ModelResourceProvider {
    public static final ResourceLocation CABLE_LOCATION = new ResourceLocation(Theatrical.MOD_ID, "block/cable");
    @Override
    public @Nullable UnbakedModel loadModelResource(ResourceLocation resourceId, ModelProviderContext context) throws ModelProviderException {
        if(resourceId.equals(CABLE_LOCATION)){
            return new CableModelFabric();
        }
        return null;
    }
}
