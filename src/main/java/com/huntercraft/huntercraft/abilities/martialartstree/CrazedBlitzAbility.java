package com.huntercraft.huntercraft.abilities.martialartstree;

import com.huntercraft.huntercraft.abilities.SkillTreeCombatAbility;
import com.huntercraft.huntercraft.animation.AnimationType;
import com.huntercraft.huntercraft.damage.HunterDamageSources;
import com.huntercraft.huntercraft.data.HunterPlayerData;
import com.huntercraft.huntercraft.entity.ToraHuntEffectEntity;
import com.huntercraft.huntercraft.progression.SkillNode;
import com.huntercraft.huntercraft.util.HunterDataUtil;
import com.huntercraft.huntercraft.util.TeleportSafetyHelper;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class CrazedBlitzAbility extends SkillTreeCombatAbility {
    private static final int CHARGE_TICKS = 8;
    private static final int ACTIVE_TICKS = 42;
    private static final double RANGE = 10.5D;
    private static final float SLAM_DAMAGE = 18.0F;
    private static final float KICK_DAMAGE = 7.5F;
    private static final float FINISH_DAMAGE = 22.0F;

    public CrazedBlitzAbility() {
        super("crazed_blitz", "Crazed Blitz", "Throw a target to the ground, stomp them with repeated kicks, then launch them away.", "textures/gui/abilities/crazed_blitz.png", SkillNode.MARTIAL_ARTS, 65);
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
    public int getActiveTicks(HunterPlayerData data) {
        return data.isActiveAbility(this.id()) ? ACTIVE_TICKS - data.getActiveAbilityTicksRemaining() : 0;
    }

    @Override
    public void use(ServerPlayer player, HunterPlayerData data, Vec3 direction) {
        if (data.isChargingAbility(this.id()) || data.isActiveAbility(this.id())) {
            return;
        }
        LivingEntity target = MartialArtsGrabHelper.findTarget(player, RANGE, 1.5D);
        if (target == null) {
            return;
        }
        Vec3 forward = MartialArtsGrabHelper.getHorizontalForward(player, target.position().subtract(player.position()));
        data.startChargingAbility(this.id(), CHARGE_TICKS, forward);
        data.setChargeTargetUuid(target.getUUID().toString());
        data.triggerAnimation(AnimationType.MARTIAL_TORA_HUNT_CHARGE);
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
            Vec3 forward = data.getChargeDirection();
            data.clearChargingAbility();
            if (target == null || !target.isAlive()) {
                this.startCooldown(data, 10);
                HunterDataUtil.sync(player);
                return;
            }
            startBlitz(player, data, target, forward);
            return;
        }
        if (!data.isActiveAbility(this.id())) {
            return;
        }
        LivingEntity target = resolveTarget(player, data.getActiveAbilityTargetUuid());
        if (target == null || !target.isAlive()) {
            finish(player, data, null, data.getActiveAbilityDirection());
            return;
        }
        int elapsed = ACTIVE_TICKS - data.getActiveAbilityTicksRemaining();
        pinTarget(player, target, data.getActiveAbilityDirection());
        if (elapsed == 2 || elapsed == 7 || elapsed == 12 || elapsed == 17 || elapsed == 22 || elapsed == 27) {
            stomp(player, target, elapsed);
        }
        data.tickActiveAbility();
        if (data.getActiveAbilityTicksRemaining() <= 0) {
            finish(player, data, target, data.getActiveAbilityDirection());
        }
    }

    private void startBlitz(ServerPlayer player, HunterPlayerData data, LivingEntity target, Vec3 forward) {
        Vec3 safe = TeleportSafetyHelper.resolveAroundTarget(player, target.position().subtract(forward.scale(1.05D)));
        player.teleportTo(safe.x, safe.y, safe.z);
        player.lookAt(EntityAnchorArgument.Anchor.EYES, target, EntityAnchorArgument.Anchor.EYES);
        playDashReleaseSound(player, 1.02F);
        target.invulnerableTime = 0;
        target.hurt(HunterDamageSources.physical(player.level(), player), SLAM_DAMAGE);
        target.invulnerableTime = 0;
        pinTarget(player, target, forward);
        HunterDataUtil.applyStun(target, player, ACTIVE_TICKS + 8);
        data.startActiveAbility(this.id(), ACTIVE_TICKS, forward);
        data.setActiveAbilityTargetUuid(target.getUUID().toString());
        data.triggerAnimation(AnimationType.MARTIAL_TORA_HUNT);
        spawnGroundImpact(player, target, forward, true);
        HunterDataUtil.sync(player);
    }

    private void pinTarget(ServerPlayer player, LivingEntity target, Vec3 forward) {
        Vec3 horizontal = MartialArtsGrabHelper.getHorizontalForward(player, forward);
        double groundY = findGroundY(player, target);
        Vec3 targetPos = new Vec3(target.getX(), groundY + 0.05D, target.getZ());
        target.teleportTo(targetPos.x, targetPos.y, targetPos.z);
        target.setDeltaMovement(Vec3.ZERO);
        target.hurtMarked = true;
        target.fallDistance = 0.0F;
        Vec3 playerPos = targetPos.subtract(horizontal.scale(0.85D));
        player.teleportTo(playerPos.x, groundY, playerPos.z);
        player.setDeltaMovement(Vec3.ZERO);
        player.hasImpulse = true;
        player.fallDistance = 0.0F;
        player.lookAt(EntityAnchorArgument.Anchor.EYES, target, EntityAnchorArgument.Anchor.EYES);
    }

    private void stomp(ServerPlayer player, LivingEntity target, int elapsed) {
        player.swing(InteractionHand.MAIN_HAND, true);
        player.getCapability(com.huntercraft.huntercraft.data.HunterPlayerDataProvider.CAPABILITY).ifPresent(data -> data.triggerAnimation(AnimationType.MARTIAL_TORA_HUNT));
        playPunchReleaseSound(player, 0.82F + (elapsed % 3) * 0.04F);
        target.invulnerableTime = 0;
        target.hurt(HunterDamageSources.physical(player.level(), player), KICK_DAMAGE);
        target.invulnerableTime = 0;
        spawnGroundImpact(player, target, MartialArtsGrabHelper.getHorizontalForward(player, target.position().subtract(player.position())), false);
    }

    private void spawnGroundImpact(ServerPlayer player, LivingEntity target, Vec3 forward, boolean slam) {
        if (!(player.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        Vec3 center = target.position().add(0.0D, 0.18D, 0.0D);
        ToraHuntEffectEntity.spawn(serverLevel, center.subtract(forward.scale(0.35D)), forward, slam ? 1.25F : 0.82F, slam ? 18 : 10, slam ? ToraHuntEffectEntity.STYLE_FINISHER : ToraHuntEffectEntity.STYLE_STRIKE);
        serverLevel.sendParticles(ParticleTypes.SWEEP_ATTACK, center.x, center.y + 0.45D, center.z, slam ? 5 : 2, 0.28D, 0.08D, 0.28D, 0.0D);
        serverLevel.sendParticles(ParticleTypes.CRIT, center.x, center.y + 0.35D, center.z, slam ? 28 : 12, 0.42D, 0.16D, 0.42D, 0.08D);
        serverLevel.sendParticles(ParticleTypes.CLOUD, center.x, center.y, center.z, slam ? 22 : 8, 0.45D, 0.05D, 0.45D, 0.04D);
    }

    private void finish(ServerPlayer player, HunterPlayerData data, LivingEntity target, Vec3 direction) {
        Vec3 forward = MartialArtsGrabHelper.getHorizontalForward(player, direction);
        if (target != null && target.isAlive()) {
            target.invulnerableTime = 0;
            target.hurt(HunterDamageSources.physical(player.level(), player), FINISH_DAMAGE);
            target.invulnerableTime = 0;
            target.setDeltaMovement(forward.x * 2.85D, 0.55D, forward.z * 2.85D);
            target.hurtMarked = true;
            spawnGroundImpact(player, target, forward, true);
        }
        data.clearActiveAbility();
        this.startCooldown(data, this.getMaxCooldownTicks());
        HunterDataUtil.sync(player);
    }

    private double findGroundY(ServerPlayer player, LivingEntity target) {
        if (!(player.level() instanceof ServerLevel serverLevel)) {
            return target.getY();
        }
        BlockPos surface = serverLevel.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, BlockPos.containing(target.getX(), target.getY(), target.getZ()));
        return surface.getY();
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
