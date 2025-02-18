package net.prizowo.examplemod.block;

import appeng.block.networking.ControllerBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.prizowo.examplemod.block.entity.CreativeControllerBlockEntity;
import net.prizowo.examplemod.registry.ModBlockEntities;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CreativeControllerBlock extends ControllerBlock {
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    public CreativeControllerBlock(Properties properties) {
        super();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(POWERED);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new CreativeControllerBlockEntity(ModBlockEntities.CREATIVE_CONTROLLER.get(), pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> type) {
        if (type == ModBlockEntities.CREATIVE_CONTROLLER.get()) {
            return (level1, pos, state1, blockEntity) -> {
                if (blockEntity instanceof CreativeControllerBlockEntity creativeController) {
                    creativeController.updateState();
                }
            };
        }
        return null;
    }

    @Override
    public boolean hasAnalogOutputSignal(@NotNull BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof CreativeControllerBlockEntity) {
            return state.getValue(POWERED) ? 15 : 0;
        }
        return 0;
    }
}