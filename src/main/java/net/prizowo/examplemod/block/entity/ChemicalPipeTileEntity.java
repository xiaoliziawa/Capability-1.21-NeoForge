package net.prizowo.examplemod.block.entity;

import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalHandler;
import mekanism.common.capabilities.chemical.ChemicalTankChemicalTank;
import mekanism.common.tier.ChemicalTankTier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.prizowo.examplemod.registry.ModBlockEntities;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class ChemicalPipeTileEntity extends BlockEntity {
    private final ChemicalTankChemicalTank chemicalTank;
    private final int transferRate = 100; // 每tick传输量
    private Direction lastReceivedFrom = null;
    private int transferCooldown = 0; // 传输冷却时间
    private static final int COOLDOWN_TIME = 2; // 冷却时间（ticks）

    public ChemicalPipeTileEntity(BlockEntityType<ChemicalPipeTileEntity> chemicalPipeTileEntityBlockEntityType, BlockPos pos, BlockState state) {
        super(ModBlockEntities.CHEMICAL_PIPE.get(), pos, state);
        this.chemicalTank = ChemicalTankChemicalTank.create(
                ChemicalTankTier.BASIC,
                this::setChanged
        );
    }

    public static void tick(Level level, BlockPos pos, BlockState state, ChemicalPipeTileEntity pipe) {
        if (level == null || level.isClientSide) return;

        if (pipe.transferCooldown > 0) {
            pipe.transferCooldown--;
            return;
        }

        // 获取所有可能的输出方向
        List<Direction> possibleOutputs = new ArrayList<>();
        for (Direction direction : Direction.values()) {
            // 防止管道内化学品传来传去，死循环
            if (direction != pipe.lastReceivedFrom) {
                BlockPos targetPos = pos.relative(direction);
                IChemicalHandler targetHandler = level.getCapability(mekanism.common.capabilities.Capabilities.CHEMICAL.block(),
                        targetPos, direction.getOpposite());
                if (targetHandler != null) {
                    possibleOutputs.add(direction);
                }
            }
        }

        // 如果没有化学品或没有可能的输出方向，直接返回
        if (pipe.chemicalTank.isEmpty() || possibleOutputs.isEmpty()) {
            return;
        }

        // 计算每个方向应该传输的量
        int outputCount = possibleOutputs.size();
        long amountPerOutput = Math.min(pipe.transferRate, pipe.chemicalTank.getStored()) / outputCount;
        if (amountPerOutput <= 0) return;

        boolean transferred = false;
        for (Direction direction : possibleOutputs) {
            BlockPos targetPos = pos.relative(direction);
            IChemicalHandler targetHandler = level.getCapability(mekanism.common.capabilities.Capabilities.CHEMICAL.block(),
                    targetPos, direction.getOpposite());

            if (targetHandler != null) {
                ChemicalStack toTransfer = pipe.chemicalTank.extract(amountPerOutput, Action.SIMULATE, AutomationType.INTERNAL);
                if (!toTransfer.isEmpty()) {
                    ChemicalStack remaining = targetHandler.insertChemical(toTransfer, Action.EXECUTE);
                    long actualTransferred = toTransfer.getAmount() - remaining.getAmount();
                    if (actualTransferred > 0) {
                        pipe.chemicalTank.extract(actualTransferred, Action.EXECUTE, AutomationType.INTERNAL);
                        transferred = true;
                    }
                }
            }
        }

        if (transferred) {
            pipe.transferCooldown = COOLDOWN_TIME;
        }
    }

    public IChemicalHandler getChemicalHandler(Direction direction) {
        return new WrappedChemicalHandler(chemicalTank, direction);
    }

    private class WrappedChemicalHandler implements IChemicalHandler {
        private final ChemicalTankChemicalTank tank;
        private final Direction side;

        public WrappedChemicalHandler(ChemicalTankChemicalTank tank, Direction side) {
            this.tank = tank;
            this.side = side;
        }

        @Override
        public int getChemicalTanks() {
            return 1;
        }

        @Override
        public @NotNull ChemicalStack getChemicalInTank(int tank) {
            return tank == 0 ? this.tank.getStack() : this.tank.getStack().copy();
        }

        @Override
        public void setChemicalInTank(int tank, @NotNull ChemicalStack stack) {
            if (tank == 0) {
                this.tank.setStack(stack);
            }
        }

        @Override
        public long getChemicalTankCapacity(int tank) {
            return tank == 0 ? this.tank.getCapacity() : 0;
        }

        @Override
        public boolean isValid(int tank, @NotNull ChemicalStack stack) {
            return tank == 0 && this.tank.isValid(stack);
        }

        @Override
        public @NotNull ChemicalStack insertChemical(int tank, @NotNull ChemicalStack stack, @NotNull Action action) {
            if (tank != 0) return stack;
            if (action.execute()) {
                lastReceivedFrom = side;
            }
            return this.tank.insert(stack, action, AutomationType.EXTERNAL);
        }

        @Override
        public @NotNull ChemicalStack extractChemical(int tank, long amount, @NotNull Action action) {
            if (tank != 0) return this.tank.getStack().copy();
            if (side == lastReceivedFrom) {
                return this.tank.getStack().copy();
            }
            return this.tank.extract(amount, action, AutomationType.EXTERNAL);
        }

        @Override
        public @NotNull ChemicalStack insertChemical(@NotNull ChemicalStack stack, @NotNull Action action) {
            return IChemicalHandler.super.insertChemical(stack, action);
        }

        @Override
        public @NotNull ChemicalStack extractChemical(long amount, @NotNull Action action) {
            return IChemicalHandler.super.extractChemical(amount, action);
        }

        @Override
        public @NotNull ChemicalStack extractChemical(@NotNull ChemicalStack stack, @NotNull Action action) {
            return IChemicalHandler.super.extractChemical(stack, action);
        }
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        CompoundTag chemicalTag = new CompoundTag();
        chemicalTag.put("Tank", chemicalTank.serializeNBT(registries));
        if (lastReceivedFrom != null) {
            chemicalTag.putInt("LastReceivedFrom", lastReceivedFrom.ordinal());
        }
        tag.put("ChemicalData", chemicalTag);
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("ChemicalData")) {
            CompoundTag chemicalTag = tag.getCompound("ChemicalData");
            chemicalTank.deserializeNBT(registries, chemicalTag.getCompound("Tank"));
            if (chemicalTag.contains("LastReceivedFrom")) {
                lastReceivedFrom = Direction.values()[chemicalTag.getInt("LastReceivedFrom")];
            }
        }
    }
}