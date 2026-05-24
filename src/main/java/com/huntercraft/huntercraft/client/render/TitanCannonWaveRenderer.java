package com.huntercraft.huntercraft.client.render;

import com.huntercraft.huntercraft.entity.TitanCannonWaveEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public class TitanCannonWaveRenderer extends EntityRenderer<TitanCannonWaveEntity> {
    private static final int SEGMENTS = 34;

    public TitanCannonWaveRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
    }

    @Override
    public void render(TitanCannonWaveEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        float fade = Mth.clamp(entity.getFade(partialTick), 0.0F, 1.0F);
        if (fade <= 0.01F) {
            return;
        }
        Vec3 direction = entity.getPulseDirection();
        float yaw = direction.lengthSqr() > 1.0E-5D ? (float) Math.toDegrees(Math.atan2(direction.x, direction.z)) : entityYaw;
        float pitch = direction.lengthSqr() > 1.0E-5D ? (float) -Math.toDegrees(Math.atan2(direction.y, Math.sqrt(direction.x * direction.x + direction.z * direction.z))) : entity.getXRot();
        float progress = 1.0F - fade;
        float scale = entity.getScale();

        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - yaw));
        poseStack.mulPose(Axis.XP.rotationDegrees(pitch));
        poseStack.scale(scale, scale, scale);
        Matrix4f matrix = poseStack.last().pose();
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lightning());

        renderPressureRing(matrix, consumer, 0.5F + progress * 0.9F, 0.13F, 0.22F, 0.55F, 1.0F, fade * 0.62F);
        renderPressureRing(matrix, consumer, 0.92F + progress * 1.3F, 0.09F, 0.8F, 0.92F, 1.0F, fade * 0.46F);
        renderWakeCone(matrix, consumer, progress, fade);
        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    private static void renderPressureRing(Matrix4f matrix, VertexConsumer consumer, float radius, float halfWidth, float red, float green, float blue, float alpha) {
        float z = 0.0F;
        for (int i = 0; i < SEGMENTS; i++) {
            float a0 = i * Mth.TWO_PI / SEGMENTS;
            float a1 = (i + 1) * Mth.TWO_PI / SEGMENTS;
            float inner = radius - halfWidth;
            float outer = radius + halfWidth;
            vertex(matrix, consumer, Mth.cos(a0) * inner, Mth.sin(a0) * inner, z, red, green, blue, alpha * 0.18F);
            vertex(matrix, consumer, Mth.cos(a1) * inner, Mth.sin(a1) * inner, z, red, green, blue, alpha * 0.18F);
            vertex(matrix, consumer, Mth.cos(a1) * outer, Mth.sin(a1) * outer, z, red, green, blue, alpha);
            vertex(matrix, consumer, Mth.cos(a0) * outer, Mth.sin(a0) * outer, z, red, green, blue, alpha);
        }
    }

    private static void renderWakeCone(Matrix4f matrix, VertexConsumer consumer, float progress, float fade) {
        float back = -0.25F - progress * 0.45F;
        float front = 1.25F + progress * 0.55F;
        for (int i = 0; i < 10; i++) {
            float side = (i - 4.5F) / 4.5F;
            float width = 0.25F + Math.abs(side) * 0.95F + progress * 0.45F;
            float alpha = fade * (0.14F + (i % 3) * 0.08F);
            vertex(matrix, consumer, side * 0.18F, 0.02F, back, 0.28F, 0.62F, 1.0F, alpha * 0.35F);
            vertex(matrix, consumer, side * 0.28F, 0.12F, back + 0.22F, 0.42F, 0.78F, 1.0F, alpha * 0.45F);
            vertex(matrix, consumer, side * width, 0.42F + Math.abs(side) * 0.18F, front, 0.82F, 0.96F, 1.0F, alpha);
            vertex(matrix, consumer, side * width * 0.7F, -0.2F, front - 0.25F, 0.18F, 0.46F, 1.0F, alpha * 0.22F);
        }
    }

    private static void vertex(Matrix4f matrix, VertexConsumer consumer, float x, float y, float z, float red, float green, float blue, float alpha) {
        consumer.vertex(matrix, x, y, z).color(red, green, blue, alpha).endVertex();
    }

    @Override
    public ResourceLocation getTextureLocation(TitanCannonWaveEntity entity) {
        return null;
    }
}
