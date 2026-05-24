package com.huntercraft.huntercraft.network.packet;

import com.huntercraft.huntercraft.abilities.ChainConjuration.ChainJailAbility;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ChainJailMinigameInputPacket {
    private final int inputTick;

    public ChainJailMinigameInputPacket(int inputTick) {
        this.inputTick = inputTick;
    }

    public static void encode(ChainJailMinigameInputPacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.inputTick);
    }

    public static ChainJailMinigameInputPacket decode(FriendlyByteBuf buffer) {
        return new ChainJailMinigameInputPacket(buffer.readVarInt());
    }

    public static void handle(ChainJailMinigameInputPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        ServerPlayer player = context.getSender();
        if (player == null) {
            context.setPacketHandled(true);
            return;
        }
        context.enqueueWork(() -> ChainJailAbility.handleEscapeInput(player, packet.inputTick));
        context.setPacketHandled(true);
    }
}
