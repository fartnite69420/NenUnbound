package com.huntercraft.huntercraft.abilities.deeppurple;

import com.huntercraft.huntercraft.abilities.SkillTreeCombatAbility;
import com.huntercraft.huntercraft.data.HunterPlayerData;
import com.huntercraft.huntercraft.entity.SmokeyChainProjectileEntity;
import com.huntercraft.huntercraft.item.HunterItems;
import com.huntercraft.huntercraft.particle.HunterParticles;
import com.huntercraft.huntercraft.progression.NenTechniqueSkillNode;
import com.huntercraft.huntercraft.progression.SkillNode;
import com.huntercraft.huntercraft.util.HunterDataUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class SmokeyChainAbility extends SkillTreeCombatAbility {
    private static final int COOLDOWN_TICKS = 160;
    private static final int CHAIN_TICKS = 60;
    private static final double MAX_CHAIN_DISTANCE = 14.0D;
    private static final int LUNG_COST = 20;

    public SmokeyChainAbility() {
        super("smokey_chain", "Smokey Chain", "Fire a smoke cloud from your pipe. If it lands, the target gets dragged on a smoke chain back to you.", "textures/gui/abilities/smokey_chain.png", SkillNode.MARTIAL_ARTS, 0, com.huntercraft.huntercraft.abilities.AbilitySourceType.NEN, com.huntercraft.huntercraft.abilities.AbilitySourceType.BLUNT);
    }

    @Override
    public boolean isUnlocked(HunterPlayerData data) {
        return data.hasUnlockedNenTechniqueNode(NenTechniqueSkillNode.DEEP_PURPLE_VEIL);
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
    public int getActiveTicks(HunterPlayerData data) {
        return data.isActiveAbility(this.id()) ? CHAIN_TICKS - data.getActiveAbilityTicksRemaining() : 0;
    }

    @Override
    public boolean canUse(ServerPlayer player, HunterPlayerData data) {
        return super.canUse(player, data) && hasSmokingPipe(player) && data.getLungCapacity() >= LUNG_COST;
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
    public void use(ServerPlayer player, HunterPlayerData data, Vec3 direction) {
        if (data.isActiveAbility(this.id())) {
            stop(player, data);
            return;
        }
        if (!(player.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        if (!data.consumeLungCapacity(LUNG_COST)) {
            return;
        }
        Vec3 launch = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : player.getLookAngle().normalize();
        SmokeyChainProjectileEntity projectile = SmokeyChainProjectileEntity.create(serverLevel, player, launch, this.id());
        if (projectile == null) {
            data.addLungCapacity(LUNG_COST);
            return;
        }
        serverLevel.addFreshEntity(projectile);
        this.startCooldown(data, this.getMaxCooldownTicks());
        HunterDataUtil.sync(player);
    }

    @Override
    public void tick(ServerPlayer player, HunterPlayerData data) {
        if (!data.isActiveAbility(this.id()) || !(player.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        LivingEntity target = resolveTarget(serverLevel, data.getActiveAbilityTargetUuid());
        if (target == null || !target.isAlive() || !hasSmokingPipe(player)) {
            stop(player, data);
            return;
        }

        Vec3 anchor = getPipeAnchor(player);
        Vec3 targetCenter = target.position().add(0.0D, target.getBbHeight() * 0.55D, 0.0D);
        Vec3 towardAnchor = anchor.subtract(targetCenter);
        double distance = towardAnchor.length();
        if (distance > MAX_CHAIN_DISTANCE * 1.4D) {
            stop(player, data);
            return;
        }
        if (distance > MAX_CHAIN_DISTANCE) {
            towardAnchor = towardAnchor.normalize().scale(0.55D);
            target.setDeltaMovement(target.getDeltaMovement().scale(0.55D).add(towardAnchor.x, towardAnchor.y * 0.35D, towardAnchor.z));
        } else if (distance > 2.25D) {
            Vec3 pull = towardAnchor.normalize().scale(0.28D + Math.min(0.22D, (distance - 2.25D) * 0.045D));
            target.setDeltaMovement(target.getDeltaMovement().scale(0.72D).add(pull.x, pull.y * 0.28D, pull.z));
        } else {
            target.setDeltaMovement(target.getDeltaMovement().scale(0.5D));
        }
        target.hurtMarked = true;
        target.hasImpulse = true;

        spawnChainParticles(serverLevel, anchor, targetCenter);

        data.tickActiveAbility();
        if (!data.isActiveAbility(this.id())) {
            stop(player, data);
        }
    }

    @Override
    public void stop(ServerPlayer player, HunterPlayerData data) {
        if (!data.isActiveAbility(this.id())) {
            return;
        }
        data.clearActiveAbility();
        HunterDataUtil.sync(player);
    }

    public void attachTarget(ServerPlayer player, HunterPlayerData data, LivingEntity target) {
        data.startActiveAbility(this.id(), CHAIN_TICKS, Vec3.ZERO);
        data.setActiveAbilityTargetUuid(target.getUUID().toString());
        HunterDataUtil.sync(player);
    }

    private static boolean hasSmokingPipe(ServerPlayer player) {
        return isSmokingPipe(player.getMainHandItem()) || isSmokingPipe(player.getOffhandItem());
    }

    private static boolean isSmokingPipe(ItemStack stack) {
        return !stack.isEmpty() && stack.is(HunterItems.SMOKING_PIPE.get());
    }

    private static LivingEntity resolveTarget(ServerLevel level, String uuidString) {
        if (uuidString == null || uuidString.isBlank()) {
            return null;
        }
        try {
            return level.getEntity(UUID.fromString(uuidString)) instanceof LivingEntity living ? living : null;
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private static Vec3 getPipeAnchor(ServerPlayer player) {
        Vec3 look = player.getLookAngle().normalize();
        return player.getEyePosition().add(look.scale(0.7D)).add(0.0D, -0.18D, 0.0D);
    }

    private static void spawnChainParticles(ServerLevel level, Vec3 start, Vec3 end) {
        Vec3 delta = end.subtract(start);
        int segments = Math.max(12, (int) Math.ceil(delta.length() * 1.4D));
        Vec3 step = delta.scale(1.0D / segments);
        for (int i = 0; i <= segments; i++) {
            Vec3 point = start.add(step.scale(i));
            double spread = 0.025D + (0.012D * Math.sin((i / (double) Math.max(1, segments)) * Math.PI));
            level.sendParticles(HunterParticles.SMOKY_CHAIN_SMOKE.get(), point.x, point.y, point.z, 5 + (i % 4), spread, spread, spread, 0.012D);
        }
        level.sendParticles(HunterParticles.SMOKY_CHAIN_SMOKE.get(), start.x, start.y, start.z, 12, 0.03D, 0.03D, 0.03D, 0.02D);
        level.sendParticles(HunterParticles.SMOKY_CHAIN_SMOKE.get(), end.x, end.y, end.z, 15, 0.06D, 0.06D, 0.06D, 0.025D);
    }
}
