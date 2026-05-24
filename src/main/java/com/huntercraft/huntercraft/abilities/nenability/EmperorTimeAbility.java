package com.huntercraft.huntercraft.abilities.nenability;

import com.huntercraft.huntercraft.data.HunterPlayerData;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

public class EmperorTimeAbility extends NenTechniqueAbility {
    public EmperorTimeAbility() {
        super("emperor_time", "Emperor Time", "Scarlet Eyes-only toggle. Drains Nen over time and enables the restricted chain abilities.", "textures/gui/abilities/emperor_time.png", 1);
    }

    @Override
    public boolean isUnlocked(HunterPlayerData data) {
        return data.hasScarletEyesTrait();
    }

    @Override
    public boolean isActive(HunterPlayerData data) {
        return data.isEmperorTimeActive();
    }

    @Override
    public int getNenDrainPerTick(HunterPlayerData data) {
        return this.isActive(data) ? 1 : 0;
    }

    @Override
    public Component getUseFailureMessage(ServerPlayer player, HunterPlayerData data) {
        if (!data.hasScarletEyesTrait()) {
            return Component.literal("You need the Scarlet Eyes trait to use Emperor Time.");
        }
        return super.getUseFailureMessage(player, data);
    }

    @Override
    protected void activate(ServerPlayer player, HunterPlayerData data, Vec3 direction) {
        data.setEmperorTimeActive(true);
    }

    @Override
    public void stop(ServerPlayer player, HunterPlayerData data) {
        data.setEmperorTimeActive(false);
    }
}
