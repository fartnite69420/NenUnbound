package com.huntercraft.huntercraft.abilities.martialartstree;

import com.huntercraft.huntercraft.abilities.GrabAbility;
import com.huntercraft.huntercraft.abilities.SkillTreeCombatAbility;
import com.huntercraft.huntercraft.animation.AnimationType;
import com.huntercraft.huntercraft.damage.HunterDamageSources;
import com.huntercraft.huntercraft.data.HunterPlayerData;
import com.huntercraft.huntercraft.entity.ToraHuntEffectEntity;
import com.huntercraft.huntercraft.progression.SkillNode;
import com.huntercraft.huntercraft.util.HunterDataUtil;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.phys.Vec3;

public class ToraHuntAbility extends SkillTreeCombatAbility implements GrabAbility {
    private static final int CHARGE_TICKS = 10;
    private static final int HOLD_TICKS = 32;
    private static final float BASE_DAMAGE = 35.0F;
    private static final float ALT_DAMAGE = 28.0F;
    private static final double DASH_SPEED = 1.65D;
    private static final double RANGE = 8.0D;
    private static final int ALT_STUN_TICKS = 16;

    public ToraHuntAbility() {
        super("tora_hunt", "Tora Hunt", "Coil into a low hunting stance, then tear forward with a tiger-claw rush that transitions into an air grab. During an air grab, cash out with a harder finishing hit.", "textures/gui/abilities/tora_hunt.png", SkillNode.MARTIAL_ARTS, 50);
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
    public boolean isContinuous() {
        return true;
    }

    @Override
    public int getActiveTicks(HunterPlayerData data) {
        return MartialArtsGrabHelper.isGrabSourceActive(data, this.id()) ? HOLD_TICKS - data.getMartialArtsGrabTicksRemaining() : 0;
    }

    @Override
    public void use(ServerPlayer player, HunterPlayerData data, Vec3 direction) {
        LivingEntity grabbedTarget = MartialArtsGrabHelper.resolveGrabTarget(player, data);
        if (grabbedTarget != null && grabbedTarget.isAlive()) {
            finishGrab(player, data, grabbedTarget);
            return;
        }
        if (data.isChargingAbility(this.id()) || MartialArtsGrabHelper.isGrabSourceActive(data, this.id())) {
            stop(player, data);
            return;
        }
        Vec3 forward = MartialArtsGrabHelper.getHorizontalForward(player, direction);
        data.startChargingAbility(this.id(), CHARGE_TICKS, forward);
        data.triggerAnimation(AnimationType.MARTIAL_TORA_HUNT_CHARGE);
        spawnToraEffect(player, player.position().add(0.0D, 0.55D, 0.0D), forward, 0.9F, CHARGE_TICKS, ToraHuntEffectEntity.STYLE_CHARGE);
        HunterDataUtil.sync(player);
    }

    @Override
    public void tick(ServerPlayer player, HunterPlayerData data) {
        if (!data.isChargingAbility(this.id())) {
            return;
        }
        player.setDeltaMovement(Vec3.ZERO);
        player.hasImpulse = true;
        player.fallDistance = 0.0F;
        spawnChargeVisuals(player, data.getChargeTicksRemaining());
        data.tickChargingAbility();
        if (data.getChargeTicksRemaining() > 0) {
            return;
        }

        data.clearChargingAbility();
        LivingEntity target = MartialArtsGrabHelper.findReleaseGrabTarget(player, data, RANGE, 1.2D);
        if (target == null) {
            this.startCooldown(data, this.getMaxCooldownTicks());
            HunterDataUtil.sync(player);
            return;
        }

        Vec3 toward = target.position().subtract(player.position());
        Vec3 forward = MartialArtsGrabHelper.getHorizontalForward(player, toward);
        Vec3 dash = forward.scale(DASH_SPEED);
        player.setDeltaMovement(dash);
        player.move(MoverType.SELF, dash);
        player.hasImpulse = true;
        player.hurtMarked = true;
        player.fallDistance = 0.0F;
        playDashReleaseSound(player, 0.92F);
        target.hurt(HunterDamageSources.physical(player.level(), player), BASE_DAMAGE);
        target.invulnerableTime = 0;
        target.hurtMarked = true;
        target.setDeltaMovement(forward.x * 0.3D, 0.25D, forward.z * 0.3D);
        MartialArtsGrabHelper.startOrRefreshGrab(player, data, target, HOLD_TICKS, this.id());
        data.triggerAnimation(AnimationType.MARTIAL_TORA_HUNT);
        spawnStrikeVisuals(player, target, false);
        HunterDataUtil.sync(player);
    }

    private void finishGrab(ServerPlayer player, HunterPlayerData data, LivingEntity target) {
        target.invulnerableTime = 0;
        target.hurt(HunterDamageSources.physical(player.level(), player), ALT_DAMAGE);
        target.invulnerableTime = 0;
        Vec3 forward = MartialArtsGrabHelper.getHorizontalForward(player, target.position().subtract(player.position()));
        target.setDeltaMovement(forward.x * 0.85D, 0.35D, forward.z * 0.85D);
        target.hurtMarked = true;
        HunterDataUtil.applyStun(target, player, ALT_STUN_TICKS);
        MartialArtsGrabHelper.clearGrab(data);
        data.triggerAnimation(AnimationType.MARTIAL_TORA_HUNT);
        spawnStrikeVisuals(player, target, true);
        HunterDataUtil.sync(player);
    }

    private void spawnChargeVisuals(ServerPlayer player, int remainingTicks) {
        if (!(player.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        Vec3 center = player.position().add(0.0D, 1.0D, 0.0D);
        Vec3 forward = dataForward(player);
        if (remainingTicks % 5 == 0) {
            spawnToraEffect(player, center.add(0.0D, -0.45D, 0.0D), forward, 0.8F, 9, ToraHuntEffectEntity.STYLE_CHARGE);
        }
        double radius = 0.9D + ((CHARGE_TICKS - remainingTicks) * 0.05D);
        for (int i = 0; i < 8; i++) {
            double angle = ((Math.PI * 2.0D) / 8.0D) * i + (remainingTicks * 0.22D);
            double x = center.x + Math.cos(angle) * radius;
            double z = center.z + Math.sin(angle) * radius;
            serverLevel.sendParticles(ParticleTypes.CLOUD, x, center.y, z, 1, 0.02D, 0.02D, 0.02D, 0.0D);
            serverLevel.sendParticles(ParticleTypes.CRIT, x, center.y + 0.4D, z, 1, 0.02D, 0.02D, 0.02D, 0.0D);
        }
    }

    private void spawnStrikeVisuals(ServerPlayer player, LivingEntity target, boolean finisher) {
        if (!(player.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        Vec3 center = target.position().add(0.0D, 0.9D, 0.0D);
        Vec3 forward = MartialArtsGrabHelper.getHorizontalForward(player, target.position().subtract(player.position()));
        ToraHuntEffectEntity.spawn(serverLevel, center.add(forward.scale(-0.55D)), forward, finisher ? 1.35F : 1.1F, finisher ? 18 : 14, finisher ? ToraHuntEffectEntity.STYLE_FINISHER : ToraHuntEffectEntity.STYLE_STRIKE);
        serverLevel.sendParticles(ParticleTypes.CRIT, center.x, center.y, center.z, finisher ? 10 : 6, 0.18D, 0.18D, 0.18D, finisher ? 0.05D : 0.03D);
        serverLevel.sendParticles(ParticleTypes.CLOUD, center.x, center.y - 0.2D, center.z, finisher ? 7 : 4, 0.18D, 0.12D, 0.18D, 0.01D);
    }

    private void spawnToraEffect(ServerPlayer player, Vec3 position, Vec3 direction, float scale, int lifeTicks, int style) {
        if (player.level() instanceof ServerLevel serverLevel) {
            ToraHuntEffectEntity.spawn(serverLevel, position, direction, scale, lifeTicks, style);
        }
    }

    private Vec3 dataForward(ServerPlayer player) {
        return MartialArtsGrabHelper.getHorizontalForward(player, player.getLookAngle());
    }

    @Override
    public void stop(ServerPlayer player, HunterPlayerData data) {
        boolean canceled = false;
        if (data.isChargingAbility(this.id())) {
            data.clearChargingAbility();
            canceled = true;
        }
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
