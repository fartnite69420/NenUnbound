package com.huntercraft.huntercraft.abilities.defensetree;

import com.huntercraft.huntercraft.abilities.SkillTreeCombatAbility;
import com.huntercraft.huntercraft.animation.AnimationType;
import com.huntercraft.huntercraft.data.HunterPlayerData;
import com.huntercraft.huntercraft.entity.DefensePulseEntity;
import com.huntercraft.huntercraft.progression.SkillNode;
import com.huntercraft.huntercraft.util.HunterDataUtil;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

public class BulwarkCannonAbility extends SkillTreeCombatAbility {
    private static final int CHARGE_TICKS = 8;
    private static final double PROJECTILE_SPEED = 1.65D;
    private static final int PROJECTILE_LIFE = 28;
    private static final float DAMAGE = 32.0F;
    private static final int COLOR = 0xEDE7FF;

    public BulwarkCannonAbility() {
        super("bulwark_cannon", "Bulwark Cannon", "Fire a compressed defensive shock projectile that blasts the first target it hits backward.", "textures/gui/abilities/bulwark_cannon.png", SkillNode.DEFENSE, 55);
    }

    @Override
    public int getMaxCooldownTicks() {
        return 240;
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
        data.triggerAnimation(AnimationType.AEGIS_RUSH);
        HunterDataUtil.sync(player);
    }

    @Override
    public void tick(ServerPlayer player, HunterPlayerData data) {
        if (!data.isChargingAbility(this.id())) {
            return;
        }
        data.tickChargingAbility();
        Vec3 forward = resolveForward(player, data.getChargeDirection());
        if (data.getChargeTicksRemaining() > 0) {
            if (player.level() instanceof ServerLevel serverLevel && data.getChargeTicksRemaining() % 2 == 0) {
                Vec3 focus = player.getEyePosition().add(forward.scale(0.9D));
                serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK, focus.x, focus.y, focus.z, 8, 0.18D, 0.16D, 0.18D, 0.05D);
            }
            return;
        }
        data.clearChargingAbility();
        fire(player, data, forward);
    }

    private void fire(ServerPlayer player, HunterPlayerData data, Vec3 forward) {
        if (player.level() instanceof ServerLevel serverLevel) {
            Vec3 spawn = player.getEyePosition().add(forward.scale(1.05D)).subtract(0.0D, 0.18D, 0.0D);
            DefensePulseEntity.spawnProjectile(serverLevel, player, spawn, forward, this.getWeaponScaledDamage(player, DAMAGE), PROJECTILE_SPEED, PROJECTILE_LIFE, COLOR);
            serverLevel.sendParticles(ParticleTypes.CLOUD, spawn.x, spawn.y, spawn.z, 16, 0.22D, 0.2D, 0.22D, 0.04D);
            serverLevel.sendParticles(ParticleTypes.CRIT, spawn.x, spawn.y, spawn.z, 16, 0.2D, 0.18D, 0.2D, 0.06D);
        }
        playSlashReleaseSound(player, 0.9F);
        this.startCooldown(data, this.getMaxCooldownTicks());
        HunterDataUtil.sync(player);
    }

    private Vec3 resolveForward(ServerPlayer player, Vec3 direction) {
        Vec3 forward = direction.lengthSqr() > 1.0E-4D ? direction : player.getLookAngle();
        return forward.lengthSqr() > 1.0E-4D ? forward.normalize() : Vec3.ZERO;
    }
}
