package net.prizowo.examplemod.event;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.prizowo.examplemod.api.ISignDisplay;
import net.prizowo.examplemod.util.SignDisplayManager;

public class SignDisplayEvents {
    
    @SubscribeEvent
    public void onChunkLoad(ChunkEvent.Load event) {
        if (event.getLevel() instanceof ServerLevel level) {
            if (event.getChunk() instanceof LevelChunk chunk) {
                // 当区块加载时，检查并更新其中的告示牌
                for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
                    if (blockEntity instanceof SignBlockEntity && blockEntity instanceof ISignDisplay display) {
                        display.updateDisplay(level, blockEntity.getBlockPos());
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ServerLevel level = player.serverLevel();
            // 获取玩家视距范围内的所有区块
            int viewDistance = level.getServer().getPlayerList().getViewDistance();
            int playerChunkX = player.chunkPosition().x;
            int playerChunkZ = player.chunkPosition().z;
            
            // 遍历玩家视距范围内的区块
            for (int x = -viewDistance; x <= viewDistance; x++) {
                for (int z = -viewDistance; z <= viewDistance; z++) {
                    int chunkX = playerChunkX + x;
                    int chunkZ = playerChunkZ + z;
                    
                    // 检查区块是否已加载
                    if (level.hasChunk(chunkX, chunkZ)) {
                        LevelChunk chunk = level.getChunk(chunkX, chunkZ);
                        // 遍历区块中的所有方块实体
                        for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
                            if (blockEntity instanceof SignBlockEntity && blockEntity instanceof ISignDisplay display) {
                                display.updateDisplay(level, blockEntity.getBlockPos());
                            }
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ServerLevel level = player.serverLevel();
            SignDisplayManager.clearDisplays(level);
        }
    }
} 