package net.prizowo.examplemod.block.entitiy;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.capabilities.Capabilities;

public class GeneratorDevice extends BlockEntity {
    // 创建一个自定义的能量存储类
    private class CustomEnergyStorage extends EnergyStorage {
        public CustomEnergyStorage(int capacity, int maxReceive, int maxExtract) {
            super(capacity, maxReceive, maxExtract);
        }

        protected void setStoredEnergy(int energy) {
            this.energy = Math.max(0, Math.min(capacity, energy));
            setChanged();
        }

        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            int energyReceived = super.receiveEnergy(maxReceive, simulate);
            if (energyReceived > 0 && !simulate) {
                setChanged();
            }
            return energyReceived;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            int energyExtracted = super.extractEnergy(maxExtract, simulate);
            if (energyExtracted > 0 && !simulate) {
                setChanged();
            }
            return energyExtracted;
        }
    }

    // 使用自定义的能量存储
    // 容量: 100,000 FE (适配Mekanism的能量等级)
    // 最大输入: 1,000 FE/t
    // 最大输出: 1,000 FE/t
    private final CustomEnergyStorage energyStorage = new CustomEnergyStorage(100000, 1000, 1000);

    public GeneratorDevice(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    public IEnergyStorage getEnergyStorage(Direction direction) {
        return energyStorage; // 允许从任何方向访问能量存储
    }

    // 每tick产生能量
    public void tick() {
        if (!level.isClientSide) {
            // 每tick产生100 FE
            energyStorage.receiveEnergy(100, false);

            // 尝试向周围的方块输出能量
            for (Direction direction : Direction.values()) {
                BlockPos targetPos = getBlockPos().relative(direction);
                IEnergyStorage targetStorage = level.getCapability(Capabilities.EnergyStorage.BLOCK, targetPos, direction.getOpposite());

                if (targetStorage != null && targetStorage.canReceive()) {
                    int maxExtract = energyStorage.extractEnergy(1000, true);
                    if (maxExtract > 0) {
                        int energyTransferred = targetStorage.receiveEnergy(maxExtract, false);
                        if (energyTransferred > 0) {
                            energyStorage.extractEnergy(energyTransferred, false);
                            setChanged();
                        }
                    }
                }
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("energy", energyStorage.getEnergyStored());
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("energy")) {
            energyStorage.setStoredEnergy(tag.getInt("energy"));
        }
    }
} 