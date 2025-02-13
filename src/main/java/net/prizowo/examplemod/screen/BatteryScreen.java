package net.prizowo.examplemod.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.prizowo.examplemod.ExampleMod;
import org.jetbrains.annotations.NotNull;

public class BatteryScreen extends AbstractContainerScreen<BatteryMenu> {
    private static final ResourceLocation TEXTURE = 
        ResourceLocation.fromNamespaceAndPath(ExampleMod.MODID, "textures/gui/battery.png");

    public BatteryScreen(BatteryMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        graphics.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight);

        int energyHeight = menu.getEnergy() * 50 / Math.max(1, menu.getMaxEnergy());
        graphics.blit(TEXTURE, x + 80, y + 17 + (50 - energyHeight), 
            176, 0, 16, energyHeight);
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        renderBackground(graphics, mouseX, mouseY, delta);
        super.render(graphics, mouseX, mouseY, delta);
        renderTooltip(graphics, mouseX, mouseY);

        if (mouseX >= leftPos + 80 && mouseX <= leftPos + 96 &&
            mouseY >= topPos + 17 && mouseY <= topPos + 67) {
            String energyText = menu.getEnergy() + " / " + menu.getMaxEnergy() + " FE";
            graphics.renderTooltip(font, Component.literal(energyText), mouseX, mouseY);
        }
    }
} 