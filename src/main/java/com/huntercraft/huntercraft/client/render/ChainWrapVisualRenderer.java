package com.huntercraft.huntercraft.client.render;

import com.huntercraft.huntercraft.client.model.SharedChainProjectileModel;
import com.huntercraft.huntercraft.entity.ChainWrapVisualEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class ChainWrapVisualRenderer extends EntityRenderer<ChainWrapVisualEntity> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("huntercraft", "textures/entity/chainprojectile_texture.png");
    private final SharedChainProjectileModel<?> model;

    public ChainWrapVisualRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
        this.model = new SharedChainProjectileModel<>(context.bakeLayer(SharedChainProjectileModel.LAYER_LOCATION));
    }

    @Override
    public void render(ChainWrapVisualEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        LivingEntity target = entity.getTarget(entity.level());
        Vec3 center = target != null
                ? new Vec3(Mth.lerp(partialTick, target.xOld, target.getX()), Mth.lerp(partialTick, target.yOld, target.getY()) + target.getBbHeight() * 0.5D, Mth.lerp(partialTick, target.zOld, target.getZ()))
                : entity.getPosition(partialTick);
        float width = target != null ? target.getBbWidth() : entity.getTargetWidth();
        float height = target != null ? target.getBbHeight() : entity.getTargetHeight();
        float yaw = target != null ? Mth.rotLerp(partialTick, target.yBodyRotO, target.yBodyRot) : entity.getYRot();
        float radius = Math.max(0.54F, width * 0.96F);
        float wrapHeight = Math.max(1.05F, height * 0.78F);
        float linkScale = Mth.clamp(Math.max(width, height) * 0.32F, 0.72F, 1.18F);
        float time = entity.tickCount + partialTick;
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));

        Player owner = entity.getOwnerPlayer(entity.level());
        if (owner != null && target != null) {
            renderTether(entity, owner, target, partialTick, poseStack, consumer, time);
        }

        poseStack.pushPose();
        poseStack.translate(center.x - this.entityRenderDispatcher.camera.getPosition().x, center.y - this.entityRenderDispatcher.camera.getPosition().y, center.z - this.entityRenderDispatcher.camera.getPosition().z);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - yaw));
        renderBand(poseStack, consumer, radius, wrapHeight, -28.0F, 0.0F, time, linkScale);
        renderBand(poseStack, consumer, radius * 1.04F, wrapHeight, 28.0F, Mth.PI, time, linkScale);
        renderBand(poseStack, consumer, radius * 1.02F, wrapHeight * 0.48F, 0.0F, time * 0.1F, time, linkScale * 0.9F);
        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(ChainWrapVisualEntity entity) {
        return TEXTURE;
    }

    @Override
    public boolean shouldRender(ChainWrapVisualEntity entity, Frustum frustum, double cameraX, double cameraY, double cameraZ) {
        return entity.distanceToSqr(cameraX, cameraY, cameraZ) < 4096.0D;
    }

    private void renderBand(PoseStack poseStack, VertexConsumer consumer, float radius, float height, float tiltDegrees, float offset, float time, float linkScale) {
        int links = Math.max(18, Mth.ceil((radius * Mth.TWO_PI) / Math.max(0.18F, SharedChainProjectileModel.SEGMENT_SPACING * linkScale)));
        for (int i = 0; i < links; i++) {
            float t = i / (float) links;
            float angle = t * Mth.TWO_PI + offset;
            float y = Mth.lerp(t, height * 0.5F, -height * 0.5F);
            if (tiltDegrees == 0.0F) {
                y = -height * 0.14F + Mth.sin(angle * 2.0F) * 0.08F;
            }
            poseStack.pushPose();
            poseStack.translate(Mth.sin(angle) * radius, y, Mth.cos(angle) * radius);
            poseStack.mulPose(Axis.YP.rotation(angle + Mth.HALF_PI));
            poseStack.mulPose(Axis.ZP.rotationDegrees(tiltDegrees == 0.0F ? 90.0F : 90.0F + tiltDegrees));
            poseStack.scale(linkScale, linkScale, linkScale);
            this.model.renderSegment(poseStack, consumer, 15728880, OverlayTexture.NO_OVERLAY, i);
            poseStack.popPose();
        }
    }

    private void renderTether(ChainWrapVisualEntity visual, Player owner, LivingEntity target, float partialTick, PoseStack poseStack, VertexConsumer consumer, float time) {
        Vec3 start = getRightHandAnchor(owner, partialTick);
        Vec3 end = new Vec3(
                Mth.lerp(partialTick, target.xOld, target.getX()),
                Mth.lerp(partialTick, target.yOld, target.getY()) + (target.getBbHeight() * 0.55D),
                Mth.lerp(partialTick, target.zOld, target.getZ())
        );
        Vec3 camera = this.entityRenderDispatcher.camera.getPosition();
        Vec3 delta = end.subtract(start);
        float length = (float) delta.length();
        if (length <= 0.05F) {
            return;
        }
        float scale = 0.95F;
        poseStack.pushPose();
        poseStack.translate(start.x - camera.x, start.y - camera.y, start.z - camera.z);
        float yaw = (float) Math.atan2(delta.x, delta.z);
        float pitch = (float) Math.atan2(delta.y, Math.sqrt((delta.x * delta.x) + (delta.z * delta.z)));
        poseStack.mulPose(Axis.YP.rotation(yaw));
        poseStack.mulPose(Axis.XP.rotation(-pitch));
        int links = Math.max(2, Mth.ceil(length / (SharedChainProjectileModel.SEGMENT_SPACING * scale)) + 1);
        for (int i = 0; i < links; i++) {
            float z = Math.min(length, i * SharedChainProjectileModel.SEGMENT_SPACING * scale);
            poseStack.pushPose();
            poseStack.translate(0.0F, 0.0F, z);
            poseStack.scale(scale, scale, scale);
            this.model.renderSegment(poseStack, consumer, 15728880, net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY, i);
            poseStack.popPose();
        }
        poseStack.popPose();
    }

    private static Vec3 getRightHandAnchor(Player player, float partialTick) {
        double x = Mth.lerp(partialTick, player.xOld, player.getX());
        double y = Mth.lerp(partialTick, player.yOld, player.getY());
        double z = Mth.lerp(partialTick, player.zOld, player.getZ());
        float yaw = Mth.rotLerp(partialTick, player.yBodyRotO, player.yBodyRot);
        double radians = Math.toRadians(yaw);
        double rightX = -Math.cos(radians) * 0.36D;
        double rightZ = -Math.sin(radians) * 0.36D;
        Vec3 look = player.getLookAngle();
        return new Vec3(x + rightX + look.x * 0.22D, y + 1.05D + (player.isCrouching() ? -0.16D : 0.0D), z + rightZ + look.z * 0.22D);
    }
}
