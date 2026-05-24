package com.huntercraft.huntercraft.abilities.martialartstree;

import com.huntercraft.huntercraft.abilities.HunterAbilities;
import com.huntercraft.huntercraft.abilities.SkillTreeCombatAbility;
import com.huntercraft.huntercraft.animation.AnimationType;
import com.huntercraft.huntercraft.data.HunterPlayerData;
import com.huntercraft.huntercraft.data.HunterPlayerDataProvider;
import com.huntercraft.huntercraft.entity.ParrySparkEffectEntity;
import com.huntercraft.huntercraft.faction.FactionUtil;
import com.huntercraft.huntercraft.network.HunterNetwork;
import com.huntercraft.huntercraft.network.packet.AfterImagePacket;
import com.huntercraft.huntercraft.util.HunterDataUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public final class MartialArtsGrabHelper {
    private static final double AIR_GRAB_HEIGHT_STEP = 20.0D;
    private static final double GRAB_FRONT_OFFSET = 0.9D;
    private static final double GRAB_PLAYER_OFFSET = 0.55D;
    private static final double GRAB_TARGET_HEIGHT = 0.85D;
    private static final int ACTIVE_GRAB_STUN_TICKS = 2;

    private MartialArtsGrabHelper() {
    }

    public static LivingEntity findTarget(ServerPlayer player, double range, double inflate) {
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle().normalize();
        AABB searchBox = player.getBoundingBox().expandTowards(look.scale(range)).inflate(inflate);
        LivingEntity closest = null;
        double closestDistance = range * range;
        for (LivingEntity living : player.level().getEntitiesOfClass(LivingEntity.class, searchBox, entity ->
                entity != player && entity.isAlive() && !(entity instanceof Player targetPlayer && FactionUtil.areFactionMates(player, targetPlayer)))) {
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

    public static LivingEntity resolveGrabTarget(ServerPlayer player, HunterPlayerData data) {
        if (!(player.level() instanceof ServerLevel serverLevel) || !data.isMartialArtsGrabActive()) {
            return null;
        }
        try {
            return serverLevel.getEntity(UUID.fromString(data.getMartialArtsGrabTargetUuid())) instanceof LivingEntity living ? living : null;
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    public static LivingEntity findReleaseGrabTarget(ServerPlayer player, HunterPlayerData data, double range, double inflate) {
        LivingEntity target = data.isMartialArtsGrabActive()
                ? resolveGrabTarget(player, data)
                : findTarget(player, range, inflate);
        if (target == null || !target.isAlive()) {
            return null;
        }
        if (isGrabParried(target)) {
            return null;
        }
        return target;
    }

    public static void startOrRefreshGrab(ServerPlayer player, HunterPlayerData data, LivingEntity target, int holdTicks, String sourceAbilityId) {
        LivingEntity activeTarget = resolveGrabTarget(player, data);
        LivingEntity grabTarget = activeTarget != null && activeTarget.isAlive() ? activeTarget : target;
        if (grabTarget == null || !grabTarget.isAlive()) {
            return;
        }
        if (data.isMartialArtsGrabActive()) {
            String previousSource = data.getMartialArtsGrabSourceAbilityId();
            if (!previousSource.isBlank() && !previousSource.equals(sourceAbilityId)) {
                startGrabCooldown(data, previousSource);
            }
        }

        Vec3 previous = player.position();
        teleportPairToGrabHeight(player, data, grabTarget);
        alignGrabPair(player, grabTarget);
        data.startMartialArtsGrab(sourceAbilityId, grabTarget.getUUID().toString(), holdTicks);
        data.setAirLaunchFallProtection(true);
        HunterDataUtil.applyStun(grabTarget, player, ACTIVE_GRAB_STUN_TICKS);
        spawnLiftVisuals(player, previous, grabTarget.position());
    }

    public static void sustainGrab(ServerPlayer player, HunterPlayerData data) {
        LivingEntity target = resolveGrabTarget(player, data);
        if (target == null || !target.isAlive()) {
            String activeSource = data.getMartialArtsGrabSourceAbilityId();
            data.clearMartialArtsGrab();
            if (!activeSource.isBlank()) {
                startGrabCooldown(data, activeSource);
                HunterDataUtil.sync(player);
            }
            return;
        }
        alignGrabPair(player, target);
        player.setDeltaMovement(Vec3.ZERO);
        player.hasImpulse = true;
        player.fallDistance = 0.0F;
        target.setDeltaMovement(Vec3.ZERO);
        target.hurtMarked = true;
        target.fallDistance = 0.0F;
        HunterDataUtil.applyStun(target, player, ACTIVE_GRAB_STUN_TICKS);
        data.setAirLaunchFallProtection(true);
        String activeSource = data.getMartialArtsGrabSourceAbilityId();
        data.tickMartialArtsGrab();
        if (!data.isMartialArtsGrabActive() && !activeSource.isBlank()) {
            startGrabCooldown(data, activeSource);
            HunterDataUtil.sync(player);
        }
    }

    public static void clearGrab(HunterPlayerData data) {
        String activeSource = data.getMartialArtsGrabSourceAbilityId();
        data.clearMartialArtsGrab();
        if (!activeSource.isBlank()) {
            startGrabCooldown(data, activeSource);
        }
    }

    public static boolean isGrabSourceActive(HunterPlayerData data, String abilityId) {
        return data.isMartialArtsGrabActive() && abilityId.equals(data.getMartialArtsGrabSourceAbilityId());
    }

    public static void slamTarget(ServerPlayer player, LivingEntity target, double horizontalPush, double downwardSpeed, int stunTicks) {
        Vec3 forward = getHorizontalForward(player, target.position().subtract(player.position()));
        target.setDeltaMovement(forward.x * horizontalPush, downwardSpeed, forward.z * horizontalPush);
        target.hurtMarked = true;
        HunterDataUtil.applyStun(target, player, stunTicks);
    }

    public static Vec3 getHorizontalForward(ServerPlayer player, Vec3 fallback) {
        Vec3 forward = player.getLookAngle().multiply(1.0D, 0.0D, 1.0D);
        if (forward.lengthSqr() < 1.0E-4D) {
            forward = fallback.multiply(1.0D, 0.0D, 1.0D);
        }
        if (forward.lengthSqr() < 1.0E-4D) {
            forward = new Vec3(0.0D, 0.0D, 1.0D);
        }
        return forward.normalize();
    }

    private static void teleportPairToGrabHeight(ServerPlayer player, HunterPlayerData data, LivingEntity target) {
        double targetY = data.isMartialArtsGrabActive()
                ? Math.max(player.getY(), target.getY()) + AIR_GRAB_HEIGHT_STEP
                : getGroundAnchorY(player, target) + AIR_GRAB_HEIGHT_STEP;
        player.teleportTo(player.getX(), targetY, player.getZ());
        target.teleportTo(target.getX(), targetY + 0.35D, target.getZ());
        player.setDeltaMovement(Vec3.ZERO);
        player.hasImpulse = true;
        target.setDeltaMovement(Vec3.ZERO);
        target.hurtMarked = true;
        player.fallDistance = 0.0F;
        target.fallDistance = 0.0F;
    }

    private static double getGroundAnchorY(ServerPlayer player, LivingEntity target) {
        BlockPos surface = player.level().getHeightmapPos(
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                BlockPos.containing(target.getX(), target.getY(), target.getZ())
        );
        return surface.getY();
    }

    private static void alignGrabPair(ServerPlayer player, LivingEntity target) {
        Vec3 forward = getHorizontalForward(player, target.position().subtract(player.position()));
        double holdY = player.getY();
        Vec3 playerPos = new Vec3(
                target.getX() - (forward.x * GRAB_PLAYER_OFFSET),
                holdY,
                target.getZ() - (forward.z * GRAB_PLAYER_OFFSET)
        );
        Vec3 targetPos = new Vec3(
                playerPos.x + (forward.x * GRAB_FRONT_OFFSET),
                holdY + GRAB_TARGET_HEIGHT,
                playerPos.z + (forward.z * GRAB_FRONT_OFFSET)
        );
        player.teleportTo(playerPos.x, playerPos.y, playerPos.z);
        target.teleportTo(targetPos.x, targetPos.y, targetPos.z);
    }

    private static void spawnLiftVisuals(ServerPlayer player, Vec3 from, Vec3 to) {
        HunterNetwork.sendToTrackingAndSelf(player, new AfterImagePacket(
                player.getUUID(),
                from.x,
                from.y,
                from.z,
                player.getYRot(),
                player.getXRot(),
                10
        ));
        if (!(player.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        Vec3 center = to.add(0.0D, 0.8D, 0.0D);
        serverLevel.sendParticles(ParticleTypes.CLOUD, center.x, center.y, center.z, 10, 0.22D, 0.22D, 0.22D, 0.01D);
        serverLevel.sendParticles(ParticleTypes.CRIT, center.x, center.y, center.z, 10, 0.18D, 0.18D, 0.18D, 0.03D);
        serverLevel.sendParticles(ParticleTypes.SWEEP_ATTACK, center.x, center.y, center.z, 2, 0.12D, 0.08D, 0.12D, 0.0D);
    }

    private static void startGrabCooldown(HunterPlayerData data, String abilityId) {
        if (HunterAbilities.byId(abilityId) instanceof SkillTreeCombatAbility combatAbility) {
            data.setAbilityCooldown(abilityId, combatAbility.getMaxCooldownTicks());
        }
    }

    private static boolean isGrabParried(LivingEntity target) {
        if (!(target instanceof ServerPlayer targetPlayer)) {
            return false;
        }
        HunterPlayerData targetData = targetPlayer.getCapability(HunterPlayerDataProvider.CAPABILITY).orElse(null);
        if (targetData == null || !targetData.isGuardParrying()) {
            return false;
        }
        targetData.triggerAnimation(AnimationType.PARRY);
        if (targetPlayer.level() instanceof ServerLevel serverLevel) {
            Vec3 direction = targetPlayer.getLookAngle().multiply(1.0D, 0.0D, 1.0D);
            if (direction.lengthSqr() < 1.0E-4D) {
                direction = Vec3.directionFromRotation(0.0F, targetPlayer.getYRot());
            }
            Vec3 position = targetPlayer.position().add(direction.normalize().scale(0.18D)).add(0.0D, 0.88D, 0.0D);
            ParrySparkEffectEntity.spawn(serverLevel, position, direction, 1.0F, 12);
            serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK, position.x, position.y, position.z, 18, 0.28D, 0.22D, 0.28D, 0.045D);
        }
        HunterDataUtil.sync(targetPlayer);
        return true;
    }
}
