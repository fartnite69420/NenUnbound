package com.huntercraft.huntercraft.client.render;

import com.huntercraft.huntercraft.entity.BoxingShockwaveProjectileEntity;
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

public class BoxingShockwaveProjectileRenderer extends EntityRenderer<BoxingShockwaveProjectileEntity> {
    public BoxingShockwaveProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
    }

    @Override
    public void render(BoxingShockwaveProjectileEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        float fade = Mth.clamp(entity.getFade(partialTick), 0.0F, 1.0F);
        if (fade <= 0.01F) {
            return;
        }
        Vec3 direction = entity.getPulseDirection();
        float yaw = direction.lengthSqr() > 1.0E-5D ? (float) Math.toDegrees(Math.atan2(direction.x, direction.z)) : entityYaw;
        float pitch = direction.lengthSqr() > 1.0E-5D ? (float) -Math.toDegrees(Math.atan2(direction.y, Math.sqrt(direction.x * direction.x + direction.z * direction.z))) : entity.getXRot();
        float progress = 1.0F - fade;
        float scale = entity.getScale() * (0.92F + Mth.sin((entity.tickCount + partialTick) * 0.7F) * 0.07F);

        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - yaw));
        poseStack.mulPose(Axis.XP.rotationDegrees(pitch));
        poseStack.scale(scale, scale, scale);
        Matrix4f matrix = poseStack.last().pose();
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lightning());
        renderCone(matrix, consumer, 0.78F, 5.0F + progress * 1.1F, 1.75F + progress * 0.45F, fade * 0.52F, 0.95F, 0.96F, 1.0F);
        renderCone(matrix, consumer, 0.55F, 4.1F, 0.66F, fade * 0.88F, 0.32F, 0.62F, 1.0F);
        renderCore(matrix, consumer, 3.4F, 0.18F, fade * 0.92F);
        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    private static void renderCone(Matrix4f matrix, VertexConsumer consumer, float startZ, float length, float width, float alpha, float red, float green, float blue) {
        int segments = 24;
        for (int i = 0; i < segments; i++) {
            float t0 = i / (float) segments;
            float t1 = (i + 1) / (float) segments;
            float x0 = (t0 - 0.5F) * width * Mth.sin(t0 * Mth.PI);
            float x1 = (t1 - 0.5F) * width * Mth.sin(t1 * Mth.PI);
            float z0 = startZ + length * t0;
            float z1 = startZ + length * t1;
            vertex(consumer, matrix, x0 * 0.35F, 0.08F, z0, red, green, blue, alpha * 0.25F);
            vertex(consumer, matrix, x1 * 0.35F, 0.08F, z1, red, green, blue, alpha * 0.25F);
            vertex(consumer, matrix, x1, 0.52F * Mth.sin(t1 * Mth.PI), z1, red, green, blue, alpha);
            vertex(consumer, matrix, x0, 0.52F * Mth.sin(t0 * Mth.PI), z0, red, green, blue, alpha);
        }
    }

    private static void renderCore(Matrix4f matrix, VertexConsumer consumer, float length, float width, float alpha) {
        vertex(consumer, matrix, -width, 0.1F, 0.2F, 1.0F, 1.0F, 1.0F, alpha);
        vertex(consumer, matrix, width, 0.1F, 0.2F, 1.0F, 1.0F, 1.0F, alpha);
        vertex(consumer, matrix, width * 0.25F, 0.16F, length, 1.0F, 1.0F, 1.0F, alpha * 0.0F);
        vertex(consumer, matrix, -width * 0.25F, 0.16F, length, 1.0F, 1.0F, 1.0F, alpha * 0.35F);
    }

    private static void vertex(VertexConsumer consumer, Matrix4f matrix, float x, float y, float z, float red, float green, float blue, float alpha) {
        consumer.vertex(matrix, x, y, z).color(red, green, blue, alpha).endVertex();
    }

    @Override
    public ResourceLocation getTextureLocation(BoxingShockwaveProjectileEntity entity) {
        return null;
    }
}
