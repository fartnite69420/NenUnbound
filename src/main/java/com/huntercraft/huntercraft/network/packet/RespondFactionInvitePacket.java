package com.huntercraft.huntercraft.network.packet;

import com.huntercraft.huntercraft.faction.FactionSavedData;
import com.huntercraft.huntercraft.util.HunterDataUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class RespondFactionInvitePacket {
    private final boolean accept;

    public RespondFactionInvitePacket(boolean accept) {
        this.accept = accept;
    }

    public static void encode(RespondFactionInvitePacket packet, FriendlyByteBuf buffer) {
        buffer.writeBoolean(packet.accept);
    }

    public static RespondFactionInvitePacket decode(FriendlyByteBuf buffer) {
        return new RespondFactionInvitePacket(buffer.readBoolean());
    }

    public static void handle(RespondFactionInvitePacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        ServerPlayer player = context.getSender();
        if (player == null) {
            context.setPacketHandled(true);
            return;
        }

        context.enqueueWork(() -> {
            FactionSavedData factionData = FactionSavedData.get(player.server);
            boolean changed = packet.accept ? factionData.acceptInvite(player) : factionData.declineInvite(player);
            if (!changed) {
                player.sendSystemMessage(Component.literal("No pending faction invite was found."));
            } else if (packet.accept) {
                player.sendSystemMessage(Component.literal("You joined the faction."));
            } else {
                player.sendSystemMessage(Component.literal("Faction invite declined."));
            }
            HunterDataUtil.sync(player);
        });

        context.setPacketHandled(true);
    }
}
