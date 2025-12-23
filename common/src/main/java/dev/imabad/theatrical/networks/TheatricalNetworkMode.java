package dev.imabad.theatrical.networks;

import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public enum TheatricalNetworkMode implements StringRepresentable {

    PUBLIC("PUBLIC"),
    INVITE("INVITE"),
    PRIVATE("PRIVATE");

    private final String name;

    TheatricalNetworkMode(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public @NotNull String getSerializedName() {
        return name;
    }

    public static TheatricalNetworkMode byName(String name){
        for (TheatricalNetworkMode value : values()) {
            if(value.getName().equals(name)){
                return value;
            }
        }
        return null;
    }
}
