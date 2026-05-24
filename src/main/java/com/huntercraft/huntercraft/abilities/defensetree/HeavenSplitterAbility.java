package com.huntercraft.huntercraft.abilities.defensetree;

import com.huntercraft.huntercraft.abilities.SkillTreeCombatAbility;
import com.huntercraft.huntercraft.animation.AnimationType;
import com.huntercraft.huntercraft.damage.HunterDamageSources;
import com.huntercraft.huntercraft.data.HunterPlayerData;
import com.huntercraft.huntercraft.entity.HeavenSplitterSlashEntity;
import com.huntercraft.huntercraft.progression.SkillNode;
import com.huntercraft.huntercraft.util.HunterDataUtil;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class HeavenSplitterAbility extends SkillTreeCombatAbility {
    private static final int CHARGE_TICKS = 6;
    private static final float BASE_DAMAGE = 45.0F;
    private static final double REACH = 5.6D;
    private static final double WIDTH = 4.6D;
    private static final int HEAVEN_SLASH_COLOR = 0xFF2424;

    public HeavenSplitterAbility() {
        super("heaven_splitter", "Heaven Splitter", "Bring your blade down in a crushing overhead swing that cleaves the space in front of you.", "textures/gui/abilities/heaven_splitter.png", SkillNode.DEFENSE, 10);
    }

    @Override
    public int getMaxCooldownTicks() {
        return 90;
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
        Vec3 forward = direction.lengthSqr() > 1.0E-4D
                ? new Vec3(direction.x, 0.0D, direction.z).normalize()
                : player.getLookAngle().multiply(1.0D, 0.0D, 1.0D).normalize();
        if (forward.lengthSqr() < 1.0E-4D) {
            return;
        }

        data.startChargingAbility(this.id(), CHARGE_TICKS, forward);
        data.triggerAnimation(AnimationType.HEAVEN_SPLITTER);
        HunterDataUtil.sync(player);
    }

    @Override
    public void tick(ServerPlayer player, HunterPlayerData data) {
        if (!data.isChargingAbility(this.id())) {
            return;
        }
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
        executeSplit(player, data, forward);
    }

    private void executeSplit(ServerPlayer player, HunterPlayerData data, Vec3 forward) {
        data.triggerAnimation(AnimationType.HEAVEN_SPLITTER);
        playSlashReleaseSound(player, 0.82F);
        Vec3 impactCenter = player.position().add(forward.scale(REACH)).add(0.0D, 1.0D, 0.0D);
        AABB hitBox = new AABB(
                impactCenter.x - WIDTH,
                impactCenter.y - 1.1D,
                impactCenter.z - WIDTH,
                impactCenter.x + WIDTH,
                impactCenter.y + 1.3D,
                impactCenter.z + WIDTH
        );

        for (LivingEntity target : player.level().getEntitiesOfClass(LivingEntity.class, hitBox, entity -> entity != player && entity.isAlive())) {
            target.hurt(HunterDamageSources.weapon(player.level(), player), this.getWeaponScaledDamage(player, BASE_DAMAGE));
            target.knockback(0.6F, -forward.x, -forward.z);
        }

        if (player.level() instanceof ServerLevel serverLevel) {
            spawnHeavenSplitVisuals(serverLevel, player.position(), forward, impactCenter);
        }

        this.startCooldown(data, this.getMaxCooldownTicks());
        HunterDataUtil.sync(player);
    }

    private void spawnHeavenSplitVisuals(ServerLevel serverLevel, Vec3 playerPos, Vec3 forward, Vec3 impactCenter) {
        Vec3 side = new Vec3(-forward.z, 0.0D, forward.x);
        HeavenSplitterSlashEntity.spawn(serverLevel, playerPos.add(forward.scale(2.55D)).add(0.0D, 0.52D, 0.0D), forward, 1.18F, -8.0F, 18, HEAVEN_SLASH_COLOR);
        HeavenSplitterSlashEntity.spawn(serverLevel, playerPos.add(forward.scale(3.25D)).add(side.scale(0.18D)).add(0.0D, 0.36D, 0.0D), forward, 0.84F, -4.0F, 16, 0xFFFFFF);
        HeavenSplitterSlashEntity.spawn(serverLevel, impactCenter.add(side.scale(-0.1D)).add(0.0D, -0.38D, 0.0D), forward, 1.32F, -10.0F, 20, HEAVEN_SLASH_COLOR);

        for (int i = 0; i < 9; i++) {
            double progress = (i + 1.0D) / 10.0D;
            Vec3 groundPoint = playerPos
                    .add(forward.scale(progress * REACH))
                    .add(side.scale((serverLevel.random.nextDouble() - 0.5D) * (0.8D + progress * 1.8D)))
                    .add(0.0D, 0.12D, 0.0D);
            serverLevel.sendParticles(ParticleTypes.CLOUD, groundPoint.x, groundPoint.y, groundPoint.z, 8, 0.25D + progress * 0.35D, 0.04D, 0.25D + progress * 0.35D, 0.045D);
            serverLevel.sendParticles(ParticleTypes.CRIT, groundPoint.x, groundPoint.y + 0.45D, groundPoint.z, 5, 0.16D, 0.12D, 0.16D, 0.035D);
            if (i % 2 == 0) {
                serverLevel.sendParticles(ParticleTypes.SWEEP_ATTACK, groundPoint.x, groundPoint.y + 0.25D, groundPoint.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
            }
        }

        serverLevel.sendParticles(ParticleTypes.FLASH, impactCenter.x, impactCenter.y + 0.25D, impactCenter.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        serverLevel.sendParticles(ParticleTypes.EXPLOSION, impactCenter.x, impactCenter.y - 0.65D, impactCenter.z, 2, 0.35D, 0.03D, 0.35D, 0.0D);
        serverLevel.sendParticles(ParticleTypes.CLOUD, impactCenter.x, impactCenter.y - 0.85D, impactCenter.z, 42, 1.55D, 0.08D, 1.55D, 0.08D);
        serverLevel.sendParticles(ParticleTypes.CRIT, impactCenter.x, impactCenter.y + 0.15D, impactCenter.z, 34, 1.0D, 0.45D, 1.0D, 0.08D);
        serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK, impactCenter.x, impactCenter.y + 0.25D, impactCenter.z, 20, 0.85D, 0.5D, 0.85D, 0.12D);
    }
}
