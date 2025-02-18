package net.prizowo.examplemod.registry;

import appeng.api.stacks.AEKeyType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.prizowo.examplemod.ExampleMod;
import net.prizowo.examplemod.item.storage.ExampleStorageCell;
import net.prizowo.examplemod.item.storage.StorageTier;

import java.util.function.Supplier;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(BuiltInRegistries.ITEM, ExampleMod.MODID);

    private static final Item.Properties CELL_PROPERTIES = new Item.Properties().stacksTo(1);

    // 存储单元
    public static final Supplier<Item> STORAGE_CELL_1M = ITEMS.register("storage_cell_1m",
            () -> new ExampleStorageCell(CELL_PROPERTIES, StorageTier.SIZE_1M.idleDrain(),
                    1024 * 1024, 8, 1024, AEKeyType.items()));
                    
    public static final Supplier<Item> STORAGE_CELL_4M = ITEMS.register("storage_cell_4m",
            () -> new ExampleStorageCell(CELL_PROPERTIES, StorageTier.SIZE_4M.idleDrain(),
                    4 * 1024 * 1024, 32, 1024, AEKeyType.items()));
                    
    public static final Supplier<Item> STORAGE_CELL_16M = ITEMS.register("storage_cell_16m",
            () -> new ExampleStorageCell(CELL_PROPERTIES, StorageTier.SIZE_16M.idleDrain(),
                    16 * 1024 * 1024, 128, 1024, AEKeyType.items()));
                    
    public static final Supplier<Item> STORAGE_CELL_MAX = ITEMS.register("storage_cell_max",
            () -> new ExampleStorageCell(CELL_PROPERTIES, StorageTier.SIZE_MAX.idleDrain(),
                    Integer.MAX_VALUE, 512, 1024, AEKeyType.items()));
    public static final Supplier<Item> STORAGE_CELL_INFINITE = ITEMS.register("storage_cell_infinite_one",
            () -> new ExampleStorageCell(CELL_PROPERTIES, StorageTier.SIZE_1M.idleDrain(),
                    Integer.MAX_VALUE, 512, 0, AEKeyType.items()));
} 