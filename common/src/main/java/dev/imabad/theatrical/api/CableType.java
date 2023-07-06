package dev.imabad.theatrical.api;

import net.minecraft.util.StringRepresentable;

public enum CableType implements StringRepresentable {
    DMX,
    BUNDLED;

    @Override
    public String getSerializedName() {
        return name().toLowerCase();
    }
}
