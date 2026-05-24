package com.huntercraft.huntercraft.abilities;

import com.huntercraft.huntercraft.data.HunterPlayerData;
import com.huntercraft.huntercraft.progression.SkillNode;

public class WeaponMasteryPassiveAbility extends CategoryPassiveAbility {
    public WeaponMasteryPassiveAbility() {
        super("weapon_mastery", "Weapon Mastery", "While locked into the Weapon category, weapon damage is increased by 15%.", SkillNode.Category.WEAPON);
    }

    public float getWeaponDamageMultiplier(HunterPlayerData data) {
        return this.isUnlocked(data) ? 1.15F : 1.0F;
    }
}
