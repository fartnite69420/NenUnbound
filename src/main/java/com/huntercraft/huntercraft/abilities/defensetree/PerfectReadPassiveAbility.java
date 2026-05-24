package com.huntercraft.huntercraft.abilities.defensetree;

import com.huntercraft.huntercraft.abilities.SkillTreePassiveAbility;
import com.huntercraft.huntercraft.data.HunterPlayerData;
import com.huntercraft.huntercraft.progression.SkillNode;

public class PerfectReadPassiveAbility extends SkillTreePassiveAbility {
    public PerfectReadPassiveAbility() {
        super("perfect_read", "Perfect Read", "At 30 Defense points, the guard parry window increases from 0.5 seconds to 0.75 seconds.", SkillNode.DEFENSE, 30);
    }

    public int getGuardParryTicks(HunterPlayerData data) {
        return this.isUnlocked(data) ? 15 : 10;
    }
}
