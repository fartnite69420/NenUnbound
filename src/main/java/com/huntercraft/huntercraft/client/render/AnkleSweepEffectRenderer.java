package com.huntercraft.huntercraft.client.render;

import com.huntercraft.huntercraft.entity.AnkleSweepEffectEntity;
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

public class AnkleSweepEffectRenderer extends EntityRenderer<AnkleSweepEffectEntity> {
    private static final int ARC_SEGMENTS = 28;

    public AnkleSweepEffectRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
    }

    @Override
    public void render(AnkleSweepEffectEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        float fade = Mth.clamp(entity.getFade(partialTick), 0.0F, 1.0F);
        if (fade <= 0.01F) {
            return;
        }
        float progress = 1.0F - fade;
        float sweep = Mth.sin(Mth.clamp(progress * Mth.PI, 0.0F, Mth.PI));
        float scale = entity.getScale() * (0.82F + progress * 0.45F + sweep * 0.16F);

        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(-Mth.lerp(partialTick, entity.yRotO, entity.getYRot())));
        poseStack.scale(scale, scale, scale);

        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lightning());
        Matrix4f matrix = poseStack.last().pose();
        renderLowSweep(matrix, consumer, fade, progress);
        renderDustLines(matrix, consumer, fade, progress);

        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    private static void renderLowSweep(Matrix4f matrix, VertexConsumer consumer, float fade, float progress) {
        renderArc(matrix, consumer, 0.18F, 1.85F + progress * 0.42F, 0.36F, 0.06F, 0.95F, 1.0F, 1.0F, fade * 0.82F);
        renderArc(matrix, consumer, 0.34F, 1.55F + progress * 0.35F, 0.24F, 0.1F, 0.24F, 0.75F, 1.0F, fade * 0.58F);
        renderArc(matrix, consumer, 0.52F, 1.18F + progress * 0.28F, 0.14F, 0.13F, 0.04F, 0.42F, 1.0F, fade * 0.34F);
    }

    private static void renderArc(Matrix4f matrix, VertexConsumer consumer, float startZ, float length, float width, float y, float red, float green, float blue, float alpha) {
        for (int i = 0; i < ARC_SEGMENTS; i++) {
            float t0 = i / (float) ARC_SEGMENTS;
            float t1 = (i + 1) / (float) ARC_SEGMENTS;
            SweepPoint inner0 = arcPoint(t0, startZ, length, width * 0.3F, y);
            SweepPoint inner1 = arcPoint(t1, startZ, length, width * 0.3F, y);
            SweepPoint outer1 = arcPoint(t1, startZ, length, width, y);
            SweepPoint outer0 = arcPoint(t0, startZ, length, width, y);
            float a0 = alpha * Mth.sin(t0 * Mth.PI);
            float a1 = alpha * Mth.sin(t1 * Mth.PI);
            vertex(matrix, consumer, inner0.x(), inner0.y(), inner0.z(), red, green, blue, a0 * 0.16F);
            vertex(matrix, consumer, inner1.x(), inner1.y(), inner1.z(), red, green, blue, a1 * 0.16F);
            vertex(matrix, consumer, outer1.x(), outer1.y(), outer1.z(), red, green, blue, a1);
            vertex(matrix, consumer, outer0.x(), outer0.y(), outer0.z(), red, green, blue, a0);
        }
    }

    private static SweepPoint arcPoint(float t, float startZ, float length, float width, float y) {
        float side = (t - 0.5F) * 2.0F;
        float curve = Mth.sin(t * Mth.PI);
        float z = startZ + length * (0.12F + curve * 0.88F);
        float x = side * width * (0.28F + curve * 0.72F);
        return new SweepPoint(x, y + curve * 0.08F, z);
    }

    private static void renderDustLines(Matrix4f matrix, VertexConsumer consumer, float fade, float progress) {
        for (int i = 0; i < 9; i++) {
            float side = (i - 4.0F) / 4.0F;
            float startZ = 0.22F + progress * 0.32F;
            float endZ = 1.9F + (i % 3) * 0.22F + progress * 0.6F;
            float startX = side * 0.1F;
            float endX = side * (0.5F + Math.abs(side) * 0.35F);
            float alpha = fade * (0.18F + (i % 2) * 0.12F);
            renderBeam(matrix, consumer, startX, 0.035F, startZ, endX, 0.055F, endZ, 0.025F, 0.72F, 0.92F, 1.0F, alpha);
        }
    }

    private static void renderBeam(Matrix4f matrix, VertexConsumer consumer, float x0, float y0, float z0, float x1, float y1, float z1, float halfWidth, float red, float green, float blue, float alpha) {
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
    public ResourceLocation getTextureLocation(AnkleSweepEffectEntity entity) {
        return null;
    }

    private record SweepPoint(float x, float y, float z) {
    }
}
