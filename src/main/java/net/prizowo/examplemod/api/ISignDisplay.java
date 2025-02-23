package net.prizowo.examplemod.api;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

public interface ISignDisplay {
    void updateDisplay(ServerLevel level, BlockPos pos);
}