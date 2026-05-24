package com.huntercraft.huntercraft.abilities.speedtree;

import com.huntercraft.huntercraft.abilities.SkillTreeCombatAbility;
import com.huntercraft.huntercraft.animation.AnimationType;
import com.huntercraft.huntercraft.damage.HunterDamageSources;
import com.huntercraft.huntercraft.data.HunterPlayerData;
import com.huntercraft.huntercraft.entity.SlashEffectEntity;
import com.huntercraft.huntercraft.entity.SpeedBladeTrailEntity;
import com.huntercraft.huntercraft.network.HunterNetwork;
import com.huntercraft.huntercraft.network.packet.AfterImagePacket;
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

public class UnseenBladeAbility extends SkillTreeCombatAbility {
    private static final int CHARGE_TICKS = 20;
    private static final int ACTIVE_TICKS = 34;
    private static final int SLASH_INTERVAL = 4;
    private static final double RANGE = 20.0D;
    private static final double DASH_DISTANCE = 3.35D;
    private static final double HOLD_HEIGHT = 3.25D;
    private static final float SLASH_DAMAGE = 8.0F;
    private static final float FINISH_DAMAGE = 14.0F;
    private static final int CYAN = 0x8DEEFF;

    public UnseenBladeAbility() {
        super("unseen_blade", "Unseen Blade", "Dash into a blind spot, suspend the target, and carve them with converging speed slashes.", "textures/gui/abilities/unseen_blade.png", SkillNode.SPEED, 65);
    }

    @Override
    public int getMaxCooldownTicks() {
        return 340;
    }

    @Override
    public int getChargeTicks(HunterPlayerData data) {
        return data.isChargingAbility(this.id()) ? CHARGE_TICKS - data.getChargeTicksRemaining() : 0;
    }

    @Override
    public int getActiveTicks(HunterPlayerData data) {
        return data.isActiveAbility(this.id()) ? ACTIVE_TICKS - data.getActiveAbilityTicksRemaining() : 0;
    }

    @Override
    public void use(ServerPlayer player, HunterPlayerData data, Vec3 direction) {
        if (data.isChargingAbility(this.id()) || data.isActiveAbility(this.id())) {
            return;
        }
        LivingEntity target = findTarget(player, RANGE);
        if (target == null) {
            return;
        }
        Vec3 forward = target.position().subtract(player.position()).multiply(1.0D, 0.0D, 1.0D);
        if (forward.lengthSqr() < 1.0E-4D) {
            forward = resolveForward(player, direction);
        }
        if (forward.lengthSqr() < 1.0E-4D) {
            return;
        }
        data.startChargingAbility(this.id(), CHARGE_TICKS, forward.normalize());
        data.setChargeTargetUuid(target.getUUID().toString());
        data.triggerAnimation(AnimationType.LION_FANG_DRAW_CHARGE);
        playDashReleaseSound(player, 1.38F);
        HunterDataUtil.sync(player);
    }

