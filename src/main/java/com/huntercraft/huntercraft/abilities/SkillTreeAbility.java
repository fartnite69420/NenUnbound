package com.huntercraft.huntercraft.abilities;

import com.huntercraft.huntercraft.ability.HunterAbility;
import com.huntercraft.huntercraft.data.HunterPlayerData;
import com.huntercraft.huntercraft.progression.SkillNode;

public class SkillTreeAbility extends HunterAbility {
    private final SkillNode skillNode;
    private final int requiredPoints;

    public SkillTreeAbility(String id, String displayName, String description, String iconPath, SkillNode skillNode, int requiredPoints) {
        this(id, displayName, description, iconPath, skillNode, requiredPoints, new AbilitySourceType[0]);
    }

    public SkillTreeAbility(String id, String displayName, String description, String iconPath, SkillNode skillNode, int requiredPoints, AbilitySourceType... sourceTypes) {
        super(id, displayName, description, iconPath, false, sourceTypes);
        this.skillNode = skillNode;
        this.requiredPoints = requiredPoints;
    }

    public SkillNode skillNode() {
        return this.skillNode;
    }

    public int requiredPoints() {
        return this.requiredPoints;
    }

    public boolean isUnlocked(HunterPlayerData data) {
        return data.getStylePoints(this.skillNode) >= this.requiredPoints;
    }
}
