package cubicoder.block;

import java.util.List;
import java.util.Objects;
import java.util.Random;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.MixinEnvironment.Side;

import com.mojang.math.Constants;

import cubicoder.BlockEntity.BlockEntityWell;
import cubicoder.config.ConfigHandler;
import cubicoder.init.ModSounds;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.SPacketTitle;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.math.*;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockWell extends Block implements EntityBlock {

	//public static final EnumProperty<Axis> AXIS = EnumProperty.create("axis", Axis.class, Axis::isHorizontal);
	public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.HORIZONTAL_AXIS;
	public static final BooleanProperty IS_BOTTOM = BooleanProperty.create("is_bottom");
	public static final BooleanProperty UPSIDE_DOWN = BooleanProperty.create("upside_down");

	protected FluidStack cachedFluid;

	public BlockWell(Material material) {
		this(material, material.getColor());
	}

	public BlockWell(Material material, MaterialColor mapColor) {
		this(Properties.of(material).color(mapColor).strength(3.0F, 1.5F).requiresCorrectToolForDrops());
	}

	public BlockWell(Properties properties) {
		super(properties);
		this.setDefaultState(getDefaultState().withProperty(IS_BOTTOM, true));
		this.registerDefaultState(this.stateDefinition.any().setValue(AXIS, Direction.Axis.X).setValue(IS_BOTTOM, false).setValue(UPSIDE_DOWN, false));
		//this.useNeighborBrightness = true;
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer.Builder(this).add(FluidUnlistedProperty.INSTANCE)
				.add(AXIS, IS_BOTTOM, UPSIDE_DOWN).build();
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState().withProperty(IS_BOTTOM, (meta & 1) == 1)
				.withProperty(UPSIDE_DOWN, (meta >> 1 & 1) == 1)
				.withProperty(AXIS, EnumFacing.getFacingFromVector((meta >> 2 & 1) ^ 1, 0, meta >> 2 & 1).getAxis());
	}

	@Override
	public int getMetaFromState(@Nonnull IBlockState state) {
		return (state.getValue(IS_BOTTOM) ? 1 : 0) | (state.getValue(UPSIDE_DOWN) ? 2 : 0)
				| state.getValue(AXIS).ordinal() << 1;
	}

	@Override
	public boolean hasBlockEntity(@Nonnull IBlockState state) {
		return state.getValue(IS_BOTTOM);
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new BlockEntityWell();
	}

	@Nullable
	@Override
	public BlockEntity createBlockEntity(@Nonnull World world, @Nonnull IBlockState state) {
		return hasBlockEntity(state) ? createNewBlockEntity(world, 0) : null;
	}

	@Override
	public boolean canPlaceBlockAt(@Nonnull World worldIn, @Nonnull BlockPos pos) {
		return super.canPlaceBlockAt(worldIn, pos)
				&& (super.canPlaceBlockAt(worldIn, pos.up()) || super.canPlaceBlockAt(worldIn, pos.down()));
	}

	@Nonnull
	@Override
	public IBlockState getStateForPlacement(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull EnumFacing facing,
			float hitX, float hitY, float hitZ, int meta, @Nonnull EntityLivingBase placer) {
		final EnumFacing.Axis axis = placer.isSneaking() ? placer.getHorizontalFacing().rotateY().getAxis()
				: placer.getHorizontalFacing().getAxis();
		return getDefaultState().withProperty(AXIS, axis).withProperty(UPSIDE_DOWN,
				!super.canPlaceBlockAt(worldIn, pos.up()));
	}

	@Override
	public void onBlockPlacedBy(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state,
			@Nonnull EntityLivingBase placer, @Nonnull ItemStack stack) {
		final int verticalDir = state.getValue(UPSIDE_DOWN) ? -1 : 1;
		worldIn.setBlockState(pos.up(verticalDir), state.withProperty(IS_BOTTOM, false),
				Constants.BlockFlags.SEND_TO_CLIENTS);
		// warn placer if only one well can function in the area
		if (ConfigHandler.onlyOnePerChunk && placer instanceof EntityPlayerMP) {
			final @Nullable BlockEntity tile = worldIn.getBlockEntity(pos);
			if (tile instanceof BlockEntityWell && ((BlockEntityWell) tile).nearbyWells > 1)
				sendWarning((EntityPlayerMP) placer, verticalDir == -1);
		}
	}

	protected void sendWarning(@Nonnull EntityPlayerMP player, boolean isUpsideDown) {
		player.connection.sendPacket(new SPacketTitle(SPacketTitle.Type.ACTIONBAR,
				new TextComponentTranslation(isUpsideDown ? "warn.well.onePerChunkFlipped" : "warn.well.onePerChunk")));
	}

	@Override
	public void breakBlock(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state) {
		final @Nullable BlockEntity tile = worldIn.getBlockEntity(pos);
		if (tile instanceof BlockEntityWell) {
			((BlockEntityWell) tile).countNearbyWells(te -> te.nearbyWells--);
			worldIn.removeBlockEntity(pos);
		}
	}

	@Override
	public void neighborChanged(@Nonnull IBlockState state, @Nonnull World worldIn, @Nonnull BlockPos pos,
			@Nonnull Block blockIn, @Nonnull BlockPos fromPos) {
		final int verticalDir = state.getValue(UPSIDE_DOWN) ? -1 : 1;
		if (pos.equals(fromPos.down(verticalDir)) && state.getValue(IS_BOTTOM)
				&& worldIn.getBlockState(fromPos).getBlock() != this)
			worldIn.destroyBlock(pos, false);

		else if (pos.equals(fromPos.up(verticalDir)) && !state.getValue(IS_BOTTOM)
				&& worldIn.getBlockState(fromPos).getBlock() != this)
			worldIn.destroyBlock(pos, false);
	}

	@Override
	public boolean onBlockActivated(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state,
			@Nonnull EntityPlayer playerIn, @Nonnull EnumHand hand, @Nonnull EnumFacing facing, float hitX, float hitY,
			float hitZ) {
		final @Nullable BlockEntity tile = worldIn.getBlockEntity(pos);
		if (tile instanceof BlockEntityWell
				&& tile.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, facing)) {
			final BlockEntityWell well = (BlockEntityWell) tile;
			final int prevAmount = well.tank.getFluidAmount();

			final @Nullable IFluidHandler handler = tile.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY,
					facing);
			final boolean changed = handler != null && FluidUtil.interactWithFluidHandler(playerIn, hand, handler);

			if (changed) {
				if (ConfigHandler.playSound && prevAmount > well.tank.getFluidAmount()) {
					worldIn.playSound(null, pos.up(), ModSounds.CRANK, SoundCategory.BLOCKS, 0.25f, 1);
					((BlockEntityWell) tile).delayUntilNextBucket = 32;
				}

				return true;
			}
		}

		return false;
	}

	@Override
	public int getLightValue(@Nonnull IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos) {
		if (state.getValue(IS_BOTTOM)) {
			final @Nullable BlockEntity tile = world.getBlockEntity(pos);
			if (tile instanceof BlockEntityWell) {
				final @Nullable FluidStack fluid = ((BlockEntityWell) tile).tank.getFluid();
				if (fluid != null && fluid.getFluid().canBePlacedInWorld()) {
					final float baseFluidLight = fluid.getFluid().getBlock().getDefaultState().getLightValue();
					if (baseFluidLight > 0) {
						if (FMLCommonHandler.instance().getSide().isClient() && canRenderFluid())
							return Math.max(state.getLightValue(), (int) baseFluidLight);

						final int fluidLight = MathHelper
								.clamp((int) (baseFluidLight * fluid.amount / ConfigHandler.tankCapacity + 0.5), 1, 15);
						return Math.max(state.getLightValue(), fluidLight);
					}
				}
			}
		}

		return state.getLightValue();
	}

	@SideOnly(Side.CLIENT)
	@Override
	public int getPackedLightmapCoords(@Nonnull IBlockState state, @Nonnull IBlockAccess source,
			@Nonnull BlockPos pos) {
		if (canRenderFluid()) {
			final @Nullable BlockEntity tile = source.getBlockEntity(pos);
			if (tile instanceof BlockEntityWell) {
				final @Nullable FluidStack fluid = ((BlockEntityWell) tile).tank.getFluid();
				if (fluid != null && fluid.getFluid().canBePlacedInWorld()) {
					final int fluidLight = fluid.getFluid().getBlock().getDefaultState().getLightValue();
					if (fluidLight > 0)
						return source.getCombinedLight(pos, Math.max(fluidLight, state.getLightValue()));
				}
			}
		}

		return super.getPackedLightmapCoords(state, source, pos);
	}

	@Override
	public void addCollisionBoxToList(@Nonnull IBlockState state, @Nonnull World worldIn, @Nonnull BlockPos pos,
			@Nonnull AxisAlignedBB entityBox, @Nonnull List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn,
			boolean isActualState) {
		WellCollisions.getCollisionBoxList(state)
				.forEach(aabb -> addCollisionBoxToList(pos, entityBox, collidingBoxes, aabb));
	}

	@Nullable
	@Override
	public RayTraceResult collisionRayTrace(@Nonnull IBlockState blockState, @Nonnull World worldIn,
			@Nonnull BlockPos pos, @Nonnull Vec3d start, @Nonnull Vec3d end) {
		final RayTraceResult[] collidingBoxes = WellCollisions.getTraceBoxList(blockState).stream()
				.map(aabb -> rayTrace(pos, start, end, aabb)).filter(Objects::nonNull).toArray(RayTraceResult[]::new);

		if (collidingBoxes.length == 0)
			return null;
		RayTraceResult furthest = null;
		double dist = -1;

		for (RayTraceResult trace : collidingBoxes) {
			final double newDist = trace.hitVec.squareDistanceTo(end);
			if (newDist > dist) {
				furthest = trace;
				dist = newDist;
			}
		}

		return furthest;
	}

	@Nonnull
	@SideOnly(Side.CLIENT)
	@Override
	public AxisAlignedBB getSelectedBoundingBox(@Nonnull IBlockState state, @Nonnull World worldIn,
			@Nonnull BlockPos pos) {
		final boolean isBottom = state.getValue(IS_BOTTOM) != state.getValue(UPSIDE_DOWN);
		return new AxisAlignedBB(0, isBottom ? 0 : -1, 0, 1, isBottom ? 2 : 1, 1).offset(pos);
	}

	@Nullable
	@Override
	public Boolean isEntityInsideMaterial(@Nonnull IBlockAccess world, @Nonnull BlockPos pos,
			@Nonnull IBlockState state, @Nonnull Entity entity, double yToTest, @Nonnull Material materialIn,
			boolean testingHead) {
		if (!testingHead)
			yToTest = entity.posY;
		final @Nullable BlockEntity tile = world.getBlockEntity(pos);
		if (tile instanceof BlockEntityWell) {
			final @Nullable FluidStack fluid = ((BlockEntityWell) tile).tank.getFluid();
			if (fluid != null && fluid.getFluid().canBePlacedInWorld())
				if (fluid.getFluid().getBlock().getDefaultState().getMaterial() == materialIn)
					return state.getValue(UPSIDE_DOWN)
							? yToTest >= pos.getY() - ConfigHandler.getRenderedFluidHeight(fluid, true)
							: yToTest <= pos.getY() + ConfigHandler.getRenderedFluidHeight(fluid, false);
		}

		return null;
	}

	@Nullable
	@Override
	public Boolean isAABBInsideMaterial(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull AxisAlignedBB boundingBox,
			@Nonnull Material materialIn) {
		final @Nullable BlockEntity tile = world.getBlockEntity(pos);
		if (tile instanceof BlockEntityWell) {
			final @Nullable FluidStack fluid = ((BlockEntityWell) tile).tank.getFluid();
			if (fluid != null && fluid.getFluid().canBePlacedInWorld()) {
				if (fluid.getFluid().getBlock().getDefaultState().getMaterial() == materialIn) {
					cachedFluid = fluid;
					return isAABBInsideLiquid(world, pos, boundingBox);
				}
			}
		}

		return null;
	}

	@Nullable
	@Override
	public Boolean isAABBInsideLiquid(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull AxisAlignedBB boundingBox) {
		if (cachedFluid == null) {
			final @Nullable BlockEntity tile = world.getBlockEntity(pos);
			if (tile instanceof BlockEntityWell) {
				final @Nullable FluidStack fluid = ((BlockEntityWell) tile).tank.getFluid();
				if (fluid != null && fluid.getFluid().canBePlacedInWorld())
					cachedFluid = fluid;
			}
		}

		final @Nullable FluidStack fluid = cachedFluid;
		cachedFluid = null;

		if (fluid == null)
			return false;
		if (world.getBlockState(pos).getValue(UPSIDE_DOWN))
			return boundingBox.minY >= pos.getY() - ConfigHandler.getRenderedFluidHeight(fluid, true) ? true : null;
		return boundingBox.minY <= pos.getY() + ConfigHandler.getRenderedFluidHeight(fluid, false) ? true : null;
	}

	@Override
	public float getBlockLiquidHeight(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state,
			@Nonnull Material material) {
		final @Nullable BlockEntity tile = world.getBlockEntity(pos);
		if (tile instanceof BlockEntityWell) {
			final @Nullable FluidStack fluid = ((BlockEntityWell) tile).tank.getFluid();
			if (fluid != null && fluid.getFluid().canBePlacedInWorld())
				if (fluid.getFluid().getBlock().getDefaultState().getMaterial() == material)
					return ConfigHandler.getRenderedFluidHeight(fluid, false);
		}

		return 0;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void randomDisplayTick(@Nonnull IBlockState stateIn, @Nonnull World worldIn, @Nonnull BlockPos pos,
			@Nonnull Random rand) {
		final @Nullable BlockEntity tile = worldIn.getBlockEntity(pos);
		if (tile instanceof BlockEntityWell) {
			final @Nullable FluidStack fluid = ((BlockEntityWell) tile).tank.getFluid();
			if (fluid != null && fluid.getFluid().canBePlacedInWorld()) {
				final float height = ConfigHandler.getRenderedFluidHeight(fluid, false);
				final IBlockState fluidState = fluid.getFluid().getBlock().getDefaultState()
						.withProperty(BlockLiquid.LEVEL, 8 - (int) (height * 8));
				fluidState.getBlock().randomDisplayTick(fluidState, worldIn, pos, rand);

				// get around lava particle check
				if (fluid.getFluid() == FluidRegistry.LAVA) {
					if (rand.nextInt(100) == 0) {
						final double x = pos.getX() + rand.nextFloat();
						final double y = pos.getY() + height;
						final double z = pos.getZ() + rand.nextFloat();
						worldIn.spawnParticle(EnumParticleTypes.LAVA, x, y, z, 0, 0, 0);
						worldIn.playSound(x, y, z, SoundEvents.BLOCK_LAVA_POP, SoundCategory.BLOCKS,
								0.2f + rand.nextFloat() * 0.2f, 0.9f + rand.nextFloat() * 0.15f, false);
					}

					if (rand.nextInt(200) == 0) {
						final double x = pos.getX() + 0.5;
						final double y = pos.getY() + height / 2;
						final double z = pos.getZ() + 0.5;
						worldIn.playSound(x, y, z, SoundEvents.BLOCK_LAVA_AMBIENT, SoundCategory.BLOCKS,
								0.2f + rand.nextFloat() * 0.2f, 0.9f + rand.nextFloat() * 0.15f, false);
					}
				}
			}
		}
	}

	@Nonnull
	@Override
	public SoundType getSoundType(@Nonnull IBlockState state, @Nonnull World world, @Nonnull BlockPos pos,
			@Nullable Entity entity) {
		// improve the roof sound if possible (some mods change the soundType of bricks
		// to be better)
		return state.getValue(IS_BOTTOM) ? getSoundType() : Blocks.BRICK_BLOCK.getSoundType();
	}

	@Nonnull
	@Override
	public EnumPushReaction getPushReaction(@Nonnull IBlockState state) {
		return EnumPushReaction.BLOCK;
	}

	@Override
	public boolean isFullCube(@Nonnull IBlockState state) {
		return false;
	}

	@Override
	public boolean isOpaqueCube(@Nonnull IBlockState state) {
		return false;
	}

	@Nonnull
	@Override
	public BlockFaceShape getBlockFaceShape(@Nonnull IBlockAccess worldIn, @Nonnull IBlockState state,
			@Nonnull BlockPos pos, @Nonnull EnumFacing face) {
		return state.getValue(IS_BOTTOM) && face != EnumFacing.UP ? BlockFaceShape.SOLID : BlockFaceShape.UNDEFINED;
	}

	@Override
	public boolean isSideSolid(@Nonnull IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos,
			@Nonnull EnumFacing side) {
		return state.getBlockFaceShape(world, pos, side) == BlockFaceShape.SOLID;
	}

	@Override
	public boolean doesSideBlockRendering(@Nonnull IBlockState state, @Nonnull IBlockAccess world,
			@Nonnull BlockPos pos, @Nonnull EnumFacing face) {
		return state.isSideSolid(world, pos, face);
	}

	@Override
	public boolean canRenderInLayer(@Nonnull IBlockState state, @Nonnull BlockRenderLayer layer) {
		return layer == BlockRenderLayer.SOLID || state.getValue(IS_BOTTOM) && layer == BlockRenderLayer.TRANSLUCENT;
	}

	public boolean canRenderFluid() {
		return MinecraftForgeClient.getRenderLayer() == BlockRenderLayer.TRANSLUCENT;
	}

	@Nonnull
	@Override
	public IBlockState getExtendedState(@Nonnull IBlockState state, @Nonnull IBlockAccess world,
			@Nonnull BlockPos pos) {
		if (state instanceof IExtendedBlockState) {
			final @Nullable BlockEntity tile = world.getBlockEntity(pos);
			if (tile instanceof BlockEntityWell)
				state = ((IExtendedBlockState) state).withProperty(FluidUnlistedProperty.INSTANCE,
						((BlockEntityWell) tile).tank.getFluid());
		}

		return state;
	}
}