    @Override
    public void tick(ServerPlayer player, HunterPlayerData data) {
        if (data.isChargingAbility(this.id())) {
            data.tickChargingAbility();
            if (data.getChargeTicksRemaining() > 0) {
                return;
            }
            LivingEntity target = resolveTarget(player, data.getChargeTargetUuid());
            Vec3 forward = resolveForward(player, data.getChargeDirection());
            data.clearChargingAbility();
            if (target == null || !target.isAlive() || forward.lengthSqr() < 1.0E-4D) {
                this.startCooldown(data, 10);
                HunterDataUtil.sync(player);
                return;
            }
            beginBladeSequence(player, data, target, forward);
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

        int elapsed = ACTIVE_TICKS - data.getActiveAbilityTicksRemaining();
        holdTarget(player, target);
        if (elapsed % SLASH_INTERVAL == 0 && elapsed <= 24) {
            slashIntoTarget(player, target, data.getActiveAbilityDirection(), elapsed / SLASH_INTERVAL);
        }

        data.tickActiveAbility();
        if (data.getActiveAbilityTicksRemaining() <= 0) {
            finishBladeSequence(player, data, target);
        }
    }

    private void beginBladeSequence(ServerPlayer player, HunterPlayerData data, LivingEntity target, Vec3 forward) {
        Vec3 start = player.position();
        Vec3 dash = forward.scale(DASH_DISTANCE);
        Vec3 dashPos = start.add(dash);
        player.teleportTo(dashPos.x, dashPos.y, dashPos.z);
        player.setDeltaMovement(Vec3.ZERO);
        player.hasImpulse = true;
        player.hurtMarked = true;
        player.lookAt(EntityAnchorArgument.Anchor.EYES, target, EntityAnchorArgument.Anchor.EYES);

        target.teleportTo(target.getX(), target.getY() + HOLD_HEIGHT, target.getZ());
        target.setDeltaMovement(Vec3.ZERO);
        target.fallDistance = 0.0F;
        target.hurtMarked = true;
        HunterDataUtil.applyStun(target, player, ACTIVE_TICKS + 8);

        data.startActiveAbility(this.id(), ACTIVE_TICKS, forward.normalize());
        data.setActiveAbilityTargetUuid(target.getUUID().toString());
        data.triggerAnimation(AnimationType.DASH);
        spawnDashAndAfterImages(player, start, dashPos, forward);
        HunterDataUtil.sync(player);
    }

    private void holdTarget(ServerPlayer player, LivingEntity target) {
        double minY = player.getY() + HOLD_HEIGHT;
        if (target.getY() < minY) {
            target.teleportTo(target.getX(), minY, target.getZ());
        }
        target.setDeltaMovement(0.0D, 0.018D, 0.0D);
        target.fallDistance = 0.0F;
        target.hurtMarked = true;
    }

    private void slashIntoTarget(ServerPlayer player, LivingEntity target, Vec3 storedForward, int index) {
        if (!(player.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        Vec3 targetCenter = target.position().add(0.0D, target.getBbHeight() * 0.5D, 0.0D);
        Vec3 forward = targetCenter.subtract(player.position().add(0.0D, 1.0D, 0.0D));
        if (forward.lengthSqr() < 1.0E-4D) {
            forward = storedForward;
        }
        forward = forward.normalize();
        Vec3 horizontal = new Vec3(forward.x, 0.0D, forward.z);
        if (horizontal.lengthSqr() < 1.0E-4D) {
            horizontal = resolveForward(player, storedForward);
        } else {
            horizontal = horizontal.normalize();
        }
        Vec3 side = new Vec3(-horizontal.z, 0.0D, horizontal.x);
        double sideSign = index % 2 == 0 ? -1.0D : 1.0D;
        double verticalOffset = 0.45D + (index % 3) * 0.32D;
        Vec3 slashStart = player.position()
                .add(0.0D, 1.0D + verticalOffset, 0.0D)
                .add(side.scale(sideSign * (2.15D + (index % 2) * 0.45D)))
                .subtract(horizontal.scale(0.55D));
        Vec3 slashVector = targetCenter.subtract(slashStart);
        Vec3 slashDir = slashVector.lengthSqr() > 1.0E-4D ? slashVector.normalize() : forward;
        Vec3 center = slashStart.lerp(targetCenter, 0.5D);
        float length = (float) Math.min(8.5D, Math.max(3.2D, slashVector.length()));

        SpeedBladeTrailEntity.spawn(serverLevel, center, slashDir, length, 0.58F, 12, CYAN);
        SlashEffectEntity.spawn(serverLevel, targetCenter.add(side.scale(sideSign * 0.28D)), slashDir, sideSign > 0.0D ? 34.0F : -34.0F, 0.78F, 12, CYAN);
        serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK, targetCenter.x, targetCenter.y, targetCenter.z, 12, 0.28D, 0.26D, 0.28D, 0.08D);
        serverLevel.sendParticles(ParticleTypes.CRIT, targetCenter.x, targetCenter.y, targetCenter.z, 9, 0.22D, 0.2D, 0.22D, 0.05D);
        playSlashReleaseSound(player, 0.92F + (index * 0.035F));

        target.hurt(HunterDamageSources.weapon(player.level(), player), this.getWeaponScaledDamage(player, SLASH_DAMAGE));
        holdTarget(player, target);
    }

    private void finishBladeSequence(ServerPlayer player, HunterPlayerData data, LivingEntity target) {
        Vec3 direction = target.position().subtract(player.position()).multiply(1.0D, 0.0D, 1.0D);
        if (direction.lengthSqr() < 1.0E-4D) {
            direction = resolveForward(player, data.getActiveAbilityDirection());
        } else {
            direction = direction.normalize();
        }
        target.hurt(HunterDamageSources.weapon(player.level(), player), this.getWeaponScaledDamage(player, FINISH_DAMAGE));
        target.setDeltaMovement(direction.x * 0.42D, 0.12D, direction.z * 0.42D);
        target.fallDistance = 0.0F;
        target.hurtMarked = true;
        if (player.level() instanceof ServerLevel serverLevel) {
            Vec3 center = target.position().add(0.0D, target.getBbHeight() * 0.55D, 0.0D);
            SpeedBladeTrailEntity.spawn(serverLevel, center, direction, 6.5F, 0.9F, 16, CYAN);
            serverLevel.sendParticles(ParticleTypes.SONIC_BOOM, center.x, center.y, center.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
            serverLevel.sendParticles(ParticleTypes.CRIT, center.x, center.y, center.z, 36, 0.65D, 0.38D, 0.65D, 0.12D);
        }
        data.clearActiveAbility();
        this.startCooldown(data, this.getMaxCooldownTicks());
        HunterDataUtil.sync(player);
    }

    private void spawnDashAndAfterImages(ServerPlayer player, Vec3 start, Vec3 end, Vec3 forward) {
        Vec3 side = new Vec3(-forward.z, 0.0D, forward.x);
        spawnAfterImage(player, start, 20);
        spawnAfterImage(player, end.add(side.scale(1.35D)), 22);
        spawnAfterImage(player, end.subtract(side.scale(1.35D)), 22);
        if (!(player.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        Vec3 path = end.subtract(start);
        SpeedBladeTrailEntity.spawn(serverLevel, start.add(path.scale(0.5D)).add(0.0D, 1.0D, 0.0D), forward, (float) path.length(), 0.76F, 10, CYAN);
        serverLevel.sendParticles(ParticleTypes.CLOUD, start.x, start.y + 0.12D, start.z, 18, 0.24D, 0.04D, 0.24D, 0.05D);
        serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK, end.x, end.y + 0.9D, end.z, 20, 0.28D, 0.2D, 0.28D, 0.1D);
    }

    private Vec3 resolveForward(ServerPlayer player, Vec3 direction) {
        Vec3 forward = direction.lengthSqr() > 1.0E-4D ? direction : player.getLookAngle();
        forward = new Vec3(forward.x, 0.0D, forward.z);
        return forward.lengthSqr() > 1.0E-4D ? forward.normalize() : Vec3.ZERO;
    }

    private void spawnAfterImage(ServerPlayer player, Vec3 position, int lifeTicks) {
        HunterNetwork.sendToTrackingAndSelf(player, new AfterImagePacket(player.getUUID(), position.x, position.y, position.z, player.getYRot(), player.getXRot(), lifeTicks));
    }

    private LivingEntity findTarget(ServerPlayer player, double range) {
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle().normalize();
        AABB searchBox = player.getBoundingBox().expandTowards(look.scale(range)).inflate(1.75D);
        LivingEntity closest = null;
        double closestDistance = range * range;
        for (LivingEntity living : player.level().getEntitiesOfClass(LivingEntity.class, searchBox, entity -> entity != player && entity.isAlive())) {
            AABB box = living.getBoundingBox().inflate(0.55D);
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
        if (uuidString == null || uuidString.isBlank() || !(player.level() instanceof ServerLevel serverLevel)) {
            return null;
        }
        try {
            return serverLevel.getEntity(UUID.fromString(uuidString)) instanceof LivingEntity living ? living : null;
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }
}
