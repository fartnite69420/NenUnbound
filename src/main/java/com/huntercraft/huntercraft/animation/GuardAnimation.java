package com.huntercraft.huntercraft.animation;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.util.Mth;

public class GuardAnimation implements HunterAnimation {
    @Override
    public void apply(PlayerModel<?> model, float progress) {
        model.rightArmPose = HumanoidModel.ArmPose.EMPTY;
        model.leftArmPose = HumanoidModel.ArmPose.EMPTY;
        model.body.xRot += 0.12F;
        model.head.xRot = Mth.clamp(model.head.xRot - 0.1F, -0.9F, 0.7F);
        model.rightArm.xRot = -2.3F;
        model.leftArm.xRot = -2.3F;
        model.rightArm.yRot = -0.38F;
        model.leftArm.yRot = 0.38F;
        model.rightArm.zRot = 0.85F;
        model.leftArm.zRot = -0.85F;
    }
}
