package net.prizowo.examplemod.util;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Display;
import net.minecraft.core.BlockPos;
import java.util.HashMap;
import java.util.UUID;

public class SignDisplayManager {
    private static final HashMap<BlockPos, UUID> signDisplays = new HashMap<>();
    
    public static HashMap<BlockPos, UUID> getDisplays() {
        return signDisplays;
    }
    
    public static void clearDisplays(ServerLevel level) {
        for (UUID uuid : signDisplays.values()) {
            if (level.getEntity(uuid) instanceof Display.TextDisplay display) {
                display.remove(Display.RemovalReason.DISCARDED);
            }
        }
        signDisplays.clear();
    }
}