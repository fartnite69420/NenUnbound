package com.huntercraft.huntercraft.abilities.speedtree;

import com.huntercraft.huntercraft.abilities.SkillTreeCombatAbility;
import com.huntercraft.huntercraft.animation.AnimationType;
import com.huntercraft.huntercraft.damage.HunterDamageSources;
import com.huntercraft.huntercraft.data.HunterPlayerData;
import com.huntercraft.huntercraft.entity.SlashEffectEntity;
import com.huntercraft.huntercraft.progression.SkillNode;
import com.huntercraft.huntercraft.util.HunterDataUtil;
import com.huntercraft.huntercraft.util.TeleportSafetyHelper;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class VoidRendAbility extends SkillTreeCombatAbility {
    private static final int CHARGE_TICKS = 20;
    private static final double RANGE = 30.0D;
    private static final double LINE_WIDTH = 3.5D;
    private static final float BASE_DAMAGE = 45.0F;
    private static final int PATH_SLASH_VISUALS = 12;
    private static final int IMPACT_SLASH_VISUALS = 14;
    private static final int AFTERIMAGE_TRAIL_SLASH_VISUALS = 16;
    private static final int PATH_SLASH_LIFE_TICKS = 18;
    private static final int IMPACT_SLASH_LIFE_TICKS = 24;

    public VoidRendAbility() {
        super("void_rend", "Void Rend", "Charge, then flash through the path ahead in a single devastating cutting slash.", "textures/gui/abilities/void_rend.png", SkillNode.SPEED, 50);
    }

    @Override
    public int getMaxCooldownTicks() {
        return 200;
    }

    @Override
    public int getChargeTicks(HunterPlayerData data) {
        return data.isChargingAbility(this.id()) ? CHARGE_TICKS - data.getChargeTicksRemaining() : 0;
    }

    @Override
    public void use(ServerPlayer player, HunterPlayerData data, Vec3 direction) {
        if (!data.isChargingAbility(this.id())) {
            Vec3 chargeDirection = direction.lengthSqr() > 1.0E-4D
                    ? direction.normalize()
                    : player.getLookAngle().normalize();
            if (chargeDirection.lengthSqr() < 1.0E-4D) {
                return;
            }
            data.startChargingAbility(this.id(), CHARGE_TICKS, chargeDirection);
            data.triggerAnimation(AnimationType.VOID_REND_CHARGE);
            HunterDataUtil.sync(player);
            return;
        }

        data.clearChargingAbility();
        player.setDeltaMovement(Vec3.ZERO);
        this.startCooldown(data, this.getMaxCooldownTicks());
        HunterDataUtil.sync(player);
    }

    @Override
    public void tick(ServerPlayer player, HunterPlayerData data) {
        if (!data.isChargingAbility(this.id())) {
            return;
        }

        player.setDeltaMovement(Vec3.ZERO);
        player.hasImpulse = true;
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 5, 6, false, false, false));
        data.tickChargingAbility();
        if (data.getChargeTicksRemaining() > 0) {
            return;
        }

        Vec3 forward = player.getLookAngle().normalize();
        if (forward.lengthSqr() < 1.0E-4D) {
            forward = data.getChargeDirection();
        }
        data.clearChargingAbility();
        executeSlash(player, data, forward);
    }

    private void executeSlash(ServerPlayer player, HunterPlayerData data, Vec3 forward) {
        Vec3 normalizedForward = forward.lengthSqr() > 1.0E-4D
                ? forward.normalize()
                : player.getLookAngle().normalize();
        Vec3 start = player.position();
        data.triggerAnimation(AnimationType.VOID_REND);
        playTeleportReleaseSound(player, 0.72F);
        playSlashReleaseSound(player, 0.82F);
        Vec3 safeEnd = TeleportSafetyHelper.resolveDashTeleport(player, normalizedForward, RANGE);
        AABB strikeBox = player.getBoundingBox().move(start.subtract(player.position())).expandTowards(safeEnd.subtract(start)).inflate(LINE_WIDTH);
        List<LivingEntity> struckTargets = new ArrayList<>();
        for (LivingEntity target : player.level().getEntitiesOfClass(LivingEntity.class, strikeBox, entity -> entity != player && entity.isAlive())) {
            target.hurt(HunterDamageSources.weapon(player.level(), player), this.getWeaponScaledDamage(player, BASE_DAMAGE));
            struckTargets.add(target);
        }

        player.stopRiding();
        player.teleportTo(safeEnd.x, safeEnd.y, safeEnd.z);
        player.setDeltaMovement(Vec3.ZERO);
        player.hurtMarked = true;

        if (player.level() instanceof ServerLevel serverLevel) {
            spawnDelayedCutVisuals(serverLevel, player, start, safeEnd, normalizedForward, struckTargets);
        }

        this.startCooldown(data, this.getMaxCooldownTicks());
        HunterDataUtil.sync(player);
    }

    private void spawnDelayedCutVisuals(ServerLevel serverLevel, ServerPlayer player, Vec3 start, Vec3 end, Vec3 forward, List<LivingEntity> struckTargets) {
        Vec3 path = end.subtract(start);
        double pathLength = Math.max(1.0D, path.horizontalDistance());
        Vec3 horizontalForward = new Vec3(forward.x, 0.0D, forward.z);
        if (horizontalForward.lengthSqr() < 1.0E-4D) {
            horizontalForward = path.multiply(1.0D, 0.0D, 1.0D);
        }
        horizontalForward = horizontalForward.lengthSqr() > 1.0E-4D ? horizontalForward.normalize() : new Vec3(0.0D, 0.0D, 1.0D);
        Vec3 side = new Vec3(-horizontalForward.z, 0.0D, horizontalForward.x);

        spawnBehindDashSlashes(serverLevel, start, end, horizontalForward, side, pathLength);

        for (int i = 0; i < PATH_SLASH_VISUALS; i++) {
            double t = (i + 1.0D) / (PATH_SLASH_VISUALS + 1.0D);
            Vec3 point = start.add(path.scale(t)).add(0.0D, 0.95D + serverLevel.random.nextDouble() * 0.45D, 0.0D);
            double sideOffset = (serverLevel.random.nextDouble() - 0.5D) * Math.min(5.4D, pathLength * 0.18D);
            Vec3 effectPos = point.add(side.scale(sideOffset));
            float roll = (float) (-68.0D + serverLevel.random.nextDouble() * 136.0D);
            float scale = 0.86F + serverLevel.random.nextFloat() * 0.34F;
            Vec3 slashDirection = horizontalForward
                    .add(side.scale((serverLevel.random.nextDouble() - 0.5D) * 0.5D))
                    .add(0.0D, -0.15D + serverLevel.random.nextDouble() * 0.3D, 0.0D)
                    .normalize();
            SlashEffectEntity.spawn(serverLevel, effectPos, slashDirection, roll, scale, PATH_SLASH_LIFE_TICKS + serverLevel.random.nextInt(6));
            serverLevel.sendParticles(ParticleTypes.CRIT, effectPos.x, effectPos.y, effectPos.z, 3, 0.08D, 0.06D, 0.08D, 0.01D);
        }

        if (struckTargets.isEmpty()) {
            Vec3 finish = end.subtract(horizontalForward.scale(1.15D)).add(0.0D, 1.15D, 0.0D);
            spawnImpactSlashBloom(serverLevel, finish, horizontalForward, side);
            return;
        }

        for (LivingEntity target : struckTargets) {
            Vec3 center = target.position().add(0.0D, Math.max(1.0D, target.getBbHeight()) * 0.55D, 0.0D);
            spawnImpactSlashBloom(serverLevel, center, horizontalForward, side);
            serverLevel.sendParticles(ParticleTypes.SWEEP_ATTACK, center.x, center.y, center.z, 3, 0.35D, 0.18D, 0.35D, 0.0D);
            serverLevel.sendParticles(ParticleTypes.CRIT, center.x, center.y, center.z, 24, 0.36D, 0.26D, 0.36D, 0.04D);
        }
    }

    private void spawnImpactSlashBloom(ServerLevel serverLevel, Vec3 center, Vec3 forward, Vec3 side) {
        for (int i = 0; i < IMPACT_SLASH_VISUALS; i++) {
            double angle = Math.toRadians((360.0D / IMPACT_SLASH_VISUALS) * i + (serverLevel.random.nextDouble() - 0.5D) * 30.0D);
            Vec3 radial = forward.scale(Math.cos(angle)).add(side.scale(Math.sin(angle))).normalize();
            double radius = 1.15D + serverLevel.random.nextDouble() * 2.25D;
            Vec3 effectPos = center
                    .add(radial.scale(radius))
                    .add(0.0D, (serverLevel.random.nextDouble() - 0.5D) * 1.15D, 0.0D);
            Vec3 direction = radial.add(0.0D, -0.28D + serverLevel.random.nextDouble() * 0.56D, 0.0D).normalize();
            float roll = (float) (-88.0D + serverLevel.random.nextDouble() * 176.0D);
            float scale = 0.84F + serverLevel.random.nextFloat() * 0.54F;
            SlashEffectEntity.spawn(serverLevel, effectPos, direction, roll, scale, IMPACT_SLASH_LIFE_TICKS + serverLevel.random.nextInt(8));
        }
    }

    private void spawnBehindDashSlashes(ServerLevel serverLevel, Vec3 start, Vec3 end, Vec3 forward, Vec3 side, double pathLength) {
        Vec3 path = end.subtract(start);
        for (int i = 0; i < AFTERIMAGE_TRAIL_SLASH_VISUALS; i++) {
            double t = i / Math.max(1.0D, AFTERIMAGE_TRAIL_SLASH_VISUALS - 1.0D);
            double eased = 1.0D - ((1.0D - t) * (1.0D - t));
            Vec3 point = start.add(path.scale(eased))
                    .add(side.scale((serverLevel.random.nextDouble() - 0.5D) * Math.min(4.6D, pathLength * 0.16D)))
                    .add(0.0D, 0.68D + serverLevel.random.nextDouble() * 0.9D, 0.0D);
            Vec3 direction = forward
                    .add(side.scale((serverLevel.random.nextDouble() - 0.5D) * 0.95D))
                    .add(0.0D, -0.22D + serverLevel.random.nextDouble() * 0.44D, 0.0D)
                    .normalize();
            float roll = (float) (-86.0D + serverLevel.random.nextDouble() * 172.0D);
            float scale = 0.72F + serverLevel.random.nextFloat() * 0.5F;
            int life = 20 + serverLevel.random.nextInt(10);
            SlashEffectEntity.spawn(serverLevel, point, direction, roll, scale, life);
        }
    }
}
