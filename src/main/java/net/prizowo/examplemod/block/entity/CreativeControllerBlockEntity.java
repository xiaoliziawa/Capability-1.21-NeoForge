package net.prizowo.examplemod.block.entity;

import appeng.api.config.Actionable;
import appeng.api.networking.*;
import appeng.api.networking.pathing.ControllerState;
import appeng.blockentity.networking.ControllerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.prizowo.examplemod.block.CreativeControllerBlock;

public class CreativeControllerBlockEntity extends ControllerBlockEntity {
    private boolean isActive = true;

    public CreativeControllerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
        this.setInternalMaxPower(Double.MAX_VALUE);
        this.setInternalPublicPowerStorage(true);
        this.getMainNode().setIdlePowerUsage(0);
    }

    @Override
    public void onMainNodeStateChanged(IGridNodeListener.State reason) {
        if (this.level != null && !this.level.isClientSide) {
            super.onMainNodeStateChanged(reason);
            this.updateState();
        }
    }

    @Override
    public void updateState() {
        if (this.level == null || this.isRemoved()) {
            return;
        }

        super.updateState();

        IManagedGridNode mainNode = this.getMainNode();
        if (mainNode != null) {
            boolean newActive = true;
            IGrid grid = mainNode.getGrid();
            if (grid != null && grid.getPathingService() != null && 
                grid.getPathingService().getControllerState() == ControllerState.CONTROLLER_CONFLICT) {
                newActive = false;
            }

            if (this.isActive != newActive) {
                this.isActive = newActive;
                BlockState currentState = this.level.getBlockState(this.worldPosition);
                if (currentState.getBlock() instanceof CreativeControllerBlock) {
                    this.level.setBlock(this.worldPosition,
                            currentState.setValue(CreativeControllerBlock.POWERED, newActive),
                            Block.UPDATE_ALL);
                    this.level.updateNeighborsAt(this.worldPosition, currentState.getBlock());
                }
            }
        }
    }

    @Override
    protected double getFunnelPowerDemand(double maxReceived) {
        return 0;
    }

    @Override
    protected double funnelPowerIntoStorage(double power, Actionable mode) {
        return 0;
    }

    @Override
    public void onReady() {
        if (this.level != null && !this.level.isClientSide) {
            super.onReady();
            this.setInternalMaxPower(Double.MAX_VALUE);
            this.injectAEPower(Double.MAX_VALUE, Actionable.MODULATE);
            this.updateState();
        }
    }

    public boolean isActive() {
        return this.isActive;
    }
}