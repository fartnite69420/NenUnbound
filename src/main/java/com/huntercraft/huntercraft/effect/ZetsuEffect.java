package com.huntercraft.huntercraft.effect;

import com.huntercraft.huntercraft.data.HunterPlayerData;
import com.huntercraft.huntercraft.data.HunterPlayerDataProvider;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class ZetsuEffect extends MobEffect {
    public ZetsuEffect() {
        super(MobEffectCategory.HARMFUL, 0x2D3140);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        entity.stopUsingItem();
        if (entity instanceof Player player) {
            HunterPlayerData data = player.getCapability(HunterPlayerDataProvider.CAPABILITY).orElse(null);
            if (data != null) {
                data.disableNenDrains();
            }
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }
}
