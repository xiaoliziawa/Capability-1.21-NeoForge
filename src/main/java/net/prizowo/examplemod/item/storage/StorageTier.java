package net.prizowo.examplemod.item.storage;

import appeng.core.definitions.AEItems;
import net.minecraft.world.item.Item;

import java.util.function.Supplier;

public record StorageTier(int index, String namePrefix, int bytes, double idleDrain, Supplier<Item> componentSupplier) {
    public static final StorageTier SIZE_1M = new StorageTier(6, "1m", 1048576, 3.0,
            AEItems.CELL_COMPONENT_16K::asItem);
    
    public static final StorageTier SIZE_4M = new StorageTier(7, "4m", 4194304, 3.5,
            AEItems.CELL_COMPONENT_64K::asItem);
    
    public static final StorageTier SIZE_16M = new StorageTier(8, "16m", 16777216, 4.0,
            AEItems.CELL_COMPONENT_256K::asItem);
    
    public static final StorageTier SIZE_MAX = new StorageTier(9, "max", Integer.MAX_VALUE, 5.0,
            AEItems.CELL_COMPONENT_256K::asItem);
} 