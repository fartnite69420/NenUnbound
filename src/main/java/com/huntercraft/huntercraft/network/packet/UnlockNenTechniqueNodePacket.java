package com.huntercraft.huntercraft.network.packet;

import com.huntercraft.huntercraft.data.HunterPlayerData;
import com.huntercraft.huntercraft.progression.NenTechniqueSkillNode;
import com.huntercraft.huntercraft.util.HunterDataUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class UnlockNenTechniqueNodePacket {
    private final String nodeId;

    public UnlockNenTechniqueNodePacket(String nodeId) {
        this.nodeId = nodeId;
    }

    public static void encode(UnlockNenTechniqueNodePacket packet, FriendlyByteBuf buffer) {
        buffer.writeUtf(packet.nodeId);
    }

    public static UnlockNenTechniqueNodePacket decode(FriendlyByteBuf buffer) {
        return new UnlockNenTechniqueNodePacket(buffer.readUtf());
    }

    public static void handle(UnlockNenTechniqueNodePacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        ServerPlayer player = context.getSender();
        if (player == null) {
            context.setPacketHandled(true);
            return;
        }

        context.enqueueWork(() -> {
            NenTechniqueSkillNode node = NenTechniqueSkillNode.byId(packet.nodeId);
            if (node == null) {
                return;
            }

            HunterPlayerData data = HunterDataUtil.get(player);
            if (!data.unlockNenTechniqueNode(node)) {
                player.sendSystemMessage(Component.literal("You cannot unlock that Nen technique node yet."));
                return;
            }

            player.sendSystemMessage(Component.literal("Unlocked " + node.displayName() + "."));
            HunterDataUtil.sync(player);
        });

        context.setPacketHandled(true);
    }
}
