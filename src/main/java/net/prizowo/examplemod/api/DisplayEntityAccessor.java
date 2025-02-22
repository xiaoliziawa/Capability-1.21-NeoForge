package net.prizowo.examplemod.api;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Display;
import net.minecraft.world.level.block.state.BlockState;
import com.mojang.math.Transformation;

public interface DisplayEntityAccessor {
    void accessor$setBlockState(BlockState state);
    void accessor$setTransformation(Transformation transformation);
    void accessor$setText(Component text);
    void accessor$setBackgroundColor(int color);
    void accessor$setLineWidth(int width);
    void accessor$setBillboardConstraints(Display.BillboardConstraints constraints);
}