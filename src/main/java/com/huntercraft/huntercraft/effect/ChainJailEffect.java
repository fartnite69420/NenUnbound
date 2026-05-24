package com.huntercraft.huntercraft.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public class ChainJailEffect extends MobEffect {
    public ChainJailEffect() {
        super(MobEffectCategory.HARMFUL, 0x3A3A5C);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        entity.stopUsingItem();
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }
}
