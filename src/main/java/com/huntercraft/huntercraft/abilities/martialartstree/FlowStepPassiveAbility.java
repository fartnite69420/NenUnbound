package com.huntercraft.huntercraft.abilities.martialartstree;

import com.huntercraft.huntercraft.abilities.SkillTreePassiveAbility;
import com.huntercraft.huntercraft.data.HunterPlayerData;
import com.huntercraft.huntercraft.progression.SkillNode;

public class FlowStepPassiveAbility extends SkillTreePassiveAbility {
    public FlowStepPassiveAbility() {
        super("flow_step", "Flow Step", "At 5 Martial Arts points, movement abilities gain 15% more speed and distance.", SkillNode.MARTIAL_ARTS, 5);
    }

    public float getMovementAbilityMultiplier(HunterPlayerData data) {
        return this.isUnlocked(data) ? 1.15F : 1.0F;
    }
}
