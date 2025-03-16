package net.prizowo.examplemod.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.prizowo.examplemod.ExampleMod;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;

/**
 * 附魔标签提供者，用于生成附魔标签JSON文件
 */
public class ModEnchantmentTagsProvider extends TagsProvider<Enchantment> {

    public ModEnchantmentTagsProvider(
            PackOutput output, 
            CompletableFuture<HolderLookup.Provider> lookupProvider, 
            @Nullable ExistingFileHelper existingFileHelper) {
        super(output, Registries.ENCHANTMENT, lookupProvider, ExampleMod.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.@NotNull Provider provider) {
        ResourceLocation ourTagLocation = ResourceLocation.parse(ExampleMod.MODID + ":enchantments_for_enchanting_table");
        TagKey<Enchantment> ourEnchantingTableTag = TagKey.create(Registries.ENCHANTMENT, ourTagLocation);
        
        ResourceLocation witherBladeId = ResourceLocation.parse(ExampleMod.MODID + ":wither_blade");
        this.tag(ourEnchantingTableTag)
            .addOptional(witherBladeId);
        
        ResourceLocation vanillaTagLocation = ResourceLocation.parse("minecraft:in_enchanting_table");
        TagKey<Enchantment> inEnchantingTable = TagKey.create(Registries.ENCHANTMENT, vanillaTagLocation);
        
        this.tag(inEnchantingTable)
            .addTag(ourEnchantingTableTag);
    }
} 