package net.prizowo.examplemod.multiblock;

import blusunrize.immersiveengineering.api.multiblocks.BlockMatcher;
import blusunrize.immersiveengineering.api.multiblocks.ClientMultiblocks.MultiblockManualData;
import blusunrize.immersiveengineering.api.multiblocks.TemplateMultiblock;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.prizowo.examplemod.ExampleMod;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class ExampleMultiblock extends TemplateMultiblock {
    private static final ResourceLocation TEMPLATE_LOCATION = ResourceLocation.fromNamespaceAndPath(ExampleMod.MODID, "example_multiblock");

    public ExampleMultiblock() {
        super(TEMPLATE_LOCATION,
                new BlockPos(1, 0, 1),
                new BlockPos(1, 0, 2),
                new BlockPos(0, 0, 0)
        );
    }

    @Override
    public boolean canBeMirrored() {
        return false;
    }

    @Override
    public ResourceLocation getUniqueName() {
        return TEMPLATE_LOCATION;
    }

    @Override
    public boolean isBlockTrigger(BlockState state, Direction side, @NotNull Level world) {
        TemplateData template = getTemplate(world);
        return BlockMatcher.matches(template.triggerState(), state, world, null, additionalPredicates).isAllow();
    }

    @Override
    public boolean createStructure(Level world, BlockPos pos, Direction side, Player player) {
        try {
            TemplateData template = getTemplate(world);
            Direction facing = side.getOpposite();

            BlockPos origin = pos.subtract(withSettingsAndOffset(BlockPos.ZERO, triggerFromOrigin, false, facing));
            BlockPos masterPos = origin.offset(withSettingsAndOffset(BlockPos.ZERO, masterFromOrigin, false, facing));

            if (!super.createStructure(world, pos, side, player)) {
                return false;
            }

            for (StructureBlockInfo blockInfo : template.blocksWithoutAir()) {
                BlockPos actualPos = origin.offset(withSettingsAndOffset(BlockPos.ZERO, blockInfo.pos(), false, facing));
                world.setBlockAndUpdate(actualPos, Blocks.AIR.defaultBlockState());
            }

            world.setBlockAndUpdate(masterPos, Blocks.ENCHANTING_TABLE.defaultBlockState());

            world.playSound(null,
                    masterPos,
                    SoundEvents.BEACON_ACTIVATE,
                    SoundSource.BLOCKS,
                    1.0F, 1.0F
            );

            if (world instanceof ServerLevel serverLevel) {
                for(int i = 0; i < 50; i++) {
                    double x = masterPos.getX() + 0.5 + (serverLevel.random.nextDouble() - 0.5) * 2;
                    double y = masterPos.getY() + 0.5 + (serverLevel.random.nextDouble() - 0.5) * 2;
                    double z = masterPos.getZ() + 0.5 + (serverLevel.random.nextDouble() - 0.5) * 2;

                    serverLevel.sendParticles(
                            ParticleTypes.END_ROD,
                            x, y, z,
                            1, 0, 0, 0,
                            0.05
                    );
                }
            }

            return true;
        } catch (Exception e) {
            return false;
        }
    }
    @Override
    protected void replaceStructureBlock(StructureBlockInfo info, Level world, BlockPos pos, boolean mirrored, Direction clickDirection, Vec3i offsetFromMaster) {
    }

    @Override
    public float getManualScale() {
        return 12.0f;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("multiblock." + ExampleMod.MODID + ".example_multiblock");
    }

    @Override
    public void initializeClient(Consumer<MultiblockManualData> consumer) {
        consumer.accept(new MultiblockManualData() {
            @Override
            public NonNullList<ItemStack> getTotalMaterials() {
                NonNullList<ItemStack> materials = NonNullList.create();
                Level level = Minecraft.getInstance().level;
                if (level != null) {
                    TemplateData template = getTemplate(level);
                    for (StructureBlockInfo blockInfo : template.blocksWithoutAir()) {
                        materials.add(new ItemStack(blockInfo.state().getBlock(), 1));
                    }
                }
                return materials;
            }

            @Override
            public boolean canRenderFormedStructure() {
                return true;
            }

            @Override
            public void renderFormedStructure(PoseStack stack, MultiBufferSource buffer) {
                renderStructure(stack, buffer);
            }
        });
    }

    private void renderStructure(PoseStack stack, MultiBufferSource buffer) {
        stack.pushPose();
        stack.translate(1.5, 1.5, 1.5);

        float time = (System.currentTimeMillis() % 10000) / 10000f;
        stack.mulPose(Axis.YP.rotationDegrees(time * 360f));

        stack.translate(-1.5, -1.5, -1.5);

        BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();
        Level level = Minecraft.getInstance().level;
        if (level != null) {
            TemplateData template = getTemplate(level);
            for (StructureBlockInfo blockInfo : template.blocksWithoutAir()) {
                stack.pushPose();
                BlockPos pos = blockInfo.pos();
                stack.translate(pos.getX(), pos.getY(), pos.getZ());

                dispatcher.renderSingleBlock(
                        blockInfo.state(),
                        stack,
                        buffer,
                        0xF000F0,
                        0,
                        ModelData.EMPTY,
                        RenderType.solid()
                );
                stack.popPose();
            }
        }
        stack.popPose();
    }
}