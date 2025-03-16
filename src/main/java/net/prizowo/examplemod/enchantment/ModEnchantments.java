package net.prizowo.examplemod.enchantment;

import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.prizowo.examplemod.ExampleMod;

public class ModEnchantments {
    public static final DeferredRegister<Enchantment> ENCHANTMENTS = DeferredRegister.create(
            Registries.ENCHANTMENT, ExampleMod.MODID);
    
    public static final ResourceKey<Enchantment> WITHER_BLADE = key();
    
    private static final TagKey<Item> SWORD_ENCHANTABLE = TagKey.create(
            Registries.ITEM, 
            ResourceLocation.fromNamespaceAndPath(ExampleMod.MODID, "enchantable/swords")
    );

    public static void bootstrap(BootstrapContext<Enchantment> context) {
        HolderGetter<DamageType> damageTypeHolderGetter = context.lookup(Registries.DAMAGE_TYPE);
        HolderGetter<Enchantment> enchantmentHolderGetter = context.lookup(Registries.ENCHANTMENT);
        HolderGetter<Item> itemHolderGetter = context.lookup(Registries.ITEM);
        HolderGetter<Block> blockHolderGetter = context.lookup(Registries.BLOCK);
        
        register(
                context,
                Enchantment.enchantment(
                        Enchantment.definition(
                                itemHolderGetter.getOrThrow(SWORD_ENCHANTABLE),
                                4,
                                10,
                                Enchantment.constantCost(5),
                                Enchantment.constantCost(15),
                                8,
                                EquipmentSlotGroup.MAINHAND
                        )
                )
        );
    }
    
    private static void register(BootstrapContext<Enchantment> context, Enchantment.Builder builder) {
        context.register(ModEnchantments.WITHER_BLADE, builder.build(ModEnchantments.WITHER_BLADE.location()));
    }
    
    private static ResourceKey<Enchantment> key() {
        return ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath(ExampleMod.MODID, "wither_blade"));
    }
    
    public static void register(IEventBus eventBus) {
        ENCHANTMENTS.register(eventBus);
    }
}