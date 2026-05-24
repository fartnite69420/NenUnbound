package com.huntercraft.huntercraft.client.render;

import com.huntercraft.huntercraft.entity.RisingShotEffectEntity;
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

public class RisingShotEffectRenderer extends EntityRenderer<RisingShotEffectEntity> {
    private static final int RING_SEGMENTS = 32;

    public RisingShotEffectRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
    }

    @Override
    public void render(RisingShotEffectEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        float fade = Mth.clamp(entity.getFade(partialTick), 0.0F, 1.0F);
        if (fade <= 0.01F) {
            return;
        }
        float progress = 1.0F - fade;
        float surge = Mth.sin(Mth.clamp(progress * Mth.PI, 0.0F, Mth.PI));
        float scale = entity.getScale() * (0.82F + progress * 0.5F + surge * 0.18F);

        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(-Mth.lerp(partialTick, entity.yRotO, entity.getYRot())));
        poseStack.scale(scale, scale, scale);

        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lightning());
        Matrix4f matrix = poseStack.last().pose();
        renderColumn(matrix, consumer, fade, progress);
        renderRings(matrix, consumer, fade, progress);

        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    private static void renderColumn(Matrix4f matrix, VertexConsumer consumer, float fade, float progress) {
        for (int i = 0; i < 10; i++) {
            float side = (i - 4.5F) / 4.5F;
            float twist = Mth.sin(progress * Mth.TWO_PI + i * 0.8F) * 0.22F;
            float x0 = side * 0.16F;
            float z0 = twist * 0.24F;
            float x1 = side * (0.42F + Math.abs(side) * 0.16F) + twist;
            float z1 = twist * 0.55F;
            float y1 = 3.0F + (i % 3) * 0.36F + progress * 1.25F;
            float alpha = fade * (0.44F + (i % 2) * 0.22F);
            renderBeam(matrix, consumer, x0, 0.15F, z0, x1, y1, z1, 0.04F, 0.18F, 0.66F, 1.0F, alpha);
        }
        renderBeam(matrix, consumer, -0.08F, 0.12F, 0.0F, 0.08F, 3.7F + progress * 0.8F, 0.0F, 0.11F, 0.9F, 0.98F, 1.0F, fade * 0.72F);
    }

    private static void renderRings(Matrix4f matrix, VertexConsumer consumer, float fade, float progress) {
        renderRing(matrix, consumer, 0.18F + progress * 0.08F, 0.75F + progress * 0.2F, 0.045F, 0.75F, 0.95F, 1.0F, fade * 0.48F);
        renderRing(matrix, consumer, 1.4F + progress * 0.65F, 0.62F + progress * 0.45F, 0.04F, 0.18F, 0.66F, 1.0F, fade * 0.62F);
        renderRing(matrix, consumer, 2.5F + progress * 1.05F, 0.45F + progress * 0.55F, 0.035F, 0.88F, 0.98F, 1.0F, fade * 0.42F);
    }

    private static void renderRing(Matrix4f matrix, VertexConsumer consumer, float y, float radius, float width, float red, float green, float blue, float alpha) {
        for (int i = 0; i < RING_SEGMENTS; i++) {
            float a0 = i * Mth.TWO_PI / RING_SEGMENTS;
            float a1 = (i + 1) * Mth.TWO_PI / RING_SEGMENTS;
            float r0 = radius - width;
            float r1 = radius + width;
            vertex(matrix, consumer, Mth.sin(a0) * r0, y, Mth.cos(a0) * r0, red, green, blue, alpha * 0.24F);
            vertex(matrix, consumer, Mth.sin(a1) * r0, y, Mth.cos(a1) * r0, red, green, blue, alpha * 0.24F);
            vertex(matrix, consumer, Mth.sin(a1) * r1, y, Mth.cos(a1) * r1, red, green, blue, alpha);
            vertex(matrix, consumer, Mth.sin(a0) * r1, y, Mth.cos(a0) * r1, red, green, blue, alpha);
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
    public ResourceLocation getTextureLocation(RisingShotEffectEntity entity) {
        return null;
    }
}
