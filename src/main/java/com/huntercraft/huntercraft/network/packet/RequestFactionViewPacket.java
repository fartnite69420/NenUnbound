package com.huntercraft.huntercraft.network.packet;

import com.huntercraft.huntercraft.util.HunterDataUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class RequestFactionViewPacket {
    public static void encode(RequestFactionViewPacket packet, FriendlyByteBuf buffer) {
    }

    public static RequestFactionViewPacket decode(FriendlyByteBuf buffer) {
        return new RequestFactionViewPacket();
    }

    public static void handle(RequestFactionViewPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        ServerPlayer player = context.getSender();
        if (player != null) {
            context.enqueueWork(() -> HunterDataUtil.sync(player));
        }
        context.setPacketHandled(true);
    }
}
