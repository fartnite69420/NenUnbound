package com.huntercraft.huntercraft.abilities.speedtree;

import com.huntercraft.huntercraft.abilities.SkillTreePassiveAbility;
import com.huntercraft.huntercraft.data.HunterPlayerData;
import com.huntercraft.huntercraft.progression.SkillNode;

public class StepDrivePassiveAbility extends SkillTreePassiveAbility {
    public StepDrivePassiveAbility() {
        super("step_drive", "Step Drive", "At 5 Speed points, movement abilities gain 10% more speed and distance.", SkillNode.SPEED, 5);
    }

    public float getMovementAbilityMultiplier(HunterPlayerData data) {
        return this.isUnlocked(data) ? 1.10F : 1.0F;
    }
}
