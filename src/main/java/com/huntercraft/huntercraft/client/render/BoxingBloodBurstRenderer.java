package com.huntercraft.huntercraft.client.render;

import com.huntercraft.huntercraft.entity.BoxingBloodBurstEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;

public class BoxingBloodBurstRenderer extends EntityRenderer<BoxingBloodBurstEntity> {
    public BoxingBloodBurstRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
    }

    @Override
    public void render(BoxingBloodBurstEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        float fade = Mth.clamp(entity.getFade(partialTick), 0.0F, 1.0F);
        if (fade <= 0.01F) {
            return;
        }
        float progress = 1.0F - fade;
        float scale = entity.getScale();
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - Mth.rotLerp(partialTick, entity.yRotO, entity.getYRot())));
        poseStack.mulPose(Axis.XP.rotationDegrees(Mth.lerp(partialTick, entity.xRotO, entity.getXRot())));
        Matrix4f matrix = poseStack.last().pose();
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lightning());
        for (int i = 0; i < 18; i++) {
            float angle = Mth.TWO_PI * i / 18.0F;
            float spread = scale * (0.28F + (i % 4) * 0.08F + progress * 1.35F);
            float length = scale * (0.7F + (i % 5) * 0.16F + progress * 1.8F);
            float x = Mth.cos(angle) * spread;
            float y = Mth.sin(angle) * spread * 0.62F;
            renderSpike(matrix, consumer, x, y, length, fade * (0.52F + (i % 3) * 0.12F));
        }
        renderCore(matrix, consumer, scale * (0.45F + progress * 0.28F), fade);
        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    private static void renderSpike(Matrix4f matrix, VertexConsumer consumer, float x, float y, float length, float alpha) {
        float width = 0.055F + length * 0.035F;
        vertex(consumer, matrix, -width, -width, 0.0F, 0.42F, 0.0F, 0.0F, alpha * 0.7F);
        vertex(consumer, matrix, width, width, 0.0F, 0.92F, 0.02F, 0.01F, alpha);
        vertex(consumer, matrix, x, y, length, 0.7F, 0.0F, 0.0F, alpha * 0.0F);
    }

    private static void renderCore(Matrix4f matrix, VertexConsumer consumer, float radius, float alpha) {
        vertex(consumer, matrix, -radius, -radius, 0.02F, 0.28F, 0.0F, 0.0F, alpha * 0.58F);
        vertex(consumer, matrix, radius, -radius, 0.02F, 1.0F, 0.04F, 0.02F, alpha * 0.72F);
        vertex(consumer, matrix, radius, radius, 0.02F, 0.9F, 0.0F, 0.0F, alpha * 0.45F);
        vertex(consumer, matrix, -radius, radius, 0.02F, 0.42F, 0.0F, 0.0F, alpha * 0.38F);
    }

    private static void vertex(VertexConsumer consumer, Matrix4f matrix, float x, float y, float z, float red, float green, float blue, float alpha) {
        consumer.vertex(matrix, x, y, z).color(red, green, blue, alpha).endVertex();
    }

    @Override
    public ResourceLocation getTextureLocation(BoxingBloodBurstEntity entity) {
        return null;
    }
}
