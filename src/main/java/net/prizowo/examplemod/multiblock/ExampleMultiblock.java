package net.prizowo.examplemod.multiblock;

import blusunrize.immersiveengineering.api.multiblocks.ClientMultiblocks.MultiblockManualData;
import blusunrize.immersiveengineering.api.multiblocks.TemplateMultiblock;
import com.mojang.blaze3d.vertex.PoseStack;
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
    private static final ResourceLocation TEMPLATE_LOCATION = ResourceLocation.fromNamespaceAndPath(ExampleMod.MODID, "example");

    public ExampleMultiblock() {
        super(TEMPLATE_LOCATION,
                new BlockPos(1, 1, 1),  // masterFromOrigin: 中心位置
                new BlockPos(2, 0, 2),  // triggerFromOrigin: 右下角触发位置
                new BlockPos(3, 3, 3)   // size: 3x3x3结构
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
        return state.is(Blocks.IRON_BLOCK);
    }

    @Override
    public boolean createStructure(Level world, BlockPos pos, Direction side, Player player) {
        try {
            Direction facing = side.getOpposite();
            BlockPos startPos = getStartPos(pos, facing);

            if (checkLayer(world, startPos, facing, 0) || // 底层
                    !checkMiddleLayer(world, startPos, facing) || // 中间层
                    checkLayer(world, startPos, facing, 2)) {  // 顶层
                return false;
            }

            for(int y = 0; y < 3; y++) {
                for(int x = 0; x < 3; x++) {
                    for(int z = 0; z < 3; z++) {
                        BlockPos blockPos = getActualPos(startPos, x, y, z, facing);
                        if(y == 1) {
                            if(x == 1 && z == 1) {
                                world.setBlockAndUpdate(blockPos, Blocks.DIAMOND_BLOCK.defaultBlockState());

                                if (world instanceof ServerLevel serverLevel) {
                                    checkBlocks(blockPos, serverLevel);
                                }

                                world.playSound(null,
                                        blockPos,
                                        SoundEvents.BEACON_ACTIVATE,
                                        SoundSource.BLOCKS,
                                        1.0F, 1.0F
                                );
                            } else {
                                world.setBlockAndUpdate(blockPos, Blocks.GLASS.defaultBlockState());
                            }
                        } else {
                            world.setBlockAndUpdate(blockPos, Blocks.IRON_BLOCK.defaultBlockState());
                        }
                    }
                }
            }

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void checkBlocks(BlockPos blockPos, ServerLevel serverLevel) {
        for (int i = 0; i < 36; i++) {
            double angle = i * Math.PI * 2 / 36;
            double particleX = blockPos.getX() + 0.5 + Math.cos(angle) * 1.5;
            double particleZ = blockPos.getZ() + 0.5 + Math.sin(angle) * 1.5;

            serverLevel.sendParticles(
                    ParticleTypes.END_ROD,
                    particleX, blockPos.getY() + 1.5, particleZ,
                    1, 0, 0, 0,
                    0.05
            );
        }
    }

    private BlockPos getStartPos(BlockPos pos, Direction facing) {
        return switch (facing) {
            case NORTH -> pos.offset(-2, 0, -2);  // 北面：向左和向后偏移
            case SOUTH -> pos;                    // 南面：当前位置即为起点
            case EAST -> pos.offset(0, 0, -2);    // 东面：向后偏移
            case WEST -> pos.offset(-2, 0, 0);    // 西面：向左偏移
            default -> pos;
        };
    }

    private boolean checkLayer(Level world, BlockPos startPos, Direction facing, int y) {
        for (int x = 0; x < 3; x++) {
            for (int z = 0; z < 3; z++) {
                BlockPos checkPos = getActualPos(startPos, x, y, z, facing);
                if (!world.getBlockState(checkPos).is(Blocks.IRON_BLOCK)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean checkMiddleLayer(Level world, BlockPos startPos, Direction facing) {
        for (int x = 0; x < 3; x++) {
            for (int z = 0; z < 3; z++) {
                if (x == 1 && z == 1) continue;  // 跳过中心位置
                BlockPos checkPos = getActualPos(startPos, x, 1, z, facing);
                if (!world.getBlockState(checkPos).is(Blocks.GLASS)) {
                    return false;
                }
            }
        }
        return true;
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
    protected void replaceStructureBlock(StructureBlockInfo info, Level world, BlockPos pos, boolean mirrored, Direction clickDirection, Vec3i offsetFromMaster) {
        if (offsetFromMaster.equals(Vec3i.ZERO)) {
            placeCenterBlock(world, pos);
        } else {
            BlockState newState = info.pos().getY() == 1 ?
                    Blocks.GLASS.defaultBlockState() :
                    Blocks.IRON_BLOCK.defaultBlockState();
            world.setBlockAndUpdate(pos, newState);
        }
    }

    private void placeCenterBlock(Level world, BlockPos pos) {
        world.setBlockAndUpdate(pos, Blocks.DIAMOND_BLOCK.defaultBlockState());

        if (world instanceof ServerLevel serverLevel) {
            spawnFormationParticles(serverLevel, pos);
        }

        world.playSound(null, pos, SoundEvents.BEACON_ACTIVATE,
                SoundSource.BLOCKS, 1.0F, 1.0F);
    }

    private void spawnFormationParticles(ServerLevel serverLevel, BlockPos pos) {
        checkBlocks(pos, serverLevel);
    }

    @Override
    protected void prepareBlockForDisassembly(Level world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        if (state.is(Blocks.DIAMOND_BLOCK)) {
            handleDiamondBlockDisassembly(world, pos);
        } else if (state.is(Blocks.GLASS)) {
            handleGlassBlockDisassembly(world, pos);
        }
    }

    private void handleDiamondBlockDisassembly(Level world, BlockPos pos) {
        if (world instanceof ServerLevel serverLevel) {
            spawnDisassemblyParticles(serverLevel, pos);
        }
        world.playSound(null, pos, SoundEvents.BEACON_DEACTIVATE,
                SoundSource.BLOCKS, 1.0F, 1.0F);
    }

    private void handleGlassBlockDisassembly(Level world, BlockPos pos) {
        if (world instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                    ParticleTypes.ITEM_SNOWBALL,
                    pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    10, 0.3, 0.3, 0.3,
                    0.15
            );
        }
        world.playSound(null, pos, SoundEvents.GLASS_BREAK,
                SoundSource.BLOCKS, 1.0F, 1.0F);
    }

    private void spawnDisassemblyParticles(ServerLevel serverLevel, BlockPos pos) {
        for (int i = 0; i < 50; i++) {
            double x = pos.getX() + 0.5 + (serverLevel.random.nextDouble() - 0.5) * 2;
            double y = pos.getY() + 0.5 + (serverLevel.random.nextDouble() - 0.5) * 2;
            double z = pos.getZ() + 0.5 + (serverLevel.random.nextDouble() - 0.5) * 2;

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
                renderStructure(stack, buffer);
            }
        });
    }

    private void renderStructure(PoseStack stack, MultiBufferSource buffer) {
        stack.pushPose();
        stack.translate(1.5, 1.5, 1.5);

        float time = (System.currentTimeMillis() % 10000) / 10000f;
        stack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(time * 360f));

        stack.translate(-1.5, -1.5, -1.5);

        BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();
        renderBlocks(stack, buffer, dispatcher);

        stack.popPose();
    }

    private void renderBlocks(PoseStack stack, MultiBufferSource buffer, BlockRenderDispatcher dispatcher) {
        for (int x = 0; x < 3; x++) {
            for (int y = 0; y < 3; y++) {
                for (int z = 0; z < 3; z++) {
                    stack.pushPose();
                    stack.translate(x, y, z);

                    BlockState state = getBlockStateForRender(x, y, z);
                    dispatcher.renderSingleBlock(
                            state,
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
        }
    }

    private BlockState getBlockStateForRender(int x, int y, int z) {
        if (y == 1) {
            if (x == 1 && z == 1) {
                return Blocks.DIAMOND_BLOCK.defaultBlockState();
            }
            return Blocks.GLASS.defaultBlockState();
        }
        return Blocks.IRON_BLOCK.defaultBlockState();
    }
} 