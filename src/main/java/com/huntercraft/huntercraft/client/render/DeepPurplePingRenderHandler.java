package com.huntercraft.huntercraft.client.render;

import com.huntercraft.huntercraft.HunterCraftMod;
import com.huntercraft.huntercraft.data.HunterPlayerData;
import com.huntercraft.huntercraft.data.HunterPlayerDataProvider;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;

@Mod.EventBusSubscriber(modid = HunterCraftMod.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class DeepPurplePingRenderHandler {
    private DeepPurplePingRenderHandler() {
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        LocalPlayer player = minecraft.player;
        if (level == null || player == null) {
            return;
        }
        HunterPlayerData data = player.getCapability(HunterPlayerDataProvider.CAPABILITY).orElse(null);
        if (data == null || data.getDeepPurpleSpottedTargetPos() == null || data.getDeepPurpleSpottedTargetTicks() <= 0) {
            return;
        }

        Vec3 marker = Vec3.atBottomCenterOf(data.getDeepPurpleSpottedTargetPos()).add(0.0D, 2.7D, 0.0D);
        Vec3 camera = minecraft.gameRenderer.getMainCamera().getPosition();
        double x = marker.x - camera.x;
        double y = marker.y - camera.y;
        double z = marker.z - camera.z;
        if ((x * x) + (y * y) + (z * z) > (220.0D * 220.0D)) {
            return;
        }

        PoseStack poseStack = event.getPoseStack();
        Font font = minecraft.font;
        MultiBufferSource.BufferSource bufferSource = minecraft.renderBuffers().bufferSource();
        String title = "SPOTTED";
        int textWidth = font.width(title);

        poseStack.pushPose();
        poseStack.translate(x, y, z);
        poseStack.mulPose(minecraft.getEntityRenderDispatcher().cameraOrientation());
        poseStack.scale(-0.028F, -0.028F, 0.028F);
        Matrix4f matrix = poseStack.last().pose();
        int background = (int) (minecraft.options.getBackgroundOpacity(0.35F) * 255.0F) << 24;
        font.drawInBatch(title, -textWidth / 2.0F, 0.0F, 0xFFE0F2FF, false, matrix, bufferSource, Font.DisplayMode.NORMAL, background, 15728880);
        font.drawInBatch("SMOKE PING", -font.width("SMOKE PING") / 2.0F, 11.0F, 0xFFB9C9D6, false, matrix, bufferSource, Font.DisplayMode.NORMAL, background, 15728880);
        poseStack.popPose();
        bufferSource.endBatch();
    }

    @SubscribeEvent
    public static void onRenderOverlay(RenderGuiOverlayEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null) {
            return;
        }
        HunterPlayerData data = player.getCapability(HunterPlayerDataProvider.CAPABILITY).orElse(null);
        if (data == null || data.getDeepPurpleSpottedTargetPos() == null || data.getDeepPurpleSpottedTargetTicks() <= 0) {
            return;
        }

        Vec3 marker = Vec3.atBottomCenterOf(data.getDeepPurpleSpottedTargetPos()).add(0.0D, 2.0D, 0.0D);
        Vec3 toMarker = marker.subtract(player.getEyePosition());
        double distance = toMarker.length();
        if (distance <= 0.001D) {
            return;
        }
        double alignment = toMarker.normalize().dot(player.getLookAngle().normalize());
        if (alignment < 0.9925D) {
            return;
        }

        int screenWidth = event.getWindow().getGuiScaledWidth();
        int screenHeight = event.getWindow().getGuiScaledHeight();
        String text = String.format(java.util.Locale.ROOT, "Smoke Ping: %.1fm", distance);
        event.getGuiGraphics().drawCenteredString(minecraft.font, text, screenWidth / 2, (screenHeight / 2) + 18, 0xFFE7F7FF);
    }
}
