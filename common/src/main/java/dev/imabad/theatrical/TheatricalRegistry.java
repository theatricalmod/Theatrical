package dev.imabad.theatrical;

import com.google.common.base.Suppliers;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.Registrar;
import dev.architectury.registry.registries.RegistrarManager;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Supplier;

public class TheatricalRegistry {
	public static final Supplier<RegistrarManager> MANAGER =
		Suppliers.memoize(() -> RegistrarManager.get(Theatrical.MOD_ID));

	public static <T> DeferredRegister<T> get(ResourceKey<Registry<T>> registry) {
		return DeferredRegister.create(Theatrical.MOD_ID, registry);
	}

	@SuppressWarnings("unchecked")
	public static <T> Registrar<T> create(ResourceLocation registryId) {
		return (Registrar<T>) MANAGER.get().builder(registryId).build();
	}
}
