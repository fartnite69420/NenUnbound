package com.huntercraft.huntercraft.abilities.speedtree;

import com.huntercraft.huntercraft.abilities.SkillTreeCombatAbility;
import com.huntercraft.huntercraft.animation.AnimationType;
import com.huntercraft.huntercraft.damage.HunterDamageSources;
import com.huntercraft.huntercraft.data.HunterPlayerData;
import com.huntercraft.huntercraft.entity.SlashEffectEntity;
import com.huntercraft.huntercraft.network.HunterNetwork;
import com.huntercraft.huntercraft.network.packet.AfterImagePacket;
import com.huntercraft.huntercraft.progression.SkillNode;
import com.huntercraft.huntercraft.util.HunterDataUtil;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class FlashCleaveAbility extends SkillTreeCombatAbility {
    private static final int CHARGE_TICKS = 6;
    private static final double TARGET_RANGE = 7.0D;
    private static final double SLASH_RADIUS = 2.25D;
    private static final float TOTAL_SLASH_DAMAGE = 30.0F;
    private static final int SLASH_COUNT = 7;
    private static final int VISUAL_SLASHES_PER_HIT = 7;
    private static final int HIT_INTERVAL_TICKS = 6;
    private static final int ACTIVE_TICKS = ((SLASH_COUNT - 1) * HIT_INTERVAL_TICKS) + 1;

    public FlashCleaveAbility() {
        super("flash_cleave", "Flash Cleave", "Break into a close-range burst of rapid slashes around the enemy in front of you.", "textures/gui/abilities/flash_cleave.png", SkillNode.SPEED, 10);
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
        if (data.isActiveAbility(this.id()) || data.isChargingAbility(this.id())) {
            return;
        }

        Vec3 forward = direction.lengthSqr() > 1.0E-4D
                ? new Vec3(direction.x, 0.0D, direction.z).normalize()
                : player.getLookAngle().multiply(1.0D, 0.0D, 1.0D).normalize();
        if (forward.lengthSqr() < 1.0E-4D) {
            return;
        }

        data.startChargingAbility(this.id(), CHARGE_TICKS, forward);
        data.triggerAnimation(AnimationType.FLASH_CLEAVE_ONE);
        HunterDataUtil.sync(player);
    }

    private void beginCleave(ServerPlayer player, HunterPlayerData data, Vec3 forward) {
        LivingEntity target = findTarget(player, forward);
        data.startActiveAbility(this.id(), ACTIVE_TICKS, forward);
        data.setActiveAbilityTargetUuid(target != null ? target.getUUID().toString() : "");
        triggerSlashAnimation(data, 0);
        playSlashReleaseSound(player, 1.18F);
        performSlash(player, target, forward, 0);
    }

    @Override
    public void tick(ServerPlayer player, HunterPlayerData data) {
        if (data.isChargingAbility(this.id())) {
            player.setDeltaMovement(Vec3.ZERO);
            player.hasImpulse = true;
            data.tickChargingAbility();
            if (data.getChargeTicksRemaining() > 0) {
                return;
            }
            Vec3 forward = data.getChargeDirection();
            if (forward.lengthSqr() < 1.0E-4D) {
                forward = player.getLookAngle().multiply(1.0D, 0.0D, 1.0D).normalize();
            }
            data.clearChargingAbility();
            beginCleave(player, data, forward);
            return;
        }

        if (!data.isActiveAbility(this.id())) {
            return;
        }

        int remaining = data.getActiveAbilityTicksRemaining();
        if (remaining <= 0) {
            data.clearActiveAbility();
            return;
        }

        LivingEntity target = resolveTarget(player, data.getActiveAbilityTargetUuid());
        Vec3 forward = data.getActiveAbilityDirection();
        if (forward.lengthSqr() < 1.0E-4D) {
            forward = player.getLookAngle().multiply(1.0D, 0.0D, 1.0D).normalize();
        }

        data.tickActiveAbility();
        int nextRemaining = data.getActiveAbilityTicksRemaining();
        if (nextRemaining <= 0) {
            data.clearActiveAbility();
            this.startCooldown(data, this.getMaxCooldownTicks());
            HunterDataUtil.sync(player);
            return;
        }

        int slashIndex = getSlashIndex(nextRemaining);
        if (slashIndex >= 0) {
            triggerSlashAnimation(data, slashIndex);
            performSlash(player, target, forward, slashIndex);
        }
    }

    private int getSlashIndex(int remainingTicks) {
        int elapsedTicks = ACTIVE_TICKS - remainingTicks;
        if (elapsedTicks <= 0 || elapsedTicks % HIT_INTERVAL_TICKS != 0) {
            return -1;
        }
        int slashIndex = elapsedTicks / HIT_INTERVAL_TICKS;
        return slashIndex >= 1 && slashIndex < SLASH_COUNT ? slashIndex : -1;
    }

    private void performSlash(ServerPlayer player, LivingEntity target, Vec3 forward, int slashIndex) {
        Vec3 side = new Vec3(-forward.z, 0.0D, forward.x);
        Vec3 slashPos;

        if (target != null && target.isAlive()) {
            Vec3 targetCenter = target.position();
            Vec3 towardTarget = new Vec3(targetCenter.x - player.getX(), 0.0D, targetCenter.z - player.getZ());
            Vec3 attackForward = towardTarget.lengthSqr() > 1.0E-4D ? towardTarget.normalize() : forward;
            side = new Vec3(-attackForward.z, 0.0D, attackForward.x);
            double sideOffset = switch (slashIndex % 4) {
                case 0 -> 0.52D;
                case 1 -> -0.46D;
                case 2 -> 0.16D;
                default -> -0.18D;
            };
            double forwardOffset = -0.2D + (slashIndex * 0.07D);
            slashPos = targetCenter.add(side.scale(sideOffset)).add(attackForward.scale(forwardOffset));
            spawnAfterImage(player);
            if (slashIndex == SLASH_COUNT - 1) {
                spawnAfterImage(player);
            }
            damageAround(player, target, slashPos, slashIndex == SLASH_COUNT - 1);
            spawnSlashBurst(player, slashPos, side, attackForward, slashIndex);
        } else {
            Vec3 center = player.position().add(forward.scale(1.75D));
            double sideOffset = (slashIndex % 2 == 0 ? 0.44D : -0.44D);
            slashPos = center.add(side.scale(sideOffset)).add(forward.scale(slashIndex * 0.09D));
            damageAround(player, null, slashPos, slashIndex == SLASH_COUNT - 1);
            spawnSlashBurst(player, slashPos, side, forward, slashIndex);
        }

        player.setDeltaMovement(Vec3.ZERO);
        player.hurtMarked = true;
    }

    private void damageAround(ServerPlayer player, LivingEntity focusTarget, Vec3 slashPos, boolean finisher) {
        AABB hitBox = new AABB(
                slashPos.x - SLASH_RADIUS,
                slashPos.y - 0.8D,
                slashPos.z - SLASH_RADIUS,
                slashPos.x + SLASH_RADIUS,
                slashPos.y + 1.8D,
                slashPos.z + SLASH_RADIUS
        );

        for (LivingEntity entity : player.level().getEntitiesOfClass(LivingEntity.class, hitBox, living -> living != player && living.isAlive())) {
            entity.invulnerableTime = 0;
            float damage = this.getWeaponScaledDamage(player, TOTAL_SLASH_DAMAGE) / SLASH_COUNT;
            if (entity == focusTarget) {
                damage += 0.25F;
            }
            if (finisher) {
                damage += 1.0F;
                Vec3 knockback = entity.position().subtract(player.position());
                entity.knockback(0.35F, -knockback.x, -knockback.z);
            }
            entity.hurt(HunterDamageSources.weapon(player.level(), player), damage);
            entity.invulnerableTime = 0;
        }
    }

    private void spawnSlashBurst(ServerPlayer player, Vec3 slashPos, Vec3 side, Vec3 forward, int slashIndex) {
        if (!(player.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        Vec3 visualForward = player.getLookAngle().multiply(1.0D, 0.0D, 1.0D);
        if (visualForward.lengthSqr() < 1.0E-4D) {
            visualForward = forward;
        }
        visualForward = visualForward.normalize();
        Vec3 visualSide = new Vec3(-visualForward.z, 0.0D, visualForward.x);

        double baseAngle = Math.toRadians((360.0D / SLASH_COUNT) * slashIndex);
        for (int i = 0; i < VISUAL_SLASHES_PER_HIT; i++) {
            double fanAngle = Math.toRadians((360.0D / VISUAL_SLASHES_PER_HIT) * i);
            double randomAngle = Math.toRadians((serverLevel.random.nextDouble() - 0.5D) * 42.0D);
            double angle = baseAngle + fanAngle + randomAngle;
            Vec3 radial = visualForward.scale(Math.cos(angle)).add(visualSide.scale(Math.sin(angle))).normalize();

            double radius = 1.02D + serverLevel.random.nextDouble() * 0.86D;
            double height = 0.78D + serverLevel.random.nextDouble() * 1.08D;
            double verticalTilt = -0.28D + serverLevel.random.nextDouble() * 0.56D;
            Vec3 slashPlaneDirection = radial.add(0.0D, verticalTilt, 0.0D).normalize();
            Vec3 effectPos = player.position()
                    .add(radial.scale(radius))
                    .add(0.0D, height, 0.0D);

            float roll = (float) (-82.0D + serverLevel.random.nextDouble() * 164.0D);
            float scale = 0.48F + (serverLevel.random.nextFloat() * 0.28F);
            int life = HIT_INTERVAL_TICKS + 2 + serverLevel.random.nextInt(4);
            SlashEffectEntity.spawn(serverLevel, effectPos, slashPlaneDirection, roll, scale, life);
        }

    }

    private void spawnAfterImage(ServerPlayer player) {
        HunterNetwork.sendToTrackingAndSelf(player, new AfterImagePacket(
                player.getUUID(),
                player.getX(),
                player.getY(),
                player.getZ(),
                player.getYRot(),
                player.getXRot(),
                8
        ));
    }

    private void triggerSlashAnimation(HunterPlayerData data, int slashIndex) {
        switch (slashIndex % 3) {
            case 0 -> data.triggerAnimation(AnimationType.FLASH_CLEAVE_ONE);
            case 1 -> data.triggerAnimation(AnimationType.FLASH_CLEAVE_TWO);
            default -> data.triggerAnimation(AnimationType.FLASH_CLEAVE_THREE);
        }
    }

    private LivingEntity findTarget(ServerPlayer player, Vec3 forward) {
        Vec3 eye = player.getEyePosition();
        Vec3 end = eye.add(forward.scale(TARGET_RANGE));
        AABB searchBox = player.getBoundingBox().expandTowards(forward.scale(TARGET_RANGE)).inflate(1.5D);
        LivingEntity closest = null;
        double closestDistance = TARGET_RANGE * TARGET_RANGE;

        for (LivingEntity target : player.level().getEntitiesOfClass(LivingEntity.class, searchBox, entity -> entity != player && entity.isAlive())) {
            Vec3 hit = target.getBoundingBox().inflate(0.35D).clip(eye, end).orElse(null);
            if (hit == null) {
                continue;
            }
            double distance = eye.distanceToSqr(hit);
            if (distance < closestDistance) {
                closestDistance = distance;
                closest = target;
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
