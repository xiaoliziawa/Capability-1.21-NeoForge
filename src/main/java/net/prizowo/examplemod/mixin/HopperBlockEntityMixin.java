package net.prizowo.examplemod.mixin;

import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import javax.annotation.Nullable;

@Mixin(HopperBlockEntity.class)
public class HopperBlockEntityMixin {


    @Shadow
    private static ItemStack addItem(Container source, Container destination, ItemStack stack, Direction direction) {
        throw new AssertionError();
    }

    @Shadow
    @Nullable
    private static Container getAttachedContainer(Level level, BlockPos pos, HopperBlockEntity blockEntity) {
        throw new AssertionError();
    }
    
    @Inject(method = "setCooldown", at = @At("HEAD"), cancellable = true)
    private void onSetCooldown(int cooldownTime, CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "ejectItems", at = @At("HEAD"), cancellable = true)
    private static void onEjectItems(Level level, BlockPos pos, HopperBlockEntity blockEntity, CallbackInfoReturnable<Boolean> cir) {
        Container container = getAttachedContainer(level, pos, blockEntity);
        if (container == null) {
            cir.setReturnValue(false);
            return;
        }

        Direction direction = blockEntity.getBlockState().getValue(net.minecraft.world.level.block.HopperBlock.FACING).getOpposite();
        
        for(int i = 0; i < blockEntity.getContainerSize(); ++i) {
            ItemStack itemstack = blockEntity.getItem(i);
            if (!itemstack.isEmpty()) {
                int transferAmount = Math.min(64, itemstack.getCount());
                ItemStack toTransfer = itemstack.copy();
                toTransfer.setCount(transferAmount);
                
                ItemStack leftover = addItem(blockEntity, container, toTransfer, direction);
                if (leftover.isEmpty()) {
                    itemstack.shrink(transferAmount);
                    blockEntity.setChanged();
                    cir.setReturnValue(true);
                    return;
                }
            }
        }
        cir.setReturnValue(false);
    }
} 