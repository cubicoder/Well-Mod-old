package cubicoder.well.block;

import java.util.Random;

import cubicoder.well.block.entity.WellBlockEntity;
import cubicoder.well.config.ConfigHandler;
import cubicoder.well.init.ModSounds;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.loading.FMLEnvironment;

public class WellBlock extends Block implements EntityBlock {

	//public static final EnumProperty<Axis> AXIS = EnumProperty.create("axis", Axis.class, Axis::isHorizontal);
	public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.HORIZONTAL_AXIS;
	public static final BooleanProperty IS_BOTTOM = BooleanProperty.create("is_bottom");
	public static final BooleanProperty UPSIDE_DOWN = BooleanProperty.create("upside_down");

	protected FluidStack cachedFluid;

	public WellBlock(Material material) {
		this(material, material.getColor());
	}

	public WellBlock(Material material, MaterialColor mapColor) {
		this(Properties.of(material).color(mapColor).strength(3.0F, 1.5F).requiresCorrectToolForDrops());
	}

	public WellBlock(Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(AXIS, Direction.Axis.X).setValue(IS_BOTTOM, false).setValue(UPSIDE_DOWN, false));
		// TODO useNeighborBrightness
		//this.useNeighborBrightness = true;
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		// TODO FluidUnlistedProperty
		builder/*.add(FluidUnlistedProperty.INSTANCE)*/.add(AXIS, IS_BOTTOM, UPSIDE_DOWN);
	}

	// TODO getStateFromMeta getMetaFromState
	/*@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState().withProperty(IS_BOTTOM, (meta & 1) == 1)
				.withProperty(UPSIDE_DOWN, (meta >> 1 & 1) == 1)
				.withProperty(AXIS, EnumFacing.getFacingFromVector((meta >> 2 & 1) ^ 1, 0, meta >> 2 & 1).getAxis());
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return (state.getValue(IS_BOTTOM) ? 1 : 0) | (state.getValue(UPSIDE_DOWN) ? 2 : 0)
				| state.getValue(AXIS).ordinal() << 1;
	}*/
	
	// TODO hasBlockEntity
	/*@Override
	public boolean hasBlockEntity(BlockState state) {
		return state.getValue(IS_BOTTOM);
	}*/

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		if (level.isClientSide()) {
			return null;
		}
		return (lvl, pos, blockState, t) -> {
			if (t instanceof WellBlockEntity be) {
				be.tick();
			}
		};
	}
	
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new WellBlockEntity(pos, state);
	}

	// TODO createBlockEntity
	/*@Override
	public BlockEntity createBlockEntity(World world, BlockState state) {
		return hasBlockEntity(state) ? createNewBlockEntity(world, 0) : null;
	}*/
	
	/*@Override
	public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
		return super.canPlaceBlockAt(worldIn, pos)
				&& (super.canPlaceBlockAt(worldIn, pos.up()) || super.canPlaceBlockAt(worldIn, pos.down()));
	}*/
	
	// TODO do I need this?
	@Override
	public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
		return super.canSurvive(state, level, pos)
				&& (super.canSurvive(state, level, pos.above()) || super.canSurvive(state, level, pos.below()));
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		Direction.Axis axis = context.getPlayer().isCrouching() ? context.getHorizontalDirection().getClockWise().getAxis()
				: context.getHorizontalDirection().getAxis();
		return super.getStateForPlacement(context).setValue(AXIS, axis)
				.setValue(UPSIDE_DOWN, !super.canSurvive(defaultBlockState(), context.getLevel(), context.getClickedPos()));
	}
	
	/*@Override
	public BlockState getStateForPlacement(World worldIn, @Nonnull BlockPos pos, @Nonnull EnumFacing facing,
			float hitX, float hitY, float hitZ, int meta, @Nonnull EntityLivingBase placer) {
		final EnumFacing.Axis axis = placer.isSneaking() ? placer.getHorizontalFacing().rotateY().getAxis()
				: placer.getHorizontalFacing().getAxis();
		return getDefaultState().withProperty(AXIS, axis).withProperty(UPSIDE_DOWN,
				!super.canPlaceBlockAt(worldIn, pos.up()));
	}*/

	@Override
	public void onPlace(BlockState pState, Level pLevel, BlockPos pPos, BlockState pOldState, boolean pIsMoving) {
		// TODO Auto-generated method stub
		super.onPlace(pState, pLevel, pPos, pOldState, pIsMoving);
	}
	
	@Override
	public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
		int verticalDir = state.getValue(UPSIDE_DOWN) ? -1 : 1;
		level.setBlock(pos.above(verticalDir), state.setValue(IS_BOTTOM, false), Block.UPDATE_CLIENTS);
		// warn placer if only one well can function in the area
		// TODO config stuff
		if (ConfigHandler.onlyOnePerChunk && placer instanceof ServerPlayer) {
			BlockEntity be = level.getBlockEntity(pos);
			if (be instanceof WellBlockEntity && ((WellBlockEntity) be).nearbyWells > 1)
				// TODO remove unnecessary extra method?
				// TODO stop block from being placed if there's only one well per area?
				sendWarning((ServerPlayer) placer, verticalDir == -1);
		}
	}
	
	/*@Override
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
	}*/

	protected void sendWarning(ServerPlayer player, boolean isUpsideDown) {
		/*player.connection.send(new SPacketTitle(SPacketTitle.Type.ACTIONBAR,
				new TextComponentTranslation(isUpsideDown ? "warn.well.onePerChunkFlipped" : "warn.well.onePerChunk")));*/
		String message = isUpsideDown ? "warn.well.onePerChunkFlipped" : "warn.well.onePerChunk";
		player.displayClientMessage(new TranslatableComponent(message), true);
	}
	
	@Override
	public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
		if (!state.is(newState.getBlock())) {
			BlockEntity be = level.getBlockEntity(pos);
			if (be instanceof WellBlockEntity) {
				((WellBlockEntity) be).countNearbyWells(te -> te.nearbyWells--);
			}
			super.onRemove(state, level, pos, newState, isMoving);
		}
	}
	
	/*@Override
	public void breakBlock(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state) {
		final @Nullable BlockEntity tile = worldIn.getBlockEntity(pos);
		if (tile instanceof WellBlockEntity) {
			((WellBlockEntity) tile).countNearbyWells(te -> te.nearbyWells--);
			worldIn.removeBlockEntity(pos);
		}
	}*/
	
	@Override
	public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
		super.neighborChanged(state, level, pos, block, fromPos, isMoving); // TODO needed?
		int verticalDir = state.getValue(UPSIDE_DOWN) ? -1 : 1;
		if (pos.equals(fromPos.below(verticalDir)) && state.getValue(IS_BOTTOM) 
				&& level.getBlockState(fromPos).getBlock() != this) {
			level.destroyBlock(pos, false);
		} else if (pos.equals(fromPos.above(verticalDir)) && !state.getValue(IS_BOTTOM) 
				&& level.getBlockState(fromPos).getBlock() != this) {
			level.destroyBlock(pos, false);
		}
	}

	/*@Override
	public void neighborChanged(@Nonnull IBlockState state, @Nonnull World worldIn, @Nonnull BlockPos pos,
			@Nonnull Block blockIn, @Nonnull BlockPos fromPos) {
		final int verticalDir = state.getValue(UPSIDE_DOWN) ? -1 : 1;
		if (pos.equals(fromPos.down(verticalDir)) && state.getValue(IS_BOTTOM)
				&& worldIn.getBlockState(fromPos).getBlock() != this)
			worldIn.destroyBlock(pos, false);

		else if (pos.equals(fromPos.up(verticalDir)) && !state.getValue(IS_BOTTOM)
				&& worldIn.getBlockState(fromPos).getBlock() != this)
			worldIn.destroyBlock(pos, false);
	}*/

	@Override
	public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		BlockEntity be = level.getBlockEntity(pos);
		LazyOptional<IFluidHandler> handler = be.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, hit.getDirection());
		if (be instanceof WellBlockEntity && handler != null && handler.isPresent()) {
			WellBlockEntity well = (WellBlockEntity) be;
			int prevAmount = well.tank.getFluidAmount();
			
			if (FluidUtil.interactWithFluidHandler(player, hand, handler.resolve().get())) {
				if (ConfigHandler.playSound && prevAmount > well.tank.getFluidAmount()) {
					level.playSound(null, pos.above(), ModSounds.CRANK, SoundSource.BLOCKS, 0.25F, 1);
					((WellBlockEntity) be).delayUntilNextBucket = 32;
				}
				
				return InteractionResult.SUCCESS;
			}
		}
		
		return InteractionResult.PASS;
	}
	
	/*@Override
	public boolean onBlockActivated(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state,
			@Nonnull EntityPlayer playerIn, @Nonnull EnumHand hand, @Nonnull EnumFacing facing, float hitX, float hitY,
			float hitZ) {
		final @Nullable BlockEntity tile = worldIn.getBlockEntity(pos);
		if (tile instanceof WellBlockEntity
				&& tile.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, facing)) {
			final WellBlockEntity well = (WellBlockEntity) tile;
			final int prevAmount = well.tank.getFluidAmount();

			final @Nullable IFluidHandler handler = tile.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY,
					facing);
			final boolean changed = handler != null && FluidUtil.interactWithFluidHandler(playerIn, hand, handler);

			if (changed) {
				if (ConfigHandler.playSound && prevAmount > well.tank.getFluidAmount()) {
					worldIn.playSound(null, pos.up(), ModSounds.CRANK, SoundCategory.BLOCKS, 0.25f, 1);
					((WellBlockEntity) tile).delayUntilNextBucket = 32;
				}

				return true;
			}
		}

		return false;
	}*/
	
	@Override
	public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
		if (state.getValue(IS_BOTTOM)) {
			BlockEntity be = level.getBlockEntity(pos);
			if (be instanceof WellBlockEntity) {
				FluidStack fluid = ((WellBlockEntity) be).tank.getFluid();
				FluidAttributes fluidAttr = fluid.getFluid().getAttributes();
				if (fluid != null && fluidAttr.canBePlacedInWorld(be.getLevel(), pos, fluid)) {
					int baseFluidLight = fluidAttr.getBlock(be.getLevel(), pos, fluid.getFluid().defaultFluidState()).getLightEmission(level, pos);
					if (baseFluidLight > 0) {
						if (FMLEnvironment.dist.isClient() && canRenderFluid()) {
							return Math.max(state.getLightEmission(level, pos), baseFluidLight);
						}
						
						int fluidLight = Mth.clamp((int) (baseFluidLight * fluid.getAmount() / ConfigHandler.tankCapacity + 0.5), 1, 15);
						return Math.max(state.getLightEmission(level, pos), fluidLight);
					}
				}
			}
		}
		
		return state.getLightEmission(level, pos);
	}
	
	/*@Override
	public int getLightValue(@Nonnull IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos) {
		if (state.getValue(IS_BOTTOM)) {
			final @Nullable BlockEntity tile = world.getBlockEntity(pos);
			if (tile instanceof WellBlockEntity) {
				final @Nullable FluidStack fluid = ((WellBlockEntity) tile).tank.getFluid();
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
	}*/
	
	// TODO getPackedLightmapCoords maybe not needed?
	/*@SideOnly(Side.CLIENT)
	@Override
	public int getPackedLightmapCoords(@Nonnull IBlockState state, @Nonnull IBlockAccess source,
			@Nonnull BlockPos pos) {
		if (canRenderFluid()) {
			final @Nullable BlockEntity tile = source.getBlockEntity(pos);
			if (tile instanceof WellBlockEntity) {
				final @Nullable FluidStack fluid = ((WellBlockEntity) tile).tank.getFluid();
				if (fluid != null && fluid.getFluid().canBePlacedInWorld()) {
					final int fluidLight = fluid.getFluid().getBlock().getDefaultState().getLightValue();
					if (fluidLight > 0)
						return source.getCombinedLight(pos, Math.max(fluidLight, state.getLightValue()));
				}
			}
		}

		return super.getPackedLightmapCoords(state, source, pos);
	}*/

	// TODO collisions - very different, look at lectern
	/*@Override
	public void addCollisionBoxToList(@Nonnull IBlockState state, @Nonnull World worldIn, @Nonnull BlockPos pos,
			@Nonnull AxisAlignedBB entityBox, @Nonnull List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn,
			boolean isActualState) {
		WellCollisions.getCollisionBoxList(state)
				.forEach(aabb -> addCollisionBoxToList(pos, entityBox, collidingBoxes, aabb));
	}*/

	/*@Nullable
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
	}*/

	/*@Nonnull
	@SideOnly(Side.CLIENT)
	@Override
	public AxisAlignedBB getSelectedBoundingBox(@Nonnull IBlockState state, @Nonnull World worldIn,
			@Nonnull BlockPos pos) {
		final boolean isBottom = state.getValue(IS_BOTTOM) != state.getValue(UPSIDE_DOWN);
		return new AxisAlignedBB(0, isBottom ? 0 : -1, 0, 1, isBottom ? 2 : 1, 1).offset(pos);
	}*/
	
	// TODO not sure what the right method is at all
	/*@Nullable
	@Override
	public Boolean isEntityInsideMaterial(@Nonnull IBlockAccess world, @Nonnull BlockPos pos,
			@Nonnull IBlockState state, @Nonnull Entity entity, double yToTest, @Nonnull Material materialIn,
			boolean testingHead) {
		if (!testingHead)
			yToTest = entity.posY;
		final @Nullable BlockEntity tile = world.getBlockEntity(pos);
		if (tile instanceof WellBlockEntity) {
			final @Nullable FluidStack fluid = ((WellBlockEntity) tile).tank.getFluid();
			if (fluid != null && fluid.getFluid().canBePlacedInWorld())
				if (fluid.getFluid().getBlock().getDefaultState().getMaterial() == materialIn)
					return state.getValue(UPSIDE_DOWN)
							? yToTest >= pos.getY() - ConfigHandler.getRenderedFluidHeight(fluid, true)
							: yToTest <= pos.getY() + ConfigHandler.getRenderedFluidHeight(fluid, false);
		}

		return null;
	}*/

	// TODO I don't know what this does nor why it is here nor why I am here
	/*@Nullable
	@Override
	public Boolean isAABBInsideMaterial(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull AxisAlignedBB boundingBox,
			@Nonnull Material materialIn) {
		final @Nullable BlockEntity tile = world.getBlockEntity(pos);
		if (tile instanceof WellBlockEntity) {
			final @Nullable FluidStack fluid = ((WellBlockEntity) tile).tank.getFluid();
			if (fluid != null && fluid.getFluid().canBePlacedInWorld()) {
				if (fluid.getFluid().getBlock().getDefaultState().getMaterial() == materialIn) {
					cachedFluid = fluid;
					return isAABBInsideLiquid(world, pos, boundingBox);
				}
			}
		}

		return null;
	}*/

	/*@Nullable
	@Override
	public Boolean isAABBInsideLiquid(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull AxisAlignedBB boundingBox) {
		if (cachedFluid == null) {
			final @Nullable BlockEntity tile = world.getBlockEntity(pos);
			if (tile instanceof WellBlockEntity) {
				final @Nullable FluidStack fluid = ((WellBlockEntity) tile).tank.getFluid();
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
	}*/
	
	/*@Override
	public float getBlockLiquidHeight(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state,
			@Nonnull Material material) {
		final @Nullable BlockEntity tile = world.getBlockEntity(pos);
		if (tile instanceof WellBlockEntity) {
			final @Nullable FluidStack fluid = ((WellBlockEntity) tile).tank.getFluid();
			if (fluid != null && fluid.getFluid().canBePlacedInWorld())
				if (fluid.getFluid().getBlock().getDefaultState().getMaterial() == material)
					return ConfigHandler.getRenderedFluidHeight(fluid, false);
		}

		return 0;
	}*/

	// TODO but is this the same as randomDisplayTick?? also playSound vs playLocalSound
	@Override
	public void randomTick(BlockState state, ServerLevel level, BlockPos pos, Random random) {
		BlockEntity be = level.getBlockEntity(pos);
		if (be instanceof WellBlockEntity) {
			FluidStack fluid = ((WellBlockEntity) be).tank.getFluid();
			if (fluid != null && fluid.getFluid().getAttributes().canBePlacedInWorld(level, pos, fluid)) {
				float height = ConfigHandler.getRenderedFluidHeight(fluid, false);
				BlockState fluidState = fluid.getFluid().getAttributes().getBlock(level, pos, fluid.getFluid().defaultFluidState())
						.setValue(LiquidBlock.LEVEL, 8 - (int) (height * 8));
				fluidState.getBlock().randomTick(fluidState, level, pos, random);
				
				// get around lava particle check
				if (fluid.getFluid() == Fluids.LAVA) {
					if (random.nextInt(100) == 0) {
						double x = pos.getX() + random.nextFloat();
						double y = pos.getY() + height;
						double z = pos.getZ() + random.nextFloat();
						level.addParticle(ParticleTypes.LAVA, x, y, z, 0, 0, 0);
						level.playLocalSound(x, y, z, SoundEvents.LAVA_POP, SoundSource.BLOCKS,
								0.2F + random.nextFloat() * 0.2F, 0.9F + random.nextFloat() * 0.15F, false);
					}
					
					if (random.nextInt(200) == 0) {
						double x = pos.getX() + 0.5;
						double y = pos.getY() + height / 2;
						double z = pos.getZ() + 0.5;
						level.playLocalSound(x, y, z, SoundEvents.LAVA_AMBIENT, SoundSource.BLOCKS,
								0.2F + random.nextFloat() * 0.2F, 0.9F + random.nextFloat() * 0.15F, false);
					}
				}
			}
		}
	}
	
	/*@SideOnly(Side.CLIENT)
	@Override
	public void randomDisplayTick(@Nonnull IBlockState stateIn, @Nonnull World worldIn, @Nonnull BlockPos pos,
			@Nonnull Random rand) {
		final @Nullable BlockEntity tile = worldIn.getBlockEntity(pos);
		if (tile instanceof WellBlockEntity) {
			final @Nullable FluidStack fluid = ((WellBlockEntity) tile).tank.getFluid();
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
	}*/

	@Override
	public SoundType getSoundType(BlockState state, LevelReader level, BlockPos pos, Entity entity) {
		// improve the roof sound if possible (some mods change the sound type of bricks to be better)
		return state.getValue(IS_BOTTOM) ? super.getSoundType(state, level, pos, entity) : Blocks.BRICKS.getSoundType(state, level, pos, entity);
	}
	
	/*@Nonnull
	@Override
	public SoundType getSoundType(@Nonnull IBlockState state, @Nonnull World world, @Nonnull BlockPos pos,
			@Nullable Entity entity) {
		// improve the roof sound if possible (some mods change the soundType of bricks
		// to be better)
		return state.getValue(IS_BOTTOM) ? getSoundType() : Blocks.BRICK_BLOCK.getSoundType();
	}*/

	@Override
	public PushReaction getPistonPushReaction(BlockState state) {
		return PushReaction.BLOCK;
	}
	
	/*@Nonnull
	@Override
	public EnumPushReaction getPushReaction(@Nonnull IBlockState state) {
		return EnumPushReaction.BLOCK;
	}*/
	
	// TODO maybe not needed anymore
	/*@Override
	public boolean isFullCube(@Nonnull IBlockState state) {
		return false;
	}

	@Override
	public boolean isOpaqueCube(@Nonnull IBlockState state) {
		return false;
	}*/
	
	// TODO idk what method this goes with
	/*@Nonnull
	@Override
	public BlockFaceShape getBlockFaceShape(@Nonnull IBlockAccess worldIn, @Nonnull IBlockState state,
			@Nonnull BlockPos pos, @Nonnull EnumFacing face) {
		return state.getValue(IS_BOTTOM) && face != EnumFacing.UP ? BlockFaceShape.SOLID : BlockFaceShape.UNDEFINED;
	}*/
	
	// TODO idk this one either
	/*@Override
	public boolean isSideSolid(@Nonnull IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos,
			@Nonnull EnumFacing side) {
		return state.getBlockFaceShape(world, pos, side) == BlockFaceShape.SOLID;
	}*/

	// TODO this one kind of combines the previous two
	@Override
	public boolean hidesNeighborFace(BlockGetter level, BlockPos pos, BlockState state, BlockState neighborState, Direction dir) {
		return state.getValue(IS_BOTTOM) && dir != Direction.UP;
	}
	
	/*@Override
	public boolean doesSideBlockRendering(@Nonnull IBlockState state, @Nonnull IBlockAccess world,
			@Nonnull BlockPos pos, @Nonnull EnumFacing face) {
		return state.isSideSolid(world, pos, face);
	}*/
	
	// TODO not sure about this one
	/*@Override
	public boolean canRenderInLayer(@Nonnull IBlockState state, @Nonnull BlockRenderLayer layer) {
		return layer == BlockRenderLayer.SOLID || state.getValue(IS_BOTTOM) && layer == BlockRenderLayer.TRANSLUCENT;
	}*/

	public boolean canRenderFluid() {
		return MinecraftForgeClient.getRenderType() == RenderType.translucent();
	}
	
	/*public boolean canRenderFluid() {
		return MinecraftForgeClient.getRenderLayer() == BlockRenderLayer.TRANSLUCENT;
	}*/

	// TODO I don't think extended states are a thing anymore?
	/*@Nonnull
	@Override
	public IBlockState getExtendedState(@Nonnull IBlockState state, @Nonnull IBlockAccess world,
			@Nonnull BlockPos pos) {
		if (state instanceof IExtendedBlockState) {
			final @Nullable BlockEntity tile = world.getBlockEntity(pos);
			if (tile instanceof WellBlockEntity)
				state = ((IExtendedBlockState) state).withProperty(FluidUnlistedProperty.INSTANCE,
						((WellBlockEntity) tile).tank.getFluid());
		}

		return state;
	}*/
}
