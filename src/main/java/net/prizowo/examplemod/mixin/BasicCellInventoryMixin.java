package net.prizowo.examplemod.mixin;

import appeng.api.storage.cells.IBasicCellItem;
import appeng.me.cells.BasicCellInventory;
import net.minecraft.world.item.ItemStack;
import net.prizowo.examplemod.item.storage.ExampleStorageCell;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BasicCellInventory.class)
public class BasicCellInventoryMixin {
    @Shadow private ItemStack i;
    @Shadow private IBasicCellItem cellType;

    @Redirect(method = "<init>", at = @At(value = "FIELD", 
            target = "Lappeng/me/cells/BasicCellInventory;maxItemTypes:I", 
            ordinal = 1))
    private int redirectMaxItemTypes(BasicCellInventory instance) {
        if (this.cellType instanceof ExampleStorageCell) {
            return this.cellType.getTotalTypes(this.i);
        }
        return 63;
    }

    @Inject(method = "canHoldNewItem", at = @At("HEAD"), cancellable = true)
    private void onCanHoldNewItem(CallbackInfoReturnable<Boolean> cir) {
        if (this.cellType != null && this.i != null && 
            this.cellType instanceof ExampleStorageCell && 
            this.cellType.getTotalTypes(this.i) == 0) {
            long bytesFree = ((BasicCellInventory)(Object)this).getFreeBytes();
            cir.setReturnValue(bytesFree > 0);
        }
    }

    @Inject(method = "getRemainingItemTypes", at = @At("HEAD"), cancellable = true)
    private void onGetRemainingItemTypes(CallbackInfoReturnable<Long> cir) {
        if (this.cellType != null && this.i != null && 
            this.cellType instanceof ExampleStorageCell && 
            this.cellType.getTotalTypes(this.i) == 0) {
            cir.setReturnValue(Long.MAX_VALUE);
        }
    }
} 