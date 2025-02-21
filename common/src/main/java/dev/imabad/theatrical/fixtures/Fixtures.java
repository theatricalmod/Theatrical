package dev.imabad.theatrical.fixtures;

import dev.architectury.registry.registries.Registrar;
import dev.architectury.registry.registries.RegistrySupplier;
import dev.imabad.theatrical.Theatrical;
import dev.imabad.theatrical.TheatricalRegistry;
import dev.imabad.theatrical.api.Fixture;
import net.minecraft.resources.ResourceLocation;

public class Fixtures {
	private static final ResourceLocation REGISTRY_ID = new ResourceLocation(Theatrical.MOD_ID, "fixtures");
	public static final Registrar<Fixture> FIXTURES = TheatricalRegistry.create(REGISTRY_ID);

	public static final RegistrySupplier<Fixture> MOVING_LIGHT =
		FIXTURES.register(new ResourceLocation(Theatrical.MOD_ID, "moving_light"), MovingLightFixture::new);

	public static final RegistrySupplier<Fixture> MOVING_WASH =
		FIXTURES.register(new ResourceLocation(Theatrical.MOD_ID, "moving_wash"), MovingWashFixture::new);

	public static final RegistrySupplier<Fixture> MOVING_VL6 =
		FIXTURES.register(new ResourceLocation(Theatrical.MOD_ID, "moving_vl6"), MovingVL6Fixture::new);
	
	public static final RegistrySupplier<Fixture> MOVING_BEAM =
		FIXTURES.register(new ResourceLocation(Theatrical.MOD_ID, "moving_beam"), MovingBeamFixture::new);

	public static final RegistrySupplier<Fixture> LED_FRESNEL =
			FIXTURES.register(new ResourceLocation(Theatrical.MOD_ID, "fresnel"), LEDFresnelFixture::new);

	public static final RegistrySupplier<Fixture> RGB_BAR =
			FIXTURES.register(new ResourceLocation(Theatrical.MOD_ID, "rgb_bar"), RGBbarFixture::new);

	public static final RegistrySupplier<Fixture> BIG_PANEL =
			FIXTURES.register(new ResourceLocation(Theatrical.MOD_ID, "big_panel"), BigPanelFixture::new);

	public static final RegistrySupplier<Fixture> BIG_PANEL2 =
			FIXTURES.register(new ResourceLocation(Theatrical.MOD_ID, "big_panel2"), BigPanel2Fixture::new);

	public static final RegistrySupplier<Fixture> LED_FOUNTAIN =
			FIXTURES.register(new ResourceLocation(Theatrical.MOD_ID, "led_fountain"), LEDfountainFixture::new);

	public static final RegistrySupplier<Fixture> MOVING_VL2C =
			FIXTURES.register(new ResourceLocation(Theatrical.MOD_ID, "moving_vl2c"), MovingVL2CFixture::new);

	public static final RegistrySupplier<Fixture> MOVING_SCAN =
			FIXTURES.register(new ResourceLocation(Theatrical.MOD_ID, "moving_scan"), MovingScanFixture::new);
		
	public static final RegistrySupplier<Fixture> LED_PANEL_2 =
			FIXTURES.register(new ResourceLocation(Theatrical.MOD_ID, "led_panel_2"), LEDPanel2Fixture::new);		

	public static final RegistrySupplier<Fixture> LED_PANEL =
			FIXTURES.register(new ResourceLocation(Theatrical.MOD_ID, "led_panel"), LEDPanelFixture::new);
			
	public static final RegistrySupplier<Fixture> PAR_LED =
			FIXTURES.register(new ResourceLocation(Theatrical.MOD_ID, "par_led"), ParLedFixture::new);

	public static final RegistrySupplier<Fixture> REDSTONE_INTERFACE = FIXTURES.register(
			new ResourceLocation(Theatrical.MOD_ID, "redstone_interface"),
			RedstoneInterfaceFixture::new
	);
	public static void init() {
		// NOOP
	}
}
