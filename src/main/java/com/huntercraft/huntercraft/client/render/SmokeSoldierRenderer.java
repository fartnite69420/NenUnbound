package com.huntercraft.huntercraft.client.render;

import com.huntercraft.huntercraft.HunterCraftMod;
import com.huntercraft.huntercraft.entity.SmokeSoldierEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;

public class SmokeSoldierRenderer extends HumanoidMobRenderer<SmokeSoldierEntity, PlayerModel<SmokeSoldierEntity>> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(HunterCraftMod.MODID, "textures/entity/smoke_soldier.png");

    public SmokeSoldierRenderer(EntityRendererProvider.Context context) {
        super(context, new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER), false), 0.35F);
    }

    @Override
    public ResourceLocation getTextureLocation(SmokeSoldierEntity entity) {
        return TEXTURE;
    }

    @Override
    protected void scale(SmokeSoldierEntity entity, PoseStack poseStack, float partialTickTime) {
        poseStack.scale(0.9F, 0.9F, 0.9F);
    }

    public PlayerModel<SmokeSoldierEntity> getSmokeSoldierModel() {
        return this.getModel();
    }
}
