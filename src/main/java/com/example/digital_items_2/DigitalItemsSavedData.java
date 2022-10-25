package com.example.digital_items_2;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

public class DigitalItemsSavedData extends SavedData {
    public HashMap<ByteBuffer, DigitizedItem> digitizedItems = new HashMap<>();

    private static DigitalItemsSavedData instance;

    @NotNull
    public static DigitalItemsSavedData getFrom(Level l) {
        if(!(l instanceof ServerLevel sl)) {
            throw new IllegalCallerException("may only be called server side!");
        }
        if(instance != null) {
            return instance;
        }
        instance = Objects.requireNonNull(Objects.requireNonNull(sl.getServer()).overworld().getDataStorage().computeIfAbsent(DigitalItemsSavedData::load, DigitalItemsSavedData::create, DigitalItems2.MODID));
        instance.prune(l);
        return instance;
    }

    @NotNull
    public static DigitalItemsSavedData create() {
        return new DigitalItemsSavedData();
    }

    // remove digitized items which decayed; but only if decay is enabled
    public void prune(Level l) {
        if(!Config.DECAY.enabled.get()) {
            return;
        }
        long currentTime = l.getGameTime();
        Iterator<Map.Entry<ByteBuffer, DigitizedItem>> it = digitizedItems.entrySet().iterator();
        it.forEachRemaining(digitizedItemEntry -> {
            if(currentTime >= digitizedItemEntry.getValue().decaysAt) {
                it.remove();
            }
        });
        setDirty();
    }

    @NotNull
    public static DigitalItemsSavedData load(CompoundTag tag) {
        DigitalItemsSavedData data = create();
        if(tag.contains("items") && tag.get("items") instanceof ListTag) {
            ListTag list = (ListTag) Objects.requireNonNull(tag.get("items"));
            list.forEach(tag1 -> {
                DigitizedItem di = new DigitizedItem((CompoundTag) Objects.requireNonNull(tag1));
                data.digitizedItems.put(di.id, di);
            });
        }
        return data;
    }


    @Override
    @NotNull
    public CompoundTag save(CompoundTag tag) {
        ListTag items = new ListTag();
        digitizedItems.values().forEach(digitizedItem -> {
            CompoundTag digitizedItemTag = new CompoundTag();
            digitizedItem.serialize(digitizedItemTag);
            items.add(digitizedItemTag);
        });
        tag.put("items", items);
        return tag;
    }
}
