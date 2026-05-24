package com.huntercraft.huntercraft.quest;

import java.util.List;

public final class PhoneQuestRegistry {
    private static final List<PhoneJobDefinition> PHONE_JOBS = List.of(
            of(QuestRegistry.PIG_HUNTING, 700, 1200, "Mid", 0xC77664),
            of(QuestRegistry.WEAPON_CREATION, 300, 650, "Low", 0x8DA8C9),
            of(QuestRegistry.ITEM_RETRIEVAL, 350, 800, "Low", 0x7BB9A8),
            of(QuestRegistry.ITEM_DELIVERY, 500, 1100, "Mid", 0xD1A76F),
            of(QuestRegistry.BANDIT_BEATING, 900, 1600, "High", 0xC96F5D),
            of(QuestRegistry.PACKAGE_PICKUP, 1100, 2000, "High", 0x8F8CE1)
    );

    private PhoneQuestRegistry() {
    }

    private static PhoneJobDefinition of(QuestDefinition quest, int minRewardXp, int maxRewardXp, String difficultyLabel, int accentColor) {
        return new PhoneJobDefinition(
                quest.id(),
                quest.titleKey(),
                quest.descriptionKey(),
                minRewardXp,
                maxRewardXp,
                difficultyLabel,
                accentColor
        );
    }

    public static List<PhoneJobDefinition> all() {
        return PHONE_JOBS;
    }

    public static boolean isPhoneQuest(String questId) {
        return PHONE_JOBS.stream().anyMatch(job -> job.id().equals(questId));
    }
}
