package com.huntercraft.huntercraft.client.render;

import com.huntercraft.huntercraft.HunterCraftMod;
import com.huntercraft.huntercraft.client.model.GreatStampPigModel;
import com.huntercraft.huntercraft.entity.GreatStampPigEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class GreatStampPigRenderer extends MobRenderer<GreatStampPigEntity, GreatStampPigModel<GreatStampPigEntity>> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(HunterCraftMod.MODID, "textures/entity/great_stamp_pig.png");

    public GreatStampPigRenderer(EntityRendererProvider.Context context) {
        super(context, new GreatStampPigModel<>(context.bakeLayer(GreatStampPigModel.LAYER_LOCATION)), 0.95F);
    }

    @Override
    public ResourceLocation getTextureLocation(GreatStampPigEntity entity) {
        return TEXTURE;
    }

    @Override
    protected void scale(GreatStampPigEntity entity, PoseStack poseStack, float partialTickTime) {
        poseStack.scale(2.0F, 2.0F, 2.0F);
        poseStack.translate(0.0F, 0.2F, 0.0F);
    }
}
