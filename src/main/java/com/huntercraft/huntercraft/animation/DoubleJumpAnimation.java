package com.huntercraft.huntercraft.animation;

import net.minecraft.client.model.PlayerModel;

public class DoubleJumpAnimation implements HunterAnimation {
    @Override
    public void apply(PlayerModel<?> model, float progress) {
        model.body.xRot -= 0.26F * progress;
        model.rightArm.xRot = -2.65F + (0.18F * progress);
        model.leftArm.xRot = -2.65F + (0.18F * progress);
        model.rightArm.yRot = -0.42F;
        model.leftArm.yRot = 0.42F;
        model.rightLeg.xRot = 0.95F * progress;
        model.leftLeg.xRot = 0.95F * progress;
    }
}
