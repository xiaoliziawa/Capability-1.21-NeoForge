package net.prizowo.examplemod.mixin.create;

import net.prizowo.examplemod.util.ChainEntityWrapper;
import com.simibubi.create.content.kinetics.chainConveyor.ChainConveyorPackage;
import com.simibubi.create.content.kinetics.chainConveyor.ChainConveyorBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Mixin(ChainConveyorBlockEntity.class)
public abstract class ChainConveyorBlockEntityMixin extends BlockEntity {

    @Shadow public abstract List<ChainConveyorPackage> getLoopingPackages();
    @Shadow public abstract Map<Integer, List<ChainConveyorPackage>> getTravellingPackages();
    
    public ChainConveyorBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }
    
    @Inject(method = "tick", at = @At("TAIL"))
    private void onTick(CallbackInfo ci) {
        if (level == null || level.isClientSide()) return;
        
        if (level.getBlockEntity(getBlockPos()) == null || !level.getBlockEntity(getBlockPos()).equals(this)) {
            releaseAllEntities();
            return;
        }
        
        List<ChainConveyorPackage> packages = new ArrayList<>(getLoopingPackages());
        
        for (ChainConveyorPackage pack : packages) {
            if (pack instanceof ChainEntityWrapper wrapper) {
                Entity entity = wrapper.getEntity();
                
                if (entity == null || entity.isRemoved()) {
                    getLoopingPackages().remove(wrapper);
                    continue;
                }
                
                updateEntityPackage(wrapper);
                
                entity.setNoGravity(true);
                entity.setOnGround(false);
                entity.resetFallDistance();
            }
        }
        
        for (List<ChainConveyorPackage> packageList : getTravellingPackages().values()) {
            List<ChainConveyorPackage> travelPackages = new ArrayList<>(packageList);
            
            for (ChainConveyorPackage pack : travelPackages) {
                if (pack instanceof ChainEntityWrapper wrapper) {
                    Entity entity = wrapper.getEntity();
                    
                    if (entity == null || entity.isRemoved()) {
                        packageList.remove(wrapper);
                        continue;
                    }
                    
                    updateEntityPackage(wrapper);
                    
                    entity.setNoGravity(true);
                    entity.setOnGround(false);
                    entity.resetFallDistance();
                }
            }
        }
    }
    
    @Inject(method = "remove", at = @At("HEAD"))
    private void onRemove(CallbackInfo ci) {
        releaseAllEntities();
    }
    
    @Inject(method = "destroy", at = @At("HEAD"))
    private void onDestroy(CallbackInfo ci) {
        releaseAllEntities();
    }
    @Inject(method = "remove", at = @At("HEAD"))
    private void onSetRemoved(CallbackInfo ci) {
        releaseAllEntities();
    }
    
    private void releaseAllEntities() {
        if (level == null) return;
        
        List<ChainEntityWrapper> entityWrappers = new ArrayList<>();
        
        for (ChainConveyorPackage pack : getLoopingPackages()) {
            if (pack instanceof ChainEntityWrapper wrapper) {
                entityWrappers.add(wrapper);
            }
        }
        
        for (List<ChainConveyorPackage> packages : getTravellingPackages().values()) {
            for (ChainConveyorPackage pack : packages) {
                if (pack instanceof ChainEntityWrapper wrapper) {
                    entityWrappers.add(wrapper);
                }
            }
        }
        
        for (ChainEntityWrapper wrapper : entityWrappers) {
            Entity entity = wrapper.getEntity();
            if (entity != null && !entity.isRemoved()) {
                wrapper.releaseEntity();
                
                entity.noPhysics = false;
                entity.setNoGravity(false);
                entity.setOnGround(false);
                entity.setDeltaMovement(0, -0.1, 0);
                entity.hasImpulse = true;
            }
        }
        
        getLoopingPackages().removeAll(entityWrappers);
        
        for (List<ChainConveyorPackage> packages : getTravellingPackages().values()) {
            packages.removeIf(pack -> pack instanceof ChainEntityWrapper);
        }
    }

    private void updateEntityPackage(ChainEntityWrapper wrapper) {
        Entity entity = wrapper.getEntity();
        if (entity == null || entity.isRemoved()) return;
        
        if (entity.level() != level) return;
        
        if (!entity.isNoGravity() || !entity.noPhysics) {
            entity.noPhysics = true;
            entity.setNoGravity(true);
            entity.setOnGround(false);
            entity.resetFallDistance();
        }
        
        wrapper.updateEntityPosition((ChainConveyorBlockEntity)(Object)this);
        
        if (entity.getDeltaMovement().lengthSqr() > 0.0001) {
            entity.setDeltaMovement(0, 0, 0);
        }
        
        entity.hasImpulse = true;
    }
    
    @Inject(method = "addLoopingPackage", at = @At("HEAD"), cancellable = true)
    private void onAddLoopingPackage(ChainConveyorPackage pack, CallbackInfoReturnable<Boolean> cir) {
        if (!(pack instanceof ChainEntityWrapper wrapper)) return;
        
        Entity targetEntity = wrapper.getEntity();
        if (targetEntity == null) return;
        
        boolean exists = false;
        
        for (ChainConveyorPackage existing : getLoopingPackages()) {
            if (existing instanceof ChainEntityWrapper existingWrapper) {
                Entity existingEntity = existingWrapper.getEntity();
                if (existingEntity != null && existingEntity.getId() == targetEntity.getId()) {
                    exists = true;
                    break;
                }
            }
        }
        
        if (!exists) {
            for (List<ChainConveyorPackage> packages : getTravellingPackages().values()) {
                for (ChainConveyorPackage existing : packages) {
                    if (existing instanceof ChainEntityWrapper existingWrapper) {
                        Entity existingEntity = existingWrapper.getEntity();
                        if (existingEntity != null && existingEntity.getId() == targetEntity.getId()) {
                            exists = true;
                            break;
                        }
                    }
                }
                if (exists) break;
            }
        }
        
        if (exists) {
            cir.setReturnValue(false);
            cir.cancel();
        }
    }
} 