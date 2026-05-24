package com.huntercraft.huntercraft.abilities.nenability;

import com.huntercraft.huntercraft.data.HunterPlayerData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

public class KoAbility extends NenTechniqueAbility {
    public KoAbility() {
        super("nen_ko", "Ko", "Pour your aura into one hand for a massive Ren-like damage boost while sacrificing Ryu's passive defense.", "textures/gui/abilities/ko.png", 2);
    }

    @Override
    public boolean isActive(HunterPlayerData data) {
        return data.isKoActive();
    }

    @Override
    public int getNenDrainPerTick(HunterPlayerData data) {
        return this.isActive(data) ? SUSTAINED_DRAIN_PER_TICK : 0;
    }

    @Override
    public float getOutgoingDamageBonus(HunterPlayerData data) {
        return this.isActive(data) ? scaleByNenLevel(data, 25.0F, 100.0F) : 0.0F;
    }

    @Override
    public float getKoDamageMultiplier(HunterPlayerData data, ServerPlayer player) {
        return 1.0F;
    }

    @Override
    public void onKoStrikeResolved(HunterPlayerData data) {
    }

    @Override
    public boolean hasVisibleFistAura(HunterPlayerData data) {
        return this.isActive(data);
    }

    @Override
    protected void activate(ServerPlayer player, HunterPlayerData data, Vec3 direction) {
        if (data.hasKoUnlocked()) {
            data.setKoActive(true);
        }
    }

    @Override
    public void stop(ServerPlayer player, HunterPlayerData data) {
        data.setKoActive(false);
    }
}
