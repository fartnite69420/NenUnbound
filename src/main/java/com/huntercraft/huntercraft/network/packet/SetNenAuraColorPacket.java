package com.huntercraft.huntercraft.network.packet;

import com.huntercraft.huntercraft.data.HunterPlayerData;
import com.huntercraft.huntercraft.util.HunterDataUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SetNenAuraColorPacket {
    private final int color;

    public SetNenAuraColorPacket(int color) {
        this.color = color;
    }

    public static void encode(SetNenAuraColorPacket packet, FriendlyByteBuf buffer) {
        buffer.writeInt(packet.color);
    }

    public static SetNenAuraColorPacket decode(FriendlyByteBuf buffer) {
        return new SetNenAuraColorPacket(buffer.readInt());
    }

    public static void handle(SetNenAuraColorPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        ServerPlayer player = context.getSender();
        if (player == null) {
            context.setPacketHandled(true);
            return;
        }

        context.enqueueWork(() -> {
            HunterPlayerData data = HunterDataUtil.get(player);
            data.setNenAuraColor(packet.color);
            HunterDataUtil.sync(player);
        });
        context.setPacketHandled(true);
    }
}
