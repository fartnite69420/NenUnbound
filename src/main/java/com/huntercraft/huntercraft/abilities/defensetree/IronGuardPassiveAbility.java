package com.huntercraft.huntercraft.abilities.defensetree;

import com.huntercraft.huntercraft.abilities.SkillTreePassiveAbility;
import com.huntercraft.huntercraft.data.HunterPlayerData;
import com.huntercraft.huntercraft.progression.SkillNode;

public class IronGuardPassiveAbility extends SkillTreePassiveAbility {
    public IronGuardPassiveAbility() {
        super("iron_guard", "Iron Guard", "At 5 Defense points, guard damage reduction falls off by only 5% per second instead of 20%.", SkillNode.DEFENSE, 5);
    }

    public float getGuardFalloffPerSecond(HunterPlayerData data) {
        return this.isUnlocked(data) ? 0.05F : 0.20F;
    }
}
