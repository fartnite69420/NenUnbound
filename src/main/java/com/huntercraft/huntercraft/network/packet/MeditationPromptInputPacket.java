package com.huntercraft.huntercraft.network.packet;

import com.huntercraft.huntercraft.animation.AnimationType;
import com.huntercraft.huntercraft.data.HunterPlayerData;
import com.huntercraft.huntercraft.quest.NenQuestStage;
import com.huntercraft.huntercraft.quest.NenQuestUtil;
import com.huntercraft.huntercraft.util.HunterDataUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

public class MeditationPromptInputPacket {
    private static final String[] PROMPT_KEYS = {"W", "A", "S", "D"};
    public static final int COUNTDOWN_TICKS = 200;
    public static final int RESPONSE_WINDOW_TICKS = 15;
    private final String inputKey;

    public MeditationPromptInputPacket(String inputKey) {
        this.inputKey = inputKey == null ? "" : inputKey.toUpperCase(Locale.ROOT);
    }

    public static void encode(MeditationPromptInputPacket packet, FriendlyByteBuf buffer) {
        buffer.writeUtf(packet.inputKey);
    }

    public static MeditationPromptInputPacket decode(FriendlyByteBuf buffer) {
        return new MeditationPromptInputPacket(buffer.readUtf());
    }

    public static void handle(MeditationPromptInputPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        ServerPlayer player = context.getSender();
        if (player == null) {
            context.setPacketHandled(true);
            return;
        }

        context.enqueueWork(() -> {
            HunterPlayerData data = HunterDataUtil.get(player);
            if (data.getNenQuestStage() != NenQuestStage.FEEL_THE_AURA || !data.isMeditationActive()) {
                return;
            }
            if (!packet.inputKey.equalsIgnoreCase(data.getMeditationPromptKey())) {
                failMeditation(player, data, "Wing: Your focus broke. Begin the meditation again.");
                return;
            }
            data.setMeditationPromptSeed(data.getMeditationPromptSeed() + 1);
            rollNextPrompt(data);
            HunterDataUtil.sync(player);
        });
        context.setPacketHandled(true);
    }

    public static void startMeditation(ServerPlayer player, HunterPlayerData data) {
        data.setFeelAuraTicks(0);
        data.setMeditationCountdownTicks(COUNTDOWN_TICKS);
        data.setMeditationActive(false);
        data.setMeditationTicksRemaining(0);
        data.setMeditationPromptSeed(0);
        data.setMeditationPromptKey("");
        data.setMeditationPromptTicksRemaining(0);
        data.triggerAnimation(AnimationType.MEDITATE);
        player.sendSystemMessage(Component.literal("Wing: Sit. Breathe. The meditation begins in ten seconds."));
    }

    public static void tickMeditation(ServerPlayer player, HunterPlayerData data) {
        if (!data.isMeditationCountdownActive() && !data.isMeditationActive()) {
            return;
        }
        data.triggerAnimation(AnimationType.MEDITATE);
        if (data.isMeditationCountdownActive()) {
            data.setMeditationCountdownTicks(data.getMeditationCountdownTicks() - 1);
            if (!data.isMeditationCountdownActive()) {
                data.setMeditationActive(true);
                data.setMeditationTicksRemaining(NenQuestUtil.FEEL_AURA_TICKS_REQUIRED);
                data.setMeditationPromptSeed(0);
                rollNextPrompt(data);
                player.sendSystemMessage(Component.literal("Wing: Good. Now answer the pulses."));
            }
            return;
        }
        data.setMeditationTicksRemaining(data.getMeditationTicksRemaining() - 1);
        data.setMeditationPromptTicksRemaining(data.getMeditationPromptTicksRemaining() - 1);
        data.setFeelAuraTicks(NenQuestUtil.FEEL_AURA_TICKS_REQUIRED - data.getMeditationTicksRemaining());
        if (data.getMeditationPromptTicksRemaining() <= 0) {
            failMeditation(player, data, "Wing: Too slow. Settle your mind and start again.");
            return;
        }
        if (data.getMeditationTicksRemaining() <= 0) {
            data.stopMeditation();
            data.setFeelAuraTicks(NenQuestUtil.FEEL_AURA_TICKS_REQUIRED);
            player.sendSystemMessage(Component.literal("Wing: Good. That stillness is the first doorway."));
        }
    }

    public static void failMeditation(ServerPlayer player, HunterPlayerData data, String message) {
        data.stopMeditation();
        data.setFeelAuraTicks(0);
        player.sendSystemMessage(Component.literal(message));
        HunterDataUtil.sync(player);
    }

    private static void rollNextPrompt(HunterPlayerData data) {
        String previous = data.getMeditationPromptKey();
        String next = previous;
        while (next.equals(previous)) {
            next = PROMPT_KEYS[ThreadLocalRandom.current().nextInt(PROMPT_KEYS.length)];
        }
        data.setMeditationPromptKey(next);
        data.setMeditationPromptTicksRemaining(RESPONSE_WINDOW_TICKS);
    }
}
