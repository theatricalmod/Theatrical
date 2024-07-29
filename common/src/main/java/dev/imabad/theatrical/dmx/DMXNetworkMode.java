package dev.imabad.theatrical.dmx;

import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public enum DMXNetworkMode implements StringRepresentable {

    PUBLIC("PUBLIC"),
    INVITE("INVITE"),
    PRIVATE("PRIVATE");

    private final String name;

    DMXNetworkMode(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public @NotNull String getSerializedName() {
        return name;
    }

    public static DMXNetworkMode byName(String name){
        for (DMXNetworkMode value : values()) {
            if(value.getName().equals(name)){
                return value;
            }
        }
        return null;
    }
}
