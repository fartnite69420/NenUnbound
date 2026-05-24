package com.huntercraft.huntercraft.entity.ability;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

public interface EntityAbility {
    String id();

    int cooldownTicks(Mob mob);

    int windupTicks(Mob mob);

    boolean canUse(Mob mob, LivingEntity target);

    default void onWindupStart(Mob mob, LivingEntity target) {
    }

    void use(Mob mob, LivingEntity target);
}
