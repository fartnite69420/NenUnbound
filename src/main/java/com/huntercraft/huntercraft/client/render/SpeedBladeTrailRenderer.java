package com.huntercraft.huntercraft.client.render;

import com.huntercraft.huntercraft.entity.SpeedBladeTrailEntity;
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

public class SpeedBladeTrailRenderer extends EntityRenderer<SpeedBladeTrailEntity> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("textures/misc/white.png");

    public SpeedBladeTrailRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
    }

    @Override
    public void render(SpeedBladeTrailEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        float fade = Mth.clamp(entity.getFade(partialTick), 0.0F, 1.0F);
        if (fade <= 0.01F) {
            return;
        }
        int color = entity.getColor();
        float red = ((color >> 16) & 255) / 255.0F;
        float green = ((color >> 8) & 255) / 255.0F;
        float blue = (color & 255) / 255.0F;
        float age = entity.tickCount + partialTick;
        float pulse = 0.9F + (Mth.sin(age * 0.55F) * 0.08F);

        poseStack.pushPose();
        float pitch = Mth.lerp(partialTick, entity.xRotO, entity.getXRot());
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - entityYaw));
        poseStack.mulPose(Axis.XP.rotationDegrees(pitch));

        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityTranslucent(TEXTURE));
        renderBladeQuad(poseStack, consumer, entity.getLength(), entity.getWidth() * 1.85F * pulse, red * 0.22F, green * 0.58F, blue, fade * 0.28F, -0.028F);
        renderBladeQuad(poseStack, consumer, entity.getLength() * 0.96F, entity.getWidth() * 0.92F * pulse, red * 0.62F, green * 0.9F, blue, fade * 0.72F, -0.018F);
        renderBladeQuad(poseStack, consumer, entity.getLength() * 0.84F, entity.getWidth() * 0.22F, 1.0F, 1.0F, 1.0F, fade * 0.95F, -0.01F);

        poseStack.mulPose(Axis.ZP.rotationDegrees(90.0F));
        renderBladeQuad(poseStack, consumer, entity.getLength() * 0.56F, entity.getWidth() * 0.1F, 1.0F, 1.0F, 1.0F, fade * 0.55F, -0.006F);
        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    private static void renderBladeQuad(PoseStack poseStack, VertexConsumer consumer, float length, float width, float red, float green, float blue, float alpha, float z) {
        if (alpha <= 0.0F) {
            return;
        }
        float halfWidth = width * 0.5F;
        Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();
        Matrix3f normal = pose.normal();
        consumer.vertex(matrix, -halfWidth, 0.0F, z).color(red, green, blue, alpha).uv(0.0F, 1.0F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(normal, 0.0F, 0.0F, -1.0F).endVertex();
        consumer.vertex(matrix, halfWidth, 0.0F, z).color(red, green, blue, alpha).uv(1.0F, 1.0F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(normal, 0.0F, 0.0F, -1.0F).endVertex();
        consumer.vertex(matrix, halfWidth * 0.28F, length, z).color(red, green, blue, alpha * 0.04F).uv(1.0F, 0.0F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(normal, 0.0F, 0.0F, -1.0F).endVertex();
        consumer.vertex(matrix, -halfWidth * 0.28F, length, z).color(red, green, blue, alpha * 0.04F).uv(0.0F, 0.0F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(normal, 0.0F, 0.0F, -1.0F).endVertex();
    }

    @Override
    public ResourceLocation getTextureLocation(SpeedBladeTrailEntity entity) {
        return TEXTURE;
    }
}
