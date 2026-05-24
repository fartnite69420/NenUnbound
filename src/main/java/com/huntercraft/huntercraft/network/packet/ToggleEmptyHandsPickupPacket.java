package com.huntercraft.huntercraft.network.packet;

import com.huntercraft.huntercraft.data.HunterPlayerData;
import com.huntercraft.huntercraft.util.HunterDataUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ToggleEmptyHandsPickupPacket {
    private final boolean enabled;

    public ToggleEmptyHandsPickupPacket(boolean enabled) {
        this.enabled = enabled;
    }

    public static void encode(ToggleEmptyHandsPickupPacket packet, FriendlyByteBuf buffer) {
        buffer.writeBoolean(packet.enabled);
    }

    public static ToggleEmptyHandsPickupPacket decode(FriendlyByteBuf buffer) {
        return new ToggleEmptyHandsPickupPacket(buffer.readBoolean());
    }

    public static void handle(ToggleEmptyHandsPickupPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        ServerPlayer player = context.getSender();
        if (player == null) {
            context.setPacketHandled(true);
            return;
        }

        context.enqueueWork(() -> {
            HunterPlayerData data = HunterDataUtil.get(player);
            data.setEmptyHandsPickupEnabled(packet.enabled);
            HunterDataUtil.sync(player);
        });
        context.setPacketHandled(true);
    }
}
