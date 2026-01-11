package dev.imabad.theatrical.compat;

import dev.architectury.platform.Platform;

public class ModCompat {

    public static final boolean SHIMMER = Platform.isModLoaded("shimmer");
    public static final boolean CREATE = Platform.isModLoaded("create");

}
