package com.huntercraft.huntercraft.abilities;

import com.huntercraft.huntercraft.data.HunterPlayerData;
import com.huntercraft.huntercraft.progression.SkillNode;

public class PhysicalPowerPassiveAbility extends CategoryPassiveAbility {
    public PhysicalPowerPassiveAbility() {
        super("physical_power", "Physical Power", "While locked into the Physical category, punches gain a solid damage increase.", SkillNode.Category.PHYSICAL);
    }

    public float getPunchDamageMultiplier(HunterPlayerData data) {
        return this.isUnlocked(data) ? 1.25F : 1.0F;
    }
}
