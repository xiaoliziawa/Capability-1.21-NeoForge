package net.prizowo.examplemod.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.network.PacketDistributor;
import net.prizowo.examplemod.block.entity.GeneratorDevice;
import net.prizowo.examplemod.network.OpenGeneratorScreenPacket;
import net.prizowo.examplemod.registry.ModBlockEntities;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.mojang.serialization.MapCodec;

import java.util.List;

public class GeneratorBlock extends BaseEntityBlock {
    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    public GeneratorBlock(Properties properties) {
        super(properties
            .lightLevel(state -> state.getValue(LIT) ? 13 : 0)  // 当燃烧时发出亮度13的光
            .hasPostProcess((state, level, pos) -> state.getValue(LIT))  // 允许光照传播
            .emissiveRendering((state, level, pos) -> state.getValue(LIT))  // 发光渲染
        );
        this.registerDefaultState(this.stateDefinition.any().setValue(LIT, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(LIT);
    }

    @Override
    public void onRemove(BlockState state, @NotNull Level level, @NotNull BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof GeneratorDevice generatorDevice) {
                ItemStackHandler itemHandler = generatorDevice.getItemHandler();
                for(int i = 0; i < itemHandler.getSlots(); i++) {
                    popResource(level, pos, itemHandler.getStackInSlot(i));
                }
            }
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }

    @Override
    public @NotNull List<ItemStack> getDrops(@NotNull BlockState state, LootParams.@NotNull Builder builder) {
        List<ItemStack> drops = super.getDrops(state, builder);
        BlockEntity blockEntity = builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        if (blockEntity instanceof GeneratorDevice generatorDevice) {
            ItemStackHandler itemHandler = generatorDevice.getItemHandler();
            for(int i = 0; i < itemHandler.getSlots(); i++) {
                drops.add(itemHandler.getStackInSlot(i));
            }
        }
        return drops;
    }

    @Override
    protected @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new GeneratorDevice(ModBlockEntities.GENERATOR.get(), pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> type) {
        return type == ModBlockEntities.GENERATOR.get() ? 
            (level1, pos, state1, blockEntity) -> ((GeneratorDevice) blockEntity).tick(level1, pos, state1, (GeneratorDevice)blockEntity) : 
            null;
    }

    @Override
    protected @NotNull MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(GeneratorBlock::new);
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(@NotNull BlockState state, Level level, @NotNull BlockPos pos,
                                                        @NotNull Player player, @NotNull BlockHitResult hitResult) {
        if (level.isClientSide) {
            PacketDistributor.sendToServer(new OpenGeneratorScreenPacket(pos));
        }
        return InteractionResult.SUCCESS;
    }
} 