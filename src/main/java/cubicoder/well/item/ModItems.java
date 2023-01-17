package cubicoder.well.item;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import cubicoder.well.block.ModBlocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.world.item.BlockItem;

/**
 *
 * @author jbred
 *
 */
public final class ModItems
{
    @Nonnull public static final List<BlockItem> INIT = new ArrayList<>();

    @Nonnull public static final BlockItem WELL = register("well", new BlockItem(ModBlocks.WELL));
    @Nonnull public static final BlockItem WHITE_WELL      = register(0);
    @Nonnull public static final BlockItem ORANGE_WELL     = register(1);
    @Nonnull public static final BlockItem MAGENTA_WELL    = register(2);
    @Nonnull public static final BlockItem LIGHT_BLUE_WELL = register(3);
    @Nonnull public static final BlockItem YELLOW_WELL     = register(4);
    @Nonnull public static final BlockItem LIME_WELL       = register(5);
    @Nonnull public static final BlockItem PINK_WELL       = register(6);
    @Nonnull public static final BlockItem GRAY_WELL       = register(7);
    @Nonnull public static final BlockItem SILVER_WELL     = register(8);
    @Nonnull public static final BlockItem CYAN_WELL       = register(9);
    @Nonnull public static final BlockItem PURPLE_WELL     = register(10);
    @Nonnull public static final BlockItem BLUE_WELL       = register(11);
    @Nonnull public static final BlockItem BROWN_WELL      = register(12);
    @Nonnull public static final BlockItem GREEN_WELL      = register(13);
    @Nonnull public static final BlockItem RED_WELL        = register(14);
    @Nonnull public static final BlockItem BLACK_WELL      = register(15);

    @Nonnull
    static BlockItem register(@Nonnull String name, @Nonnull BlockItem item) {
        item.setRegistryName("well", name).setTranslationKey("well." + name);
        INIT.add(item);
        return item;
    }

    @Nonnull
    static BlockItem register(int index) {
        return register(
                EnumDyeColor.byMetadata(index).getName() + "_well",
                new BlockItemColoredWell(ModBlocks.INIT.get(index + 1))
        );
    }
}
