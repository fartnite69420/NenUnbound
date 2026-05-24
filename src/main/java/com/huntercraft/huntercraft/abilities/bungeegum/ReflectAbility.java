package com.huntercraft.huntercraft.abilities.bungeegum;

import com.huntercraft.huntercraft.abilities.SkillTreeCombatAbility;
import com.huntercraft.huntercraft.data.HunterPlayerData;
import com.huntercraft.huntercraft.entity.BungeeGumReflectEntity;
import com.huntercraft.huntercraft.progression.NenTechniqueSkillNode;
import com.huntercraft.huntercraft.progression.SkillNode;
import com.huntercraft.huntercraft.util.HunterDataUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

public class ReflectAbility extends SkillTreeCombatAbility {
    public static final int NEN_COST = 300;
    private static final int COOLDOWN_TICKS = 14 * 20;

    public ReflectAbility() {
        super("elastic_reflect", "Reflect", "Wrap yourself in a slick shell of transmuted gum that rebounds incoming projectiles back at their source with extra speed.", "textures/gui/abilities/bungee_gum_reflect.png", SkillNode.MARTIAL_ARTS, 0, com.huntercraft.huntercraft.abilities.AbilitySourceType.NEN, com.huntercraft.huntercraft.abilities.AbilitySourceType.BLUNT);
    }

    @Override
    public boolean isUnlocked(HunterPlayerData data) {
        return data.hasUnlockedNenTechniqueNode(NenTechniqueSkillNode.ELASTIC_AURA_REFLECT);
    }

    @Override
    public int getMaxCooldownTicks() {
        return COOLDOWN_TICKS;
    }

    @Override
    public boolean isContinuous() {
        return true;
    }

    @Override
    protected boolean requiresWeaponInHand() {
        return false;
    }

    @Override
    protected boolean requiresEmptyMainHand() {
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
        if (this.isActive(data)) {
            stop(player, data);
            return;
        }
        if (!data.consumeNen(NEN_COST)) {
            return;
        }
        boolean hidden = ElasticAuraManager.consumeTextureSurprise(player);
        ElasticAuraManager.setReflectHidden(player, hidden);
        if (player.level() instanceof ServerLevel level) {
            BungeeGumReflectEntity.spawn(level, player, data.getNenAuraColor(), hidden);
        }
        data.startActiveAbility(this.id(), ElasticAuraManager.REFLECT_TICKS, direction);
        data.triggerAnimation(com.huntercraft.huntercraft.animation.AnimationType.ELASTIC_REFLECT);
        HunterDataUtil.sync(player);
    }

    @Override
    public void tick(ServerPlayer player, HunterPlayerData data) {
        if (!this.isActive(data)) {
            return;
        }
        int remaining = data.getActiveAbilityTicksRemaining();
        data.tickActiveAbility();
        if (remaining > 0 && data.getActiveAbilityTicksRemaining() <= 0) {
            data.clearActiveAbility();
            data.triggerAnimation(com.huntercraft.huntercraft.animation.AnimationType.NONE);
            ElasticAuraManager.setReflectHidden(player, false);
            this.startCooldown(data, COOLDOWN_TICKS);
            HunterDataUtil.sync(player);
        }
    }

    @Override
    public void stop(ServerPlayer player, HunterPlayerData data) {
        if (!this.isActive(data)) {
            return;
        }
        data.clearActiveAbility();
        data.triggerAnimation(com.huntercraft.huntercraft.animation.AnimationType.NONE);
        ElasticAuraManager.setReflectHidden(player, false);
        this.startCooldown(data, COOLDOWN_TICKS);
        HunterDataUtil.sync(player);
    }
}
