package com.huntercraft.huntercraft.client.render;

import com.huntercraft.huntercraft.entity.SmokeCloneEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public class SmokeCloneRenderer extends HumanoidMobRenderer<SmokeCloneEntity, PlayerModel<SmokeCloneEntity>> {
    private final PlayerModel<SmokeCloneEntity> normalModel;
    private final PlayerModel<SmokeCloneEntity> slimModel;

    public SmokeCloneRenderer(EntityRendererProvider.Context context) {
        super(context, new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER), false), 0.5F);
        this.normalModel = this.getModel();
        this.slimModel = new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER_SLIM), true);
    }

    @Override
    public ResourceLocation getTextureLocation(SmokeCloneEntity entity) {
        UUID sourceUuid = entity.getSourceUuid();
        if (sourceUuid == null) {
            sourceUuid = entity.getOwnerUuid();
        }
        if (sourceUuid == null) {
            return DefaultPlayerSkin.getDefaultSkin(entity.getUUID());
        }
        PlayerInfo playerInfo = Minecraft.getInstance().getConnection() != null
                ? Minecraft.getInstance().getConnection().getPlayerInfo(sourceUuid)
                : null;
        return playerInfo != null ? playerInfo.getSkinLocation() : DefaultPlayerSkin.getDefaultSkin(sourceUuid);
    }

    @Override
    public void render(SmokeCloneEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        this.model = getModelFor(entity);
        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    protected void scale(SmokeCloneEntity entity, PoseStack poseStack, float partialTickTime) {
        poseStack.scale(1.0F, 1.0F, 1.0F);
    }

    public PlayerModel<SmokeCloneEntity> getModelFor(SmokeCloneEntity entity) {
        return usesSlimModel(entity) ? this.slimModel : this.normalModel;
    }

    private boolean usesSlimModel(SmokeCloneEntity entity) {
        UUID sourceUuid = entity.getSourceUuid();
        if (sourceUuid == null) {
            sourceUuid = entity.getOwnerUuid();
        }
        if (sourceUuid == null) {
            return false;
        }
        PlayerInfo playerInfo = Minecraft.getInstance().getConnection() != null
                ? Minecraft.getInstance().getConnection().getPlayerInfo(sourceUuid)
                : null;
        if (playerInfo != null) {
            return "slim".equals(playerInfo.getModelName());
        }
        return "slim".equals(DefaultPlayerSkin.getSkinModelName(sourceUuid));
    }
}
