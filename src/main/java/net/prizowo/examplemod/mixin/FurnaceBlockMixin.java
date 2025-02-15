package net.prizowo.examplemod.mixin;

import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalHandler;
import mekanism.common.capabilities.chemical.ChemicalTankChemicalTank;
import mekanism.common.tier.ChemicalTankTier;
import mekanism.common.registries.MekanismChemicals;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.prizowo.examplemod.api.IEnergyFurnace;
import net.prizowo.examplemod.util.CustomEnergyStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractFurnaceBlockEntity.class)
public abstract class FurnaceBlockMixin implements IEnergyFurnace {
    @Unique
    private CustomEnergyStorage exampleMod_1_21$energyStorage;

    @Unique
    private ChemicalTankChemicalTank exampleMod_1_21$chemicalTank;

    @Shadow protected int litTime;
    @Shadow protected int litDuration;

    @Inject(method = "<init>*", at = @At("RETURN"))
    private void onConstructed(CallbackInfo ci) {
        this.exampleMod_1_21$energyStorage = new CustomEnergyStorage(50000, 1000, 500);
        this.exampleMod_1_21$chemicalTank = ChemicalTankChemicalTank.create(
                ChemicalTankTier.BASIC,
                () -> {
                    AbstractFurnaceBlockEntity blockEntity = (AbstractFurnaceBlockEntity)(Object)this;
                    blockEntity.setChanged();
                }
        );
    }

    @Override
    public IEnergyStorage getEnergyStorage(Direction direction) {
        return exampleMod_1_21$energyStorage;
    }

    @Override
    public IChemicalHandler getChemicalStorage(Direction direction) {
        return exampleMod_1_21$chemicalTank;
    }
    @Inject(method = "serverTick", at = @At("HEAD"))
    private static void onServerTickStart(Level level, BlockPos pos, BlockState state, AbstractFurnaceBlockEntity blockEntity, CallbackInfo ci) {
        if (!(blockEntity instanceof IEnergyFurnace)) return;

        FurnaceBlockMixin furnace = (FurnaceBlockMixin)(Object)blockEntity;
        if (furnace.litTime > 0) {
            furnace.exampleMod_1_21$energyStorage.receiveEnergy(100, false);

            ChemicalStack steamChemical = MekanismChemicals.STEAM.getStack(10);
            furnace.exampleMod_1_21$chemicalTank.insert(steamChemical, Action.EXECUTE, AutomationType.INTERNAL);
        }
    }

    @Inject(method = "serverTick", at = @At("TAIL"))
    private static void onServerTickEnd(Level level, BlockPos pos, BlockState state, AbstractFurnaceBlockEntity blockEntity, CallbackInfo ci) {
        if (!(blockEntity instanceof IEnergyFurnace)) return;

        FurnaceBlockMixin furnace = (FurnaceBlockMixin)(Object)blockEntity;

        if (furnace.exampleMod_1_21$energyStorage.getEnergyStored() > 0) {
            for (Direction direction : Direction.values()) {
                BlockPos targetPos = pos.relative(direction);
                IEnergyStorage targetStorage = level.getCapability(Capabilities.EnergyStorage.BLOCK,
                        targetPos, direction.getOpposite());

                if (targetStorage != null && targetStorage.canReceive()) {
                    int maxExtract = furnace.exampleMod_1_21$energyStorage.extractEnergy(500, true);
                    if (maxExtract > 0) {
                        int energyTransferred = targetStorage.receiveEnergy(maxExtract, false);
                        if (energyTransferred > 0) {
                            furnace.exampleMod_1_21$energyStorage.extractEnergy(energyTransferred, false);
                        }
                    }
                }
            }
        }

        if (!furnace.exampleMod_1_21$chemicalTank.isEmpty()) {
            for (Direction direction : Direction.values()) {
                BlockPos targetPos = pos.relative(direction);
                IChemicalHandler targetHandler = level.getCapability(mekanism.common.capabilities.Capabilities.CHEMICAL.block(),
                        targetPos, direction.getOpposite());

                if (targetHandler != null) {
                    ChemicalStack chemicalToTransfer = furnace.exampleMod_1_21$chemicalTank.extract(100, Action.SIMULATE, AutomationType.INTERNAL);
                    if (!chemicalToTransfer.isEmpty()) {
                        ChemicalStack remaining = targetHandler.insertChemical(chemicalToTransfer, Action.EXECUTE);
                        long transferred = chemicalToTransfer.getAmount() - remaining.getAmount();
                        if (transferred > 0) {
                            furnace.exampleMod_1_21$chemicalTank.extract(transferred, Action.EXECUTE, AutomationType.INTERNAL);
                        }
                    }
                }
            }
        }
    }

    @Inject(method = "saveAdditional", at = @At("TAIL"))
    private void onSaveAdditional(CompoundTag tag, HolderLookup.Provider registries, CallbackInfo ci) {
        tag.putInt("ExampleModEnergy", exampleMod_1_21$energyStorage.getEnergyStored());

        CompoundTag chemicalTag = new CompoundTag();
        chemicalTag.put("Tank", exampleMod_1_21$chemicalTank.serializeNBT(registries));
        tag.put("ExampleModChemical", chemicalTag);
    }

    @Inject(method = "loadAdditional", at = @At("TAIL"))
    private void onLoadAdditional(CompoundTag tag, HolderLookup.Provider registries, CallbackInfo ci) {
        if (tag.contains("ExampleModEnergy")) {
            exampleMod_1_21$energyStorage.setEnergy(tag.getInt("ExampleModEnergy"));
        }
        if (tag.contains("ExampleModChemical")) {
            exampleMod_1_21$chemicalTank.deserializeNBT(registries, tag.getCompound("ExampleModChemical").getCompound("Tank"));
        }
    }
}