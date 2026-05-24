package com.huntercraft.huntercraft.abilities.speedtree;

import com.huntercraft.huntercraft.abilities.SkillTreeCombatAbility;
import com.huntercraft.huntercraft.damage.HunterDamageSources;
import com.huntercraft.huntercraft.data.HunterPlayerData;
import com.huntercraft.huntercraft.network.HunterNetwork;
import com.huntercraft.huntercraft.network.packet.AfterImagePacket;
import com.huntercraft.huntercraft.progression.SkillNode;
import com.huntercraft.huntercraft.util.HunterDataUtil;
import com.huntercraft.huntercraft.util.TeleportSafetyHelper;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class PhantomRingAbility extends SkillTreeCombatAbility {
    private static final int CHARGE_TICKS = 6;
    private static final int BLITZ_TICKS = 18;
    private static final int ZIP_INTERVAL_TICKS = 3;
    private static final double ZIP_RADIUS = 6.2D;
    private static final double ZIP_HEIGHT = 4.6D;
    private static final int AFTER_IMAGE_LIFE = 14;
    private static final float FINISHING_STRIKE_DAMAGE = 45.0F;

    public PhantomRingAbility() {
        super("phantom_ring", "Speed Blitz", "Zip through the air in rapid bursts, then home in on the tracked target with a finishing strike.", "textures/gui/abilities/phantom_ring.png", SkillNode.SPEED, 55);
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
        LivingEntity target = this.findTarget(player, 24.0D);
        if (target == null) {
            return;
        }
        Vec3 forward = target.position().subtract(player.position()).multiply(1.0D, 0.0D, 1.0D);
        if (forward.lengthSqr() < 1.0E-4D) {
            forward = player.getLookAngle().multiply(1.0D, 0.0D, 1.0D);
        }
        if (forward.lengthSqr() < 1.0E-4D) {
            return;
        }
        data.startChargingAbility(this.id(), CHARGE_TICKS, forward.normalize());
        data.setChargeTargetUuid(target.getUUID().toString());
        data.triggerAnimation(com.huntercraft.huntercraft.animation.AnimationType.DASH);
        HunterDataUtil.sync(player);
    }

    private void beginBlitz(ServerPlayer player, HunterPlayerData data, LivingEntity target, Vec3 forward) {
        data.startActiveAbility(this.id(), BLITZ_TICKS, forward.normalize());
        data.setActiveAbilityTargetUuid(target.getUUID().toString());
        data.triggerAnimation(com.huntercraft.huntercraft.animation.AnimationType.DASH);
        playTeleportReleaseSound(player, 1.28F);
        HunterDataUtil.sync(player);
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
            beginBlitz(player, data, target, forward);
            return;
        }
        if (!data.isActiveAbility(this.id())) {
            return;
        }
        LivingEntity target = resolveTarget(player, data.getActiveAbilityTargetUuid());
        if (target == null || !target.isAlive()) {
            data.clearActiveAbility();
            return;
        }

        int remaining = data.getActiveAbilityTicksRemaining();
        int elapsed = BLITZ_TICKS - remaining;
        if (elapsed % ZIP_INTERVAL_TICKS == 0 && remaining > ZIP_INTERVAL_TICKS) {
            zipThroughAir(player, target, elapsed);
        }

        data.tickActiveAbility();
        if (data.getActiveAbilityTicksRemaining() > 0) {
            return;
        }

        data.clearActiveAbility();
        finishStrike(player, data, target);
    }

    private void zipThroughAir(ServerPlayer player, LivingEntity target, int elapsed) {
        double progress = Math.min(1.0D, elapsed / (double) BLITZ_TICKS);
        double angle = progress * Math.PI * 2.0D * 2.35D;
        Vec3 center = target.position();
        Vec3 currentPos = player.position();
        double radius = ZIP_RADIUS * (1.0D - (progress * 0.22D));
        double x = center.x + Math.cos(angle) * radius;
        double z = center.z + Math.sin(angle) * radius;
        double y = target.getY() + ZIP_HEIGHT + Math.sin(angle * 1.7D) * 1.2D;

        HunterNetwork.sendToTrackingAndSelf(player, new AfterImagePacket(
                player.getUUID(),
                currentPos.x,
                currentPos.y,
                currentPos.z,
                player.getYRot(),
                player.getXRot(),
                AFTER_IMAGE_LIFE
        ));

        Vec3 zipPos = new Vec3(x, y, z);
        Vec3 midpoint = currentPos.lerp(zipPos, 0.5D);
        HunterNetwork.sendToTrackingAndSelf(player, new AfterImagePacket(
                player.getUUID(),
                midpoint.x,
                midpoint.y,
                midpoint.z,
                player.getYRot(),
                player.getXRot(),
                AFTER_IMAGE_LIFE
        ));
        player.teleportTo(zipPos.x, zipPos.y, zipPos.z);
        player.setDeltaMovement(Vec3.ZERO);
        player.hurtMarked = true;
        player.lookAt(EntityAnchorArgument.Anchor.EYES, target, EntityAnchorArgument.Anchor.EYES);

        if (player.level() instanceof ServerLevel serverLevel) {
            Vec3 effectCenter = target.position().add(0.0D, 1.0D, 0.0D);
            serverLevel.sendParticles(ParticleTypes.CLOUD, zipPos.x, zipPos.y + 0.2D, zipPos.z, 10, 0.18D, 0.18D, 0.18D, 0.04D);
            serverLevel.sendParticles(ParticleTypes.CRIT, zipPos.x, zipPos.y + 0.2D, zipPos.z, 8, 0.16D, 0.16D, 0.16D, 0.04D);
            serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK, zipPos.x, zipPos.y + 0.2D, zipPos.z, 8, 0.2D, 0.2D, 0.2D, 0.08D);
            serverLevel.sendParticles(ParticleTypes.ENCHANT, effectCenter.x, effectCenter.y, effectCenter.z, 12, ZIP_RADIUS, ZIP_HEIGHT * 0.35D, ZIP_RADIUS, 0.05D);
        }
    }

    private void finishStrike(ServerPlayer player, HunterPlayerData data, LivingEntity target) {
        Vec3 fromTargetToPlayer = player.position().subtract(target.position()).multiply(1.0D, 0.0D, 1.0D);
        if (fromTargetToPlayer.lengthSqr() < 1.0E-4D) {
            fromTargetToPlayer = target.getLookAngle().multiply(1.0D, 0.0D, 1.0D).reverse();
        }
        Vec3 strikeDirection = fromTargetToPlayer.lengthSqr() > 1.0E-4D ? fromTargetToPlayer.normalize() : new Vec3(0.0D, 0.0D, 1.0D);
        Vec3 strikePosition = target.position().add(strikeDirection.scale(0.95D));
        Vec3 safeStrikePos = TeleportSafetyHelper.resolveAroundTarget(player, new Vec3(strikePosition.x, target.getY(), strikePosition.z));
        Vec3 beforeStrike = player.position();
        spawnBlitzTrail(player, beforeStrike, target.position().add(0.0D, 1.0D, 0.0D));
        player.teleportTo(safeStrikePos.x, safeStrikePos.y, safeStrikePos.z);
        player.lookAt(EntityAnchorArgument.Anchor.EYES, target, EntityAnchorArgument.Anchor.EYES);
        target.hurt(HunterDamageSources.weapon(player.level(), player), this.getWeaponScaledDamage(player, FINISHING_STRIKE_DAMAGE));
        target.knockback(0.7F, -strikeDirection.x, -strikeDirection.z);
        if (player.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.SWEEP_ATTACK, target.getX(), target.getEyeY(), target.getZ(), 18, 0.8D, 0.4D, 0.8D, 0.0D);
            serverLevel.sendParticles(ParticleTypes.CRIT, target.getX(), target.getEyeY(), target.getZ(), 36, 0.95D, 0.5D, 0.95D, 0.1D);
            serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK, target.getX(), target.getEyeY(), target.getZ(), 28, 0.7D, 0.45D, 0.7D, 0.12D);
            serverLevel.sendParticles(ParticleTypes.CLOUD, target.getX(), target.getEyeY(), target.getZ(), 18, 0.55D, 0.3D, 0.55D, 0.08D);
        }
        this.startCooldown(data, this.getMaxCooldownTicks());
        HunterDataUtil.sync(player);
    }

    private void spawnBlitzTrail(ServerPlayer player, Vec3 start, Vec3 end) {
        HunterNetwork.sendToTrackingAndSelf(player, new AfterImagePacket(
                player.getUUID(),
                start.x,
                start.y,
                start.z,
                player.getYRot(),
                player.getXRot(),
                AFTER_IMAGE_LIFE
        ));
        if (!(player.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        Vec3 path = end.subtract(start);
        for (int i = 0; i < 9; i++) {
            Vec3 point = start.add(path.scale((i + 1.0D) / 10.0D));
            serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK, point.x, point.y, point.z, 4, 0.12D, 0.12D, 0.12D, 0.06D);
            serverLevel.sendParticles(ParticleTypes.CLOUD, point.x, point.y, point.z, 3, 0.08D, 0.08D, 0.08D, 0.025D);
        }
    }

    private LivingEntity resolveTarget(ServerPlayer player, String uuidString) {
        if (uuidString == null || uuidString.isBlank() || !(player.level() instanceof ServerLevel serverLevel)) {
            return null;
        }
        try {
            return serverLevel.getEntity(UUID.fromString(uuidString)) instanceof LivingEntity living ? living : null;
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private LivingEntity findTarget(ServerPlayer player, double range) {
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle().normalize();
        AABB searchBox = player.getBoundingBox().expandTowards(look.scale(range)).inflate(1.5D);
        LivingEntity closest = null;
        double closestDistance = range * range;
        for (LivingEntity living : player.level().getEntitiesOfClass(LivingEntity.class, searchBox, entity -> entity != player && entity.isAlive())) {
            AABB box = living.getBoundingBox().inflate(0.4D);
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
}
