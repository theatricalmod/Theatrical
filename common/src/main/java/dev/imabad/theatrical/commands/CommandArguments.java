package dev.imabad.theatrical.commands;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import dev.imabad.theatrical.TheatricalRegistry;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.core.registries.Registries;

public class CommandArguments {

    public static final DeferredRegister<ArgumentTypeInfo<?, ?>> ARGUMENTS = TheatricalRegistry.get(Registries.COMMAND_ARGUMENT_TYPE);

    public static final RegistrySupplier<ArgumentTypeInfo<DMXNetworkModeArgument, ?>> NETWORK_MODE = ARGUMENTS.register("network_mode", () -> SingletonArgumentInfo.contextFree(DMXNetworkModeArgument::networkMode));
}
