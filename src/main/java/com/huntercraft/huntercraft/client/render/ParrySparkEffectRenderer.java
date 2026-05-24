package com.huntercraft.huntercraft.client.render;

import com.huntercraft.huntercraft.entity.ParrySparkEffectEntity;
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

public class ParrySparkEffectRenderer extends EntityRenderer<ParrySparkEffectEntity> {
    public ParrySparkEffectRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
    }

    @Override
    public void render(ParrySparkEffectEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        float fade = Mth.clamp(entity.getFade(partialTick), 0.0F, 1.0F);
        if (fade <= 0.01F) {
            return;
        }
        float progress = 1.0F - fade;
        float flash = Mth.sin(Mth.clamp(progress * Mth.PI, 0.0F, Mth.PI));
        float scale = entity.getScale() * (0.85F + progress * 0.45F + flash * 0.2F);

        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(-Mth.lerp(partialTick, entity.yRotO, entity.getYRot())));
        poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
        poseStack.scale(scale, scale, scale);

        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lightning());
        Matrix4f matrix = poseStack.last().pose();
        renderCoreFlash(matrix, consumer, fade, flash);
        renderSparks(matrix, consumer, fade, progress);
        renderCrossSlash(matrix, consumer, fade);

        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    private static void renderCoreFlash(Matrix4f matrix, VertexConsumer consumer, float fade, float flash) {
        float alpha = fade * (0.72F + flash * 0.28F);
        renderDiamond(matrix, consumer, 0.0F, 1.12F, 0.0F, 0.58F, 0.38F, 1.0F, 0.94F, 0.2F, alpha);
        renderDiamond(matrix, consumer, 0.0F, 1.12F, 0.02F, 0.32F, 0.22F, 1.0F, 1.0F, 0.82F, alpha);
    }

    private static void renderSparks(Matrix4f matrix, VertexConsumer consumer, float fade, float progress) {
        for (int i = 0; i < 18; i++) {
            float angle = i * Mth.TWO_PI / 18.0F + (i % 3) * 0.21F;
            float length = 0.72F + (i % 5) * 0.18F + progress * 0.75F;
            float yStart = 1.1F + ((i % 4) - 1.5F) * 0.08F;
            float yEnd = yStart + ((i % 5) - 2.0F) * 0.12F;
            float x0 = Mth.sin(angle) * 0.14F;
            float z0 = Mth.cos(angle) * 0.08F;
            float x1 = Mth.sin(angle) * length;
            float z1 = Mth.cos(angle) * length * 0.72F;
            float alpha = fade * (0.62F + (i % 2) * 0.24F);
            renderBeam(matrix, consumer, x0, yStart, z0, x1, yEnd, z1, 0.025F + (i % 3) * 0.008F, 1.0F, 0.78F, 0.05F, alpha);
        }
    }

    private static void renderCrossSlash(Matrix4f matrix, VertexConsumer consumer, float fade) {
        renderBeam(matrix, consumer, -0.65F, 0.68F, 0.06F, 0.7F, 1.52F, 0.02F, 0.055F, 1.0F, 0.96F, 0.42F, fade * 0.72F);
        renderBeam(matrix, consumer, 0.65F, 0.7F, -0.04F, -0.7F, 1.5F, 0.04F, 0.055F, 1.0F, 0.66F, 0.02F, fade * 0.62F);
    }

    private static void renderDiamond(Matrix4f matrix, VertexConsumer consumer, float x, float y, float z, float width, float height, float red, float green, float blue, float alpha) {
        vertex(matrix, consumer, x, y + height, z, red, green, blue, alpha);
        vertex(matrix, consumer, x + width, y, z, red, green, blue, alpha * 0.62F);
        vertex(matrix, consumer, x, y - height, z, red, green, blue, alpha);
        vertex(matrix, consumer, x - width, y, z, red, green, blue, alpha * 0.62F);
    }

    private static void renderBeam(Matrix4f matrix, VertexConsumer consumer, float x0, float y0, float z0, float x1, float y1, float z1, float halfWidth, float red, float green, float blue, float alpha) {
        float dx = x1 - x0;
        float dz = z1 - z0;
        float length = Mth.sqrt(dx * dx + dz * dz);
        float nx = length > 1.0E-4F ? -dz / length * halfWidth : halfWidth;
        float nz = length > 1.0E-4F ? dx / length * halfWidth : 0.0F;
        vertex(matrix, consumer, x0 - nx, y0, z0 - nz, red, green, blue, alpha * 0.2F);
        vertex(matrix, consumer, x0 + nx, y0, z0 + nz, red, green, blue, alpha * 0.2F);
        vertex(matrix, consumer, x1 + nx, y1, z1 + nz, red, green, blue, alpha);
        vertex(matrix, consumer, x1 - nx, y1, z1 - nz, red, green, blue, alpha);
    }

    private static void vertex(Matrix4f matrix, VertexConsumer consumer, float x, float y, float z, float red, float green, float blue, float alpha) {
        consumer.vertex(matrix, x, y, z).color(red, green, blue, alpha).endVertex();
    }

    @Override
    public ResourceLocation getTextureLocation(ParrySparkEffectEntity entity) {
        return null;
    }
}
