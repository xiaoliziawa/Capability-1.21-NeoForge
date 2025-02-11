package net.prizowo.examplemod.block.entitiy;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.Nullable;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

//自己创建一个Block类
// LirxOwO: 感谢 vfyjxf大佬的技术指导！！！
public class FluidTankDevice extends BlockEntity {
    public FluidTankDevice(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }
    /**
     * 单格流体容器，如果你需要多个格子的，请自己实现一个IFluidHandler，
     * 但是请注意，neoforge提供的api不允许你获取某个格子里面的流体，只允许把这个容器看做整体取出流体或存入流体
     */
    private final FluidTank fluidTank = new FluidTank(20000);

    public IFluidHandler getFluidHandler() {
        return fluidTank;
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        var tag = new CompoundTag();
        var data = new CompoundTag();
        fluidTank.writeToNBT(registries, data);
        tag.put("sync_data", data);
        return super.getUpdateTag(registries);
    }

    @Override
    public ModelData getModelData() {
        //模型数据，渲染用,TODO.....
        return super.getModelData();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("fluid", fluidTank.writeToNBT(registries, new CompoundTag()));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        //如果你不想用这个，参考neoforge文档的onDataPacket，具体是在https://docs.neoforged.net/docs/blockentities/#syncing
        if (tag.contains("sync_data")) {//sync data
            fluidTank.readFromNBT(registries, tag.getCompound("sync_data"));
        } else {
            super.loadAdditional(tag, registries);
            if (tag.contains("fluid")) {
                fluidTank.readFromNBT(registries, tag.getCompound("fluid"));
            }
        }
    }
}