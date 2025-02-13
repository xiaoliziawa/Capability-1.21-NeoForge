package net.prizowo.examplemod.registry;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.prizowo.examplemod.ExampleMod;
import net.prizowo.examplemod.screen.GeneratorMenu;
import net.prizowo.examplemod.screen.BatteryMenu;

import java.util.function.Supplier;

public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENU_TYPES = 
        DeferredRegister.create(BuiltInRegistries.MENU, ExampleMod.MODID);

    public static final Supplier<MenuType<GeneratorMenu>> GENERATOR_MENU = 
        MENU_TYPES.register("generator",
            () -> IMenuTypeExtension.create(GeneratorMenu::new));

    public static final Supplier<MenuType<BatteryMenu>> BATTERY_MENU = 
        MENU_TYPES.register("battery",
            () -> IMenuTypeExtension.create(BatteryMenu::new));
} 