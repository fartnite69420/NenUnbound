package com.huntercraft.huntercraft.abilities.martialartstree;

import com.huntercraft.huntercraft.abilities.SkillTreePassiveAbility;
import com.huntercraft.huntercraft.data.HunterPlayerData;
import com.huntercraft.huntercraft.progression.SkillNode;

public class FlowStatePassiveAbility extends SkillTreePassiveAbility {
    public FlowStatePassiveAbility() {
        super("flow_state", "Flow State", "At 30 Martial Arts points, stun time from other players is reduced by 25%.", SkillNode.MARTIAL_ARTS, 30);
    }

    public float getStunFactor(HunterPlayerData data) {
        return this.isUnlocked(data) ? 0.75F : 1.0F;
    }
}
