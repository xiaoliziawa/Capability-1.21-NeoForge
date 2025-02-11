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
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.prizowo.examplemod.ExampleMod;
import net.prizowo.examplemod.block.entitiy.GeneratorDevice;
import net.prizowo.examplemod.screen.GeneratorMenu;
import org.jetbrains.annotations.NotNull;

public record OpenGeneratorScreenPacket(BlockPos pos) implements CustomPacketPayload {
    public static final Type<OpenGeneratorScreenPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(ExampleMod.MODID, "open_generator_screen"));

    public static final StreamCodec<FriendlyByteBuf, OpenGeneratorScreenPacket> STREAM_CODEC = StreamCodec.composite(
        StreamCodec.of(
            (buf, pos) -> {
                buf.writeInt(pos.getX());
                buf.writeInt(pos.getY());
                buf.writeInt(pos.getZ());
            },
            buf -> new BlockPos(buf.readInt(), buf.readInt(), buf.readInt())
        ),
        OpenGeneratorScreenPacket::pos,
        OpenGeneratorScreenPacket::new
    );

    public static void handle(final OpenGeneratorScreenPacket data, final IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (ctx.player() instanceof ServerPlayer serverPlayer) {
                BlockEntity be = serverPlayer.level().getBlockEntity(data.pos);
                if (be instanceof GeneratorDevice) {
                    MenuProvider containerProvider = new MenuProvider() {
                        @Override
                        public @NotNull Component getDisplayName() {
                            return Component.translatable("screen.examplemod.generator");
                        }

                        @Override
                        public AbstractContainerMenu createMenu(int windowId, @NotNull Inventory playerInventory, @NotNull Player playerEntity) {
                            return new GeneratorMenu(windowId, playerInventory, be, new SimpleContainerData(4));
                        }
                    };
                    
                    serverPlayer.openMenu(containerProvider, buf -> buf.writeBlockPos(data.pos));
                }
            }
        });
    }

    @Override
    @NotNull
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
} 