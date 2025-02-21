package net.prizowo.examplemod.villagers;

import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.prizowo.examplemod.ExampleMod;
import net.minecraft.world.entity.npc.VillagerTrades;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

@EventBusSubscriber(modid = ExampleMod.MODID, bus = EventBusSubscriber.Bus.GAME)
public class ModVillagers {
    public static final DeferredRegister<VillagerProfession> VILLAGER_PROFESSIONS =
            DeferredRegister.create(BuiltInRegistries.VILLAGER_PROFESSION, ExampleMod.MODID);

    public static final Supplier<VillagerProfession> ENCHANTER = VILLAGER_PROFESSIONS.register("enchanter",
            () -> new VillagerProfession("enchanter",
                    holder -> holder.is(ModPOIs.ENCHANTER_POI_KEY),
                    holder -> holder.is(ModPOIs.ENCHANTER_POI_KEY),
                    ImmutableSet.<Item>of(),
                    ImmutableSet.<Block>of(),
                    SoundEvents.VILLAGER_WORK_LIBRARIAN));

    @SubscribeEvent
    public static void addCustomTrades(VillagerTradesEvent event) {
        if (event.getType() == ENCHANTER.get()) {
            Int2ObjectMap<List<VillagerTrades.ItemListing>> trades = event.getTrades();
            
            // 新手 - 低级附魔书
            trades.get(1).add((trader, rand) -> {
                List<Holder<Enchantment>> enchantments = new ArrayList<>();
                trader.level().registryAccess().registryOrThrow(Registries.ENCHANTMENT).holders().forEach(enchantments::add);
                Holder<Enchantment> enchantment = enchantments.get(rand.nextInt(enchantments.size()));
                int level = Math.min(enchantment.value().getMaxLevel(), 1);
                ItemStack book = EnchantedBookItem.createForEnchantment(new EnchantmentInstance(enchantment, level));
                return new MerchantOffer(new ItemCost(Items.EMERALD, 5 + rand.nextInt(3)), Optional.empty(), book, 12, 2, 0.05f);
            });

            // 学徒 - 中低级附魔书
            trades.get(2).add((trader, rand) -> {
                List<Holder<Enchantment>> enchantments = new ArrayList<>();
                trader.level().registryAccess().registryOrThrow(Registries.ENCHANTMENT).holders().forEach(enchantments::add);
                Holder<Enchantment> enchantment = enchantments.get(rand.nextInt(enchantments.size()));
                int level = Math.min(enchantment.value().getMaxLevel(), 2);
                ItemStack book = EnchantedBookItem.createForEnchantment(new EnchantmentInstance(enchantment, level));
                return new MerchantOffer(new ItemCost(Items.EMERALD, 10 + rand.nextInt(5)), Optional.empty(), book, 12, 5, 0.05f);
            });

            // 老手 - 中级附魔书
            trades.get(3).add((trader, rand) -> {
                List<Holder<Enchantment>> enchantments = new ArrayList<>();
                trader.level().registryAccess().registryOrThrow(Registries.ENCHANTMENT).holders().forEach(enchantments::add);
                Holder<Enchantment> enchantment = enchantments.get(rand.nextInt(enchantments.size()));
                int level = Math.min(enchantment.value().getMaxLevel(), 3);
                ItemStack book = EnchantedBookItem.createForEnchantment(new EnchantmentInstance(enchantment, level));
                return new MerchantOffer(new ItemCost(Items.EMERALD, 15 + rand.nextInt(5)), Optional.empty(), book, 12, 10, 0.05f);
            });

            // 专家 - 高级附魔书
            trades.get(4).add((trader, rand) -> {
                List<Holder<Enchantment>> enchantments = new ArrayList<>();
                trader.level().registryAccess().registryOrThrow(Registries.ENCHANTMENT).holders().forEach(enchantments::add);
                Holder<Enchantment> enchantment = enchantments.get(rand.nextInt(enchantments.size()));
                int level = Math.min(enchantment.value().getMaxLevel(), 4);
                ItemStack book = EnchantedBookItem.createForEnchantment(new EnchantmentInstance(enchantment, level));
                return new MerchantOffer(new ItemCost(Items.EMERALD, 20 + rand.nextInt(5)), Optional.empty(), book, 12, 15, 0.05f);
            });

            // 大师 - 最高级附魔书
            trades.get(5).add((trader, rand) -> {
                List<Holder<Enchantment>> enchantments = new ArrayList<>();
                trader.level().registryAccess().registryOrThrow(Registries.ENCHANTMENT).holders().forEach(enchantments::add);
                Holder<Enchantment> enchantment = enchantments.get(rand.nextInt(enchantments.size()));
                int level = enchantment.value().getMaxLevel(); // 直接使用最高等级
                ItemStack book = EnchantedBookItem.createForEnchantment(new EnchantmentInstance(enchantment, level));
                return new MerchantOffer(new ItemCost(Items.EMERALD, 30 + rand.nextInt(5)), Optional.empty(), book, 12, 30, 0.05f);
            });

            // 为每个等级添加额外的交易选项
            for (int level = 1; level <= 5; level++) {
                final int villagerLevel = level;
                // 添加第二个交易选项
                trades.get(level).add((trader, rand) -> {
                    List<Holder<Enchantment>> enchantments = new ArrayList<>();
                    trader.level().registryAccess().registryOrThrow(Registries.ENCHANTMENT).holders().forEach(enchantments::add);
                    Holder<Enchantment> enchantment = enchantments.get(rand.nextInt(enchantments.size()));
                    int enchLevel = Math.min(enchantment.value().getMaxLevel(), villagerLevel);
                    ItemStack book = EnchantedBookItem.createForEnchantment(new EnchantmentInstance(enchantment, enchLevel));
                    return new MerchantOffer(new ItemCost(Items.EMERALD, (5 * villagerLevel) + rand.nextInt(5)), Optional.empty(), book, 12, 5 * villagerLevel, 0.05f);
                });
                
                // 添加第三个交易选项
                trades.get(level).add((trader, rand) -> {
                    List<Holder<Enchantment>> enchantments = new ArrayList<>();
                    trader.level().registryAccess().registryOrThrow(Registries.ENCHANTMENT).holders().forEach(enchantments::add);
                    Holder<Enchantment> enchantment = enchantments.get(rand.nextInt(enchantments.size()));
                    int enchLevel = Math.min(enchantment.value().getMaxLevel(), villagerLevel);
                    ItemStack book = EnchantedBookItem.createForEnchantment(new EnchantmentInstance(enchantment, enchLevel));
                    return new MerchantOffer(new ItemCost(Items.EMERALD, (5 * villagerLevel) + rand.nextInt(5)), Optional.empty(), book, 12, 5 * villagerLevel, 0.05f);
                });
            }
        }
    }
}