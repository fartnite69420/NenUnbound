package com.huntercraft.huntercraft.abilities.boxertree;

import com.huntercraft.huntercraft.abilities.GrabAbility;
import com.huntercraft.huntercraft.abilities.SkillTreeCombatAbility;
import com.huntercraft.huntercraft.animation.AnimationType;
import com.huntercraft.huntercraft.abilities.defensetree.SkybreakerDiveAbility;
import com.huntercraft.huntercraft.damage.HunterDamageSources;
import com.huntercraft.huntercraft.data.HunterPlayerData;
import com.huntercraft.huntercraft.entity.AegisSlamEffectEntity;
import com.huntercraft.huntercraft.faction.FactionUtil;
import com.huntercraft.huntercraft.progression.SkillNode;
import com.huntercraft.huntercraft.util.HunterDataUtil;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class AtlasDropAbility extends SkillTreeCombatAbility implements GrabAbility {
    private static final int CHARGE_TICKS = 10;
    private static final int ACTIVE_TICKS = 50;
    private static final int FLOAT_START_REMAINING = 42;
    private static final int JUMP_REMAINING = 22;
    private static final int SLAM_START_REMAINING = 14;
    private static final int IMPACT_REMAINING = 5;
    private static final float UPPERCUT_DAMAGE = 16.0F;
    private static final float IMPACT_DAMAGE = 42.0F;
    private static final double AIR_HEIGHT = 5.2D;
    private static final int BOXING_SLAM_COLOR = 0xFFE36A;

    public AtlasDropAbility() {
        super("atlas_drop", "Flying Uppercut", "Uppercut a target into the air, leap after them, then slam them down hard enough to break the ground.", "textures/gui/abilities/atlas_drop.png", SkillNode.BOXING, 50);
    }

    @Override
    public int getMaxCooldownTicks() {
        return 300;
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
        LivingEntity target = findTarget(player, 4.0D);
        if (target == null) {
            return;
        }
        Vec3 forward = target.position().subtract(player.position()).multiply(1.0D, 0.0D, 1.0D);
        if (forward.lengthSqr() < 1.0E-4D) {
            forward = direction.lengthSqr() > 1.0E-4D ? direction.multiply(1.0D, 0.0D, 1.0D) : player.getLookAngle().multiply(1.0D, 0.0D, 1.0D);
        }
        if (forward.lengthSqr() < 1.0E-4D) {
            forward = Vec3.directionFromRotation(0.0F, player.getYRot());
        }
        forward = forward.normalize();
        data.startChargingAbility(this.id(), CHARGE_TICKS, forward);
        data.setChargeTargetUuid(target.getUUID().toString());
        data.triggerAnimation(AnimationType.BOXER_GRAB_LIFT);
        HunterDataUtil.sync(player);
    }

    private void beginUppercut(ServerPlayer player, HunterPlayerData data, LivingEntity target, Vec3 forward) {
        data.startActiveAbility(this.id(), ACTIVE_TICKS, forward);
        data.setActiveAbilityTargetUuid(target.getUUID().toString());
        data.triggerAnimation(AnimationType.BOXER_GRAB_LIFT);
        playPunchReleaseSound(player, 1.02F);
        startUppercut(player, target, forward);
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
            LivingEntity target = resolveTarget(player, data.getChargeTargetUuid());
            Vec3 forward = data.getChargeDirection();
            data.clearChargingAbility();
            if (target == null || !target.isAlive()) {
                this.startCooldown(data, 10);
                HunterDataUtil.sync(player);
                return;
            }
            beginUppercut(player, data, target, forward.lengthSqr() > 1.0E-4D ? forward.normalize() : Vec3.directionFromRotation(0.0F, player.getYRot()));
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
        int remaining = data.getActiveAbilityTicksRemaining();
        Vec3 forward = data.getActiveAbilityDirection();
        if (forward.lengthSqr() < 1.0E-4D) {
            forward = new Vec3(0.0D, 0.0D, 1.0D);
        }
        forward = forward.normalize();

        if (remaining > JUMP_REMAINING) {
            holdTargetInAir(player, target, forward, remaining);
        } else if (remaining == JUMP_REMAINING) {
            jumpToTarget(player, target, forward, data);
        } else if (remaining > SLAM_START_REMAINING) {
            holdAerialGrab(player, target, forward);
        } else if (remaining == SLAM_START_REMAINING) {
            data.triggerAnimation(AnimationType.BOXER_GRAB_SLAM);
            target.teleportTo(player.getX() + forward.x * 0.35D, player.getY() + 0.85D, player.getZ() + forward.z * 0.35D);
            target.setDeltaMovement(0.0D, -1.65D, 0.0D);
            target.hurtMarked = true;
            player.setDeltaMovement(forward.x * 0.18D, -1.35D, forward.z * 0.18D);
            player.hasImpulse = true;
            player.hurtMarked = true;
        } else if (remaining > IMPACT_REMAINING) {
            target.setDeltaMovement(0.0D, -1.65D, 0.0D);
            target.hurtMarked = true;
            player.setDeltaMovement(forward.x * 0.18D, -1.35D, forward.z * 0.18D);
            player.hasImpulse = true;
            player.hurtMarked = true;
        }

        data.tickActiveAbility();
        if (remaining <= IMPACT_REMAINING || data.getActiveAbilityTicksRemaining() <= 0) {
            slam(player, target);
            data.clearActiveAbility();
            this.startCooldown(data, this.getMaxCooldownTicks());
            HunterDataUtil.sync(player);
        }
    }

    private void startUppercut(ServerPlayer player, LivingEntity target, Vec3 forward) {
        target.invulnerableTime = 0;
        target.hurt(HunterDamageSources.physical(player.level(), player), UPPERCUT_DAMAGE);
        target.invulnerableTime = 0;
        target.setDeltaMovement(forward.x * 0.12D, 1.55D, forward.z * 0.12D);
        target.hasImpulse = true;
        target.hurtMarked = true;
        HunterDataUtil.applyStun(target, player, ACTIVE_TICKS + 8);
        if (player.level() instanceof ServerLevel serverLevel) {
            Vec3 impact = target.position().add(0.0D, target.getBbHeight() * 0.52D, 0.0D);
            serverLevel.sendParticles(ParticleTypes.CRIT, impact.x, impact.y, impact.z, 18, 0.22D, 0.18D, 0.22D, 0.06D);
            serverLevel.sendParticles(ParticleTypes.SWEEP_ATTACK, impact.x, impact.y + 0.1D, impact.z, 2, 0.12D, 0.08D, 0.12D, 0.0D);
        }
    }

    private void holdTargetInAir(ServerPlayer player, LivingEntity target, Vec3 forward, int remaining) {
        Vec3 anchor = player.position().add(forward.scale(0.85D)).add(0.0D, AIR_HEIGHT, 0.0D);
        if (remaining <= FLOAT_START_REMAINING) {
            target.teleportTo(anchor.x, anchor.y, anchor.z);
            target.setDeltaMovement(Vec3.ZERO);
        }
        target.hasImpulse = true;
        target.hurtMarked = true;
        if (player.level() instanceof ServerLevel serverLevel && remaining % 4 == 0) {
            serverLevel.sendParticles(ParticleTypes.CLOUD, anchor.x, anchor.y, anchor.z, 4, 0.16D, 0.12D, 0.16D, 0.012D);
            serverLevel.sendParticles(ParticleTypes.CRIT, anchor.x, anchor.y, anchor.z, 3, 0.18D, 0.18D, 0.18D, 0.03D);
        }
    }

    private void jumpToTarget(ServerPlayer player, LivingEntity target, Vec3 forward, HunterPlayerData data) {
        Vec3 jumpPos = target.position().subtract(forward.scale(0.72D)).add(0.0D, -0.15D, 0.0D);
        player.teleportTo(jumpPos.x, jumpPos.y, jumpPos.z);
        player.setDeltaMovement(forward.x * 0.14D, 0.24D, forward.z * 0.14D);
        player.hasImpulse = true;
        player.hurtMarked = true;
        data.triggerAnimation(AnimationType.BOXER_GRAB_LIFT);
    }

    private void holdAerialGrab(ServerPlayer player, LivingEntity target, Vec3 forward) {
        target.teleportTo(player.getX() + forward.x * 0.45D, player.getY() + 0.88D, player.getZ() + forward.z * 0.45D);
        target.setDeltaMovement(Vec3.ZERO);
        target.hasImpulse = true;
        target.hurtMarked = true;
    }

    private void slam(ServerPlayer player, LivingEntity target) {
        Vec3 ground = findGroundImpact(player, target);
        player.teleportTo(ground.x, ground.y, ground.z);
        target.teleportTo(ground.x, ground.y, ground.z);
        target.hurt(HunterDamageSources.physical(player.level(), player), IMPACT_DAMAGE);
        target.setDeltaMovement(0.0D, -1.3D, 0.0D);
        target.hurtMarked = true;
        AABB splash = new AABB(ground.x - 3.35D, ground.y - 0.5D, ground.z - 3.35D, ground.x + 3.35D, ground.y + 1.45D, ground.z + 3.35D);
        for (LivingEntity other : player.level().getEntitiesOfClass(LivingEntity.class, splash, entity -> entity != player && entity != target && entity.isAlive())) {
            other.hurt(HunterDamageSources.physical(player.level(), player), 10.0F);
            Vec3 knock = other.position().subtract(ground).multiply(1.0D, 0.0D, 1.0D);
            if (knock.lengthSqr() > 1.0E-4D) {
                knock = knock.normalize().scale(0.9D);
                other.setDeltaMovement(knock.x, 0.35D, knock.z);
                other.hurtMarked = true;
            }
        }
        if (player.level() instanceof ServerLevel serverLevel) {
            playGroundSmashReleaseSound(player, 0.86F);
            AegisSlamEffectEntity.spawn(serverLevel, ground.add(0.0D, 0.05D, 0.0D), player.getYRot(), 1.15F, 18, BOXING_SLAM_COLOR);
            SkybreakerDiveAbility.launchImpactBlocks(serverLevel, ground.add(0.0D, 0.55D, 0.0D));
            serverLevel.sendParticles(ParticleTypes.EXPLOSION, ground.x, ground.y + 0.2D, ground.z, 2, 0.18D, 0.0D, 0.18D, 0.0D);
            serverLevel.sendParticles(ParticleTypes.CLOUD, ground.x, ground.y + 0.1D, ground.z, 40, 1.45D, 0.14D, 1.45D, 0.07D);
            serverLevel.sendParticles(ParticleTypes.CRIT, ground.x, ground.y + 0.5D, ground.z, 28, 1.0D, 0.35D, 1.0D, 0.08D);
        }
        player.setDeltaMovement(Vec3.ZERO);
        player.hurtMarked = true;
        player.fallDistance = 0.0F;
    }

    private Vec3 findGroundImpact(ServerPlayer player, LivingEntity target) {
        double x = target.getX();
        double z = target.getZ();
        double startY = Math.max(player.getY(), target.getY());
        for (int i = 0; i < 18; i++) {
            double y = startY - i;
            if (!player.level().getBlockState(net.minecraft.core.BlockPos.containing(x, y - 0.1D, z)).isAir()) {
                return new Vec3(x, y, z);
            }
        }
        return new Vec3(x, player.getY(), z);
    }

    private LivingEntity findTarget(ServerPlayer player, double range) {
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle().normalize();
        AABB searchBox = player.getBoundingBox().expandTowards(look.scale(range)).inflate(1.2D);
        LivingEntity closest = null;
        double closestDistance = range * range;
        for (LivingEntity living : player.level().getEntitiesOfClass(LivingEntity.class, searchBox, entity ->
                entity != player && entity.isAlive() && !(entity instanceof Player targetPlayer && FactionUtil.areFactionMates(player, targetPlayer)))) {
            AABB box = living.getBoundingBox().inflate(0.35D);
            Vec3 hit = box.clip(eye, eye.add(look.scale(range))).orElse(null);
            if (hit == null) {
                continue;
            }
            double distance = eye.distanceToSqr(hit);
            if (distance < closestDistance) {
                closestDistance = distance;
                closest = living;
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
