package com.huntercraft.huntercraft.abilities.ChainConjuration;

import com.huntercraft.huntercraft.abilities.SkillTreeCombatAbility;
import com.huntercraft.huntercraft.abilities.nenability.NenTechniqueAbility;
import com.huntercraft.huntercraft.animation.AnimationType;
import com.huntercraft.huntercraft.damage.HunterDamageSources;
import com.huntercraft.huntercraft.data.HunterPlayerData;
import com.huntercraft.huntercraft.effect.HunterMobEffects;
import com.huntercraft.huntercraft.entity.SmokeyChainProjectileEntity;
import com.huntercraft.huntercraft.progression.NenTechniqueSkillNode;
import com.huntercraft.huntercraft.progression.SkillNode;
import com.huntercraft.huntercraft.sound.HunterSoundEvents;
import com.huntercraft.huntercraft.util.HunterDataUtil;
import com.huntercraft.huntercraft.util.NenVowUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class DowsingChainAbility extends SkillTreeCombatAbility {
    private static final int PULL_COOLDOWN_TICKS = 20 * 7;
    private static final int PUNCH_COOLDOWN_TICKS = 20 * 15;
    private static final int CHAIN_TICKS = 20 * 14;
    private static final int PULL_TICKS = 46;
    private static final int PUNCH_TRIGGER_REMAINING = 10;
    private static final int NEN_COST = 150;
    private static final float HIT_DAMAGE = 6.0F;
    private static final float PUNCH_DAMAGE = 22.0F;
    private static final double MAX_CHAIN_RADIUS = 20.0D;
    private static final String PULLING_TAG = "HuntercraftDowsingChainPulling";

    public DowsingChainAbility() {
        super("dowsing_chain", "Dowsing Chain", "Throw a chain that binds a target to your range, then recast to yank them into a heavy punch.", "textures/gui/abilities/dowsing_chain.png", SkillNode.MARTIAL_ARTS, 0, com.huntercraft.huntercraft.abilities.AbilitySourceType.NEN, com.huntercraft.huntercraft.abilities.AbilitySourceType.SHARP);
    }

    @Override
    public boolean isUnlocked(HunterPlayerData data) {
        return data.hasUnlockedNenTechniqueNode(NenTechniqueSkillNode.CHAIN_NEN_CORE);
    }

    @Override
    public int getMaxCooldownTicks() {
        return PUNCH_COOLDOWN_TICKS;
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
        return data.isActiveAbility(this.id()) ? CHAIN_TICKS - data.getActiveAbilityTicksRemaining() : 0;
    }

    @Override
    public boolean canUse(ServerPlayer player, HunterPlayerData data) {
        if (data.isActiveAbility(this.id()) && player.getPersistentData().getBoolean(PULLING_TAG)) {
            return false;
        }
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
            return Component.literal("You need the Chain Nen technique to use Dowsing Chain.");
        }
        if (!data.hasStaminaForNenCost(NEN_COST)) {
            return this.createNenCostMessage(NEN_COST);
        }
        return null;
    }

    @Override
    public void use(ServerPlayer player, HunterPlayerData data, Vec3 direction) {
        if (data.isActiveAbility(this.id())) {
            LivingEntity target = resolveTarget((ServerLevel) player.level(), data.getActiveAbilityTargetUuid());
            if (target != null && target.isAlive()) {
                player.getPersistentData().putBoolean(PULLING_TAG, true);
                data.startActiveAbility(this.id(), PULL_TICKS, player.getLookAngle());
                data.setActiveAbilityTargetUuid(target.getUUID().toString());
                HunterDataUtil.sync(player);
            } else {
                stop(player, data);
            }
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
        data.triggerAnimation(AnimationType.DOWSING_CHAIN_SWING);
        this.startCooldown(data, PULL_COOLDOWN_TICKS);
        HunterDataUtil.sync(player);
    }

    @Override
    public void tick(ServerPlayer player, HunterPlayerData data) {
        if (!data.isActiveAbility(this.id()) || !(player.level() instanceof ServerLevel level)) {
            return;
        }
        LivingEntity target = resolveTarget(level, data.getActiveAbilityTargetUuid());
        if (target == null || !target.isAlive()) {
            stop(player, data);
            return;
        }

        if (player.getPersistentData().getBoolean(PULLING_TAG)) {
            tickPullPunch(player, data, target);
        } else {
            keepTargetInChainRadius(player, target);
            target.addEffect(new MobEffectInstance(HunterMobEffects.DOWSING_CHAINED.get(), 6, 0, false, true, true));
        }

        data.tickActiveAbility();
        if (!data.isActiveAbility(this.id())) {
            stop(player, data);
        }
    }

    @Override
    public void stop(ServerPlayer player, HunterPlayerData data) {
        if (!data.isActiveAbility(this.id())) {
            return;
        }
        LivingEntity target = player.level() instanceof ServerLevel level ? resolveTarget(level, data.getActiveAbilityTargetUuid()) : null;
        if (target != null) {
            target.removeEffect(HunterMobEffects.DOWSING_CHAINED.get());
        }
        player.getPersistentData().remove(PULLING_TAG);
        data.clearActiveAbility();
        HunterDataUtil.sync(player);
    }

    public void attachTarget(ServerPlayer player, HunterPlayerData data, LivingEntity target) {
        if (!target.hurt(player.damageSources().playerAttack(player), (HIT_DAMAGE + NenTechniqueAbility.getPassiveRenDamageBonus(data)) * NenVowUtil.getEffectiveness(player, data, this.id(), target))) {
            return;
        }
        target.addEffect(new MobEffectInstance(HunterMobEffects.DOWSING_CHAINED.get(), CHAIN_TICKS, 0, false, true, true));
        data.startActiveAbility(this.id(), CHAIN_TICKS, Vec3.ZERO);
        data.setActiveAbilityTargetUuid(target.getUUID().toString());
        player.getPersistentData().remove(PULLING_TAG);
        HunterDataUtil.sync(player);
    }

    private static void keepTargetInChainRadius(ServerPlayer player, LivingEntity target) {
        Vec3 offset = target.position().subtract(player.position());
        double distance = offset.length();
        if (distance <= MAX_CHAIN_RADIUS) {
            return;
        }
        Vec3 direction = offset.normalize();
        Vec3 clamped = player.position().add(direction.scale(MAX_CHAIN_RADIUS));
        Vec3 safeBottom = findSafeBottomPosition((ServerLevel) player.level(), target, new Vec3(clamped.x, target.getY(), clamped.z), target.position());
        target.teleportTo(safeBottom.x, safeBottom.y, safeBottom.z);
        Vec3 velocity = target.getDeltaMovement();
        double outward = velocity.dot(direction);
        if (outward > 0.0D) {
            velocity = velocity.subtract(direction.scale(outward));
        }
        Vec3 tug = player.position().subtract(target.position()).normalize().scale(0.18D);
        target.setDeltaMovement(velocity.x + tug.x, velocity.y, velocity.z + tug.z);
        target.hurtMarked = true;
        target.hasImpulse = true;
    }

    private static void tickPullPunch(ServerPlayer player, HunterPlayerData data, LivingEntity target) {
        int remaining = data.getActiveAbilityTicksRemaining();
        Vec3 forward = player.getLookAngle().normalize();
        Vec3 holdPoint = player.position().add(forward.scale(1.15D)).add(0.0D, 0.82D, 0.0D);
        if (remaining > PUNCH_TRIGGER_REMAINING + 10) {
            Vec3 towardPlayer = holdPoint.subtract(target.position());
            Vec3 pull = towardPlayer.normalize().scale(Math.min(1.7D, 0.55D + (towardPlayer.length() * 0.18D)));
            Vec3 nextBottom = target.position().add(pull.x, Math.max(-0.10D, pull.y * 0.42D), pull.z);
            if (canOccupy((ServerLevel) player.level(), target, nextBottom)) {
                target.setDeltaMovement(pull.x, Math.max(-0.10D, pull.y * 0.42D), pull.z);
            } else {
                target.setDeltaMovement(Vec3.ZERO);
                Vec3 safeBottom = findSafeBottomPosition((ServerLevel) player.level(), target, target.position(), player.position().add(forward.scale(1.35D)));
                target.teleportTo(safeBottom.x, safeBottom.y, safeBottom.z);
            }
        } else if (remaining > PUNCH_TRIGGER_REMAINING) {
            Vec3 desiredBottom = new Vec3(holdPoint.x, holdPoint.y - (target.getBbHeight() * 0.5D), holdPoint.z);
            Vec3 safeBottom = findSafeBottomPosition((ServerLevel) player.level(), target, desiredBottom, player.position().add(forward.scale(1.35D)));
            target.teleportTo(safeBottom.x, safeBottom.y, safeBottom.z);
            target.setDeltaMovement(Vec3.ZERO);
            HunterDataUtil.applyStun(target, player, 6);
        } else if (remaining == PUNCH_TRIGGER_REMAINING) {
            player.swing(InteractionHand.MAIN_HAND, true);
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(), HunterSoundEvents.PUNCH.get(), SoundSource.PLAYERS, 0.95F, 0.78F);
            target.hurt(HunterDamageSources.physical(player.level(), player), PUNCH_DAMAGE + NenTechniqueAbility.getPassiveRenDamageBonus(data));
            Vec3 knockback = target.position().subtract(player.position());
            if (knockback.horizontalDistanceSqr() <= 1.0E-4D) {
                knockback = forward;
            }
            knockback = new Vec3(knockback.x, 0.0D, knockback.z).normalize();
            target.removeEffect(HunterMobEffects.STUNNED.get());
            target.removeEffect(HunterMobEffects.DOWSING_CHAINED.get());
            target.setDeltaMovement(knockback.x * 3.0D, 0.43D, knockback.z * 3.0D);
            target.hurtMarked = true;
            target.hasImpulse = true;
            data.setAbilityCooldown("dowsing_chain", PUNCH_COOLDOWN_TICKS);
        }
    }

    private static Vec3 findSafeBottomPosition(ServerLevel level, LivingEntity target, Vec3 desiredBottom, Vec3 fallbackBottom) {
        if (canOccupy(level, target, desiredBottom)) {
            return desiredBottom;
        }
        Vec3[] offsets = {
                Vec3.ZERO,
                new Vec3(0.0D, 1.0D, 0.0D),
                new Vec3(0.0D, 2.0D, 0.0D),
                new Vec3(0.45D, 0.0D, 0.0D),
                new Vec3(-0.45D, 0.0D, 0.0D),
                new Vec3(0.0D, 0.0D, 0.45D),
                new Vec3(0.0D, 0.0D, -0.45D),
                new Vec3(0.75D, 0.0D, 0.75D),
                new Vec3(-0.75D, 0.0D, 0.75D),
                new Vec3(0.75D, 0.0D, -0.75D),
                new Vec3(-0.75D, 0.0D, -0.75D)
        };
        for (Vec3 offset : offsets) {
            Vec3 candidate = desiredBottom.add(offset);
            if (canOccupy(level, target, candidate)) {
                return candidate;
            }
        }
        if (canOccupy(level, target, fallbackBottom)) {
            return fallbackBottom;
        }
        return target.position();
    }

    private static boolean canOccupy(ServerLevel level, LivingEntity target, Vec3 bottomPosition) {
        AABB movedBox = target.getBoundingBox().move(bottomPosition.subtract(target.position()));
        return level.noCollision(target, movedBox);
    }

    private static LivingEntity resolveTarget(ServerLevel level, String uuidString) {
        if (uuidString == null || uuidString.isBlank()) {
            return null;
        }
        try {
            return level.getEntity(UUID.fromString(uuidString)) instanceof LivingEntity living ? living : null;
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }
}
