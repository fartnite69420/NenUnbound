package com.huntercraft.huntercraft.abilities.deeppurple;

import com.huntercraft.huntercraft.abilities.SkillTreeCombatAbility;
import com.huntercraft.huntercraft.data.HunterPlayerData;
import com.huntercraft.huntercraft.entity.HunterEntityTypes;
import com.huntercraft.huntercraft.entity.SmokyJailBarrierEntity;
import com.huntercraft.huntercraft.item.HunterItems;
import com.huntercraft.huntercraft.progression.NenTechniqueSkillNode;
import com.huntercraft.huntercraft.progression.SkillNode;
import com.huntercraft.huntercraft.util.HunterDataUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class SmokeyJailAbility extends SkillTreeCombatAbility {
    public static final float BARRIER_RADIUS = 20.0F;
    public static final int MAX_DURATION_TICKS = 20 * 30;
    private static final int MAX_COOLDOWN_TICKS = 20 * 60;  // 60 seconds
    private static final int MIN_COOLDOWN_TICKS = 20 * 10;  // 10 seconds
    private static final int SMOKE_COST = 100;
    private static final int NEN_COST = 1000;

    public SmokeyJailAbility() {
        super("smokey_jail", "Smokey Jail", "Create a 20-block smoke barrier that traps movement across its wall and blocks incoming projectiles for 30 seconds. Use again to cancel it early.", "textures/gui/abilities/smokey_jail.png", SkillNode.MARTIAL_ARTS, 0, com.huntercraft.huntercraft.abilities.AbilitySourceType.NEN, com.huntercraft.huntercraft.abilities.AbilitySourceType.BLUNT);
    }

    @Override
    public boolean isUnlocked(HunterPlayerData data) {
        return data.hasUnlockedNenTechniqueNode(NenTechniqueSkillNode.DEEP_PURPLE_DOMAIN);
    }

    @Override
    public int getMaxCooldownTicks() {
        return MAX_COOLDOWN_TICKS;
    }

    @Override
    public boolean isContinuous() {
        return true;
    }

    @Override
    public boolean isPassiveWhileActive() {
        return true;
    }

    @Override
    public int getActiveTicks(HunterPlayerData data) {
        return data.isActiveAbility(this.id()) ? data.getActiveAbilityTicksRemaining() : 0;
    }

    @Override
    public boolean canUse(ServerPlayer player, HunterPlayerData data) {
        if (this.isActive(data)) {
            return true;
        }
        return super.canUse(player, data)
                && hasSmokingPipe(player)
                && data.getLungCapacity() >= SMOKE_COST
                && data.hasStaminaForNenCost(NEN_COST);
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
        if (this.isActive(data)) {
            this.stop(player, data);
            return;
        }
        if (!(player.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        if (findBarrier(player) != null) {
            player.sendSystemMessage(Component.literal("Smoky Jail is already active."));
            return;
        }
        if (!data.consumeLungCapacity(SMOKE_COST)) {
            return;
        }
        if (!data.consumeNen(NEN_COST)) {
            data.addLungCapacity(SMOKE_COST);
            return;
        }

        SmokyJailBarrierEntity barrier = HunterEntityTypes.SMOKY_JAIL_BARRIER.get().create(serverLevel);
        if (barrier == null) {
            data.addLungCapacity(SMOKE_COST);
            data.addStamina(data.getReducedNenStaminaCost(NEN_COST));
            return;
        }

        barrier.setOwner(player);
        barrier.setRadius(BARRIER_RADIUS);
        barrier.setDurationTicks(MAX_DURATION_TICKS);
        barrier.moveTo(player.getX(), player.getY() + (player.getBbHeight() * 0.5D), player.getZ(), 0.0F, 0.0F);
        serverLevel.addFreshEntity(barrier);
        data.startActiveAbility(this.id(), MAX_DURATION_TICKS, Vec3.ZERO);
        HunterDataUtil.sync(player);
    }

    @Override
    public void tick(ServerPlayer player, HunterPlayerData data) {
        if (!this.isActive(data)) {
            return;
        }
        data.tickActiveAbility();
        HunterDataUtil.sync(player);
        SmokyJailBarrierEntity barrier = findBarrier(player);
        if (barrier == null) {
            this.stop(player, data);
        }
    }

    @Override
    public void stop(ServerPlayer player, HunterPlayerData data) {
        SmokyJailBarrierEntity barrier = findBarrier(player);
        if (barrier != null) {
            barrier.discard();
        }
        if (data.isActiveAbility(this.id())) {
            // Compute heldTicks BEFORE clearing the active ability (remaining ticks are still valid here)
            int heldTicks = MAX_DURATION_TICKS - data.getActiveAbilityTicksRemaining();
            // Scale cooldown linearly: 0s held = MIN_COOLDOWN, full 30s held = MAX_COOLDOWN
            float fraction = Math.min(1.0F, (float) heldTicks / MAX_DURATION_TICKS);
            int cooldown = MIN_COOLDOWN_TICKS + (int)(fraction * (MAX_COOLDOWN_TICKS - MIN_COOLDOWN_TICKS));
            data.clearActiveAbility();
            this.startCooldown(data, cooldown);
            
        }
    }

    private SmokyJailBarrierEntity findBarrier(ServerPlayer player) {
        AABB searchBox = player.getBoundingBox().inflate(BARRIER_RADIUS + 24.0D);
        return player.serverLevel().getEntitiesOfClass(SmokyJailBarrierEntity.class, searchBox, barrier -> barrier.isOwnedBy(player))
                .stream()
                .findFirst()
                .orElse(null);
    }

    private static boolean hasSmokingPipe(ServerPlayer player) {
        return isSmokingPipe(player.getMainHandItem()) || isSmokingPipe(player.getOffhandItem());
    }

    private static boolean isSmokingPipe(ItemStack stack) {
        return !stack.isEmpty() && stack.is(HunterItems.SMOKING_PIPE.get());
    }
}
