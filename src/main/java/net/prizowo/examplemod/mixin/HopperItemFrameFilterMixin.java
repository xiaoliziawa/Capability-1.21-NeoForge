package net.prizowo.examplemod.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.entity.Hopper;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;
import javax.annotation.Nullable;

@Mixin(HopperBlockEntity.class)
public class HopperItemFrameFilterMixin {

    @Unique
    private static final Map<BlockPos, Set<ItemFrame>> FRAMES_CACHE = new WeakHashMap<>();
    @Unique
    private static final Map<BlockPos, Set<ItemStack>> FILTERS_CACHE = new WeakHashMap<>();

    @Shadow
    private static boolean isFullContainer(Container container, Direction direction) {
        throw new AssertionError();
    }

    @Shadow
    public static ItemStack addItem(@Nullable Container source, Container destination, ItemStack stack, @Nullable Direction direction) {
        throw new AssertionError();
    }

    @Shadow
    public static Container getContainerAt(Level level, BlockPos pos) {
        throw new AssertionError();
    }

    @Shadow
    private static Container getAttachedContainer(Level level, BlockPos pos, HopperBlockEntity blockEntity) {
        throw new AssertionError();
    }

    /**
     * 拦截从一个槽位到另一个槽位的物品传输
     */
    @Inject(method = "tryMoveInItem", at = @At("HEAD"), cancellable = true)
    private static void onTryMoveInItem(Container source, Container destination, ItemStack stack, int slot, Direction direction, CallbackInfoReturnable<ItemStack> cir) {
        if (destination instanceof HopperBlockEntity hopperEntity) {
            Level level = hopperEntity.getLevel();
            if (level == null) return;

            Set<ItemStack> filterItems = getFilterItems(level, hopperEntity);

            if (!filterItems.isEmpty()) {
                if (!itemMatchesAny(stack, filterItems)) {
                    cir.setReturnValue(stack);
                }
            }
        }

        if (source instanceof HopperBlockEntity hopperEntity) {
            Level level = hopperEntity.getLevel();
            if (level == null) return;

            Set<ItemStack> filterItems = getFilterItems(level, hopperEntity);

            if (!filterItems.isEmpty()) {
                if (!itemMatchesAny(stack, filterItems)) {
                    cir.setReturnValue(stack);
                }
            }
        }
    }

    /**
     * 在tryTakeInItemFromSlot方法中拦截从容器到漏斗的物品提取
     */
    @Inject(method = "tryTakeInItemFromSlot", at = @At("HEAD"), cancellable = true)
    private static void onTryTakeInItemFromSlot(Hopper hopper, Container container, int slot, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        if (!(hopper instanceof HopperBlockEntity hopperEntity)) return;

        Level level = hopperEntity.getLevel();
        if (level == null) return;

        Set<ItemStack> filterItems = getFilterItems(level, hopperEntity);

        if (!filterItems.isEmpty()) {
            ItemStack sourceStack = container.getItem(slot);

            if (!sourceStack.isEmpty() && !itemMatchesAny(sourceStack, filterItems)) {
                cir.setReturnValue(false);
            }
        }
    }

    /**
     * 获取附着在漏斗上的所有展示框中的物
     */
    private static Set<ItemStack> getFilterItems(Level level, HopperBlockEntity hopper) {
        BlockPos hopperPos = hopper.getBlockPos();

        List<ItemFrame> currentFrames = findAttachedItemFrames(level, hopper);
        Set<ItemFrame> cachedFrames = FRAMES_CACHE.get(hopperPos);

        boolean needsUpdate = false;

        if (cachedFrames != null) {
            if (cachedFrames.size() != currentFrames.size()) {
                needsUpdate = true;
            } else {
                for (ItemFrame frame : cachedFrames) {
                    if (frame.isRemoved() || !currentFrames.contains(frame)) {
                        needsUpdate = true;
                        break;
                    }
                    ItemStack displayedItem = frame.getItem();
                    boolean itemFound = false;
                    for (ItemStack cachedItem : FILTERS_CACHE.getOrDefault(hopperPos, new HashSet<>())) {
                        if (ItemStack.matches(displayedItem, cachedItem)) {
                            itemFound = true;
                            break;
                        }
                    }
                    if (!itemFound) {
                        needsUpdate = true;
                        break;
                    }
                }
            }
        } else {
            needsUpdate = true;
        }

        if (needsUpdate) {
            Set<ItemStack> newFilters = new HashSet<>();
            Set<ItemFrame> newFrames = new HashSet<>();

            for (ItemFrame frame : currentFrames) {
                ItemStack displayedItem = frame.getItem();
                if (!displayedItem.isEmpty()) {
                    newFilters.add(displayedItem.copy());
                    newFrames.add(frame);
                }
            }

            if (!newFrames.isEmpty()) {
                FRAMES_CACHE.put(hopperPos, newFrames);
                FILTERS_CACHE.put(hopperPos, newFilters);
                return newFilters;
            } else {
                FRAMES_CACHE.remove(hopperPos);
                FILTERS_CACHE.remove(hopperPos);
                return new HashSet<>();
            }
        }

        return FILTERS_CACHE.getOrDefault(hopperPos, new HashSet<>());
    }

    /**
     * 查找附着在漏斗上的所有展示框
     */
    private static List<ItemFrame> findAttachedItemFrames(Level level, HopperBlockEntity hopper) {
        BlockPos hopperPos = hopper.getBlockPos();
        AABB searchBox = new AABB(hopperPos).inflate(1.0);
        List<ItemFrame> frames = level.getEntitiesOfClass(ItemFrame.class, searchBox);
        List<ItemFrame> attachedFrames = new ArrayList<>();

        if (!frames.isEmpty()) {
            for (ItemFrame frame : frames) {
                Direction frameDirection = frame.getDirection();
                BlockPos framePos = BlockPos.containing(frame.getX(), frame.getY(), frame.getZ());
                BlockPos attachPos = framePos.relative(frameDirection.getOpposite());

                if (attachPos.equals(hopperPos)) {
                    attachedFrames.add(frame);
                }
            }
        }
        return attachedFrames;
    }

    /**
     * 检查物品是否匹配任何过滤物品
     */
    private static boolean itemMatchesAny(ItemStack stack, Set<ItemStack> filters) {
        for (ItemStack filter : filters) {
            if (stack.getItem() == filter.getItem()) {
                return true;
            }
        }
        return false;
    }
} 
