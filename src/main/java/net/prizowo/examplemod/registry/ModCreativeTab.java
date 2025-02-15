package net.prizowo.examplemod.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.prizowo.examplemod.ExampleMod;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ModCreativeTab {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ExampleMod.MODID);

    private static final List<Supplier<ItemStack>> ADDITIONAL_ITEMS = new ArrayList<>();

    public static final Supplier<CreativeModeTab> EXAMPLE_TAB = CREATIVE_MODE_TABS.register("example_tab",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(Blocks.FURNACE))
                    .title(Component.translatable("itemGroup." + ExampleMod.MODID + ".creative"))
                    .displayItems((parameters, output) -> {
                        ModBlocks.BLOCKS.getEntries().forEach(block -> {
                            Item blockItem = block.get().asItem();
                            if (blockItem != null) {
                                output.accept(new ItemStack(blockItem));
                            }
                        });

                        addBlock(Blocks.FURNACE);


                        ModBlocks.ITEMS.getEntries().forEach(item ->
                                output.accept(new ItemStack(item.get())));
                        ADDITIONAL_ITEMS.forEach(itemStack ->
                                output.accept(itemStack.get(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS));
                    })
                    .build()
    );

    /**
     * 添加单个物品到创造物品栏
     * @param item 要添加的物品
     */
    public static void addItem(Item item) {
        ADDITIONAL_ITEMS.add(() -> new ItemStack(item));
    }

    /**
     * 添加单个方块到创造物品栏
     * @param block 要添加的方块
     */
    public static void addBlock(Block block) {
        if (block.asItem() != null) {
            ADDITIONAL_ITEMS.add(() -> new ItemStack(block));
        }
    }
}