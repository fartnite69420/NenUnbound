package com.huntercraft.huntercraft.client.render;

import com.huntercraft.huntercraft.abilities.bungeegum.ElasticAuraManager;
import com.huntercraft.huntercraft.entity.ElasticAuraProjectileEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class ElasticAuraProjectileRenderer extends EntityRenderer<ElasticAuraProjectileEntity> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("huntercraft", "textures/effects/solid_white.png");

    public ElasticAuraProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
    }

    @Override
    public void render(ElasticAuraProjectileEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        Player owner = entity.getOwnerPlayerEntity();
        if (entity.isHidden() || !NenVisibilityUtil.canLocalPlayerSeeNenVisuals(owner)) {
            return;
        }
        float red = ((entity.getColor() >> 16) & 0xFF) / 255.0F;
        float green = ((entity.getColor() >> 8) & 0xFF) / 255.0F;
        float blue = (entity.getColor() & 0xFF) / 255.0F;
        if (owner != null) {
            poseStack.pushPose();
            Vec3 hand = ElasticAuraManager.getHandPosition(owner);
            // If the projectile has tagged an entity, draw the string to that entity
            String taggedUuid = entity.getTaggedEntityUuid();
            Vec3 stringEnd = null;
            if (!taggedUuid.isBlank() && net.minecraft.client.Minecraft.getInstance().level != null) {
                try {
                    java.util.UUID uuid = java.util.UUID.fromString(taggedUuid);
                    for (net.minecraft.world.entity.Entity e : net.minecraft.client.Minecraft.getInstance().level.entitiesForRendering()) {
                        if (e.getUUID().equals(uuid) && e instanceof net.minecraft.world.entity.LivingEntity living) {
                            stringEnd = living.getEyePosition();
                            break;
                        }
                    }
                } catch (IllegalArgumentException ignored) {
                }
            }
            if (stringEnd == null) {
                // In-flight: draw from hand to projectile
                stringEnd = entity.position();
            }
            Vec3 from = hand.subtract(stringEnd);
            VertexConsumer lineConsumer = buffer.getBuffer(RenderType.lines());
            ElasticAuraRenderUtil.renderLine(poseStack, lineConsumer, (float) from.x, (float) from.y, (float) from.z, 0.0F, 0.0F, 0.0F, red, green, blue, 0.95F);
            poseStack.popPose();
        }
        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(ElasticAuraProjectileEntity entity) {
        return TEXTURE;
    }
}
