package net.prizowo.examplemod.recipe;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RecipesUpdatedEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.prizowo.examplemod.ExampleMod;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@EventBusSubscriber(modid = ExampleMod.MODID, bus = EventBusSubscriber.Bus.GAME)
public class RecipeRemover {
    private static final Set<String> MODS_TO_REMOVE = new HashSet<>();
    private static final Set<ResourceLocation> RECIPES_TO_REMOVE = new HashSet<>();

    /**
     * 添加要删除所有配方的模组ID 例如: "mekanism"
     */
    public static void addModToRemove(String modId) {
        MODS_TO_REMOVE.add(modId);
    }

    /**
     * 添加要删除的配方物品ID 例如: "minecraft:furnace"
     */
    public static void addRecipeToRemove(String recipeId) {
        RECIPES_TO_REMOVE.add(ResourceLocation.parse(recipeId));
    }

    /**
     * 添加要删除的配方,指定命名空间和物品ID 例如: "minecraft", "furnace"
     */
    public static void addRecipeToRemove(String namespace, String path) {
        RECIPES_TO_REMOVE.add(ResourceLocation.fromNamespaceAndPath(namespace, path));
    }

    @SubscribeEvent
    public static void onRecipesUpdated(RecipesUpdatedEvent event) {
        removeRecipes(event.getRecipeManager());
    }

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        removeRecipes(event.getServer().getRecipeManager());
    }

    private static void removeRecipes(RecipeManager recipeManager) {
        Collection<RecipeHolder<?>> allRecipes = recipeManager.getRecipes();
        Collection<RecipeHolder<?>> recipesToKeep = allRecipes.stream()
                .filter(holder -> {
                    ResourceLocation id = holder.id();
                    return !MODS_TO_REMOVE.contains(id.getNamespace()) &&
                            !RECIPES_TO_REMOVE.contains(id);
                })
                .collect(Collectors.toList());
        recipeManager.replaceRecipes(recipesToKeep);
    }
} 