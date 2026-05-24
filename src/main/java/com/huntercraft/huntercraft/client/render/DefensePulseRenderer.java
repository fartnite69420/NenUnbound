package com.huntercraft.huntercraft.client.render;

import com.huntercraft.huntercraft.entity.DefensePulseEntity;
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

public class DefensePulseRenderer extends EntityRenderer<DefensePulseEntity> {
    public DefensePulseRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
    }

    @Override
    public void render(DefensePulseEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        float fade = Mth.clamp(entity.getFade(partialTick), 0.0F, 1.0F);
        if (fade <= 0.01F) {
            return;
        }
        int color = entity.getColor();
        float red = ((color >> 16) & 255) / 255.0F;
        float green = ((color >> 8) & 255) / 255.0F;
        float blue = (color & 255) / 255.0F;
        float progress = 1.0F - fade;
        float scale = entity.getScale();

        poseStack.pushPose();
        net.minecraft.world.phys.Vec3 direction = entity.getPulseDirection();
        float yaw = direction.lengthSqr() > 1.0E-5D
                ? (float) Math.toDegrees(Math.atan2(direction.x, direction.z))
                : Mth.rotLerp(partialTick, entity.yRotO, entity.getYRot());
        float pitch = direction.lengthSqr() > 1.0E-5D
                ? (float) -Math.toDegrees(Math.atan2(direction.y, Math.sqrt((direction.x * direction.x) + (direction.z * direction.z))))
                : Mth.lerp(partialTick, entity.xRotO, entity.getXRot());
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - yaw));
        poseStack.mulPose(Axis.XP.rotationDegrees(pitch));
        Matrix4f matrix = poseStack.last().pose();
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lightning());
        if (entity.getMode() == DefensePulseEntity.MODE_PROJECTILE) {
            renderProjectile(matrix, consumer, scale, red, green, blue, fade, entity.tickCount + partialTick);
        } else {
            renderIchimonji(matrix, consumer, scale, red, green, blue, fade, progress);
        }
        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    private static void renderProjectile(Matrix4f matrix, VertexConsumer consumer, float scale, float red, float green, float blue, float alpha, float age) {
        float pulse = 0.9F + Mth.sin(age * 0.8F) * 0.08F;
        renderBlade(matrix, consumer, 3.6F * scale, 0.72F * scale * pulse, red, green, blue, alpha * 0.72F);
        renderBlade(matrix, consumer, 2.6F * scale, 0.22F * scale, 1.0F, 1.0F, 1.0F, alpha * 0.82F);
        renderRing(matrix, consumer, 0.64F * scale * pulse, 0.12F * scale, red, green, blue, alpha * 0.62F, 1.1F * scale);
        renderRing(matrix, consumer, 0.42F * scale * pulse, 0.08F * scale, 1.0F, 1.0F, 1.0F, alpha * 0.5F, 0.08F);
    }

    private static void renderIchimonji(Matrix4f matrix, VertexConsumer consumer, float scale, float red, float green, float blue, float alpha, float progress) {
        float radius = scale * (1.2F + progress * 2.8F);
        renderVerticalSlash(matrix, consumer, scale * 5.2F, scale * 0.32F, 0.02F, red, green, blue, alpha * 0.92F);
        renderVerticalSlash(matrix, consumer, scale * 4.6F, scale * 0.12F, 0.05F, 1.0F, 1.0F, 1.0F, alpha * 0.72F);
        renderGroundFan(matrix, consumer, radius, red, green, blue, alpha * 0.62F);
        for (int i = 0; i < 8; i++) {
            float angle = Mth.TWO_PI * i / 8.0F;
            float x = Mth.cos(angle) * radius * 0.52F;
            float z = Mth.sin(angle) * radius * 0.52F;
            renderSpike(matrix, consumer, x, z, scale * (1.2F + progress * 1.1F), red * 0.18F, green * 0.05F, blue * 0.05F, alpha * 0.7F);
        }
    }

    private static void renderBlade(Matrix4f matrix, VertexConsumer consumer, float length, float width, float red, float green, float blue, float alpha) {
        float half = width * 0.5F;
        vertex(consumer, matrix, -half, -half * 0.2F, 0.0F, red, green, blue, alpha * 0.0F);
        vertex(consumer, matrix, half, half * 0.2F, 0.0F, red, green, blue, alpha);
        vertex(consumer, matrix, half * 0.25F, half * 0.1F, length, red, green, blue, alpha * 0.0F);
        vertex(consumer, matrix, -half * 0.25F, -half * 0.1F, length, red, green, blue, alpha * 0.65F);
    }

    private static void renderRing(Matrix4f matrix, VertexConsumer consumer, float radius, float width, float red, float green, float blue, float alpha, float z) {
        int segments = 18;
        float inner = radius - width;
        float outer = radius + width;
        for (int i = 0; i < segments; i++) {
            float a0 = Mth.TWO_PI * i / segments;
            float a1 = Mth.TWO_PI * (i + 1) / segments;
            vertex(consumer, matrix, Mth.cos(a0) * inner, Mth.sin(a0) * inner, z, red, green, blue, alpha * 0.35F);
            vertex(consumer, matrix, Mth.cos(a1) * inner, Mth.sin(a1) * inner, z, red, green, blue, alpha * 0.35F);
            vertex(consumer, matrix, Mth.cos(a1) * outer, Mth.sin(a1) * outer, z, red, green, blue, alpha * 0.0F);
            vertex(consumer, matrix, Mth.cos(a0) * outer, Mth.sin(a0) * outer, z, red, green, blue, alpha * 0.0F);
        }
    }

    private static void renderVerticalSlash(Matrix4f matrix, VertexConsumer consumer, float height, float width, float z, float red, float green, float blue, float alpha) {
        vertex(consumer, matrix, -width, 0.0F, z, red, green, blue, alpha * 0.0F);
        vertex(consumer, matrix, width, 0.0F, z, red, green, blue, alpha);
        vertex(consumer, matrix, width * 0.25F, height, z, red, green, blue, alpha * 0.0F);
        vertex(consumer, matrix, -width * 0.25F, height, z, red, green, blue, alpha * 0.72F);
    }

    private static void renderGroundFan(Matrix4f matrix, VertexConsumer consumer, float radius, float red, float green, float blue, float alpha) {
        int segments = 22;
        for (int i = 0; i < segments; i++) {
            float a0 = Mth.TWO_PI * i / segments;
            float a1 = Mth.TWO_PI * (i + 1) / segments;
            vertex(consumer, matrix, 0.0F, 0.03F, 0.0F, red, green, blue, alpha * 0.38F);
            vertex(consumer, matrix, Mth.cos(a0) * radius, 0.03F, Mth.sin(a0) * radius, red, green, blue, alpha * 0.0F);
            vertex(consumer, matrix, Mth.cos(a1) * radius, 0.03F, Mth.sin(a1) * radius, red, green, blue, alpha * 0.0F);
        }
    }

    private static void renderSpike(Matrix4f matrix, VertexConsumer consumer, float x, float z, float height, float red, float green, float blue, float alpha) {
        float w = 0.12F;
        vertex(consumer, matrix, x - w, 0.04F, z, red, green, blue, alpha);
        vertex(consumer, matrix, x + w, 0.04F, z, red, green, blue, alpha);
        vertex(consumer, matrix, x, height, z, red, green, blue, alpha * 0.0F);
    }

    private static void vertex(VertexConsumer consumer, Matrix4f matrix, float x, float y, float z, float red, float green, float blue, float alpha) {
        consumer.vertex(matrix, x, y, z).color(red, green, blue, alpha).endVertex();
    }

    @Override
    public ResourceLocation getTextureLocation(DefensePulseEntity entity) {
        return null;
    }
}
