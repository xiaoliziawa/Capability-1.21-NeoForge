package net.prizowo.examplemod.mixin.DisplayAccessor;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Display;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Display.TextDisplay.class)
public abstract class TextDisplayMixin extends DisplayEntityMixin {
    @Shadow protected abstract void setText(Component text);
    @Shadow protected abstract void setBackgroundColor(int color);
    @Shadow protected abstract void setLineWidth(int width);

    @Override
    public void accessor$setText(Component text) {
        setText(text);
    }

    @Override
    public void accessor$setBackgroundColor(int color) {
        setBackgroundColor(color);
    }

    @Override
    public void accessor$setLineWidth(int width) {
        setLineWidth(width);
    }
}
