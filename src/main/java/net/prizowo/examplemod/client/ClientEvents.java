package net.prizowo.examplemod.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.prizowo.examplemod.ExampleMod;
import net.prizowo.examplemod.registry.ModMenuTypes;
import net.prizowo.examplemod.screen.GeneratorScreen;
import net.prizowo.examplemod.screen.BatteryScreen;

@EventBusSubscriber(modid = ExampleMod.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientEvents {
    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenuTypes.GENERATOR_MENU.get(), GeneratorScreen::new);
        event.register(ModMenuTypes.BATTERY_MENU.get(), BatteryScreen::new);
    }
} 