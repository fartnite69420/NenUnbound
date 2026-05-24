package com.huntercraft.huntercraft.abilities.nenability;

import com.huntercraft.huntercraft.data.HunterPlayerData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

public class RyuAbility extends NenTechniqueAbility {
    public RyuAbility() {
        super("nen_ryu", "Ryu", "Passively harden your aura with protection-grade defense once mastered.", "textures/gui/abilities/ryu.png", 2);
    }

    @Override
    public boolean isActive(HunterPlayerData data) {
        return false;
    }

    @Override
    public int getNenDrainPerTick(HunterPlayerData data) {
        return 0;
    }

    @Override
    public float getIncomingDamageReduction(HunterPlayerData data) {
        return data.hasRyuUnlocked() && !data.isZetsuActive() && !data.isKoActive() ? 0.64F : 0.0F;
    }

    @Override
    public float getOutgoingDamageBonus(HunterPlayerData data) {
        return 0.0F;
    }

    @Override
    public boolean hasVisibleBodyAura(HunterPlayerData data) {
        return this.isActive(data);
    }

    @Override
    public boolean hasVisibleFistAura(HunterPlayerData data) {
        return this.isActive(data);
    }

    @Override
    protected void activate(ServerPlayer player, HunterPlayerData data, Vec3 direction) {
        data.setRyuActive(false);
    }

    @Override
    public void stop(ServerPlayer player, HunterPlayerData data) {
        data.setRyuActive(false);
    }
}
