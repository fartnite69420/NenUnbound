package com.huntercraft.huntercraft.abilities.boxertree;

import com.huntercraft.huntercraft.abilities.SkillTreePassiveAbility;
import com.huntercraft.huntercraft.data.HunterPlayerData;
import com.huntercraft.huntercraft.progression.SkillNode;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;

public class SlipGuardPassiveAbility extends SkillTreePassiveAbility {
    private static final float DODGE_CHANCE = 0.30F;

    public SlipGuardPassiveAbility() {
        super("slip_guard", "Slip Weave", "At 5 Boxing points, you gain a 30% chance to dodge direct melee hits.", SkillNode.BOXING, 5);
    }

    public boolean shouldDodgeMelee(HunterPlayerData data, ServerPlayer player, DamageSource source) {
        if (!this.isUnlocked(data)) {
            return false;
        }
        if (source.getEntity() == null || source.getDirectEntity() != source.getEntity()) {
            return false;
        }
        return player.getRandom().nextFloat() < DODGE_CHANCE;
    }
}
