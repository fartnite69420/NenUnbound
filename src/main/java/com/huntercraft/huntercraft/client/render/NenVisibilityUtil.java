package com.huntercraft.huntercraft.client.render;

import com.huntercraft.huntercraft.data.HunterPlayerData;
import com.huntercraft.huntercraft.data.HunterPlayerDataProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;

public final class NenVisibilityUtil {
    private NenVisibilityUtil() {
    }

    public static boolean canLocalPlayerSeeNenVisuals(Player owner) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer viewer = minecraft.player;
        if (viewer == null) {
            return false;
        }
        if (owner != null && owner.getUUID().equals(viewer.getUUID())) {
            return true;
        }
        HunterPlayerData viewerData = viewer.getCapability(HunterPlayerDataProvider.CAPABILITY).orElse(null);
        return viewerData != null && viewerData.hasNen();
    }
}
