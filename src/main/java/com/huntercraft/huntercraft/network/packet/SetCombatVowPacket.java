package com.huntercraft.huntercraft.network.packet;

import com.huntercraft.huntercraft.util.HunterDataUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record SetCombatVowPacket(String vowType, int percent) {
    public static void encode(SetCombatVowPacket packet, FriendlyByteBuf buffer) {
        buffer.writeUtf(packet.vowType);
        buffer.writeInt(packet.percent);
    }

    public static SetCombatVowPacket decode(FriendlyByteBuf buffer) {
        return new SetCombatVowPacket(buffer.readUtf(16), buffer.readInt());
    }

    public static void handle(SetCombatVowPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) {
                return;
            }
            HunterDataUtil.getOptional(player).ifPresent(data -> {
                data.setCombatVow(packet.vowType, packet.percent);
                HunterDataUtil.sync(player);
            });
        });
        context.setPacketHandled(true);
    }
}
