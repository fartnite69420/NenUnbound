package com.huntercraft.huntercraft.abilities.martialartstree;

import com.huntercraft.huntercraft.abilities.GrabAbility;
import com.huntercraft.huntercraft.abilities.SkillTreeCombatAbility;
import com.huntercraft.huntercraft.animation.AnimationType;
import com.huntercraft.huntercraft.damage.HunterDamageSources;
import com.huntercraft.huntercraft.data.HunterPlayerData;
import com.huntercraft.huntercraft.entity.RisingShotEffectEntity;
import com.huntercraft.huntercraft.progression.SkillNode;
import com.huntercraft.huntercraft.util.HunterDataUtil;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class RisingShotAbility extends SkillTreeCombatAbility implements GrabAbility {
    private static final int CHARGE_TICKS = 10;
    private static final int HOLD_TICKS = 24;
    private static final float BASE_DAMAGE = 30.0F;
    private static final double LAUNCH_VERTICAL_SPEED = 1.85D;
    private static final double KICK_FORWARD_SPEED = 0.5D;

    public RisingShotAbility() {
        super("rising_shot", "Rising Shot", "Kick the first target in front of you upward. On hit, they are launched into the air and briefly held for follow-up martial arts attacks.", "textures/gui/abilities/rising_shot.png", SkillNode.MARTIAL_ARTS, 35);
    }

    @Override
    public int getMaxCooldownTicks() {
        return 170;
    }

    @Override
    public int getChargeTicks(HunterPlayerData data) {
        return data.isChargingAbility(this.id()) ? CHARGE_TICKS - data.getChargeTicksRemaining() : 0;
    }

    @Override
    public boolean isContinuous() {
        return true;
    }

    @Override
    public int getActiveTicks(HunterPlayerData data) {
        return MartialArtsGrabHelper.isGrabSourceActive(data, this.id()) ? data.getMartialArtsGrabTicksRemaining() : 0;
    }

    @Override
    public void use(ServerPlayer player, HunterPlayerData data, Vec3 direction) {
        if (MartialArtsGrabHelper.isGrabSourceActive(data, this.id())) {
            stop(player, data);
            return;
        }
        if (data.isChargingAbility(this.id())) {
            return;
        }
        Vec3 forward = MartialArtsGrabHelper.getHorizontalForward(player, direction);
        data.startChargingAbility(this.id(), CHARGE_TICKS, forward);
        data.triggerAnimation(AnimationType.MARTIAL_RISING_SHOT_CHARGE);
        HunterDataUtil.sync(player);
    }

    private void executeKick(ServerPlayer player, HunterPlayerData data, Vec3 forward) {
        player.setDeltaMovement(forward.x * KICK_FORWARD_SPEED, 0.0D, forward.z * KICK_FORWARD_SPEED);
        player.hasImpulse = true;
        player.hurtMarked = true;
        data.triggerAnimation(AnimationType.MARTIAL_RISING_SHOT);
        playPunchReleaseSound(player, 1.12F);
        LivingEntity target = findKickTarget(player, forward);
        if (target == null) {
            this.startCooldown(data, this.getMaxCooldownTicks());
            HunterDataUtil.sync(player);
            return;
        }
        launchTarget(player, data, target, forward);
        this.startCooldown(data, this.getMaxCooldownTicks());
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
        Vec3 forward = data.getChargeDirection();
        data.clearChargingAbility();
        executeKick(player, data, forward);
    }

    private void launchTarget(ServerPlayer player, HunterPlayerData data, LivingEntity target, Vec3 forward) {
        target.hurt(HunterDamageSources.physical(player.level(), player), BASE_DAMAGE);
        target.setDeltaMovement(forward.x * 0.16D, LAUNCH_VERTICAL_SPEED, forward.z * 0.16D);
        target.hasImpulse = true;
        target.hurtMarked = true;
        player.setDeltaMovement(Vec3.ZERO);
        player.hasImpulse = true;
        player.fallDistance = 0.0F;
        MartialArtsGrabHelper.startOrRefreshGrab(player, data, target, HOLD_TICKS, this.id());
        data.triggerAnimation(AnimationType.MARTIAL_RISING_SHOT);

        if (player.level() instanceof ServerLevel serverLevel) {
            Vec3 center = target.position().add(0.0D, 0.1D, 0.0D);
            RisingShotEffectEntity.spawn(serverLevel, center, forward, 1.05F, 15);
            serverLevel.sendParticles(ParticleTypes.SWEEP_ATTACK, target.getX(), target.getY() + 0.65D, target.getZ(), 4, 0.2D, 0.18D, 0.2D, 0.0D);
            serverLevel.sendParticles(ParticleTypes.CRIT, target.getX(), target.getY() + 0.8D, target.getZ(), 18, 0.22D, 0.28D, 0.22D, 0.07D);
            serverLevel.sendParticles(ParticleTypes.CLOUD, target.getX(), target.getY() + 0.08D, target.getZ(), 14, 0.22D, 0.04D, 0.22D, 0.025D);
        }
    }

    private LivingEntity findKickTarget(ServerPlayer player, Vec3 forward) {
        AABB hitBox = player.getBoundingBox().expandTowards(forward.scale(2.25D)).inflate(0.85D, 0.75D, 0.85D);
        LivingEntity closest = null;
        double closestDistance = Double.MAX_VALUE;
        for (LivingEntity target : player.level().getEntitiesOfClass(LivingEntity.class, hitBox, entity -> entity != player && entity.isAlive())) {
            Vec3 toTarget = target.position().subtract(player.position()).multiply(1.0D, 0.0D, 1.0D);
            if (toTarget.lengthSqr() > 1.0E-4D && toTarget.normalize().dot(forward) < 0.12D) {
                continue;
            }
            double distance = target.distanceToSqr(player);
            if (distance < closestDistance) {
                closest = target;
                closestDistance = distance;
            }
        }
        return closest;
    }

    @Override
    public void stop(ServerPlayer player, HunterPlayerData data) {
        boolean canceled = false;
        if (MartialArtsGrabHelper.isGrabSourceActive(data, this.id())) {
            MartialArtsGrabHelper.clearGrab(data);
            canceled = true;
        }
        if (canceled) {
            data.triggerAnimation(AnimationType.NONE);
            this.startCooldown(data, this.getMaxCooldownTicks());
            HunterDataUtil.sync(player);
        }
    }
}
