package com.huntercraft.huntercraft.abilities.martialartstree;

import com.huntercraft.huntercraft.abilities.SkillTreeCombatAbility;
import com.huntercraft.huntercraft.animation.AnimationType;
import com.huntercraft.huntercraft.damage.HunterDamageSources;
import com.huntercraft.huntercraft.data.HunterPlayerData;
import com.huntercraft.huntercraft.entity.MartialWhirlwindEffectEntity;
import com.huntercraft.huntercraft.network.HunterNetwork;
import com.huntercraft.huntercraft.network.packet.AfterImagePacket;
import com.huntercraft.huntercraft.progression.SkillNode;
import com.huntercraft.huntercraft.util.HunterDataUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class WhirlwindArcAbility extends SkillTreeCombatAbility {
    private static final int CHARGE_TICKS = 6;
    private static final int DASH_TICKS = 22;
    private static final int DASH_HIT_INTERVAL = 4;
    private static final int AIR_BARRAGE_TICKS = 18;
    private static final int AIR_BARRAGE_HIT_INTERVAL = 2;
    private static final float DASH_DAMAGE = 40.8F;
    private static final float AIR_BARRAGE_DAMAGE = 8.2F;
    private static final float AIR_BARRAGE_FINISHER_DAMAGE = 12.5F;
    private static final double DASH_SPEED = 0.675D;
    private static final double DASH_RADIUS = 5.5D;
    private static final double DASH_PULL_STRENGTH = 0.42D;
    private static final float DASH_STEP_HEIGHT = 3.2F;
    private static final float DEFAULT_STEP_HEIGHT = 1.6F;
    private static final double GRAVITY_PER_TICK = 0.08D;
    private static final double ALT_FINISHER_PUSH = 0.65D;

    public WhirlwindArcAbility() {
        super("whirlwind_arc", "Whirlwind Arc", "Dash forward in a spinning storm of wind slashes that shreds and drags targets in. During an air grab, unleash a rapid punch barrage into the suspended target before ending with a heavier strike.", "textures/gui/abilities/whirlwind_arc.png", SkillNode.MARTIAL_ARTS, 40);
    }

    @Override
    public int getMaxCooldownTicks() {
        return 140;
    }

    @Override
    public boolean isContinuous() {
        return true;
    }

    @Override
    public int getChargeTicks(HunterPlayerData data) {
        return data.isChargingAbility(this.id()) ? CHARGE_TICKS - data.getChargeTicksRemaining() : 0;
    }

    @Override
    public int getActiveTicks(HunterPlayerData data) {
        if (!data.isActiveAbility(this.id())) {
            return 0;
        }
        int maxTicks = data.getActiveAbilityTargetUuid().isBlank() ? DASH_TICKS : AIR_BARRAGE_TICKS;
        return maxTicks - data.getActiveAbilityTicksRemaining();
    }

    @Override
    public void use(ServerPlayer player, HunterPlayerData data, Vec3 direction) {
        if (data.isChargingAbility(this.id()) || data.isActiveAbility(this.id())) {
            return;
        }

        LivingEntity grabbedTarget = MartialArtsGrabHelper.resolveGrabTarget(player, data);
        if (grabbedTarget != null && grabbedTarget.isAlive()) {
            data.startActiveAbility(this.id(), AIR_BARRAGE_TICKS, Vec3.ZERO);
            data.setActiveAbilityTargetUuid(grabbedTarget.getUUID().toString());
            data.triggerAnimation(AnimationType.MARTIAL_AIR_BARRAGE);
            HunterDataUtil.sync(player);
            return;
        }

        Vec3 forward = MartialArtsGrabHelper.getHorizontalForward(player, direction);
        data.startChargingAbility(this.id(), CHARGE_TICKS, forward);
        data.triggerAnimation(AnimationType.MARTIAL_WHIRLWIND_ARC);
        HunterDataUtil.sync(player);
    }

    private void beginDash(ServerPlayer player, HunterPlayerData data, Vec3 forward) {
        data.startActiveAbility(this.id(), DASH_TICKS, forward);
        data.triggerAnimation(AnimationType.MARTIAL_WHIRLWIND_ARC);
        playDashReleaseSound(player, 1.1F);
        if (player.level() instanceof ServerLevel serverLevel) {
            MartialWhirlwindEffectEntity.spawn(serverLevel, player, forward, 1.0F, DASH_TICKS + 4);
        }
        HunterNetwork.sendToTrackingAndSelf(player, new AfterImagePacket(
                player.getUUID(),
                player.getX(),
                player.getY(),
                player.getZ(),
                player.getYRot(),
                player.getXRot(),
                10
        ));
        pulseDash(player, 0);
        HunterDataUtil.sync(player);
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
                beginDash(player, data, forward);
            }
            return;
        }
        if (!data.isActiveAbility(this.id())) {
            return;
        }
        if (data.getActiveAbilityTargetUuid().isBlank()) {
            tickDash(player, data);
        } else {
            tickAirBarrage(player, data);
        }
    }

    private void tickDash(ServerPlayer player, HunterPlayerData data) {
        Vec3 forward = resolveDashDirection(player, data);
        Vec3 start = player.position();
        Vec3 horizontalStep = forward.scale(DASH_SPEED);
        Vec3 currentMotion = player.getDeltaMovement();
        double verticalSpeed = player.onGround() ? Math.min(0.0D, currentMotion.y) : currentMotion.y - GRAVITY_PER_TICK;
        Vec3 dashMotion = new Vec3(horizontalStep.x, verticalSpeed, horizontalStep.z);
        player.setMaxUpStep(DASH_STEP_HEIGHT);
        player.setDeltaMovement(dashMotion);
        moveDashWithStepAssist(player, start, horizontalStep, verticalSpeed);
        player.hasImpulse = true;
        player.hurtMarked = true;

        int remaining = data.getActiveAbilityTicksRemaining();
        int stage = DASH_TICKS - remaining;
        if (remaining % DASH_HIT_INTERVAL == 0) {
            pulseDash(player, stage);
        }

        data.tickActiveAbility();
        if (data.getActiveAbilityTicksRemaining() <= 0) {
            Vec3 finalMotion = player.getDeltaMovement();
            player.setMaxUpStep(DEFAULT_STEP_HEIGHT);
            player.setDeltaMovement(forward.x * 0.12D, finalMotion.y, forward.z * 0.12D);
            data.clearActiveAbility();
            this.startCooldown(data, this.getMaxCooldownTicks());
            HunterDataUtil.sync(player);
        }
    }

    private Vec3 resolveDashDirection(ServerPlayer player, HunterPlayerData data) {
        Vec3 liveLook = player.getLookAngle().multiply(1.0D, 0.0D, 1.0D);
        if (liveLook.lengthSqr() > 1.0E-4D) {
            return liveLook.normalize();
        }
        Vec3 stored = data.getActiveAbilityDirection().multiply(1.0D, 0.0D, 1.0D);
        return stored.lengthSqr() > 1.0E-4D ? stored.normalize() : new Vec3(0.0D, 0.0D, 1.0D);
    }

    private void moveDashWithStepAssist(ServerPlayer player, Vec3 start, Vec3 horizontalStep, double verticalSpeed) {
        player.move(MoverType.SELF, new Vec3(horizontalStep.x, verticalSpeed, horizontalStep.z));
        double movedDistance = player.position().subtract(start).horizontalDistance();
        if (movedDistance >= horizontalStep.horizontalDistance() * 0.9D) {
            return;
        }

        player.teleportTo(start.x, start.y, start.z);
        if (tryStepClimbDash(player, start, horizontalStep, verticalSpeed)) {
            return;
        }

        player.move(MoverType.SELF, new Vec3(horizontalStep.x * 0.35D, verticalSpeed, horizontalStep.z * 0.35D));
    }

    private boolean tryStepClimbDash(ServerPlayer player, Vec3 start, Vec3 horizontalStep, double verticalSpeed) {
        AABB startBox = player.getBoundingBox();
        for (double step = 0.25D; step <= DASH_STEP_HEIGHT; step += 0.25D) {
            Vec3 raised = start.add(0.0D, step, 0.0D);
            Vec3 target = raised.add(horizontalStep);
            if (!player.level().noCollision(player, startBox.move(raised.subtract(start)))) {
                continue;
            }
            if (!player.level().noCollision(player, startBox.move(target.subtract(start)))) {
                continue;
            }
            player.teleportTo(target.x, target.y, target.z);
            player.setDeltaMovement(horizontalStep.x, verticalSpeed, horizontalStep.z);
            player.hasImpulse = true;
            settleDashToGround(player);
            return true;
        }
        return false;
    }

    private void settleDashToGround(ServerPlayer player) {
        Vec3 current = player.position();
        for (double drop = 0.25D; drop <= DASH_STEP_HEIGHT + 1.0D; drop += 0.25D) {
            Vec3 candidate = current.add(0.0D, -drop, 0.0D);
            if (!player.level().noCollision(player, player.getBoundingBox().move(candidate.subtract(current)))) {
                continue;
            }
            BlockPos belowPos = BlockPos.containing(candidate.x, candidate.y - 0.05D, candidate.z);
            BlockState below = player.level().getBlockState(belowPos);
            if (!below.getCollisionShape(player.level(), belowPos).isEmpty()) {
                player.teleportTo(candidate.x, candidate.y, candidate.z);
                return;
            }
        }
    }

    private void tickAirBarrage(ServerPlayer player, HunterPlayerData data) {
        LivingEntity target = MartialArtsGrabHelper.resolveGrabTarget(player, data);
        if (target == null || !target.isAlive()) {
            data.clearActiveAbility();
            this.startCooldown(data, this.getMaxCooldownTicks());
            HunterDataUtil.sync(player);
            return;
        }

        int remaining = data.getActiveAbilityTicksRemaining();
        if (remaining % AIR_BARRAGE_HIT_INTERVAL == 0) {
            target.invulnerableTime = 0;
            target.hurt(HunterDamageSources.physical(player.level(), player), AIR_BARRAGE_DAMAGE);
            target.invulnerableTime = 0;
            target.hurtMarked = true;
            spawnBarrageHitVisuals(player, target, false);
        }

        data.tickActiveAbility();
        if (data.getActiveAbilityTicksRemaining() <= 0) {
            Vec3 forward = MartialArtsGrabHelper.getHorizontalForward(player, target.position().subtract(player.position()));
            target.invulnerableTime = 0;
            target.hurt(HunterDamageSources.physical(player.level(), player), AIR_BARRAGE_FINISHER_DAMAGE);
            target.invulnerableTime = 0;
            target.setDeltaMovement(forward.x * ALT_FINISHER_PUSH, 0.22D, forward.z * ALT_FINISHER_PUSH);
            target.hurtMarked = true;
            HunterDataUtil.applyStun(target, player, 12);
            spawnBarrageHitVisuals(player, target, true);
            data.clearActiveAbility();
            this.startCooldown(data, this.getMaxCooldownTicks());
            HunterDataUtil.sync(player);
        }
    }

    private void pulseDash(ServerPlayer player, int stage) {
        Vec3 center = player.position().add(0.0D, 1.0D, 0.0D);
        AABB hitBox = new AABB(
                center.x - DASH_RADIUS, center.y - 1.0D, center.z - DASH_RADIUS,
                center.x + DASH_RADIUS, center.y + 2.0D, center.z + DASH_RADIUS
        );
        for (LivingEntity target : player.level().getEntitiesOfClass(LivingEntity.class, hitBox, entity -> entity != player && entity.isAlive())) {
            Vec3 toPlayer = center.subtract(target.position());
            Vec3 pull = toPlayer.lengthSqr() > 1.0E-4D ? toPlayer.normalize().scale(DASH_PULL_STRENGTH) : Vec3.ZERO;
            target.invulnerableTime = 0;
            target.hurt(HunterDamageSources.physical(player.level(), player), DASH_DAMAGE);
            target.invulnerableTime = 0;
            target.setDeltaMovement(pull.x, Math.max(0.08D, pull.y * 0.15D), pull.z);
            target.hurtMarked = true;
        }

        if (!(player.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        serverLevel.sendParticles(ParticleTypes.CLOUD, center.x, center.y + 0.3D, center.z, 6, 0.42D, 0.18D, 0.42D, 0.025D);
        serverLevel.sendParticles(ParticleTypes.CRIT, center.x, center.y + 0.9D, center.z, 5, 0.35D, 0.22D, 0.35D, 0.05D);
    }

    private void spawnBarrageHitVisuals(ServerPlayer player, LivingEntity target, boolean finisher) {
        if (!(player.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        Vec3 center = target.position().add(0.0D, 1.0D, 0.0D);
        serverLevel.sendParticles(ParticleTypes.CRIT, center.x, center.y, center.z, finisher ? 14 : 6, 0.18D, 0.18D, 0.18D, finisher ? 0.06D : 0.03D);
        serverLevel.sendParticles(ParticleTypes.CLOUD, center.x, center.y, center.z, finisher ? 8 : 3, 0.12D, 0.12D, 0.12D, 0.01D);
        if (finisher) {
            serverLevel.sendParticles(ParticleTypes.SWEEP_ATTACK, center.x, center.y, center.z, 3, 0.28D, 0.12D, 0.28D, 0.0D);
        }
    }

    @Override
    public void stop(ServerPlayer player, HunterPlayerData data) {
        if (data.isActiveAbility(this.id())) {
            player.setMaxUpStep(DEFAULT_STEP_HEIGHT);
        }
    }
}
