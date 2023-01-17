package cubicoder.well;

import cubicoder.well.block.ModBlocks;
import cubicoder.well.item.ModItems;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(WellMod.MODID)
public final class WellMod {
	
	public static final String MODID = "well";

	public WellMod() {
		ModBlocks.init();
		ModItems.init();
		
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
	}

	private void setup(final FMLCommonSetupEvent event) {

	}
	
	/*@Mod.EventHandler
	static void init(@Nonnull FMLInitializationEvent event) {
		ConfigHandler.initData();
	}*/
}
