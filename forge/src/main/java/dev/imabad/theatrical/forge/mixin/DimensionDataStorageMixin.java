package dev.imabad.theatrical.forge.mixin;

import com.mojang.datafixers.DataFixer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.storage.DimensionDataStorage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.io.File;
import java.io.FileInputStream;
import java.io.PushbackInputStream;

/**
 * This mixin is only required for Forge 1.20.2 because it seems the saved data got busted if you don't pass in a datafixer
 *
 */
@Mixin(DimensionDataStorage.class)
public class DimensionDataStorageMixin {


    @Inject(method = "readTagFromDisk(Ljava/lang/String;Lnet/minecraft/util/datafix/DataFixTypes;I)Lnet/minecraft/nbt/CompoundTag;", at=@At(value= "INVOKE", target = "Lnet/minecraft/nbt/NbtUtils;getDataVersion(Lnet/minecraft/nbt/CompoundTag;I)I", shift = At.Shift.AFTER), cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
    public void afterGetVersion(String filename, DataFixTypes dataFixType, int version, CallbackInfoReturnable<CompoundTag> cir, File file, FileInputStream fio, PushbackInputStream pis, CompoundTag compoundTag){
        if(dataFixType == null){
            cir.setReturnValue(compoundTag);
            cir.cancel();
        }
    }
}
