package net.prizowo.examplemod.api;

import net.minecraft.core.Direction;
import net.neoforged.neoforge.energy.IEnergyStorage;
import mekanism.api.chemical.IChemicalHandler;

public interface IEnergyFurnace {
    IEnergyStorage getEnergyStorage(Direction direction);
    IChemicalHandler getChemicalStorage(Direction direction);
} 