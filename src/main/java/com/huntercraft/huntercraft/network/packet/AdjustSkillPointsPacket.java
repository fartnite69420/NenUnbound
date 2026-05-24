package com.huntercraft.huntercraft.network.packet;

import com.huntercraft.huntercraft.data.HunterPlayerData;
import com.huntercraft.huntercraft.progression.SkillNode;
import com.huntercraft.huntercraft.util.HunterDataUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class AdjustSkillPointsPacket {
    private final String styleId;
    private final int delta;

    public AdjustSkillPointsPacket(String styleId, int delta) {
        this.styleId = styleId;
        this.delta = delta;
    }

    public static void encode(AdjustSkillPointsPacket packet, FriendlyByteBuf buffer) {
        buffer.writeUtf(packet.styleId);
        buffer.writeInt(packet.delta);
    }

    public static AdjustSkillPointsPacket decode(FriendlyByteBuf buffer) {
        return new AdjustSkillPointsPacket(buffer.readUtf(), buffer.readInt());
    }

    public static void handle(AdjustSkillPointsPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        ServerPlayer player = context.getSender();
        if (player == null) {
            context.setPacketHandled(true);
            return;
        }

        context.enqueueWork(() -> {
            SkillNode node = SkillNode.byId(packet.styleId);
            if (node == null || (packet.delta != 1 && packet.delta != -1)) {
                return;
            }

            HunterPlayerData data = HunterDataUtil.get(player);
            boolean changed = packet.delta > 0 ? data.addSkillPoint(node) : data.removeSkillPoint(node);
            if (!changed) {
                if (packet.delta > 0 && data.getSelectedSkillCategory() != null && data.getSelectedSkillCategory() != node.category()) {
                    player.sendSystemMessage(Component.translatable("message.huntercraft.category_locked",
                            Component.translatable(data.getSelectedSkillCategory().translationKey())));
                    return;
                }
                player.sendSystemMessage(packet.delta > 0
                        ? Component.translatable("message.huntercraft.node_no_points")
                        : Component.translatable("message.huntercraft.node_cannot_remove"));
                return;
            }

            player.sendSystemMessage(Component.translatable(
                    packet.delta > 0 ? "message.huntercraft.node_added" : "message.huntercraft.node_removed",
                    Component.translatable(node.translationKey())
            ));
            HunterDataUtil.sync(player);
        });

        context.setPacketHandled(true);
    }
}
