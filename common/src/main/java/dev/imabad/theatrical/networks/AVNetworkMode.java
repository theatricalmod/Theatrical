package dev.imabad.theatrical.networks;

import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public enum AVNetworkMode implements StringRepresentable {

    PUBLIC("PUBLIC"),
    INVITE("INVITE"),
    PRIVATE("PRIVATE");

    private final String name;

    AVNetworkMode(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public @NotNull String getSerializedName() {
        return name;
    }

    public static AVNetworkMode byName(String name){
        for (AVNetworkMode value : values()) {
            if(value.getName().equals(name)){
                return value;
            }
        }
        return null;
    }
}
