package com.huntercraft.huntercraft.api.blacklist;

import com.huntercraft.huntercraft.HunterCraftMod;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

public final class HunterBlacklistHandler {
    private HunterBlacklistHandler() {
    }

    public static void handleBlacklist(ServerPlayer player) {
        UUID uuid = player.getUUID();
        if (!HunterBlacklistManager.isBlacklisted(uuid)) {
            return;
        }

        String reason = HunterBlacklistManager.getBlacklistReason(uuid);
        String message = String.format("§c[%s] You have been kicked from this server.%n§cReason: %s", HunterCraftMod.MODID, reason);
        player.connection.disconnect(Component.literal(message));
    }
}
