package com.huntercraft.huntercraft.abilities.deeppurple;

import com.huntercraft.huntercraft.abilities.SkillTreeCombatAbility;
import com.huntercraft.huntercraft.animation.AnimationType;
import com.huntercraft.huntercraft.data.HunterPlayerData;
import com.huntercraft.huntercraft.entity.HunterEntityTypes;
import com.huntercraft.huntercraft.entity.SmokeCloneEntity;
import com.huntercraft.huntercraft.faction.FactionUtil;
import com.huntercraft.huntercraft.item.HunterItems;
import com.huntercraft.huntercraft.particle.HunterParticles;
import com.huntercraft.huntercraft.progression.NenTechniqueSkillNode;
import com.huntercraft.huntercraft.progression.SkillNode;
import com.huntercraft.huntercraft.util.HunterDataUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;

public class SmokeCloneAbility extends SkillTreeCombatAbility {
    private static final int CHARGE_TICKS = 10;
    private static final int COOLDOWN_TICKS = 60;
    private static final int SMOKE_COST = 25;

    public SmokeCloneAbility() {
        super("smoke_clone", "Smoke Clone", "Shape a low-health smoke copy of a fighter. Crouch-use swaps between cloning yourself and cloning a faction ally you are looking at.", "textures/gui/abilities/smoke_clone.png", SkillNode.MARTIAL_ARTS, 0, com.huntercraft.huntercraft.abilities.AbilitySourceType.NEN);
    }

    @Override
    public boolean isUnlocked(HunterPlayerData data) {
        return data.hasUnlockedNenTechniqueNode(NenTechniqueSkillNode.DEEP_PURPLE_COLUMN);
    }

    @Override
    public int getMaxCooldownTicks() {
        return COOLDOWN_TICKS;
    }

    @Override
    public int getChargeTicks(HunterPlayerData data) {
        return data.isChargingAbility(this.id()) ? CHARGE_TICKS - data.getChargeTicksRemaining() : 0;
    }

    @Override
    public boolean canUse(ServerPlayer player, HunterPlayerData data) {
        if (player.isShiftKeyDown()) {
            return this.isUnlocked(data);
        }
        return super.canUse(player, data) && hasSmokingPipe(player) && data.getLungCapacity() >= SMOKE_COST;
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
        if (player.isShiftKeyDown()) {
            data.cycleDeepPurpleCloneMode();
            player.sendSystemMessage(Component.literal("Smoke Clone mode: " + data.getDeepPurpleCloneModeDisplayName()));
            HunterDataUtil.sync(player);
            return;
        }
        if (data.isChargingAbility(this.id())) {
            return;
        }
        data.startChargingAbility(this.id(), CHARGE_TICKS, Vec3.ZERO);
        data.triggerAnimation(AnimationType.SMOKE_SOLDIER_SUMMON);
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
        data.clearChargingAbility();
        summonClone(player, data);
    }

    private void summonClone(ServerPlayer player, HunterPlayerData data) {
        if (!(player.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        if (!data.consumeLungCapacity(SMOKE_COST)) {
            return;
        }

        ServerPlayer source = data.isDeepPurpleFactionCloneMode() ? findLookedFactionMate(player) : player;
        if (source == null) {
            data.addLungCapacity(SMOKE_COST);
            player.sendSystemMessage(Component.literal("Look at a faction member to copy them."));
            return;
        }

        SmokeCloneEntity clone = HunterEntityTypes.SMOKE_CLONE.get().create(serverLevel);
        if (clone == null) {
            data.addLungCapacity(SMOKE_COST);
            return;
        }

        clone.setOwner(player);
        clone.setCloneSource(source);
        clone.moveTo(player.getX(), player.getY(), player.getZ(), player.getYRot(), 0.0F);
        data.triggerAnimation(AnimationType.SMOKE_SOLDIER_SUMMON);
        spawnCloneSmoke(serverLevel, player);
        serverLevel.addFreshEntity(clone);
        this.startCooldown(data, COOLDOWN_TICKS);
        HunterDataUtil.sync(player);
    }

    private ServerPlayer findLookedFactionMate(ServerPlayer player) {
        Vec3 eyePosition = player.getEyePosition();
        Vec3 lookVector = player.getLookAngle().normalize();
        AABB searchBox = player.getBoundingBox().expandTowards(lookVector.scale(24.0D)).inflate(8.0D);
        return player.level().getEntitiesOfClass(ServerPlayer.class, searchBox, target ->
                        target != player
                                && target.isAlive()
                                && !target.isSpectator()
                                && FactionUtil.areFactionMates(player, target)
                                && player.hasLineOfSight(target))
                .stream()
                .filter(target -> {
                    Vec3 toTarget = target.getEyePosition().subtract(eyePosition);
                    double distanceSqr = toTarget.lengthSqr();
                    if (distanceSqr <= 1.0E-4D || distanceSqr > (24.0D * 24.0D)) {
                        return false;
                    }
                    return toTarget.normalize().dot(lookVector) >= 0.965D;
                })
                .min(Comparator.comparingDouble(player::distanceToSqr))
                .orElse(null);
    }

    private void spawnCloneSmoke(ServerLevel level, ServerPlayer player) {
        Vec3 look = player.getLookAngle().normalize();
        Vec3 mouth = player.getEyePosition().add(look.scale(0.3D)).add(0.0D, -0.12D, 0.0D);
        level.sendParticles(HunterParticles.MOREL_SMOKE.get(), mouth.x, mouth.y, mouth.z, 12, 0.07D, 0.07D, 0.07D, 0.02D);
        level.sendParticles(HunterParticles.MOREL_SMOKE.get(), player.getX(), player.getY(0.9D), player.getZ(), 16, 0.2D, 0.32D, 0.2D, 0.018D);
    }

    private static boolean hasSmokingPipe(ServerPlayer player) {
        return isSmokingPipe(player.getMainHandItem()) || isSmokingPipe(player.getOffhandItem());
    }

    private static boolean isSmokingPipe(ItemStack stack) {
        return !stack.isEmpty() && stack.is(HunterItems.SMOKING_PIPE.get());
    }
}
