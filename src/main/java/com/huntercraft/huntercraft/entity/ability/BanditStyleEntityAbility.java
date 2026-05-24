package com.huntercraft.huntercraft.entity.ability;

import com.huntercraft.huntercraft.effect.HunterMobEffects;
import com.huntercraft.huntercraft.entity.BanditAbilityProfile;
import com.huntercraft.huntercraft.util.HunterDataUtil;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.function.Supplier;

public class BanditStyleEntityAbility implements EntityAbility {
    private final Supplier<BanditAbilityProfile> profileSupplier;

    public BanditStyleEntityAbility(Supplier<BanditAbilityProfile> profileSupplier) {
        this.profileSupplier = profileSupplier;
    }

    @Override
    public String id() {
        return "bandit_style";
    }

    @Override
    public int cooldownTicks(Mob mob) {
        return this.profileSupplier.get().cooldownTicks();
    }

    @Override
    public int windupTicks(Mob mob) {
        return 12;
    }

    @Override
    public boolean canUse(Mob mob, LivingEntity target) {
        return target.isAlive() && mob.distanceToSqr(target) <= 81.0D && mob.hasLineOfSight(target);
    }

    @Override
    public void use(Mob mob, LivingEntity primaryTarget) {
        BanditAbilityProfile profile = this.profileSupplier.get();
        Vec3 direction = primaryTarget.position().subtract(mob.position()).normalize();
        if (!Double.isFinite(direction.x) || !Double.isFinite(direction.z)) {
            direction = new Vec3(0.0D, 0.0D, 1.0D);
        }

        mob.lookAt(primaryTarget, 30.0F, 30.0F);
        mob.swing(net.minecraft.world.InteractionHand.MAIN_HAND);
        mob.setDeltaMovement(direction.x * profile.lungeStrength(), Math.max(mob.getDeltaMovement().y, profile.launchStrength()), direction.z * profile.lungeStrength());
        mob.hasImpulse = true;

        if (profile.areaRadius() > 0.0F) {
            AABB box = mob.getBoundingBox().inflate(profile.areaRadius());
            List<LivingEntity> targets = mob.level().getEntitiesOfClass(LivingEntity.class, box, entity ->
                    entity != mob && entity.isAlive() && entity instanceof Player);
            for (LivingEntity target : targets) {
                if (mob.hasLineOfSight(target)) {
                    hitTarget(mob, target, profile);
                }
            }
        } else {
            hitTarget(mob, primaryTarget, profile);
        }
    }

    private static void hitTarget(Mob mob, LivingEntity target, BanditAbilityProfile profile) {
        float damage = profile.baseDamage();
        if (profile.requiresWeapon()) {
            damage += (float) mob.getAttributeValue(Attributes.ATTACK_DAMAGE);
        }
        boolean hit = target.hurt(mob.damageSources().mobAttack(mob), damage);
        if (!hit) {
            return;
        }

        Vec3 knockbackDir = target.position().subtract(mob.position());
        if (knockbackDir.lengthSqr() > 1.0E-4D) {
            knockbackDir = knockbackDir.normalize();
            target.push(knockbackDir.x * profile.knockbackStrength(), profile.launchStrength(), knockbackDir.z * profile.knockbackStrength());
        } else if (profile.launchStrength() > 0.0F) {
            target.push(0.0D, profile.launchStrength(), 0.0D);
        }

        if (profile.stunTicks() > 0 && target instanceof ServerPlayer serverPlayer) {
            HunterDataUtil.applyStun(serverPlayer, null, profile.stunTicks());
        } else if (profile.stunTicks() > 0) {
            target.addEffect(new MobEffectInstance(HunterMobEffects.STUNNED.get(), profile.stunTicks(), 0, false, false, true));
        }

        mob.playSound(profile.requiresWeapon() ? SoundEvents.PLAYER_ATTACK_SWEEP : SoundEvents.PLAYER_ATTACK_STRONG, 0.9F, 0.9F + (mob.getRandom().nextFloat() * 0.1F));
    }
}
