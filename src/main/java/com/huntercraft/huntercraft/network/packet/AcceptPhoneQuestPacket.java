package com.huntercraft.huntercraft.network.packet;

import com.huntercraft.huntercraft.data.HunterPlayerData;
import com.huntercraft.huntercraft.quest.PhoneQuestRegistry;
import com.huntercraft.huntercraft.quest.QuestDefinition;
import com.huntercraft.huntercraft.quest.QuestRegistry;
import com.huntercraft.huntercraft.quest.QuestSiteUtil;
import com.huntercraft.huntercraft.util.HunterDataUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class AcceptPhoneQuestPacket {
    private final String questId;

    public AcceptPhoneQuestPacket(String questId) {
        this.questId = questId;
    }

    public static void encode(AcceptPhoneQuestPacket packet, FriendlyByteBuf buffer) {
        buffer.writeUtf(packet.questId);
    }

    public static AcceptPhoneQuestPacket decode(FriendlyByteBuf buffer) {
        return new AcceptPhoneQuestPacket(buffer.readUtf());
    }

    public static void handle(AcceptPhoneQuestPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
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
            data.refreshPhoneQuestsIfReady(player.level().getGameTime());
            if (data.getCompletedQuests().contains(packet.questId)) {
                player.sendSystemMessage(Component.translatable("message.huntercraft.phone.already_completed", Component.translatable(quest.titleKey())));
                HunterDataUtil.sync(player);
                return;
            }
            if (data.getActiveQuests().contains(packet.questId)) {
                player.sendSystemMessage(Component.translatable("message.huntercraft.phone.already_active", Component.translatable(quest.titleKey())));
                HunterDataUtil.sync(player);
                return;
            }
            boolean hasActivePhoneQuest = data.getActiveQuests().stream().anyMatch(PhoneQuestRegistry::isPhoneQuest);
            if (hasActivePhoneQuest) {
                player.sendSystemMessage(Component.translatable("message.huntercraft.phone.only_one"));
                HunterDataUtil.sync(player);
                return;
            }
            if (data.isPhoneQuestRefreshing(player.level().getGameTime())) {
                player.sendSystemMessage(Component.translatable("message.huntercraft.phone.refreshing", data.getPhoneQuestRefreshRemaining(player.level().getGameTime()) / 20L));
                HunterDataUtil.sync(player);
                return;
            }
            int targetCount = QuestRegistry.getScaledTargetCount(quest, data.getLevel());
            int rewardXp = QuestRegistry.getScaledRewardXp(quest, data.getLevel());
            if (data.addScaledQuest(packet.questId, targetCount, rewardXp)) {
                BlockPos questSite = QuestSiteUtil.createQuestSite(player, quest, targetCount);
                data.setQuestLocation(packet.questId, questSite);
                player.sendSystemMessage(Component.translatable("message.huntercraft.phone.accepted", Component.translatable(quest.titleKey())));
                player.sendSystemMessage(Component.translatable("message.huntercraft.phone.location_set", questSite.getX(), questSite.getY(), questSite.getZ()));
                HunterDataUtil.sync(player);
            }
        });
        context.setPacketHandled(true);
    }
}
