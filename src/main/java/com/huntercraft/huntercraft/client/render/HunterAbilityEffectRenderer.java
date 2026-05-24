package com.huntercraft.huntercraft.client.render;

import com.huntercraft.huntercraft.entity.HunterAbilityEffectEntity;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class HunterAbilityEffectRenderer extends GeoEntityRenderer<HunterAbilityEffectEntity> {
    public HunterAbilityEffectRenderer(EntityRendererProvider.Context context) {
        super(context, new HunterAbilityEffectModel());
        this.shadowRadius = 0.0F;
    }

    @Override
    public RenderType getRenderType(HunterAbilityEffectEntity animatable, ResourceLocation texture, net.minecraft.client.renderer.MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityCutoutNoCull(texture);
    }
}
