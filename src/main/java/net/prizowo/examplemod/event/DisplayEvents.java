package net.prizowo.examplemod.event;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.ChatFormatting;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import com.mojang.math.Transformation;
import net.prizowo.examplemod.api.DisplayEntityAccessor;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class DisplayEvents {
    private static final List<DisplayInfo> activeDisplays = new ArrayList<>();
    private static final Random RANDOM = new Random();
    private static final ChatFormatting[] RAINBOW_COLORS = {
            ChatFormatting.RED,
            ChatFormatting.GOLD,
            ChatFormatting.YELLOW,
            ChatFormatting.GREEN,
            ChatFormatting.BLUE,
            ChatFormatting.DARK_PURPLE,
            ChatFormatting.LIGHT_PURPLE
    };

    private static class DisplayInfo {
        final Display.BlockDisplay blockDisplay;
        final Display.TextDisplay textDisplay;
        int ticksLeft;
        float rotationSpeed;
        float scalePhase;
        int colorIndex;
        boolean isAmethyst;

        DisplayInfo(Display.BlockDisplay blockDisplay, Display.TextDisplay textDisplay, int ticks) {
            this.blockDisplay = blockDisplay;
            this.textDisplay = textDisplay;
            this.ticksLeft = ticks;
            this.rotationSpeed = 0.05f;
            this.scalePhase = 0;
            this.colorIndex = 0;
            this.isAmethyst = false;
        }
    }

    @SubscribeEvent
    public void onServerTick(ServerTickEvent.Post event) {
        Iterator<DisplayInfo> iterator = activeDisplays.iterator();
        while (iterator.hasNext()) {
            DisplayInfo info = iterator.next();
            info.ticksLeft--;

            if (info.blockDisplay != null && info.blockDisplay.isAlive()) {
                Transformation currentTransform = new Transformation(
                        new Vector3f(0, (float)Math.sin(info.scalePhase) * 0.1f, 0),
                        new Quaternionf().rotateY(info.ticksLeft * info.rotationSpeed),
                        new Vector3f(1 + (float)Math.sin(info.scalePhase) * 0.2f),
                        new Quaternionf()
                );
                ((DisplayEntityAccessor)info.blockDisplay).accessor$setTransformation(currentTransform);

                if (info.ticksLeft % 20 == 0) {
                    info.isAmethyst = !info.isAmethyst;
                    ((DisplayEntityAccessor)info.blockDisplay).accessor$setBlockState(
                            info.isAmethyst ?
                                    Blocks.AMETHYST_BLOCK.defaultBlockState() :
                                    Blocks.DIAMOND_BLOCK.defaultBlockState()
                    );
                }

                if (info.ticksLeft % 2 == 0 && info.blockDisplay.level() instanceof ServerLevel serverLevel) {
                    Vec3 pos = info.blockDisplay.position();
                    for (int i = 0; i < 5; i++) {
                        double angle = RANDOM.nextDouble() * Math.PI * 2;
                        double radius = 0.7;
                        double offsetX = Math.cos(angle) * radius;
                        double offsetZ = Math.sin(angle) * radius;
                        double offsetY = RANDOM.nextFloat() * 0.8;
                        
                        serverLevel.sendParticles(
                            RANDOM.nextBoolean() ? ParticleTypes.END_ROD : ParticleTypes.SOUL_FIRE_FLAME,
                            pos.x + offsetX,
                            pos.y + offsetY,
                            pos.z + offsetZ,
                            2,
                            0, 0.02, 0,
                            0.02
                        );
                    }
                }
            }

            if (info.textDisplay != null && info.textDisplay.isAlive()) {
                if (info.ticksLeft % 5 == 0) {
                    info.colorIndex = (info.colorIndex + 1) % RAINBOW_COLORS.length;
                    int secondsLeft = (info.ticksLeft + 19) / 20;
                    ((DisplayEntityAccessor)info.textDisplay).accessor$setText(
                            Component.literal("✧ 装逼我让你飞起来！ ✧")
                                    .withStyle(Style.EMPTY
                                            .withColor(RAINBOW_COLORS[info.colorIndex])
                                            .withBold(true)
                                            .withItalic(true))
                                    .append(Component.literal(" " + secondsLeft + "s")
                                            .withStyle(Style.EMPTY
                                                    .withColor(ChatFormatting.AQUA)
                                                    .withBold(false)
                                                    .withItalic(false)))
                    );
                }
            }

            info.scalePhase += 0.1f;

            if (info.ticksLeft <= 0) {
                if (info.blockDisplay != null && info.blockDisplay.isAlive()) {
                    info.blockDisplay.remove(Display.RemovalReason.DISCARDED);
                }
                if (info.textDisplay != null && info.textDisplay.isAlive()) {
                    info.textDisplay.remove(Display.RemovalReason.DISCARDED);
                }
                iterator.remove();
            }
        }
    }

    @SubscribeEvent
    public void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (event.getItemStack().is(Items.STICK)) {
            if (!event.getLevel().isClientSide) {
                Vec3 pos = event.getEntity().position();
                
                Display.BlockDisplay blockDisplay = EntityType.BLOCK_DISPLAY.create(event.getLevel());
                if (blockDisplay != null) {
                    blockDisplay.setPos(pos.x - 0.5, pos.y + 2.0, pos.z - 0.5);
                    
                    ((DisplayEntityAccessor)blockDisplay).accessor$setBlockState(
                        Blocks.DIAMOND_BLOCK.defaultBlockState()
                    );
                    
                    Transformation transformation = new Transformation(
                        new Vector3f(0, 0, 0),
                        new Quaternionf().rotateY((float)Math.toRadians(45)),
                        new Vector3f(1, 1, 1),
                        new Quaternionf()
                    );
                    ((DisplayEntityAccessor)blockDisplay).accessor$setTransformation(transformation);
                    
                    event.getLevel().addFreshEntity(blockDisplay);
                    
                    Display.TextDisplay textDisplay = EntityType.TEXT_DISPLAY.create(event.getLevel());
                    if (textDisplay != null) {
                        textDisplay.setPos(pos.x, pos.y + 3.0, pos.z);
                        
                        ((DisplayEntityAccessor)textDisplay).accessor$setText(
                                Component.literal("✧ 装逼我让你飞起来！ ✧")
                                        .withStyle(Style.EMPTY
                                                .withColor(RAINBOW_COLORS[0])
                                                .withBold(true)
                                                .withItalic(true))
                                        .append(Component.literal(" 10s")
                                                .withStyle(Style.EMPTY
                                                        .withColor(ChatFormatting.AQUA)
                                                        .withBold(false)
                                                        .withItalic(false)))
                        );
                        ((DisplayEntityAccessor)textDisplay).accessor$setBackgroundColor(0x88000000);
                        ((DisplayEntityAccessor)textDisplay).accessor$setLineWidth(200);
                        ((DisplayEntityAccessor)textDisplay).accessor$setBillboardConstraints(Display.BillboardConstraints.CENTER);

                        event.getLevel().addFreshEntity(textDisplay);
                    }
                    
                    if (event.getLevel() instanceof ServerLevel serverLevel) {
                        for (int i = 0; i < 72; i++) {
                            double particleAngle = i * (Math.PI * 2) / 36;
                            double height = (i % 2) * 0.2;
                            
                            serverLevel.sendParticles(
                                ParticleTypes.END_ROD,
                                pos.x + Math.cos(particleAngle) * 0.7,
                                pos.y + 2.5 + height,
                                pos.z + Math.sin(particleAngle) * 0.7,
                                2,
                                Math.cos(particleAngle) * 0.05,
                                0.05,
                                Math.sin(particleAngle) * 0.05,
                                0.01
                            );
                            
                            serverLevel.sendParticles(
                                ParticleTypes.SOUL_FIRE_FLAME,
                                pos.x + Math.cos(-particleAngle) * 0.5,
                                pos.y + 2.5 + height,
                                pos.z + Math.sin(-particleAngle) * 0.5,
                                2,
                                Math.cos(-particleAngle) * 0.05,
                                0.05,
                                Math.sin(-particleAngle) * 0.05,
                                0.01
                            );
                        }
                    }
                    
                    activeDisplays.add(new DisplayInfo(blockDisplay, textDisplay, 200));
                }
            }
            
            event.setCancellationResult(InteractionResult.SUCCESS);
            event.setCanceled(true);
        }
    }
} 