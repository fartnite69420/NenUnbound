package com.huntercraft.huntercraft.abilities.boxertree;

import com.huntercraft.huntercraft.abilities.SkillTreeCombatAbility;
import com.huntercraft.huntercraft.animation.AnimationType;
import com.huntercraft.huntercraft.damage.HunterDamageSources;
import com.huntercraft.huntercraft.data.HunterPlayerData;
import com.huntercraft.huntercraft.entity.BoxingBloodBurstEntity;
import com.huntercraft.huntercraft.network.HunterNetwork;
import com.huntercraft.huntercraft.network.packet.AfterImagePacket;
import com.huntercraft.huntercraft.progression.SkillNode;
import com.huntercraft.huntercraft.util.HunterDataUtil;
import com.huntercraft.huntercraft.util.TeleportSafetyHelper;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class RuptureStepAbility extends SkillTreeCombatAbility {
    private static final int CHARGE_TICKS = 10;
    private static final double RANGE = 16.0D;
    private static final float DAMAGE = 58.0F;

    public RuptureStepAbility() {
        super("rupture_step", "Rupture Step", "Blink behind a target and drive a brutal body punch through their stomach.", "textures/gui/abilities/rupture_step.png", SkillNode.BOXING, 65);
    }

    @Override
    public int getMaxCooldownTicks() {
        return 360;
    }

    @Override
    public int getChargeTicks(HunterPlayerData data) {
        return data.isChargingAbility(this.id()) ? CHARGE_TICKS - data.getChargeTicksRemaining() : 0;
    }

    @Override
    public void use(ServerPlayer player, HunterPlayerData data, Vec3 direction) {
        if (data.isChargingAbility(this.id())) {
            return;
        }
        LivingEntity target = findTarget(player, RANGE);
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
        data.triggerAnimation(AnimationType.BOXER_COUNTER_GUARD);
        HunterDataUtil.sync(player);
    }

    @Override
    public void tick(ServerPlayer player, HunterPlayerData data) {
        if (!data.isChargingAbility(this.id())) {
            return;
        }
        player.setDeltaMovement(Vec3.ZERO);
        player.hasImpulse = true;
        data.tickChargingAbility();
        if (data.getChargeTicksRemaining() > 0) {
            return;
        }
        LivingEntity target = resolveTarget(player, data.getChargeTargetUuid());
        data.clearChargingAbility();
        if (target == null || !target.isAlive()) {
            this.startCooldown(data, 10);
            HunterDataUtil.sync(player);
            return;
        }
        executePunch(player, data, target);
    }

    private void executePunch(ServerPlayer player, HunterPlayerData data, LivingEntity target) {
        Vec3 targetForward = target.getLookAngle().multiply(1.0D, 0.0D, 1.0D);
        if (targetForward.lengthSqr() < 1.0E-4D) {
            targetForward = target.position().subtract(player.position()).multiply(1.0D, 0.0D, 1.0D);
        }
        if (targetForward.lengthSqr() < 1.0E-4D) {
            targetForward = player.getLookAngle().multiply(1.0D, 0.0D, 1.0D).reverse();
        }
        targetForward = targetForward.normalize();
        Vec3 behind = target.position().subtract(targetForward.scale(1.25D));
        Vec3 before = player.position();
        Vec3 safeBehind = TeleportSafetyHelper.resolveAroundTarget(player, new Vec3(behind.x, target.getY(), behind.z));
        HunterNetwork.sendToTrackingAndSelf(player, new AfterImagePacket(player.getUUID(), before.x, before.y, before.z, player.getYRot(), player.getXRot(), 14));
        player.teleportTo(safeBehind.x, safeBehind.y, safeBehind.z);
        player.lookAt(EntityAnchorArgument.Anchor.EYES, target, EntityAnchorArgument.Anchor.EYES);
        player.swing(InteractionHand.MAIN_HAND, true);
        data.triggerAnimation(AnimationType.BOXER_COUNTER_STRIKE);
        playTeleportReleaseSound(player, 0.95F);
        playPunchReleaseSound(player, 0.64F);

        Vec3 punchDir = target.position().subtract(player.position()).add(0.0D, target.getBbHeight() * 0.45D, 0.0D);
        punchDir = punchDir.lengthSqr() > 1.0E-4D ? punchDir.normalize() : targetForward;
        Vec3 stomach = target.position().add(0.0D, target.getBbHeight() * 0.48D, 0.0D).subtract(targetForward.scale(0.12D));
        target.invulnerableTime = 0;
        target.hurt(HunterDamageSources.physical(player.level(), player), this.getWeaponScaledDamage(player, DAMAGE));
        target.invulnerableTime = 0;
        HunterDataUtil.applyStun(target, player, 16);
        target.setDeltaMovement(punchDir.x * 0.35D, 0.12D, punchDir.z * 0.35D);
        target.hurtMarked = true;

        if (player.level() instanceof ServerLevel serverLevel) {
            BoxingBloodBurstEntity.spawn(serverLevel, stomach, punchDir, 1.35F, 18);
            serverLevel.sendParticles(ParticleTypes.DAMAGE_INDICATOR, stomach.x, stomach.y, stomach.z, 18, 0.55D, 0.35D, 0.55D, 0.08D);
            serverLevel.sendParticles(ParticleTypes.CRIT, stomach.x, stomach.y, stomach.z, 32, 0.5D, 0.28D, 0.5D, 0.1D);
            serverLevel.sendParticles(ParticleTypes.SQUID_INK, stomach.x, stomach.y, stomach.z, 20, 0.4D, 0.26D, 0.4D, 0.08D);
            serverLevel.sendParticles(ParticleTypes.PORTAL, before.x, before.y + 1.0D, before.z, 14, 0.2D, 0.35D, 0.2D, 0.08D);
        }
        this.startCooldown(data, this.getMaxCooldownTicks());
        HunterDataUtil.sync(player);
    }

    private LivingEntity findTarget(ServerPlayer player, double range) {
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle().normalize();
        AABB searchBox = player.getBoundingBox().expandTowards(look.scale(range)).inflate(1.6D);
        LivingEntity closest = null;
        double closestDistance = range * range;
        for (LivingEntity living : player.level().getEntitiesOfClass(LivingEntity.class, searchBox, entity -> entity != player && entity.isAlive())) {
            Vec3 hit = living.getBoundingBox().inflate(0.45D).clip(eye, eye.add(look.scale(range))).orElse(null);
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
