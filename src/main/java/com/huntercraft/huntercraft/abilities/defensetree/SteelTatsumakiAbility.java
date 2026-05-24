package com.huntercraft.huntercraft.abilities.defensetree;

import com.huntercraft.huntercraft.abilities.SkillTreeCombatAbility;
import com.huntercraft.huntercraft.animation.AnimationType;
import com.huntercraft.huntercraft.damage.HunterDamageSources;
import com.huntercraft.huntercraft.data.HunterPlayerData;
import com.huntercraft.huntercraft.effect.HunterMobEffects;
import com.huntercraft.huntercraft.entity.WhirlwindSlashEntity;
import com.huntercraft.huntercraft.progression.SkillNode;
import com.huntercraft.huntercraft.util.HunterDataUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class SteelTatsumakiAbility extends SkillTreeCombatAbility {
    private static final int CHARGE_TICKS = 6;
    private static final int ACTIVE_TICKS = 24;
    private static final int SELF_STUN_REFRESH_TICKS = 3;
    private static final float BASE_DAMAGE = 30.0F;
    private static final double RADIUS = 6.5D;
    private static final int RED_SLASH_COLOR = 0xFF2424;

    public SteelTatsumakiAbility() {
        super("steel_tatsumaki", "Steel Whirlwind", "Whip a giant circular storm of slashes around yourself and grind everything nearby into the tornado.", "textures/gui/abilities/steel_whirlwind.png", SkillNode.DEFENSE, 50);
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
        return data.isActiveAbility(this.id()) ? ACTIVE_TICKS - data.getActiveAbilityTicksRemaining() : 0;
    }

    @Override
    public int getChargeTicks(HunterPlayerData data) {
        return data.isChargingAbility(this.id()) ? CHARGE_TICKS - data.getChargeTicksRemaining() : 0;
    }

    @Override
    public void use(ServerPlayer player, HunterPlayerData data, Vec3 direction) {
        if (data.isActiveAbility(this.id())) {
            this.stop(player, data);
            HunterDataUtil.sync(player);
            return;
        }
        if (data.isChargingAbility(this.id())) {
            data.clearChargingAbility();
            HunterDataUtil.sync(player);
            return;
        }
        data.startChargingAbility(this.id(), CHARGE_TICKS, new Vec3(player.getX(), 0.0D, player.getZ()));
        data.triggerAnimation(AnimationType.STEEL_TATSUMAKI);
        HunterDataUtil.sync(player);
    }

    private void beginWhirlwind(ServerPlayer player, HunterPlayerData data) {
        Vec3 anchor = new Vec3(player.getX(), 0.0D, player.getZ());
        data.startActiveAbility(this.id(), ACTIVE_TICKS, anchor);
        data.triggerAnimation(AnimationType.STEEL_TATSUMAKI);
        playSlashReleaseSound(player, 0.9F);
        player.addEffect(new MobEffectInstance(HunterMobEffects.STUNNED.get(), SELF_STUN_REFRESH_TICKS, 0, false, false, true));
        player.setDeltaMovement(Vec3.ZERO);
        player.hasImpulse = true;
        pulse(player, 0);
    }

    @Override
    public void tick(ServerPlayer player, HunterPlayerData data) {
        if (data.isChargingAbility(this.id())) {
            player.setDeltaMovement(Vec3.ZERO);
            player.hasImpulse = true;
            data.tickChargingAbility();
            if (data.getChargeTicksRemaining() <= 0) {
                data.clearChargingAbility();
                beginWhirlwind(player, data);
            }
            return;
        }
        if (!data.isActiveAbility(this.id())) {
            return;
        }
        Vec3 anchor = data.getActiveAbilityDirection();
        player.setPos(anchor.x, player.getY(), anchor.z);
        player.addEffect(new MobEffectInstance(HunterMobEffects.STUNNED.get(), SELF_STUN_REFRESH_TICKS, 0, false, false, true));
        player.setDeltaMovement(Vec3.ZERO);
        player.hasImpulse = true;
        player.stopUsingItem();
        player.setSprinting(false);

        int remaining = data.getActiveAbilityTicksRemaining();
        if (remaining % 3 == 0) {
            pulse(player, ACTIVE_TICKS - remaining);
        }

        data.tickActiveAbility();
        if (data.getActiveAbilityTicksRemaining() <= 0) {
            data.clearActiveAbility();
            this.startCooldown(data, this.getMaxCooldownTicks());
            HunterDataUtil.sync(player);
        }
    }

    @Override
    public void stop(ServerPlayer player, HunterPlayerData data) {
        if (!data.isActiveAbility(this.id())) {
            return;
        }
        data.clearActiveAbility();
        this.startCooldown(data, this.getMaxCooldownTicks());
        player.setDeltaMovement(Vec3.ZERO);
        player.hasImpulse = true;
    }

    private void pulse(ServerPlayer player, int stage) {
        Vec3 center = player.position().add(0.0D, 1.0D, 0.0D);
        AABB hitBox = new AABB(center.x - RADIUS, center.y - 1.0D, center.z - RADIUS, center.x + RADIUS, center.y + 2.4D, center.z + RADIUS);
        for (LivingEntity target : player.level().getEntitiesOfClass(LivingEntity.class, hitBox, entity -> entity != player && entity.isAlive())) {
            Vec3 pull = center.subtract(target.position());
            Vec3 pushDir = pull.lengthSqr() > 1.0E-4D ? pull.normalize() : Vec3.ZERO;
            target.hurt(HunterDamageSources.weapon(player.level(), player), this.getWeaponScaledDamage(player, BASE_DAMAGE));
            target.setDeltaMovement(pushDir.x * 0.32D, 0.18D, pushDir.z * 0.32D);
            target.hurtMarked = true;
            if (stage >= ACTIVE_TICKS - 6) {
                HunterDataUtil.applyStun(target, player, 16);
            }
        }

        if (player.level() instanceof ServerLevel serverLevel) {
            spawnWhirlwindSlashVisuals(serverLevel, center, stage);
        }
    }

    private void spawnWhirlwindSlashVisuals(ServerLevel serverLevel, Vec3 center, int stage) {
        for (int ring = 0; ring < 4; ring++) {
            double orbitRadius = 1.65D + ring * 1.18D;
            double y = center.y - 0.35D + ring * 0.48D;
            double twist = stage * 0.42D + ring * 0.82D;
            int slashCount = 2 + ring;
            for (int i = 0; i < slashCount; i++) {
                double angle = twist + (Math.PI * 2.0D * i / slashCount);
                Vec3 radial = new Vec3(Math.cos(angle), 0.0D, Math.sin(angle));
                Vec3 effectCenter = center
                        .add(radial.scale(orbitRadius))
                        .add(0.0D, y - center.y + serverLevel.random.nextDouble() * 0.18D, 0.0D);
                float bladeRadius = 1.15F + ring * 0.18F + serverLevel.random.nextFloat() * 0.16F;
                float arcLength = 1.18F + ring * 0.08F + serverLevel.random.nextFloat() * 0.18F;
                float spinSpeed = 0.12F + ring * 0.02F;
                float height = 0.42F + ring * 0.08F;
                WhirlwindSlashEntity.spawn(serverLevel, effectCenter, bladeRadius, height, (float) (angle + Math.PI * 0.5D), arcLength, spinSpeed, 10 + ring * 2, RED_SLASH_COLOR);
            }
        }
    }
}
