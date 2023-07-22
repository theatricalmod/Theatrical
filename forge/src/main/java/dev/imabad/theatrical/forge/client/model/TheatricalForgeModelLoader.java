package dev.imabad.theatrical.forge.client.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import dev.imabad.theatrical.Theatrical;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.geometry.IGeometryLoader;

public class TheatricalForgeModelLoader implements IGeometryLoader<CableModelForge> {
    public static final ResourceLocation CABLE_MODEL_LOADER = new ResourceLocation(Theatrical.MOD_ID, "cable_model_loader");

    @Override
    public CableModelForge read(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        return new CableModelForge();
    }

}
