package com.huntercraft.huntercraft.client.render;

import com.huntercraft.huntercraft.entity.NenAuraEffectEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import org.joml.Matrix4f;

import java.util.UUID;

public class NenAuraEffectRenderer extends EntityRenderer<NenAuraEffectEntity> {
    private static final int RING_SEGMENTS = 40;
    private static final int WISP_SEGMENTS = 8;

    public NenAuraEffectRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
    }

    @Override
    public void render(NenAuraEffectEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        if (entity.getStyle() == NenAuraEffectEntity.STYLE_REN) {
            return;
        }

        float level = Mth.clamp(entity.getNenLevel() / 10.0F, 0.1F, 1.0F);
        float time = entity.tickCount + partialTick;
        int color = entity.getColor();
        float red = ((color >> 16) & 0xFF) / 255.0F;
        float green = ((color >> 8) & 0xFF) / 255.0F;
        float blue = (color & 0xFF) / 255.0F;

        poseStack.pushPose();
        Player owner = getOwner(entity);
        Minecraft minecraft = Minecraft.getInstance();
        if (owner == minecraft.player && minecraft.options.getCameraType().isFirstPerson()) {
            poseStack.popPose();
            return;
        }
        if (!NenVisibilityUtil.canLocalPlayerSeeNenVisuals(owner)) {
            poseStack.popPose();
            return;
        }
        translateToOwner(entity, owner, partialTick, poseStack);
        float auraYaw = owner != null ? Mth.rotLerp(partialTick, owner.yRotO, owner.getYRot()) : Mth.lerp(partialTick, entity.yRotO, entity.getYRot());
        poseStack.mulPose(Axis.YP.rotationDegrees(-auraYaw));
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lightning());
        Matrix4f matrix = poseStack.last().pose();
        if (entity.getStyle() == NenAuraEffectEntity.STYLE_KEN) {
            renderTenAura(matrix, consumer, time, level, red, green, blue, 0.54F);
        } else {
            renderTenAura(matrix, consumer, time, level, red, green, blue, 1.0F);
        }
        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    private static void renderTenAura(Matrix4f matrix, VertexConsumer consumer, float time, float level, float red, float green, float blue, float intensity) {
        float radius = (0.78F + level * 0.42F) * (0.92F + intensity * 0.08F);
        float height = (2.3F + level * 0.64F) * (0.82F + intensity * 0.18F);
        float alpha = (0.68F + level * 0.3F) * intensity;
        float pulse = 0.5F + 0.5F * Mth.sin(time * 0.22F);
        float glowRed = mix(red, 1.0F, 0.24F);
        float glowGreen = mix(green, 1.0F, 0.24F);
        float glowBlue = mix(blue, 1.0F, 0.24F);

        renderAuraShell(matrix, consumer, radius * (1.0F + pulse * 0.06F), height, glowRed, glowGreen, glowBlue, alpha * 0.58F, time, intensity);
        renderGroundRing(matrix, consumer, radius * (1.08F + pulse * 0.12F), (0.065F + level * 0.045F) * (0.82F + intensity * 0.18F), red, green, blue, alpha * 0.82F);

        int sparks = Math.max(3, Math.round((7 + level * 7.0F) * intensity));
        for (int i = 0; i < sparks; i++) {
            float angle = i * Mth.TWO_PI / (12.0F + level * 12.0F) - time * 0.08F;
            float y = 0.32F + (i % 7) * 0.25F;
            float r = radius * (0.48F + (i % 3) * 0.16F);
            renderSpark(matrix, consumer, Mth.sin(angle) * r, y, Mth.cos(angle) * r, (0.14F + level * 0.11F) * (0.82F + intensity * 0.18F), mix(red, 1.0F, 0.38F), mix(green, 1.0F, 0.38F), mix(blue, 1.0F, 0.38F), alpha * 0.34F);
        }
    }

    private static void renderAuraShell(Matrix4f matrix, VertexConsumer consumer, float radius, float height, float red, float green, float blue, float alpha, float time, float intensity) {
        int plumes = Math.max(14, Math.round(30.0F * intensity));
        for (int i = 0; i < plumes; i++) {
            float angle = i * Mth.TWO_PI / plumes + time * 0.02F;
            float wave = 0.1F * Mth.sin(time * 0.12F + i * 1.73F);
            float baseRadius = radius + wave;
            float x0 = Mth.sin(angle) * baseRadius;
            float z0 = Mth.cos(angle) * baseRadius;
            float inward = 0.58F + 0.08F * Mth.sin(time * 0.15F + i);
            float twist = angle + 0.22F * Mth.sin(time * 0.09F + i * 2.1F);
            float x1 = Mth.sin(twist) * baseRadius * inward;
            float z1 = Mth.cos(twist) * baseRadius * inward;
            float y1 = height * (0.7F + 0.26F * Mth.sin(time * 0.1F + i * 1.3F) * Mth.sin(time * 0.1F + i * 1.3F));
            float width = 0.12F + (i % 3) * 0.024F;
            renderWisp(matrix, consumer, x0, 0.08F, z0, x1, y1, z1, width, red, green, blue, alpha);
        }
    }

    private static Player getOwner(NenAuraEffectEntity entity) {
        UUID ownerUuid = entity.getOwnerUuid();
        return ownerUuid == null ? null : entity.level().getPlayerByUUID(ownerUuid);
    }

    private static void translateToOwner(NenAuraEffectEntity entity, Player owner, float partialTick, PoseStack poseStack) {
        if (owner == null) {
            return;
        }
        double entityX = Mth.lerp(partialTick, entity.xOld, entity.getX());
        double entityY = Mth.lerp(partialTick, entity.yOld, entity.getY());
        double entityZ = Mth.lerp(partialTick, entity.zOld, entity.getZ());
        double ownerX = Mth.lerp(partialTick, owner.xOld, owner.getX());
        double ownerY = Mth.lerp(partialTick, owner.yOld, owner.getY());
        double ownerZ = Mth.lerp(partialTick, owner.zOld, owner.getZ());
        poseStack.translate(ownerX - entityX, ownerY - entityY, ownerZ - entityZ);
    }

    private static void renderRenHands(Matrix4f matrix, VertexConsumer consumer, float time, float level, float red, float green, float blue) {
        float alpha = 0.62F + level * 0.32F;
        renderArmAura(matrix, consumer, -0.43F, time, level, red, green, blue, alpha, 0.0F);
        renderArmAura(matrix, consumer, 0.43F, time, level, red, green, blue, alpha, 2.4F);
    }

    private static void renderArmAura(Matrix4f matrix, VertexConsumer consumer, float side, float time, float level, float red, float green, float blue, float alpha, float offset) {
        float upperY = 1.12F;
        float handY = 0.62F;
        float armZ = -0.24F;
        renderHandAura(matrix, consumer, side, handY, armZ, time, level, red, green, blue, alpha, offset);
        renderWisp(matrix, consumer, side * 0.92F, upperY, armZ + 0.02F, side * 1.04F, handY - 0.12F, armZ - 0.04F, 0.08F + level * 0.04F, mix(red, 1.0F, 0.35F), mix(green, 1.0F, 0.35F), mix(blue, 1.0F, 0.35F), alpha * 0.72F);
        renderWisp(matrix, consumer, side * 0.74F, upperY - 0.08F, armZ + 0.04F, side * 0.98F, handY - 0.04F, armZ - 0.08F, 0.06F + level * 0.035F, red, green, blue, alpha * 0.58F);
        for (int i = 0; i < 6; i++) {
            float t = i / 5.0F;
            float y = Mth.lerp(t, upperY, handY);
            float x = side * (0.86F + 0.12F * Mth.sin(time * 0.22F + i + offset));
            float z = armZ + 0.05F * Mth.cos(time * 0.2F + i * 1.7F + offset);
            renderSpark(matrix, consumer, x, y, z, 0.055F + level * 0.035F, red, green, blue, alpha * 0.42F);
        }
    }

    private static void renderHandAura(Matrix4f matrix, VertexConsumer consumer, float cx, float cy, float cz, float time, float level, float red, float green, float blue, float alpha, float offset) {
        float radius = 0.26F + level * 0.15F;
        renderSpark(matrix, consumer, cx, cy, cz, 0.32F + level * 0.2F, 1.0F, 1.0F, 1.0F, alpha * 0.62F);
        for (int i = 0; i < 18 + Math.round(level * 14.0F); i++) {
            float angle = i * Mth.TWO_PI / (18.0F + level * 14.0F) + time * (0.18F + level * 0.08F) + offset;
            float rise = 0.24F + (i % 5) * 0.08F + level * 0.28F;
            float x0 = cx + Mth.sin(angle) * radius * 0.55F;
            float z0 = cz + Mth.cos(angle) * radius * 0.55F;
            float x1 = cx + Mth.sin(angle + 0.58F) * (radius + 0.24F + level * 0.14F);
            float z1 = cz + Mth.cos(angle + 0.58F) * (radius + 0.24F + level * 0.14F);
            float y0 = cy - 0.12F + Mth.sin(time * 0.11F + i) * 0.03F;
            float y1 = cy + rise;
            float core = i % 3 == 0 ? 0.55F : 0.0F;
            renderWisp(matrix, consumer, x0, y0, z0, x1, y1, z1, 0.065F + level * 0.055F, mix(red, 1.0F, core), mix(green, 1.0F, core), mix(blue, 1.0F, core), alpha * (0.48F + (i % 4) * 0.12F));
        }
        for (int i = 0; i < 8; i++) {
            float angle = i * Mth.HALF_PI + time * 0.24F + offset;
            renderSpark(matrix, consumer, cx + Mth.sin(angle) * radius, cy + 0.02F + i * 0.04F, cz + Mth.cos(angle) * radius, 0.08F + level * 0.06F, red, green, blue, alpha * 0.55F);
        }
    }

    private static void renderGroundRing(Matrix4f matrix, VertexConsumer consumer, float radius, float halfWidth, float red, float green, float blue, float alpha) {
        for (int i = 0; i < RING_SEGMENTS; i++) {
            float a0 = i * Mth.TWO_PI / RING_SEGMENTS;
            float a1 = (i + 1) * Mth.TWO_PI / RING_SEGMENTS;
            float r0 = radius - halfWidth;
            float r1 = radius + halfWidth;
            vertex(matrix, consumer, Mth.sin(a0) * r0, 0.035F, Mth.cos(a0) * r0, red, green, blue, alpha * 0.24F);
            vertex(matrix, consumer, Mth.sin(a1) * r0, 0.035F, Mth.cos(a1) * r0, red, green, blue, alpha * 0.24F);
            vertex(matrix, consumer, Mth.sin(a1) * r1, 0.035F, Mth.cos(a1) * r1, red, green, blue, alpha);
            vertex(matrix, consumer, Mth.sin(a0) * r1, 0.035F, Mth.cos(a0) * r1, red, green, blue, alpha);
        }
    }

    private static void renderWisp(Matrix4f matrix, VertexConsumer consumer, float x0, float y0, float z0, float x1, float y1, float z1, float halfWidth, float red, float green, float blue, float alpha) {
        for (int i = 0; i < WISP_SEGMENTS; i++) {
            float t0 = i / (float) WISP_SEGMENTS;
            float t1 = (i + 1) / (float) WISP_SEGMENTS;
            WispPoint p0 = wispPoint(x0, y0, z0, x1, y1, z1, t0);
            WispPoint p1 = wispPoint(x0, y0, z0, x1, y1, z1, t1);
            float w0 = halfWidth * taper(t0);
            float w1 = halfWidth * taper(t1);
            float a0 = alpha * (1.0F - t0) * taper(t0);
            float a1 = alpha * (1.0F - t1) * taper(t1);
            renderBillboardSegment(matrix, consumer, p0, p1, w0, w1, red, green, blue, a0, a1);
        }
    }

    private static WispPoint wispPoint(float x0, float y0, float z0, float x1, float y1, float z1, float t) {
        float curve = Mth.sin(t * Mth.PI);
        float x = Mth.lerp(t, x0, x1) + curve * 0.08F * Mth.sin((x0 + z1) * 4.7F + t * 3.0F);
        float y = Mth.lerp(t, y0, y1);
        float z = Mth.lerp(t, z0, z1) + curve * 0.08F * Mth.cos((z0 + x1) * 4.7F + t * 3.0F);
        return new WispPoint(x, y, z);
    }

    private static void renderBillboardSegment(Matrix4f matrix, VertexConsumer consumer, WispPoint p0, WispPoint p1, float w0, float w1, float red, float green, float blue, float a0, float a1) {
        float dx = p1.x() - p0.x();
        float dz = p1.z() - p0.z();
        float len = Mth.sqrt(dx * dx + dz * dz);
        float nx = len > 1.0E-4F ? -dz / len : 1.0F;
        float nz = len > 1.0E-4F ? dx / len : 0.0F;
        vertex(matrix, consumer, p0.x() - nx * w0, p0.y(), p0.z() - nz * w0, red, green, blue, a0 * 0.2F);
        vertex(matrix, consumer, p0.x() + nx * w0, p0.y(), p0.z() + nz * w0, red, green, blue, a0);
        vertex(matrix, consumer, p1.x() + nx * w1, p1.y(), p1.z() + nz * w1, red, green, blue, a1);
        vertex(matrix, consumer, p1.x() - nx * w1, p1.y(), p1.z() - nz * w1, red, green, blue, a1 * 0.2F);
    }

    private static void renderSpark(Matrix4f matrix, VertexConsumer consumer, float x, float y, float z, float size, float red, float green, float blue, float alpha) {
        vertex(matrix, consumer, x - size, y, z, red, green, blue, 0.0F);
        vertex(matrix, consumer, x, y + size, z, red, green, blue, alpha);
        vertex(matrix, consumer, x + size, y, z, red, green, blue, 0.0F);
        vertex(matrix, consumer, x, y - size, z, red, green, blue, alpha * 0.55F);
    }

    private static float taper(float t) {
        return Mth.clamp(Mth.sin(t * Mth.PI) * 1.25F, 0.0F, 1.0F);
    }

    private static float mix(float a, float b, float t) {
        return a + (b - a) * Mth.clamp(t, 0.0F, 1.0F);
    }

    private static void vertex(Matrix4f matrix, VertexConsumer consumer, float x, float y, float z, float red, float green, float blue, float alpha) {
        consumer.vertex(matrix, x, y, z).color(red, green, blue, alpha).endVertex();
    }

    @Override
    public ResourceLocation getTextureLocation(NenAuraEffectEntity entity) {
        return null;
    }

    private record WispPoint(float x, float y, float z) {
    }
}
