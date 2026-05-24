package com.huntercraft.huntercraft.abilities.deeppurple;

import com.huntercraft.huntercraft.abilities.SkillTreeCombatAbility;
import com.huntercraft.huntercraft.animation.AnimationType;
import com.huntercraft.huntercraft.data.HunterPlayerData;
import com.huntercraft.huntercraft.entity.SmokeSoldierEntity;
import com.huntercraft.huntercraft.particle.HunterParticles;
import com.huntercraft.huntercraft.progression.NenTechniqueSkillNode;
import com.huntercraft.huntercraft.progression.SkillNode;
import com.huntercraft.huntercraft.util.HunterDataUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

public class SmokeSoldierAbility extends SkillTreeCombatAbility {
    private static final int CHARGE_TICKS = 10;
    private static final int COOLDOWN_TICKS = 60;

    public SmokeSoldierAbility() {
        super("smoke_soldier", "Smoke Soldier", "Shape a smoke soldier from your pipe. Crouch-use to swap between Hunt and Return orders.", "textures/gui/abilities/smoke_soldier.png", SkillNode.MARTIAL_ARTS, 0, com.huntercraft.huntercraft.abilities.AbilitySourceType.NEN);
    }

    @Override
    public boolean isUnlocked(HunterPlayerData data) {
        return data.hasUnlockedNenTechniqueNode(NenTechniqueSkillNode.DEEP_PURPLE_PUPPET);
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
        return super.canUse(player, data) && data.getLungCapacity() >= SmokeSoldierEntity.SMOKE_COST;
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
            data.cycleDeepPurpleSoldierMode();
            player.sendSystemMessage(Component.literal("Smoke Soldier mode: " + data.getDeepPurpleSoldierModeDisplayName()));
            HunterDataUtil.sync(player);
            return;
        }
        if (data.isDeepPurpleReturnMode()) {
            data.clearDeepPurplePing();
            player.sendSystemMessage(Component.literal("Smoke Soldiers returning to you."));
            this.startCooldown(data, 6);
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
        summonSoldier(player, data);
    }

    private void summonSoldier(ServerPlayer player, HunterPlayerData data) {
        if (!(player.level() instanceof ServerLevel serverLevel) || !data.consumeLungCapacity(SmokeSoldierEntity.SMOKE_COST)) {
            return;
        }

        SmokeSoldierEntity soldier = com.huntercraft.huntercraft.entity.HunterEntityTypes.SMOKE_SOLDIER.get().create(serverLevel);
        if (soldier == null) {
            data.addLungCapacity(SmokeSoldierEntity.SMOKE_COST);
            return;
        }

        soldier.setOwner(player);
        soldier.copyOwnerSnapshot(data);
        soldier.setScoutAngleDegrees(getScoutAngle(player));
        soldier.moveTo(player.getX(), player.getY(), player.getZ(), player.getYRot(), 0.0F);
        data.triggerAnimation(AnimationType.SMOKE_SOLDIER_SUMMON);
        spawnSummonSmoke(serverLevel, player);
        serverLevel.addFreshEntity(soldier);
        this.startCooldown(data, COOLDOWN_TICKS);
        HunterDataUtil.sync(player);
    }

    private float getScoutAngle(ServerPlayer player) {
        Vec3 look = player.getLookAngle();
        return (float) Math.toDegrees(Math.atan2(look.z, look.x));
    }

    private void spawnSummonSmoke(ServerLevel level, ServerPlayer player) {
        Vec3 look = player.getLookAngle().normalize();
        Vec3 mouth = player.getEyePosition().add(look.scale(0.32D)).add(0.0D, -0.12D, 0.0D);
        level.sendParticles(HunterParticles.MOREL_SMOKE.get(), mouth.x, mouth.y, mouth.z, 10, 0.06D, 0.06D, 0.06D, 0.02D);
        level.sendParticles(HunterParticles.MOREL_SMOKE.get(), mouth.x + (look.x * 0.15D), mouth.y + 0.02D, mouth.z + (look.z * 0.15D), 16, 0.12D, 0.08D, 0.12D, 0.03D);
        level.sendParticles(HunterParticles.MOREL_SMOKE.get(), player.getX(), player.getY(0.9D), player.getZ(), 8, 0.18D, 0.28D, 0.18D, 0.015D);
    }
}
