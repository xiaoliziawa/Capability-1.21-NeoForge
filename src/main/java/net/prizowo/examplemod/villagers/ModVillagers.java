package net.prizowo.examplemod.villagers;

import com.google.common.collect.ImmutableSet;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
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
import net.minecraft.util.RandomSource;

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
            event.getTrades().get(1).add(new EnchantedBookForEmeralds()); // 新手级别
            event.getTrades().get(2).add(new EnchantedBookForEmeralds()); // 学徒级别
            event.getTrades().get(3).add(new EnchantedBookForEmeralds()); // 老手级别
            event.getTrades().get(4).add(new EnchantedBookForEmeralds()); // 专家级别
            event.getTrades().get(5).add(new EnchantedBookForEmeralds()); // 大师级别
        }
    }

    public static class EnchantedBookForEmeralds implements VillagerTrades.ItemListing {
        @Override
        public MerchantOffer getOffer(Entity trader, RandomSource random) {
            // 获取所有可用的附魔
            List<Holder<Enchantment>> allEnchantments = new ArrayList<>();
            trader.level().registryAccess().registryOrThrow(Registries.ENCHANTMENT).holders().forEach(allEnchantments::add);

            // 创建三本不同的附魔书
            ItemStack[] books = new ItemStack[3];
            int totalEmeralds = 0;

            // 创建三本不同的附魔书
            for (int i = 0; i < 3; i++) {
                Holder<Enchantment> enchantmentHolder = allEnchantments.get(random.nextInt(allEnchantments.size()));
                int level = Math.min(enchantmentHolder.value().getMaxLevel(), i + 3); // 等级从3开始递增

                // 创建附魔书
                books[i] = EnchantedBookItem.createForEnchantment(new EnchantmentInstance(enchantmentHolder, level));

                // 累加价格，降低基础价格
                totalEmeralds += 3 + (level * 3) + random.nextInt(5); // 降低基础价格
            }

            // 创建交易选项，支持多个输出物品
            return new MerchantOffer(
                new ItemCost(Items.EMERALD, totalEmeralds), // 总价格
                Optional.empty(), // 第二个输入物品（空）
                books[0], // 第一本书作为输出
                6, // 最大使用次数
                20, // 村民经验
                0.1f // 价格乘数
            );
        }
    }
}