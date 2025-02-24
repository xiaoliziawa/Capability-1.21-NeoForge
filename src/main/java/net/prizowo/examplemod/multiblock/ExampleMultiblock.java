package net.prizowo.examplemod.multiblock;

import blusunrize.immersiveengineering.api.multiblocks.ClientMultiblocks.MultiblockManualData;
import blusunrize.immersiveengineering.api.multiblocks.TemplateMultiblock;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
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
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.prizowo.examplemod.ExampleMod;
import net.minecraft.core.Vec3i;

import java.util.function.Consumer;

public class ExampleMultiblock extends TemplateMultiblock {
    private static final ResourceLocation TEMPLATE_LOCATION =
            ResourceLocation.fromNamespaceAndPath(ExampleMod.MODID, "example");

    public ExampleMultiblock() {
        super(TEMPLATE_LOCATION,
                new BlockPos(1, 1, 1),  // 中心位置（相对于原点）
                new BlockPos(2, 0, 2),  // 触发方块位置（右下角）
                new BlockPos(3, 3, 3)   // 结构大小
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
    public boolean isBlockTrigger(BlockState state, Direction side, Level world) {
        return state.is(Blocks.IRON_BLOCK);
    }

    @Override
    public boolean createStructure(Level world, BlockPos pos, Direction side, Player player) {
        try {
            Direction facing = side.getOpposite();

            BlockPos startPos = switch (facing) {
                case NORTH -> pos.offset(-2, 0, -2);
                case SOUTH -> pos;
                case EAST -> pos.offset(0, 0, -2);
                case WEST -> pos.offset(-2, 0, 0);
                default -> pos;
            };

            // 检查底层 (y=0)
            for(int x = 0; x < 3; x++) {
                for(int z = 0; z < 3; z++) {
                    BlockPos checkPos = getActualPos(startPos, x, 0, z, facing);
                    BlockState state = world.getBlockState(checkPos);
                    if(!state.is(Blocks.IRON_BLOCK)) {
                        return false;
                    }
                }
            }

            // 检查中间层 (y=1)
            for(int x = 0; x < 3; x++) {
                for(int z = 0; z < 3; z++) {
                    BlockPos checkPos = getActualPos(startPos, x, 1, z, facing);
                    BlockState state = world.getBlockState(checkPos);

                    if(x == 1 && z == 1) {
                        continue;
                    }

                    if(!state.is(Blocks.GLASS)) {
                        return false;
                    }
                }
            }

            // 检查顶层 (y=2)
            for(int x = 0; x < 3; x++) {
                for(int z = 0; z < 3; z++) {
                    BlockPos checkPos = getActualPos(startPos, x, 2, z, facing);
                    BlockState state = world.getBlockState(checkPos);
                    if(!state.is(Blocks.IRON_BLOCK)) {
                        return false;
                    }
                }
            }

            // 替换中心方块为钻石块
            BlockPos centerPos = getActualPos(startPos, 1, 1, 1, facing);
            world.setBlockAndUpdate(centerPos, Blocks.DIAMOND_BLOCK.defaultBlockState());

            // 生成粒子效果
            if (world instanceof ServerLevel serverLevel) {
                for (int i = 0; i < 36; i++) {
                    double angle = i * Math.PI * 2 / 36;
                    double x = centerPos.getX() + 0.5 + Math.cos(angle) * 1.5;
                    double z = centerPos.getZ() + 0.5 + Math.sin(angle) * 1.5;

                    serverLevel.sendParticles(
                            ParticleTypes.END_ROD,
                            x, centerPos.getY() + 1.5, z,
                            1, 0, 0, 0,
                            0.05
                    );
                }
            }

            // 播放音效
            world.playSound(null,
                    centerPos,
                    SoundEvents.BEACON_ACTIVATE,
                    SoundSource.BLOCKS,
                    1.0F, 1.0F
            );

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private BlockPos getActualPos(BlockPos origin, int x, int y, int z, Direction facing) {
        int[] adjusted = switch (facing) {
            case NORTH -> new int[]{2-x, y, 2-z};  // 北面：从右下角开始
            case SOUTH -> new int[]{x, y, z};      // 南面：从左下角开始
            case EAST -> new int[]{z, y, 2-x};     // 东面：从右下角开始
            case WEST -> new int[]{2-z, y, x};     // 西面：从右下角开始
            default -> new int[]{x, y, z};
        };

        return origin.offset(adjusted[0], adjusted[1], adjusted[2]);
    }

    @Override
    protected void replaceStructureBlock(StructureBlockInfo info, Level world, BlockPos actualPos,
                                         boolean mirrored, Direction clickDirection, Vec3i offsetFromMaster) {
        try {
            BlockState state = info.state();

            if (offsetFromMaster.equals(Vec3i.ZERO)) {
                world.setBlockAndUpdate(actualPos, Blocks.DIAMOND_BLOCK.defaultBlockState());

                if (world instanceof ServerLevel serverLevel) {
                    for (int i = 0; i < 36; i++) {
                        double angle = i * Math.PI * 2 / 36;
                        double x = actualPos.getX() + 0.5 + Math.cos(angle) * 1.5;
                        double z = actualPos.getZ() + 0.5 + Math.sin(angle) * 1.5;

                        serverLevel.sendParticles(
                                ParticleTypes.END_ROD,
                                x, actualPos.getY() + 1.5, z,
                                1, 0, 0, 0,
                                0.05
                        );
                    }
                }

                world.playSound(null,
                        actualPos,
                        SoundEvents.BEACON_ACTIVATE,
                        SoundSource.BLOCKS,
                        1.0F, 1.0F
                );
            } else {
                BlockState newState = info.pos().getY() == 1 ?
                        Blocks.GLASS.defaultBlockState() :
                        Blocks.IRON_BLOCK.defaultBlockState();
                world.setBlockAndUpdate(actualPos, newState);
            }
        } catch (Exception e) {
        }
    }

    @Override
    protected void prepareBlockForDisassembly(Level world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);

        if (state.is(Blocks.DIAMOND_BLOCK)) {
            if (world instanceof ServerLevel serverLevel) {
                for (int i = 0; i < 50; i++) {
                    double x = pos.getX() + 0.5 + (world.random.nextDouble() - 0.5) * 2;
                    double y = pos.getY() + 0.5 + (world.random.nextDouble() - 0.5) * 2;
                    double z = pos.getZ() + 0.5 + (world.random.nextDouble() - 0.5) * 2;

                    serverLevel.sendParticles(
                            ParticleTypes.LARGE_SMOKE,
                            x, y, z,
                            1, 0, 0, 0,
                            0.05
                    );

                    if (i % 5 == 0) {
                        serverLevel.sendParticles(
                                ParticleTypes.FLASH,
                                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                                1, 0, 0, 0,
                                0
                        );
                    }
                }
            }

            world.playSound(null,
                    pos,
                    SoundEvents.BEACON_DEACTIVATE,
                    SoundSource.BLOCKS,
                    1.0F, 1.0F
            );
        }

        if (state.is(Blocks.GLASS)) {
            if (world instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(
                        ParticleTypes.ITEM_SNOWBALL,
                        pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                        10, 0.3, 0.3, 0.3,
                        0.15
                );

                world.playSound(null,
                        pos,
                        SoundEvents.GLASS_BREAK,
                        SoundSource.BLOCKS,
                        1.0F, 1.0F
                );
            }
        }
    }

    @Override
    public float getManualScale() {
        return 1.0f;
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
                materials.add(new ItemStack(Blocks.IRON_BLOCK, 18));
                materials.add(new ItemStack(Blocks.GLASS, 8));
                materials.add(new ItemStack(Blocks.DIAMOND_BLOCK, 1));
                return materials;
            }

            @Override
            public boolean canRenderFormedStructure() {
                return true;
            }

            @Override
            public void renderFormedStructure(PoseStack stack, MultiBufferSource buffer) {
                stack.pushPose();

                stack.translate(1.5, 1.5, 1.5);

                float time = (System.currentTimeMillis() % 10000) / 10000f;
                float rotation = time * 360f;
                stack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(rotation));

                stack.translate(-1.5, -1.5, -1.5);

                Minecraft mc = Minecraft.getInstance();
                BlockRenderDispatcher dispatcher = mc.getBlockRenderer();

                for (int x = 0; x < 3; x++) {
                    for (int y = 0; y < 3; y++) {
                        for (int z = 0; z < 3; z++) {
                            stack.pushPose();
                            stack.translate(x, y, z);

                            BlockState state;
                            if (y == 1) {
                                if (x == 1 && z == 1) {
                                    state = Blocks.DIAMOND_BLOCK.defaultBlockState();
                                } else {
                                    state = Blocks.GLASS.defaultBlockState();
                                }
                            } else {
                                state = Blocks.IRON_BLOCK.defaultBlockState();
                            }

                            dispatcher.renderSingleBlock(
                                    state,
                                    stack,
                                    buffer,
                                    0xF000F0,
                                    RenderShape.MODEL.ordinal()
                            );

                            stack.popPose();
                        }
                    }
                }

                stack.popPose();
            }
        });
    }
} 