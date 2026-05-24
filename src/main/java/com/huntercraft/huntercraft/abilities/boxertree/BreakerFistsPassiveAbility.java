package com.huntercraft.huntercraft.abilities.boxertree;

import com.huntercraft.huntercraft.abilities.SkillTreePassiveAbility;
import com.huntercraft.huntercraft.data.HunterPlayerData;
import com.huntercraft.huntercraft.progression.SkillNode;

public class BreakerFistsPassiveAbility extends SkillTreePassiveAbility {
    public BreakerFistsPassiveAbility() {
        super("breaker_fists", "Tight Guard", "At 30 Boxing points, the guard parry window increases from 0.5 seconds to 0.6 seconds.", SkillNode.BOXING, 30);
    }

    public int getGuardParryTicks(HunterPlayerData data) {
        return this.isUnlocked(data) ? 12 : 10;
    }
}
