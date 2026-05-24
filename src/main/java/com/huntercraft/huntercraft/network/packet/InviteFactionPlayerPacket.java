package com.huntercraft.huntercraft.network.packet;

import com.huntercraft.huntercraft.faction.FactionSavedData;
import com.huntercraft.huntercraft.util.HunterDataUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class InviteFactionPlayerPacket {
    private final String targetName;

    public InviteFactionPlayerPacket(String targetName) {
        this.targetName = targetName;
    }

    public static void encode(InviteFactionPlayerPacket packet, FriendlyByteBuf buffer) {
        buffer.writeUtf(packet.targetName);
    }

    public static InviteFactionPlayerPacket decode(FriendlyByteBuf buffer) {
        return new InviteFactionPlayerPacket(buffer.readUtf());
    }

    public static void handle(InviteFactionPlayerPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        ServerPlayer player = context.getSender();
        if (player == null) {
            context.setPacketHandled(true);
            return;
        }

        context.enqueueWork(() -> {
            ServerPlayer target = player.server.getPlayerList().getPlayerByName(packet.targetName);
            if (target == null) {
                player.sendSystemMessage(Component.literal("That player is not online."));
                HunterDataUtil.sync(player);
                return;
            }
            boolean invited = FactionSavedData.get(player.server).invitePlayer(player, target);
            if (!invited) {
                player.sendSystemMessage(Component.literal("Unable to invite that player."));
            } else {
                player.sendSystemMessage(Component.literal("Invited " + target.getScoreboardName() + " to your faction."));
                target.sendSystemMessage(Component.literal(player.getScoreboardName() + " invited you to join their faction."));
            }
            HunterDataUtil.sync(player);
            HunterDataUtil.sync(target);
        });

        context.setPacketHandled(true);
    }
}
