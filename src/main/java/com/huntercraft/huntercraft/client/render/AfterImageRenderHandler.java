package com.huntercraft.huntercraft.client.render;

import com.huntercraft.huntercraft.HunterCraftMod;
import com.huntercraft.huntercraft.client.AfterImageStore;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = HunterCraftMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class AfterImageRenderHandler {
    private AfterImageRenderHandler() {
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            AfterImageStore.INSTANCE.clear();
            return;
        }
        AfterImageStore.INSTANCE.tick();
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level == null || minecraft.player == null || AfterImageStore.INSTANCE.getSnapshots().isEmpty()) {
            return;
        }

        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource bufferSource = minecraft.renderBuffers().bufferSource();
        Vec3 camera = minecraft.gameRenderer.getMainCamera().getPosition();

        for (AfterImageStore.Snapshot snapshot : AfterImageStore.INSTANCE.getSnapshots()) {
            float alpha = snapshot.getAlpha();
            if (alpha <= 0.01F) {
                continue;
            }

            if (!(level.getPlayerByUUID(snapshot.playerUuid) instanceof AbstractClientPlayer player)) {
                continue;
            }

            EntityRenderer<? super AbstractClientPlayer> rawRenderer = minecraft.getEntityRenderDispatcher().getRenderer(player);
            if (!(rawRenderer instanceof PlayerRenderer renderer)) {
                continue;
            }

            PlayerModel<AbstractClientPlayer> model = renderer.getModel();
            poseStack.pushPose();
            poseStack.translate(snapshot.x - camera.x, snapshot.y - camera.y, snapshot.z - camera.z);
            poseStack.scale(-1.0F, -1.0F, 1.0F);
            poseStack.translate(0.0D, -1.501D, 0.0D);
            poseStack.mulPose(Axis.YP.rotationDegrees(snapshot.yaw));

            model.young = false;
            model.crouching = false;
            model.riding = false;
            model.attackTime = 0.0F;
            model.rightArmPose = HumanoidModel.ArmPose.EMPTY;
            model.leftArmPose = HumanoidModel.ArmPose.EMPTY;

            model.prepareMobModel(player, 0.0F, 0.0F, event.getPartialTick());
            model.setupAnim(player, 0.0F, 0.0F, 0.0F, snapshot.yaw, snapshot.pitch);

            VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityTranslucent(player.getSkinTextureLocation()));
            model.renderToBuffer(poseStack, consumer, 15728880, OverlayTexture.NO_OVERLAY, 0.70F, 0.86F, 1.0F, alpha * 0.63F);
            poseStack.popPose();
        }

        bufferSource.endBatch();
    }
}
