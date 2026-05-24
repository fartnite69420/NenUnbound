package com.huntercraft.huntercraft.client.render;

import com.huntercraft.huntercraft.HunterCraftMod;
import com.huntercraft.huntercraft.entity.SmokyJailBarrierEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class SmokyJailBarrierRenderer extends EntityRenderer<SmokyJailBarrierEntity> {
    private static final ResourceLocation SMOKE_TEXTURE = new ResourceLocation(HunterCraftMod.MODID, "textures/effects/solid_white.png");
    private static final int RINGS = 18;
    private static final int SEGMENTS = 56;

    public SmokyJailBarrierRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(SmokyJailBarrierEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        float radius = entity.getRadius();
        if (radius <= 1.0F) {
            return;
        }

        float time = entity.tickCount + partialTick;
        VertexConsumer consumer = buffer.getBuffer(RenderType.entityCutoutNoCull(SMOKE_TEXTURE));
        poseStack.pushPose();
        renderSmokeShell(poseStack, consumer, radius, time, packedLight, 1.0F, 0.98F);
        renderSmokeShell(poseStack, consumer, radius * 0.985F, time + 17.0F, packedLight, -1.0F, 0.82F);
        renderTopCap(poseStack, consumer, radius, time, packedLight);
        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    private static void renderSmokeShell(PoseStack poseStack, VertexConsumer consumer, float radius, float time, int packedLight, float winding, float alphaScale) {
        Matrix4f matrix = poseStack.last().pose();
        Matrix3f normal = poseStack.last().normal();
        for (int ring = 0; ring < RINGS; ring++) {
            float pitch0 = Mth.lerp(ring / (float) RINGS, 0.02F, Mth.PI - 0.02F);
            float pitch1 = Mth.lerp((ring + 1) / (float) RINGS, 0.02F, Mth.PI - 0.02F);
            for (int segment = 0; segment < SEGMENTS; segment++) {
                float yaw0 = (segment / (float) SEGMENTS) * Mth.TWO_PI;
                float yaw1 = ((segment + 1) / (float) SEGMENTS) * Mth.TWO_PI;
                float noise = 0.76F + 0.18F * Mth.sin(time * 0.035F + ring * 0.83F + segment * 0.41F);
                float alpha = 1.0F;
                float tone = 0.84F + 0.12F * Mth.sin(time * 0.027F + segment * 0.67F);
                if (winding > 0.0F) {
                    smokeVertex(matrix, normal, consumer, radius, pitch0, yaw0, tone, alpha, packedLight);
                    smokeVertex(matrix, normal, consumer, radius, pitch1, yaw0, tone, alpha, packedLight);
                    smokeVertex(matrix, normal, consumer, radius, pitch1, yaw1, tone, alpha, packedLight);
                    smokeVertex(matrix, normal, consumer, radius, pitch0, yaw1, tone, alpha, packedLight);
                } else {
                    smokeVertex(matrix, normal, consumer, radius, pitch0, yaw1, tone, alpha, packedLight);
                    smokeVertex(matrix, normal, consumer, radius, pitch1, yaw1, tone, alpha, packedLight);
                    smokeVertex(matrix, normal, consumer, radius, pitch1, yaw0, tone, alpha, packedLight);
                    smokeVertex(matrix, normal, consumer, radius, pitch0, yaw0, tone, alpha, packedLight);
                }
            }
        }
    }

    private static void renderTopCap(PoseStack poseStack, VertexConsumer consumer, float radius, float time, int packedLight) {
        Matrix4f matrix = poseStack.last().pose();
        Matrix3f normal = poseStack.last().normal();
        float topY = radius;
        int capRings = 8;
        for (int ring = 0; ring < capRings; ring++) {
            float inner = (ring / (float) capRings) * radius * 0.46F;
            float outer = ((ring + 1) / (float) capRings) * radius * 0.46F;
            float y0 = topY - (inner / radius) * (inner / radius) * radius * 0.12F;
            float y1 = topY - (outer / radius) * (outer / radius) * radius * 0.12F;
            for (int segment = 0; segment < SEGMENTS; segment++) {
                float yaw0 = (segment / (float) SEGMENTS) * Mth.TWO_PI + time * 0.01F;
                float yaw1 = ((segment + 1) / (float) SEGMENTS) * Mth.TWO_PI + time * 0.01F;
                float tone = 0.88F + 0.08F * Mth.sin(time * 0.04F + ring + segment * 0.4F);
                float alpha = 1.0F;
                flatVertex(matrix, normal, consumer, inner, y0, yaw0, tone, alpha, packedLight);
                flatVertex(matrix, normal, consumer, outer, y1, yaw0, tone, alpha, packedLight);
                flatVertex(matrix, normal, consumer, outer, y1, yaw1, tone, alpha, packedLight);
                flatVertex(matrix, normal, consumer, inner, y0, yaw1, tone, alpha, packedLight);
            }
        }
    }

    private static void smokeVertex(Matrix4f matrix, Matrix3f normal, VertexConsumer consumer, float radius, float pitch, float yaw, float tone, float alpha, int packedLight) {
        float sinPitch = Mth.sin(pitch);
        float x = Mth.cos(yaw) * sinPitch * radius;
        float y = Mth.cos(pitch) * radius;
        float z = Mth.sin(yaw) * sinPitch * radius;
        float u = yaw / Mth.TWO_PI;
        float v = pitch / Mth.PI;
        consumer.vertex(matrix, x, y, z)
                .color(tone, tone, tone, alpha)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(normal, 0.0F, 1.0F, 0.0F)
                .endVertex();
    }

    private static void flatVertex(Matrix4f matrix, Matrix3f normal, VertexConsumer consumer, float radius, float y, float yaw, float tone, float alpha, int packedLight) {
        float x = Mth.cos(yaw) * radius;
        float z = Mth.sin(yaw) * radius;
        consumer.vertex(matrix, x, y, z)
                .color(tone, tone, tone, alpha)
                .uv(radius * 0.03F, yaw / Mth.TWO_PI)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(normal, 0.0F, 1.0F, 0.0F)
                .endVertex();
    }

    @Override
    public ResourceLocation getTextureLocation(SmokyJailBarrierEntity entity) {
        return SMOKE_TEXTURE;
    }
}
