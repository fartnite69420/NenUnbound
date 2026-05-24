package com.huntercraft.huntercraft.client.render;

import com.huntercraft.huntercraft.entity.LiverBreakEffectEntity;
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

public class LiverBreakEffectRenderer extends EntityRenderer<LiverBreakEffectEntity> {
    private static final int RING_SEGMENTS = 36;
    private static final int ARC_SEGMENTS = 24;

    public LiverBreakEffectRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
    }

    @Override
    public void render(LiverBreakEffectEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        float fade = Mth.clamp(entity.getFade(partialTick), 0.0F, 1.0F);
        if (fade <= 0.01F) {
            return;
        }

        float progress = 1.0F - fade;
        float pulse = Mth.sin(Mth.clamp(progress * Mth.PI, 0.0F, Mth.PI));
        float scale = entity.getScale() * (0.82F + progress * 0.42F + pulse * 0.16F);

        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(-Mth.lerp(partialTick, entity.yRotO, entity.getYRot())));
        poseStack.scale(scale, scale, scale);

        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lightning());
        Matrix4f matrix = poseStack.last().pose();
        if (entity.getStyle() == LiverBreakEffectEntity.STYLE_GUARD) {
            renderGuardFlare(matrix, consumer, fade, progress);
        } else {
            renderImpactBurst(matrix, consumer, fade, progress);
        }

        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    private static void renderGuardFlare(Matrix4f matrix, VertexConsumer consumer, float fade, float progress) {
        renderGroundRing(matrix, consumer, 0.85F + progress * 0.65F, 0.07F, 0.08F, 0.72F, 1.0F, fade * 0.72F);
        renderGroundRing(matrix, consumer, 1.18F + progress * 0.42F, 0.035F, 0.62F, 0.96F, 1.0F, fade * 0.42F);
        for (int i = 0; i < 6; i++) {
            float angle = i * Mth.TWO_PI / 6.0F + progress * 0.9F;
            float x = Mth.sin(angle) * 0.78F;
            float z = Mth.cos(angle) * 0.78F;
            renderSlashQuad(matrix, consumer, x * 0.55F, 0.18F, z * 0.55F, x * 1.08F, 1.55F, z * 1.08F, 0.08F, 0.12F, 0.72F, 1.0F, fade * 0.52F);
        }
    }

    private static void renderImpactBurst(Matrix4f matrix, VertexConsumer consumer, float fade, float progress) {
        renderGroundRing(matrix, consumer, 0.95F + progress * 1.05F, 0.06F, 0.08F, 0.72F, 1.0F, fade * 0.62F);
        renderGroundRing(matrix, consumer, 1.62F + progress * 1.25F, 0.035F, 0.72F, 0.98F, 1.0F, fade * 0.34F);
        renderForwardArc(matrix, consumer, 0.18F, 2.55F, 1.18F, 0.82F, 0.08F, 0.68F, 1.0F, fade * 0.74F);
        renderForwardArc(matrix, consumer, 0.42F, 2.2F, 0.72F, 1.06F, 0.78F, 0.98F, 1.0F, fade * 0.82F);

        for (int i = 0; i < 12; i++) {
            float side = (i - 5.5F) / 5.5F;
            float height = 0.42F + (i % 4) * 0.18F;
            float startX = side * 0.16F;
            float endX = side * (0.92F + Math.abs(side) * 0.42F);
            float endZ = 1.45F + (i % 3) * 0.38F + progress * 0.52F;
            float alpha = fade * (0.34F + (i % 2) * 0.2F);
            renderSlashQuad(matrix, consumer, startX, height, 0.2F, endX, height + 0.38F, endZ, 0.055F, 0.72F, 0.98F, 1.0F, alpha);
        }

        for (int i = 0; i < 7; i++) {
            float y = 0.55F + i * 0.16F;
            float width = 0.12F + i * 0.06F;
            float z = 0.5F + i * 0.22F;
            renderSlashQuad(matrix, consumer, -width, y, z, width, y + 0.16F, z + 0.64F, 0.075F, 0.94F, 1.0F, 1.0F, fade * 0.62F);
        }
    }

    private static void renderGroundRing(Matrix4f matrix, VertexConsumer consumer, float radius, float width, float red, float green, float blue, float alpha) {
        for (int i = 0; i < RING_SEGMENTS; i++) {
            float a0 = i * Mth.TWO_PI / RING_SEGMENTS;
            float a1 = (i + 1) * Mth.TWO_PI / RING_SEGMENTS;
            float r0 = radius - width;
            float r1 = radius + width;
            vertex(matrix, consumer, Mth.sin(a0) * r0, 0.035F, Mth.cos(a0) * r0, red, green, blue, alpha * 0.3F);
            vertex(matrix, consumer, Mth.sin(a1) * r0, 0.035F, Mth.cos(a1) * r0, red, green, blue, alpha * 0.3F);
            vertex(matrix, consumer, Mth.sin(a1) * r1, 0.035F, Mth.cos(a1) * r1, red, green, blue, alpha);
            vertex(matrix, consumer, Mth.sin(a0) * r1, 0.035F, Mth.cos(a0) * r1, red, green, blue, alpha);
        }
    }

    private static void renderForwardArc(Matrix4f matrix, VertexConsumer consumer, float startZ, float length, float width, float y, float red, float green, float blue, float alpha) {
        for (int i = 0; i < ARC_SEGMENTS; i++) {
            float t0 = i / (float) ARC_SEGMENTS;
            float t1 = (i + 1) / (float) ARC_SEGMENTS;
            ArcPoint inner0 = arcPoint(t0, startZ, length, width * 0.42F, y);
            ArcPoint inner1 = arcPoint(t1, startZ, length, width * 0.42F, y);
            ArcPoint outer1 = arcPoint(t1, startZ, length, width, y);
            ArcPoint outer0 = arcPoint(t0, startZ, length, width, y);
            float a0 = alpha * Mth.sin(t0 * Mth.PI);
            float a1 = alpha * Mth.sin(t1 * Mth.PI);
            vertex(matrix, consumer, inner0.x(), inner0.y(), inner0.z(), red, green, blue, a0 * 0.22F);
            vertex(matrix, consumer, inner1.x(), inner1.y(), inner1.z(), red, green, blue, a1 * 0.22F);
            vertex(matrix, consumer, outer1.x(), outer1.y(), outer1.z(), red, green, blue, a1);
            vertex(matrix, consumer, outer0.x(), outer0.y(), outer0.z(), red, green, blue, a0);
        }
    }

    private static ArcPoint arcPoint(float t, float startZ, float length, float width, float y) {
        float side = (t - 0.5F) * 2.0F;
        float curve = Mth.sin(t * Mth.PI);
        return new ArcPoint(side * width * curve, y + curve * 0.24F, startZ + length * (0.12F + curve * 0.88F));
    }

    private static void renderSlashQuad(Matrix4f matrix, VertexConsumer consumer, float x0, float y0, float z0, float x1, float y1, float z1, float halfWidth, float red, float green, float blue, float alpha) {
        float dx = x1 - x0;
        float dz = z1 - z0;
        float length = Mth.sqrt(dx * dx + dz * dz);
        float nx = length > 1.0E-4F ? -dz / length * halfWidth : halfWidth;
        float nz = length > 1.0E-4F ? dx / length * halfWidth : 0.0F;
        vertex(matrix, consumer, x0 - nx, y0, z0 - nz, red, green, blue, alpha * 0.18F);
        vertex(matrix, consumer, x0 + nx, y0, z0 + nz, red, green, blue, alpha * 0.18F);
        vertex(matrix, consumer, x1 + nx, y1, z1 + nz, red, green, blue, alpha);
        vertex(matrix, consumer, x1 - nx, y1, z1 - nz, red, green, blue, alpha);
    }

    private static void vertex(Matrix4f matrix, VertexConsumer consumer, float x, float y, float z, float red, float green, float blue, float alpha) {
        consumer.vertex(matrix, x, y, z).color(red, green, blue, alpha).endVertex();
    }

    @Override
    public ResourceLocation getTextureLocation(LiverBreakEffectEntity entity) {
        return null;
    }

    private record ArcPoint(float x, float y, float z) {
    }
}
