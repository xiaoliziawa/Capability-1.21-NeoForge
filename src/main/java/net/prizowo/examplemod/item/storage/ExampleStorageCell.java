package net.prizowo.examplemod.item.storage;

import appeng.api.stacks.AEKeyType;
import appeng.items.storage.BasicStorageCell;
import appeng.me.cells.BasicCellInventory;
import appeng.util.Platform;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class ExampleStorageCell extends BasicStorageCell {
    private final int bytes;
    private final int types;
    
    public ExampleStorageCell(Item.Properties properties, double idleDrain, int bytes, int bytesPerType, int totalTypes, AEKeyType keyType) {
        super(properties, idleDrain, bytes / 1024, bytesPerType, totalTypes, keyType);
        this.bytes = bytes;
        this.types = totalTypes;
    }


    @Override
    public boolean isStorageCell(ItemStack itemStack) {
        return true;
    }

    @Override
    public boolean storableInStorageCell() {
        return false;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> lines, TooltipFlag advancedTooltips) {
        if (Platform.isClient()) {
            this.addCellInformationToTooltip(stack, lines);
        }
    }

    @Override
    public int getBytes(ItemStack cellItem) {
        return this.bytes;
    }

    @Override
    public int getTotalTypes(ItemStack cellItem) {
        return this.types;
    }
} 