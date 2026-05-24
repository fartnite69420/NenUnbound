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

public class LeaveFactionPacket {
    public static void encode(LeaveFactionPacket packet, FriendlyByteBuf buffer) {
    }

    public static LeaveFactionPacket decode(FriendlyByteBuf buffer) {
        return new LeaveFactionPacket();
    }

    public static void handle(LeaveFactionPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        ServerPlayer player = context.getSender();
        if (player == null) {
            context.setPacketHandled(true);
            return;
        }

        context.enqueueWork(() -> {
            FactionSavedData factionData = FactionSavedData.get(player.server);
            Set<UUID> affectedMembers = factionData.getFactionMemberIds(player.getUUID());
            boolean left = factionData.leaveFaction(player);
            if (!left) {
                player.sendSystemMessage(Component.literal("Unable to leave this faction. Leaders must disband it instead."));
            } else {
                player.sendSystemMessage(Component.literal("You left the faction."));
            }
            HunterDataUtil.sync(player);
            for (UUID memberId : affectedMembers) {
                if (memberId.equals(player.getUUID())) {
                    continue;
                }
                ServerPlayer onlineMember = player.server.getPlayerList().getPlayer(memberId);
                if (onlineMember != null) {
                    HunterDataUtil.sync(onlineMember);
                }
            }
        });

        context.setPacketHandled(true);
    }
}
