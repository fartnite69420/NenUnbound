package com.huntercraft.huntercraft.quest;

public record QuestDefinition(
        String id,
        String titleKey,
        String descriptionKey,
        QuestObjectiveType objectiveType,
        String targetId,
        int targetCount,
        int rewardXp
) {
}

