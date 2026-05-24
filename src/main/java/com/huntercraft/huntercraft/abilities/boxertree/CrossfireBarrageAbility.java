package com.huntercraft.huntercraft.abilities.boxertree;

import com.huntercraft.huntercraft.abilities.SkillTreeCombatAbility;
import com.huntercraft.huntercraft.animation.AnimationType;
import com.huntercraft.huntercraft.damage.HunterDamageSources;
import com.huntercraft.huntercraft.data.HunterPlayerData;
import com.huntercraft.huntercraft.entity.CrossfireBarrageEffectEntity;
import com.huntercraft.huntercraft.progression.SkillNode;
import com.huntercraft.huntercraft.util.HunterDataUtil;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class CrossfireBarrageAbility extends SkillTreeCombatAbility {
    private static final int CHARGE_TICKS = 6;
    private static final int ACTIVE_TICKS = 60;
    private static final int HIT_INTERVAL = 2;
    private static final float DAMAGE = 5.8F;
    private static final float KNOCKBACK = 0.18F;

    public CrossfireBarrageAbility() {
        super("crossfire_barrage", "Crossfire Barrage", "Unload a close-range continuous punch barrage that can be canceled early.", "textures/gui/abilities/crossfire_barrage.png", SkillNode.BOXING, 20);
    }

    @Override
    public int getMaxCooldownTicks() {
        return 220;
    }

    @Override
    public boolean isContinuous() {
        return true;
    }

    @Override
    public int getActiveTicks(HunterPlayerData data) {
        return data.isActiveAbility(this.id()) ? ACTIVE_TICKS - data.getActiveAbilityTicksRemaining() : 0;
    }

    @Override
    public int getChargeTicks(HunterPlayerData data) {
        return data.isChargingAbility(this.id()) ? CHARGE_TICKS - data.getChargeTicksRemaining() : 0;
    }

    @Override
    public void use(ServerPlayer player, HunterPlayerData data, Vec3 direction) {
        if (data.isActiveAbility(this.id())) {
            stop(player, data);
            return;
        }
        if (data.isChargingAbility(this.id())) {
            data.clearChargingAbility();
            HunterDataUtil.sync(player);
            return;
        }
        Vec3 forward = direction.lengthSqr() > 1.0E-4D ? new Vec3(direction.x, 0.0D, direction.z).normalize() : player.getLookAngle().multiply(1.0D, 0.0D, 1.0D).normalize();
        if (forward.lengthSqr() < 1.0E-4D) {
            return;
        }
        data.startChargingAbility(this.id(), CHARGE_TICKS, forward);
        data.triggerAnimation(AnimationType.BOXER_BARRAGE);
        HunterDataUtil.sync(player);
    }

    private void beginBarrage(ServerPlayer player, HunterPlayerData data, Vec3 forward) {
        data.startActiveAbility(this.id(), ACTIVE_TICKS, forward);
        data.triggerAnimation(AnimationType.BOXER_BARRAGE_FAST);
        playPunchReleaseSound(player, 1.18F);
        if (player.level() instanceof ServerLevel serverLevel) {
            CrossfireBarrageEffectEntity.spawn(serverLevel, player, forward, 1.0F, ACTIVE_TICKS);
        }
        hit(player, forward, 0);
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
                beginBarrage(player, data, forward);
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
        player.setDeltaMovement(Vec3.ZERO);
        player.hasImpulse = true;

        int elapsed = ACTIVE_TICKS - data.getActiveAbilityTicksRemaining();
        int remaining = data.getActiveAbilityTicksRemaining();
        if (remaining % HIT_INTERVAL == 0) {
            hit(player, forward, elapsed);
        }
        data.tickActiveAbility();
        if (data.getActiveAbilityTicksRemaining() <= 0) {
            finish(player, data);
        }
    }

    @Override
    public void stop(ServerPlayer player, HunterPlayerData data) {
        if (!data.isActiveAbility(this.id())) {
            return;
        }
        data.clearActiveAbility();
        data.triggerAnimation(AnimationType.NONE);
        this.startCooldown(data, this.getMaxCooldownTicks());
        HunterDataUtil.sync(player);
    }

    private void finish(ServerPlayer player, HunterPlayerData data) {
        data.clearActiveAbility();
        data.triggerAnimation(AnimationType.NONE);
        this.startCooldown(data, this.getMaxCooldownTicks());
        HunterDataUtil.sync(player);
    }

    private void hit(ServerPlayer player, Vec3 forward, int stage) {
        Vec3 center = player.position().add(forward.scale(1.75D)).add(0.0D, 1.0D, 0.0D);
        AABB hitBox = player.getBoundingBox().expandTowards(forward.scale(2.75D)).inflate(1.2D, 0.9D, 1.2D);
        for (LivingEntity target : player.level().getEntitiesOfClass(LivingEntity.class, hitBox, entity -> entity != player && entity.isAlive())) {
            Vec3 toTarget = target.position().subtract(player.position()).multiply(1.0D, 0.0D, 1.0D);
            if (toTarget.lengthSqr() > 1.0E-4D && toTarget.normalize().dot(forward) < 0.18D) {
                continue;
            }
            target.invulnerableTime = 0;
            target.hurt(HunterDamageSources.physical(player.level(), player), DAMAGE);
            target.invulnerableTime = 0;
            target.setDeltaMovement(forward.x * KNOCKBACK, KNOCKBACK * 0.5D, forward.z * KNOCKBACK);
            target.hasImpulse = true;
            target.hurtMarked = true;
        }
        if (player.level() instanceof ServerLevel serverLevel) {
            double sideX = -forward.z;
            double sideZ = forward.x;
            double offset = ((stage % 6) - 2.5D) * 0.18D;
            Vec3 fistPoint = center.add(sideX * offset, ((stage % 3) - 1) * 0.18D, sideZ * offset);
            serverLevel.sendParticles(ParticleTypes.CRIT, fistPoint.x, fistPoint.y, fistPoint.z, 5, 0.16D, 0.12D, 0.16D, 0.035D);
            serverLevel.sendParticles(ParticleTypes.CLOUD, fistPoint.x, fistPoint.y, fistPoint.z, 3 + (stage % 3), 0.12D, 0.1D, 0.12D, 0.012D);
            if (stage % 4 == 0) {
                serverLevel.sendParticles(ParticleTypes.SWEEP_ATTACK, fistPoint.x, fistPoint.y, fistPoint.z, 1, 0.08D, 0.04D, 0.08D, 0.0D);
            }
        }
    }
}
