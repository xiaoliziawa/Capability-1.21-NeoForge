package net.prizowo.examplemod.registry;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.prizowo.examplemod.block.entitiy.FluidTankDevice;
import net.prizowo.examplemod.block.entitiy.GeneratorDevice;

import java.util.function.Supplier;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, "examplemod");

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
}