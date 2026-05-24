package com.huntercraft.huntercraft.client.render;

import com.huntercraft.huntercraft.entity.AegisSlamEffectEntity;
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

public class AegisSlamEffectRenderer extends EntityRenderer<AegisSlamEffectEntity> {
    public AegisSlamEffectRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
    }

    @Override
    public void render(AegisSlamEffectEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        float fade = Mth.clamp(entity.getFade(partialTick), 0.0F, 1.0F);
        if (fade <= 0.01F) {
            return;
        }
        float progress = 1.0F - fade;
        int color = entity.getColor();
        float red = ((color >> 16) & 255) / 255.0F;
        float green = ((color >> 8) & 255) / 255.0F;
        float blue = (color & 255) / 255.0F;
        float scale = entity.getScale();

        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(-entityYaw));
        Matrix4f matrix = poseStack.last().pose();
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lightning());
        renderVerticalCut(matrix, consumer, scale, red, green, blue, fade);
        renderVerticalCut(matrix, consumer, scale * 0.78F, 1.0F, 1.0F, 1.0F, fade * 0.72F);
        renderGroundBurst(matrix, consumer, scale * (0.6F + progress * 1.25F), red, green, blue, fade);
        poseStack.popPose();

        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    private static void renderVerticalCut(Matrix4f matrix, VertexConsumer consumer, float scale, float red, float green, float blue, float alpha) {
        float height = 4.4F * scale;
        float halfWidth = 0.22F * scale;
        float bottom = 0.02F;
        float top = height;
        vertex(consumer, matrix, -halfWidth, bottom, 0.0F, red, green, blue, alpha * 0.0F);
        vertex(consumer, matrix, halfWidth, bottom, 0.0F, red, green, blue, alpha * 0.85F);
        vertex(consumer, matrix, halfWidth * 0.35F, top, 0.0F, red, green, blue, alpha * 0.0F);
        vertex(consumer, matrix, -halfWidth * 0.35F, top, 0.0F, red, green, blue, alpha * 0.85F);

        vertex(consumer, matrix, 0.0F, bottom, -halfWidth, red, green, blue, alpha * 0.0F);
        vertex(consumer, matrix, 0.0F, bottom, halfWidth, red, green, blue, alpha * 0.75F);
        vertex(consumer, matrix, 0.0F, top, halfWidth * 0.35F, red, green, blue, alpha * 0.0F);
        vertex(consumer, matrix, 0.0F, top, -halfWidth * 0.35F, red, green, blue, alpha * 0.75F);
    }

    private static void renderGroundBurst(Matrix4f matrix, VertexConsumer consumer, float radius, float red, float green, float blue, float alpha) {
        int segments = 18;
        float inner = radius * 0.26F;
        for (int i = 0; i < segments; i++) {
            float a0 = Mth.TWO_PI * i / segments;
            float a1 = Mth.TWO_PI * (i + 1) / segments;
            float x0 = Mth.cos(a0);
            float z0 = Mth.sin(a0);
            float x1 = Mth.cos(a1);
            float z1 = Mth.sin(a1);
            vertex(consumer, matrix, x0 * inner, 0.02F, z0 * inner, red, green, blue, alpha * 0.28F);
            vertex(consumer, matrix, x1 * inner, 0.02F, z1 * inner, red, green, blue, alpha * 0.28F);
            vertex(consumer, matrix, x1 * radius, 0.02F, z1 * radius, red, green, blue, alpha * 0.0F);
            vertex(consumer, matrix, x0 * radius, 0.02F, z0 * radius, red, green, blue, alpha * 0.0F);
        }
    }

    private static void vertex(VertexConsumer consumer, Matrix4f matrix, float x, float y, float z, float red, float green, float blue, float alpha) {
        consumer.vertex(matrix, x, y, z).color(red, green, blue, alpha).endVertex();
    }

    @Override
    public ResourceLocation getTextureLocation(AegisSlamEffectEntity entity) {
        return null;
    }
}
