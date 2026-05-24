package com.huntercraft.huntercraft.animation;

import net.minecraft.client.model.PlayerModel;

public interface HunterAnimation {
    void apply(PlayerModel<?> model, float progress);
}
