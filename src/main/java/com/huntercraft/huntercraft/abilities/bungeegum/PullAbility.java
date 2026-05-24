package com.huntercraft.huntercraft.abilities.bungeegum;

import com.huntercraft.huntercraft.abilities.SkillTreeCombatAbility;
import com.huntercraft.huntercraft.abilities.nenability.NenTechniqueAbility;
import com.huntercraft.huntercraft.damage.HunterDamageSources;
import com.huntercraft.huntercraft.data.HunterPlayerData;
import com.huntercraft.huntercraft.effect.HunterMobEffects;
import com.huntercraft.huntercraft.entity.ElasticAuraProjectileEntity;
import com.huntercraft.huntercraft.entity.HunterEntityTypes;
import com.huntercraft.huntercraft.progression.NenTechniqueSkillNode;
import com.huntercraft.huntercraft.progression.SkillNode;
import com.huntercraft.huntercraft.util.HunterDataUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class PullAbility extends SkillTreeCombatAbility {
    public static final int NEN_COST = 250;
    private static final String PULL_HIDDEN_TAG = "HuntercraftElasticPullHidden";
    private static final int COOLDOWN_TICKS = 12 * 20;
    private static final int ACTIVE_TICKS = 46;
    private static final int PUNCH_TRIGGER_REMAINING = 10;
    private static final float DAMAGE = 26.0F;

    public PullAbility() {
        super("elastic_pull", "Pull", "Fire a fast gum projectile that drags a target into your grasp, holds them in place, then blasts them away with a heavy Nen punch.", "textures/gui/abilities/bungee_gum_pull.png", SkillNode.MARTIAL_ARTS, 0, com.huntercraft.huntercraft.abilities.AbilitySourceType.NEN, com.huntercraft.huntercraft.abilities.AbilitySourceType.BLUNT);
    }

    @Override
    public boolean isUnlocked(HunterPlayerData data) {
        return data.hasUnlockedNenTechniqueNode(NenTechniqueSkillNode.ELASTIC_AURA_PULL);
    }

    @Override
    public int getMaxCooldownTicks() {
        return COOLDOWN_TICKS;
    }

    @Override
    public boolean isContinuous() {
        return true;
    }

    @Override
    protected boolean requiresWeaponInHand() {
        return false;
    }

    @Override
    protected boolean requiresEmptyMainHand() {
        return false;
    }

    @Override
    public boolean canUse(ServerPlayer player, HunterPlayerData data) {
        return super.canUse(player, data)
                && ElasticAuraManager.TECHNIQUE_ID.equals(data.getNenTechniqueId())
                && data.hasStaminaForNenCost(NEN_COST);
    }

    @Override
    public void use(ServerPlayer player, HunterPlayerData data, Vec3 direction) {
        if (!(player.level() instanceof ServerLevel level) || !data.consumeNen(NEN_COST)) {
            return;
        }
        ElasticAuraProjectileEntity projectile = HunterEntityTypes.ELASTIC_AURA_PROJECTILE.get().create(level);
        if (projectile == null) {
            data.addStamina(data.getReducedNenStaminaCost(NEN_COST));
            return;
        }
        boolean hidden = ElasticAuraManager.consumeTextureSurprise(player);
        Vec3 launch = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : player.getLookAngle().normalize();
        projectile.configure(player, ElasticAuraProjectileEntity.MODE_PULL, hidden);
        projectile.moveTo(player.getX(), player.getEyeY() - 0.08D, player.getZ(), player.getYRot(), player.getXRot());
        projectile.setDeltaMovement(launch.scale(4.65D));
        level.addFreshEntity(projectile);
        this.startCooldown(data, COOLDOWN_TICKS);
    }

    @Override
    public void tick(ServerPlayer player, HunterPlayerData data) {
        if (!data.isActiveAbility(this.id()) || !(player.level() instanceof ServerLevel level)) {
            return;
        }
        LivingEntity target = resolveTarget(level, data.getActiveAbilityTargetUuid());
        if (target == null || !target.isAlive()) {
            stop(player, data);
            return;
        }

        int remaining = data.getActiveAbilityTicksRemaining();
        Vec3 holdPoint = player.position().add(player.getLookAngle().normalize().scale(1.1D)).add(0.0D, 0.85D, 0.0D);
        if (remaining > PUNCH_TRIGGER_REMAINING + 10) {
            Vec3 towardPlayer = holdPoint.subtract(target.position());
            Vec3 pull = towardPlayer.normalize().scale(Math.min(1.55D, 0.48D + (towardPlayer.length() * 0.16D)));
            target.setDeltaMovement(pull.x, Math.max(-0.12D, pull.y * 0.42D), pull.z);
        } else if (remaining > PUNCH_TRIGGER_REMAINING) {
            target.teleportTo(holdPoint.x, holdPoint.y - (target.getBbHeight() * 0.5D), holdPoint.z);
            target.setDeltaMovement(Vec3.ZERO);
            HunterDataUtil.applyStun(target, player, 6);
        } else if (remaining == PUNCH_TRIGGER_REMAINING) {
            Vec3 forward = player.getLookAngle().normalize();
            player.swing(InteractionHand.MAIN_HAND, true);
            playPunchReleaseSound(player, 0.78F);
            target.hurt(HunterDamageSources.physical(player.level(), player), DAMAGE + NenTechniqueAbility.getPassiveRenDamageBonus(data));
            Vec3 knockback = target.position().subtract(player.position());
            if (knockback.horizontalDistanceSqr() <= 1.0E-4D) {
                knockback = forward;
            }
            knockback = new Vec3(knockback.x, 0.0D, knockback.z).normalize();
            target.removeEffect(HunterMobEffects.STUNNED.get());
            target.setDeltaMovement(knockback.x * 6.0D, 0.85D, knockback.z * 6.0D);
            target.hurtMarked = true;
            target.hasImpulse = true;
            ElasticAuraManager.clearPullString(player);
        }

        data.tickActiveAbility();
        if (data.getActiveAbilityTicksRemaining() <= 0) {
            stop(player, data);
        }
    }

    @Override
    public void stop(ServerPlayer player, HunterPlayerData data) {
        data.clearActiveAbility();
        player.getPersistentData().remove(PULL_HIDDEN_TAG);
        ElasticAuraManager.clearPullString(player);
    }

    public void startSequence(ServerPlayer player, HunterPlayerData data, LivingEntity target, boolean hidden) {
        data.startActiveAbility(this.id(), ACTIVE_TICKS, player.getLookAngle());
        data.setActiveAbilityTargetUuid(target.getUUID().toString());
        if (hidden) {
            player.getPersistentData().putBoolean(PULL_HIDDEN_TAG, true);
        } else {
            player.getPersistentData().remove(PULL_HIDDEN_TAG);
        }
        ElasticAuraManager.spawnPullString(player, target, hidden, ACTIVE_TICKS);
        HunterDataUtil.sync(player);
    }

    private static LivingEntity resolveTarget(ServerLevel level, String uuid) {
        if (uuid == null || uuid.isBlank()) {
            return null;
        }
        try {
            return level.getEntity(UUID.fromString(uuid)) instanceof LivingEntity living ? living : null;
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }
}
