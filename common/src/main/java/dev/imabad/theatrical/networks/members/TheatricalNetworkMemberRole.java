package dev.imabad.theatrical.networks.members;

import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public enum TheatricalNetworkMemberRole implements StringRepresentable {
    NONE("NONE"),
    SEND("SEND"),
    ADMIN("ADMIN");

    private final String name;

    TheatricalNetworkMemberRole(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public @NotNull String getSerializedName() {
        return name;
    }

    public static TheatricalNetworkMemberRole byName(String name){
        for (TheatricalNetworkMemberRole value : values()) {
            if(value.getName().equals(name)){
                return value;
            }
        }
        return null;
    }
}
