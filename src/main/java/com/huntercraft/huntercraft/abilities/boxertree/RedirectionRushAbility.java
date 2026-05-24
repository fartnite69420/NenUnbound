package com.huntercraft.huntercraft.abilities.boxertree;

import com.huntercraft.huntercraft.abilities.SkillTreeCombatAbility;
import com.huntercraft.huntercraft.animation.AnimationType;
import com.huntercraft.huntercraft.damage.HunterDamageSources;
import com.huntercraft.huntercraft.data.HunterPlayerData;
import com.huntercraft.huntercraft.entity.HammerShockwaveEntity;
import com.huntercraft.huntercraft.progression.SkillNode;
import com.huntercraft.huntercraft.util.HunterDataUtil;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class RedirectionRushAbility extends SkillTreeCombatAbility {
    private static final int CHARGE_TICKS = 6;
    private static final int DASH_TICKS = 18;
    private static final int COMBO_TICKS = 24;
    private static final int FINAL_PUNCH_REMAINING = 7;
    private static final float FIRST_HIT_DAMAGE = 14.0F;
    private static final float FINAL_HIT_DAMAGE = 40.0F;
    private static final double DASH_SPEED = 1.55D;
    private static final double FINAL_KNOCKBACK = 5.25D;

    public RedirectionRushAbility() {
        super("redirection_rush", "Redirection Rush", "Dash into a target, pin them with a fast body shot, then wind up a giant shockwave punch that sends them flying.", "textures/gui/abilities/redirection_rush.png", SkillNode.BOXING, 40);
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
        if (data.isActiveAbility(this.id()) || data.isChargingAbility(this.id())) {
            return;
        }
        Vec3 forward = direction.lengthSqr() > 1.0E-4D ? new Vec3(direction.x, 0.0D, direction.z).normalize() : player.getLookAngle().multiply(1.0D, 0.0D, 1.0D).normalize();
        if (forward.lengthSqr() < 1.0E-4D) {
            return;
        }
        data.startChargingAbility(this.id(), CHARGE_TICKS, forward);
        data.triggerAnimation(AnimationType.BOXER_REDIRECTION);
        HunterDataUtil.sync(player);
    }

    private void beginRush(ServerPlayer player, HunterPlayerData data, Vec3 forward) {
        data.startActiveAbility(this.id(), DASH_TICKS, forward);
        data.triggerAnimation(AnimationType.BOXER_REDIRECTION);
        playDashReleaseSound(player, 1.0F);
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
                beginRush(player, data, forward);
            }
            return;
        }
        if (!data.isActiveAbility(this.id())) {
            return;
        }
        Vec3 forward = data.getActiveAbilityDirection();
        if (forward.lengthSqr() < 1.0E-4D) {
            forward = player.getLookAngle().multiply(1.0D, 0.0D, 1.0D).normalize();
        }
        if (data.getActiveAbilityTargetUuid().isBlank()) {
            tickDash(player, data, forward);
            return;
        }

        int remaining = data.getActiveAbilityTicksRemaining();
        LivingEntity target = getTarget(player, data);
        if (target == null || !target.isAlive()) {
            finish(player, data);
            return;
        }

        Vec3 toTarget = target.position().subtract(player.position()).multiply(1.0D, 0.0D, 1.0D);
        if (toTarget.lengthSqr() > 1.0E-4D) {
            forward = toTarget.normalize();
        }
        player.lookAt(EntityAnchorArgument.Anchor.EYES, target.getEyePosition());
        player.setDeltaMovement(Vec3.ZERO);
        player.hasImpulse = true;

        if (remaining > FINAL_PUNCH_REMAINING) {
            windupEffects(player, target, forward, remaining);
        } else if (remaining == FINAL_PUNCH_REMAINING) {
            finalPunch(player, target, forward);
        }

        data.tickActiveAbility();
        if (data.getActiveAbilityTicksRemaining() <= 0) {
            finish(player, data);
        }
    }

    private void tickDash(ServerPlayer player, HunterPlayerData data, Vec3 forward) {
        Vec3 speed = new Vec3(forward.x * DASH_SPEED, 0.0D, forward.z * DASH_SPEED);
        player.setDeltaMovement(speed);
        player.move(MoverType.SELF, speed);
        player.hasImpulse = true;
        player.hurtMarked = true;
        trail(player);

        LivingEntity target = findDashTarget(player, forward);
        if (target != null) {
            startCombo(player, data, target, forward);
            return;
        }
        data.tickActiveAbility();
        if (data.getActiveAbilityTicksRemaining() <= 0) {
            finish(player, data);
        }
    }

    private void startCombo(ServerPlayer player, HunterPlayerData data, LivingEntity target, Vec3 forward) {
        player.lookAt(EntityAnchorArgument.Anchor.EYES, target.getEyePosition());
        target.invulnerableTime = 0;
        target.hurt(HunterDamageSources.physical(player.level(), player), FIRST_HIT_DAMAGE);
        target.invulnerableTime = 0;
        target.setDeltaMovement(Vec3.ZERO);
        target.hasImpulse = true;
        target.hurtMarked = true;
        HunterDataUtil.applyStun(target, player, 14);
        data.startActiveAbility(this.id(), COMBO_TICKS, forward);
        data.setActiveAbilityTargetUuid(target.getUUID().toString());
        data.triggerAnimation(AnimationType.BOXER_REDIRECTION);
        if (player.level() instanceof ServerLevel serverLevel) {
            Vec3 impact = target.position().add(0.0D, target.getBbHeight() * 0.55D, 0.0D);
            serverLevel.sendParticles(ParticleTypes.CRIT, impact.x, impact.y, impact.z, 14, 0.18D, 0.16D, 0.18D, 0.04D);
            serverLevel.sendParticles(ParticleTypes.CLOUD, impact.x, impact.y, impact.z, 8, 0.14D, 0.12D, 0.14D, 0.015D);
        }
    }

    private void finalPunch(ServerPlayer player, LivingEntity primaryTarget, Vec3 forward) {
        Vec3 center = player.position().add(forward.scale(1.45D)).add(0.0D, 1.0D, 0.0D);
        AABB hitBox = new AABB(center.x - 2.2D, center.y - 1.05D, center.z - 2.2D, center.x + 2.2D, center.y + 1.15D, center.z + 2.2D)
                .expandTowards(forward.scale(2.0D));
        for (LivingEntity target : player.level().getEntitiesOfClass(LivingEntity.class, hitBox, entity -> entity != player && entity.isAlive())) {
            Vec3 toTarget = target.position().subtract(player.position()).multiply(1.0D, 0.0D, 1.0D);
            if (target != primaryTarget && toTarget.lengthSqr() > 1.0E-4D && toTarget.normalize().dot(forward) < 0.08D) {
                continue;
            }
            target.invulnerableTime = 0;
            target.hurt(HunterDamageSources.physical(player.level(), player), FINAL_HIT_DAMAGE);
            target.invulnerableTime = 0;
            target.setDeltaMovement(forward.x * FINAL_KNOCKBACK, 0.62D, forward.z * FINAL_KNOCKBACK);
            target.hasImpulse = true;
            target.hurtMarked = true;
        }
        if (player.level() instanceof ServerLevel serverLevel) {
            HammerShockwaveEntity.spawn(serverLevel, player.position().add(forward.scale(0.85D)).add(0.0D, 0.32D, 0.0D), forward, 1.85F, 20);
            serverLevel.sendParticles(ParticleTypes.EXPLOSION, center.x, center.y, center.z, 2, 0.16D, 0.12D, 0.16D, 0.0D);
            serverLevel.sendParticles(ParticleTypes.CRIT, center.x, center.y, center.z, 34, 0.55D, 0.35D, 0.55D, 0.08D);
            serverLevel.sendParticles(ParticleTypes.CLOUD, center.x, center.y - 0.18D, center.z, 24, 0.42D, 0.2D, 0.42D, 0.04D);
        }
        playPunchReleaseSound(player, 0.72F);
    }

    private LivingEntity findDashTarget(ServerPlayer player, Vec3 forward) {
        Vec3 center = player.position().add(forward.scale(1.15D)).add(0.0D, 1.0D, 0.0D);
        AABB hitBox = player.getBoundingBox().expandTowards(forward.scale(1.35D)).inflate(0.62D, 0.55D, 0.62D);
        LivingEntity closest = null;
        double closestDistance = Double.MAX_VALUE;
        for (LivingEntity target : player.level().getEntitiesOfClass(LivingEntity.class, hitBox, entity -> entity != player && entity.isAlive())) {
            Vec3 toTarget = target.position().subtract(player.position()).multiply(1.0D, 0.0D, 1.0D);
            if (toTarget.lengthSqr() > 1.0E-4D && toTarget.normalize().dot(forward) < 0.18D) {
                continue;
            }
            double distance = target.distanceToSqr(center);
            if (distance < closestDistance) {
                closest = target;
                closestDistance = distance;
            }
        }
        return closest;
    }

    private LivingEntity getTarget(ServerPlayer player, HunterPlayerData data) {
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

    private void windupEffects(ServerPlayer player, LivingEntity target, Vec3 forward, int remaining) {
        if (player.level() instanceof ServerLevel serverLevel && remaining % 3 == 0) {
            Vec3 fist = player.position().add(forward.scale(0.8D)).add(0.0D, 1.05D, 0.0D);
            serverLevel.sendParticles(ParticleTypes.CRIT, fist.x, fist.y, fist.z, 5, 0.12D, 0.08D, 0.12D, 0.035D);
            serverLevel.sendParticles(ParticleTypes.CLOUD, target.getX(), target.getY() + target.getBbHeight() * 0.48D, target.getZ(), 3, 0.16D, 0.12D, 0.16D, 0.01D);
        }
    }

    private void trail(ServerPlayer player) {
        if (player.level() instanceof ServerLevel serverLevel) {
            Vec3 pos = player.position().add(0.0D, 1.0D, 0.0D);
            serverLevel.sendParticles(ParticleTypes.CLOUD, pos.x, pos.y, pos.z, 4, 0.12D, 0.08D, 0.12D, 0.01D);
        }
    }

    private void finish(ServerPlayer player, HunterPlayerData data) {
        data.clearActiveAbility();
        data.triggerAnimation(AnimationType.NONE);
        this.startCooldown(data, this.getMaxCooldownTicks());
        HunterDataUtil.sync(player);
    }
}
