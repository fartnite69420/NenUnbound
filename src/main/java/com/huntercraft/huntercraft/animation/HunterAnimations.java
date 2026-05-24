package com.huntercraft.huntercraft.animation;

import net.minecraft.client.model.PlayerModel;

import java.util.EnumMap;
import java.util.Map;

public final class HunterAnimations {
    private static final Map<AnimationType, HunterAnimation> ANIMATIONS = new EnumMap<>(AnimationType.class);

    static {
        ANIMATIONS.put(AnimationType.DASH, new DashAnimation());
        ANIMATIONS.put(AnimationType.DOUBLE_JUMP, new DoubleJumpAnimation());
        ANIMATIONS.put(AnimationType.GUARD, new GuardAnimation());
    }

    private HunterAnimations() {
    }

    public static void apply(PlayerModel<?> model, AnimationType type, float progress) {
        HunterAnimation animation = ANIMATIONS.get(type);
        if (animation != null) {
            animation.apply(model, progress);
        }
    }
}
