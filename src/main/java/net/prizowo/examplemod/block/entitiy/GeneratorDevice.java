package net.prizowo.examplemod.block.entitiy;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.Block;

public class GeneratorDevice extends BlockEntity implements BlockEntityTicker<GeneratorDevice> {
    private final ItemStackHandler itemHandler = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    private int burnTime = 0;
    private int totalBurnTime = 0;
    private boolean isBurning = false;

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
    // 容量: 100,000 FE
    // 最大输入: 1,000 FE/t
    // 最大输出: 1,000 FE/t
    private final CustomEnergyStorage energyStorage = new CustomEnergyStorage(100000, 1000, 1000);

    public GeneratorDevice(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    public IEnergyStorage getEnergyStorage(Direction direction) {
        return energyStorage; // 允许从任何方向访问能量存储
    }

    public ItemStackHandler getItemHandler() {
        return itemHandler;
    }

    @Override
    public void tick(Level level, BlockPos pos, BlockState state, GeneratorDevice blockEntity) {
        if (!level.isClientSide) {
            boolean wasActive = isBurning;
            
            if (isBurning) {
                // 正在燃烧
                energyStorage.receiveEnergy(getFuelPowerOutput(), false);
                burnTime--;
                if (burnTime <= 0) {
                    isBurning = false;
                }
            }

            if (!isBurning) {
                // 检查是否可以开始新的燃烧
                ItemStack fuel = itemHandler.getStackInSlot(0);
                if (!fuel.isEmpty()) {
                    int burnValue = getBurnTime(fuel);
                    if (burnValue > 0 && energyStorage.getEnergyStored() < energyStorage.getMaxEnergyStored()) {
                        itemHandler.extractItem(0, 1, false);
                        burnTime = burnValue;
                        totalBurnTime = burnTime;
                        isBurning = true;
                    }
                }
            }

            // 如果燃烧状态改变，更新方块状态
            if (wasActive != isBurning) {
                level.setBlock(pos, 
                    state.setValue(BlockStateProperties.LIT, isBurning), 
                    Block.UPDATE_ALL);  // 使用 UPDATE_ALL 来确保光照更新
                setChanged();
            }

            // 尝试输出能量到周围方块
            outputEnergy();
        }
    }

    private void outputEnergy() {
        for (Direction direction : Direction.values()) {
            BlockPos targetPos = getBlockPos().relative(direction);
            IEnergyStorage targetStorage = level.getCapability(Capabilities.EnergyStorage.BLOCK, 
                targetPos, direction.getOpposite());

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

    private int getBurnTime(ItemStack stack) {
        // 临时使用原版熔炉的燃烧时间逻辑
        if (stack.isEmpty()) {
            return 0;
        }
        
        if (stack.is(Blocks.COAL_BLOCK.asItem())) {
            return 16000;
        } else if (stack.is(Items.COAL)) {
            return 1600;
        } else if (stack.is(Items.CHARCOAL)) {
            return 1600;
        } else if (stack.is(ItemTags.PLANKS)) {
            return 300;
        } else if (stack.is(ItemTags.LOGS)) {
            return 300;
        } else if (stack.is(ItemTags.WOODEN_SLABS)) {
            return 150;
        } else if (stack.is(Items.STICK)) {
            return 100;
        }
        
        return 0;
    }

    private int getFuelPowerOutput() {
        // 根据不同燃料返回不同的发电量
        ItemStack fuelType = itemHandler.getStackInSlot(0);
        if (fuelType.isEmpty()) return 100; // 默认发电量
        
        int burnTime = getBurnTime(fuelType);
        // 根据燃烧时间返回相应的发电量
        if (burnTime > 1600) return 200;      // 煤炭等高级燃料
        else if (burnTime > 800) return 150;  // 木炭等中级燃料
        return 100;                           // 木棍等低级燃料
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("inventory", itemHandler.serializeNBT(registries));
        tag.putInt("burn_time", burnTime);
        tag.putInt("total_burn_time", totalBurnTime);
        tag.putBoolean("is_burning", isBurning);
        tag.putInt("energy", energyStorage.getEnergyStored());
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        itemHandler.deserializeNBT(registries, tag.getCompound("inventory"));
        burnTime = tag.getInt("burn_time");
        totalBurnTime = tag.getInt("total_burn_time");
        isBurning = tag.getBoolean("is_burning");
        if (tag.contains("energy")) {
            energyStorage.setStoredEnergy(tag.getInt("energy"));
        }
    }
} 