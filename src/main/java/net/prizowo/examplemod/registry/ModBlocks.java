package net.prizowo.examplemod.registry;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.prizowo.examplemod.ExampleMod;
import net.prizowo.examplemod.block.BatteryBlock;
import net.prizowo.examplemod.block.ChemicalPipeBlock;
import net.prizowo.examplemod.block.CreativeControllerBlock;
import net.prizowo.examplemod.block.FluidTankBlock;
import net.prizowo.examplemod.block.GeneratorBlock;

import java.util.function.Supplier;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(BuiltInRegistries.BLOCK, ExampleMod.MODID);

    public static final Supplier<Block> FLUID_TANK = BLOCKS.register("fluid_tank",
            () -> new FluidTankBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(3.0f)
                    .requiresCorrectToolForDrops()));

    public static final Supplier<Block> GENERATOR = BLOCKS.register("generator",
            () -> new GeneratorBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(3.0f)
                    .requiresCorrectToolForDrops()));

    public static final Supplier<Block> BATTERY = BLOCKS.register("battery",
            () -> new BatteryBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(3.0f)
                    .requiresCorrectToolForDrops()));

    public static final Supplier<Block> CHEMICAL_PIPE = BLOCKS.register("chemical_pipe",
            () -> new ChemicalPipeBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(2.0f)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()));

    public static final Supplier<Block> CREATIVE_CONTROLLER = BLOCKS.register("creative_controller",
            () -> new CreativeControllerBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_LIGHT_BLUE)
                    .strength(3.0f)
                    .requiresCorrectToolForDrops()
                    .lightLevel(state -> state.getValue(CreativeControllerBlock.POWERED) ? 15 : 0)));

}