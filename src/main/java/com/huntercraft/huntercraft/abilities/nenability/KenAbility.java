package com.huntercraft.huntercraft.abilities.nenability;

import com.huntercraft.huntercraft.data.HunterPlayerData;
import com.huntercraft.huntercraft.entity.NenAuraEffectEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

public class KenAbility extends NenTechniqueAbility {
    public KenAbility() {
        super("nen_ken", "Ken", "Blend Ten and Ren into a balanced full-body aura state with steady offense and defense.", "textures/gui/abilities/ken.png", 2);
    }

    @Override
    public boolean isActive(HunterPlayerData data) {
        return data.isKenActive();
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
    public float getOutgoingDamageBonus(HunterPlayerData data) {
        return this.isActive(data) ? scaleByNenLevel(data, 6.0F, 30.0F) : 0.0F;
    }

    @Override
    public boolean hasVisibleBodyAura(HunterPlayerData data) {
        return false;
    }

    @Override
    public boolean hasVisibleFistAura(HunterPlayerData data) {
        return this.isActive(data);
    }

    @Override
    protected void activate(ServerPlayer player, HunterPlayerData data, Vec3 direction) {
        if (data.hasKenUnlocked()) {
            data.setKenActive(true);
            if (data.isKenActive() && player.level() instanceof ServerLevel serverLevel) {
                NenAuraEffectEntity.spawn(serverLevel, player, data, NenAuraEffectEntity.STYLE_KEN);
            }
        }
    }

    @Override
    public void stop(ServerPlayer player, HunterPlayerData data) {
        data.setKenActive(false);
    }
}
