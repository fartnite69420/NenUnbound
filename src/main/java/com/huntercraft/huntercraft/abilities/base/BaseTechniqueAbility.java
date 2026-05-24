package com.huntercraft.huntercraft.abilities.base;

import com.huntercraft.huntercraft.ability.HunterAbility;
import com.huntercraft.huntercraft.data.HunterPlayerData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

public abstract class BaseTechniqueAbility extends HunterAbility {
    protected BaseTechniqueAbility(String id, String displayName, String description, String iconPath) {
        super(id, displayName, description, iconPath, true);
    }

    public abstract int getMaxCooldownTicks();

    public int getCurrentCooldown(HunterPlayerData data) {
        return data.getAbilityCooldown(this.id());
    }

    protected void startCooldown(HunterPlayerData data, int cooldownTicks) {
        data.setAbilityCooldown(this.id(), cooldownTicks);
    }

    public int getStaminaCost(HunterPlayerData data) {
        int baseCost = switch (this.id()) {
            case "dash" -> 45;
            case "double_jump" -> 65;
            case "guard" -> 25;
            default -> 35;
        };
        return data.getReducedStaminaCost(baseCost);
    }

    public abstract void use(ServerPlayer player, HunterPlayerData data, Vec3 direction);

    public boolean isContinuous() {
        return false;
    }

    public boolean isActive(HunterPlayerData data) {
        return false;
    }

    public boolean isCharging(HunterPlayerData data) {
        return false;
    }

    public int getActiveTicks(HunterPlayerData data) {
        return 0;
    }

    public int getChargeTicks(HunterPlayerData data) {
        return 0;
    }

    public void stop(ServerPlayer player, HunterPlayerData data) {
    }
}
