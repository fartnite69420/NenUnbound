package com.huntercraft.huntercraft.abilities.speedtree;

import com.huntercraft.huntercraft.abilities.SkillTreeCombatAbility;
import com.huntercraft.huntercraft.animation.AnimationType;
import com.huntercraft.huntercraft.damage.HunterDamageSources;
import com.huntercraft.huntercraft.data.HunterPlayerData;
import com.huntercraft.huntercraft.entity.SlashEffectEntity;
import com.huntercraft.huntercraft.progression.SkillNode;
import com.huntercraft.huntercraft.util.HunterDataUtil;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class LionFangDrawAbility extends SkillTreeCombatAbility {
    private static final int CHARGE_TICKS = 10;
    private static final int DASH_TICKS = 20;
    private static final double DASH_SPEED = 1.35D;
    private static final float HIT_RANGE = 4.2F;
    private static final double TARGET_LOCK_RANGE = 14.0D;
    private static final float RUSH_STEP_HEIGHT = 3.1F;
    private static final float DEFAULT_STEP_HEIGHT = 1.6F;
    private static final float BASE_DAMAGE = 35.0F;
    private static final float LOCKED_TARGET_BONUS_DAMAGE = 4.0F;
    private static final int DARK_SLASH_COLOR = 0x17121C;

    public LionFangDrawAbility() {
        super("lion_fang_draw", "Lion Fang Draw", "Draw in power, then carve through your target in a dark flash and finish behind them.", "textures/gui/abilities/lion_fang_draw.png", SkillNode.SPEED, 40);
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
        if (data.isChargingAbility(this.id())) {
            return;
        }
        Vec3 chargeDirection = direction.lengthSqr() > 1.0E-4D
                ? new Vec3(direction.x, 0.0D, direction.z).normalize()
                : player.getLookAngle().multiply(1.0D, 0.0D, 1.0D).normalize();
        if (chargeDirection.lengthSqr() < 1.0E-4D) {
            return;
        }
        data.startChargingAbility(this.id(), CHARGE_TICKS, chargeDirection);
        LivingEntity target = findTarget(player, TARGET_LOCK_RANGE);
        if (target != null) {
            data.setChargeTargetUuid(target.getUUID().toString());
        }
        data.triggerAnimation(AnimationType.LION_FANG_DRAW_CHARGE);
    }

    @Override
    public void tick(ServerPlayer player, HunterPlayerData data) {
        if (data.isChargingAbility(this.id())) {
            data.tickChargingAbility();
            if (data.getChargeTicksRemaining() > 0) {
                return;
            }
            Vec3 forward = data.getChargeDirection();
            LivingEntity target = resolveTarget(player, data.getChargeTargetUuid());
            data.clearChargingAbility();
            startDash(player, data, forward, target);
            return;
        }
        if (data.isActiveAbility(this.id())) {
            tickDash(player, data);
        }
    }

    private void startDash(ServerPlayer player, HunterPlayerData data, Vec3 forward, LivingEntity target) {
        Vec3 normalizedForward = forward.lengthSqr() > 1.0E-4D
                ? new Vec3(forward.x, 0.0D, forward.z).normalize()
                : player.getLookAngle().multiply(1.0D, 0.0D, 1.0D).normalize();
        data.startActiveAbility(this.id(), DASH_TICKS, normalizedForward);
        if (target != null && target.isAlive()) {
            data.setActiveAbilityTargetUuid(target.getUUID().toString());
            player.lookAt(EntityAnchorArgument.Anchor.EYES, target.getEyePosition());
        }
        data.triggerAnimation(AnimationType.DASH);
        playDashReleaseSound(player, 0.84F);
    }

    private void tickDash(ServerPlayer player, HunterPlayerData data) {
        Vec3 forward = resolveDashDirection(player, data);
        Vec3 start = player.position();
        Vec3 horizontalSpeed = new Vec3(forward.x * DASH_SPEED, 0.0D, forward.z * DASH_SPEED);
        Vec3 currentMotion = player.getDeltaMovement();
        Vec3 dashMotion = new Vec3(horizontalSpeed.x, currentMotion.y, horizontalSpeed.z);
        player.setMaxUpStep(RUSH_STEP_HEIGHT);
        player.setDeltaMovement(dashMotion);
        player.hasImpulse = true;
        player.move(MoverType.SELF, horizontalSpeed);
        double movedDistance = player.position().subtract(start).horizontalDistance();
        if (movedDistance < horizontalSpeed.horizontalDistance() * 0.6D) {
            player.teleportTo(start.x, start.y, start.z);
            tryStepClimbRush(player, start, horizontalSpeed);
        }
        player.hurtMarked = true;
        spawnDashVisuals(player, start, player.position(), horizontalSpeed);

        LivingEntity target = resolveTarget(player, data.getActiveAbilityTargetUuid());
        AABB strikeBox = player.getBoundingBox().expandTowards(horizontalSpeed).inflate(HIT_RANGE);
        for (LivingEntity struckTarget : player.level().getEntitiesOfClass(LivingEntity.class, strikeBox, entity -> entity != player && entity.isAlive())) {
            float damage = this.getWeaponScaledDamage(player, BASE_DAMAGE + (target != null && struckTarget == target ? LOCKED_TARGET_BONUS_DAMAGE : 0.0F));
            struckTarget.hurt(HunterDamageSources.weapon(player.level(), player), damage);
            struckTarget.knockback(0.42F, -forward.x, -forward.z);
            finishDash(player, data, struckTarget.position().add(forward.scale(0.8D)), forward);
            return;
        }

        data.tickActiveAbility();
        if (data.getActiveAbilityTicksRemaining() <= 0) {
            finishDash(player, data, null, forward);
        }
    }

    private Vec3 resolveDashDirection(ServerPlayer player, HunterPlayerData data) {
        Vec3 liveLook = player.getLookAngle().multiply(1.0D, 0.0D, 1.0D);
        if (liveLook.lengthSqr() > 1.0E-4D) {
            return liveLook.normalize();
        }
        Vec3 stored = data.getActiveAbilityDirection();
        return stored.lengthSqr() > 1.0E-4D ? new Vec3(stored.x, 0.0D, stored.z).normalize() : Vec3.ZERO;
    }

    private void finishDash(ServerPlayer player, HunterPlayerData data, Vec3 burstCenter, Vec3 forward) {
        if (player.level() instanceof ServerLevel serverLevel) {
            Vec3 center = burstCenter != null ? burstCenter.add(0.0D, 1.0D, 0.0D) : player.position().add(0.0D, 1.0D, 0.0D);
            spawnDarkClawBurst(serverLevel, center, forward);
            serverLevel.sendParticles(ParticleTypes.SWEEP_ATTACK, center.x, center.y, center.z, 4, 0.3D, 0.2D, 0.3D, 0.0D);
            serverLevel.sendParticles(ParticleTypes.SQUID_INK, center.x, center.y, center.z, 34, 0.65D, 0.34D, 0.65D, 0.05D);
            serverLevel.sendParticles(ParticleTypes.SMOKE, center.x, center.y, center.z, 28, 0.55D, 0.22D, 0.55D, 0.035D);
            serverLevel.sendParticles(ParticleTypes.CRIT, center.x, center.y, center.z, 26, 0.45D, 0.28D, 0.45D, 0.05D);
        }
        data.clearActiveAbility();
        player.setMaxUpStep(DEFAULT_STEP_HEIGHT);
        player.setDeltaMovement(Vec3.ZERO);
        player.hasImpulse = true;
        this.startCooldown(data, this.getMaxCooldownTicks());
        HunterDataUtil.sync(player);
    }

    private void tryStepClimbRush(ServerPlayer player, Vec3 start, Vec3 speed) {
        for (double step = 0.5D; step <= RUSH_STEP_HEIGHT; step += 0.5D) {
            Vec3 raised = start.add(0.0D, step, 0.0D);
            if (!player.level().noCollision(player, player.getBoundingBox().move(raised.subtract(start)))) {
                continue;
            }
            Vec3 moved = raised.add(speed);
            if (!player.level().noCollision(player, player.getBoundingBox().move(moved.subtract(start)))) {
                continue;
            }
            player.teleportTo(raised.x, raised.y, raised.z);
            player.setDeltaMovement(speed);
            player.hasImpulse = true;
            player.move(MoverType.SELF, speed);
            settleRushToGround(player);
            return;
        }
    }

    private void settleRushToGround(ServerPlayer player) {
        Vec3 current = player.position();
        for (double drop = 0.25D; drop <= RUSH_STEP_HEIGHT + 1.0D; drop += 0.25D) {
            Vec3 candidate = current.add(0.0D, -drop, 0.0D);
            if (!player.level().noCollision(player, player.getBoundingBox().move(candidate.subtract(current)))) {
                continue;
            }
            BlockPos belowPos = BlockPos.containing(candidate.x, candidate.y - 0.05D, candidate.z).below();
            BlockState below = player.level().getBlockState(belowPos);
            if (!below.getCollisionShape(player.level(), belowPos).isEmpty()) {
                player.teleportTo(candidate.x, candidate.y, candidate.z);
                return;
            }
        }
    }

    private void spawnDashVisuals(ServerPlayer player, Vec3 start, Vec3 end, Vec3 speed) {
        if (player.level() instanceof ServerLevel serverLevel) {
            Vec3 path = end.subtract(start);
            Vec3 forward = speed.multiply(1.0D, 0.0D, 1.0D);
            if (forward.lengthSqr() > 1.0E-4D) {
                forward = forward.normalize();
                Vec3 side = new Vec3(-forward.z, 0.0D, forward.x);
                for (int i = 0; i < 2; i++) {
                    double t = serverLevel.random.nextDouble();
                    Vec3 point = start.add(path.scale(t))
                            .add(side.scale((serverLevel.random.nextDouble() - 0.5D) * 1.05D))
                            .add(0.0D, 0.75D + serverLevel.random.nextDouble() * 0.58D, 0.0D);
                    Vec3 direction = forward
                            .add(side.scale((serverLevel.random.nextDouble() - 0.5D) * 0.75D))
                            .add(0.0D, -0.22D + serverLevel.random.nextDouble() * 0.44D, 0.0D)
                            .normalize();
                    float roll = (float) (-78.0D + serverLevel.random.nextDouble() * 156.0D);
                    float scale = 0.44F + serverLevel.random.nextFloat() * 0.24F;
                    SlashEffectEntity.spawn(serverLevel, point, direction, roll, scale, 12, DARK_SLASH_COLOR);
                }
            }
            for (int i = 0; i < 4; i++) {
                Vec3 point = start.add(path.scale(i / 3.0D)).add(0.0D, 1.0D, 0.0D);
                serverLevel.sendParticles(ParticleTypes.SWEEP_ATTACK, point.x, point.y, point.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
                serverLevel.sendParticles(ParticleTypes.SQUID_INK, point.x, point.y, point.z, 3, 0.1D, 0.1D, 0.1D, 0.01D);
                serverLevel.sendParticles(ParticleTypes.CRIT, point.x, point.y, point.z, 4, 0.08D, 0.08D, 0.08D, 0.02D);
                serverLevel.sendParticles(ParticleTypes.SMOKE, point.x, point.y, point.z, 2, 0.08D, 0.06D, 0.08D, 0.01D);
            }
            Vec3 trail = start.subtract(speed.normalize().scale(0.4D)).add(0.0D, 1.0D, 0.0D);
            serverLevel.sendParticles(ParticleTypes.SQUID_INK, trail.x, trail.y, trail.z, 2, 0.08D, 0.08D, 0.08D, 0.0D);
        }
    }

    private void spawnDarkClawBurst(ServerLevel serverLevel, Vec3 center, Vec3 forward) {
        Vec3 horizontalForward = forward.multiply(1.0D, 0.0D, 1.0D);
        if (horizontalForward.lengthSqr() < 1.0E-4D) {
            horizontalForward = new Vec3(0.0D, 0.0D, 1.0D);
        } else {
            horizontalForward = horizontalForward.normalize();
        }
        Vec3 side = new Vec3(-horizontalForward.z, 0.0D, horizontalForward.x);
        for (int i = 0; i < 11; i++) {
            double angle = Math.toRadians((360.0D / 11.0D) * i + (serverLevel.random.nextDouble() - 0.5D) * 26.0D);
            Vec3 radial = horizontalForward.scale(Math.cos(angle)).add(side.scale(Math.sin(angle))).normalize();
            Vec3 effectPos = center
                    .add(radial.scale(0.72D + serverLevel.random.nextDouble() * 1.25D))
                    .add(0.0D, (serverLevel.random.nextDouble() - 0.5D) * 0.75D, 0.0D);
            Vec3 direction = radial
                    .add(0.0D, -0.24D + serverLevel.random.nextDouble() * 0.48D, 0.0D)
                    .normalize();
            float roll = (float) (-88.0D + serverLevel.random.nextDouble() * 176.0D);
            float scale = 0.62F + serverLevel.random.nextFloat() * 0.42F;
            SlashEffectEntity.spawn(serverLevel, effectPos, direction, roll, scale, 18 + serverLevel.random.nextInt(8), DARK_SLASH_COLOR);
        }
    }

    private LivingEntity findTarget(ServerPlayer player, double range) {
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle().normalize();
        AABB searchBox = player.getBoundingBox().expandTowards(look.scale(range)).inflate(1.5D);
        LivingEntity closest = null;
        double closestDistance = range * range;
        for (LivingEntity living : player.level().getEntitiesOfClass(LivingEntity.class, searchBox, entity -> entity != player && entity.isAlive())) {
            AABB box = living.getBoundingBox().inflate(0.4D);
            Vec3 hit = box.clip(eye, eye.add(look.scale(range))).orElse(null);
            if (hit == null) {
                continue;
            }
            double distance = eye.distanceToSqr(hit);
            if (distance < closestDistance) {
                closestDistance = distance;
                closest = living;
            }
        }
        return closest;
    }

    private LivingEntity resolveTarget(ServerPlayer player, String uuidString) {
        if (uuidString == null || uuidString.isBlank() || !(player.level() instanceof ServerLevel serverLevel)) {
            return null;
        }
        try {
            return serverLevel.getEntity(UUID.fromString(uuidString)) instanceof LivingEntity living ? living : null;
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }
}
