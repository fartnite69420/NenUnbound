package com.huntercraft.huntercraft.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.UUID;

public class StunnedEffect extends MobEffect {
    private static final UUID STUN_SPEED_MODIFIER = UUID.fromString("f70468b1-842d-4d5d-9f3f-d6067ca74337");

    public StunnedEffect() {
        this(0x8AA7C8);
    }

    protected StunnedEffect(int color) {
        super(MobEffectCategory.HARMFUL, color);
        this.addAttributeModifier(Attributes.MOVEMENT_SPEED, STUN_SPEED_MODIFIER.toString(), -1.0D, AttributeModifier.Operation.MULTIPLY_TOTAL);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        entity.stopUsingItem();
        entity.setSprinting(false);
        entity.setJumping(false);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }
}
