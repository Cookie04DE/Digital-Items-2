package com.example.digital_items_2;

import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.shared.peripheral.generic.data.ItemData;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

public class DigitizerPeripheral implements IPeripheral {

    public static final SecureRandom rand = new SecureRandom();

    @NotNull
    private final DigitizerBlockEntity blockEntity;

    public DigitizerPeripheral(@NotNull DigitizerBlockEntity blockEntity) {
        this.blockEntity = blockEntity;
    }

    private void requireEnergy(int requiredEnergy) throws LuaException {
        if(!Config.ENERGY.enabled.get()) {
            return;
        }
        if(blockEntity.energy.getEnergyStored() < requiredEnergy) {
            throw new LuaException("Not enough energy! Requires at least: " + requiredEnergy);
        }
        blockEntity.energy.setEnergyStored(blockEntity.energy.getEnergyStored() - requiredEnergy);
    }

    private DigitizedItem checkID(DigitalItemsSavedData sd, ByteBuffer id) throws LuaException {
        DigitizedItem item = sd.digitizedItems.get(id);
        if(item == null || item.decayed(blockEntity.getLevel())) {
            if(sd.digitizedItems.remove(id) != null) {
                sd.setDirty();
            }
            throw new LuaException("Invalid item ID");
        }
        return item;
    }

    @NotNull
    @Override
    public String getType() {
        return "digitizer";
    }

    @LuaFunction(mainThread = true)
    public final int getEnergy() {
        return blockEntity.energy.getEnergyStored();
    }

    @LuaFunction(mainThread = true)
    public final int getEnergyCapacity() {
        return blockEntity.energy.getMaxEnergyStored();
    }

    @LuaFunction(mainThread = true)
    public final int getPerTickEnergyDraw() {
        return Config.ENERGY.perTickDraw.get();
    }

    @LuaFunction(mainThread = true)
    public final boolean getEnergyRequired() {
        return Config.ENERGY.enabled.get();
    }

    @LuaFunction(mainThread = true)
    public final boolean getDecayEnabled() {
        return Config.DECAY.enabled.get();
    }

    @LuaFunction(mainThread = true)
    public final long getDecayTicks() {
        return Config.DECAY.decayTicks.get();
    }

    @LuaFunction(mainThread = true)
    public final int getDigitizeCost() {
        return Config.ENERGY.digitizationCost.get();
    }

    @LuaFunction(mainThread = true)
    public final byte[] digitize() throws LuaException {
        return digitizeAmount(blockEntity.inventory.getStackInSlot(0).getCount());
    }

    @LuaFunction(mainThread = true)
    public final byte[] digitizeAmount(int amount) throws LuaException {
        ItemStack item = blockEntity.inventory.getStackInSlot(0);
        if(amount <= 0) {
            throw new LuaException("Invalid amount");
        }
        if(item.getCount() < amount) {
            throw new LuaException("Fewer items present than requested for digitization");
        }
        requireEnergy(Config.ENERGY.digitizationCost.get());
        byte[] id = new byte[16];
        rand.nextBytes(id);
        DigitalItemsSavedData data = DigitalItemsSavedData.getFrom(blockEntity.getLevel());
        data.digitizedItems.put(ByteBuffer.wrap(id), new DigitizedItem(id, blockEntity.inventory.extractItem(0, amount, false).copy(), blockEntity.getLevel().getGameTime()));
        item.setCount(item.getCount() - amount);
        if(item.getCount() != 0) {
            blockEntity.inventory.setStackInSlot(0, item);
        } else {
            blockEntity.inventory.setStackInSlot(0, ItemStack.EMPTY);
        }
        data.setDirty();
        return id;
    }

    @LuaFunction(mainThread = true)
    public final int getRematerializeCost() {
        return Config.ENERGY.rematerializationCost.get();
    }

    @LuaFunction(mainThread = true)
    public final void rematerialize(ByteBuffer id) throws LuaException {
        DigitalItemsSavedData sd = DigitalItemsSavedData.getFrom(blockEntity.getLevel());
        DigitizedItem item = checkID(sd, id);
        rematerializeAmount(id, item.item.getCount());
    }

    @LuaFunction(mainThread = true)
    public final void rematerializeAmount(ByteBuffer id, int amount) throws LuaException {
        DigitalItemsSavedData sd = DigitalItemsSavedData.getFrom(blockEntity.getLevel());
        DigitizedItem item = checkID(sd, id);
        if(amount <= 0) {
            throw new LuaException("Invalid amount");
        }
        if(item.item.getCount() < amount) {
            throw new LuaException("Fewer items present in ID than requested for rematerialization");
        }
        ItemStack limitedAmount = item.item.copy();
        limitedAmount.setCount(amount);

        ItemStack remaining = blockEntity.inventory.insertItem(0, limitedAmount, true);
        if(remaining.getCount() != 0) {
            throw new LuaException("Failed to merge items inside the digitizer already with the rematerialized items");
        }
        requireEnergy(Config.ENERGY.rematerializationCost.get());
        blockEntity.inventory.insertItem(0, limitedAmount, false);
        item.item.setCount(item.item.getCount() - amount);
        if(item.item.getCount() == 0) {
            sd.digitizedItems.remove(id);
        } else {
            long currentTime = blockEntity.getLevel().getGameTime();
            item.lastRefresh = currentTime;
            item.decaysAt = currentTime + Config.DECAY.decayTicks.get();
        }
        sd.setDirty();
    }

    @LuaFunction(mainThread = true)
    public final int getRefreshCost() {
        return Config.ENERGY.refreshCost.get();
    }

    @LuaFunction(mainThread = true)
    public final void refresh(ByteBuffer id) throws LuaException {
        DigitalItemsSavedData sd = DigitalItemsSavedData.getFrom(blockEntity.getLevel());
        DigitizedItem item = checkID(sd, id);
        requireEnergy(Config.ENERGY.refreshCost.get());
        long currentTime = blockEntity.getLevel().getGameTime();
        item.lastRefresh = currentTime;
        item.decaysAt = currentTime + Config.DECAY.decayTicks.get();
        sd.setDirty();
    }

    @LuaFunction(mainThread = true)
    public final int getInfoCost() {
        return Config.ENERGY.idInfo.get();
    }

    @LuaFunction(mainThread = true)
    public final Map<String, Object> getIDInfo(ByteBuffer id) throws LuaException {
        DigitalItemsSavedData sd = DigitalItemsSavedData.getFrom(blockEntity.getLevel());
        DigitizedItem item = checkID(sd, id);
        requireEnergy(Config.ENERGY.idInfo.get());
        HashMap<String, Object> root = new HashMap<>();
        long currentTime = blockEntity.getLevel().getGameTime();
        root.put("currentTime", currentTime);
        root.put("digitizedAt", item.digitizedAt);
        root.put("decaysAt", item.decaysAt);
        root.put("lastRefresh", item.lastRefresh);
        HashMap<String, Object> itemData = new HashMap<>();
        ItemData.fill(itemData, item.item);
        root.put("item", itemData);
        return root;
    }

    @LuaFunction(mainThread = true)
    public final Map<String, Object> getItemDetail() {
        HashMap<String, Object> itemData = new HashMap<>();
        ItemData.fill(itemData, blockEntity.inventory.getStackInSlot(0));
        return itemData;
    }

    @LuaFunction(mainThread = true)
    public final int getItemLimit() {
        return blockEntity.inventory.getSlotLimit(0);
    }

    @Override
    public boolean equals(@Nullable IPeripheral other) {
        return other instanceof DigitizerPeripheral && ((DigitizerPeripheral) other).blockEntity == blockEntity;
    }
}
