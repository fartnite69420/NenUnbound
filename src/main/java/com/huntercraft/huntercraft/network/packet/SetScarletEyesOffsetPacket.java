package com.huntercraft.huntercraft.network.packet;

import com.huntercraft.huntercraft.util.HunterDataUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record SetScarletEyesOffsetPacket(int leftX, int leftY, int leftLength, int leftVerticalLength, int rightX, int rightY, int rightLength, int rightVerticalLength) {
    public SetScarletEyesOffsetPacket(int offsetX, int offsetY) {
        this(offsetX - 6, offsetY, -1, 1, offsetX + 6, offsetY, 1, 1);
    }

    public SetScarletEyesOffsetPacket(int leftX, int leftY, int leftLength, int rightX, int rightY, int rightLength) {
        this(leftX, leftY, leftLength, 1, rightX, rightY, rightLength, 1);
    }

    public static void encode(SetScarletEyesOffsetPacket packet, FriendlyByteBuf buffer) {
        buffer.writeInt(packet.leftX);
        buffer.writeInt(packet.leftY);
        buffer.writeInt(packet.leftLength);
        buffer.writeInt(packet.leftVerticalLength);
        buffer.writeInt(packet.rightX);
        buffer.writeInt(packet.rightY);
        buffer.writeInt(packet.rightLength);
        buffer.writeInt(packet.rightVerticalLength);
    }

    public static SetScarletEyesOffsetPacket decode(FriendlyByteBuf buffer) {
        return new SetScarletEyesOffsetPacket(buffer.readInt(), buffer.readInt(), buffer.readInt(), buffer.readInt(), buffer.readInt(), buffer.readInt(), buffer.readInt(), buffer.readInt());
    }

    public static void handle(SetScarletEyesOffsetPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) {
                return;
            }
            HunterDataUtil.getOptional(player).ifPresent(data -> {
                if (!data.hasScarletEyesTrait()) {
                    return;
                }
                data.setScarletEyesLayout(packet.leftX, packet.leftY, packet.leftLength, packet.leftVerticalLength, packet.rightX, packet.rightY, packet.rightLength, packet.rightVerticalLength);
                HunterDataUtil.sync(player);
            });
        });
        context.setPacketHandled(true);
    }
}
