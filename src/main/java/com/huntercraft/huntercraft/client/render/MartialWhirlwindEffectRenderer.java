package com.huntercraft.huntercraft.client.render;

import com.huntercraft.huntercraft.entity.MartialWhirlwindEffectEntity;
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

public class MartialWhirlwindEffectRenderer extends EntityRenderer<MartialWhirlwindEffectEntity> {
    private static final int ARC_SEGMENTS = 30;

    public MartialWhirlwindEffectRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
    }

    @Override
    public void render(MartialWhirlwindEffectEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        float fade = Mth.clamp(entity.getFade(partialTick), 0.0F, 1.0F);
        if (fade <= 0.01F) {
            return;
        }
        float progress = 1.0F - fade;
        float spin = (entity.tickCount + partialTick) * 34.0F;
        float scale = entity.getScale() * (0.78F + progress * 0.34F);

        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(-Mth.lerp(partialTick, entity.yRotO, entity.getYRot())));
        poseStack.mulPose(Axis.YP.rotationDegrees(spin));
        poseStack.scale(scale, scale, scale);

        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lightning());
        Matrix4f matrix = poseStack.last().pose();
        renderSwirlArc(matrix, consumer, 0.0F, 0.75F, 1.35F, 0.28F, 0.92F, 1.0F, 1.0F, fade * 0.86F);
        renderSwirlArc(matrix, consumer, 120.0F, 1.05F, 1.15F, 0.62F, 0.72F, 0.94F, 1.0F, fade * 0.62F);
        renderSwirlArc(matrix, consumer, 240.0F, 1.38F, 0.95F, 0.98F, 0.96F, 1.0F, 1.0F, fade * 0.54F);
        renderDashStreaks(matrix, consumer, fade, progress);

        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    private static void renderSwirlArc(Matrix4f matrix, VertexConsumer consumer, float offsetDegrees, float radius, float length, float y, float red, float green, float blue, float alpha) {
        float start = (float) Math.toRadians(offsetDegrees);
        for (int i = 0; i < ARC_SEGMENTS; i++) {
            float t0 = i / (float) ARC_SEGMENTS;
            float t1 = (i + 1) / (float) ARC_SEGMENTS;
            SwirlPoint inner0 = swirlPoint(start, t0, radius * 0.72F, length, y);
            SwirlPoint inner1 = swirlPoint(start, t1, radius * 0.72F, length, y);
            SwirlPoint outer1 = swirlPoint(start, t1, radius, length, y);
            SwirlPoint outer0 = swirlPoint(start, t0, radius, length, y);
            float a0 = alpha * taper(t0);
            float a1 = alpha * taper(t1);
            vertex(matrix, consumer, inner0, red, green, blue, a0 * 0.18F);
            vertex(matrix, consumer, inner1, red, green, blue, a1 * 0.18F);
            vertex(matrix, consumer, outer1, red, green, blue, a1);
            vertex(matrix, consumer, outer0, red, green, blue, a0);
        }
    }

    private static SwirlPoint swirlPoint(float start, float t, float radius, float length, float y) {
        float angle = start + t * Mth.PI * 1.35F;
        float stretch = (t - 0.5F) * length;
        float curve = Mth.sin(t * Mth.PI);
        float x = Mth.sin(angle) * radius * (0.65F + curve * 0.35F);
        float z = Mth.cos(angle) * radius * (0.65F + curve * 0.35F) + stretch * 0.38F;
        return new SwirlPoint(x, y + curve * 0.16F, z);
    }

    private static void renderDashStreaks(Matrix4f matrix, VertexConsumer consumer, float fade, float progress) {
        for (int i = 0; i < 10; i++) {
            float side = (i - 4.5F) / 4.5F;
            float y = 0.22F + (i % 4) * 0.18F;
            float startZ = -0.75F - progress * 0.35F;
            float endZ = 1.45F + (i % 3) * 0.24F;
            float startX = side * 0.18F;
            float endX = side * (0.92F + Math.abs(side) * 0.28F);
            renderBeam(matrix, consumer, startX, y, startZ, endX, y + 0.12F, endZ, 0.035F, 0.78F, 0.96F, 1.0F, fade * 0.34F);
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

    private static float taper(float t) {
        return Mth.clamp(Mth.sin(t * Mth.PI) * 1.18F, 0.0F, 1.0F);
    }

    private static void vertex(Matrix4f matrix, VertexConsumer consumer, SwirlPoint point, float red, float green, float blue, float alpha) {
        vertex(matrix, consumer, point.x(), point.y(), point.z(), red, green, blue, alpha);
    }

    private static void vertex(Matrix4f matrix, VertexConsumer consumer, float x, float y, float z, float red, float green, float blue, float alpha) {
        consumer.vertex(matrix, x, y, z).color(red, green, blue, alpha).endVertex();
    }

    @Override
    public ResourceLocation getTextureLocation(MartialWhirlwindEffectEntity entity) {
        return null;
    }

    private record SwirlPoint(float x, float y, float z) {
    }
}
