package com.huntercraft.huntercraft.client.render;

import com.huntercraft.huntercraft.entity.HeavenSplitterSlashEntity;
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

public class HeavenSplitterSlashRenderer extends EntityRenderer<HeavenSplitterSlashEntity> {
    private static final int SEGMENTS = 18;

    public HeavenSplitterSlashRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
    }

    @Override
    public void render(HeavenSplitterSlashEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        float fade = Mth.clamp(entity.getFade(partialTick), 0.0F, 1.0F);
        if (fade <= 0.01F) {
            return;
        }

        float age = entity.tickCount + partialTick;
        float lifeProgress = 1.0F - fade;
        float bloom = Mth.sin(Mth.clamp(lifeProgress * Mth.PI, 0.0F, Mth.PI));
        float scale = entity.getScale() * (0.92F + bloom * 0.18F);

        int color = entity.getColor();
        float red = ((color >> 16) & 255) / 255.0F;
        float green = ((color >> 8) & 255) / 255.0F;
        float blue = (color & 255) / 255.0F;

        poseStack.pushPose();
        float yaw = Mth.lerp(partialTick, entity.yRotO, entity.getYRot());
        poseStack.mulPose(Axis.YP.rotationDegrees(-yaw));
        poseStack.mulPose(Axis.ZP.rotationDegrees(entity.getTilt()));
        poseStack.scale(scale, scale, scale);

        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lightning());
        renderBladeLayer(poseStack, consumer, -0.22F, age * -0.018F, 1.32F, 0.98F, red * 0.48F, green * 0.04F, blue * 0.05F, fade * 0.38F);
        renderBladeLayer(poseStack, consumer, -0.08F, age * -0.012F, 1.10F, 1.00F, red, green * 0.12F, blue * 0.10F, fade * 0.86F);
        renderBladeLayer(poseStack, consumer, 0.05F, age * -0.006F, 0.58F, 0.82F, 1.0F, 0.78F, 0.70F, fade);
        renderCuttingEdge(poseStack, consumer, 1.0F, 0.92F, 0.88F, fade);

        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    private static void renderBladeLayer(PoseStack poseStack, VertexConsumer consumer, float xOffset, float zOffset, float widthScale, float heightScale, float red, float green, float blue, float alphaScale) {
        Matrix4f matrix = poseStack.last().pose();
        for (int i = 0; i < SEGMENTS; i++) {
            float t0 = i / (float) SEGMENTS;
            float t1 = (i + 1) / (float) SEGMENTS;
            BladePoint inner0 = bladePoint(t0, xOffset, zOffset, widthScale, heightScale, false);
            BladePoint inner1 = bladePoint(t1, xOffset, zOffset, widthScale, heightScale, false);
            BladePoint outer1 = bladePoint(t1, xOffset, zOffset, widthScale, heightScale, true);
            BladePoint outer0 = bladePoint(t0, xOffset, zOffset, widthScale, heightScale, true);
            float alpha0 = alphaScale * taper(t0);
            float alpha1 = alphaScale * taper(t1);
            quad(matrix, consumer, inner0, inner1, outer1, outer0, red, green, blue, alpha0, alpha1);
            quad(matrix, consumer, outer0, outer1, inner1, inner0, red, green, blue, alpha0 * 0.78F, alpha1 * 0.78F);
        }
    }

    private static void renderCuttingEdge(PoseStack poseStack, VertexConsumer consumer, float red, float green, float blue, float fade) {
        Matrix4f matrix = poseStack.last().pose();
        for (int i = 0; i < SEGMENTS; i++) {
            float t0 = i / (float) SEGMENTS;
            float t1 = (i + 1) / (float) SEGMENTS;
            BladePoint a0 = edgePoint(t0, -0.012F);
            BladePoint a1 = edgePoint(t1, -0.012F);
            BladePoint b1 = edgePoint(t1, 0.045F * taper(t1));
            BladePoint b0 = edgePoint(t0, 0.045F * taper(t0));
            float alpha0 = fade * taper(t0) * 0.92F;
            float alpha1 = fade * taper(t1) * 0.92F;
            quad(matrix, consumer, a0, a1, b1, b0, red, green, blue, alpha0, alpha1);
        }
    }

    private static BladePoint bladePoint(float t, float xOffset, float zOffset, float widthScale, float heightScale, boolean outer) {
        float bend = Mth.sin((t * 1.12F - 0.08F) * Mth.PI);
        float hook = Mth.sin(t * Mth.PI * 0.62F);
        float centerX = xOffset + Mth.sin(t * Mth.PI) * 0.18F;
        float centerY = Mth.lerp(t, 2.95F * heightScale, -0.38F);
        float centerZ = -0.88F + bend * 1.92F + hook * 0.34F + zOffset;
        float width = (0.16F + Mth.sin(t * Mth.PI) * 0.92F) * widthScale;
        float edgeBias = outer ? width : -width * 0.34F;
        float pointPull = t > 0.78F ? (t - 0.78F) * 1.9F : 0.0F;
        return new BladePoint(centerX + edgeBias, centerY, centerZ + pointPull + (outer ? 0.018F : -0.018F));
    }

    private static BladePoint edgePoint(float t, float edgeInset) {
        BladePoint outer = bladePoint(t, 0.05F, -0.012F, 0.62F, 0.82F, true);
        return new BladePoint(outer.x() + edgeInset, outer.y(), outer.z() - 0.026F);
    }

    private static float taper(float t) {
        return Mth.clamp(Mth.sin(t * Mth.PI) * 1.2F, 0.0F, 1.0F);
    }

    private static void quad(Matrix4f matrix, VertexConsumer consumer, BladePoint p0, BladePoint p1, BladePoint p2, BladePoint p3, float red, float green, float blue, float alpha0, float alpha1) {
        consumer.vertex(matrix, p0.x(), p0.y(), p0.z()).color(red, green, blue, alpha0).endVertex();
        consumer.vertex(matrix, p1.x(), p1.y(), p1.z()).color(red, green, blue, alpha1).endVertex();
        consumer.vertex(matrix, p2.x(), p2.y(), p2.z()).color(red, green, blue, alpha1).endVertex();
        consumer.vertex(matrix, p3.x(), p3.y(), p3.z()).color(red, green, blue, alpha0).endVertex();
    }

    @Override
    public ResourceLocation getTextureLocation(HeavenSplitterSlashEntity entity) {
        return null;
    }

    private record BladePoint(float x, float y, float z) {
    }
}
