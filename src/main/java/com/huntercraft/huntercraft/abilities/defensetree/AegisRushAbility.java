package com.huntercraft.huntercraft.abilities.defensetree;

import com.huntercraft.huntercraft.abilities.SkillTreeCombatAbility;
import com.huntercraft.huntercraft.animation.AnimationType;
import com.huntercraft.huntercraft.damage.HunterDamageSources;
import com.huntercraft.huntercraft.data.HunterPlayerData;
import com.huntercraft.huntercraft.entity.AegisSlamEffectEntity;
import com.huntercraft.huntercraft.progression.SkillNode;
import com.huntercraft.huntercraft.util.HunterDataUtil;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class AegisRushAbility extends SkillTreeCombatAbility {
    private static final int CHARGE_TICKS = 6;
    private static final int ACTIVE_TICKS = 28;
    private static final int SLAM_ELAPSED_TICK = 15;
    private static final float LAUNCH_DAMAGE = 10.0F;
    private static final float SLAM_DAMAGE = 34.0F;
    private static final double TARGET_RANGE = 10.0D;
    private static final double SLAM_RADIUS = 3.35D;
    private static final int AEGIS_COLOR = 0xDDEEFF;

    public AegisRushAbility() {
        super("aegis_rush", "Aegis Rush", "Snap into range and layer fast defensive slashes around the target in front of you.", "textures/gui/abilities/aegis_rush.png", SkillNode.DEFENSE, 35);
    }

    @Override
    public int getMaxCooldownTicks() {
        return 150;
    }

    @Override
    public int getChargeTicks(HunterPlayerData data) {
        return data.isChargingAbility(this.id()) ? CHARGE_TICKS - data.getChargeTicksRemaining() : 0;
    }

    @Override
    public void use(ServerPlayer player, HunterPlayerData data, Vec3 direction) {
        if (data.isActiveAbility(this.id()) || data.isChargingAbility(this.id())) {
            return;
        }
        Vec3 forward = direction.lengthSqr() > 1.0E-4D
                ? new Vec3(direction.x, 0.0D, direction.z).normalize()
                : player.getLookAngle().multiply(1.0D, 0.0D, 1.0D).normalize();
        if (forward.lengthSqr() < 1.0E-4D) {
            return;
        }

        data.startChargingAbility(this.id(), CHARGE_TICKS, forward);
        data.triggerAnimation(AnimationType.AEGIS_RUSH);
        HunterDataUtil.sync(player);
    }

    private void beginRush(ServerPlayer player, HunterPlayerData data, Vec3 forward) {
        LivingEntity target = findNearestTarget(player);
        if (target == null) {
            this.startCooldown(data, 10);
            HunterDataUtil.sync(player);
            return;
        }

        data.startActiveAbility(this.id(), ACTIVE_TICKS, forward);
        data.setActiveAbilityTargetUuid(target.getUUID().toString());
        data.triggerAnimation(AnimationType.AEGIS_RUSH);
        playSlashReleaseSound(player, 1.05F);
        startLauncher(player, target, forward);
    }

    @Override
    public void tick(ServerPlayer player, HunterPlayerData data) {
        if (data.isChargingAbility(this.id())) {
            player.setDeltaMovement(Vec3.ZERO);
            player.hasImpulse = true;
            data.tickChargingAbility();
            if (data.getChargeTicksRemaining() > 0) {
                return;
            }
            Vec3 forward = data.getChargeDirection();
            if (forward.lengthSqr() < 1.0E-4D) {
                forward = player.getLookAngle().multiply(1.0D, 0.0D, 1.0D).normalize();
            }
            data.clearChargingAbility();
            beginRush(player, data, forward);
            return;
        }

        if (!data.isActiveAbility(this.id())) {
            return;
        }

        LivingEntity target = resolveTarget(player, data.getActiveAbilityTargetUuid());
        if (target == null || !target.isAlive()) {
            data.clearActiveAbility();
            this.startCooldown(data, this.getMaxCooldownTicks());
            HunterDataUtil.sync(player);
            return;
        }

        Vec3 forward = data.getActiveAbilityDirection();
        if (forward.lengthSqr() < 1.0E-4D) {
            Vec3 towardTarget = target.position().subtract(player.position());
            forward = towardTarget.lengthSqr() > 1.0E-4D
                    ? new Vec3(towardTarget.x, 0.0D, towardTarget.z).normalize()
                    : player.getLookAngle().multiply(1.0D, 0.0D, 1.0D).normalize();
        }

        player.setDeltaMovement(Vec3.ZERO);
        player.hasImpulse = true;

        int remaining = data.getActiveAbilityTicksRemaining();
        int elapsed = ACTIVE_TICKS - remaining;
        if (elapsed < SLAM_ELAPSED_TICK) {
            juggleTarget(player, target, forward, elapsed);
        } else if (elapsed == SLAM_ELAPSED_TICK) {
            slamTarget(player, target, forward);
        } else {
            player.lookAt(EntityAnchorArgument.Anchor.EYES, target.getEyePosition());
        }

        data.tickActiveAbility();
        if (data.getActiveAbilityTicksRemaining() <= 0) {
            data.clearActiveAbility();
            player.setDeltaMovement(Vec3.ZERO);
            this.startCooldown(data, this.getMaxCooldownTicks());
            HunterDataUtil.sync(player);
        }
    }

    private void startLauncher(ServerPlayer player, LivingEntity target, Vec3 forward) {
        Vec3 targetCenter = target.position();
        Vec3 towardTarget = targetCenter.subtract(player.position());
        Vec3 attackForward = towardTarget.lengthSqr() > 1.0E-4D
                ? new Vec3(towardTarget.x, 0.0D, towardTarget.z).normalize()
                : forward;
        if (attackForward.lengthSqr() < 1.0E-4D) {
            attackForward = player.getLookAngle().multiply(1.0D, 0.0D, 1.0D).normalize();
        }

        Vec3 playerPos = targetCenter.subtract(attackForward.scale(0.74D));
        player.teleportTo(playerPos.x, target.getY(), playerPos.z);
        player.lookAt(EntityAnchorArgument.Anchor.EYES, target.getEyePosition());

        target.invulnerableTime = 0;
        target.hurt(HunterDamageSources.weapon(player.level(), player), this.getWeaponScaledDamage(player, LAUNCH_DAMAGE));
        target.invulnerableTime = 0;
        target.setDeltaMovement(attackForward.x * 0.08D, 1.22D, attackForward.z * 0.08D);
        target.hurtMarked = true;

        if (player.level() instanceof ServerLevel serverLevel) {
            Vec3 effectPos = target.position().add(0.0D, 0.2D, 0.0D);
            AegisSlamEffectEntity.spawn(serverLevel, effectPos, player.getYRot(), 0.72F, 12, AEGIS_COLOR);
            serverLevel.sendParticles(ParticleTypes.CLOUD, effectPos.x, effectPos.y, effectPos.z, 18, 0.35D, 0.08D, 0.35D, 0.04D);
            serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK, effectPos.x, effectPos.y + 0.45D, effectPos.z, 14, 0.35D, 0.35D, 0.35D, 0.08D);
        }

        player.setDeltaMovement(Vec3.ZERO);
        player.hurtMarked = true;
    }

    private void juggleTarget(ServerPlayer player, LivingEntity target, Vec3 forward, int elapsed) {
        Vec3 targetPos = target.position();
        Vec3 hoverPos = targetPos.add(0.0D, 2.55D + Math.sin(elapsed * 0.45D) * 0.08D, 0.0D);
        Vec3 playerPos = targetPos.subtract(forward.scale(0.86D)).add(0.0D, Math.min(1.2D, elapsed * 0.08D), 0.0D);
        player.teleportTo(playerPos.x, playerPos.y, playerPos.z);
        player.lookAt(EntityAnchorArgument.Anchor.EYES, target.getEyePosition());
        if (elapsed > 5) {
            target.setDeltaMovement(0.0D, 0.08D, 0.0D);
            target.hurtMarked = true;
        }
        if (player.level() instanceof ServerLevel serverLevel && elapsed % 3 == 0) {
            AegisSlamEffectEntity.spawn(serverLevel, hoverPos, player.getYRot(), 0.46F, 8, AEGIS_COLOR);
        }
    }

    private void slamTarget(ServerPlayer player, LivingEntity target, Vec3 forward) {
        Vec3 slamCenter = target.position();
        Vec3 playerPos = slamCenter.subtract(forward.scale(0.82D)).add(0.0D, 0.35D, 0.0D);
        player.teleportTo(playerPos.x, playerPos.y, playerPos.z);
        player.lookAt(EntityAnchorArgument.Anchor.EYES, target.getEyePosition());

        target.invulnerableTime = 0;
        target.hurt(HunterDamageSources.weapon(player.level(), player), this.getWeaponScaledDamage(player, SLAM_DAMAGE));
        target.invulnerableTime = 0;
        target.setDeltaMovement(forward.x * 0.14D, -1.65D, forward.z * 0.14D);
        target.hurtMarked = true;

        AABB hitBox = new AABB(
                slamCenter.x - SLAM_RADIUS,
                slamCenter.y - 1.4D,
                slamCenter.z - SLAM_RADIUS,
                slamCenter.x + SLAM_RADIUS,
                slamCenter.y + 1.9D,
                slamCenter.z + SLAM_RADIUS
        );
        for (LivingEntity victim : player.level().getEntitiesOfClass(LivingEntity.class, hitBox, entity -> entity != player && entity.isAlive())) {
            if (victim == target) {
                continue;
            }
            victim.invulnerableTime = 0;
            float damage = this.getWeaponScaledDamage(player, SLAM_DAMAGE * 0.42F);
            victim.hurt(HunterDamageSources.weapon(player.level(), player), damage);
            victim.invulnerableTime = 0;
            victim.knockback(0.42F, -forward.x, -forward.z);
            victim.setDeltaMovement(victim.getDeltaMovement().x, -0.65D, victim.getDeltaMovement().z);
        }

        if (player.level() instanceof ServerLevel serverLevel) {
            AegisSlamEffectEntity.spawn(serverLevel, slamCenter.add(0.0D, -0.4D, 0.0D), player.getYRot(), 1.22F, 18, AEGIS_COLOR);
            serverLevel.sendParticles(ParticleTypes.EXPLOSION, slamCenter.x, slamCenter.y - 0.6D, slamCenter.z, 2, 0.35D, 0.02D, 0.35D, 0.0D);
            serverLevel.sendParticles(ParticleTypes.CLOUD, slamCenter.x, slamCenter.y - 0.7D, slamCenter.z, 42, 1.2D, 0.08D, 1.2D, 0.08D);
            serverLevel.sendParticles(ParticleTypes.CRIT, slamCenter.x, slamCenter.y + 0.2D, slamCenter.z, 28, 0.75D, 0.35D, 0.75D, 0.06D);
        }

        player.setDeltaMovement(Vec3.ZERO);
        player.hurtMarked = true;
    }

    private LivingEntity findTarget(ServerPlayer player, Vec3 forward) {
        Vec3 eye = player.getEyePosition();
        Vec3 end = eye.add(forward.scale(TARGET_RANGE));
        AABB searchBox = player.getBoundingBox().expandTowards(forward.scale(TARGET_RANGE)).inflate(1.5D);
        LivingEntity closest = null;
        double closestDistance = TARGET_RANGE * TARGET_RANGE;
        for (LivingEntity target : player.level().getEntitiesOfClass(LivingEntity.class, searchBox, entity -> entity != player && entity.isAlive())) {
            Vec3 hit = target.getBoundingBox().inflate(0.35D).clip(eye, end).orElse(null);
            if (hit == null) {
                continue;
            }
            double distance = eye.distanceToSqr(hit);
            if (distance < closestDistance) {
                closestDistance = distance;
                closest = target;
            }
        }
        return closest;
    }

    private LivingEntity findNearestTarget(ServerPlayer player) {
        AABB searchBox = player.getBoundingBox().inflate(TARGET_RANGE, 2.4D, TARGET_RANGE);
        LivingEntity closest = null;
        double closestDistance = TARGET_RANGE * TARGET_RANGE;
        for (LivingEntity target : player.level().getEntitiesOfClass(LivingEntity.class, searchBox, entity -> entity != player && entity.isAlive())) {
            double distance = target.distanceToSqr(player);
            if (distance < closestDistance && player.hasLineOfSight(target)) {
                closestDistance = distance;
                closest = target;
            }
        }
        return closest;
    }

    private LivingEntity resolveTarget(ServerPlayer player, String uuidString) {
        if (!(player.level() instanceof ServerLevel serverLevel) || uuidString == null || uuidString.isBlank()) {
            return null;
        }
        try {
            return serverLevel.getEntity(UUID.fromString(uuidString)) instanceof LivingEntity living ? living : null;
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }
}
