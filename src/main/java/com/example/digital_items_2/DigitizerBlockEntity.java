package com.example.digital_items_2;

import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.peripheral.IPeripheralProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class DigitizerBlockEntity extends BlockEntity implements MenuProvider, IPeripheralProvider {
    public final ItemStackHandler inventory = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            Objects.requireNonNull(getLevel()).updateNeighborsAt(getBlockPos(), getBlockState().getBlock());
        }
    };

    private final LazyOptional<IItemHandlerModifiable> invOptional = LazyOptional.of(() -> inventory);

    SimpleContainerData data = new SimpleContainerData(8);

    public void setCurrentEnergy(int energy) {
        data.set(0, (energy & 0xff000000) >> 16);
        data.set(1, (energy & 0x00ff0000) >> 12);
        data.set(2, (energy & 0x0000ff00) >> 8);
        data.set(3, energy & 0x000000ff);
    }

    public void setMaxEnergy(int maxEnergy) {
        data.set(4, (maxEnergy & 0xff000000) >> 16);
        data.set(5, (maxEnergy & 0x00ff0000) >> 12);
        data.set(6, (maxEnergy & 0x0000ff00) >> 8);
        data.set(7, maxEnergy & 0x000000ff);
    }

    public final SettableEnergyStorage energy = new SettableEnergyStorage(Config.ENERGY.maxEnergy.get(), Config.ENERGY.perTickDraw.get()) {
        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            int received = super.receiveEnergy(maxReceive, simulate);
            setCurrentEnergy(getEnergyStored());
            return received;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            int extracted =  super.extractEnergy(maxExtract, simulate);
            setCurrentEnergy(getEnergyStored());
            return extracted;
        }
    };

    private final LazyOptional<IEnergyStorage> energyOptional = Config.ENERGY.enabled.get() ? LazyOptional.of(() -> energy) : LazyOptional.empty();

    private final DigitizerPeripheral peripheral;

    public DigitizerBlockEntity(BlockPos pos, BlockState state) {
        super(Registration.DIGITIZER_BLOCK_ENTITY.get(), pos, state);
        peripheral = new DigitizerPeripheral(this);
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag nbt) {
        super.saveAdditional(nbt);
        nbt.put("inventory", this.inventory.serializeNBT());
        nbt.putInt("energy", this.energy.getEnergyStored());
    }

    @Override
    public void load(@NotNull CompoundTag nbt) {
        super.load(nbt);
        this.inventory.deserializeNBT(nbt.getCompound("inventory"));

        int loaded_energy = nbt.getInt("energy"); // clamp the loaded energy value between 0 and maxEnergy
        if(loaded_energy < 0) {
            loaded_energy = 0;
        }
        if(loaded_energy > energy.getMaxEnergyStored()) {
            loaded_energy = energy.getMaxEnergyStored();
        }
        this.energy.setEnergyStored(loaded_energy);
        setCurrentEnergy(loaded_energy);
    }

    public void updatePoweredState(Level level, BlockPos pos, BlockState state) {
        if(!(level instanceof ServerLevel)) {
            return;
        }
        if(!Config.ENERGY.enabled.get()) {
            level.setBlockAndUpdate(pos, state.setValue(Digitizer.POWERED, true));
            return;
        }
        level.setBlockAndUpdate(pos, state.setValue(Digitizer.POWERED, this.energy.getEnergyStored() >= Config.ENERGY.digitizationCost.get()));
    }

    private static final Capability<IPeripheralProvider> PERIPHERAL_PROVIDER_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});

    private final LazyOptional<IPeripheralProvider> peripheralProviderOptional = LazyOptional.of(() -> this);

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return this.invOptional.cast();
        }
        if (cap == ForgeCapabilities.ENERGY) {
            return this.energyOptional.cast();
        }
        if (cap == PERIPHERAL_PROVIDER_CAPABILITY) {
            return peripheralProviderOptional.cast();
        }

        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        invOptional.invalidate();
        energyOptional.invalidate();
        peripheralProviderOptional.invalidate();
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("block.digital_items_2.digitizer");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, @NotNull Inventory inv, @NotNull Player player) {
        setCurrentEnergy(energy.getEnergyStored());
        setMaxEnergy(energy.getMaxEnergyStored());
        return new DigitizerMenu(id, inv, this, data);
    }

    @NotNull
    @Override
    public LazyOptional<IPeripheral> getPeripheral(@NotNull Level world, @NotNull BlockPos pos, @NotNull Direction side) {
        return LazyOptional.of(() -> peripheral);
    }

}
