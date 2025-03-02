package net.prizowo.examplemod.mixin.create;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.prizowo.examplemod.util.ChainEntityWrapper;
import com.simibubi.create.content.kinetics.chainConveyor.ChainConveyorBlockEntity;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

@Mixin(Entity.class)
public class EntityInteractionMixin {
    @Inject(method = "interact", at = @At("HEAD"), cancellable = true)
    private void onInteract(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        Entity entity = (Entity)(Object)this;
        Level level = entity.level();
        

        if (player.isShiftKeyDown()) {
            return;
        }
        
        if (isEntityOnChain(entity, level)) {

            releaseEntityFromChain(entity, level);
            level.playSound(null, entity.blockPosition(), SoundEvents.CHAIN_BREAK, SoundSource.BLOCKS, 0.5F, 1.2F);
            
            cir.setReturnValue(InteractionResult.SUCCESS);
            cir.cancel();
            
        }
    }
    
    private boolean isEntityOnChain(Entity entity, Level level) {
        if (!entity.isNoGravity() || !entity.noPhysics) {
            return false;
        }
        
        Vec3 pos = entity.position();
        
        int searchRange = 8;
        
        for (int y = -searchRange; y <= searchRange; y++) {
            for (int x = -searchRange; x <= searchRange; x++) {
                for (int z = -searchRange; z <= searchRange; z++) {
                    BlockPos blockPos = new BlockPos(
                        (int)pos.x + x,
                        (int)pos.y + y,
                        (int)pos.z + z
                    );
                    
                    BlockEntity blockEntity = level.getBlockEntity(blockPos);
                    if (blockEntity instanceof ChainConveyorBlockEntity conveyorEntity) {
                        if (isEntityInConveyor(entity, conveyorEntity)) {
                            return true;
                        }
                    }
                }
            }
        }
        
        return false;
    }
    
    private boolean isEntityInConveyor(Entity entity, ChainConveyorBlockEntity conveyor) {
        for (var pack : conveyor.getLoopingPackages()) {
            if (pack instanceof ChainEntityWrapper wrapper) {
                Entity wrappedEntity = wrapper.getEntity();
                if (wrappedEntity != null && wrappedEntity.getId() == entity.getId()) {
                    return true;
                }
            }
        }
        
        for (var packages : conveyor.getTravellingPackages().values()) {
            for (var pack : packages) {
                if (pack instanceof ChainEntityWrapper wrapper) {
                    Entity wrappedEntity = wrapper.getEntity();
                    if (wrappedEntity != null && wrappedEntity.getId() == entity.getId()) {
                        return true;
                    }
                }
            }
        }
        
        return false;
    }
    private void releaseEntityFromChain(Entity entity, Level level) {
        Vec3 pos = entity.position();
        
        int searchRange = 8;
        
        for (int y = -searchRange; y <= searchRange; y++) {
            for (int x = -searchRange; x <= searchRange; x++) {
                for (int z = -searchRange; z <= searchRange; z++) {
                    BlockPos blockPos = new BlockPos(
                        (int)pos.x + x,
                        (int)pos.y + y,
                        (int)pos.z + z
                    );
                    
                    BlockEntity blockEntity = level.getBlockEntity(blockPos);
                    if (blockEntity instanceof ChainConveyorBlockEntity conveyorEntity) {
                        if (releaseEntityFromConveyor(entity, conveyorEntity)) {
                            return;
                        }
                    }
                }
            }
        }
        
    }
    
    private boolean releaseEntityFromConveyor(Entity entity, ChainConveyorBlockEntity conveyor) {
        for (var pack : new java.util.ArrayList<>(conveyor.getLoopingPackages())) {
            if (pack instanceof ChainEntityWrapper wrapper) {
                Entity wrappedEntity = wrapper.getEntity();
                if (wrappedEntity != null && wrappedEntity.getId() == entity.getId()) {
                    wrapper.removeFromChain(conveyor);
                    return true;
                }
            }
        }
        
        for (var entry : new java.util.HashMap<>(conveyor.getTravellingPackages()).entrySet()) {
            var packages = entry.getValue();
            for (var pack : new java.util.ArrayList<>(packages)) {
                if (pack instanceof ChainEntityWrapper wrapper) {
                    Entity wrappedEntity = wrapper.getEntity();
                    if (wrappedEntity != null && wrappedEntity.getId() == entity.getId()) {
                        wrapper.removeFromChain(conveyor);
                        return true;
                    }
                }
            }
        }
        
        return false;
    }
} 