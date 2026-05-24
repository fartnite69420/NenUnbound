package com.huntercraft.huntercraft.abilities.speedtree;

import com.huntercraft.huntercraft.abilities.SkillTreePassiveAbility;
import com.huntercraft.huntercraft.data.HunterPlayerData;
import com.huntercraft.huntercraft.progression.SkillNode;

public class FlashAccelPassiveAbility extends SkillTreePassiveAbility {
    public FlashAccelPassiveAbility() {
        super("flash_accel", "Flash Accel", "At 30 Speed points, movement abilities gain a 50% speed bonus.", SkillNode.SPEED, 30);
    }

    public float getMovementAbilityMultiplier(HunterPlayerData data) {
        return this.isUnlocked(data) ? 1.50F : 1.0F;
    }
}
