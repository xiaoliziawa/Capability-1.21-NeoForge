package net.prizowo.examplemod;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.prizowo.examplemod.block.entitiy.GeneratorDevice;
import net.prizowo.examplemod.registry.ModBlockEntities;
import net.prizowo.examplemod.registry.ModBlocks;

@Mod("examplemod")
public class ExampleMod {
    public ExampleMod(IEventBus modEventBus) {
        ModBlocks.BLOCKS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        ModBlocks.ITEMS.register(modEventBus);
        
        modEventBus.addListener(this::registerCapabilities);
    }

    private void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
            Capabilities.FluidHandler.BLOCK,
            ModBlockEntities.FLUID_TANK.get(),
            (blockEntity, context) -> blockEntity.getFluidHandler()
        );
        
        event.registerBlockEntity(
            Capabilities.EnergyStorage.BLOCK,
            ModBlockEntities.GENERATOR.get(),
                GeneratorDevice::getEnergyStorage
        );
    }
}