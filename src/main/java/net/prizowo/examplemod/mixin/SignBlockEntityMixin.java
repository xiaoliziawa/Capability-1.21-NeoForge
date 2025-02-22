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
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.server.network.FilteredText;
import net.prizowo.examplemod.api.DisplayEntityAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@Mixin(SignBlockEntity.class)
public abstract class SignBlockEntityMixin extends BlockEntity {
    @Shadow public abstract SignText getFrontText();

    private static final HashMap<BlockPos, UUID> signDisplays = new HashMap<>();

    private SignBlockEntityMixin() {
        super(null, null, null);
    }

    @Inject(method = "markUpdated", at = @At("HEAD"))
    private void onMarkUpdated(CallbackInfo ci) {
        if (this.level == null || !(this.level instanceof ServerLevel level)) return;
        updateDisplayText(level, this.getBlockPos());
    }

    @Inject(method = "updateSignText", at = @At("TAIL"))
    private void onUpdateSignText(Player player, boolean isFrontText, List<FilteredText> filteredText, CallbackInfo ci) {
        if (this.level == null || !(this.level instanceof ServerLevel level)) return;
        updateDisplayText(level, this.getBlockPos());
    }

    @Inject(method = "loadAdditional", at = @At("TAIL"))
    private void onLoadAdditional(CompoundTag tag, HolderLookup.Provider registries, CallbackInfo ci) {
        if (this.level instanceof ServerLevel level) {
            updateDisplayText(level, this.getBlockPos());
        }
    }

    private void updateDisplayText(ServerLevel level, BlockPos pos) {
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
        UUID existingUUID = signDisplays.get(pos);

        if (existingUUID != null && level.getEntity(existingUUID) instanceof Display.TextDisplay existing) {
            textDisplay = existing;
        } else {
            textDisplay = EntityType.TEXT_DISPLAY.create(level);
            if (textDisplay == null) return;

            textDisplay.setPos(pos.getX() + 0.5, pos.getY() + 1.5, pos.getZ() + 0.5);

            ((DisplayEntityAccessor)textDisplay).accessor$setBillboardConstraints(Display.BillboardConstraints.CENTER);
            ((DisplayEntityAccessor)textDisplay).accessor$setBackgroundColor(0x00000000);
            ((DisplayEntityAccessor)textDisplay).accessor$setLineWidth(200);

            level.addFreshEntity(textDisplay);
            signDisplays.put(pos, textDisplay.getUUID());
        }

        Style style = Style.EMPTY.withBold(true);
        if (dyeColor != null) {
            style = style.withColor(dyeColor.getTextColor());
        } else {
            style = style.withColor(ChatFormatting.WHITE);
        }

        ((DisplayEntityAccessor)textDisplay).accessor$setText(
            Component.literal(fullText.toString()).withStyle(style)
        );
    }

    @Override
    public void setRemoved() {
        if (this.level instanceof ServerLevel level) {
            removeDisplayEntity(level, this.getBlockPos());
        }
        super.setRemoved();
    }

    private void removeDisplayEntity(ServerLevel level, BlockPos pos) {
        UUID displayUUID = signDisplays.remove(pos);
        if (displayUUID != null) {
            Display.TextDisplay display = (Display.TextDisplay) level.getEntity(displayUUID);
            if (display != null) {
                display.remove(Display.RemovalReason.DISCARDED);
            }
        }
    }
} 