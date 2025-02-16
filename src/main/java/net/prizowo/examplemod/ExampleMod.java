package net.prizowo.examplemod;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.prizowo.examplemod.api.IEnergyFurnace;
import net.prizowo.examplemod.block.entity.ChemicalPipeTileEntity;
import net.prizowo.examplemod.block.entity.GeneratorDevice;
import net.prizowo.examplemod.block.entity.BatteryDevice;
import net.prizowo.examplemod.registry.ModBlockEntities;
import net.prizowo.examplemod.registry.ModBlocks;
import net.prizowo.examplemod.registry.ModCreativeTab;
import net.prizowo.examplemod.registry.ModMenuTypes;

import static net.prizowo.examplemod.recipe.Recipes.*;

@Mod("examplemod")
public class ExampleMod {
    public static final String MODID = "examplemod";
    public ExampleMod(IEventBus modEventBus) {
        ModBlocks.BLOCKS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        ModBlocks.ITEMS.register(modEventBus);
        ModMenuTypes.MENU_TYPES.register(modEventBus);
        ModCreativeTab.CREATIVE_MODE_TABS.register(modEventBus);

        modEventBus.addListener(this::registerCapabilities);

        // removeMod("mekanism");
        // removeMod("farmersdelight");

        // removeRecipe("mekanism:factory/advanced/purifying");

        // removeItemId("minecraft:diamond_block");
        // removeItemId("minecraft:diamond_sword");

        // removeRecipe("minecraft", "furnace");
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

        event.registerBlockEntity(
            Capabilities.EnergyStorage.BLOCK,
            BlockEntityType.FURNACE,
            (blockEntity, direction) -> ((IEnergyFurnace)blockEntity).getEnergyStorage(direction)
        );

        event.registerBlockEntity(
            Capabilities.EnergyStorage.BLOCK,
            BlockEntityType.BLAST_FURNACE,
            (blockEntity, direction) -> ((IEnergyFurnace)blockEntity).getEnergyStorage(direction)
        );

        event.registerBlockEntity(
            Capabilities.EnergyStorage.BLOCK,
            BlockEntityType.SMOKER,
            (blockEntity, direction) -> ((IEnergyFurnace)blockEntity).getEnergyStorage(direction)
        );

        event.registerBlockEntity(
            mekanism.common.capabilities.Capabilities.CHEMICAL.block(),
            BlockEntityType.FURNACE,
            (blockEntity, direction) -> ((IEnergyFurnace)blockEntity).getChemicalStorage(direction)
        );

        event.registerBlockEntity(
            mekanism.common.capabilities.Capabilities.CHEMICAL.block(),
            BlockEntityType.BLAST_FURNACE,
            (blockEntity, direction) -> ((IEnergyFurnace)blockEntity).getChemicalStorage(direction)
        );

        event.registerBlockEntity(
            mekanism.common.capabilities.Capabilities.CHEMICAL.block(),
            BlockEntityType.SMOKER,
            (blockEntity, direction) -> ((IEnergyFurnace)blockEntity).getChemicalStorage(direction)
        );

        event.registerBlockEntity(
            mekanism.common.capabilities.Capabilities.CHEMICAL.block(),
            ModBlockEntities.CHEMICAL_PIPE.get(),
            (blockEntity, direction) -> ((ChemicalPipeTileEntity)blockEntity).getChemicalHandler(direction)
        );
    }
}