package com.huntercraft.huntercraft.damage;

import com.huntercraft.huntercraft.HunterCraftMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageType;

public final class HunterDamageTypes {
    public static final ResourceKey<DamageType> WEAPON = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(HunterCraftMod.MODID, "weapon"));
    public static final ResourceKey<DamageType> PHYSICAL = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(HunterCraftMod.MODID, "physical"));
    public static final ResourceKey<DamageType> SHARP = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(HunterCraftMod.MODID, "sharp"));
    public static final ResourceKey<DamageType> BLUNT = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(HunterCraftMod.MODID, "blunt"));
    public static final ResourceKey<DamageType> NEN = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(HunterCraftMod.MODID, "nen"));
    public static final ResourceKey<DamageType> UNAVOIDABLE_WEAPON = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(HunterCraftMod.MODID, "unavoidable_weapon"));

    private HunterDamageTypes() {
    }
}
