package com.huntercraft.huntercraft.animation;

import net.minecraft.client.model.PlayerModel;
import net.minecraft.util.Mth;

public class DashAnimation implements HunterAnimation {
    @Override
    public void apply(PlayerModel<?> model, float progress) {
        model.head.xRot += (15.0F * Mth.DEG_TO_RAD) * progress;
        model.head.z -= 1.0F * progress;

        model.leftArm.xRot = (97.5F * Mth.DEG_TO_RAD) * progress;
        model.leftArm.y += 3.0F * progress;
        model.leftArm.z += 5.0F * progress;

        model.rightArm.xRot = (-77.5F * Mth.DEG_TO_RAD) * progress;
        model.rightArm.y += 2.0F * progress;
        model.rightArm.z -= 5.0F * progress;

        model.leftLeg.xRot = (-32.5F * Mth.DEG_TO_RAD) * progress;
        model.leftLeg.y += 1.0F * progress;
        model.leftLeg.z -= 2.0F * progress;

        model.rightLeg.xRot = (32.5F * Mth.DEG_TO_RAD) * progress;
        model.rightLeg.y += 1.0F * progress;
        model.rightLeg.z += 2.0F * progress;

        model.body.xRot += (-22.5F * Mth.DEG_TO_RAD) * progress;
    }
}
