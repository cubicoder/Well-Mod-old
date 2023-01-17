package cubicoder.well;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Main.MODID)
public final class Main {
	
	public static final String MODID = "well";

	public Main() {
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
	}

	private void setup(final FMLCommonSetupEvent event) {

	}
	
	/*@Mod.EventHandler
	static void init(@Nonnull FMLInitializationEvent event) {
		ConfigHandler.initData();
	}*/
}
