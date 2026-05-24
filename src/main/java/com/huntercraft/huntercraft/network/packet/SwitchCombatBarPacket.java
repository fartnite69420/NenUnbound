package com.huntercraft.huntercraft.network.packet;

import com.huntercraft.huntercraft.data.HunterPlayerData;
import com.huntercraft.huntercraft.util.HunterDataUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SwitchCombatBarPacket {
    private final int barIndex;

    public SwitchCombatBarPacket(int barIndex) {
        this.barIndex = barIndex;
    }

    public static void encode(SwitchCombatBarPacket packet, FriendlyByteBuf buffer) {
        buffer.writeInt(packet.barIndex);
    }

    public static SwitchCombatBarPacket decode(FriendlyByteBuf buffer) {
        return new SwitchCombatBarPacket(buffer.readInt());
    }

    public static void handle(SwitchCombatBarPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        ServerPlayer player = context.getSender();
        if (player == null) {
            context.setPacketHandled(true);
            return;
        }

        context.enqueueWork(() -> {
            HunterPlayerData data = HunterDataUtil.get(player);
            data.setActiveCombatBar(packet.barIndex);
            HunterDataUtil.sync(player);
        });
        context.setPacketHandled(true);
    }
}
