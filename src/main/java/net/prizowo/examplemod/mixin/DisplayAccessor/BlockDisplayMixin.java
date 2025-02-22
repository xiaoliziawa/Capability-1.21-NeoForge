package net.prizowo.examplemod.mixin.DisplayAccessor;

import net.minecraft.world.entity.Display;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Display.BlockDisplay.class)
public abstract class BlockDisplayMixin extends DisplayEntityMixin {
    @Shadow protected abstract void setBlockState(BlockState state);
    
    @Override
    public void accessor$setBlockState(BlockState state) {
        setBlockState(state);
    }
}

