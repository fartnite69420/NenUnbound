package com.huntercraft.huntercraft.abilities.base;

import com.huntercraft.huntercraft.animation.AnimationType;
import com.huntercraft.huntercraft.abilities.HunterAbilities;
import com.huntercraft.huntercraft.data.HunterPlayerData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

public class DashAbility extends BaseTechniqueAbility {
    public DashAbility() {
        super("dash", "Dash", "Omni-directional movement burst.", "textures/gui/abilities/dash.png");
    }

    @Override
    public int getMaxCooldownTicks() {
        return 40;
    }

    @Override
    public void use(ServerPlayer player, HunterPlayerData data, Vec3 direction) {
        if (this.getCurrentCooldown(data) > 0) {
            return;
        }
        Vec3 resolvedDirection = new Vec3(direction.x, 0.0D, direction.z);
        if (resolvedDirection.lengthSqr() < 0.001D) {
            Vec3 look = player.getLookAngle();
            resolvedDirection = new Vec3(look.x, 0.0D, look.z);
        }
        resolvedDirection = resolvedDirection.normalize();
        float movementMultiplier = HunterAbilities.FLOW_STEP.getMovementAbilityMultiplier(data);
        movementMultiplier *= HunterAbilities.FLASH_ACCEL.isUnlocked(data)
                ? HunterAbilities.FLASH_ACCEL.getMovementAbilityMultiplier(data)
                : HunterAbilities.STEP_DRIVE.getMovementAbilityMultiplier(data);
        player.setDeltaMovement(resolvedDirection.scale(1.45D * movementMultiplier));
        player.hurtMarked = true;
        player.fallDistance = 0.0F;
        data.setAirLaunchFallProtection(true);
        data.setDashIFrameTicks(10);
        data.triggerAnimation(AnimationType.DASH);
        this.startCooldown(data, this.getMaxCooldownTicks());
    }
}
