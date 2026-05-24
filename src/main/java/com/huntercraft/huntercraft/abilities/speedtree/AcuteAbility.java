package com.huntercraft.huntercraft.abilities.speedtree;

import com.huntercraft.huntercraft.abilities.SkillTreeCombatAbility;
import com.huntercraft.huntercraft.animation.AnimationType;
import com.huntercraft.huntercraft.damage.HunterDamageSources;
import com.huntercraft.huntercraft.data.HunterPlayerData;
import com.huntercraft.huntercraft.entity.SlashEffectEntity;
import com.huntercraft.huntercraft.entity.SpeedBladeTrailEntity;
import com.huntercraft.huntercraft.network.HunterNetwork;
import com.huntercraft.huntercraft.network.packet.AfterImagePacket;
import com.huntercraft.huntercraft.progression.SkillNode;
import com.huntercraft.huntercraft.util.HunterDataUtil;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class AcuteAbility extends SkillTreeCombatAbility {
    private static final int CHARGE_TICKS = 6;
    private static final int DASH_TICKS = 7;
    private static final double DASH_DISTANCE = 8.5D;
    private static final float DAMAGE = 31.0F;
    private static final int CYAN = 0x7BE8FF;

    public AcuteAbility() {
        super("acute", "Acute", "Blink through the line ahead and leave a sharp blue cutting trail in your wake.", "textures/gui/abilities/acute.png", SkillNode.SPEED, 35);
    }

    @Override
    public int getMaxCooldownTicks() {
        return 190;
    }

    @Override
    public int getChargeTicks(HunterPlayerData data) {
        return data.isChargingAbility(this.id()) ? CHARGE_TICKS - data.getChargeTicksRemaining() : 0;
    }

    @Override
    public void use(ServerPlayer player, HunterPlayerData data, Vec3 direction) {
        if (data.isChargingAbility(this.id()) || data.isActiveAbility(this.id())) {
            return;
        }
        Vec3 forward = resolveForward(player, direction);
        if (forward.lengthSqr() < 1.0E-4D) {
            return;
        }
        data.startChargingAbility(this.id(), CHARGE_TICKS, forward);
        data.triggerAnimation(AnimationType.LION_FANG_DRAW_CHARGE);
        playDashReleaseSound(player, 1.32F);
    }

    @Override
    public void tick(ServerPlayer player, HunterPlayerData data) {
        if (data.isChargingAbility(this.id())) {
            data.tickChargingAbility();
            if (data.getChargeTicksRemaining() > 0) {
                return;
            }
            Vec3 forward = resolveForward(player, data.getChargeDirection());
            data.clearChargingAbility();
            execute(player, data, forward);
        }
    }

    private void execute(ServerPlayer player, HunterPlayerData data, Vec3 forward) {
        Vec3 start = player.position();
        Vec3 end = start.add(forward.scale(DASH_DISTANCE));
        if (player.level() instanceof ServerLevel serverLevel) {
            spawnAfterImage(player, start, 16);
            spawnAfterImage(player, start.add(forward.scale(1.8D)), 12);
            Vec3 center = start.add(end).scale(0.5D).add(0.0D, 1.0D, 0.0D);
            SpeedBladeTrailEntity.spawn(serverLevel, center, forward, (float) DASH_DISTANCE, 1.05F, 15, CYAN);
            SlashEffectEntity.spawn(serverLevel, end.add(0.0D, 1.0D, 0.0D), forward, -24.0F, 0.95F, 16, CYAN);
            serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK, start.x, start.y + 0.2D, start.z, 18, 0.18D, 0.08D, 0.18D, 0.09D);
            serverLevel.sendParticles(ParticleTypes.CLOUD, center.x, center.y - 0.62D, center.z, 18, 0.45D, 0.08D, 0.45D, 0.05D);
        }
        AABB hitBox = new AABB(start, end).inflate(1.6D, 1.2D, 1.6D);
        for (LivingEntity target : player.level().getEntitiesOfClass(LivingEntity.class, hitBox, entity -> entity != player && entity.isAlive())) {
            if (!player.hasLineOfSight(target)) {
                continue;
            }
            target.hurt(HunterDamageSources.weapon(player.level(), player), this.getWeaponScaledDamage(player, DAMAGE));
            target.knockback(0.55F, -forward.x, -forward.z);
        }
        player.teleportTo(end.x, end.y, end.z);
        player.setDeltaMovement(forward.scale(0.22D));
        player.hurtMarked = true;
        data.triggerAnimation(AnimationType.DASH);
        this.startCooldown(data, this.getMaxCooldownTicks());
        HunterDataUtil.sync(player);
    }

    private Vec3 resolveForward(ServerPlayer player, Vec3 direction) {
        Vec3 forward = direction.lengthSqr() > 1.0E-4D ? direction : player.getLookAngle();
        forward = new Vec3(forward.x, 0.0D, forward.z);
        return forward.lengthSqr() > 1.0E-4D ? forward.normalize() : Vec3.ZERO;
    }

    private void spawnAfterImage(ServerPlayer player, Vec3 position, int lifeTicks) {
        HunterNetwork.sendToTrackingAndSelf(player, new AfterImagePacket(player.getUUID(), position.x, position.y, position.z, player.getYRot(), player.getXRot(), lifeTicks));
    }
}
