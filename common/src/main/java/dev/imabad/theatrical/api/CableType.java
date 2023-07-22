package dev.imabad.theatrical.api;

import dev.imabad.theatrical.Theatrical;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;

public enum CableType implements StringRepresentable {
    DMX(new ResourceLocation(Theatrical.MOD_ID, "block/cable/dmx")),
    BUNDLED(new ResourceLocation(Theatrical.MOD_ID, "block/cable/bundled"));

    ResourceLocation tex;

    CableType(ResourceLocation textureLoc){
        this.tex = textureLoc;
    }

    public ResourceLocation getTex() {
        return tex;
    }

    @Override
    public String getSerializedName() {
        return name().toLowerCase();
    }
}
