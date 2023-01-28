package cubicoder.well.block.entity;

import java.util.function.Consumer;

import cubicoder.well.block.ModBlocks;
import cubicoder.well.block.WellBlock;
import cubicoder.well.config.ConfigHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.TileFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;

public class WellBlockEntity extends TileFluidHandler {
	
	//public final FluidTankSynced tank = new FluidTankSynced(this, ConfigHandler.tankCapacity);
	public long fillTick = 0;
	public int nearbyWells = 1;
	public int delayUntilNextBucket = 0;
	public boolean initialized;
	protected Biome biome;
	
	public WellBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlocks.WELL_BE.get(), pos, state);
		tank = new FluidTankSynced(this, ConfigHandler.tankCapacity);
	}
	
	public void tick() {
		if (!initialized) {
			onLoad();
		}
		if (delayUntilNextBucket > 0) {
			delayUntilNextBucket--;
		}
		if (initialized && hasLevel() && !level.isClientSide && fillTick <= level.getGameTime()
				&& ConfigHandler.canGenerateFluid(nearbyWells)) {
			FluidStack fluidToFill = getFluidToFill();
			if (fluidToFill != null) tank.fill(fluidToFill, FluidAction.EXECUTE);
			initFillTick();
		}
		
	}
	
	/*@Override
	public void update() {
		if (!initialized)
			onLoad();
		if (delayUntilNextBucket > 0)
			delayUntilNextBucket--;
		if (initialized && hasWorld() && !world.isRemote && fillTick <= world.getTotalWorldTime()
				&& ConfigHandler.canGenerateFluid(nearbyWells)) {
			final FluidStack fluidToFill = getFluidToFill();
			if (fluidToFill != null)
				tank.fill(fluidToFill, true);
			initFillTick();
		}
	}*/
	
	@Override
	public void onLoad() {
		if (!initialized) {
			initialized = true;
			if (!level.isClientSide) {
				initFillTick();
				countNearbyWells(be -> {
					be.nearbyWells++;
					nearbyWells++;
				});
			}
		}

		if (((FluidTankSynced) tank).updateLight(tank.getFluid())) level.setBlocksDirty(getBlockPos(), getBlockState(), getBlockState());
	}

	/*@Override
	public void onLoad() {
		if (!initialized) {
			initialized = true;
			if (!world.isRemote) {
				initFillTick();
				countNearbyWells(te -> {
					te.nearbyWells++;
					nearbyWells++;
				});
			}
		}

		if (tank.updateLight(tank.getFluid()))
			world.markBlockRangeForRenderUpdate(pos, pos);
	}*/
	
	protected FluidStack getFluidToFill() {
		return ConfigHandler.getFillFluid(getBiome(), level, isUpsideDown(), nearbyWells);
	}

	/*@Nullable
	protected FluidStack getFluidToFill() {
		return ConfigHandler.getFillFluid(getBiome(), world, isUpsideDown(), nearbyWells);
	}*/

	protected void initFillTick() {
		fillTick = level.getGameTime() + ConfigHandler.getFillDelay(getBiome(), level.random, isUpsideDown());
	}
	
	/*protected void initFillTick() {
		fillTick = world.getTotalWorldTime() + ConfigHandler.getFillDelay(getBiome(), world.rand, isUpsideDown());
	}*/

	public void countNearbyWells(Consumer<WellBlockEntity> updateScript) {
		BlockPos.betweenClosed(getBlockPos().offset(-15, -15, -15), getBlockPos().offset(15, 15, 15)).forEach(otherPos -> {
			if (!otherPos.equals(getBlockPos()) && level.getBiome(otherPos).value() == getBiome()) {
				final BlockEntity be = level.getBlockEntity(otherPos);
				if (be instanceof WellBlockEntity && isUpsideDown(be) == isUpsideDown()) {
					updateScript.accept((WellBlockEntity) be);
				}
			}
		});
	}
	
	/*public void countNearbyWells(@Nonnull Consumer<WellBlockEntity> updateScript) {
		BlockPos.getAllInBox(pos.add(-15, -15, -15), pos.add(15, 15, 15)).forEach(otherPos -> {
			if (!otherPos.equals(pos) && world.getBiome(otherPos) == getBiome()) {
				final @Nullable TileEntity tile = world.getTileEntity(otherPos);
				if (tile instanceof WellBlockEntity && isUpsideDown(tile) == isUpsideDown())
					updateScript.accept((WellBlockEntity) tile);
			}
		});
	}*/

	public boolean isUpsideDown() {
		return this.getBlockState().getValue(WellBlock.UPSIDE_DOWN);
	}
	
	/*public boolean isUpsideDown() {
		return (getBlockMetadata() >> 1 & 1) == 1;
	}*/

	public static boolean isUpsideDown(BlockEntity be) {
		return be instanceof WellBlockEntity && ((WellBlockEntity) be).isUpsideDown();
	}
	
	@Override
	public void setChanged() {
		if (hasLevel()) biome = level.getBiome(getBlockPos()).value();
		super.setChanged();
	}
	
	/*@Override
	public void markDirty() {
		if (hasWorld())
			biome = world.getBiome(pos);
		super.markDirty();
	}*/
	
	public Biome getBiome() {
		return biome == null ? (biome = level.getBiome(getBlockPos()).value()) : biome;
	}

	/*@Nonnull
	public Biome getBiome() {
		return biome == null ? (biome = world.getBiome(pos)) : biome;
	}*/

	@Override
	public CompoundTag getUpdateTag() {
		// TODO is this right?
		return saveWithoutMetadata();
	}
	
	/*@Nonnull
	@Override
	public NBTTagCompound getUpdateTag() {
		return writeToNBT(new NBTTagCompound());
	}*/

	@Override
	public Packet<ClientGamePacketListener> getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}
	
	/*@Nullable
	@Override
	public SPacketUpdateTileEntity getUpdatePacket() {
		return new SPacketUpdateTileEntity(pos, 0, getUpdateTag());
	}*/

	@Override
	public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
		FluidStack oldFluid = tank.getFluid();
		handleUpdateTag(pkt.getTag());
		FluidStack newFluid = tank.getFluid();
		
		boolean wasEmpty = newFluid != null && oldFluid == null;
		boolean wasFull = newFluid == null && oldFluid != null;

		// update renderer and light level if needed
		if (wasEmpty || wasFull || newFluid != null && newFluid.getAmount() != oldFluid.getAmount()) {
			if (newFluid != null) ((FluidTankSynced) tank).updateLight(newFluid);
			else ((FluidTankSynced) tank).updateLight(oldFluid);
			level.setBlocksDirty(getBlockPos(), getBlockState(), getBlockState());
		}
	}
	
	/*@Override
	public void onDataPacket(@Nonnull NetworkManager net, @Nonnull SPacketUpdateTileEntity pkt) {
		final @Nullable FluidStack oldFluid = tank.getFluid();
		handleUpdateTag(pkt.getNbtCompound());
		final @Nullable FluidStack newFluid = tank.getFluid();

		final boolean wasEmpty = newFluid != null && oldFluid == null;
		final boolean wasFull = newFluid == null && oldFluid != null;

		// update renderer and light level if needed
		if (wasEmpty || wasFull || newFluid != null && newFluid.amount != oldFluid.amount) {
			if (newFluid != null)
				tank.updateLight(newFluid);
			else
				tank.updateLight(oldFluid);
			world.markBlockRangeForRenderUpdate(pos, pos);
		}
	}*/
	
	@Override
	public void load(CompoundTag tag) {
		super.load(tag);
		tank.readFromNBT(tag);
		fillTick = tag.getLong("fillTick");
		initialized = tag.getBoolean("initialized");
		nearbyWells = Math.max(1, tag.getInt("nearbyWells"));
	}
	
	/*@Override
	public void readFromNBT(@Nonnull NBTTagCompound tag) {
		super.readFromNBT(tag);
		tank.readFromNBT(tag);
		fillTick = tag.getLong("fillTick");
		initialized = tag.getBoolean("initialized");
		nearbyWells = Math.max(1, tag.getInteger("nearbyWells"));
	}*/

	@Override
	protected void saveAdditional(CompoundTag tag) {
		super.saveAdditional(tag);
		tank.writeToNBT(tag);
		tag.putLong("fillTick", fillTick);
		tag.putBoolean("initialized", initialized);
		tag.putInt("nearbyWells", nearbyWells);
	}
	
	/*@Nonnull
	@Override
	public NBTTagCompound writeToNBT(@Nonnull NBTTagCompound tag) {
		tag = super.writeToNBT(tag);
		tank.writeToNBT(tag);
		tag.setLong("fillTick", fillTick);
		tag.setBoolean("initialized", initialized);
		tag.setInteger("nearbyWells", nearbyWells);
		return tag;
	}*/
	
	// TODO no equivalent / might not be needed anymore
	/*@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		return capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY || super.hasCapability(capability, facing);
	}*/

	// TODO shouldn't need to override since TileFluidHandler takes care of this for us
	/*@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
		if (cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) return (LazyOptional<T>) tank;
		return super.getCapability(cap, side);
	}*/
	
	/*@SuppressWarnings("unchecked")
	@Nullable
	@Override
	public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
		if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)
			return (T) tank;
		return super.getCapability(capability, facing);
	}*/

	public FluidTank getTank() {
		return tank;
	}
	
	public static class FluidTankSynced extends FluidTank {

		private BlockEntity be;
		
		public FluidTankSynced(BlockEntity be, int capacity) {
			super(capacity);
			this.be = be;
			setValidator(fluid -> {
				// well is upside down, only allow upside down fluids, or vice versa
				if (WellBlockEntity.isUpsideDown(be)) {
					if (!fluid.getFluid().getAttributes().isLighterThanAir()) return false;
				} else if (fluid.getFluid().getAttributes().isLighterThanAir()) return false;
				
				// no evaporation
				if (be.getLevel().dimensionType().ultraWarm() 
						&& fluid.getFluid().getAttributes().doesVaporize(be.getLevel(), be.getBlockPos(), fluid)) return false;
				
				return true;
			});
		}
		
		/*@Override
		public boolean canFillFluidType(@Nonnull FluidStack fluid) {
			// well is upside down, only allow upside down fluids
			if (WellBlockEntity.isUpsideDown(be)) {
				if (!fluid.getFluid().isLighterThanAir())
					return false;
			}
			// well is not upside down, only allow non upside down fluids
			else if (fluid.getFluid().isLighterThanAir())
				return false;
			// no evaporation
			if (tile.getWorld().provider.doesWaterVaporize() && fluid.getFluid().doesVaporize(fluid))
				return false;
			return canFill();
		}*/

		@Override
		public int fill(FluidStack resource, FluidAction action) {
			int fill = super.fill(resource, action);
			if (action.execute() && fill > 0) {
				BlockState state = be.getBlockState();
				be.getLevel().sendBlockUpdated(be.getBlockPos(), state, state, Block.UPDATE_ALL);
				updateLight(resource);
			}
			
			return fill;
		}
		
		/*@Override
		public int fillInternal(@Nullable FluidStack resource, boolean doFill) {
			final int fill = super.fillInternal(resource, doFill);
			if (doFill && fill > 0) {
				final IBlockState state = tile.getBlockType().getDefaultState();
				tile.getWorld().notifyBlockUpdate(tile.getPos(), state, state, Constants.BlockFlags.DEFAULT);
				updateLight(resource);
			}

			return fill;
		}*/

		@Override
		public FluidStack drain(int maxDrain, FluidAction action) {
			if (((WellBlockEntity) be).delayUntilNextBucket > 0) return null;
			FluidStack resource = super.drain(maxDrain, action);
			if (resource != null && action.execute()) {
				BlockState state = be.getBlockState();
				be.getLevel().sendBlockUpdated(be.getBlockPos(), state, state, Block.UPDATE_ALL);
				updateLight(resource);
			}
			
			return resource;
		}
		
		/*@Nullable
		@Override
		public FluidStack drainInternal(int maxDrain, boolean doDrain) {
			if (((WellBlockEntity) tile).delayUntilNextBucket > 0)
				return null;
			final @Nullable FluidStack resource = super.drainInternal(maxDrain, doDrain);
			if (resource != null && doDrain) {
				final IBlockState state = tile.getBlockType().getDefaultState();
				tile.getWorld().notifyBlockUpdate(tile.getPos(), state, state, Constants.BlockFlags.DEFAULT);
				updateLight(resource);
			}

			return resource;
		}*/
		
		protected boolean updateLight(FluidStack resource) {
			FluidAttributes attr = resource.getFluid().getAttributes();
			if (resource != null && attr.canBePlacedInWorld(be.getLevel(), be.getBlockPos(), resource)) {
				if (attr.getBlock(be.getLevel(), be.getBlockPos(), fluid.getFluid().defaultFluidState())
						.getLightEmission(be.getLevel(), be.getBlockPos()) > 0) {
					// TODO canSeeSky is definitely not the right thing here
					return be.getLevel().canSeeSky(be.getBlockPos());
				}
			}

			return false;
		}

		/*protected boolean updateLight(@Nullable FluidStack resource) {
			if (resource != null && resource.getFluid().canBePlacedInWorld()) {
				if (resource.getFluid().getBlock().getDefaultState().getLightValue(tile.getWorld(), tile.getPos()) > 0)
					return tile.getWorld().checkLight(tile.getPos());
			}

			return false;
		}*/
	}
}
