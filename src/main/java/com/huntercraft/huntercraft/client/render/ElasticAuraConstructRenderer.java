package com.huntercraft.huntercraft.client.render;

import com.huntercraft.huntercraft.HunterCraftMod;
import com.huntercraft.huntercraft.abilities.bungeegum.ElasticAuraManager;
import com.huntercraft.huntercraft.client.model.ElasticAuraConstructModel;
import com.huntercraft.huntercraft.entity.ElasticAuraConstructEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class ElasticAuraConstructRenderer extends EntityRenderer<ElasticAuraConstructEntity> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(HunterCraftMod.MODID, "textures/effects/solid_white.png");
    private final ElasticAuraConstructModel<ElasticAuraConstructEntity> model;

    public ElasticAuraConstructRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new ElasticAuraConstructModel<>(context.bakeLayer(ElasticAuraConstructModel.LAYER_LOCATION));
        this.shadowRadius = 0.0F;
    }

    @Override
    public void render(ElasticAuraConstructEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        Player owner = entity.getOwnerPlayerEntity();
        if (entity.isHidden() || (!entity.isTrap() && !NenVisibilityUtil.canLocalPlayerSeeNenVisuals(owner))) {
            return;
        }
        float red = ((entity.getColor() >> 16) & 0xFF) / 255.0F;
        float green = ((entity.getColor() >> 8) & 0xFF) / 255.0F;
        float blue = (entity.getColor() & 0xFF) / 255.0F;
        if (entity.isStringVisual()) {
            renderStringVisual(entity, partialTick, poseStack, buffer, red, green, blue);
            super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
            return;
        }
        if (!entity.isTrap()) {
            // Draw anchor tether: from construct to tagged entity if one is held, otherwise to player's hand.
            Vec3 tetherTarget = null;
            String heldUuid = entity.getHeldTargetUuid();
            LivingEntity heldTarget = resolveLiving(heldUuid);
            if (heldTarget != null) {
                tetherTarget = heldTarget.getEyePosition();
            }
            if (tetherTarget == null) {
                if (owner != null) {
                    tetherTarget = ElasticAuraManager.getHandPosition(owner);
                }
            }
            if (tetherTarget != null) {
                poseStack.pushPose();
                Vec3 from = tetherTarget.subtract(entity.position().add(0.0D, 0.05D, 0.0D));
                VertexConsumer lineConsumer = buffer.getBuffer(RenderType.lines());
                renderElasticString(poseStack, lineConsumer, from, new Vec3(0.0D, 0.05D, 0.0D), entity.tickCount + partialTick, red, green, blue);
                poseStack.popPose();
            }
        }
        poseStack.pushPose();
        poseStack.translate(0.0D, entity.isTrap() ? -0.86D : -0.92D, 0.0D);
        float scale = entity.isTrap() ? 1.25F : 1.0F;
        poseStack.scale(scale, scale, scale);
        VertexConsumer consumer = buffer.getBuffer(RenderType.entityTranslucent(TEXTURE));
        this.model.setupAnim(entity, 0.0F, 0.0F, entity.tickCount + partialTick, 0.0F, 0.0F);
        this.model.renderToBuffer(poseStack, consumer, packedLight, 0, red, green, blue, entity.isTrap() ? 0.88F : 0.72F);
        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(ElasticAuraConstructEntity entity) {
        return TEXTURE;
    }

    private static void renderStringVisual(ElasticAuraConstructEntity entity, float partialTick, PoseStack poseStack, MultiBufferSource buffer, float red, float green, float blue) {
        String type = entity.getConstructType();
        Vec3 worldStart = null;
        Vec3 worldEnd = null;
        if (ElasticAuraConstructEntity.TYPE_BIND_STRING.equals(type)) {
            String[] uuids = entity.getHeldTargetUuid().split(";", 2);
            LivingEntity first = uuids.length > 0 ? resolveLiving(uuids[0]) : null;
            LivingEntity second = uuids.length > 1 ? resolveLiving(uuids[1]) : null;
            if (first != null && second != null) {
                worldStart = first.getEyePosition(partialTick);
                worldEnd = second.getEyePosition(partialTick);
            }
        } else {
            Player owner = entity.getOwnerPlayerEntity();
            LivingEntity target = resolveLiving(entity.getHeldTargetUuid());
            if (owner != null && target != null) {
                worldStart = ElasticAuraManager.getHandPosition(owner);
                worldEnd = target.getEyePosition(partialTick);
            }
        }
        if (worldStart == null || worldEnd == null) {
            return;
        }

        poseStack.pushPose();
        Vec3 origin = entity.position();
        VertexConsumer lineConsumer = buffer.getBuffer(RenderType.lines());
        renderElasticString(poseStack, lineConsumer, worldStart.subtract(origin), worldEnd.subtract(origin), entity.tickCount + partialTick, red, green, blue);
        poseStack.popPose();
    }

    private static LivingEntity resolveLiving(String uuidString) {
        if (uuidString == null || uuidString.isBlank() || net.minecraft.client.Minecraft.getInstance().level == null) {
            return null;
        }
        try {
            java.util.UUID uuid = java.util.UUID.fromString(uuidString);
            for (net.minecraft.world.entity.Entity entity : net.minecraft.client.Minecraft.getInstance().level.entitiesForRendering()) {
                if (entity.getUUID().equals(uuid) && entity instanceof LivingEntity living) {
                    return living;
                }
            }
        } catch (IllegalArgumentException ignored) {
        }
        return null;
    }

    private static void renderElasticString(PoseStack poseStack, VertexConsumer consumer, Vec3 start, Vec3 end, float time, float red, float green, float blue) {
        Vec3 delta = end.subtract(start);
        if (delta.lengthSqr() < 1.0E-5D) {
            return;
        }
        Vec3 side = new Vec3(-delta.z, 0.0D, delta.x);
        if (side.lengthSqr() < 1.0E-5D) {
            side = new Vec3(1.0D, 0.0D, 0.0D);
        }
        side = side.normalize();
        Vec3 up = delta.normalize().cross(side).normalize();
        for (int strand = 0; strand < 5; strand++) {
            double phase = time * 0.18D + strand * 1.31D;
            Vec3 offsetA = side.scale(Math.sin(phase) * 0.018D * strand).add(up.scale(Math.cos(phase) * 0.012D * strand));
            Vec3 previous = start.add(offsetA.scale(0.25D));
            for (int i = 1; i <= 16; i++) {
                double t = i / 16.0D;
                double taper = Math.sin(t * Math.PI);
                Vec3 wobble = side.scale(Math.sin((t * 7.0D) + phase) * 0.045D * taper)
                        .add(up.scale(Math.cos((t * 5.5D) + phase) * 0.025D * taper));
                Vec3 point = start.lerp(end, t).add(wobble).add(offsetA.scale(1.0D - t));
                float alpha = strand == 0 ? 1.0F : 0.72F;
                ElasticAuraRenderUtil.renderLine(poseStack, consumer,
                        (float) previous.x, (float) previous.y, (float) previous.z,
                        (float) point.x, (float) point.y, (float) point.z,
                        red, green, blue, alpha);
                previous = point;
            }
        }
    }
}
