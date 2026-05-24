package com.huntercraft.huntercraft.network.packet;

import com.huntercraft.huntercraft.data.HunterPlayerData;
import com.huntercraft.huntercraft.quest.QuestDefinition;
import com.huntercraft.huntercraft.quest.QuestRegistry;
import com.huntercraft.huntercraft.util.HunterDataUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class CancelPhoneQuestPacket {
    private final String questId;

    public CancelPhoneQuestPacket(String questId) {
        this.questId = questId;
    }

    public static void encode(CancelPhoneQuestPacket packet, FriendlyByteBuf buffer) {
        buffer.writeUtf(packet.questId);
    }

    public static CancelPhoneQuestPacket decode(FriendlyByteBuf buffer) {
        return new CancelPhoneQuestPacket(buffer.readUtf());
    }

    public static void handle(CancelPhoneQuestPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        ServerPlayer player = context.getSender();
        if (player == null) {
            context.setPacketHandled(true);
            return;
        }

        context.enqueueWork(() -> {
            HunterPlayerData data = HunterDataUtil.get(player);
            QuestDefinition quest = QuestRegistry.byId(packet.questId);
            if (quest == null) {
                return;
            }
            if (data.cancelQuest(packet.questId)) {
                if (com.huntercraft.huntercraft.quest.PhoneQuestRegistry.isPhoneQuest(packet.questId)) {
                    data.startPhoneQuestRefresh(player.level().getGameTime());
                }
                player.sendSystemMessage(Component.translatable("message.huntercraft.phone.canceled", Component.translatable(quest.titleKey())));
                HunterDataUtil.sync(player);
            }
        });
        context.setPacketHandled(true);
    }
}
