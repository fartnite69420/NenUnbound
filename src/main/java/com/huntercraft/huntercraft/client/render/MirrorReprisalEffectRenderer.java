package com.huntercraft.huntercraft.client.render;

import com.huntercraft.huntercraft.entity.MirrorReprisalEffectEntity;
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

public class MirrorReprisalEffectRenderer extends EntityRenderer<MirrorReprisalEffectEntity> {
    private static final int RING_SEGMENTS = 40;

    public MirrorReprisalEffectRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
    }

    @Override
    public void render(MirrorReprisalEffectEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        float fade = Mth.clamp(entity.getFade(partialTick), 0.0F, 1.0F);
        if (fade <= 0.01F) {
            return;
        }

        float age = entity.tickCount + partialTick;
        float progress = 1.0F - fade;
        float pulse = Mth.sin(Mth.clamp(progress * Mth.PI, 0.0F, Mth.PI));
        float scale = entity.getScale() * (0.92F + pulse * 0.18F);

        poseStack.pushPose();
        poseStack.scale(scale, scale, scale);
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lightning());
        if (entity.getMode() == MirrorReprisalEffectEntity.MODE_STRIKE) {
            poseStack.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partialTick, entity.yRotO, entity.getYRot())));
            renderStrike(poseStack.last().pose(), consumer, fade, age);
        } else {
            renderGuard(poseStack.last().pose(), consumer, fade, age);
        }
        poseStack.popPose();

        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    private static void renderGuard(Matrix4f matrix, VertexConsumer consumer, float fade, float age) {
        renderRing(matrix, consumer, 1.42F, 0.05F, 0.16F, 1.0F, 0.03F, 0.03F, fade * 0.92F);
        renderRing(matrix, consumer, 1.76F + Mth.sin(age * 0.28F) * 0.04F, 0.09F, 0.08F, 1.0F, 0.0F, 0.0F, fade * 0.48F);
        renderRing(matrix, consumer, 1.05F, 0.03F, 0.05F, 1.0F, 0.55F, 0.48F, fade * 0.55F);

        for (int i = 0; i < 8; i++) {
            float angle = age * 0.08F + (Mth.TWO_PI * i / 8.0F);
            float x = Mth.cos(angle) * 1.34F;
            float z = Mth.sin(angle) * 1.34F;
            renderShard(matrix, consumer, x, 0.1F, z, angle + Mth.HALF_PI, 0.34F, 0.76F, fade * 0.62F);
        }
    }

    private static void renderStrike(Matrix4f matrix, VertexConsumer consumer, float fade, float age) {
        renderRing(matrix, consumer, 1.9F, 0.08F, 0.08F, 1.0F, 0.0F, 0.0F, fade * 0.72F);
        renderRing(matrix, consumer, 1.18F, 0.035F, 0.06F, 1.0F, 0.62F, 0.56F, fade * 0.56F);
        for (int i = 0; i < 5; i++) {
            float offset = (i - 2) * 0.34F;
            renderForwardSlash(matrix, consumer, offset, -0.28F - i * 0.025F, 0.86F - Math.abs(offset) * 0.08F, fade);
        }
        for (int i = 0; i < 10; i++) {
            float angle = age * -0.12F + (Mth.TWO_PI * i / 10.0F);
            float radius = 1.0F + (i % 2) * 0.42F;
            renderShard(matrix, consumer, Mth.cos(angle) * radius, 0.28F, Mth.sin(angle) * radius, angle, 0.22F, 0.58F, fade * 0.5F);
        }
    }

    private static void renderRing(Matrix4f matrix, VertexConsumer consumer, float radius, float width, float y, float red, float green, float blue, float alpha) {
        for (int i = 0; i < RING_SEGMENTS; i++) {
            float a0 = Mth.TWO_PI * i / RING_SEGMENTS;
            float a1 = Mth.TWO_PI * (i + 1) / RING_SEGMENTS;
            float pulse0 = 0.82F + Mth.sin(a0 * 3.0F) * 0.18F;
            float pulse1 = 0.82F + Mth.sin(a1 * 3.0F) * 0.18F;
            float inner0 = radius - width * pulse0;
            float outer0 = radius + width * pulse0;
            float inner1 = radius - width * pulse1;
            float outer1 = radius + width * pulse1;
            vertex(matrix, consumer, Mth.cos(a0) * inner0, y, Mth.sin(a0) * inner0, red, green, blue, alpha * 0.55F);
            vertex(matrix, consumer, Mth.cos(a1) * inner1, y, Mth.sin(a1) * inner1, red, green, blue, alpha * 0.55F);
            vertex(matrix, consumer, Mth.cos(a1) * outer1, y, Mth.sin(a1) * outer1, red, green, blue, alpha);
            vertex(matrix, consumer, Mth.cos(a0) * outer0, y, Mth.sin(a0) * outer0, red, green, blue, alpha);
        }
    }

    private static void renderShard(Matrix4f matrix, VertexConsumer consumer, float x, float y, float z, float angle, float width, float length, float alpha) {
        float dx = Mth.cos(angle);
        float dz = Mth.sin(angle);
        float sx = -dz * width;
        float sz = dx * width;
        vertex(matrix, consumer, x - sx, y, z - sz, 1.0F, 0.05F, 0.04F, alpha * 0.15F);
        vertex(matrix, consumer, x + sx, y, z + sz, 1.0F, 0.05F, 0.04F, alpha * 0.15F);
        vertex(matrix, consumer, x + dx * length, y + length * 0.18F, z + dz * length, 1.0F, 0.68F, 0.62F, alpha);
        vertex(matrix, consumer, x + dx * length * 0.2F, y + length * 0.08F, z + dz * length * 0.2F, 1.0F, 0.0F, 0.0F, alpha * 0.55F);
    }

    private static void renderForwardSlash(Matrix4f matrix, VertexConsumer consumer, float offset, float z, float height, float fade) {
        float alpha = fade * 0.92F;
        vertex(matrix, consumer, -0.18F + offset, 0.08F, z, 1.0F, 0.0F, 0.0F, alpha * 0.12F);
        vertex(matrix, consumer, 0.18F + offset, 0.12F, z + 0.08F, 1.0F, 0.0F, 0.0F, alpha * 0.18F);
        vertex(matrix, consumer, 0.62F + offset, height, z + 1.45F, 1.0F, 0.78F, 0.68F, alpha);
        vertex(matrix, consumer, -0.58F + offset, height * 0.72F, z + 1.18F, 1.0F, 0.02F, 0.02F, alpha * 0.68F);
    }

    private static void vertex(Matrix4f matrix, VertexConsumer consumer, float x, float y, float z, float red, float green, float blue, float alpha) {
        consumer.vertex(matrix, x, y, z).color(red, green, blue, alpha).endVertex();
    }

    @Override
    public ResourceLocation getTextureLocation(MirrorReprisalEffectEntity entity) {
        return null;
    }
}
