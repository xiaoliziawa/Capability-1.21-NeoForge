package net.prizowo.examplemod.mixin.create;

import net.prizowo.examplemod.util.ChainEntityWrapper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.simibubi.create.content.kinetics.chainConveyor.ChainConveyorBlock;
import com.simibubi.create.content.kinetics.chainConveyor.ChainConveyorBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.LeadItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Comparator;

@Mixin(ChainConveyorBlock.class)
public class ChainConveyorBlockMixin {

    @Inject(method = "useItemOn", at = @At("HEAD"), cancellable = true)
    private void onUseItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, 
                     Player player, InteractionHand hand, 
                     BlockHitResult hitResult, CallbackInfoReturnable<ItemInteractionResult> cir) {
        if (stack.getItem() instanceof LeadItem) {
            if (handleLeashInteraction(level, pos, player)) {
                cir.setReturnValue(ItemInteractionResult.SUCCESS);
                return;
            }
        }
        
        if (stack.isEmpty() && player.isShiftKeyDown()) {
            if (handleDirectEntityInteraction(level, pos, player)) {
                cir.setReturnValue(ItemInteractionResult.SUCCESS);
                return;
            }
        }
    }
    
    private boolean handleLeashInteraction(Level level, BlockPos pos, Player player) {
        if (level.isClientSide() || !(level.getBlockEntity(pos) instanceof ChainConveyorBlockEntity blockEntity))
            return false;
        
        AABB searchBox = new AABB(pos).inflate(10.0D);
        List<Mob> nearbyEntities = level.getEntitiesOfClass(Mob.class, searchBox, 
            entity -> entity.isLeashed() && entity.getLeashHolder() == player);
        
        if (!nearbyEntities.isEmpty()) {
            Mob targetEntity = nearbyEntities.get(0);
            
            if (!hasEnoughSpace(level, pos, targetEntity)) {
                level.playSound(null, pos, SoundEvents.CHAIN_FALL, SoundSource.BLOCKS, 0.5F, 0.7F);
                return false;
            }
            
            targetEntity.noPhysics = true;
            targetEntity.setNoGravity(true);
            
            float randomAngle = (float)(Math.random() * 360);
            ChainEntityWrapper wrapper = new ChainEntityWrapper(targetEntity);
            wrapper.setLeashed(true);
            wrapper.chainPosition = randomAngle;
            
            if (blockEntity.addLoopingPackage(wrapper)) {
                targetEntity.dropLeash(true, false);
                createHangEffect(level, targetEntity);
                return true;
            } else {
                targetEntity.noPhysics = false;
                targetEntity.setNoGravity(false);
            }
        }
        
        return false;
    }
    
    private boolean handleDirectEntityInteraction(Level level, BlockPos pos, Player player) {
        if (level.isClientSide() || !(level.getBlockEntity(pos) instanceof ChainConveyorBlockEntity blockEntity))
            return false;
        
        if (!blockEntity.canAcceptMorePackages())
            return false;
        
        AABB searchBox = new AABB(pos).inflate(5.0D);
        List<LivingEntity> nearbyEntities = level.getEntitiesOfClass(LivingEntity.class, searchBox,
            entity -> entity != player && !entity.isSpectator() && entity.isAlive());
        
        if (!nearbyEntities.isEmpty()) {
            nearbyEntities.sort(Comparator.comparingDouble(entity -> entity.distanceToSqr(player)));
            
            LivingEntity targetEntity = nearbyEntities.get(0);
            
            if (targetEntity.distanceToSqr(player) > 3.0D)
                return false;
                
            if (!hasEnoughSpace(level, pos, targetEntity)) {
                level.playSound(null, pos, SoundEvents.CHAIN_FALL, SoundSource.BLOCKS, 0.5F, 0.7F);
                return false;
            }
            
            targetEntity.noPhysics = true;
            targetEntity.setNoGravity(true);
            
            float randomAngle = (float)(Math.random() * 360);
            ChainEntityWrapper wrapper = new ChainEntityWrapper(targetEntity);
            wrapper.chainPosition = randomAngle;
            
            if (blockEntity.addLoopingPackage(wrapper)) {
                createHangEffect(level, targetEntity);
                return true;
            } else {
                targetEntity.noPhysics = false;
                targetEntity.setNoGravity(false);
            }
        }
        
        return false;
    }
    

    private boolean hasEnoughSpace(Level level, BlockPos pos, Entity entity) {
        float entityHeight = entity.getBbHeight();
        
        BlockPos belowPos = pos.below();
        int spacesNeeded = Math.max(1, (int)Math.ceil(entityHeight));
        
        for (int i = 0; i < spacesNeeded; i++) {
            BlockPos checkPos = belowPos.below(i);
            if (!level.getBlockState(checkPos).getCollisionShape(level, checkPos).isEmpty()) {
                return false;
            }
        }
        
        return true;
    }

    private void createHangEffect(Level level, Entity entity) {
        level.playSound(null, entity.blockPosition(), SoundEvents.CHAIN_PLACE, SoundSource.BLOCKS, 0.5F, 1.0F);
        
        Vec3 pos = entity.position();
        for (int i = 0; i < 10; i++) {
            double xOffset = (Math.random() - 0.5) * entity.getBbWidth();
            double yOffset = Math.random() * entity.getBbHeight();
            double zOffset = (Math.random() - 0.5) * entity.getBbWidth();
            
            level.addParticle(
                ParticleTypes.CRIT,
                pos.x + xOffset,
                pos.y + yOffset,
                pos.z + zOffset,
                0, 0.1, 0
            );
        }
    }
}