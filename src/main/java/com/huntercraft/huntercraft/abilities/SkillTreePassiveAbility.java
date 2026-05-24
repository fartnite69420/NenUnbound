package com.huntercraft.huntercraft.abilities;

import com.huntercraft.huntercraft.progression.SkillNode;

public class SkillTreePassiveAbility extends SkillTreeAbility {
    public SkillTreePassiveAbility(String id, String displayName, String description, SkillNode skillNode, int requiredPoints) {
        super(id, displayName, description, "textures/gui/abilities/guard.png", skillNode, requiredPoints);
    }
}
