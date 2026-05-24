package com.huntercraft.huntercraft.quest;

public record PhoneJobDefinition(
        String id,
        String titleKey,
        String descriptionKey,
        int minRewardXp,
        int maxRewardXp,
        String difficultyLabel,
        int accentColor
) {
}
