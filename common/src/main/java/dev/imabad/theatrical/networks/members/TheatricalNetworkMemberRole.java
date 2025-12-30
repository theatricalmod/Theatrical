package dev.imabad.theatrical.networks.members;

import dev.imabad.theatrical.networks.TheatricalNetworkMode;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public enum TheatricalNetworkMemberRole implements StringRepresentable {
    NONE("NONE"),
    SEND("SEND"),
    ADMIN("ADMIN");

    public static final EnumCodec<TheatricalNetworkMemberRole> CODEC = StringRepresentable.fromEnum(TheatricalNetworkMemberRole::values);
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
