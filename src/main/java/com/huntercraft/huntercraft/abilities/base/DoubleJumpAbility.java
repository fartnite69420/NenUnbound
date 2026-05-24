package com.huntercraft.huntercraft.abilities.base;

import com.huntercraft.huntercraft.animation.AnimationType;
import com.huntercraft.huntercraft.abilities.HunterAbilities;
import com.huntercraft.huntercraft.data.HunterPlayerData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

public class DoubleJumpAbility extends BaseTechniqueAbility {
    public DoubleJumpAbility() {
        super("double_jump", "Double Jump", "Double tap space to launch upward again.", "textures/gui/abilities/double_jump.png");
    }

    @Override
    public int getMaxCooldownTicks() {
        return 60;
    }

    @Override
    public void use(ServerPlayer player, HunterPlayerData data, Vec3 direction) {
        if (player.onGround() || data.getAirJumpsUsed() >= 1 || this.getCurrentCooldown(data) > 0) {
            return;
        }

        Vec3 motion = player.getDeltaMovement();
        float movementMultiplier = HunterAbilities.FLOW_STEP.getMovementAbilityMultiplier(data);
        movementMultiplier *= HunterAbilities.FLASH_ACCEL.isUnlocked(data)
                ? HunterAbilities.FLASH_ACCEL.getMovementAbilityMultiplier(data)
                : HunterAbilities.STEP_DRIVE.getMovementAbilityMultiplier(data);
        Vec3 adjustedMotion;
        double verticalBoost;
        if (player.onGround()) {
            adjustedMotion = motion.scale(2.0D * movementMultiplier);
            verticalBoost = adjustedMotion.y;
        } else if (player.isInWaterOrBubble()) {
            adjustedMotion = motion;
            verticalBoost = 1.86D * movementMultiplier;
        } else {
            adjustedMotion = motion.scale(1.5D * movementMultiplier);
            verticalBoost = 1.25D * movementMultiplier;
        }

        Vec3 horizontalDirection = new Vec3(direction.x, 0.0D, direction.z);
        if (horizontalDirection.lengthSqr() < 1.0E-4D) {
            Vec3 look = player.getLookAngle();
            horizontalDirection = new Vec3(look.x, 0.0D, look.z);
        }
        if (horizontalDirection.lengthSqr() > 1.0E-4D) {
            Vec3 horizontalPush = horizontalDirection.normalize().scale(0.78D * movementMultiplier);
            adjustedMotion = new Vec3(
                    Math.abs(adjustedMotion.x) > 0.12D ? adjustedMotion.x : horizontalPush.x,
                    adjustedMotion.y,
                    Math.abs(adjustedMotion.z) > 0.12D ? adjustedMotion.z : horizontalPush.z
            );
        }
        if (player.zza < 0.0F) {
            adjustedMotion = adjustedMotion.multiply(-1.0D, 1.0D, -1.0D);
        }

        player.setDeltaMovement(adjustedMotion.x, verticalBoost, adjustedMotion.z);
        player.hurtMarked = true;
        player.fallDistance = 0.0F;
        data.setAirJumpsUsed(1);
        data.setAirLaunchFallProtection(true);
        data.triggerAnimation(AnimationType.DOUBLE_JUMP);
        this.startCooldown(data, this.getMaxCooldownTicks());
    }
}
