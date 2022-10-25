package com.example.digital_items_2;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.nio.ByteBuffer;
import java.util.Objects;

public class DigitizedItem {
    public ByteBuffer id;

    public ItemStack item;

    public long digitizedAt;
    public long lastRefresh;
    public long decaysAt;

    public DigitizedItem(byte[] id, ItemStack item, long currentTime) {
        this.id = ByteBuffer.wrap(id);
        this.item = item;
        this.digitizedAt = currentTime;
        this.lastRefresh = currentTime;
        this.decaysAt = currentTime + Config.DECAY.decayTicks.get();
    }

    public DigitizedItem(CompoundTag compoundTag) {
        id = ByteBuffer.wrap(compoundTag.getByteArray("id"));
        item = ItemStack.of((CompoundTag) Objects.requireNonNull(compoundTag.get("itemStack")));
        digitizedAt = compoundTag.getLong("digitizedAt");
        lastRefresh = compoundTag.getLong("lastRefresh");
        decaysAt = compoundTag.getLong("decaysAt");
    }

    private static byte[] getBytes(ByteBuffer buf) {
        if(buf.hasArray()) {
            return buf.array();
        }
        byte[] bytes = new byte[buf.remaining()];
        buf.get(bytes);
        return bytes;
    }

    public void serialize(CompoundTag compoundTag) {
        compoundTag.putByteArray("id", getBytes(id));
        compoundTag.put("itemStack", item.serializeNBT());
        compoundTag.putLong("digitizedAt", digitizedAt);
        compoundTag.putLong("lastRefresh", lastRefresh);
        compoundTag.putLong("decaysAt", decaysAt);
    }

    public boolean decayed(Level level) {
        if(!Config.DECAY.enabled.get()) {
            return false;
        }
        return decaysAt <= Objects.requireNonNull(level.getServer()).overworld().getGameTime();
    }
}
