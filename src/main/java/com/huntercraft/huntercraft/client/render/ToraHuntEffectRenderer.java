package com.huntercraft.huntercraft.client.render;

import com.huntercraft.huntercraft.entity.ToraHuntEffectEntity;
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

public class ToraHuntEffectRenderer extends EntityRenderer<ToraHuntEffectEntity> {
    private static final int ARC_SEGMENTS = 28;
    private static final int RING_SEGMENTS = 36;

    public ToraHuntEffectRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
    }

    @Override
    public void render(ToraHuntEffectEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        float fade = Mth.clamp(entity.getFade(partialTick), 0.0F, 1.0F);
        if (fade <= 0.01F) {
            return;
        }

        float progress = 1.0F - fade;
        float pulse = Mth.sin(Mth.clamp(progress * Mth.PI, 0.0F, Mth.PI));
        float scale = entity.getScale() * (0.85F + progress * 0.5F + pulse * 0.12F);

        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(-Mth.lerp(partialTick, entity.yRotO, entity.getYRot())));
        poseStack.scale(scale, scale, scale);

        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lightning());
        Matrix4f matrix = poseStack.last().pose();
        if (entity.getStyle() == ToraHuntEffectEntity.STYLE_CHARGE) {
            renderCharge(matrix, consumer, fade, progress);
        } else {
            renderStrike(matrix, consumer, fade, progress, entity.getStyle() == ToraHuntEffectEntity.STYLE_FINISHER);
        }

        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    private static void renderCharge(Matrix4f matrix, VertexConsumer consumer, float fade, float progress) {
        renderGroundRing(matrix, consumer, 0.65F + progress * 0.7F, 0.04F, 1.0F, 0.04F, 0.02F, fade * 0.62F);
        renderGroundRing(matrix, consumer, 1.0F + progress * 0.45F, 0.025F, 0.05F, 0.0F, 0.0F, fade * 0.42F);
        for (int i = 0; i < 7; i++) {
            float angle = i * Mth.TWO_PI / 7.0F + progress * 2.6F;
            float x = Mth.sin(angle) * 0.65F;
            float z = Mth.cos(angle) * 0.65F;
            renderSlash(matrix, consumer, x * 0.42F, 0.18F, z * 0.42F, x * 0.95F, 1.35F, z * 0.95F, 0.055F, 0.88F, 0.0F, 0.0F, fade * 0.48F);
        }
    }

    private static void renderStrike(Matrix4f matrix, VertexConsumer consumer, float fade, float progress, boolean finisher) {
        float alphaBoost = finisher ? 1.25F : 1.0F;
        float reach = finisher ? 3.45F : 2.85F;
        float width = finisher ? 1.6F : 1.25F;
        renderClawArc(matrix, consumer, -0.45F, 0.56F, reach, width, 1.0F, 0.02F, 0.02F, fade * 0.78F * alphaBoost);
        renderClawArc(matrix, consumer, 0.0F, 0.84F, reach * 0.92F, width * 0.82F, 0.82F, 0.12F, 0.0F, fade * 0.66F * alphaBoost);
        renderClawArc(matrix, consumer, 0.42F, 1.1F, reach * 0.78F, width * 0.62F, 1.0F, 0.62F, 0.46F, fade * 0.72F * alphaBoost);
        renderGroundRing(matrix, consumer, 0.85F + progress * 1.2F, 0.05F, 0.9F, 0.0F, 0.0F, fade * 0.44F);

        for (int i = 0; i < 9; i++) {
            float side = (i - 4.0F) / 4.0F;
            float z0 = -0.18F - progress * 0.22F;
            float z1 = reach * (0.52F + (i % 3) * 0.14F);
            float y = 0.3F + (i % 5) * 0.16F;
            float red = i % 2 == 0 ? 0.08F : 0.95F;
            renderSlash(matrix, consumer, side * 0.14F, y, z0, side * (0.55F + Math.abs(side) * 0.45F), y + 0.2F, z1, 0.045F, red, 0.0F, 0.0F, fade * 0.36F);
        }
    }

    private static void renderClawArc(Matrix4f matrix, VertexConsumer consumer, float xOffset, float y, float length, float width, float red, float green, float blue, float alpha) {
        for (int i = 0; i < ARC_SEGMENTS; i++) {
            float t0 = i / (float) ARC_SEGMENTS;
            float t1 = (i + 1) / (float) ARC_SEGMENTS;
            ClawPoint inner0 = clawPoint(t0, xOffset, y, length, width * 0.42F);
            ClawPoint inner1 = clawPoint(t1, xOffset, y, length, width * 0.42F);
            ClawPoint outer1 = clawPoint(t1, xOffset, y, length, width);
            ClawPoint outer0 = clawPoint(t0, xOffset, y, length, width);
            float a0 = alpha * taper(t0);
            float a1 = alpha * taper(t1);
            vertex(matrix, consumer, inner0.x(), inner0.y(), inner0.z(), red, green, blue, a0 * 0.16F);
            vertex(matrix, consumer, inner1.x(), inner1.y(), inner1.z(), red, green, blue, a1 * 0.16F);
            vertex(matrix, consumer, outer1.x(), outer1.y(), outer1.z(), red, green, blue, a1);
            vertex(matrix, consumer, outer0.x(), outer0.y(), outer0.z(), red, green, blue, a0);
        }
    }

    private static ClawPoint clawPoint(float t, float xOffset, float y, float length, float width) {
        float sweep = Mth.sin(t * Mth.PI);
        float side = (t - 0.5F) * 2.0F;
        float x = xOffset + side * width * sweep;
        float z = 0.08F + length * (0.08F + sweep * 0.92F);
        return new ClawPoint(x, y + sweep * 0.28F, z);
    }

    private static void renderGroundRing(Matrix4f matrix, VertexConsumer consumer, float radius, float halfWidth, float red, float green, float blue, float alpha) {
        for (int i = 0; i < RING_SEGMENTS; i++) {
            float a0 = i * Mth.TWO_PI / RING_SEGMENTS;
            float a1 = (i + 1) * Mth.TWO_PI / RING_SEGMENTS;
            float r0 = radius - halfWidth;
            float r1 = radius + halfWidth;
            vertex(matrix, consumer, Mth.sin(a0) * r0, 0.035F, Mth.cos(a0) * r0, red, green, blue, alpha * 0.22F);
            vertex(matrix, consumer, Mth.sin(a1) * r0, 0.035F, Mth.cos(a1) * r0, red, green, blue, alpha * 0.22F);
            vertex(matrix, consumer, Mth.sin(a1) * r1, 0.035F, Mth.cos(a1) * r1, red, green, blue, alpha);
            vertex(matrix, consumer, Mth.sin(a0) * r1, 0.035F, Mth.cos(a0) * r1, red, green, blue, alpha);
        }
    }

    private static void renderSlash(Matrix4f matrix, VertexConsumer consumer, float x0, float y0, float z0, float x1, float y1, float z1, float halfWidth, float red, float green, float blue, float alpha) {
        float dx = x1 - x0;
        float dz = z1 - z0;
        float length = Mth.sqrt(dx * dx + dz * dz);
        float nx = length > 1.0E-4F ? -dz / length * halfWidth : halfWidth;
        float nz = length > 1.0E-4F ? dx / length * halfWidth : 0.0F;
        vertex(matrix, consumer, x0 - nx, y0, z0 - nz, red, green, blue, alpha * 0.14F);
        vertex(matrix, consumer, x0 + nx, y0, z0 + nz, red, green, blue, alpha * 0.14F);
        vertex(matrix, consumer, x1 + nx, y1, z1 + nz, red, green, blue, alpha);
        vertex(matrix, consumer, x1 - nx, y1, z1 - nz, red, green, blue, alpha);
    }

    private static float taper(float t) {
        return Mth.clamp(Mth.sin(t * Mth.PI) * 1.18F, 0.0F, 1.0F);
    }

    private static void vertex(Matrix4f matrix, VertexConsumer consumer, float x, float y, float z, float red, float green, float blue, float alpha) {
        consumer.vertex(matrix, x, y, z).color(red, green, blue, alpha).endVertex();
    }

    @Override
    public ResourceLocation getTextureLocation(ToraHuntEffectEntity entity) {
        return null;
    }

    private record ClawPoint(float x, float y, float z) {
    }
}
