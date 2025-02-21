package net.prizowo.examplemod.villagers;

import com.google.common.collect.ImmutableSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.prizowo.examplemod.ExampleMod;

import java.util.function.Supplier;

public class ModPOIs {
    public static final DeferredRegister<PoiType> POI_TYPES = 
        DeferredRegister.create(BuiltInRegistries.POINT_OF_INTEREST_TYPE, ExampleMod.MODID);

    public static final ResourceKey<PoiType> ENCHANTER_POI_KEY = ResourceKey.create(
            Registries.POINT_OF_INTEREST_TYPE,
            ResourceLocation.fromNamespaceAndPath(ExampleMod.MODID, "enchanter"));

    public static final Supplier<PoiType> ENCHANTER_POI = POI_TYPES.register("enchanter",
            () -> new PoiType(
                    ImmutableSet.copyOf(Blocks.ENCHANTING_TABLE.getStateDefinition().getPossibleStates()),
                    1, // 最大票数（同时只能有一个村民使用这个工作站）
                    8  // 有效范围（村民可以在8格范围内检测到这个工作站）
            ));
} 