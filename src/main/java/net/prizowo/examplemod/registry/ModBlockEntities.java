package net.prizowo.examplemod.registry;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.prizowo.examplemod.ExampleMod;
import net.prizowo.examplemod.block.entity.FluidTankDevice;
import net.prizowo.examplemod.block.entity.GeneratorDevice;
import net.prizowo.examplemod.block.entity.BatteryDevice;
import net.prizowo.examplemod.block.entity.ChemicalPipeTileEntity;
import net.prizowo.examplemod.block.entity.CreativeControllerBlockEntity;

import java.util.function.Supplier;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, ExampleMod.MODID);

    public static final Supplier<BlockEntityType<CreativeControllerBlockEntity>> CREATIVE_CONTROLLER = BLOCK_ENTITIES.register("creative_controller",
            () -> BlockEntityType.Builder.<CreativeControllerBlockEntity>of(
                    (pos, state) -> new CreativeControllerBlockEntity(ModBlockEntities.CREATIVE_CONTROLLER.get(), pos, state),
                    ModBlocks.CREATIVE_CONTROLLER.get()
            ).build(null));

    public static final Supplier<BlockEntityType<FluidTankDevice>> FLUID_TANK = BLOCK_ENTITIES.register("fluid_tank",
            () -> BlockEntityType.Builder.<FluidTankDevice>of(
                    (pos, state) -> new FluidTankDevice(ModBlockEntities.FLUID_TANK.get(), pos, state),
                    ModBlocks.FLUID_TANK.get()
            ).build(null));

    public static final Supplier<BlockEntityType<GeneratorDevice>> GENERATOR = BLOCK_ENTITIES.register("generator",
            () -> BlockEntityType.Builder.<GeneratorDevice>of(
                    (pos, state) -> new GeneratorDevice(ModBlockEntities.GENERATOR.get(), pos, state),
                    ModBlocks.GENERATOR.get()
            ).build(null));

    public static final Supplier<BlockEntityType<BatteryDevice>> BATTERY = BLOCK_ENTITIES.register("battery",
            () -> BlockEntityType.Builder.<BatteryDevice>of(
                    (pos, state) -> new BatteryDevice(ModBlockEntities.BATTERY.get(), pos, state),
                    ModBlocks.BATTERY.get()
            ).build(null));

    public static final Supplier<BlockEntityType<ChemicalPipeTileEntity>> CHEMICAL_PIPE = BLOCK_ENTITIES.register("chemical_pipe",
            () -> BlockEntityType.Builder.<ChemicalPipeTileEntity>of(
                    (pos, state) -> new ChemicalPipeTileEntity(ModBlockEntities.CHEMICAL_PIPE.get(), pos, state),
                    ModBlocks.CHEMICAL_PIPE.get()
            ).build(null));
}