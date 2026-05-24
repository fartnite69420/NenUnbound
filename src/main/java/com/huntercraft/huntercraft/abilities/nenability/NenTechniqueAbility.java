package com.huntercraft.huntercraft.abilities.nenability;

import com.huntercraft.huntercraft.abilities.HunterAbilities;
import com.huntercraft.huntercraft.abilities.AbilitySourceType;
import com.huntercraft.huntercraft.abilities.SkillTreeCombatAbility;
import com.huntercraft.huntercraft.data.HunterPlayerData;
import com.huntercraft.huntercraft.effect.HunterMobEffects;
import com.huntercraft.huntercraft.progression.SkillNode;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

public abstract class NenTechniqueAbility extends SkillTreeCombatAbility {
    public static final int PASSIVE_REGEN_PER_TICK = 3;
    public static final int SUSTAINED_DRAIN_PER_TICK = 1;
    private static final int SUSTAINED_DRAIN_INTERVAL_TICKS = 5;
    private final int requiredNenLevel;

    protected NenTechniqueAbility(String id, String displayName, String description, String iconPath, int requiredNenLevel) {
        super(id, displayName, description, iconPath, SkillNode.MARTIAL_ARTS, 0, AbilitySourceType.NEN);
        this.requiredNenLevel = requiredNenLevel;
    }

    public int requiredNenLevel() {
        return this.requiredNenLevel;
    }

    @Override
    public boolean isUnlocked(HunterPlayerData data) {
        if (data.getNenLevel() < this.requiredNenLevel) {
            return false;
        }
        return switch (this.id()) {
            case "nen_ten" -> data.hasTenUnlocked();
            case "nen_zetsu" -> data.hasZetsuUnlocked();
            case "nen_ren" -> data.hasRenUnlocked();
            case "nen_en" -> data.hasEnUnlocked();
            case "nen_ko" -> data.hasKoUnlocked();
            case "nen_ken" -> data.hasKenUnlocked();
            case "nen_ryu" -> data.hasRyuUnlocked();
            default -> false;
        };
    }

    @Override
    public int getMaxCooldownTicks() {
        return 0;
    }

    @Override
    public boolean isContinuous() {
        return true;
    }

    @Override
    public int getActiveTicks(HunterPlayerData data) {
        return this.isActive(data) ? 1 : 0;
    }

    @Override
    public boolean canUse(ServerPlayer player, HunterPlayerData data) {
        return this.isUnlocked(data)
                && (!player.hasEffect(HunterMobEffects.ZETSU.get()) || this.isActive(data))
                && this.getCurrentCooldown(data) <= 0
                && (this.isActive(data) || this.canActivateWithoutNen() || data.getCurrentStamina() > 0);
    }

