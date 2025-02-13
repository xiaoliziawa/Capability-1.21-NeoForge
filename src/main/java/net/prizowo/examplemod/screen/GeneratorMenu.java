package net.prizowo.examplemod.screen;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.neoforge.items.SlotItemHandler;
import net.prizowo.examplemod.block.entity.GeneratorDevice;
import net.prizowo.examplemod.registry.ModBlocks;
import net.prizowo.examplemod.registry.ModMenuTypes;
import org.jetbrains.annotations.NotNull;
import net.minecraft.core.BlockPos;

public class GeneratorMenu extends AbstractContainerMenu {
    private final GeneratorDevice blockEntity;
    private final ContainerData data;

    public GeneratorMenu(int containerId, Inventory inventory, BlockEntity entity, ContainerData data) {
        super(ModMenuTypes.GENERATOR_MENU.get(), containerId);
        this.blockEntity = (GeneratorDevice) entity;
        this.data = data;


        addSlot(new SlotItemHandler(blockEntity.getItemHandler(), 0, 80, 35));

        for(int row = 0; row < 3; row++) {
            for(int col = 0; col < 9; col++) {
                addSlot(new Slot(inventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }

        for(int col = 0; col < 9; col++) {
            addSlot(new Slot(inventory, col, 8 + col * 18, 142));
        }

        addDataSlots(data);
    }

    public GeneratorMenu(int containerId, Inventory inventory, FriendlyByteBuf extraData) {
        super(ModMenuTypes.GENERATOR_MENU.get(), containerId);
        BlockPos pos = extraData.readBlockPos();
        BlockEntity entity = inventory.player.level().getBlockEntity(pos);
        this.blockEntity = (GeneratorDevice) entity;
        this.data = ((GeneratorDevice) entity).getContainerData();
        
        addSlot(new SlotItemHandler(blockEntity.getItemHandler(), 0, 80, 35));

        for(int row = 0; row < 3; row++) {
            for(int col = 0; col < 9; col++) {
                addSlot(new Slot(inventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }

        for(int col = 0; col < 9; col++) {
            addSlot(new Slot(inventory, col, 8 + col * 18, 142));
        }

        addDataSlots(data);
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot.hasItem()) {
            ItemStack stack = slot.getItem();
            itemstack = stack.copy();

            if (index == 0) {
                if (!this.moveItemStackTo(stack, 1, 37, true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (this.moveItemStackTo(stack, 0, 1, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (stack.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (stack.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, stack);
        }

        return itemstack;
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return stillValid(ContainerLevelAccess.create(blockEntity.getLevel(),
            blockEntity.getBlockPos()), player, ModBlocks.GENERATOR.get());
    }

    public int getBurnTime() {
        return data.get(0);
    }

    public int getTotalBurnTime() {
        return data.get(1);
    }

    public int getEnergy() {
        return data.get(2);
    }

    public int getMaxEnergy() {
        return data.get(3);
    }
} 