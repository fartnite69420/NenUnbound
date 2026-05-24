package com.huntercraft.huntercraft.network.packet;

import com.huntercraft.huntercraft.faction.FactionSavedData;
import com.huntercraft.huntercraft.util.HunterDataUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

public class DisbandFactionPacket {
    public static void encode(DisbandFactionPacket packet, FriendlyByteBuf buffer) {
    }

    public static DisbandFactionPacket decode(FriendlyByteBuf buffer) {
        return new DisbandFactionPacket();
    }

    public static void handle(DisbandFactionPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        ServerPlayer player = context.getSender();
        if (player == null) {
            context.setPacketHandled(true);
            return;
        }

        context.enqueueWork(() -> {
            FactionSavedData factionData = FactionSavedData.get(player.server);
            Set<UUID> affectedMembers = factionData.getFactionMemberIds(player.getUUID());
            boolean disbanded = factionData.disbandFaction(player);
            if (!disbanded) {
                player.sendSystemMessage(Component.literal("Only the faction leader can disband the faction."));
            } else {
                player.sendSystemMessage(Component.literal("Faction disbanded."));
            }
            for (UUID memberId : affectedMembers) {
                ServerPlayer onlineMember = player.server.getPlayerList().getPlayer(memberId);
                if (onlineMember != null) {
                    HunterDataUtil.sync(onlineMember);
                }
            }
            HunterDataUtil.sync(player);
        });

        context.setPacketHandled(true);
    }
}
