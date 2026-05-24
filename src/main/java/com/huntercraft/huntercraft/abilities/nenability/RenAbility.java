package com.huntercraft.huntercraft.abilities.nenability;

import com.huntercraft.huntercraft.data.HunterPlayerData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

public class RenAbility extends NenTechniqueAbility {
    public RenAbility() {
        super("nen_ren", "Ren", "Force more aura through your body and fists to increase damage output at a steady nen cost.", "textures/gui/abilities/ren.png", 1);
    }

    @Override
    public boolean isActive(HunterPlayerData data) {
        return data.isRenActive();
    }

    @Override
    public int getNenDrainPerTick(HunterPlayerData data) {
        return this.isActive(data) ? SUSTAINED_DRAIN_PER_TICK : 0;
    }

    @Override
    public float getOutgoingDamageBonus(HunterPlayerData data) {
        return this.isActive(data) ? scaleByNenLevel(data, 10.0F, 50.0F) : 0.0F;
    }

    @Override
    public boolean hasVisibleFistAura(HunterPlayerData data) {
        return this.isActive(data);
    }

    @Override
    protected void activate(ServerPlayer player, HunterPlayerData data, Vec3 direction) {
        if (data.hasRenUnlocked()) {
            data.setRenActive(true);
        }
    }

    @Override
    public void stop(ServerPlayer player, HunterPlayerData data) {
        data.setRenActive(false);
    }
}
