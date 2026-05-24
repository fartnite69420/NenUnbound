package com.huntercraft.huntercraft.client.render;

import com.huntercraft.huntercraft.entity.DowsingChainEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class DowsingChainRenderer extends GeoEntityRenderer<DowsingChainEntity> {
    public DowsingChainRenderer(EntityRendererProvider.Context context) {
        super(context, new DowsingChainModel());
        this.shadowRadius = 0.0F;
    }

    @Override
    public void render(DowsingChainEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        if (!NenVisibilityUtil.canLocalPlayerSeeNenVisuals(entity.getOwner())) {
            return;
        }
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    @Override
    public RenderType getRenderType(DowsingChainEntity animatable, ResourceLocation texture,
            net.minecraft.client.renderer.MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityCutoutNoCull(texture);
    }
}
