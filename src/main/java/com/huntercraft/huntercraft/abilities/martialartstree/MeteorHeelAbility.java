package com.huntercraft.huntercraft.abilities.martialartstree;

import com.huntercraft.huntercraft.abilities.HunterAbilities;
import com.huntercraft.huntercraft.abilities.SkillTreeCombatAbility;
import com.huntercraft.huntercraft.abilities.defensetree.SkybreakerDiveAbility;
import com.huntercraft.huntercraft.animation.AnimationType;
import com.huntercraft.huntercraft.damage.HunterDamageSources;
import com.huntercraft.huntercraft.data.HunterPlayerData;
import com.huntercraft.huntercraft.entity.AegisSlamEffectEntity;
import com.huntercraft.huntercraft.progression.SkillNode;
import com.huntercraft.huntercraft.util.HunterDataUtil;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class MeteorHeelAbility extends SkillTreeCombatAbility {
    private static final int CHARGE_TICKS = 10;
    private static final int ACTIVE_TICKS = 44;
    private static final int ASCENT_TICKS = 10;
    private static final int SECOND_SHOCKWAVE_DELAY_TICKS = 20;
    private static final float FIRST_IMPACT_DAMAGE = 24.0F;
    private static final float SECOND_IMPACT_DAMAGE = 32.0F;
    private static final float ALT_PUNCH_DAMAGE = 6.0F;
    private static final double FIRST_IMPACT_RADIUS = 4.25D;
    private static final double SECOND_IMPACT_RADIUS = 6.25D;
    private static final double DESCENT_VERTICAL_SPEED = -4.75D;
    private static final double DESCENT_FORWARD_SPEED = 0.22D;
    private static final double LAUNCH_FORWARD_SPEED = 0.72D;
    private static final double LAUNCH_VERTICAL_SPEED = 3.3D;
    private static final int FIRST_STUN_TICKS = 10;
    private static final double SECOND_KNOCKBACK = 2.8D;
    private static final int METEOR_COLOR = 0xFF3428;

    public MeteorHeelAbility() {
        super("meteor_heel", "Meteor Heel", "Leap up and crash down with a driving axe kick. During an air grab, smash the target down first and dive after them.", "textures/gui/abilities/meteor_heel.png", SkillNode.MARTIAL_ARTS, 20);
    }

    @Override
    public int getMaxCooldownTicks() {
        return 240;
    }

    @Override
    public int getChargeTicks(HunterPlayerData data) {
        return data.isChargingAbility(this.id()) ? CHARGE_TICKS - data.getChargeTicksRemaining() : 0;
    }

    @Override
    public void use(ServerPlayer player, HunterPlayerData data, Vec3 direction) {
        if (data.isChargingAbility(this.id()) || data.isActiveAbility(this.id())) {
            return;
        }
        Vec3 forward = MartialArtsGrabHelper.getHorizontalForward(player, direction);
        data.startChargingAbility(this.id(), CHARGE_TICKS, forward);
        data.triggerAnimation(AnimationType.MARTIAL_METEOR_HEEL);
        HunterDataUtil.sync(player);
    }

    private void beginMeteor(ServerPlayer player, HunterPlayerData data, Vec3 forward) {
        LivingEntity grabbedTarget = MartialArtsGrabHelper.resolveGrabTarget(player, data);
        if (grabbedTarget != null && grabbedTarget.isAlive()) {
            grabbedTarget.hurt(HunterDamageSources.physical(player.level(), player), ALT_PUNCH_DAMAGE);
            MartialArtsGrabHelper.slamTarget(player, grabbedTarget, 0.08D, -1.25D, 14);
            MartialArtsGrabHelper.clearGrab(data);
            player.teleportTo(grabbedTarget.getX(), grabbedTarget.getY() + 2.8D, grabbedTarget.getZ());
            player.setDeltaMovement(0.0D, -1.25D, 0.0D);
        } else {
            launchMeteorHeel(player, data, forward, forward);
        }
        player.hurtMarked = true;
        player.hasImpulse = true;
        player.fallDistance = 0.0F;
        data.setAirLaunchFallProtection(true);
        data.startActiveAbility(this.id(), ACTIVE_TICKS, forward);
        data.triggerAnimation(AnimationType.MARTIAL_METEOR_HEEL);
        playDashReleaseSound(player, 0.72F);
    }

    @Override
    public void tick(ServerPlayer player, HunterPlayerData data) {
        if (data.isChargingAbility(this.id())) {
            player.setDeltaMovement(Vec3.ZERO);
            player.hasImpulse = true;
            data.tickChargingAbility();
            if (data.getChargeTicksRemaining() <= 0) {
                Vec3 forward = data.getChargeDirection();
                data.clearChargingAbility();
                beginMeteor(player, data, forward);
            }
            return;
        }
        if (!data.isActiveAbility(this.id())) {
            return;
        }

        int remaining = data.getActiveAbilityTicksRemaining();
        Vec3 forward = data.getActiveAbilityDirection();
        if (data.getActiveAbilityTargetUuid().startsWith("impact:")) {
            tickSecondShockwaveDelay(player, data, remaining);
            return;
        }
        int elapsed = ACTIVE_TICKS - remaining;
        if (elapsed >= ASCENT_TICKS || player.getDeltaMovement().y <= 0.05D) {
            player.setDeltaMovement(forward.x * DESCENT_FORWARD_SPEED, DESCENT_VERTICAL_SPEED, forward.z * DESCENT_FORWARD_SPEED);
            player.hasImpulse = true;
            player.hurtMarked = true;
        }
        player.fallDistance = 0.0F;
        spawnDiveTrail(player);
        if (player.onGround()) {
            impact(player);
            data.startActiveAbility(this.id(), SECOND_SHOCKWAVE_DELAY_TICKS, forward);
            data.setActiveAbilityTargetUuid("impact:" + player.getX() + "," + player.getY() + "," + player.getZ());
            data.triggerAnimation(AnimationType.MARTIAL_METEOR_HEEL_IMPACT);
            HunterDataUtil.sync(player);
            return;
        }
        data.tickActiveAbility();
        if (data.getActiveAbilityTicksRemaining() <= 0) {
            finish(player, data);
        }
    }

    private void impact(ServerPlayer player) {
        Vec3 center = player.position().add(0.0D, 0.25D, 0.0D);
        HunterPlayerData data = HunterDataUtil.get(player);
        data.triggerAnimation(AnimationType.MARTIAL_METEOR_HEEL_IMPACT);
        playGroundSmashReleaseSound(player, 0.82F);
        AABB hitBox = new AABB(center.x - FIRST_IMPACT_RADIUS, center.y - 0.8D, center.z - FIRST_IMPACT_RADIUS, center.x + FIRST_IMPACT_RADIUS, center.y + 1.2D, center.z + FIRST_IMPACT_RADIUS);
        for (LivingEntity target : player.level().getEntitiesOfClass(LivingEntity.class, hitBox, entity -> entity != player && entity.isAlive())) {
            target.hurt(HunterDamageSources.physical(player.level(), player), FIRST_IMPACT_DAMAGE);
            HunterDataUtil.applyStun(target, player, FIRST_STUN_TICKS);
            target.setDeltaMovement(target.getDeltaMovement().x, -0.35D, target.getDeltaMovement().z);
            target.hurtMarked = true;
        }
        if (player.level() instanceof ServerLevel serverLevel) {
            AegisSlamEffectEntity.spawn(serverLevel, center.add(0.0D, -0.2D, 0.0D), player.getYRot(), 1.15F, 18, METEOR_COLOR);
            SkybreakerDiveAbility.launchImpactBlocks(serverLevel, center);
            serverLevel.sendParticles(ParticleTypes.EXPLOSION, center.x, center.y, center.z, 3, 0.16D, 0.0D, 0.16D, 0.0D);
            serverLevel.sendParticles(ParticleTypes.CLOUD, center.x, center.y, center.z, 34, 1.2D, 0.12D, 1.2D, 0.07D);
            serverLevel.sendParticles(ParticleTypes.CRIT, center.x, center.y + 0.35D, center.z, 32, 0.9D, 0.28D, 0.9D, 0.08D);
            serverLevel.sendParticles(ParticleTypes.SWEEP_ATTACK, center.x, center.y + 0.35D, center.z, 7, 1.05D, 0.1D, 1.05D, 0.0D);
            serverLevel.sendParticles(ParticleTypes.FLAME, center.x, center.y + 0.25D, center.z, 18, 0.85D, 0.18D, 0.85D, 0.06D);
        }
        player.setDeltaMovement(Vec3.ZERO);
        player.fallDistance = 0.0F;
    }

    private void tickSecondShockwaveDelay(ServerPlayer player, HunterPlayerData data, int remaining) {
        Vec3 center = parseImpactCenter(data.getActiveAbilityTargetUuid(), player.position());
        player.setDeltaMovement(Vec3.ZERO);
        player.hasImpulse = true;
        player.hurtMarked = true;
        player.fallDistance = 0.0F;
        if (player.level() instanceof ServerLevel serverLevel && remaining % 4 == 0) {
            serverLevel.sendParticles(ParticleTypes.SMOKE, center.x, center.y + 0.15D, center.z, 8, 0.8D, 0.05D, 0.8D, 0.02D);
            serverLevel.sendParticles(ParticleTypes.FLAME, center.x, center.y + 0.12D, center.z, 5, 0.65D, 0.04D, 0.65D, 0.025D);
        }
        data.tickActiveAbility();
        if (data.getActiveAbilityTicksRemaining() <= 0) {
            secondShockwave(player, center);
            finish(player, data);
        }
    }

    private void secondShockwave(ServerPlayer player, Vec3 center) {
        AABB hitBox = new AABB(center.x - SECOND_IMPACT_RADIUS, center.y - 0.8D, center.z - SECOND_IMPACT_RADIUS, center.x + SECOND_IMPACT_RADIUS, center.y + 1.8D, center.z + SECOND_IMPACT_RADIUS);
        for (LivingEntity target : player.level().getEntitiesOfClass(LivingEntity.class, hitBox, entity -> entity != player && entity.isAlive())) {
            target.hurt(HunterDamageSources.physical(player.level(), player), SECOND_IMPACT_DAMAGE);
            Vec3 knock = target.position().subtract(center).multiply(1.0D, 0.0D, 1.0D);
            if (knock.lengthSqr() < 1.0E-4D) {
                knock = player.getLookAngle().multiply(1.0D, 0.0D, 1.0D);
            }
            if (knock.lengthSqr() < 1.0E-4D) {
                knock = new Vec3(0.0D, 0.0D, 1.0D);
            }
            knock = knock.normalize();
            target.removeEffect(com.huntercraft.huntercraft.effect.HunterMobEffects.STUNNED.get());
            target.setDeltaMovement(knock.x * SECOND_KNOCKBACK, 0.75D, knock.z * SECOND_KNOCKBACK);
            target.hasImpulse = true;
            target.hurtMarked = true;
        }
        if (player.level() instanceof ServerLevel serverLevel) {
            AegisSlamEffectEntity.spawn(serverLevel, center.add(0.0D, -0.15D, 0.0D), player.getYRot(), 1.65F, 24, METEOR_COLOR);
            SkybreakerDiveAbility.launchImpactBlocks(serverLevel, center.add(0.0D, 0.3D, 0.0D));
            serverLevel.sendParticles(ParticleTypes.EXPLOSION, center.x, center.y + 0.1D, center.z, 5, 0.45D, 0.0D, 0.45D, 0.0D);
            serverLevel.sendParticles(ParticleTypes.CLOUD, center.x, center.y + 0.05D, center.z, 68, 2.2D, 0.18D, 2.2D, 0.1D);
            serverLevel.sendParticles(ParticleTypes.SWEEP_ATTACK, center.x, center.y + 0.48D, center.z, 16, 2.0D, 0.14D, 2.0D, 0.0D);
            serverLevel.sendParticles(ParticleTypes.FLAME, center.x, center.y + 0.28D, center.z, 34, 1.65D, 0.18D, 1.65D, 0.08D);
            serverLevel.sendParticles(ParticleTypes.CRIT, center.x, center.y + 0.55D, center.z, 44, 1.55D, 0.45D, 1.55D, 0.11D);
        }
    }

    private Vec3 parseImpactCenter(String targetUuid, Vec3 fallback) {
        if (targetUuid == null || !targetUuid.startsWith("impact:")) {
            return fallback;
        }
        String[] parts = targetUuid.substring("impact:".length()).split(",");
        if (parts.length != 3) {
            return fallback;
        }
        try {
            return new Vec3(Double.parseDouble(parts[0]), Double.parseDouble(parts[1]), Double.parseDouble(parts[2]));
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    private void finish(ServerPlayer player, HunterPlayerData data) {
        data.clearActiveAbility();
        this.startCooldown(data, this.getMaxCooldownTicks());
        HunterDataUtil.sync(player);
    }

    private void launchMeteorHeel(ServerPlayer player, HunterPlayerData data, Vec3 direction, Vec3 forward) {
        float movementMultiplier = HunterAbilities.FLOW_STEP.getMovementAbilityMultiplier(data);
        Vec3 motion = player.getDeltaMovement();
        Vec3 adjustedMotion = motion.scale(player.onGround() ? 1.25D * movementMultiplier : 1.5D * movementMultiplier);
        Vec3 launchDirection = new Vec3(direction.x, 0.0D, direction.z);
        if (launchDirection.lengthSqr() < 1.0E-4D) {
            launchDirection = forward;
        }
        if (launchDirection.lengthSqr() > 1.0E-4D) {
            Vec3 horizontalPush = launchDirection.normalize().scale(LAUNCH_FORWARD_SPEED * movementMultiplier);
            adjustedMotion = new Vec3(
                    Math.abs(adjustedMotion.x) > 0.12D ? adjustedMotion.x : horizontalPush.x,
                    adjustedMotion.y,
                    Math.abs(adjustedMotion.z) > 0.12D ? adjustedMotion.z : horizontalPush.z
            );
        }
        player.setDeltaMovement(adjustedMotion.x, LAUNCH_VERTICAL_SPEED * movementMultiplier, adjustedMotion.z);
        player.hurtMarked = true;
    }

    private void spawnDiveTrail(ServerPlayer player) {
        if (!(player.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        Vec3 point = player.position().add(0.0D, 1.0D, 0.0D);
        serverLevel.sendParticles(ParticleTypes.CLOUD, point.x, point.y, point.z, 5, 0.16D, 0.08D, 0.16D, 0.02D);
        serverLevel.sendParticles(ParticleTypes.FLAME, point.x, point.y - 0.25D, point.z, 4, 0.14D, 0.08D, 0.14D, 0.025D);
        serverLevel.sendParticles(ParticleTypes.CRIT, point.x, point.y, point.z, 4, 0.1D, 0.1D, 0.1D, 0.02D);
    }
}
