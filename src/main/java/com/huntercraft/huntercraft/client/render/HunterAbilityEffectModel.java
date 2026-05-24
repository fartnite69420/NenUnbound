package com.huntercraft.huntercraft.client.render;

import com.huntercraft.huntercraft.HunterCraftMod;
import com.huntercraft.huntercraft.animation.AnimationType;
import com.huntercraft.huntercraft.entity.HunterAbilityEffectEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class HunterAbilityEffectModel extends GeoModel<HunterAbilityEffectEntity> {
    private static final ResourceLocation MODEL = new ResourceLocation(HunterCraftMod.MODID, "geo/hunter_ability_effect.geo.json");
    private static final ResourceLocation ANIMATIONS = new ResourceLocation(HunterCraftMod.MODID, "animations/hunter_ability_effect.animation.json");
    private static final ResourceLocation DASH_TEXTURE = new ResourceLocation(HunterCraftMod.MODID, "textures/gui/abilities/dash.png");
    private static final ResourceLocation DOUBLE_JUMP_TEXTURE = new ResourceLocation(HunterCraftMod.MODID, "textures/gui/abilities/double_jump.png");
    private static final ResourceLocation GUARD_TEXTURE = new ResourceLocation(HunterCraftMod.MODID, "textures/gui/abilities/guard.png");

    @Override
    public ResourceLocation getModelResource(HunterAbilityEffectEntity animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(HunterAbilityEffectEntity animatable) {
        return switch (animatable.getEffectType()) {
            case DASH -> DASH_TEXTURE;
            case DOUBLE_JUMP -> DOUBLE_JUMP_TEXTURE;
            case GUARD -> GUARD_TEXTURE;
            default -> DASH_TEXTURE;
        };
    }

    @Override
    public ResourceLocation getAnimationResource(HunterAbilityEffectEntity animatable) {
        return ANIMATIONS;
    }
}
