package com.huntercraft.huntercraft.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public class DowsingChainedEffect extends MobEffect {
    public DowsingChainedEffect() {
        super(MobEffectCategory.HARMFUL, 0xBFC7D5);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return false;
    }
}
