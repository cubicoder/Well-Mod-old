package cubicoder.well.block;

import cubicoder.well.WellMod;
import cubicoder.well.block.entity.WellBlockEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlocks {

	public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, WellMod.MODID);

	public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITIES, WellMod.MODID);
	
	public static final RegistryObject<Block> WELL             = BLOCKS.register("well", () -> new WellBlock(Material.STONE, MaterialColor.COLOR_RED));
	public static final RegistryObject<Block> WHITE_WELL       = BLOCKS.register("white_well", () -> new WellBlock(Material.STONE, MaterialColor.SNOW));
	public static final RegistryObject<Block> ORANGE_WELL      = BLOCKS.register("orange_well", () -> new WellBlock(Material.STONE, MaterialColor.COLOR_ORANGE));
	public static final RegistryObject<Block> MAGENTA_WELL     = BLOCKS.register("magenta_well", () -> new WellBlock(Material.STONE, MaterialColor.COLOR_MAGENTA));
	public static final RegistryObject<Block> LIGHT_BLUE_WELL  = BLOCKS.register("light_blue_well", () -> new WellBlock(Material.STONE, MaterialColor.COLOR_LIGHT_BLUE));
	public static final RegistryObject<Block> YELLOW_WELL      = BLOCKS.register("yellow_well", () -> new WellBlock(Material.STONE, MaterialColor.COLOR_YELLOW));
	public static final RegistryObject<Block> LIME_WELL        = BLOCKS.register("lime_well", () -> new WellBlock(Material.STONE, MaterialColor.COLOR_LIGHT_GREEN));
	public static final RegistryObject<Block> PINK_WELL        = BLOCKS.register("pink_well", () -> new WellBlock(Material.STONE, MaterialColor.COLOR_PINK));
	public static final RegistryObject<Block> GRAY_WELL        = BLOCKS.register("gray_well", () -> new WellBlock(Material.STONE, MaterialColor.COLOR_GRAY));
	public static final RegistryObject<Block> LIGHT_GRAY_WELL  = BLOCKS.register("silver_well", () -> new WellBlock(Material.STONE, MaterialColor.COLOR_LIGHT_GRAY));
	public static final RegistryObject<Block> CYAN_WELL        = BLOCKS.register("cyan_well", () -> new WellBlock(Material.STONE, MaterialColor.COLOR_CYAN));
	public static final RegistryObject<Block> PURPLE_WELL      = BLOCKS.register("purple_well", () -> new WellBlock(Material.STONE, MaterialColor.COLOR_PURPLE));
	public static final RegistryObject<Block> BLUE_WELL        = BLOCKS.register("blue_well", () -> new WellBlock(Material.STONE, MaterialColor.COLOR_BLUE));
	public static final RegistryObject<Block> BROWN_WELL       = BLOCKS.register("brown_well", () -> new WellBlock(Material.STONE, MaterialColor.COLOR_BROWN));
	public static final RegistryObject<Block> GREEN_WELL       = BLOCKS.register("green_well", () -> new WellBlock(Material.STONE, MaterialColor.COLOR_GREEN));
	public static final RegistryObject<Block> RED_WELL         = BLOCKS.register("red_well", () -> new WellBlock(Material.STONE, MaterialColor.COLOR_RED));
	public static final RegistryObject<Block> BLACK_WELL       = BLOCKS.register("black_well", () -> new WellBlock(Material.STONE, MaterialColor.COLOR_BLACK));
	
	public static final RegistryObject<BlockEntityType<?>> WELL_BE = BLOCK_ENTITIES.register("well", 
			() -> BlockEntityType.Builder.of(WellBlockEntity::new, WELL.get()).build(null));
	
	public static void init() {
        BLOCKS.register(FMLJavaModLoadingContext.get().getModEventBus());
        BLOCK_ENTITIES.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
}
