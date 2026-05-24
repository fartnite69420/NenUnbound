package com.huntercraft.huntercraft.abilities.boxertree;

import com.huntercraft.huntercraft.abilities.SkillTreeCombatAbility;
import com.huntercraft.huntercraft.animation.AnimationType;
import com.huntercraft.huntercraft.damage.HunterDamageSources;
import com.huntercraft.huntercraft.data.HunterPlayerData;
import com.huntercraft.huntercraft.entity.HammerShockwaveEntity;
import com.huntercraft.huntercraft.progression.SkillNode;
import com.huntercraft.huntercraft.util.HunterDataUtil;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class HammerJabAbility extends SkillTreeCombatAbility {
    private static final int CHARGE_TICKS = 6;
    private static final double RANGE = 7.2D;
    private static final double WIDTH = 2.6D;
    private static final float DAMAGE = 42.0F;
    private static final double KNOCKBACK = 5.4D;
    private static final double LIFT = 0.42D;

    public HammerJabAbility() {
        super("hammer_jab", "Hammer Jab", "Drive a sharp jab forward with a crushing shockwave that sends enemies flying.", "textures/gui/abilities/hammer_jab.png", SkillNode.BOXING, 10);
    }

    @Override
    public int getMaxCooldownTicks() {
        return 140;
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
        Vec3 aim = direction.lengthSqr() > 1.0E-4D ? direction : player.getLookAngle();
        Vec3 forward = new Vec3(aim.x, 0.0D, aim.z);
        if (forward.lengthSqr() > 1.0E-4D) {
            forward = forward.normalize();
        }
        if (forward.lengthSqr() < 1.0E-4D) {
            return;
        }
        data.startChargingAbility(this.id(), CHARGE_TICKS, forward);
        data.triggerAnimation(AnimationType.BOXER_HAMMER_STRIKE);
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
        executeJab(player, data, forward);
    }

    private void executeJab(ServerPlayer player, HunterPlayerData data, Vec3 forward) {
        player.swing(InteractionHand.MAIN_HAND, true);
        data.triggerAnimation(AnimationType.BOXER_HAMMER_STRIKE);
        playPunchReleaseSound(player, 0.76F);
        Vec3 center = player.position().add(forward.scale(RANGE * 0.55D)).add(0.0D, 1.0D, 0.0D);
        AABB hitBox = player.getBoundingBox().expandTowards(forward.scale(RANGE)).inflate(WIDTH, 1.15D, WIDTH);
        for (LivingEntity target : player.level().getEntitiesOfClass(LivingEntity.class, hitBox, entity -> entity != player && entity.isAlive())) {
            Vec3 toTarget = target.position().subtract(player.position());
            double forwardDot = toTarget.normalize().dot(forward);
            if (forwardDot < 0.42D) {
                continue;
            }
            target.hurt(HunterDamageSources.physical(player.level(), player), DAMAGE);
            HunterDataUtil.applyStun(target, player, 16);
            Vec3 currentMotion = target.getDeltaMovement();
            target.setDeltaMovement(forward.x * KNOCKBACK, Math.max(currentMotion.y, LIFT), forward.z * KNOCKBACK);
            target.hasImpulse = true;
            target.hurtMarked = true;
        }
        if (player.level() instanceof ServerLevel serverLevel) {
            HammerShockwaveEntity.spawn(serverLevel, player.position().add(forward.scale(0.8D)).add(0.0D, 0.35D, 0.0D), forward, 1.22F, 16);
            serverLevel.sendParticles(ParticleTypes.EXPLOSION, center.x, center.y - 0.15D, center.z, 1, 0.2D, 0.05D, 0.2D, 0.0D);
            serverLevel.sendParticles(ParticleTypes.CLOUD, center.x, center.y - 0.3D, center.z, 34, 1.1D, 0.12D, 1.1D, 0.08D);
            serverLevel.sendParticles(ParticleTypes.CRIT, center.x, center.y, center.z, 24, 0.8D, 0.35D, 0.8D, 0.08D);
            serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK, center.x, center.y, center.z, 18, 0.6D, 0.25D, 0.6D, 0.06D);
        }
        this.startCooldown(data, this.getMaxCooldownTicks());
        HunterDataUtil.sync(player);
    }
}
