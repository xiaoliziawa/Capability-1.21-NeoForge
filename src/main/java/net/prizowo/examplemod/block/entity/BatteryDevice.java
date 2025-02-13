package net.prizowo.examplemod.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.minecraft.world.item.ItemStack;

public class BatteryDevice extends BlockEntity implements BlockEntityTicker<BatteryDevice> {
    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> energyStorage.getEnergyStored();
                case 1 -> energyStorage.getMaxEnergyStored();
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            if (index == 0) {
                energyStorage.setStoredEnergy(value);
            }
        }

        @Override
        public int getCount() {
            return 2;
        }
    };

    private class CustomEnergyStorage extends EnergyStorage {
        public CustomEnergyStorage(int capacity, int maxReceive, int maxExtract) {
            super(capacity, maxReceive, maxExtract);
        }

        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            int energyReceived = super.receiveEnergy(maxReceive, simulate);
            if (energyReceived > 0 && !simulate) {
                setChanged();
                if (level != null && !level.isClientSide) {
                    level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
                }
            }
            return energyReceived;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            int energyExtracted = super.extractEnergy(maxExtract, simulate);
            if (energyExtracted > 0 && !simulate) {
                setChanged();
                if (level != null && !level.isClientSide) {
                    level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
                }
            }
            return energyExtracted;
        }

        protected void setStoredEnergy(int energy) {
            this.energy = Math.max(0, Math.min(capacity, energy));
            setChanged();
            if (level != null && !level.isClientSide) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
            }
        }
    }

    // 容量: 10,000,000 FE
    // 最大输入/输出: 10,000 FE/t
    private final CustomEnergyStorage energyStorage = new CustomEnergyStorage(10000000, 10000, 10000);

    private final ItemStackHandler chargeSlotHandler = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return stack.getCapability(Capabilities.EnergyStorage.ITEM) != null;
        }
    };

    public BatteryDevice(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    public IEnergyStorage getEnergyStorage(Direction direction) {
        return energyStorage;
    }

    public ContainerData getContainerData() {
        return data;
    }

    public ItemStackHandler getChargeSlotHandler() {
        return chargeSlotHandler;
    }

    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.@NotNull Provider registries) {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag, registries);
        return tag;
    }

    @Override
    public void handleUpdateTag(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        loadAdditional(tag, registries);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("energy", energyStorage.getEnergyStored());
        tag.put("charge_slot", chargeSlotHandler.serializeNBT(registries));
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("energy")) {
            energyStorage.setStoredEnergy(tag.getInt("energy"));
        }
        if (tag.contains("charge_slot")) {
            chargeSlotHandler.deserializeNBT(registries, tag.getCompound("charge_slot"));
        }
    }

    @Override
    public void tick(Level level, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull BatteryDevice blockEntity) {
        if (!level.isClientSide) {
            boolean energyChanged = false;
            int energyBefore = energyStorage.getEnergyStored();
            
            // 充电槽
            ItemStack chargeItem = chargeSlotHandler.getStackInSlot(0);
            if (!chargeItem.isEmpty()) {
                IEnergyStorage itemEnergy = chargeItem.getCapability(Capabilities.EnergyStorage.ITEM);
                if (itemEnergy != null && itemEnergy.canReceive()) {
                    int maxExtract = energyStorage.extractEnergy(10000, true);
                    if (maxExtract > 0) {
                        int energyTransferred = itemEnergy.receiveEnergy(maxExtract, false);
                        if (energyTransferred > 0) {
                            energyStorage.extractEnergy(energyTransferred, false);
                            energyChanged = true;
                        }
                    }
                }
            }
            
            if (energyStorage.getEnergyStored() < energyStorage.getMaxEnergyStored()) {
                // 电池不满，继续抽电
                receiveEnergy();
            } else {
                // 电池满了的话，开始向外面输出能量
                outputEnergy();
            }
            
            if (energyStorage.getEnergyStored() != energyBefore) {
                energyChanged = true;
            }

            if (energyChanged) {
                setChanged();
                level.sendBlockUpdated(pos, state, state, Block.UPDATE_ALL);
            }
        }
    }

    private void receiveEnergy() {
        if (level == null || level.isClientSide) return;

        for (Direction direction : Direction.values()) {
            BlockPos targetPos = getBlockPos().relative(direction);
            IEnergyStorage targetStorage = level.getCapability(Capabilities.EnergyStorage.BLOCK,
                    targetPos, direction.getOpposite());

            if (targetStorage != null && targetStorage.canExtract()) {
                int maxReceive = energyStorage.receiveEnergy(10000, true);
                if (maxReceive > 0) {
                    int energyReceived = targetStorage.extractEnergy(maxReceive, false);
                    if (energyReceived > 0) {
                        energyStorage.receiveEnergy(energyReceived, false);
                    }
                }
            }
        }
    }

    private void outputEnergy() {
        if (level == null || level.isClientSide) return;

        for (Direction direction : Direction.values()) {
            BlockPos targetPos = getBlockPos().relative(direction);
            IEnergyStorage targetStorage = level.getCapability(Capabilities.EnergyStorage.BLOCK,
                    targetPos, direction.getOpposite());

            if (targetStorage != null && targetStorage.canReceive()) {
                int maxExtract = energyStorage.extractEnergy(10000, true);
                if (maxExtract > 0) {
                    int energyTransferred = targetStorage.receiveEnergy(maxExtract, false);
                    if (energyTransferred > 0) {
                        energyStorage.extractEnergy(energyTransferred, false);
                    }
                }
            }
        }
    }
} 