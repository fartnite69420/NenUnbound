package com.huntercraft.huntercraft.client.render;

import com.huntercraft.huntercraft.HunterCraftMod;
import com.huntercraft.huntercraft.entity.BanditEntity;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.resources.ResourceLocation;

public class BanditRenderer extends HumanoidMobRenderer<BanditEntity, PlayerModel<BanditEntity>> {
    private static final ResourceLocation[] SKINS = new ResourceLocation[] {
            new ResourceLocation(HunterCraftMod.MODID, "textures/entity/bandit_1.png"),
            new ResourceLocation(HunterCraftMod.MODID, "textures/entity/bandit_2.png"),
            new ResourceLocation(HunterCraftMod.MODID, "textures/entity/bandit_3.png"),
            new ResourceLocation(HunterCraftMod.MODID, "textures/entity/bandit_4.png")
    };
    private final PlayerModel<BanditEntity> normalModel;
    private final PlayerModel<BanditEntity> slimModel;

    public BanditRenderer(EntityRendererProvider.Context context) {
        super(context, new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER), false), 0.5F);
        this.normalModel = this.getModel();
        this.slimModel = new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER_SLIM), true);
    }

    @Override
    public ResourceLocation getTextureLocation(BanditEntity entity) {
        return SKINS[entity.getSkinVariant()];
    }

    @Override
    public void render(BanditEntity entity, float entityYaw, float partialTick, PoseStack poseStack, net.minecraft.client.renderer.MultiBufferSource buffer, int packedLight) {
        this.model = entity.usesSlimModel() ? this.slimModel : this.normalModel;
        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
    }
}
