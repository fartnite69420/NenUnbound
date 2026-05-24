package com.huntercraft.huntercraft.abilities;

import com.huntercraft.huntercraft.data.HunterPlayerData;
import com.huntercraft.huntercraft.progression.SkillNode;

public class CategoryPassiveAbility extends SkillTreePassiveAbility {
    private final SkillNode.Category category;

    public CategoryPassiveAbility(String id, String displayName, String description, SkillNode.Category category) {
        super(id, displayName, description, category == SkillNode.Category.WEAPON ? SkillNode.SPEED : SkillNode.BOXING, 0);
        this.category = category;
    }

    @Override
    public boolean isUnlocked(HunterPlayerData data) {
        return data.getSelectedSkillCategory() == this.category;
    }

    public SkillNode.Category category() {
        return this.category;
    }
}
