package com.huntercraft.huntercraft.client.render;

import com.huntercraft.huntercraft.HunterCraftMod;
import com.huntercraft.huntercraft.entity.WhirlwindSlashEntity;
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

public class WhirlwindSlashRenderer extends EntityRenderer<WhirlwindSlashEntity> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(HunterCraftMod.MODID, "textures/effects/whirlwind_slash.png");
    private static final int SEGMENTS = 12;

    public WhirlwindSlashRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
    }

    @Override
    public void render(WhirlwindSlashEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        float fade = Mth.clamp(entity.getFade(partialTick), 0.0F, 1.0F);
        if (fade <= 0.01F) {
            return;
        }

        float age = entity.tickCount + partialTick;
        float angle = entity.getStartAngle() + (age * entity.getSpinSpeed());
        int color = entity.getColor();
        float red = ((color >> 16) & 255) / 255.0F;
        float green = ((color >> 8) & 255) / 255.0F;
        float blue = (color & 255) / 255.0F;

        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lightning());
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotation(angle));
        Matrix4f matrix = poseStack.last().pose();
        renderSlashArc(matrix, consumer, -entity.getArcLength() * 0.55F, entity.getArcLength() * 1.1F, entity.getRadius() * 1.04F, entity.getHeight(), 0.34F, red * 0.32F, green * 0.08F, blue * 0.08F, fade * 0.6F);
        renderSlashArc(matrix, consumer, -entity.getArcLength() * 0.5F, entity.getArcLength(), entity.getRadius(), entity.getHeight() * 0.72F, 0.2F, red, green * 0.16F, blue * 0.12F, fade);
        renderSlashArc(matrix, consumer, -entity.getArcLength() * 0.36F, entity.getArcLength() * 0.72F, entity.getRadius() * 0.98F, entity.getHeight() * 0.34F, 0.055F, 1.0F, 0.72F, 0.62F, fade * 0.84F);
        poseStack.popPose();

        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    private static void renderSlashArc(Matrix4f matrix, VertexConsumer consumer, float start, float arcLength, float radius, float lift, float maxWidth, float red, float green, float blue, float fade) {
        for (int i = 0; i < SEGMENTS; i++) {
            float t0 = i / (float) SEGMENTS;
            float t1 = (i + 1) / (float) SEGMENTS;
            float a0 = start + arcLength * t0;
            float a1 = start + arcLength * t1;
            float taper0 = Mth.sin(t0 * Mth.PI);
            float taper1 = Mth.sin(t1 * Mth.PI);
            float alpha0 = fade * taper0 * 0.92F;
            float alpha1 = fade * taper1 * 0.92F;
            float width0 = maxWidth * taper0;
            float width1 = maxWidth * taper1;
            float y0 = Mth.sin(t0 * Mth.PI) * lift * 0.22F;
            float y1 = Mth.sin(t1 * Mth.PI) * lift * 0.22F;

            float innerX0 = Mth.cos(a0) * (radius - width0 * 0.28F) - radius;
            float innerZ0 = Mth.sin(a0) * (radius - width0 * 0.28F);
            float outerX0 = Mth.cos(a0) * (radius + width0) - radius;
            float outerZ0 = Mth.sin(a0) * (radius + width0);
            float innerX1 = Mth.cos(a1) * (radius - width1 * 0.28F) - radius;
            float innerZ1 = Mth.sin(a1) * (radius - width1 * 0.28F);
            float outerX1 = Mth.cos(a1) * (radius + width1) - radius;
            float outerZ1 = Mth.sin(a1) * (radius + width1);

            consumer.vertex(matrix, innerX0, y0, innerZ0).color(red, green, blue, alpha0).endVertex();
            consumer.vertex(matrix, innerX1, y1, innerZ1).color(red, green, blue, alpha1).endVertex();
            consumer.vertex(matrix, outerX1, y1 + width1 * 0.06F, outerZ1).color(red, green, blue, alpha1).endVertex();
            consumer.vertex(matrix, outerX0, y0 + width0 * 0.06F, outerZ0).color(red, green, blue, alpha0).endVertex();

            consumer.vertex(matrix, outerX0, y0 + width0 * 0.06F, outerZ0).color(red, green, blue, alpha0 * 0.72F).endVertex();
            consumer.vertex(matrix, outerX1, y1 + width1 * 0.06F, outerZ1).color(red, green, blue, alpha1 * 0.72F).endVertex();
            consumer.vertex(matrix, innerX1, y1, innerZ1).color(red, green, blue, alpha1 * 0.72F).endVertex();
            consumer.vertex(matrix, innerX0, y0, innerZ0).color(red, green, blue, alpha0 * 0.72F).endVertex();
        }
    }

    @Override
    public ResourceLocation getTextureLocation(WhirlwindSlashEntity entity) {
        return TEXTURE;
    }
}
