package com.huntercraft.huntercraft.client.model;

import com.huntercraft.huntercraft.HunterCraftMod;
import com.huntercraft.huntercraft.entity.SmokeyChainProjectileEntity;
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

public class SharedChainProjectileModel<T extends SmokeyChainProjectileEntity> extends EntityModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(new ResourceLocation(HunterCraftMod.MODID, "chainprojectile"), "main");
    public static final float SEGMENT_SPACING = 0.16F;

    private final ModelPart chainLinkStraight;
    private final ModelPart chainLinkTwist;

    public SharedChainProjectileModel(ModelPart root) {
        this.chainLinkStraight = root.getChild("chain_link_straight");
        this.chainLinkTwist = root.getChild("chain_link_twist");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild("chain_link_straight",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-0.5F, -1.5F, -1.5F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 4).addBox(-0.5F, -1.5F, 1.5F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(8, 0).addBox(-0.5F, -1.5F, -0.5F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 8).addBox(-0.5F, 0.5F, -0.5F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.ZERO);

        partdefinition.addOrReplaceChild("chain_link_twist",
                CubeListBuilder.create()
                        .texOffs(4, 0).addBox(-1.5F, -0.5F, -1.5F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(4, 4).addBox(-1.5F, -0.5F, 1.5F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(4, 8).addBox(-1.5F, -0.5F, -0.5F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                        .texOffs(8, 3).addBox(0.5F, -0.5F, -0.5F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.ZERO);

        return LayerDefinition.create(meshdefinition, 16, 16);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
    }

    public void renderSegment(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int segmentIndex) {
        if ((segmentIndex & 1) == 0) {
            this.chainLinkStraight.render(poseStack, vertexConsumer, packedLight, packedOverlay);
        } else {
            this.chainLinkTwist.render(poseStack, vertexConsumer, packedLight, packedOverlay);
        }
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        this.chainLinkStraight.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        this.chainLinkTwist.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
