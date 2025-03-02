package net.prizowo.examplemod.util;

import com.simibubi.create.content.kinetics.chainConveyor.ChainConveyorPackage;
import com.simibubi.create.content.kinetics.chainConveyor.ChainConveyorBlockEntity;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;

public class ChainEntityWrapper extends ChainConveyorPackage {
    
    private Entity entity;
    private int entityId;
    private boolean isLeashed = false;
    private Vec3 lastPosition = Vec3.ZERO;
    private boolean forcedPosition = false;
    
    private static final ItemStack DUMMY_ITEM = createDummyItem();
    
    private static ItemStack createDummyItem() {
        return new ItemStack(Items.AIR);
    }
    
    public ChainEntityWrapper(Entity entity) {
        super(0.0f, DUMMY_ITEM.copy());
        this.entity = entity;
        this.entityId = entity.getId();

        disableEntityPhysics(entity);
    }
    
    private void disableEntityPhysics(Entity entity) {

        entity.noPhysics = true;
        entity.setNoGravity(true);
        entity.setOnGround(false);
        entity.resetFallDistance();
        entity.setDeltaMovement(0, 0, 0);
        
        entity.setInvulnerable(true);
        
        if (entity instanceof Mob mob) {
            mob.getNavigation().stop();
        }
        
        entity.hasImpulse = true;
    }
    
    public static ChainEntityWrapper fromEntity(Entity entity, Vec3 position) {
        ChainEntityWrapper wrapper = new ChainEntityWrapper(entity);
        wrapper.worldPosition = position;
        wrapper.lastPosition = position;
        return wrapper;
    }
    
    public Entity getEntity() {
        return entity;
    }
    
    public void setEntity(Entity entity) {
        this.entity = entity;
        this.entityId = entity.getId();

        disableEntityPhysics(entity);
    }
    
    public int getEntityId() {
        return entityId;
    }
    
    public boolean isLeashed() {
        return isLeashed;
    }
    
    public void setLeashed(boolean leashed) {
        this.isLeashed = leashed;
    }
    
    @Override
    public CompoundTag write(HolderLookup.Provider registries) {
        CompoundTag tag = super.write(registries);
        tag.putInt("EntityId", entityId);
        tag.putBoolean("IsEntityWrapper", true);
        tag.putBoolean("IsLeashed", isLeashed);
        if (entity != null) {
            ResourceLocation entityId = EntityType.getKey(entity.getType());
            tag.putString("EntityType", entityId.toString());
        }
        return tag;
    }
    
    @Override
    public CompoundTag writeToClient(HolderLookup.Provider registries) {
        CompoundTag tag = super.writeToClient(registries);
        tag.putInt("EntityId", entityId);
        tag.putBoolean("IsEntityWrapper", true);
        tag.putBoolean("IsLeashed", isLeashed);
        return tag;
    }
    
    public static ChainEntityWrapper read(CompoundTag tag, HolderLookup.Provider registries, Level level) {
        ChainConveyorPackage base = ChainConveyorPackage.read(tag, registries);
        
        int entityId = tag.getInt("EntityId");
        Entity entity = level.getEntity(entityId);

        ChainEntityWrapper wrapper = new ChainEntityWrapper(entity);
        wrapper.chainPosition = base.chainPosition;
        wrapper.worldPosition = base.worldPosition;
        wrapper.lastPosition = base.worldPosition;
        wrapper.yaw = base.yaw;
        wrapper.justFlipped = base.justFlipped;
        wrapper.entityId = entityId;
        wrapper.isLeashed = tag.getBoolean("IsLeashed");
        
        return wrapper;
    }
    
    public void updateEntityPosition(ChainConveyorBlockEntity blockEntity) {
        if (entity == null || entity.isRemoved()) return;
        
        if (worldPosition != null) {
            float yOffset = getEntityHangOffset();
            
            Vec3 newPos = new Vec3(
                worldPosition.x,
                worldPosition.y - yOffset - 1.0,
                worldPosition.z
            );
            
            entity.setPos(newPos.x, newPos.y, newPos.z);
            
            Vec3 currentMotion = entity.getDeltaMovement();
            if (Math.abs(currentMotion.y) > 0.001) {
                entity.setDeltaMovement(currentMotion.x * 0.2, 0, currentMotion.z * 0.2);
            }
            
            if (!forcedPosition) {
                entity.setYRot(yaw);
                entity.setXRot(0);
            }
            
            entity.setAirSupply(entity.getMaxAirSupply());
            
            lastPosition = newPos;
            
            entity.noPhysics = true;
            entity.setNoGravity(true);
            
            entity.hasImpulse = true;
            
            if (!forcedPosition && entity.level().isClientSide()) {
                try {
                    entity.lerpTo(newPos.x, newPos.y, newPos.z, entity.getYRot(), entity.getXRot(), 3);
                    forcedPosition = true;
                } catch (Exception ignored) {

                }
            }
        }
    }
    
    private float getEntityHangOffset() {
        if (entity == null) return 0.7f;
        
        float height = entity.getBbHeight();
        float width = entity.getBbWidth();
        
        float offset;
        
        if (height > 2.0f || width > 2.0f) {
            offset = Math.max(0.8f, height * 0.5f);
        }
        else if (height > 1.0f || width > 1.0f) {
            offset = Math.max(0.6f, height * 0.6f);
        }
        else {
            offset = Math.max(0.4f, height * 0.7f);
        }
        
        return offset;
    }
    
    public void releaseEntity() {
        if (entity == null || entity.isRemoved()) return;
        
        if (lastPosition != Vec3.ZERO) {
            entity.setPos(lastPosition.x, lastPosition.y, lastPosition.z);
        }
        
        entity.noPhysics = false;
        entity.setNoGravity(false);
        entity.setInvulnerable(false);
        entity.setOnGround(false);
        
        entity.setDeltaMovement(0, -0.1, 0);
        
        if (entity instanceof Mob mob) {
            mob.setNoAi(false);
            
            mob.getNavigation().stop();
            mob.getNavigation().recomputePath();
            
            if (isLeashed && !mob.isLeashed()) {
                isLeashed = false;
            }
        }
        
        entity.hasImpulse = true;
        
        forcedPosition = false;
    }
    
    public void removeFromChain(ChainConveyorBlockEntity blockEntity) {
        releaseEntity();
        
        if (blockEntity.getLoopingPackages().contains(this)) {
            blockEntity.getLoopingPackages().remove(this);
            blockEntity.notifyUpdate();
        } else {
            for (var packages : blockEntity.getTravellingPackages().values()) {
                if (packages.contains(this)) {
                    packages.remove(this);
                    blockEntity.notifyUpdate();
                    break;
                }
            }
        }
    }
} 