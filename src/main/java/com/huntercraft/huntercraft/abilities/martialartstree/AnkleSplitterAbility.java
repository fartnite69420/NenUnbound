package com.huntercraft.huntercraft.abilities.martialartstree;

import com.huntercraft.huntercraft.abilities.SkillTreeCombatAbility;
import com.huntercraft.huntercraft.animation.AnimationType;
import com.huntercraft.huntercraft.damage.HunterDamageSources;
import com.huntercraft.huntercraft.data.HunterPlayerData;
import com.huntercraft.huntercraft.entity.AnkleSweepEffectEntity;
import com.huntercraft.huntercraft.progression.SkillNode;
import com.huntercraft.huntercraft.util.HunterDataUtil;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class AnkleSplitterAbility extends SkillTreeCombatAbility {
    private static final int CHARGE_TICKS = 6;
    private static final float DROP_KICK_DAMAGE = 45.0F;
    private static final float ALT_JAB_DAMAGE = 20.5F;
    private static final int STUN_TICKS = 14;
    private static final double SLIDE_SPEED = 1.15D;

    public AnkleSplitterAbility() {
        super("ankle_splitter", "Ankle Splitter", "Slide in low with a sweeping kick that cuts through the target's stance. During an air grab, crack the target with a sharp face jab.", "textures/gui/abilities/ankle_splitter.png", SkillNode.MARTIAL_ARTS, 10);
    }

    @Override
    public int getMaxCooldownTicks() {
        return 160;
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
        LivingEntity grabbedTarget = MartialArtsGrabHelper.resolveGrabTarget(player, data);
        if (grabbedTarget != null && grabbedTarget.isAlive()) {
            grabbedTarget.hurt(HunterDamageSources.physical(player.level(), player), ALT_JAB_DAMAGE);
            HunterDataUtil.applyStun(grabbedTarget, player, 8);
            if (player.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.CRIT, grabbedTarget.getX(), grabbedTarget.getEyeY(), grabbedTarget.getZ(), 8, 0.15D, 0.15D, 0.15D, 0.03D);
            }
            data.triggerAnimation(AnimationType.MARTIAL_FACE_JAB);
            this.startCooldown(data, this.getMaxCooldownTicks());
            return;
        }

        Vec3 forward = MartialArtsGrabHelper.getHorizontalForward(player, direction);
        data.startChargingAbility(this.id(), CHARGE_TICKS, forward);
        data.triggerAnimation(AnimationType.MARTIAL_ANKLE_SPLITTER);
        HunterDataUtil.sync(player);
    }

    @Override
    public void tick(ServerPlayer player, HunterPlayerData data) {
        if (!data.isChargingAbility(this.id())) {
            return;
        }
        data.tickChargingAbility();
        if (data.getChargeTicksRemaining() > 0) {
            return;
        }
        Vec3 forward = data.getChargeDirection();
        data.clearChargingAbility();
        executeSweep(player, data, forward);
    }

    private void executeSweep(ServerPlayer player, HunterPlayerData data, Vec3 forward) {
        Vec3 slide = new Vec3(forward.x * SLIDE_SPEED, 0.0D, forward.z * SLIDE_SPEED);
        player.setDeltaMovement(slide);
        player.move(MoverType.SELF, slide);
        player.hasImpulse = true;
        player.hurtMarked = true;
        data.triggerAnimation(AnimationType.MARTIAL_ANKLE_SPLITTER);
        playPunchReleaseSound(player, 1.25F);
        sweepHit(player, forward);

        if (player.level() instanceof ServerLevel serverLevel) {
            Vec3 center = player.position().add(forward.scale(1.05D)).add(0.0D, 0.08D, 0.0D);
            AnkleSweepEffectEntity.spawn(serverLevel, center, forward, 1.12F, 13);
            serverLevel.sendParticles(ParticleTypes.SWEEP_ATTACK, center.x, center.y + 0.28D, center.z, 3, 0.22D, 0.05D, 0.22D, 0.0D);
            serverLevel.sendParticles(ParticleTypes.CRIT, center.x, center.y + 0.2D, center.z, 16, 0.34D, 0.12D, 0.34D, 0.055D);
            serverLevel.sendParticles(ParticleTypes.CLOUD, center.x, center.y - 0.02D, center.z, 18, 0.42D, 0.035D, 0.42D, 0.025D);
        }

        this.startCooldown(data, this.getMaxCooldownTicks());
        HunterDataUtil.sync(player);
    }

    private void sweepHit(ServerPlayer player, Vec3 forward) {
        AABB hitBox = player.getBoundingBox().expandTowards(forward.scale(1.85D)).inflate(0.9D, 0.35D, 0.9D);
        for (LivingEntity target : player.level().getEntitiesOfClass(LivingEntity.class, hitBox, entity -> entity != player && entity.isAlive())) {
            Vec3 toTarget = target.position().subtract(player.position()).multiply(1.0D, 0.0D, 1.0D);
            if (toTarget.lengthSqr() > 1.0E-4D && toTarget.normalize().dot(forward) < 0.05D) {
                continue;
            }
            target.invulnerableTime = 0;
            target.hurt(HunterDamageSources.physical(player.level(), player), DROP_KICK_DAMAGE);
            target.invulnerableTime = 0;
            HunterDataUtil.applyStun(target, player, STUN_TICKS);
            target.setDeltaMovement(forward.x * 0.85D, 0.18D, forward.z * 0.85D);
            target.hasImpulse = true;
            target.hurtMarked = true;
        }
    }
}
