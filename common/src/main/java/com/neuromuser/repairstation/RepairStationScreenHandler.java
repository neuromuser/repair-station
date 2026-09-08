package com.neuromuser.repairstation;

import com.neuromuser.repairstation.config.FuelMatcher;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class RepairStationScreenHandler extends AbstractContainerMenu {
    private final Container inventory;
    private final ContainerData containerData;

    public RepairStationScreenHandler(int syncId, Inventory playerInventory) {
        this(syncId, playerInventory, new SimpleContainer(3), new SimpleContainerData(4));
    }

    public RepairStationScreenHandler(int syncId, Inventory playerInventory, Container inventory, ContainerData containerData) {
        super(RepairStation.REPAIR_STATION_SCREEN_HANDLER, syncId);
        checkContainerSize(inventory, 3);
        checkContainerDataCount(containerData, 4);
        this.inventory = inventory;
        this.containerData = containerData;
        inventory.startOpen(playerInventory.player);

        this.addSlot(new Slot(inventory, 0, 56, 17) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.isDamageableItem() && stack.getDamageValue() > 0;
            }
        });

        this.addSlot(new Slot(inventory, 1, 56, 53) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return FuelMatcher.isFuel(stack);
            }
        });

        this.addSlot(new Slot(inventory, 2, 116, 35) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });

        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }

        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
        }

        this.addDataSlots(containerData);
    }

    public int getFuelProgress() {
        int i = this.containerData.get(1);
        if (i == 0) {
            i = 200;
        }
        return this.containerData.get(0) * 13 / i;
    }

    public int getRepairProgress() {
        int i = this.containerData.get(2);
        int j = this.containerData.get(3);
        return j != 0 && i != 0 ? i * 24 / j : 0;
    }

    public boolean isRepairing() {
        return this.containerData.get(0) > 0;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack originalStack = slot.getItem();
            itemStack = originalStack.copy();

            if (index == 2) {
                if (!this.moveItemStackTo(originalStack, 3, 39, true)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickCraft(originalStack, itemStack);
            } else if (index != 1 && index != 0) {
                if (originalStack.isDamageableItem() && originalStack.getDamageValue() > 0) {
                    if (!this.moveItemStackTo(originalStack, 0, 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (FuelMatcher.isFuel(originalStack)) {
                    if (!this.moveItemStackTo(originalStack, 1, 2, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (index >= 3 && index < 30) {
                    if (!this.moveItemStackTo(originalStack, 30, 39, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (index >= 30 && index < 39 && !this.moveItemStackTo(originalStack, 3, 30, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(originalStack, 3, 39, false)) {
                return ItemStack.EMPTY;
            }

            if (originalStack.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (originalStack.getCount() == itemStack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, originalStack);
        }

        return itemStack;
    }

    @Override
    public boolean stillValid(Player player) {
        return this.inventory.stillValid(player);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.inventory.stopOpen(player);
    }
}