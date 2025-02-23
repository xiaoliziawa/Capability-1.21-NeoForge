package net.prizowo.examplemod.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.ChatFormatting;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.HangingSignBlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.server.network.FilteredText;
import net.prizowo.examplemod.api.DisplayEntityAccessor;
import net.prizowo.examplemod.api.ISignDisplay;
import net.prizowo.examplemod.util.SignDisplayManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.UUID;

@Mixin(SignBlockEntity.class)
public abstract class SignBlockEntityMixin extends BlockEntity implements ISignDisplay {
    @Shadow public abstract SignText getFrontText();

    private SignBlockEntityMixin() {
        super(null, null, null);
    }

    @Override
    public void updateDisplay(ServerLevel level, BlockPos pos) {
        SignText signText = getFrontText();
        StringBuilder fullText = new StringBuilder();
        DyeColor dyeColor = signText.getColor();

        for (int i = 0; i < 4; i++) {
            Component lineText = signText.getMessage(i, false);
            if (!lineText.getString().isEmpty()) {
                if (fullText.length() > 0) {
                    fullText.append("\n");
                }
                fullText.append(lineText.getString());
            }
        }

        if (fullText.length() == 0) {
            removeDisplayEntity(level, pos);
            return;
        }

        Display.TextDisplay textDisplay;
        UUID existingUUID = SignDisplayManager.getDisplays().get(pos);

        if (existingUUID != null && level.getEntity(existingUUID) instanceof Display.TextDisplay existing) {
            textDisplay = existing;
        } else {
            textDisplay = EntityType.TEXT_DISPLAY.create(level);
            if (textDisplay == null) return;

            boolean isHangingSign = (Object)this instanceof HangingSignBlockEntity;
            double yOffset = isHangingSign ? -0.5 : 1.5;

            textDisplay.setPos(pos.getX() + 0.5, pos.getY() + yOffset, pos.getZ() + 0.5);

            ((DisplayEntityAccessor)textDisplay).accessor$setBillboardConstraints(Display.BillboardConstraints.CENTER);
            ((DisplayEntityAccessor)textDisplay).accessor$setBackgroundColor(0x00000000);
            ((DisplayEntityAccessor)textDisplay).accessor$setLineWidth(200);

            level.addFreshEntity(textDisplay);
            SignDisplayManager.getDisplays().put(pos, textDisplay.getUUID());
        }

        Style style = Style.EMPTY.withBold(true);
        if (dyeColor != null) {
            style = style.withColor(dyeColor.getTextColor());
        } else {
            style = style.withColor(ChatFormatting.WHITE);
        }

        if (signText.hasGlowingText()) {
            style = style.withBold(true);
            if (style.getColor() == null) {
                style = style.withColor(ChatFormatting.YELLOW);
            }
        }

        ((DisplayEntityAccessor)textDisplay).accessor$setText(
            Component.literal(fullText.toString()).withStyle(style)
        );
    }

    @Inject(method = "markUpdated", at = @At("HEAD"))
    private void onMarkUpdated(CallbackInfo ci) {
        if (this.level == null || !(this.level instanceof ServerLevel level)) return;
        updateDisplay(level, this.getBlockPos());
    }

    @Inject(method = "updateSignText", at = @At("TAIL"))
    private void onUpdateSignText(Player player, boolean isFrontText, List<FilteredText> filteredText, CallbackInfo ci) {
        if (this.level == null || !(this.level instanceof ServerLevel level)) return;
        updateDisplay(level, this.getBlockPos());
    }

    @Inject(method = "saveAdditional", at = @At("TAIL"))
    private void onSaveAdditional(CompoundTag tag, HolderLookup.Provider registries, CallbackInfo ci) {
        tag.putBoolean("hasTextDisplay", SignDisplayManager.getDisplays().containsKey(this.getBlockPos()));
    }

    @Inject(method = "loadAdditional", at = @At("TAIL"))
    private void onLoadAdditional(CompoundTag tag, HolderLookup.Provider registries, CallbackInfo ci) {
        if (this.level instanceof ServerLevel level) {
            if (tag.getBoolean("hasTextDisplay")) {
                updateDisplay(level, this.getBlockPos());
            }
        }
    }

    private void removeDisplayEntity(ServerLevel level, BlockPos pos) {
        UUID displayUUID = SignDisplayManager.getDisplays().remove(pos);
        if (displayUUID != null) {
            if (level.getEntity(displayUUID) instanceof Display.TextDisplay display) {
                display.remove(Display.RemovalReason.DISCARDED);
            }
        }
    }

    @Override
    public void setRemoved() {
        if (this.level instanceof ServerLevel level) {
            removeDisplayEntity(level, this.getBlockPos());
        }
        super.setRemoved();
    }
} 