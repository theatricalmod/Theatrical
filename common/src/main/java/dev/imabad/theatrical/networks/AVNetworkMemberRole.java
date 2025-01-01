package dev.imabad.theatrical.networks;

import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public enum AVNetworkMemberRole implements StringRepresentable {
    NONE("NONE"),
    SEND("SEND"),
    ADMIN("ADMIN");

    private final String name;

    AVNetworkMemberRole(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public @NotNull String getSerializedName() {
        return name;
    }

    public static AVNetworkMemberRole byName(String name){
        for (AVNetworkMemberRole value : values()) {
            if(value.getName().equals(name)){
                return value;
            }
        }
        return null;
    }
}
