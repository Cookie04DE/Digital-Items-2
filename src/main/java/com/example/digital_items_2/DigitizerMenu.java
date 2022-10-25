package com.example.digital_items_2;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public class DigitizerMenu extends AbstractContainerMenu {

    public final DigitizerBlockEntity blockEntity;
    private final Level level;

    public DigitizerMenu(int id, Inventory inv, FriendlyByteBuf extraData) {
        this(id, inv, inv.player.level.getBlockEntity(extraData.readBlockPos()), new SimpleContainerData(8));
    }

    public final ContainerData data;

    public DigitizerMenu(int id, Inventory inv, BlockEntity entity, ContainerData data) {
        super(Registration.DIGITIZER_MENU.get(), id);
        checkContainerSize(inv, 3);
        checkContainerDataCount(data, 8);
        blockEntity = (DigitizerBlockEntity) entity;
        this.level = inv.player.level;
        this.data = data;

        addPlayerInventory(inv);
        addPlayerHotbar(inv);

        this.blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(handler -> addSlot(new SlotItemHandler(handler, 0, 80, 35)));

        addDataSlots(data);
    }

    // 0 - 8 player inventory top row
    // 9 - 17 player inventory middle row
    // 18 - 26 player inventory bottom row
    // 27 - 35 player inventory hot bar
    // 36 digitizer slot

    private static final int playerInvIndex = 0;
    private static final int playerInvLength = 27;

    private static final int playerHotBarIndex = 27;
    private static final int playerHotBarLength = 9;

    private static final int digitizerIndex = 36;
    private static final int digitizerLength = 1;

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player playerIn, int index) {
        Slot source = slots.get(index);
        ItemStack sourceStack = source.getItem();
        ItemStack sourceStackCopy = source.getItem().copy();
        if(!source.hasItem()) {
            return ItemStack.EMPTY; // no item do move; nothing to do...
        }
        if(index == digitizerIndex) {
            if(!moveItemStackTo(sourceStack, playerHotBarIndex, playerHotBarIndex+playerHotBarLength, true)) { // try the hotbar; last to first
                if(!moveItemStackTo(sourceStack, playerInvIndex, playerInvIndex+playerInvLength, true)) { // try the inv; last to first
                    return ItemStack.EMPTY; // neither hotbar nor inv was empty
                }
            }
        } else {
            if(!moveItemStackTo(sourceStack, digitizerIndex, digitizerIndex+digitizerLength, true)) { // try to move the item into the digitizer; order doesn't matter since there's only one slot in the digitizer
                return ItemStack.EMPTY;
            }
        }

        if (sourceStack.getCount() == 0) {
            source.set(ItemStack.EMPTY); // we moved the entire item stack; set the original one to be empty
        } else {
            source.setChanged();
        }
        source.onTake(playerIn, sourceStack);
        return sourceStackCopy;
    }

    // https://github.com/Tutorials-By-Kaupenjoe/Forge-Tutorial-1.19/blob/10137e0ad48cd32477891e79d14a0ed43da67459/src/main/java/net/kaupenjoe/tutorialmod/screen/GemInfusingStationMenu.java

    @Override
    public boolean stillValid(@NotNull Player player) {
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()),
                player, Registration.DIGITIZER_BLOCK.get());
    }

    private void addPlayerInventory(Inventory playerInventory) {
        for (int i = 0; i < 3; i++) {
            for (int l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInventory, l + i * 9 + 9, 8 + l * 18, 86 + i * 18 - 2));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInventory) {
        for (int i = 0; i < 9; i++) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
        }
    }
}
