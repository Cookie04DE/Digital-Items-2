package com.example.digital_items_2;

import net.minecraftforge.energy.EnergyStorage;

public class SettableEnergyStorage extends EnergyStorage {
    public SettableEnergyStorage(int capacity) {
        super(capacity);
    }

    public SettableEnergyStorage(int capacity, int maxTransfer) {
        super(capacity, maxTransfer);
    }

    public SettableEnergyStorage(int capacity, int maxReceive, int maxExtract) {
        super(capacity, maxReceive, maxExtract);
    }

    public SettableEnergyStorage(int capacity, int maxReceive, int maxExtract, int energy) {
        super(capacity, maxReceive, maxExtract, energy);
    }

    public void setEnergyStored(int energy) {
        this.energy = energy;
    }
}
