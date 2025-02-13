package net.prizowo.examplemod.screen;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.SlotItemHandler;
import net.prizowo.examplemod.block.entity.BatteryDevice;
import net.prizowo.examplemod.registry.ModBlocks;
import net.prizowo.examplemod.registry.ModMenuTypes;
import org.jetbrains.annotations.NotNull;

public class BatteryMenu extends AbstractContainerMenu {
    private final ContainerData data;
    private final BlockEntity blockEntity;

    private static final int CHARGE_SLOT = 0;
    private static final int PLAYER_INVENTORY_START = 1;
    private static final int PLAYER_HOTBAR_START = 28;
    private static final int PLAYER_HOTBAR_END = 37;

    private static final int CHARGE_SLOT_X = 116;
    private static final int CHARGE_SLOT_Y = 35;
    private static final int INVENTORY_START_X = 8;
    private static final int INVENTORY_START_Y = 84;
    private static final int HOTBAR_START_Y = 142;
    private static final int SLOT_SIZE = 18;

    public BatteryMenu(int containerId, Inventory inventory, BlockEntity entity, ContainerData data) {
        super(ModMenuTypes.BATTERY_MENU.get(), containerId);
        this.blockEntity = entity;
        this.data = data;
        addDataSlots(this.data);

        addChargeSlot((BatteryDevice) entity);
        addPlayerInventory(inventory);
        addPlayerHotbar(inventory);
    }

    public BatteryMenu(int containerId, Inventory inventory, FriendlyByteBuf extraData) {
        super(ModMenuTypes.BATTERY_MENU.get(), containerId);
        BlockPos pos = extraData.readBlockPos();
        this.blockEntity = inventory.player.level().getBlockEntity(pos);
        
        if (blockEntity instanceof BatteryDevice device) {
            this.data = device.getContainerData();
        } else {
            throw new IllegalStateException("Wrong block entity!");
        }
        addDataSlots(this.data);

        addChargeSlot((BatteryDevice) blockEntity);
        addPlayerInventory(inventory);
        addPlayerHotbar(inventory);
    }

    private void addChargeSlot(BatteryDevice device) {
        addSlot(new SlotItemHandler(device.getChargeSlotHandler(), 0, CHARGE_SLOT_X, CHARGE_SLOT_Y) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return stack.getCapability(Capabilities.EnergyStorage.ITEM) != null;
            }
        });
    }

    private void addPlayerInventory(Inventory inventory) {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(inventory, 
                    col + row * 9 + 9, 
                    INVENTORY_START_X + col * SLOT_SIZE, 
                    INVENTORY_START_Y + row * SLOT_SIZE));
            }
        }
    }

    private void addPlayerHotbar(Inventory inventory) {
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(inventory, col, 
                INVENTORY_START_X + col * SLOT_SIZE, 
                HOTBAR_START_Y));
        }
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return stillValid(ContainerLevelAccess.create(blockEntity.getLevel(),
                blockEntity.getBlockPos()), player, ModBlocks.BATTERY.get());
    }

    public int getEnergy() {
        return data.get(0);
    }

    public int getMaxEnergy() {
        return data.get(1);
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            itemstack = slotStack.copy();

            if (index == CHARGE_SLOT) {
                if (!moveToPlayerInventory(slotStack)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickCraft(slotStack, itemstack);
            } else {
                if (!moveToChargeSlotOrPlayerInventory(index, slotStack)) {
                    return ItemStack.EMPTY;
                }
            }

            if (slotStack.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (slotStack.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, slotStack);
        }

        return itemstack;
    }

    private boolean moveToPlayerInventory(ItemStack stack) {
        return moveItemStackTo(stack, PLAYER_INVENTORY_START, PLAYER_HOTBAR_END, true);
    }

    private boolean moveToChargeSlotOrPlayerInventory(int index, ItemStack stack) {
        if (stack.getCapability(Capabilities.EnergyStorage.ITEM) != null) {
            return moveItemStackTo(stack, CHARGE_SLOT, CHARGE_SLOT + 1, false);
        } else if (index < PLAYER_HOTBAR_START) {
            return moveItemStackTo(stack, PLAYER_HOTBAR_START, PLAYER_HOTBAR_END, false);
        } else {
            return moveItemStackTo(stack, PLAYER_INVENTORY_START, PLAYER_HOTBAR_START, false);
        }
    }
} 