package com.huntercraft.huntercraft.abilities.boxertree;

import com.huntercraft.huntercraft.abilities.SkillTreeCombatAbility;
import com.huntercraft.huntercraft.animation.AnimationType;
import com.huntercraft.huntercraft.damage.HunterDamageSources;
import com.huntercraft.huntercraft.data.HunterPlayerData;
import com.huntercraft.huntercraft.entity.LiverBreakEffectEntity;
import com.huntercraft.huntercraft.progression.SkillNode;
import com.huntercraft.huntercraft.util.HunterDataUtil;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class LiverBreakCounterAbility extends SkillTreeCombatAbility {
    private static final int WINDOW_TICKS = 10;
    private static final float DAMAGE = 45.0F;

    public LiverBreakCounterAbility() {
        super("liver_break_counter", "Liver Break", "Enter a short counter stance and drive a crushing body shot into the next attacker.", "textures/gui/abilities/liver_break.png", SkillNode.BOXING, 35);
    }

    @Override
    public int getMaxCooldownTicks() {
        return 240;
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
        data.startActiveAbility(this.id(), WINDOW_TICKS, direction);
        data.triggerAnimation(AnimationType.BOXER_COUNTER_GUARD);
        if (player.level() instanceof ServerLevel serverLevel) {
            Vec3 forward = direction.lengthSqr() > 1.0E-4D ? direction.multiply(1.0D, 0.0D, 1.0D).normalize() : player.getLookAngle().multiply(1.0D, 0.0D, 1.0D).normalize();
            if (forward.lengthSqr() < 1.0E-4D) {
                forward = Vec3.directionFromRotation(0.0F, player.getYRot());
            }
            LiverBreakEffectEntity.spawn(serverLevel, player.position().add(0.0D, 0.05D, 0.0D), forward, 0.92F, WINDOW_TICKS, LiverBreakEffectEntity.STYLE_GUARD);
            serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK, player.getX(), player.getY() + 1.0D, player.getZ(), 10, 0.32D, 0.42D, 0.32D, 0.012D);
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
        Vec3 position = attacker.position().subtract(forward.scale(0.85D));
        player.teleportTo(position.x, attacker.getY(), position.z);
        player.lookAt(EntityAnchorArgument.Anchor.EYES, attacker.getEyePosition());
        data.clearActiveAbility();
        data.triggerAnimation(AnimationType.BOXER_COUNTER_STRIKE);
        attacker.hurt(HunterDamageSources.physical(player.level(), player), DAMAGE);
        HunterDataUtil.applyStun(attacker, player, 16);
        attacker.knockback(0.85F, -forward.x, -forward.z);
        if (player.level() instanceof ServerLevel serverLevel) {
            Vec3 impact = attacker.position().add(0.0D, attacker.getBbHeight() * 0.56D, 0.0D).subtract(forward.scale(0.22D));
            LiverBreakEffectEntity.spawn(serverLevel, impact, forward, 1.16F, 15, LiverBreakEffectEntity.STYLE_IMPACT);
            serverLevel.sendParticles(ParticleTypes.CRIT, impact.x, impact.y, impact.z, 26, 0.34D, 0.24D, 0.34D, 0.055D);
            serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK, impact.x, impact.y, impact.z, 18, 0.22D, 0.2D, 0.22D, 0.018D);
            serverLevel.sendParticles(ParticleTypes.CLOUD, impact.x, impact.y - 0.18D, impact.z, 12, 0.24D, 0.1D, 0.24D, 0.025D);
        }
        this.startCooldown(data, this.getMaxCooldownTicks());
        HunterDataUtil.sync(player);
        return true;
    }
}
