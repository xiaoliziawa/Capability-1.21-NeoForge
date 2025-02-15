package net.prizowo.examplemod.jade;

import mekanism.api.chemical.ChemicalStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.prizowo.examplemod.ExampleMod;
import net.prizowo.examplemod.api.IEnergyFurnace;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.BoxStyle;
import snownee.jade.api.ui.IElementHelper;
import snownee.jade.api.ui.IElement;

public enum FurnaceProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
    INSTANCE;

    private static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(ExampleMod.MODID, "furnace_info");

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        CompoundTag data = accessor.getServerData();
        if (data.contains("energy")) {
            int energy = data.getInt("energy");
            int maxEnergy = data.getInt("maxEnergy");

            IElementHelper helper = IElementHelper.get();
            IElement energyBar = helper.progress(
                    energy / (float) maxEnergy,  // 进度
                    Component.literal(energy + " / " + maxEnergy + " FE"), // 文本
                    helper
                            .progressStyle()
                            .color(0xFF3498DB)  // 蓝色进度条
                            .textColor(0xFFFFFFFF),  // 白色文本
                    BoxStyle.GradientBorder.DEFAULT_NESTED_BOX,  //盒子样式
                    true  //  是否显示文本
            );
            tooltip.add(energyBar);
        }

    }

    @Override
    public void appendServerData(CompoundTag data, BlockAccessor accessor) {
        if (accessor.getBlockEntity() instanceof AbstractFurnaceBlockEntity blockEntity) {
            if (blockEntity instanceof IEnergyFurnace furnace) {
                // 能量
                var energyStorage = furnace.getEnergyStorage(null);
                data.putInt("energy", energyStorage.getEnergyStored());
                data.putInt("maxEnergy", energyStorage.getMaxEnergyStored());

                // 化学品
                var chemicalStorage = furnace.getChemicalStorage(null);
                ChemicalStack stack = chemicalStorage.getChemicalInTank(0);
                if (!stack.isEmpty()) {
                    data.putLong("chemical_amount", stack.getAmount());
                }
            }
        }
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }
} 