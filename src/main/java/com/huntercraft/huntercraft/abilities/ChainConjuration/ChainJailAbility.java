package com.huntercraft.huntercraft.abilities.ChainConjuration;

import com.huntercraft.huntercraft.abilities.SkillTreeCombatAbility;
import com.huntercraft.huntercraft.data.HunterPlayerData;
import com.huntercraft.huntercraft.effect.HunterMobEffects;
import com.huntercraft.huntercraft.entity.SmokeyChainProjectileEntity;
import com.huntercraft.huntercraft.progression.NenTechniqueSkillNode;
import com.huntercraft.huntercraft.progression.SkillNode;
import com.huntercraft.huntercraft.util.HunterDataUtil;
import com.huntercraft.huntercraft.util.NenVowUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class ChainJailAbility extends SkillTreeCombatAbility {
    public static final float ESCAPE_GREEN_START = 0.455F;
    public static final float ESCAPE_GREEN_END = 0.545F;
    private static final int COOLDOWN_TICKS = 20 * 12;
    private static final int ACTIVE_TICKS = 20 * 60 * 30;
    private static final int EFFECT_REFRESH_TICKS = 35;
    private static final int NEN_COST = 400;
    private static final String JAIL_OWNER_TAG = "HunterChainJailOwner";
    private static final String JAIL_BREAKS_TAG = "HunterChainJailBreaks";
    private static final String JAIL_VOW_LOCKED_TAG = "HunterChainJailVowLocked";

    public ChainJailAbility() {
        super("chain_jail", "Chain Jail", "Launch a restraining chain that stuns the target until they break free.", "textures/gui/abilities/chain_jail.png", SkillNode.MARTIAL_ARTS, 0, com.huntercraft.huntercraft.abilities.AbilitySourceType.NEN, com.huntercraft.huntercraft.abilities.AbilitySourceType.SHARP);
    }

    @Override
    public boolean isUnlocked(HunterPlayerData data) {
        return data.hasUnlockedNenTechniqueNode(NenTechniqueSkillNode.CHAIN_NEN_JAIL);
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
    protected boolean requiresEmptyMainHand() {
        return false;
    }

    @Override
    public boolean isContinuous() {
        return true;
    }

    @Override
    public int getActiveTicks(HunterPlayerData data) {
        return data.isActiveAbility(this.id()) ? ACTIVE_TICKS - data.getActiveAbilityTicksRemaining() : 0;
    }

    @Override
    public boolean canUse(ServerPlayer player, HunterPlayerData data) {
        return super.canUse(player, data)
                && data.hasChainTechnique()
                && data.hasStaminaForNenCost(NEN_COST);
    }

    @Override
    public Component getUseFailureMessage(ServerPlayer player, HunterPlayerData data) {
        Component baseMessage = super.getUseFailureMessage(player, data);
        if (baseMessage != null) {
            return baseMessage;
        }
        if (!data.hasChainTechnique()) {
            return Component.literal("You need the Chain Nen technique to use Chain Jail.");
        }
        if (!data.hasStaminaForNenCost(NEN_COST)) {
            return this.createNenCostMessage(NEN_COST);
        }
        return null;
    }

    @Override
    public void use(ServerPlayer player, HunterPlayerData data, Vec3 direction) {
        if (data.isActiveAbility(this.id())) {
            stop(player, data);
            this.startCooldown(data, COOLDOWN_TICKS);
            HunterDataUtil.sync(player);
            return;
        }
        if (!(player.level() instanceof ServerLevel serverLevel) || !data.consumeNen(NEN_COST)) {
            return;
        }
        Vec3 launch = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : player.getLookAngle().normalize();
        SmokeyChainProjectileEntity projectile = SmokeyChainProjectileEntity.create(serverLevel, player, launch, this.id());
        if (projectile == null) {
            data.addStamina(data.getReducedNenStaminaCost(NEN_COST));
            return;
        }
        serverLevel.addFreshEntity(projectile);
        this.startCooldown(data, COOLDOWN_TICKS);
        HunterDataUtil.sync(player);
    }

    @Override
    public void tick(ServerPlayer player, HunterPlayerData data) {
        if (!data.isActiveAbility(this.id())) {
            return;
        }
        data.tickActiveAbility();
        LivingEntity target = resolveTarget(player, data);
        if (target == null || !target.isAlive()) {
            stop(player, data);
            return;
        }
        refreshJailEffects(player, data, target);
        if (!data.isActiveAbility(this.id())) {
            stop(player, data);
        }
    }

    @Override
    public void stop(ServerPlayer player, HunterPlayerData data) {
        if (!data.isActiveAbility(this.id())) {
            return;
        }
        LivingEntity target = resolveTarget(player, data);
        if (target != null) {
            releaseTarget(player, target);
        }
        data.clearActiveAbility();
        HunterDataUtil.sync(player);
    }

    public void attachTarget(ServerPlayer player, HunterPlayerData data, LivingEntity target) {
        data.startActiveAbility(this.id(), ACTIVE_TICKS, Vec3.ZERO);
        data.setActiveAbilityTargetUuid(target.getUUID().toString());
        boolean vowLocked = NenVowUtil.matchesRequiredVow(player, data, this.id(), target);
        int breaksNeeded = data.isEmperorTimeActive() ? 2 : 1;
        target.getPersistentData().putUUID(JAIL_OWNER_TAG, player.getUUID());
        target.getPersistentData().putInt(JAIL_BREAKS_TAG, breaksNeeded);
        target.getPersistentData().putBoolean(JAIL_VOW_LOCKED_TAG, vowLocked);
        refreshJailEffects(player, data, target);
        HunterDataUtil.sync(player);
    }

    public static boolean handleEscapeInput(ServerPlayer target) {
        return handleEscapeInput(target, target.tickCount);
    }

    public static boolean handleEscapeInput(ServerPlayer target, int inputTick) {
        MobEffectInstance effect = target.getEffect(HunterMobEffects.CHAIN_JAIL.get());
        if (effect == null) {
            return false;
        }
        if (target.getPersistentData().getBoolean(JAIL_VOW_LOCKED_TAG)) {
            target.sendSystemMessage(Component.literal("A vow-bound Chain Jail cannot be escaped."));
            return true;
        }
        if (!isEscapeCursorInGreen(inputTick)) {
            return true;
        }
        int remaining = Math.max(1, target.getPersistentData().getInt(JAIL_BREAKS_TAG)) - 1;
        target.getPersistentData().putInt(JAIL_BREAKS_TAG, remaining);
        if (remaining > 0) {
            target.addEffect(new MobEffectInstance(HunterMobEffects.CHAIN_JAIL.get(), EFFECT_REFRESH_TICKS, 0, false, true, true));
            target.sendSystemMessage(Component.literal("Chain Jail weakened. Break it once more."));
            return true;
        }
        UUID ownerId = target.getPersistentData().hasUUID(JAIL_OWNER_TAG) ? target.getPersistentData().getUUID(JAIL_OWNER_TAG) : null;
        if (ownerId != null && target.serverLevel().getPlayerByUUID(ownerId) instanceof ServerPlayer owner) {
            HunterPlayerData ownerData = HunterDataUtil.get(owner);
            if ("chain_jail".equals(ownerData.getActiveAbilityId())) {
                ownerData.clearActiveAbility();
                HunterDataUtil.sync(owner);
            }
        }
        clearJailTags(target);
        target.removeEffect(HunterMobEffects.CHAIN_JAIL.get());
        target.removeEffect(HunterMobEffects.DOWSING_CHAINED.get());
        target.removeEffect(HunterMobEffects.STUNNED.get());
        target.sendSystemMessage(Component.literal("You broke out of Chain Jail."));
        return true;
    }

    public static float getEscapeCursorProgress(int tickCount) {
        int period = 40;
        int value = Math.floorMod(tickCount, period);
        float phase = value / (float) (period - 1);
        return phase <= 0.5F ? phase * 2.0F : (1.0F - phase) * 2.0F;
    }

    private static boolean isEscapeCursorInGreen(int tickCount) {
        float progress = getEscapeCursorProgress(tickCount);
        return progress >= ESCAPE_GREEN_START && progress <= ESCAPE_GREEN_END;
    }

    private static void clearJailTags(LivingEntity target) {
        target.getPersistentData().remove(JAIL_OWNER_TAG);
        target.getPersistentData().remove(JAIL_BREAKS_TAG);
        target.getPersistentData().remove(JAIL_VOW_LOCKED_TAG);
    }

    private void refreshJailEffects(ServerPlayer player, HunterPlayerData data, LivingEntity target) {
        int amplifier = target.getPersistentData().getBoolean(JAIL_VOW_LOCKED_TAG)
                ? 2
                : Math.max(0, Math.min(1, target.getPersistentData().getInt(JAIL_BREAKS_TAG) - 1));
        target.addEffect(new MobEffectInstance(HunterMobEffects.STUNNED.get(), EFFECT_REFRESH_TICKS, 0, false, false, true));
        target.addEffect(new MobEffectInstance(HunterMobEffects.CHAIN_JAIL.get(), EFFECT_REFRESH_TICKS, amplifier, false, true, true));
        target.addEffect(new MobEffectInstance(HunterMobEffects.DOWSING_CHAINED.get(), EFFECT_REFRESH_TICKS, 1, false, true, true));
    }

    private void releaseTarget(ServerPlayer player, LivingEntity target) {
        clearJailTags(target);
        target.removeEffect(HunterMobEffects.CHAIN_JAIL.get());
        target.removeEffect(HunterMobEffects.DOWSING_CHAINED.get());
        target.removeEffect(HunterMobEffects.STUNNED.get());
    }

    private LivingEntity resolveTarget(ServerPlayer player, HunterPlayerData data) {
        if (!(player.level() instanceof ServerLevel serverLevel) || data.getActiveAbilityTargetUuid().isBlank()) {
            return null;
        }
        try {
            Entity entity = serverLevel.getEntity(UUID.fromString(data.getActiveAbilityTargetUuid()));
            return entity instanceof LivingEntity living ? living : null;
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }
}
