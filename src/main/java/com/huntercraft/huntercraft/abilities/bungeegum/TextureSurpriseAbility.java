package com.huntercraft.huntercraft.abilities.bungeegum;

import com.huntercraft.huntercraft.abilities.SkillTreeCombatAbility;
import com.huntercraft.huntercraft.data.HunterPlayerData;
import com.huntercraft.huntercraft.progression.NenTechniqueSkillNode;
import com.huntercraft.huntercraft.progression.SkillNode;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

public class TextureSurpriseAbility extends SkillTreeCombatAbility {
    public static final int NEN_COST = 500;
    private static final int COOLDOWN_TICKS = 20 * 20;

    public TextureSurpriseAbility() {
        super("texture_surprise", "Texture Surprise", "Masks the next Elastic Aura technique by suppressing its visible projectile, strands, trap shell, or reflective aura.", "textures/gui/abilities/texture_surprise.png", SkillNode.MARTIAL_ARTS, 0, com.huntercraft.huntercraft.abilities.AbilitySourceType.NEN);
    }

    @Override
    public boolean isUnlocked(HunterPlayerData data) {
        return data.hasUnlockedNenTechniqueNode(NenTechniqueSkillNode.ELASTIC_AURA_TEXTURE_SURPRISE);
    }

    @Override
    public int getMaxCooldownTicks() {
        return COOLDOWN_TICKS;
    }

    @Override
    protected boolean requiresWeaponInHand() {
        return false;
    }

    @Override
    public boolean canUse(ServerPlayer player, HunterPlayerData data) {
        return super.canUse(player, data)
                && ElasticAuraManager.TECHNIQUE_ID.equals(data.getNenTechniqueId())
                && data.hasStaminaForNenCost(NEN_COST);
    }

    @Override
    public void use(ServerPlayer player, HunterPlayerData data, Vec3 direction) {
        if (!data.consumeNen(NEN_COST)) {
            return;
        }
        ElasticAuraManager.armTextureSurprise(player);
        this.startCooldown(data, COOLDOWN_TICKS);
    }
}
