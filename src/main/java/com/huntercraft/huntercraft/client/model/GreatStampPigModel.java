package com.huntercraft.huntercraft.client.model;

import com.huntercraft.huntercraft.HunterCraftMod;
import com.huntercraft.huntercraft.entity.GreatStampPigEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class GreatStampPigModel<T extends GreatStampPigEntity> extends EntityModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation(HunterCraftMod.MODID, "great_stamp_pig"), "main");

    private final ModelPart upperLeftLeg;
    private final ModelPart lowerRightLeg;
    private final ModelPart upperRightLeg;
    private final ModelPart lowerLeftLeg;
    private final ModelPart body;

    public GreatStampPigModel(ModelPart root) {
        this.upperLeftLeg = root.getChild("ULLEG");
        this.lowerRightLeg = root.getChild("LRLEG");
        this.upperRightLeg = root.getChild("URLEG");
        this.lowerLeftLeg = root.getChild("LLLEG");
        this.body = root.getChild("bb_main");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild("ULLEG", CubeListBuilder.create().texOffs(56, 55).addBox(-2.0F, -4.0F, -2.0F, 4.0F, 5.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(12, 65).addBox(-1.5F, 1.0F, -1.5F, 3.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(5.0F, 18.0F, -10.0F));

        partdefinition.addOrReplaceChild("LRLEG", CubeListBuilder.create().texOffs(0, 71).addBox(-1.5F, 1.0F, -1.5F, 3.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(60, 64).addBox(-2.0F, -4.0F, -2.0F, 4.0F, 5.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(-9.0F, 18.0F, 10.0F));

        partdefinition.addOrReplaceChild("URLEG", CubeListBuilder.create().texOffs(12, 69).addBox(-1.5F, 1.0F, -1.5F, 3.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(28, 63).addBox(-2.0F, -4.0F, -2.0F, 4.0F, 5.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(-9.0F, 18.0F, -10.0F));

        partdefinition.addOrReplaceChild("LLLEG", CubeListBuilder.create().texOffs(24, 72).addBox(-1.5F, 1.0F, -1.5F, 3.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(44, 64).addBox(-2.0F, -4.0F, -2.0F, 4.0F, 5.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(5.0F, 18.0F, 10.0F));

        PartDefinition body = partdefinition.addOrReplaceChild("bb_main", CubeListBuilder.create().texOffs(0, 0).addBox(-12.0F, -29.0F, -14.0F, 20.0F, 20.0F, 28.0F, new CubeDeformation(0.0F))
                .texOffs(28, 48).addBox(-6.0F, -22.0F, -20.0F, 8.0F, 9.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

        body.addOrReplaceChild("ear_r1", CubeListBuilder.create().texOffs(72, 59).addBox(-2.0F, -2.0F, -2.0F, 3.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-8.0F, -26.0F, -14.0F, 0.2467F, 0.0886F, -0.3381F));
        body.addOrReplaceChild("nosetip_r1", CubeListBuilder.create().texOffs(0, 48).addBox(-5.0F, -11.0F, -3.0F, 10.0F, 13.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.0F, -14.0F, -19.0F, 0.1745F, 0.0F, 0.0F));
        body.addOrReplaceChild("tail_r1", CubeListBuilder.create().texOffs(0, 65).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.0F, -17.0F, 14.0F, -0.2182F, 0.0F, 0.0F));
        body.addOrReplaceChild("ear_r2", CubeListBuilder.create().texOffs(72, 55).addBox(-1.0F, -2.0F, -2.0F, 3.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.0F, -26.0F, -14.0F, 0.2467F, -0.0886F, 0.3381F));
        body.addOrReplaceChild("nosetip_r2", CubeListBuilder.create().texOffs(56, 48).addBox(-3.0F, -1.0F, -3.0F, 6.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.0F, -26.8025F, -21.2574F, 0.1745F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        float moveScale = entity.isRunningAtPlayer() ? 1.55F : 1.0F;
        float moveSpeed = entity.isRunningAtPlayer() ? 1.35F : 0.85F;
        float walk = limbSwingAmount * moveScale;
        float stomp = entity.getStompProgress(ageInTicks - entity.tickCount);
        float bodyBob = Mth.cos(limbSwing * moveSpeed * 1.5F) * walk * 0.18F;
        float walkAngle = Mth.sin(limbSwing * moveSpeed) * (15.0F * Mth.DEG_TO_RAD) * walk;

        this.upperLeftLeg.xRot = walkAngle - (stomp * 1.35F);
        this.lowerRightLeg.xRot = walkAngle;
        this.upperRightLeg.xRot = -walkAngle - (stomp * 0.45F);
        this.lowerLeftLeg.xRot = -walkAngle;

        this.body.xRot = (headPitch * Mth.DEG_TO_RAD * 0.15F) - (stomp * 0.2F);
        this.body.y = 24.0F + bodyBob + (stomp * 1.6F);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        this.upperLeftLeg.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        this.lowerRightLeg.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        this.upperRightLeg.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        this.lowerLeftLeg.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        this.body.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
