package com.huntercraft.huntercraft.abilities.defensetree;

import com.huntercraft.huntercraft.abilities.SkillTreeCombatAbility;
import com.huntercraft.huntercraft.animation.AnimationType;
import com.huntercraft.huntercraft.damage.HunterDamageSources;
import com.huntercraft.huntercraft.data.HunterPlayerData;
import com.huntercraft.huntercraft.entity.MirrorReprisalEffectEntity;
import com.huntercraft.huntercraft.progression.SkillNode;
import com.huntercraft.huntercraft.util.HunterDataUtil;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class MirrorReprisalAbility extends SkillTreeCombatAbility {
    private static final int WINDOW_TICKS = 10;
    private static final float BASE_DAMAGE = 50.0F;
    private static final double STRIKE_RADIUS = 2.35D;

    public MirrorReprisalAbility() {
        super("mirror_reprisal", "Mirror Reprisal", "Enter a short 0.5 second counter stance and instantly punish the next attacker with a crushing return slash.", "textures/gui/abilities/mirror_reprisal.png", SkillNode.DEFENSE, 40);
    }

    @Override
    public int getMaxCooldownTicks() {
        return 170;
    }

    @Override
    public boolean isContinuous() {
        return true;
    }

    @Override
    public int getActiveTicks(HunterPlayerData data) {
        return data.isActiveAbility(this.id()) ? WINDOW_TICKS - data.getActiveAbilityTicksRemaining() : 0;
    }

    @Override
    public void use(ServerPlayer player, HunterPlayerData data, Vec3 direction) {
        if (data.isActiveAbility(this.id())) {
            return;
        }
        Vec3 forward = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : player.getLookAngle().normalize();
        data.startActiveAbility(this.id(), WINDOW_TICKS, forward);
        data.triggerAnimation(AnimationType.MIRROR_REPRISAL_GUARD);
        if (player.level() instanceof ServerLevel serverLevel) {
            MirrorReprisalEffectEntity.spawn(serverLevel, player.position().add(0.0D, 0.03D, 0.0D), forward, MirrorReprisalEffectEntity.MODE_GUARD, 1.0F, WINDOW_TICKS + 4);
            serverLevel.sendParticles(ParticleTypes.CRIT, player.getX(), player.getY() + 0.9D, player.getZ(), 10, 0.45D, 0.45D, 0.45D, 0.03D);
        }
    }

    @Override
    public void tick(ServerPlayer player, HunterPlayerData data) {
        if (!data.isActiveAbility(this.id())) {
            return;
        }
        player.setDeltaMovement(Vec3.ZERO);
        player.hasImpulse = true;
        data.tickActiveAbility();
        if (data.getActiveAbilityTicksRemaining() <= 0) {
            data.clearActiveAbility();
            this.startCooldown(data, this.getMaxCooldownTicks());
            HunterDataUtil.sync(player);
        }
    }

    public boolean tryCounter(ServerPlayer player, HunterPlayerData data, LivingEntity attacker) {
        if (!data.isActiveAbility(this.id()) || attacker == null || !attacker.isAlive()) {
            return false;
        }

        Vec3 toAttacker = attacker.position().subtract(player.position());
        Vec3 forward = toAttacker.lengthSqr() > 1.0E-4D ? toAttacker.normalize() : player.getLookAngle().normalize();
        Vec3 destination = attacker.position().subtract(forward.scale(0.7D));
        player.teleportTo(destination.x, attacker.getY(), destination.z);
        player.lookAt(EntityAnchorArgument.Anchor.EYES, attacker.getEyePosition());
        data.clearActiveAbility();
        data.triggerAnimation(AnimationType.MIRROR_REPRISAL_STRIKE);
        attacker.hurt(HunterDamageSources.weapon(player.level(), player), this.getWeaponScaledDamage(player, BASE_DAMAGE));
        HunterDataUtil.applyStun(attacker, player, 24);
        attacker.knockback(0.6F, -forward.x, -forward.z);

        if (player.level() instanceof ServerLevel serverLevel) {
            Vec3 center = attacker.position().add(0.0D, 0.12D, 0.0D);
            MirrorReprisalEffectEntity.spawn(serverLevel, player.position().add(0.0D, 0.05D, 0.0D), forward, MirrorReprisalEffectEntity.MODE_STRIKE, 1.18F, 16);
            MirrorReprisalEffectEntity.spawn(serverLevel, center, forward, MirrorReprisalEffectEntity.MODE_STRIKE, 0.86F, 13);
            for (LivingEntity target : serverLevel.getEntitiesOfClass(LivingEntity.class, attacker.getBoundingBox().inflate(STRIKE_RADIUS), entity -> entity != player && entity != attacker && entity.isAlive())) {
                target.hurt(HunterDamageSources.weapon(player.level(), player), this.getWeaponScaledDamage(player, BASE_DAMAGE * 0.36F));
                target.knockback(0.42F, -forward.x, -forward.z);
            }
            serverLevel.sendParticles(ParticleTypes.SWEEP_ATTACK, attacker.getX(), attacker.getEyeY(), attacker.getZ(), 5, 0.55D, 0.18D, 0.55D, 0.0D);
            serverLevel.sendParticles(ParticleTypes.CRIT, attacker.getX(), attacker.getEyeY(), attacker.getZ(), 26, 0.7D, 0.4D, 0.7D, 0.08D);
            serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK, attacker.getX(), attacker.getEyeY(), attacker.getZ(), 18, 0.5D, 0.3D, 0.5D, 0.08D);
            serverLevel.sendParticles(ParticleTypes.FLASH, attacker.getX(), attacker.getEyeY(), attacker.getZ(), 1, 0.0D, 0.0D, 0.0D, 0.0D);
        }

        this.startCooldown(data, this.getMaxCooldownTicks());
        HunterDataUtil.sync(player);
        return true;
    }
}
