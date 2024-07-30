package dev.imabad.theatrical.mixin;

import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(ArgumentTypeInfos.class)
public interface ArgumentTypeInfosAccessor {

    @Accessor("BY_CLASS")
    static Map<Class<?>, ArgumentTypeInfo<?, ?>> classMap(){
        throw new UnsupportedOperationException();
    }

}
