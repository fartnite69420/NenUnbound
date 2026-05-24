package com.huntercraft.huntercraft.abilities.nenability;

import com.huntercraft.huntercraft.data.HunterPlayerData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

public class EnAbility extends NenTechniqueAbility {
    private static final float BASE_RADIUS = 15.0F;
    public EnAbility() {
        super("nen_en", "En", "Expand a spherical aura field to sense entities in range unless they are hidden by Zetsu.", "textures/gui/abilities/en.png", 2);
    }

    @Override
    public boolean isActive(HunterPlayerData data) {
        return data.isEnActive();
    }

    @Override
    public int getNenDrainPerTick(HunterPlayerData data) {
        return this.isActive(data) ? 2 : 0;
    }

    public float getRadius(HunterPlayerData data) {
        int levelAboveUnlock = Math.max(0, data.getNenLevel() - 2);
        return (float) (BASE_RADIUS * Math.pow(2.0D, levelAboveUnlock / 3.2D));
    }

    @Override
    protected void activate(ServerPlayer player, HunterPlayerData data, Vec3 direction) {
        if (data.hasEnUnlocked()) {
            data.setEnActive(true);
        }
    }

    @Override
    public void stop(ServerPlayer player, HunterPlayerData data) {
        data.setEnActive(false);
    }
}
