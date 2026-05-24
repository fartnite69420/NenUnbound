package com.huntercraft.huntercraft.damage;

import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public final class HunterDamageSources {
    private HunterDamageSources() {
    }

    public static DamageSource weapon(Level level, Entity attacker) {
        return weapon(level, attacker, attacker);
    }

    public static DamageSource weapon(Level level, Entity directEntity, Entity attacker) {
        return new DamageSource(getType(level.registryAccess(), HunterDamageTypes.WEAPON), directEntity, attacker);
    }

    public static DamageSource unavoidableWeapon(Level level, Entity attacker) {
        return unavoidableWeapon(level, attacker, attacker);
    }

    public static DamageSource unavoidableWeapon(Level level, Entity directEntity, Entity attacker) {
        return new DamageSource(getType(level.registryAccess(), HunterDamageTypes.UNAVOIDABLE_WEAPON), directEntity, attacker);
    }

    public static DamageSource physical(Level level, Entity attacker) {
        return physical(level, attacker, attacker);
    }

    public static DamageSource physical(Level level, Entity directEntity, Entity attacker) {
        return new DamageSource(getType(level.registryAccess(), HunterDamageTypes.PHYSICAL), directEntity, attacker);
    }

    public static DamageSource sharp(Level level, Entity attacker) {
        return sharp(level, attacker, attacker);
    }

    public static DamageSource sharp(Level level, Entity directEntity, Entity attacker) {
        return new DamageSource(getType(level.registryAccess(), HunterDamageTypes.SHARP), directEntity, attacker);
    }

    public static DamageSource blunt(Level level, Entity attacker) {
        return blunt(level, attacker, attacker);
    }

    public static DamageSource blunt(Level level, Entity directEntity, Entity attacker) {
        return new DamageSource(getType(level.registryAccess(), HunterDamageTypes.BLUNT), directEntity, attacker);
    }

    public static DamageSource nen(Level level, Entity attacker) {
        return nen(level, attacker, attacker);
    }

    public static DamageSource nen(Level level, Entity directEntity, Entity attacker) {
        return new DamageSource(getType(level.registryAccess(), HunterDamageTypes.NEN), directEntity, attacker);
    }

    private static Holder<DamageType> getType(RegistryAccess access, net.minecraft.resources.ResourceKey<DamageType> key) {
        return access.registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(key);
    }
}
