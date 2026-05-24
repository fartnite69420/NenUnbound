package com.huntercraft.huntercraft.network.packet;

import com.huntercraft.huntercraft.util.HunterDataUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record SetVowFactionPacket(String abilityId, String playerNames) {
    public static void encode(SetVowFactionPacket packet, FriendlyByteBuf buffer) {
        buffer.writeUtf(packet.abilityId);
        buffer.writeUtf(packet.playerNames);
    }

    public static SetVowFactionPacket decode(FriendlyByteBuf buffer) {
        return new SetVowFactionPacket(buffer.readUtf(64), buffer.readUtf(64));
    }

    public static void handle(SetVowFactionPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) {
                return;
            }
            HunterDataUtil.getOptional(player).ifPresent(data -> {
                if ("chain_hatsu".equals(packet.abilityId)) {
                    data.setAbilityVowFaction("dowsing_chain", packet.playerNames);
                    data.setAbilityVowFaction("chain_jail", packet.playerNames);
                    data.setAbilityVowFaction("steal_chain", packet.playerNames);
                    data.setAbilityVowFaction("judgment_chain", packet.playerNames);
                } else {
                    data.setAbilityVowFaction(packet.abilityId, packet.playerNames);
                }
                HunterDataUtil.sync(player);
            });
        });
        context.setPacketHandled(true);
    }
}
