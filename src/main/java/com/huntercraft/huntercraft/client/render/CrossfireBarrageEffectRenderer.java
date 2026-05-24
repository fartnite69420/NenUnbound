package com.huntercraft.huntercraft.client.render;

import com.huntercraft.huntercraft.entity.CrossfireBarrageEffectEntity;
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

public class CrossfireBarrageEffectRenderer extends EntityRenderer<CrossfireBarrageEffectEntity> {
    private static final int FIST_COUNT = 18;

    public CrossfireBarrageEffectRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
    }

    @Override
    public void render(CrossfireBarrageEffectEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        float fade = Mth.clamp(entity.getFade(partialTick), 0.0F, 1.0F);
        if (fade <= 0.01F) {
            return;
        }

        float age = entity.tickCount + partialTick;
        float startFade = Mth.clamp((entity.getMaxLifeTicks() - entity.getLifeTicks() + partialTick) / 5.0F, 0.0F, 1.0F);
        float alpha = fade * startFade;

        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(-Mth.lerp(partialTick, entity.yRotO, entity.getYRot())));
        poseStack.scale(entity.getScale(), entity.getScale(), entity.getScale());

        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lightning());
        Matrix4f matrix = poseStack.last().pose();
        for (int i = 0; i < FIST_COUNT; i++) {
            renderFist(matrix, consumer, i, age, alpha);
        }
        renderSpeedLines(matrix, consumer, age, alpha);

        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    private static void renderFist(Matrix4f matrix, VertexConsumer consumer, int index, float age, float alpha) {
        float lane = ((index % 6) - 2.5F) / 2.5F;
        float row = ((index / 6) - 1.0F);
        float phase = (age * 0.72F + index * 0.37F) % 1.0F;
        float punch = Mth.sin(phase * Mth.PI);
        float z = 0.55F + phase * 2.75F;
        float x = lane * (0.42F + phase * 0.64F) + Mth.sin(age * 0.55F + index) * 0.07F;
        float y = 0.35F + row * 0.42F + punch * 0.22F;
        float size = 0.20F + punch * 0.18F + phase * 0.06F;
        float localAlpha = alpha * (0.28F + punch * 0.72F) * (1.0F - phase * 0.25F);

        renderTrail(matrix, consumer, x, y, z, size, phase, localAlpha * 0.62F);
        renderKnuckleQuad(matrix, consumer, x, y, z, size, localAlpha);
        for (int k = 0; k < 4; k++) {
            float knuckleX = x + (k - 1.5F) * size * 0.34F;
            renderMiniQuad(matrix, consumer, knuckleX, y + size * 0.34F, z + 0.025F, size * 0.23F, 0.92F, 0.96F, 1.0F, localAlpha);
        }
    }

    private static void renderTrail(Matrix4f matrix, VertexConsumer consumer, float x, float y, float z, float size, float phase, float alpha) {
        float length = 0.72F + phase * 0.78F;
        vertex(matrix, consumer, x - size * 0.55F, y - size * 0.45F, z - length, 0.55F, 0.07F, 0.11F, alpha * 0.18F);
        vertex(matrix, consumer, x + size * 0.55F, y - size * 0.45F, z - length, 0.55F, 0.07F, 0.11F, alpha * 0.18F);
        vertex(matrix, consumer, x + size * 0.7F, y + size * 0.5F, z, 1.0F, 0.13F, 0.18F, alpha);
        vertex(matrix, consumer, x - size * 0.7F, y + size * 0.5F, z, 1.0F, 0.13F, 0.18F, alpha);
    }

    private static void renderKnuckleQuad(Matrix4f matrix, VertexConsumer consumer, float x, float y, float z, float size, float alpha) {
        vertex(matrix, consumer, x - size, y - size * 0.62F, z, 0.10F, 0.12F, 0.16F, alpha * 0.86F);
        vertex(matrix, consumer, x + size, y - size * 0.62F, z, 0.10F, 0.12F, 0.16F, alpha * 0.86F);
        vertex(matrix, consumer, x + size * 0.86F, y + size * 0.78F, z, 0.86F, 0.92F, 0.74F, alpha);
        vertex(matrix, consumer, x - size * 0.86F, y + size * 0.78F, z, 0.86F, 0.92F, 0.74F, alpha);
    }

    private static void renderMiniQuad(Matrix4f matrix, VertexConsumer consumer, float x, float y, float z, float size, float red, float green, float blue, float alpha) {
        vertex(matrix, consumer, x - size, y - size, z, red * 0.45F, green * 0.45F, blue * 0.45F, alpha * 0.82F);
        vertex(matrix, consumer, x + size, y - size, z, red * 0.45F, green * 0.45F, blue * 0.45F, alpha * 0.82F);
        vertex(matrix, consumer, x + size, y + size, z, red, green, blue, alpha);
        vertex(matrix, consumer, x - size, y + size, z, red, green, blue, alpha);
    }

    private static void renderSpeedLines(Matrix4f matrix, VertexConsumer consumer, float age, float alpha) {
        for (int i = 0; i < 14; i++) {
            float side = ((i % 7) - 3.0F) / 3.0F;
            float y = 0.18F + (i / 7) * 0.85F + Mth.sin(age * 0.35F + i) * 0.12F;
            float phase = (age * 0.36F + i * 0.19F) % 1.0F;
            float z0 = 0.25F + phase * 2.5F;
            float z1 = z0 + 1.25F;
            float x = side * (0.7F + phase * 0.9F);
            float lineAlpha = alpha * (0.22F + (i % 3) * 0.08F);
            vertex(matrix, consumer, x - 0.035F, y, z0, 0.78F, 0.02F, 0.08F, lineAlpha * 0.25F);
            vertex(matrix, consumer, x + 0.035F, y, z0, 0.78F, 0.02F, 0.08F, lineAlpha * 0.25F);
            vertex(matrix, consumer, x + 0.07F, y + 0.04F, z1, 1.0F, 0.08F, 0.12F, lineAlpha);
            vertex(matrix, consumer, x - 0.07F, y + 0.04F, z1, 1.0F, 0.08F, 0.12F, lineAlpha);
        }
    }

    private static void vertex(Matrix4f matrix, VertexConsumer consumer, float x, float y, float z, float red, float green, float blue, float alpha) {
        consumer.vertex(matrix, x, y, z).color(red, green, blue, alpha).endVertex();
    }

    @Override
    public ResourceLocation getTextureLocation(CrossfireBarrageEffectEntity entity) {
        return null;
    }
}
