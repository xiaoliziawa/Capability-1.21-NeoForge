package net.prizowo.examplemod;

import blusunrize.immersiveengineering.api.ManualHelper;
import blusunrize.immersiveengineering.api.multiblocks.MultiblockHandler;
import blusunrize.lib.manual.ManualEntry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.prizowo.examplemod.api.IEnergyFurnace;
import net.prizowo.examplemod.block.entity.ChemicalPipeTileEntity;
import net.prizowo.examplemod.block.entity.GeneratorDevice;
import net.prizowo.examplemod.block.entity.BatteryDevice;
import net.prizowo.examplemod.event.DisplayEvents;
import net.prizowo.examplemod.event.SignDisplayEvents;
import net.prizowo.examplemod.multiblock.ExampleMultiblock;
import net.prizowo.examplemod.registry.*;
import net.prizowo.examplemod.villagers.ModPOIs;
import net.prizowo.examplemod.villagers.ModVillagers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.EquipmentSlot;

@Mod(ExampleMod.MODID)
public class ExampleMod {
    public static final String MODID = "examplemod";
    public ExampleMod(IEventBus modEventBus) {
        ModBlocks.BLOCKS.register(modEventBus);
        ModBlockItems.ITEMS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        ModMenuTypes.MENU_TYPES.register(modEventBus);
        ModCreativeTab.CREATIVE_MODE_TABS.register(modEventBus);
        ModVillagers.VILLAGER_PROFESSIONS.register(modEventBus);
        ModPOIs.POI_TYPES.register(modEventBus);
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::registerCapabilities);
        NeoForge.EVENT_BUS.register(new DisplayEvents());
        NeoForge.EVENT_BUS.register(new SignDisplayEvents());
        
    }

//    private void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
//        if (event.getItemStack().is(Items.DIAMOND_SWORD)) {
//            if (event.getHand() == InteractionHand.MAIN_HAND) {
//                Level level = event.getLevel();
//                if (level instanceof ServerLevel serverLevel) {
//                    event.getItemStack().hurtAndBreak(1, event.getEntity(), EquipmentSlot.MAINHAND);
//                }
//                event.getEntity().swing(InteractionHand.MAIN_HAND, true);
//            }
//        }
//    }
    
    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            var multiblock = new ExampleMultiblock();
            MultiblockHandler.registerMultiblock(multiblock);
            
            // 沉浸手册页面
            if (ManualHelper.getManual() != null) {
                var manual = ManualHelper.getManual();
                var root = manual.getRoot();
                
                var builder = new ManualEntry.ManualEntryBuilder(manual);
                builder.readFromFile(ResourceLocation.fromNamespaceAndPath(MODID, "example_multiblock"));
                
                manual.addEntry(root, builder.create());
            }
        });
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