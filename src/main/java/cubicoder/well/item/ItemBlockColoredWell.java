package cubicoder.well.item;

import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.ItemHandlerHelper;

public class ItemBlockColoredWell extends BlockItem {

	public ItemBlockColoredWell(Block block, Properties properties) {
		super(block, properties);
	}
	
	@Override
	public InteractionResult useOn(UseOnContext context) {
		BlockState state = context.getLevel().getBlockState(context.getClickedPos());
		if (state.getBlock() instanceof LayeredCauldronBlock) {
			if (!context.getLevel().isClientSide && !context.getPlayer().isCreative()) {
				int level = state.getValue(LayeredCauldronBlock.LEVEL);
				if (level > 0) {
					context.getPlayer().getItemInHand(context.getHand()).shrink(1);
					context.getPlayer().awardStat(Stats.USE_CAULDRON);
					ItemHandlerHelper.giveItemToPlayer(context.getPlayer(), new ItemStack(ModItems.WELL.get()));
					LayeredCauldronBlock.lowerFillLevel(state, context.getLevel(), context.getClickedPos());
				}
			}
		}
		
		return InteractionResult.SUCCESS;
	}
	
	/*@Nonnull
	@Override
	public EnumActionResult onItemUse(@Nonnull EntityPlayer player, @Nonnull World worldIn, @Nonnull BlockPos pos,
			@Nonnull EnumHand hand, @Nonnull EnumFacing facing, float hitX, float hitY, float hitZ) {
		final IBlockState state = worldIn.getBlockState(pos);
		if (state.getBlock() instanceof BlockCauldron) {
			if (!worldIn.isRemote && !player.isCreative()) {
				final int level = state.getValue(BlockCauldron.LEVEL);
				if (level > 0) {
					player.getHeldItem(hand).shrink(1);
					player.addStat(StatList.CAULDRON_USED);
					ItemHandlerHelper.giveItemToPlayer(player, new ItemStack(ModItems.WELL));
					((BlockCauldron) state.getBlock()).setWaterLevel(worldIn, pos, state, level - 1);

				}
			}

			return EnumActionResult.SUCCESS;
		}

		return super.onItemUse(player, worldIn, pos, hand, facing, hitX, hitY, hitZ);
	}*/
}
