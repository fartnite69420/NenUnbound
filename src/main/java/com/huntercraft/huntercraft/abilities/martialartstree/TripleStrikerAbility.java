package com.huntercraft.huntercraft.abilities.martialartstree;

import com.huntercraft.huntercraft.abilities.SkillTreeCombatAbility;
import com.huntercraft.huntercraft.animation.AnimationType;
import com.huntercraft.huntercraft.damage.HunterDamageSources;
import com.huntercraft.huntercraft.data.HunterPlayerData;
import com.huntercraft.huntercraft.entity.ToraHuntEffectEntity;
import com.huntercraft.huntercraft.progression.SkillNode;
import com.huntercraft.huntercraft.util.HunterDataUtil;
import com.huntercraft.huntercraft.util.TeleportSafetyHelper;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class TripleStrikerAbility extends SkillTreeCombatAbility {
    private static final int CHARGE_TICKS = 6;
    private static final int ACTIVE_TICKS = 18;
    private static final double RANGE = 10.0D;
    private static final float HIT_DAMAGE = 13.0F;
    private static final float FINISH_DAMAGE = 22.0F;

    public TripleStrikerAbility() {
        super("triple_striker", "Triple Striker", "Rush into point blank range, land three rapid strikes, then knock the target away.", "textures/gui/abilities/triple_striker.png", SkillNode.MARTIAL_ARTS, 55);
    }

    @Override
    public int getMaxCooldownTicks() {
        return 250;
    }

    @Override
    public int getChargeTicks(HunterPlayerData data) {
        return data.isChargingAbility(this.id()) ? CHARGE_TICKS - data.getChargeTicksRemaining() : 0;
    }

    @Override
    public int getActiveTicks(HunterPlayerData data) {
        return data.isActiveAbility(this.id()) ? ACTIVE_TICKS - data.getActiveAbilityTicksRemaining() : 0;
    }

    @Override
    public void use(ServerPlayer player, HunterPlayerData data, Vec3 direction) {
        if (data.isChargingAbility(this.id()) || data.isActiveAbility(this.id())) {
            return;
        }
        LivingEntity target = MartialArtsGrabHelper.findTarget(player, RANGE, 1.4D);
        if (target == null) {
            return;
        }
        Vec3 forward = MartialArtsGrabHelper.getHorizontalForward(player, target.position().subtract(player.position()));
        data.startChargingAbility(this.id(), CHARGE_TICKS, forward);
        data.setChargeTargetUuid(target.getUUID().toString());
        data.triggerAnimation(AnimationType.MARTIAL_FACE_JAB);
        HunterDataUtil.sync(player);
    }

    @Override
    public void tick(ServerPlayer player, HunterPlayerData data) {
        if (data.isChargingAbility(this.id())) {
            data.tickChargingAbility();
            if (data.getChargeTicksRemaining() > 0) {
                return;
            }
            LivingEntity target = resolveTarget(player, data.getChargeTargetUuid());
            Vec3 forward = data.getChargeDirection();
            data.clearChargingAbility();
            if (target == null || !target.isAlive()) {
                this.startCooldown(data, 10);
                HunterDataUtil.sync(player);
                return;
            }
            startCombo(player, data, target, forward);
            return;
        }
        if (!data.isActiveAbility(this.id())) {
            return;
        }
        LivingEntity target = resolveTarget(player, data.getActiveAbilityTargetUuid());
        if (target == null || !target.isAlive()) {
            finish(player, data, null, data.getActiveAbilityDirection());
            return;
        }
        int elapsed = ACTIVE_TICKS - data.getActiveAbilityTicksRemaining();
        holdPair(player, target);
        if (elapsed == 1 || elapsed == 7 || elapsed == 13) {
            strike(player, target, elapsed == 13);
        }
        data.tickActiveAbility();
        if (data.getActiveAbilityTicksRemaining() <= 0) {
            finish(player, data, target, data.getActiveAbilityDirection());
        }
    }

    private void startCombo(ServerPlayer player, HunterPlayerData data, LivingEntity target, Vec3 forward) {
        Vec3 targetPos = target.position();
        Vec3 approach = targetPos.subtract(forward.scale(1.05D));
        Vec3 safe = TeleportSafetyHelper.resolveAroundTarget(player, new Vec3(approach.x, target.getY(), approach.z));
        player.teleportTo(safe.x, safe.y, safe.z);
        player.lookAt(EntityAnchorArgument.Anchor.EYES, target, EntityAnchorArgument.Anchor.EYES);
        data.startActiveAbility(this.id(), ACTIVE_TICKS, forward);
        data.setActiveAbilityTargetUuid(target.getUUID().toString());
        data.triggerAnimation(AnimationType.MARTIAL_FACE_JAB);
        playDashReleaseSound(player, 1.05F);
        HunterDataUtil.applyStun(target, player, ACTIVE_TICKS + 4);
        HunterDataUtil.sync(player);
    }

    private void holdPair(ServerPlayer player, LivingEntity target) {
        Vec3 forward = MartialArtsGrabHelper.getHorizontalForward(player, target.position().subtract(player.position()));
        Vec3 playerPos = target.position().subtract(forward.scale(0.95D));
        player.teleportTo(playerPos.x, target.getY(), playerPos.z);
        player.setDeltaMovement(Vec3.ZERO);
        player.hasImpulse = true;
        target.setDeltaMovement(Vec3.ZERO);
        target.hurtMarked = true;
        player.lookAt(EntityAnchorArgument.Anchor.EYES, target, EntityAnchorArgument.Anchor.EYES);
    }

    private void strike(ServerPlayer player, LivingEntity target, boolean finisher) {
        player.swing(InteractionHand.MAIN_HAND, true);
        player.getCapability(com.huntercraft.huntercraft.data.HunterPlayerDataProvider.CAPABILITY).ifPresent(data -> data.triggerAnimation(AnimationType.MARTIAL_FACE_JAB));
        playPunchReleaseSound(player, finisher ? 0.78F : 1.05F);
        target.invulnerableTime = 0;
        target.hurt(HunterDamageSources.physical(player.level(), player), finisher ? FINISH_DAMAGE : HIT_DAMAGE);
        target.invulnerableTime = 0;
        Vec3 forward = MartialArtsGrabHelper.getHorizontalForward(player, target.position().subtract(player.position()));
        if (player.level() instanceof ServerLevel serverLevel) {
            Vec3 center = target.position().add(0.0D, target.getBbHeight() * 0.55D, 0.0D);
            ToraHuntEffectEntity.spawn(serverLevel, center.subtract(forward.scale(0.55D)), forward, finisher ? 1.15F : 0.82F, finisher ? 14 : 10, finisher ? ToraHuntEffectEntity.STYLE_FINISHER : ToraHuntEffectEntity.STYLE_STRIKE);
            serverLevel.sendParticles(ParticleTypes.SWEEP_ATTACK, center.x, center.y, center.z, finisher ? 4 : 2, 0.16D, 0.08D, 0.16D, 0.0D);
            serverLevel.sendParticles(ParticleTypes.CRIT, center.x, center.y, center.z, finisher ? 18 : 10, 0.24D, 0.18D, 0.24D, 0.06D);
        }
    }

    private void finish(ServerPlayer player, HunterPlayerData data, LivingEntity target, Vec3 direction) {
        if (target != null && target.isAlive()) {
            Vec3 forward = MartialArtsGrabHelper.getHorizontalForward(player, direction);
            target.setDeltaMovement(forward.x * 2.1D, 0.42D, forward.z * 2.1D);
            target.hurtMarked = true;
        }
        data.clearActiveAbility();
        this.startCooldown(data, this.getMaxCooldownTicks());
        HunterDataUtil.sync(player);
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
