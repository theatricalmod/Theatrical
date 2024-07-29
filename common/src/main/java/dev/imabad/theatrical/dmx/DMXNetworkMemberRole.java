package dev.imabad.theatrical.dmx;

import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public enum DMXNetworkMemberRole implements StringRepresentable {
    NONE("NONE"),
    SEND("SEND"),
    ADMIN("ADMIN");

    private final String name;

    DMXNetworkMemberRole(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public @NotNull String getSerializedName() {
        return name;
    }

    public static DMXNetworkMemberRole byName(String name){
        for (DMXNetworkMemberRole value : values()) {
            if(value.getName().equals(name)){
                return value;
            }
        }
        return null;
    }
}
