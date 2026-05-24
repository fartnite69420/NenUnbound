package com.huntercraft.huntercraft.abilities.speedtree;

import com.huntercraft.huntercraft.abilities.SkillTreeCombatAbility;
import com.huntercraft.huntercraft.animation.AnimationType;
import com.huntercraft.huntercraft.damage.HunterDamageSources;
import com.huntercraft.huntercraft.data.HunterPlayerData;
import com.huntercraft.huntercraft.entity.SlashEffectEntity;
import com.huntercraft.huntercraft.progression.SkillNode;
import com.huntercraft.huntercraft.util.HunterDataUtil;
import com.huntercraft.huntercraft.util.TeleportSafetyHelper;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class GhostStepAbility extends SkillTreeCombatAbility {
    private static final int CHARGE_TICKS = 10;
    private static final float BACK_SLASH_DAMAGE = 18.0F;
    private static final int BACK_SLASH_VISUALS = 3;

    public GhostStepAbility() {
        super("ghost_step", "Ghost Step", "Blink behind the target you are aiming at from up to 15 blocks away.", "textures/gui/abilities/ghost_step.png", SkillNode.SPEED, 20);
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
        LivingEntity target = this.findTarget(player, 15.0D);
        if (target == null) {
            return;
        }
        Vec3 forward = target.position().subtract(player.position()).multiply(1.0D, 0.0D, 1.0D);
        if (forward.lengthSqr() < 1.0E-4D) {
            forward = player.getLookAngle().multiply(1.0D, 0.0D, 1.0D);
        }
        if (forward.lengthSqr() < 1.0E-4D) {
            return;
        }
        data.startChargingAbility(this.id(), CHARGE_TICKS, forward.normalize());
        data.setChargeTargetUuid(target.getUUID().toString());
        data.triggerAnimation(AnimationType.VOID_REND_CHARGE);
        player.setInvisible(true);
        player.noPhysics = true;
        HunterDataUtil.sync(player);
    }

    @Override
    public void tick(ServerPlayer player, HunterPlayerData data) {
        if (!data.isChargingAbility(this.id())) {
            return;
        }
        player.setInvisible(true);
        player.noPhysics = true;
        player.setDeltaMovement(Vec3.ZERO);
        player.hasImpulse = true;
        data.tickChargingAbility();
        if (data.getChargeTicksRemaining() > 0) {
            return;
        }
        LivingEntity target = resolveTarget(player, data.getChargeTargetUuid());
        data.clearChargingAbility();
        player.setInvisible(false);
        player.noPhysics = false;
        if (target == null || !target.isAlive()) {
            this.startCooldown(data, 10);
            HunterDataUtil.sync(player);
            return;
        }
        executeStep(player, data, target);
    }

    private void executeStep(ServerPlayer player, HunterPlayerData data, LivingEntity target) {
        Vec3 behind = target.position().subtract(target.getLookAngle().normalize().scale(1.4D));
        Vec3 from = player.position();
        Vec3 safeBehind = TeleportSafetyHelper.resolveAroundTarget(player, new Vec3(behind.x, target.getY(), behind.z));
        player.teleportTo(safeBehind.x, safeBehind.y, safeBehind.z);
        player.lookAt(EntityAnchorArgument.Anchor.EYES, target, EntityAnchorArgument.Anchor.EYES);
        data.triggerAnimation(AnimationType.VOID_REND);
        playTeleportReleaseSound(player, 1.24F);
        slashTargetBack(player, target);
        if (player.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.PORTAL, from.x, from.y + 1.0D, from.z, 18, 0.25D, 0.45D, 0.25D, 0.1D);
            serverLevel.sendParticles(ParticleTypes.PORTAL, safeBehind.x, safeBehind.y + 1.0D, safeBehind.z, 18, 0.25D, 0.45D, 0.25D, 0.1D);
        }
        this.startCooldown(data, this.getMaxCooldownTicks());
        HunterDataUtil.sync(player);
    }

    private LivingEntity resolveTarget(ServerPlayer player, String uuidString) {
        if (uuidString == null || uuidString.isBlank() || !(player.level() instanceof ServerLevel serverLevel)) {
            return null;
        }
        try {
            return serverLevel.getEntity(java.util.UUID.fromString(uuidString)) instanceof LivingEntity living ? living : null;
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private void slashTargetBack(ServerPlayer player, LivingEntity target) {
        if (!(player.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        Vec3 targetForward = target.getLookAngle().multiply(1.0D, 0.0D, 1.0D);
        if (targetForward.lengthSqr() < 1.0E-4D) {
            targetForward = target.position().subtract(player.position()).multiply(1.0D, 0.0D, 1.0D);
        }
        if (targetForward.lengthSqr() < 1.0E-4D) {
            targetForward = player.getLookAngle().multiply(1.0D, 0.0D, 1.0D).reverse();
        }
        targetForward = targetForward.normalize();

        Vec3 backDirection = targetForward.reverse();
        Vec3 side = new Vec3(-targetForward.z, 0.0D, targetForward.x).normalize();
        double bodyHeight = Math.max(1.0D, target.getBbHeight());
        Vec3 backCenter = target.position()
                .add(backDirection.scale((target.getBbWidth() * 0.5D) + 0.16D))
                .add(0.0D, bodyHeight * 0.58D, 0.0D);

        for (int i = 0; i < BACK_SLASH_VISUALS; i++) {
            double offset = (i - 1) * 0.16D;
            double heightOffset = (i - 1) * -0.08D;
            Vec3 effectPos = backCenter
                    .add(side.scale(offset))
                    .add(0.0D, heightOffset, 0.0D);
            float roll = switch (i) {
                case 0 -> -38.0F;
                case 1 -> -22.0F;
                default -> -52.0F;
            };
            float scale = 0.5F + (i * 0.06F);
            SlashEffectEntity.spawn(serverLevel, effectPos, backDirection, roll, scale, 8);
        }

        target.invulnerableTime = 0;
        target.hurt(HunterDamageSources.weapon(player.level(), player), this.getWeaponScaledDamage(player, BACK_SLASH_DAMAGE));
        target.invulnerableTime = 0;
        target.knockback(0.24F, targetForward.x, targetForward.z);

        serverLevel.sendParticles(ParticleTypes.SWEEP_ATTACK, backCenter.x, backCenter.y, backCenter.z, 2, 0.12D, 0.08D, 0.12D, 0.0D);
        serverLevel.sendParticles(ParticleTypes.CRIT, backCenter.x, backCenter.y, backCenter.z, 18, 0.25D, 0.2D, 0.25D, 0.04D);
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
}
