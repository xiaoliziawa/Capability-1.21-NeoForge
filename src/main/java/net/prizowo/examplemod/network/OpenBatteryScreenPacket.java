package net.prizowo.examplemod.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.prizowo.examplemod.ExampleMod;
import net.prizowo.examplemod.block.entity.BatteryDevice;
import net.prizowo.examplemod.screen.BatteryMenu;
import org.jetbrains.annotations.NotNull;

public record OpenBatteryScreenPacket(BlockPos pos) implements CustomPacketPayload {
    public static final Type<OpenBatteryScreenPacket> TYPE = new Type<>(
        ResourceLocation.fromNamespaceAndPath(ExampleMod.MODID, "open_battery_screen"));

    public static final StreamCodec<FriendlyByteBuf, OpenBatteryScreenPacket> STREAM_CODEC = StreamCodec.composite(
        StreamCodec.of(
            (buf, pos) -> {
                buf.writeInt(pos.getX());
                buf.writeInt(pos.getY());
                buf.writeInt(pos.getZ());
            },
            buf -> new BlockPos(buf.readInt(), buf.readInt(), buf.readInt())
        ),
        OpenBatteryScreenPacket::pos,
        OpenBatteryScreenPacket::new
    );

    public static void handle(final OpenBatteryScreenPacket data, final IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (ctx.player() instanceof ServerPlayer serverPlayer) {
                BlockEntity be = serverPlayer.level().getBlockEntity(data.pos);
                if (be instanceof BatteryDevice) {
                    MenuProvider containerProvider = new MenuProvider() {
                        @Override
                        public @NotNull Component getDisplayName() {
                            return Component.translatable("screen.examplemod.battery");
                        }

                        @Override
                        public AbstractContainerMenu createMenu(int windowId, @NotNull Inventory playerInventory, @NotNull Player playerEntity) {
                            return new BatteryMenu(windowId, playerInventory, be, ((BatteryDevice) be).getContainerData());
                        }
                    };
                    
                    serverPlayer.openMenu(containerProvider, buf -> buf.writeBlockPos(data.pos));
                }
            }
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}