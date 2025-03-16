package net.prizowo.examplemod.event;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.prizowo.examplemod.enchantment.ModEnchantments;

public class EnchantmentEvents {
    
    /**
     * 当玩家攻击实体时触发，用于处理凋零附魔效果
     */
    @SubscribeEvent
    public static void onPlayerAttackEntity(AttackEntityEvent event) {
        Player player = event.getEntity();
        
        ItemStack weapon = player.getMainHandItem();
        
        var lookup = CommonHooks.resolveLookup(
            Registries.ENCHANTMENT);
        if (lookup == null) return;
        
        var enchantments = weapon.getAllEnchantments(lookup);
        
        for (var entry : enchantments.entrySet()) {
            ResourceKey<Enchantment> enchantmentKey = entry.getKey().unwrapKey().orElse(null);
            
            if (enchantmentKey != null && enchantmentKey.equals(ModEnchantments.WITHER_BLADE)) {
                int level = entry.getIntValue();
                
                float chance = 0.1f + level * 0.05f;
                
                if (player.getRandom().nextFloat() < chance) {
                    int duration = 40 + level * 20;
                    int amplifier = Math.max(1, level / 2);
                    
                    if (event.getTarget() instanceof LivingEntity livingTarget) {
                        livingTarget.addEffect(new MobEffectInstance(MobEffects.WITHER, duration, amplifier - 1));
                    }
                }
                
                break;
            }
        }
    }
} 