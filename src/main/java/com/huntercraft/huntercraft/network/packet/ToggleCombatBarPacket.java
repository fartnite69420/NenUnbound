package com.huntercraft.huntercraft.network.packet;

import com.huntercraft.huntercraft.data.HunterPlayerData;
import com.huntercraft.huntercraft.util.HunterDataUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ToggleCombatBarPacket {
    private final boolean visible;

    public ToggleCombatBarPacket(boolean visible) {
        this.visible = visible;
    }

    public static void encode(ToggleCombatBarPacket packet, FriendlyByteBuf buffer) {
        buffer.writeBoolean(packet.visible);
    }

    public static ToggleCombatBarPacket decode(FriendlyByteBuf buffer) {
        return new ToggleCombatBarPacket(buffer.readBoolean());
    }

    public static void handle(ToggleCombatBarPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        ServerPlayer player = context.getSender();
        if (player == null) {
            context.setPacketHandled(true);
            return;
        }

        context.enqueueWork(() -> {
            HunterPlayerData data = HunterDataUtil.get(player);
            data.setCombatBarVisible(packet.visible);
            HunterDataUtil.sync(player);
        });
        context.setPacketHandled(true);
    }
}
