package com.huntercraft.huntercraft.client.render;

import com.huntercraft.huntercraft.client.model.SharedChainProjectileModel;
import com.huntercraft.huntercraft.entity.SmokeyChainProjectileEntity;
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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class SmokeyChainProjectileRenderer extends EntityRenderer<SmokeyChainProjectileEntity> {
    private static final ResourceLocation SMOKEY_TEXTURE = new ResourceLocation("huntercraft", "textures/effects/solid_white.png");
    private static final ResourceLocation CHAIN_TEXTURE = new ResourceLocation("huntercraft", "textures/entity/chainprojectile_texture.png");
    private static final float LEGACY_LINK_SPACING = 0.22F;

    private final SharedChainProjectileModel<SmokeyChainProjectileEntity> sharedChainModel;

    public SmokeyChainProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
        this.sharedChainModel = new SharedChainProjectileModel<>(context.bakeLayer(SharedChainProjectileModel.LAYER_LOCATION));
    }

    @Override
    public void render(SmokeyChainProjectileEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        // The actual chain is rendered from the level overlay so it can always
        // start at the owner's right hand and extend link-by-link by distance.
    }

    @Override
    public ResourceLocation getTextureLocation(SmokeyChainProjectileEntity entity) {
        return CHAIN_TEXTURE;
    }

    private static Vec3 resolveChainStart(SmokeyChainProjectileEntity entity, float partialTick) {
        Player owner = entity.getOwnerPlayer();
        if (owner == null) {
            Vec3 origin = entity.getOrigin();
            return origin.lengthSqr() > 1.0E-5D ? origin : entity.position();
        }
        Vec3 launchDirection = entity.getDeltaMovement().lengthSqr() > 1.0E-5D
                ? entity.getDeltaMovement().normalize()
                : owner.getLookAngle();
        double bodyYaw = Math.toRadians(Mth.rotLerp(partialTick, owner.yBodyRotO, owner.yBodyRot));
        double rightX = -Math.cos(bodyYaw) * 0.36D;
        double rightZ = -Math.sin(bodyYaw) * 0.36D;
        Vec3 bodyPos = new Vec3(
                Mth.lerp(partialTick, owner.xOld, owner.getX()),
                Mth.lerp(partialTick, owner.yOld, owner.getY()),
                Mth.lerp(partialTick, owner.zOld, owner.getZ())
        );
        return bodyPos.add(rightX + (launchDirection.x * 0.22D), 1.05D + (owner.isCrouching() ? -0.16D : 0.0D), rightZ + (launchDirection.z * 0.22D));
    }

    private static void renderSharedChainBody(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, float length, SharedChainProjectileModel<SmokeyChainProjectileEntity> model) {
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(CHAIN_TEXTURE));
        int segments = Math.max(2, Mth.ceil(length / SharedChainProjectileModel.SEGMENT_SPACING) + 1);
        for (int i = 0; i < segments; i++) {
            float z = Math.min(length, i * SharedChainProjectileModel.SEGMENT_SPACING);
            poseStack.pushPose();
            poseStack.translate(0.0D, 0.0D, z);
            model.renderSegment(poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY, i);
            poseStack.popPose();
        }
    }

    private static void renderLegacyChainLinks(PoseStack poseStack, VertexConsumer consumer, int packedLight, float length) {
        int links = Math.max(2, Mth.ceil(length / LEGACY_LINK_SPACING) + 1);
        for (int i = 0; i < links; i++) {
            float z = Math.min(length, i * LEGACY_LINK_SPACING);
            renderCrossLink(poseStack, consumer, packedLight, z, 0.0F, 0.16F, 0.22F);
        }
    }

    private static void renderLegacyHead(PoseStack poseStack, VertexConsumer consumer, int packedLight, float length) {
        renderCrossLink(poseStack, consumer, packedLight, length, 0.0F, 0.34F, 0.34F);
    }

    private static void renderCrossLink(PoseStack poseStack, VertexConsumer consumer, int packedLight, float z, float x, float width, float height) {
        Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();
        Matrix3f normal = pose.normal();
        putQuad(consumer, matrix, normal, x - width, -height, z, x + width, height, z, packedLight);
        putQuad(consumer, matrix, normal, -0.02F, -height, z - width, 0.02F, height, z + width, packedLight);
    }

    private static void putQuad(VertexConsumer consumer, Matrix4f matrix, Matrix3f normal, float minX, float minY, float minZ, float maxX, float maxY, float maxZ, int packedLight) {
        consumer.vertex(matrix, minX, minY, minZ).color(255, 255, 255, 255).uv(0.0F, 1.0F).overlayCoords(0).uv2(packedLight).normal(normal, 0.0F, 1.0F, 0.0F).endVertex();
        consumer.vertex(matrix, maxX, minY, maxZ).color(255, 255, 255, 255).uv(1.0F, 1.0F).overlayCoords(0).uv2(packedLight).normal(normal, 0.0F, 1.0F, 0.0F).endVertex();
        consumer.vertex(matrix, maxX, maxY, maxZ).color(255, 255, 255, 255).uv(1.0F, 0.0F).overlayCoords(0).uv2(packedLight).normal(normal, 0.0F, 1.0F, 0.0F).endVertex();
        consumer.vertex(matrix, minX, maxY, minZ).color(255, 255, 255, 255).uv(0.0F, 0.0F).overlayCoords(0).uv2(packedLight).normal(normal, 0.0F, 1.0F, 0.0F).endVertex();
    }
}
