package net.prizowo.examplemod.mixin.DisplayAccessor;

import com.mojang.math.Transformation;
import net.minecraft.world.entity.Display;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;
import net.prizowo.examplemod.api.DisplayEntityAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Display.class)
public abstract class DisplayEntityMixin implements DisplayEntityAccessor {
    @Shadow protected abstract void setTransformation(Transformation transformation);
    @Shadow protected abstract void setBillboardConstraints(Display.BillboardConstraints constraints);
    
    @Override
    public void accessor$setTransformation(Transformation transformation) {
        setTransformation(transformation);
    }
    
    @Override
    public void accessor$setBillboardConstraints(Display.BillboardConstraints constraints) {
        setBillboardConstraints(constraints);
    }
    
    @Override
    public void accessor$setText(Component text) {}
    
    @Override
    public void accessor$setBackgroundColor(int color) {}
    
    @Override
    public void accessor$setLineWidth(int width) {}
    
    @Override
    public void accessor$setBlockState(BlockState state) {}
}
