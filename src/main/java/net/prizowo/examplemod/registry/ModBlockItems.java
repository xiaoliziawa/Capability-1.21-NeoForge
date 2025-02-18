package net.prizowo.examplemod.registry;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.prizowo.examplemod.ExampleMod;

import java.util.function.Supplier;

public class ModBlockItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(BuiltInRegistries.ITEM, ExampleMod.MODID);

    public static final Supplier<Item> FLUID_TANK = ITEMS.register("fluid_tank",
            () -> new BlockItem(ModBlocks.FLUID_TANK.get(), new Item.Properties()));

    public static final Supplier<Item> GENERATOR = ITEMS.register("generator",
            () -> new BlockItem(ModBlocks.GENERATOR.get(), new Item.Properties()));

    public static final Supplier<Item> BATTERY = ITEMS.register("battery",
            () -> new BlockItem(ModBlocks.BATTERY.get(), new Item.Properties()));

    public static final Supplier<Item> CHEMICAL_PIPE = ITEMS.register("chemical_pipe",
            () -> new BlockItem(ModBlocks.CHEMICAL_PIPE.get(), new Item.Properties()));

    public static final Supplier<Item> CREATIVE_CONTROLLER = ITEMS.register("creative_controller",
            () -> new BlockItem(ModBlocks.CREATIVE_CONTROLLER.get(), new Item.Properties()));
}