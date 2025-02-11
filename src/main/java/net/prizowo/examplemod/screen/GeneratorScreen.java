package net.prizowo.examplemod.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.prizowo.examplemod.ExampleMod;
import org.jetbrains.annotations.NotNull;

public class GeneratorScreen extends AbstractContainerScreen<GeneratorMenu> {
    private static final ResourceLocation TEXTURE = 
        ResourceLocation.fromNamespaceAndPath(ExampleMod.MODID, "textures/gui/generator.png");
    private static final ResourceLocation MISSING_TEXTURE = 
        ResourceLocation.withDefaultNamespace("textures/misc/unknown_pack.png");

    public GeneratorScreen(GeneratorMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        try {
            graphics.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight);

            if (menu.getTotalBurnTime() > 0) {
                int burnProgress = menu.getBurnTime() * 13 / Math.max(1, menu.getTotalBurnTime());
                graphics.blit(TEXTURE, x + 81, y + 54 + 12 - burnProgress, 
                    176, 12 - burnProgress, 14, burnProgress + 1);
            }

            int energyHeight = menu.getEnergy() * 50 / Math.max(1, menu.getMaxEnergy());
            graphics.blit(TEXTURE, x + 154, y + 17 + (50 - energyHeight), 
                176, 31, 16, energyHeight);
        } catch (Exception e) {
            graphics.blit(MISSING_TEXTURE, x, y, 0, 0, imageWidth, imageHeight);
        }
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        renderBackground(graphics, mouseX, mouseY, delta);
        super.render(graphics, mouseX, mouseY, delta);
        renderTooltip(graphics, mouseX, mouseY);

        if (mouseX >= leftPos + 154 && mouseX <= leftPos + 170 &&
            mouseY >= topPos + 17 && mouseY <= topPos + 67) {
            graphics.renderTooltip(font, Component.literal(
                menu.getEnergy() + " / " + menu.getMaxEnergy() + " FE"), 
                mouseX, mouseY);
        }
    }
} 