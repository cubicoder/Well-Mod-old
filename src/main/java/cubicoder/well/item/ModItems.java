package cubicoder.well.item;

import cubicoder.well.WellMod;
import cubicoder.well.block.ModBlocks;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModItems {

	public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, WellMod.MODID);
	
	public static final RegistryObject<BlockItem> WELL = fromBlock(ModBlocks.WELL);
	public static final RegistryObject<BlockItem> WHITE_WELL = fromBlock(ModBlocks.WHITE_WELL);
	public static final RegistryObject<BlockItem> ORANGE_WELL = fromBlock(ModBlocks.ORANGE_WELL);
	public static final RegistryObject<BlockItem> MAGENTA_WELL = fromBlock(ModBlocks.MAGENTA_WELL);
	public static final RegistryObject<BlockItem> LIGHT_BLUE_WELL = fromBlock(ModBlocks.LIGHT_BLUE_WELL);
	public static final RegistryObject<BlockItem> YELLOW_WELL = fromBlock(ModBlocks.YELLOW_WELL);
	public static final RegistryObject<BlockItem> LIME_WELL = fromBlock(ModBlocks.LIME_WELL);
	public static final RegistryObject<BlockItem> PINK_WELL = fromBlock(ModBlocks.PINK_WELL);
	public static final RegistryObject<BlockItem> GRAY_WELL = fromBlock(ModBlocks.GRAY_WELL);
	public static final RegistryObject<BlockItem> LIGHT_GRAY_WELL = fromBlock(ModBlocks.LIGHT_GRAY_WELL);
	public static final RegistryObject<BlockItem> CYAN_WELL = fromBlock(ModBlocks.CYAN_WELL);
	public static final RegistryObject<BlockItem> PURPLE_WELL = fromBlock(ModBlocks.PURPLE_WELL);
	public static final RegistryObject<BlockItem> BLUE_WELL = fromBlock(ModBlocks.BLUE_WELL);
	public static final RegistryObject<BlockItem> BROWN_WELL = fromBlock(ModBlocks.BROWN_WELL);
	public static final RegistryObject<BlockItem> GREEN_WELL = fromBlock(ModBlocks.GREEN_WELL);
	public static final RegistryObject<BlockItem> RED_WELL = fromBlock(ModBlocks.RED_WELL);
	public static final RegistryObject<BlockItem> BLACK_WELL = fromBlock(ModBlocks.BLACK_WELL);

	public static void init() {
        ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
	
	private static RegistryObject<BlockItem> fromBlock(RegistryObject<Block> block) {
		return ITEMS.register(block.getId().getPath(), () -> new BlockItem(block.get(), 
				new Item.Properties().tab(CreativeModeTab.TAB_DECORATIONS)));
	}
	
}