    @Override
    public Component getUseFailureMessage(ServerPlayer player, HunterPlayerData data) {
        if (!this.isUnlocked(data)) {
            return Component.literal(this.displayName() + " is not unlocked yet.");
        }
        if (player.hasEffect(HunterMobEffects.ZETSU.get()) && !this.isActive(data)) {
            return Component.literal("Zetsu is suppressing your Nen.");
        }
        if (this.getCurrentCooldown(data) > 0) {
            return super.getUseFailureMessage(player, data);
        }
        if (!this.isActive(data) && !this.canActivateWithoutNen() && data.getCurrentStamina() <= 0) {
            return Component.literal("You need stamina to activate " + this.displayName() + ".");
        }
        return null;
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
    public void use(ServerPlayer player, HunterPlayerData data, Vec3 direction) {
        if (this.isActive(data)) {
            this.stop(player, data);
        } else {
            this.activate(player, data, direction);
        }
    }

    protected boolean canActivateWithoutNen() {
        return false;
    }

    public int getNenDrainPerTick(HunterPlayerData data) {
        return 0;
    }

    public int getNenRegenPerTick(HunterPlayerData data) {
        return 0;
    }

    public float getIncomingDamageReduction(HunterPlayerData data) {
        return 0.0F;
    }

    public float getOutgoingDamageBonus(HunterPlayerData data) {
        return 0.0F;
    }

    public float getArmorPierceBonus(HunterPlayerData data) {
        return 0.0F;
    }

    public float getKoDamageMultiplier(HunterPlayerData data, ServerPlayer player) {
        return 1.0F;
    }

    public void onKoStrikeResolved(HunterPlayerData data) {
    }

    public boolean hasVisibleBodyAura(HunterPlayerData data) {
        return false;
    }

    public boolean hasVisibleFistAura(HunterPlayerData data) {
        return false;
    }

    protected abstract void activate(ServerPlayer player, HunterPlayerData data, Vec3 direction);

    protected float getNenLevelScale(HunterPlayerData data) {
        if (data == null) {
            return 0.0F;
        }
        return Math.max(0.0F, Math.min(1.0F, (data.getNenLevel() - 1) / 9.0F));
    }

    protected float scaleByNenLevel(HunterPlayerData data, float minValue, float maxValue) {
        return minValue + ((maxValue - minValue) * getNenLevelScale(data));
    }

    public static void tickNen(ServerPlayer player, HunterPlayerData data) {
        int totalDrain = 0;
        if (data.hasNen()) {
            for (NenTechniqueAbility ability : HunterAbilities.NEN_TECHNIQUES) {
                totalDrain += ability.getNenDrainPerTick(data);
            }
        }

        if (totalDrain > 0) {
            data.resetNenRegenProgress();
            if (player.tickCount % SUSTAINED_DRAIN_INTERVAL_TICKS == 0) {
                data.consumeNen(totalDrain);
            }
            return;
        }

        if (data.tickAndHasStaminaRegenDelay()) {
            data.resetNenRegenProgress();
            return;
        }

        double regenPerTick = HunterAbilities.ZETSU.isActive(data)
                ? Math.max(PASSIVE_REGEN_PER_TICK, HunterAbilities.ZETSU.getNenRegenPerTick(data)) * 0.08D
                : Math.max(0.15D, data.getMaxStamina() / 20000.0D);
        if (regenPerTick <= 0.0D) {
            data.resetNenRegenProgress();
            return;
        }

        data.addNenRegenProgress(regenPerTick);
        while (data.consumeNenRegenStep()) {
            data.addNen(1);
        }
    }

    public static float getIncomingReduction(HunterPlayerData data) {
        float reduction = 0.0F;
        for (NenTechniqueAbility ability : HunterAbilities.NEN_TECHNIQUES) {
            reduction += ability.getIncomingDamageReduction(data);
        }
        return Math.min(0.75F, reduction);
    }

    public static float getOutgoingDamageBonusTotal(HunterPlayerData data) {
        float bonus = 0.0F;
        for (NenTechniqueAbility ability : HunterAbilities.NEN_TECHNIQUES) {
            bonus += ability.getOutgoingDamageBonus(data);
        }
        return bonus;
    }

    public static float getPassiveRenDamageBonus(HunterPlayerData data) {
        if (data == null || !data.hasNen() || data.isRenActive() || data.isKenActive()) {
            return 0.0F;
        }
        float scale = Math.max(0.0F, Math.min(1.0F, (data.getNenLevel() - 1) / 9.0F));
        return 10.0F + ((50.0F - 10.0F) * scale);
    }

    public static float getTotalArmorPierceBonus(HunterPlayerData data) {
        float bonus = 0.0F;
        for (NenTechniqueAbility ability : HunterAbilities.NEN_TECHNIQUES) {
            bonus += ability.getArmorPierceBonus(data);
        }
        return bonus;
    }

    public static float getKoStrikeMultiplier(HunterPlayerData data, ServerPlayer player) {
        return HunterAbilities.KO.getKoDamageMultiplier(data, player);
    }

    public static void resolveKoStrike(HunterPlayerData data) {
        HunterAbilities.KO.onKoStrikeResolved(data);
    }

    public static boolean canSeeNenAura(HunterPlayerData viewerData, HunterPlayerData targetData) {
        return viewerData != null
                && viewerData.hasGyoUnlocked()
                && targetData != null
                && !HunterAbilities.ZETSU.isActive(targetData)
                && (hasAnyVisibleBodyAura(targetData) || hasAnyVisibleFistAura(targetData));
    }

    public static boolean hasAnyVisibleBodyAura(HunterPlayerData data) {
        for (NenTechniqueAbility ability : HunterAbilities.NEN_TECHNIQUES) {
            if (ability.hasVisibleBodyAura(data)) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasAnyVisibleFistAura(HunterPlayerData data) {
        for (NenTechniqueAbility ability : HunterAbilities.NEN_TECHNIQUES) {
            if (ability.hasVisibleFistAura(data)) {
                return true;
            }
        }
        return false;
    }
}
