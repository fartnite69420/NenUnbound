package com.huntercraft.huntercraft.client.render;

import com.huntercraft.huntercraft.HunterCraftMod;
import com.huntercraft.huntercraft.entity.BungeeGumReflectEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class BungeeGumReflectRenderer extends EntityRenderer<BungeeGumReflectEntity> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(HunterCraftMod.MODID, "textures/effects/solid_white.png");

    public BungeeGumReflectRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
    }

    @Override
    public void render(BungeeGumReflectEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        Entity owner = entity.getOwner();
        if (entity.isHidden() || (owner instanceof Player player && !NenVisibilityUtil.canLocalPlayerSeeNenVisuals(player))) {
            return;
        }
        float red = ((entity.getColor() >> 16) & 0xFF) / 255.0F;
        float green = ((entity.getColor() >> 8) & 0xFF) / 255.0F;
        float blue = (entity.getColor() & 0xFF) / 255.0F;
        float time = entity.tickCount + partialTick;

        poseStack.pushPose();
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(180.0F - entity.getYRot()));
        VertexConsumer sheet = buffer.getBuffer(RenderType.entityTranslucent(TEXTURE));
        renderGumVeil(poseStack, sheet, packedLight, time, red, green, blue);

        VertexConsumer lines = buffer.getBuffer(RenderType.lines());
        renderElasticArcs(poseStack, lines, time, red, green, blue);
        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    private static void renderGumVeil(PoseStack poseStack, VertexConsumer consumer, int packedLight, float time, float red, float green, float blue) {
        Matrix4f matrix = poseStack.last().pose();
        Matrix3f normal = poseStack.last().normal();
        int strips = 8;
        for (int i = 0; i < strips; i++) {
            float t0 = i / (float) strips;
            float t1 = (i + 1) / (float) strips;
            float y0 = t0 * 2.35F;
            float y1 = t1 * 2.35F;
            float width0 = widthAt(t0, time);
            float width1 = widthAt(t1, time);
            float sideBias0 = Mth.sin(time * 0.24F + i * 0.8F) * 0.05F;
            float sideBias1 = Mth.sin(time * 0.24F + (i + 1) * 0.8F) * 0.05F;
            float alpha = 0.72F - t0 * 0.18F;
            quad(matrix, normal, consumer,
                    -width0 + sideBias0, y0, -0.05F,
                    width0 + sideBias0, y0, -0.05F,
                    width1 + sideBias1, y1, -0.08F,
                    -width1 + sideBias1, y1, -0.08F,
                    red, green, blue, alpha, packedLight);
        }

        for (int i = 0; i < 5; i++) {
            float x = -0.45F + i * 0.22F + Mth.sin(time * 0.18F + i) * 0.04F;
            float y = 0.3F + i * 0.31F;
            float w = 0.18F + Mth.sin(time * 0.22F + i * 0.7F) * 0.035F;
            quad(matrix, normal, consumer,
                    x - w, y - 0.08F, -0.09F,
                    x + w, y - 0.02F, -0.09F,
                    x + w * 0.55F, y + 0.16F, -0.11F,
                    x - w * 0.5F, y + 0.12F, -0.11F,
                    red, green, blue, 0.82F, packedLight);
        }
    }

    private static float widthAt(float t, float time) {
        float body = 0.42F + Mth.sin(t * Mth.PI) * 0.22F;
        float ragged = Mth.sin(time * 0.18F + t * 17.0F) * 0.06F;
        return Math.max(0.16F, body + ragged);
    }

    private static void renderElasticArcs(PoseStack poseStack, VertexConsumer consumer, float time, float red, float green, float blue) {
        for (int arc = 0; arc < 3; arc++) {
            float radius = 0.85F + arc * 0.17F;
            float start = -0.92F + arc * 0.1F;
            float end = 0.92F - arc * 0.08F;
            float prevX = 0.0F;
            float prevY = 0.0F;
            float prevZ = 0.0F;
            for (int i = 0; i <= 18; i++) {
                float p = i / 18.0F;
                float angle = Mth.lerp(p, start, end);
                float x = Mth.sin(angle) * radius;
                float y = 1.05F + Mth.cos(angle) * (1.1F + arc * 0.08F);
                float z = -0.18F - arc * 0.035F + Mth.sin(time * 0.08F + p * 5.0F) * 0.025F;
                if (i > 0) {
                    ElasticAuraRenderUtil.renderLine(poseStack, consumer, prevX, prevY, prevZ, x, y, z, red, green, blue, 0.82F);
                }
                prevX = x;
                prevY = y;
                prevZ = z;
            }
        }
    }

    private static void quad(Matrix4f matrix, Matrix3f normal, VertexConsumer consumer,
                             float x1, float y1, float z1, float x2, float y2, float z2,
                             float x3, float y3, float z3, float x4, float y4, float z4,
                             float red, float green, float blue, float alpha, int packedLight) {
        vertex(matrix, normal, consumer, x1, y1, z1, 0.0F, 0.0F, red, green, blue, alpha, packedLight);
        vertex(matrix, normal, consumer, x2, y2, z2, 1.0F, 0.0F, red, green, blue, alpha, packedLight);
        vertex(matrix, normal, consumer, x3, y3, z3, 1.0F, 1.0F, red, green, blue, alpha, packedLight);
        vertex(matrix, normal, consumer, x4, y4, z4, 0.0F, 1.0F, red, green, blue, alpha, packedLight);
    }

    private static void vertex(Matrix4f matrix, Matrix3f normal, VertexConsumer consumer, float x, float y, float z, float u, float v,
                               float red, float green, float blue, float alpha, int packedLight) {
        consumer.vertex(matrix, x, y, z)
                .color(red, green, blue, alpha)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(normal, 0.0F, 1.0F, 0.0F)
                .endVertex();
    }

    @Override
    public ResourceLocation getTextureLocation(BungeeGumReflectEntity entity) {
        return TEXTURE;
    }
}
