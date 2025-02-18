package net.prizowo.examplemod.client.render.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

public class GlintEntityLayer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {

    public GlintEntityLayer(RenderLayerParent<T, M> parent) {
        super(parent);
    }

    @Override
    public void render(@NotNull PoseStack poseStack, @NotNull MultiBufferSource buffer, int packedLight,
                       @NotNull T entity, float limbSwing, float limbSwingAmount, float partialTicks,
                       float ageInTicks, float netHeadYaw, float headPitch) {
        VertexConsumer vertexConsumer3 = buffer.getBuffer(RenderType.entityGlint());
        this.getParentModel().renderToBuffer(
                poseStack,
                vertexConsumer3,
                packedLight,
                OverlayTexture.NO_OVERLAY,
                0x00FFFF
        );

    }
} 