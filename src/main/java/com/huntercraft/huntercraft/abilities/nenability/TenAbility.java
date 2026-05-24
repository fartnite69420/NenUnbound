package com.huntercraft.huntercraft.abilities.nenability;

import com.huntercraft.huntercraft.data.HunterPlayerData;
import com.huntercraft.huntercraft.entity.NenAuraEffectEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

public class TenAbility extends NenTechniqueAbility {
    public TenAbility() {
        super("nen_ten", "Ten", "Release a steady veil of aura around your body to reduce incoming damage while draining nen.", "textures/gui/abilities/ten.png", 1);
    }

    @Override
    public boolean isActive(HunterPlayerData data) {
        return data.isTenActive();
    }

    @Override
    public int getNenDrainPerTick(HunterPlayerData data) {
        return this.isActive(data) ? SUSTAINED_DRAIN_PER_TICK : 0;
    }

    @Override
    public float getIncomingDamageReduction(HunterPlayerData data) {
        return 0.0F;
    }

    @Override
    public boolean hasVisibleBodyAura(HunterPlayerData data) {
        return false;
    }

    @Override
    protected void activate(ServerPlayer player, HunterPlayerData data, Vec3 direction) {
        if (data.hasTenUnlocked()) {
            data.setTenActive(true);
            if (data.isTenActive() && player.level() instanceof ServerLevel serverLevel) {
                NenAuraEffectEntity.spawn(serverLevel, player, data, NenAuraEffectEntity.STYLE_TEN);
            }
        }
    }

    @Override
    public void stop(ServerPlayer player, HunterPlayerData data) {
        data.setTenActive(false);
    }
}
