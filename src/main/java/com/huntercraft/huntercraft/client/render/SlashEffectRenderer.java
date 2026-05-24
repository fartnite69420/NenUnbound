package com.huntercraft.huntercraft.client.render;

import com.huntercraft.huntercraft.HunterCraftMod;
import com.huntercraft.huntercraft.entity.SlashEffectEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class SlashEffectRenderer extends EntityRenderer<SlashEffectEntity> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(HunterCraftMod.MODID, "textures/effects/slash_entity_texture.png");
    private static final float QUAD_WIDTH = 3.8F;
    private static final float QUAD_HEIGHT = 2.2F;

    public SlashEffectRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
    }

    @Override
    public void render(SlashEffectEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        float fade = Mth.clamp(entity.getFade(partialTick), 0.0F, 1.0F);
        if (fade <= 0.01F) {
            return;
        }

        float age = entity.tickCount + partialTick;
        float lifeProgress = 1.0F - fade;
        float snap = Mth.sin(Mth.clamp(lifeProgress * 3.1415927F, 0.0F, 3.1415927F));
        float scale = entity.getScale() * (1.0F + (snap * 0.16F));

        poseStack.pushPose();
        float pitch = Mth.lerp(partialTick, entity.xRotO, entity.getXRot());
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - entityYaw));
        poseStack.mulPose(Axis.XP.rotationDegrees(pitch));
        poseStack.mulPose(Axis.ZP.rotationDegrees(entity.getRoll()));
        poseStack.scale(scale, scale, scale);

        int color = entity.getColor();
        float tintRed = ((color >> 16) & 255) / 255.0F;
        float tintGreen = ((color >> 8) & 255) / 255.0F;
        float tintBlue = (color & 255) / 255.0F;
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityTranslucent(TEXTURE));
        renderGhostLayer(poseStack, consumer, fade, -0.36F, 0.08F, -8.0F, 1.16F, 0.92F, tintRed * 0.24F, tintGreen * 0.53F, tintBlue);
        renderGhostLayer(poseStack, consumer, fade, -0.23F, 0.04F, -4.0F, 1.08F, 0.98F, tintRed * 0.38F, tintGreen * 0.67F, tintBlue);
        renderGhostLayer(poseStack, consumer, fade, -0.10F, 0.00F, 0.0F, 1.0F, 1.0F, tintRed * 0.68F, tintGreen * 0.88F, tintBlue);
        renderSlashQuad(poseStack, consumer, tintRed, tintGreen, tintBlue, Math.round(232.0F * fade), QUAD_WIDTH * 0.78F, QUAD_HEIGHT * 0.74F);

        poseStack.pushPose();
        poseStack.translate(0.08F + (age * 0.006F), -0.015F, -0.006F);
        poseStack.mulPose(Axis.ZP.rotationDegrees(2.0F));
        renderSlashQuad(poseStack, consumer, tintRed * 0.88F, tintGreen * 0.98F, tintBlue, Math.round(180.0F * fade), QUAD_WIDTH * 0.9F, QUAD_HEIGHT * 0.8F);
        poseStack.popPose();

        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    private static void renderGhostLayer(PoseStack poseStack, VertexConsumer consumer, float fade, float xOffset, float yOffset, float roll, float widthScale, float heightScale, float red, float green, float blue) {
        poseStack.pushPose();
        poseStack.translate(xOffset, yOffset, -0.012F);
        poseStack.mulPose(Axis.ZP.rotationDegrees(roll));
        renderSlashQuad(
                poseStack,
                consumer,
                red,
                green,
                blue,
                Math.round(118.0F * fade),
                QUAD_WIDTH * widthScale,
                QUAD_HEIGHT * heightScale
        );
        poseStack.popPose();
    }

    private static void renderSlashQuad(PoseStack poseStack, VertexConsumer consumer, float red, float green, float blue, int alpha, float width, float height) {
        if (alpha <= 0) {
            return;
        }
        float halfWidth = width * 0.5F;
        float halfHeight = height * 0.5F;
        Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();
        Matrix3f normal = pose.normal();
        consumer.vertex(matrix, -halfWidth, -halfHeight, 0.0F).color(red, green, blue, alpha / 255.0F).uv(0.0F, 1.0F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(normal, 0.0F, 0.0F, -1.0F).endVertex();
        consumer.vertex(matrix, halfWidth, -halfHeight, 0.0F).color(red, green, blue, alpha / 255.0F).uv(1.0F, 1.0F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(normal, 0.0F, 0.0F, -1.0F).endVertex();
        consumer.vertex(matrix, halfWidth, halfHeight, 0.0F).color(red, green, blue, alpha / 255.0F).uv(1.0F, 0.0F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(normal, 0.0F, 0.0F, -1.0F).endVertex();
        consumer.vertex(matrix, -halfWidth, halfHeight, 0.0F).color(red, green, blue, alpha / 255.0F).uv(0.0F, 0.0F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(normal, 0.0F, 0.0F, -1.0F).endVertex();
    }

    @Override
    public ResourceLocation getTextureLocation(SlashEffectEntity entity) {
        return TEXTURE;
    }
}
