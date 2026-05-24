package com.huntercraft.huntercraft.client.render;

import com.huntercraft.huntercraft.entity.HammerShockwaveEntity;
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

public class HammerShockwaveRenderer extends EntityRenderer<HammerShockwaveEntity> {
    private static final int ARC_SEGMENTS = 28;

    public HammerShockwaveRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
    }

    @Override
    public void render(HammerShockwaveEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        float fade = Mth.clamp(entity.getFade(partialTick), 0.0F, 1.0F);
        if (fade <= 0.01F) {
            return;
        }

        float progress = 1.0F - fade;
        float bloom = Mth.sin(Mth.clamp(progress * Mth.PI, 0.0F, Mth.PI));
        float scale = entity.getScale() * (0.72F + progress * 0.58F + bloom * 0.16F);

        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(-Mth.lerp(partialTick, entity.yRotO, entity.getYRot())));
        poseStack.scale(scale, scale, scale);

        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lightning());
        Matrix4f matrix = poseStack.last().pose();
        renderConeArc(matrix, consumer, 0.9F, 4.4F, 1.72F, 0.28F, 0.52F, 0.86F, 1.0F, fade * 0.34F);
        renderConeArc(matrix, consumer, 1.15F, 4.05F, 1.28F, 0.12F, 0.70F, 0.92F, 1.0F, fade * 0.78F);
        renderConeArc(matrix, consumer, 1.45F, 3.45F, 0.48F, 0.04F, 1.0F, 1.0F, 1.0F, fade * 0.92F);
        renderImpactSpokes(matrix, consumer, fade, progress);

        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    private static void renderConeArc(Matrix4f matrix, VertexConsumer consumer, float startZ, float length, float width, float y, float red, float green, float blue, float alpha) {
        for (int i = 0; i < ARC_SEGMENTS; i++) {
            float t0 = i / (float) ARC_SEGMENTS;
            float t1 = (i + 1) / (float) ARC_SEGMENTS;
            WavePoint inner0 = conePoint(t0, startZ, length, width * 0.52F, y, false);
            WavePoint inner1 = conePoint(t1, startZ, length, width * 0.52F, y, false);
            WavePoint outer1 = conePoint(t1, startZ, length, width, y, true);
            WavePoint outer0 = conePoint(t0, startZ, length, width, y, true);
            float a0 = alpha * taper(t0);
            float a1 = alpha * taper(t1);
            vertex(matrix, consumer, inner0, red, green, blue, a0 * 0.45F);
            vertex(matrix, consumer, inner1, red, green, blue, a1 * 0.45F);
            vertex(matrix, consumer, outer1, red, green, blue, a1);
            vertex(matrix, consumer, outer0, red, green, blue, a0);
        }
    }

    private static WavePoint conePoint(float t, float startZ, float length, float width, float y, boolean outer) {
        float side = (t - 0.5F) * 2.0F;
        float z = startZ + length * (0.15F + Mth.sin(t * Mth.PI) * 0.85F);
        float spread = width * (0.28F + Mth.sin(t * Mth.PI) * 0.72F);
        float x = side * spread;
        float lip = outer ? 0.18F : -0.1F;
        return new WavePoint(x + side * lip, y + Mth.sin(t * Mth.PI) * 0.38F, z);
    }

    private static void renderImpactSpokes(Matrix4f matrix, VertexConsumer consumer, float fade, float progress) {
        for (int i = 0; i < 10; i++) {
            float side = (i - 4.5F) / 4.5F;
            float baseZ = 0.65F + progress * 0.8F;
            float endZ = 3.9F + (i % 3) * 0.32F;
            float baseX = side * 0.34F;
            float endX = side * (0.9F + Math.abs(side) * 0.72F);
            float alpha = fade * (0.36F + (i % 2) * 0.24F);
            vertex(matrix, consumer, new WavePoint(baseX - 0.04F, 0.1F, baseZ), 0.35F, 0.72F, 1.0F, alpha * 0.18F);
            vertex(matrix, consumer, new WavePoint(baseX + 0.04F, 0.1F, baseZ), 0.35F, 0.72F, 1.0F, alpha * 0.18F);
            vertex(matrix, consumer, new WavePoint(endX + 0.08F * side, 0.58F, endZ), 0.92F, 0.98F, 1.0F, alpha);
            vertex(matrix, consumer, new WavePoint(endX - 0.08F * side, 0.42F, endZ - 0.28F), 0.14F, 0.45F, 1.0F, alpha * 0.42F);
        }
    }

    private static float taper(float t) {
        return Mth.clamp(Mth.sin(t * Mth.PI) * 1.22F, 0.0F, 1.0F);
    }

    private static void vertex(Matrix4f matrix, VertexConsumer consumer, WavePoint point, float red, float green, float blue, float alpha) {
        consumer.vertex(matrix, point.x(), point.y(), point.z()).color(red, green, blue, alpha).endVertex();
    }

    @Override
    public ResourceLocation getTextureLocation(HammerShockwaveEntity entity) {
        return null;
    }

    private record WavePoint(float x, float y, float z) {
    }
}
