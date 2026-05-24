package com.huntercraft.huntercraft.network.packet;

import com.huntercraft.huntercraft.faction.FactionSavedData;
import com.huntercraft.huntercraft.util.HunterDataUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class CreateFactionPacket {
    private final String factionName;

    public CreateFactionPacket(String factionName) {
        this.factionName = factionName;
    }

    public static void encode(CreateFactionPacket packet, FriendlyByteBuf buffer) {
        buffer.writeUtf(packet.factionName);
    }

    public static CreateFactionPacket decode(FriendlyByteBuf buffer) {
        return new CreateFactionPacket(buffer.readUtf());
    }

    public static void handle(CreateFactionPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        ServerPlayer player = context.getSender();
        if (player == null) {
            context.setPacketHandled(true);
            return;
        }

        context.enqueueWork(() -> {
            boolean created = FactionSavedData.get(player.server).createFaction(player, packet.factionName);
            if (!created) {
                player.sendSystemMessage(Component.literal("Unable to create faction. Use a unique 3-20 character name."));
            }
            HunterDataUtil.sync(player);
        });

        context.setPacketHandled(true);
    }
}
