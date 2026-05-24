package com.huntercraft.huntercraft.abilities.boxertree;

import com.huntercraft.huntercraft.abilities.SkillTreeCombatAbility;
import com.huntercraft.huntercraft.animation.AnimationType;
import com.huntercraft.huntercraft.data.HunterPlayerData;
import com.huntercraft.huntercraft.entity.BoxingShockwaveProjectileEntity;
import com.huntercraft.huntercraft.progression.SkillNode;
import com.huntercraft.huntercraft.util.HunterDataUtil;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.Vec3;

public class TitanCannonAbility extends SkillTreeCombatAbility {
    private static final int CHARGE_TICKS = 8;
    private static final float DAMAGE = 36.0F;
    private static final double SPEED = 1.85D;
    private static final double KNOCKBACK = 3.35D;

    public TitanCannonAbility() {
        super("titan_cannon", "Titan Cannon", "Punch a giant compressed shockwave projectile forward, blasting enemies away on contact.", "textures/gui/abilities/titan_cannon.png", SkillNode.BOXING, 55);
    }

    @Override
    public int getMaxCooldownTicks() {
        return 260;
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
        Vec3 forward = direction.lengthSqr() > 1.0E-4D ? direction : player.getLookAngle();
        if (forward.lengthSqr() < 1.0E-4D) {
            return;
        }
        data.startChargingAbility(this.id(), CHARGE_TICKS, forward.normalize());
        data.triggerAnimation(AnimationType.BOXER_HAMMER_STRIKE);
        HunterDataUtil.sync(player);
    }

    @Override
    public void tick(ServerPlayer player, HunterPlayerData data) {
        if (!data.isChargingAbility(this.id())) {
            return;
        }
        data.tickChargingAbility();
        Vec3 forward = data.getChargeDirection().lengthSqr() > 1.0E-4D ? data.getChargeDirection().normalize() : player.getLookAngle().normalize();
        if (data.getChargeTicksRemaining() > 0) {
            if (player.level() instanceof ServerLevel serverLevel && data.getChargeTicksRemaining() % 2 == 0) {
                Vec3 focus = player.getEyePosition().add(forward.scale(0.75D)).subtract(0.0D, 0.28D, 0.0D);
                serverLevel.sendParticles(ParticleTypes.CLOUD, focus.x, focus.y, focus.z, 6, 0.14D, 0.12D, 0.14D, 0.035D);
                serverLevel.sendParticles(ParticleTypes.CRIT, focus.x, focus.y, focus.z, 5, 0.12D, 0.1D, 0.12D, 0.05D);
            }
            return;
        }
        data.clearChargingAbility();
        fire(player, data, forward);
    }

    private void fire(ServerPlayer player, HunterPlayerData data, Vec3 forward) {
        player.swing(InteractionHand.MAIN_HAND, true);
        data.triggerAnimation(AnimationType.BOXER_HAMMER_STRIKE);
        playPunchReleaseSound(player, 0.74F);
        if (player.level() instanceof ServerLevel serverLevel) {
            Vec3 spawn = player.getEyePosition().add(forward.scale(1.15D)).subtract(0.0D, 0.25D, 0.0D);
            BoxingShockwaveProjectileEntity.spawn(serverLevel, player, spawn, forward, DAMAGE, SPEED, KNOCKBACK, 1.35F, 34);
            serverLevel.sendParticles(ParticleTypes.EXPLOSION, spawn.x, spawn.y, spawn.z, 1, 0.08D, 0.04D, 0.08D, 0.0D);
            serverLevel.sendParticles(ParticleTypes.CLOUD, spawn.x, spawn.y, spawn.z, 22, 0.28D, 0.18D, 0.28D, 0.08D);
            serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK, spawn.x, spawn.y, spawn.z, 18, 0.22D, 0.18D, 0.22D, 0.08D);
        }
        this.startCooldown(data, this.getMaxCooldownTicks());
        HunterDataUtil.sync(player);
    }
}
