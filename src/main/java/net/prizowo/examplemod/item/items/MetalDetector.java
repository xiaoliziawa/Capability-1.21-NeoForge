package net.prizowo.examplemod.item.items;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.Tags;
import net.prizowo.examplemod.ExampleMod;
import org.jetbrains.annotations.NotNull;


/**
 * 金属探测器
 * 用于探测地下的矿物
 * 
 * @author Qi-Month
 */
public class MetalDetector extends Item {
    private static final int MAX_DEPTH = 64; // 最大探测深度
    private static final int MIN_DEPTH = 0;  // 最小探测深度
    
    public MetalDetector(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        if (context.getLevel().isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        // 获取必要的上下文信息
        ItemStack itemInHand = context.getItemInHand();
        Player player = context.getPlayer();
        Level level = context.getLevel();
        BlockPos clickedPos = context.getClickedPos();

        if (player != null) {
            searchForValuableBlocks(level, clickedPos, player);
        }

        handleToolDurability(itemInHand, context);

        return InteractionResult.SUCCESS;
    }

    /**
     * 搜索有价值的方块
     *
     * @param level 世界
     * @param startPos 起始位置
     * @param player 玩家
     */
    private void searchForValuableBlocks(Level level, BlockPos startPos, Player player) {
        int startY = startPos.getY();
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
        
        // 从点击位置向下搜索
        for (int depth = MIN_DEPTH; depth <= Math.min(startY + MAX_DEPTH, level.getMaxBuildHeight()); depth++) {
            mutablePos.set(startPos.getX(), startPos.getY() - depth, startPos.getZ());
            BlockState state = level.getBlockState(mutablePos);

            if (isValuableBlock(state)) {
                outputValuableCoordinates(mutablePos.immutable(), player, state.getBlock());
                return;
            }
        }

        // 未找到矿物时发送消息
        sendNoValuableBlocksMessage(player);
    }

    /**
     * 处理工具的耐久度
     *
     * @param itemStack 物品堆
     * @param context 使用上下文
     */
    private void handleToolDurability(ItemStack itemStack, UseOnContext context) {
        if (itemStack.getMaxDamage() > itemStack.getDamageValue()) {
            EquipmentSlot slot = context.getHand() == InteractionHand.MAIN_HAND ? 
                EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
            itemStack.hurtAndBreak(1, context.getPlayer(), slot);
        }
    }

    /**
     * 发送未找到矿物的消息
     *
     * @param player 玩家
     */
    private void sendNoValuableBlocksMessage(Player player) {
        player.sendSystemMessage(Component.translatable("info." + ExampleMod.MODID + ".metal_detector"));
    }

    /**
     * 输出找到的有价值方块的坐标
     *
     * @param blockPos 方块位置
     * @param player 玩家
     * @param block 方块
     */
    private void outputValuableCoordinates(BlockPos blockPos, Player player, Block block) {
        player.sendSystemMessage(Component.translatable("info." + ExampleMod.MODID + ".metal_detector_2",
                blockPos.getX(), blockPos.getY(), blockPos.getZ(), 
                I18n.get(block.getDescriptionId())
        ));
    }

    /**
     * 判断是否为有价值的方块
     * 目前使用Tags.Blocks.ORES标签来判断
     *
     * @param state 方块的状态
     * @return 是否为有价值的方块
     */
    private boolean isValuableBlock(BlockState state) {
        return state.is(Tags.Blocks.ORES);
    }
}