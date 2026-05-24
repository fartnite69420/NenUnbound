package com.huntercraft.huntercraft.faction;

import com.huntercraft.huntercraft.data.HunterPlayerData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public final class FactionUtil {
    private FactionUtil() {
    }

    public static void populateClientView(ServerPlayer player, HunterPlayerData data) {
        FactionSavedData factionData = FactionSavedData.get(player.server);
        data.setPlayerDisplayName(player.getScoreboardName());
        data.setFactionName(factionData.getFactionName(player.getUUID()));
        data.setFactionOwnerName(factionData.getFactionOwnerName(player.server, player.getUUID()));
        data.setPendingFactionInviteName(factionData.getPendingInviteFaction(player.getUUID()));
        data.setPendingFactionInviterName(factionData.getPendingInviterName(player.getUUID()));
        data.setFactionMembers(factionData.getFactionMemberNames(player.server, player.getUUID()));
        data.setInvitablePlayers(factionData.getInvitablePlayerNames(player.server, player.getUUID()));
    }

    public static boolean areFactionMates(Player first, Player second) {
        if (first.level().isClientSide() || first.getServer() == null) {
            return false;
        }
        return FactionSavedData.get(first.getServer()).areFactionMates(first, second);
    }
}
