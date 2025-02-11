package net.prizowo.examplemod.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.prizowo.examplemod.block.entitiy.GeneratorDevice;
import net.prizowo.examplemod.registry.ModBlockEntities;
import org.jetbrains.annotations.Nullable;
import com.mojang.serialization.MapCodec;

public class GeneratorBlock extends BaseEntityBlock {
    public GeneratorBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new GeneratorDevice(ModBlockEntities.GENERATOR.get(), pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, ModBlockEntities.GENERATOR.get(),
                (level1, pos, state1, blockEntity) -> ((GeneratorDevice) blockEntity).tick());
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(GeneratorBlock::new);
    }
} 