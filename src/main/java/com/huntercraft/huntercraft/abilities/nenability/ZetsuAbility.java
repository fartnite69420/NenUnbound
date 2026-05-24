package com.huntercraft.huntercraft.abilities.nenability;

import com.huntercraft.huntercraft.data.HunterPlayerData;
import com.huntercraft.huntercraft.effect.HunterMobEffects;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.phys.Vec3;

public class ZetsuAbility extends NenTechniqueAbility {
    public static final int EFFECT_REFRESH_TICKS = 40;

    public ZetsuAbility() {
        super("nen_zetsu", "Zetsu", "Shut your aura off completely, halting nen usage and hiding from En without any drain.", "textures/gui/abilities/zetsu.png", 1);
    }

    @Override
    protected boolean canActivateWithoutNen() {
        return true;
    }

    @Override
    public boolean isActive(HunterPlayerData data) {
        return data.isZetsuActive();
    }

    @Override
    public int getNenRegenPerTick(HunterPlayerData data) {
        return this.isActive(data) ? 7 : 0;
    }

    @Override
    protected void activate(ServerPlayer player, HunterPlayerData data, Vec3 direction) {
        if (data.hasZetsuUnlocked()) {
            data.setZetsuActive(true);
            if (data.isZetsuActive()) {
                player.addEffect(new MobEffectInstance(HunterMobEffects.ZETSU.get(), EFFECT_REFRESH_TICKS, 0, false, false, true));
            }
        }
    }

    @Override
    public void stop(ServerPlayer player, HunterPlayerData data) {
        data.setZetsuActive(false);
        player.removeEffect(HunterMobEffects.ZETSU.get());
        if (data.isZetsuForcedInvisibility()) {
            data.setZetsuForcedInvisibility(false);
        }
    }
}
