package com.huntercraft.huntercraft.abilities.defensetree;

import com.huntercraft.huntercraft.abilities.SkillTreeCombatAbility;
import com.huntercraft.huntercraft.animation.AnimationType;
import com.huntercraft.huntercraft.damage.HunterDamageSources;
import com.huntercraft.huntercraft.data.HunterPlayerData;
import com.huntercraft.huntercraft.entity.AegisSlamEffectEntity;
import com.huntercraft.huntercraft.entity.DefensePulseEntity;
import com.huntercraft.huntercraft.entity.SlashEffectEntity;
import com.huntercraft.huntercraft.progression.SkillNode;
import com.huntercraft.huntercraft.util.HunterDataUtil;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class IchimonjiAbility extends SkillTreeCombatAbility {
    private static final int CHARGE_TICKS = 12;
    private static final double RANGE = 8.25D;
    private static final double WIDTH = 2.8D;
    private static final float DAMAGE = 62.0F;
    private static final int COLOR = 0xFF2A2A;

    public IchimonjiAbility() {
        super("ichimonji", "Ichimonji", "Plant your stance and carve an unavoidable line through the ground, breaking the terrain beneath the cut.", "textures/gui/abilities/ichimonji.png", SkillNode.DEFENSE, 65);
    }

    @Override
    public int getMaxCooldownTicks() {
        return 420;
    }

    @Override
    public int getChargeTicks(HunterPlayerData data) {
        return data.isChargingAbility(this.id()) ? CHARGE_TICKS - data.getChargeTicksRemaining() : 0;
    }

    @Override
    public void use(ServerPlayer player, HunterPlayerData data, Vec3 direction) {
        if (data.isChargingAbility(this.id()) || data.isActiveAbility(this.id())) {
            return;
        }
        Vec3 forward = resolveForward(player, direction);
        if (forward.lengthSqr() < 1.0E-4D) {
            return;
        }
        data.startChargingAbility(this.id(), CHARGE_TICKS, forward);
        data.triggerAnimation(AnimationType.HEAVEN_SPLITTER);
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
            if (player.level() instanceof ServerLevel serverLevel && data.getChargeTicksRemaining() % 3 == 0) {
                Vec3 focus = player.position().add(data.getChargeDirection().scale(1.0D)).add(0.0D, 0.85D, 0.0D);
                serverLevel.sendParticles(ParticleTypes.CRIT, focus.x, focus.y, focus.z, 6, 0.22D, 0.18D, 0.22D, 0.04D);
            }
            return;
        }
        Vec3 forward = resolveForward(player, data.getChargeDirection());
        data.clearChargingAbility();
        strike(player, data, forward);
    }

    private void strike(ServerPlayer player, HunterPlayerData data, Vec3 forward) {
        Vec3 start = player.position().add(0.0D, 0.75D, 0.0D);
        Vec3 end = start.add(forward.scale(RANGE));
        AABB hitBox = new AABB(start, end).inflate(WIDTH, 1.8D, WIDTH);
        for (LivingEntity target : player.level().getEntitiesOfClass(LivingEntity.class, hitBox, entity -> entity != player && entity.isAlive())) {
            Vec3 center = target.position().add(0.0D, target.getBbHeight() * 0.5D, 0.0D);
            if (distanceToLineSqr(start, end, center) > WIDTH * WIDTH) {
                continue;
            }
            target.hurt(HunterDamageSources.unavoidableWeapon(player.level(), player), this.getWeaponScaledDamage(player, DAMAGE));
            target.setDeltaMovement(forward.x * 0.55D, -0.2D, forward.z * 0.55D);
            target.hurtMarked = true;
        }
        if (player.level() instanceof ServerLevel serverLevel) {
            Vec3 impact = player.position().add(forward.scale(3.0D)).add(0.0D, 0.52D, 0.0D);
            playSlashReleaseSound(player, 0.68F);
            playGroundSmashReleaseSound(player, 0.72F);
            DefensePulseEntity.spawnImpact(serverLevel, impact.add(0.0D, -0.45D, 0.0D), player.getYRot(), 1.45F, 18, COLOR);
            AegisSlamEffectEntity.spawn(serverLevel, impact.add(0.0D, -0.55D, 0.0D), player.getYRot(), 1.55F, 20, COLOR);
            SkybreakerDiveAbility.launchImpactBlocks(serverLevel, impact);
            for (int i = 0; i < 5; i++) {
                Vec3 point = player.position().add(forward.scale(1.3D + i * 1.25D)).add(0.0D, 1.0D, 0.0D);
                SlashEffectEntity.spawn(serverLevel, point, forward, i % 2 == 0 ? 0.0F : 180.0F, 1.3F - i * 0.08F, 16, COLOR);
                serverLevel.sendParticles(ParticleTypes.SWEEP_ATTACK, point.x, point.y, point.z, 2, 0.2D, 0.08D, 0.2D, 0.0D);
            }
            serverLevel.sendParticles(ParticleTypes.EXPLOSION, impact.x, impact.y, impact.z, 3, 0.25D, 0.0D, 0.25D, 0.0D);
            serverLevel.sendParticles(ParticleTypes.CLOUD, impact.x, impact.y - 0.2D, impact.z, 58, 2.1D, 0.18D, 2.1D, 0.09D);
        }
        this.startCooldown(data, this.getMaxCooldownTicks());
        HunterDataUtil.sync(player);
    }

    private double distanceToLineSqr(Vec3 start, Vec3 end, Vec3 point) {
        Vec3 line = end.subtract(start);
        double lengthSqr = line.lengthSqr();
        if (lengthSqr < 1.0E-4D) {
            return point.distanceToSqr(start);
        }
        double t = Math.max(0.0D, Math.min(1.0D, point.subtract(start).dot(line) / lengthSqr));
        return point.distanceToSqr(start.add(line.scale(t)));
    }

    private Vec3 resolveForward(ServerPlayer player, Vec3 direction) {
        Vec3 forward = direction.lengthSqr() > 1.0E-4D ? direction : player.getLookAngle();
        forward = new Vec3(forward.x, 0.0D, forward.z);
        return forward.lengthSqr() > 1.0E-4D ? forward.normalize() : Vec3.ZERO;
    }
}
