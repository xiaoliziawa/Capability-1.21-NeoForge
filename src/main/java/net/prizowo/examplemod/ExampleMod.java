package net.prizowo.examplemod;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.prizowo.examplemod.api.IEnergyFurnace;
import net.prizowo.examplemod.block.entity.GeneratorDevice;
import net.prizowo.examplemod.block.entity.BatteryDevice;
import net.prizowo.examplemod.registry.ModBlockEntities;
import net.prizowo.examplemod.registry.ModBlocks;
import net.prizowo.examplemod.registry.ModMenuTypes;

@Mod("examplemod")
public class ExampleMod {
    public static final String MODID = "examplemod";
    public ExampleMod(IEventBus modEventBus) {
        ModBlocks.BLOCKS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        ModBlocks.ITEMS.register(modEventBus);
        ModMenuTypes.MENU_TYPES.register(modEventBus);

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

        event.registerBlockEntity(
            Capabilities.EnergyStorage.BLOCK,
            ModBlockEntities.BATTERY.get(),
            BatteryDevice::getEnergyStorage
        );

        // 注册熔炉的能量存储功能
        event.registerBlockEntity(
            Capabilities.EnergyStorage.BLOCK,
            BlockEntityType.FURNACE,
            (blockEntity, direction) -> ((IEnergyFurnace)blockEntity).getEnergyStorage(direction)
        );

        // 注册高炉的能量存储功能
        event.registerBlockEntity(
            Capabilities.EnergyStorage.BLOCK,
            BlockEntityType.BLAST_FURNACE,
            (blockEntity, direction) -> ((IEnergyFurnace)blockEntity).getEnergyStorage(direction)
        );

        // 注册烟熏炉的能量存储功能
        event.registerBlockEntity(
            Capabilities.EnergyStorage.BLOCK,
            BlockEntityType.SMOKER,
            (blockEntity, direction) -> ((IEnergyFurnace)blockEntity).getEnergyStorage(direction)
        );

        // 注册熔炉的化学品处理功能
        event.registerBlockEntity(
            mekanism.common.capabilities.Capabilities.CHEMICAL.block(),
            BlockEntityType.FURNACE,
            (blockEntity, direction) -> ((IEnergyFurnace)blockEntity).getChemicalStorage(direction)
        );

        // 注册高炉的化学品处理功能
        event.registerBlockEntity(
            mekanism.common.capabilities.Capabilities.CHEMICAL.block(),
            BlockEntityType.BLAST_FURNACE,
            (blockEntity, direction) -> ((IEnergyFurnace)blockEntity).getChemicalStorage(direction)
        );

        // 注册烟熏炉的化学品处理功能
        event.registerBlockEntity(
            mekanism.common.capabilities.Capabilities.CHEMICAL.block(),
            BlockEntityType.SMOKER,
            (blockEntity, direction) -> ((IEnergyFurnace)blockEntity).getChemicalStorage(direction)
        );
    }
}