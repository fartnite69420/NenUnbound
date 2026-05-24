package com.huntercraft.huntercraft.client.render;

import com.huntercraft.huntercraft.HunterCraftMod;
import com.huntercraft.huntercraft.entity.DowsingChainEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class DowsingChainModel extends GeoModel<DowsingChainEntity> {
    private static final ResourceLocation MODEL = new ResourceLocation(HunterCraftMod.MODID, "geo/dowsing_chain_swing.geo.json");
    private static final ResourceLocation ANIMATIONS = new ResourceLocation(HunterCraftMod.MODID, "animations/dowsing_chain_swing.animation.json");
    private static final ResourceLocation TEXTURE = new ResourceLocation(HunterCraftMod.MODID, "textures/entity/dowsing_chain_projectile_texture.png");

    @Override
    public ResourceLocation getModelResource(DowsingChainEntity animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(DowsingChainEntity animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(DowsingChainEntity animatable) {
        return ANIMATIONS;
    }
}
