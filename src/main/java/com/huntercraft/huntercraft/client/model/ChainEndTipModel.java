package com.huntercraft.huntercraft.client.model;

import com.huntercraft.huntercraft.HunterCraftMod;
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
import net.minecraft.world.entity.Entity;

public class ChainEndTipModel extends EntityModel<Entity> {
    public static final ModelLayerLocation DOWSING_LAYER = layer("dowsing_chain_end");
    public static final ModelLayerLocation CHAIN_JAIL_LAYER = layer("chain_jail_end");
    public static final ModelLayerLocation HOLY_LAYER = layer("holy_chain_end");
    public static final ModelLayerLocation JUDGMENT_LAYER = layer("judgement_chain_end");
    public static final ModelLayerLocation STEAL_LAYER = layer("steal_chain_end");

    private final ModelPart root;

    public ChainEndTipModel(ModelPart root) {
        this.root = root;
    }

    private static ModelLayerLocation layer(String name) {
        return new ModelLayerLocation(new ResourceLocation(HunterCraftMod.MODID, name), "main");
    }

    public static LayerDefinition createDowsingLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("bone", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-13.0F, -18.0F, -8.0F, 5.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 24.0F, 0.0F));
        return LayerDefinition.create(mesh, 32, 32);
    }

    public static LayerDefinition createHolyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition bone = root.addOrReplaceChild("bone", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-1.5F, -2.5F, -1.0F, 2.0F, 2.0F, 12.0F, new CubeDeformation(0.0F))
                        .texOffs(28, 0).addBox(5.0F, -2.0F, 4.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(28, 2).addBox(-7.0F, -2.0F, 4.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(28, 4).addBox(-1.0F, -2.0F, 10.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(28, 6).addBox(-1.0F, -2.0F, -1.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 28).addBox(-2.0F, -3.0F, 3.5F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-10.0F, 10.0F, 4.0F));
        bone.addOrReplaceChild("cube_r1", CubeListBuilder.create()
                        .texOffs(0, 14).addBox(-1.0F, -1.0F, -6.0F, 2.0F, 2.0F, 12.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-0.5F, -1.5F, 5.0F, 0.0F, -1.5708F, 0.0F));
        return LayerDefinition.create(mesh, 64, 64);
    }

    public static LayerDefinition createStealLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("bone", CubeListBuilder.create()
                        .texOffs(18, 14).addBox(-2.0F, -2.0F, 0.0F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(18, 12).addBox(-3.0F, -2.0F, 1.0F, 5.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 0).addBox(-2.0F, -3.0F, -9.0F, 3.0F, 3.0F, 9.0F, new CubeDeformation(0.0F))
                        .texOffs(18, 16).addBox(-1.0F, -2.0F, -10.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 12).addBox(-0.5F, -2.0F, -19.0F, 0.0F, 1.0F, 9.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-10.0F, 10.0F, -5.0F));
        return LayerDefinition.create(mesh, 32, 32);
    }

    public static LayerDefinition createChainJailLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition bone = root.addOrReplaceChild("bone", CubeListBuilder.create(), PartPose.offsetAndRotation(-10.5F, 8.2774F, -10.3486F, 0.0F, 0.0F, -1.5708F));
        bone.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(12, 16).addBox(-0.5F, -1.4864F, 0.75F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(23, 17).addBox(-0.5F, -0.5136F, -2.25F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(0, 13).addBox(-0.5F, -1.0136F, -1.25F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 3.079F, 2.1603F, -2.3126F, 0.0F, 0.0F));
        bone.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(16, 7).addBox(-0.5F, -0.0662F, -0.2116F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 4.2888F, 1.5601F, -2.7053F, 0.0F, 0.0F));
        bone.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(10, 0).addBox(-0.5F, -0.6064F, -1.8174F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 4.129F, -1.734F, 2.9671F, 0.0F, 0.0F));
        bone.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(8, 9).addBox(-0.5F, -0.4253F, -1.8307F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 2.9479F, -4.0207F, 2.3562F, 0.0F, 0.0F));
        bone.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(0, 9).addBox(-0.5F, -0.8125F, -1.8436F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.5351F, -4.8078F, 1.8326F, 0.0F, 0.0F));
        bone.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(8, 5).addBox(-0.5F, -0.322F, -1.2962F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -1.3554F, -5.0553F, 1.1345F, 0.0F, 0.0F));
        bone.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(0, 5).addBox(-0.5F, 0.9247F, -2.1257F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -4.9021F, -3.2257F, 0.5236F, 0.0F, 0.0F));
        bone.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(6, 16).addBox(-0.5F, 0.3247F, -0.8757F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -4.9021F, -1.2257F, 0.0436F, 0.0F, 0.0F));
        bone.addOrReplaceChild("cube_r9", CubeListBuilder.create().texOffs(16, 4).addBox(-0.5F, -0.1986F, -0.619F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -4.2788F, 0.4676F, -0.2182F, 0.0F, 0.0F));
        bone.addOrReplaceChild("cube_r10", CubeListBuilder.create().texOffs(12, 13).addBox(-0.5F, -0.8259F, 0.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -3.4515F, 1.4301F, -0.48F, 0.0F, 0.0F));
        bone.addOrReplaceChild("cube_r11", CubeListBuilder.create().texOffs(0, 0).addBox(-0.5F, -0.8503F, -1.4769F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -1.6271F, 3.8254F, -0.8727F, 0.0F, 0.0F));
        bone.addOrReplaceChild("cube_r12", CubeListBuilder.create().texOffs(6, 13).addBox(-0.5F, -0.5F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.2226F, 6.3486F, -0.3054F, 0.0F, 0.0F));
        return LayerDefinition.create(mesh, 32, 32);
    }

    public static LayerDefinition createJudgmentLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition bone = root.addOrReplaceChild("bone", CubeListBuilder.create().texOffs(24, 17).addBox(-1.0F, -1.0F, -0.5F, 2.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)).texOffs(24, 5).addBox(-0.5F, -0.5F, -5.5F, 1.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)).texOffs(32, 22).addBox(-0.5F, -1.0F, -3.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-10.5F, 8.5F, -4.5F));
        bone.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(0, 24).addBox(-8.5F, -0.5F, -2.0F, 9.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(24, 0).addBox(-2.5F, -0.5F, -3.0F, 3.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, 0.0F, -6.0F, 0.0F, -1.5708F, 0.0F));
        bone.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(0, 12).addBox(-0.1605F, -0.5F, -8.0F, 1.0F, 1.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.7395F, 0.0F, -8.4772F, 0.0F, -0.1745F, 0.0F));
        bone.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(0, 0).addBox(-0.8F, -0.5F, -9.5F, 1.0F, 1.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.0F, 0.0F, -7.0F, 0.0F, 0.1745F, 0.0F));
        bone.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(14, 32).addBox(-1.0F, -0.5F, -0.5F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0F, 0.0F, -6.0F, 0.0F, 1.2654F, 0.0F));
        bone.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(8, 32).addBox(0.0F, -0.5F, -0.5F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, 0.0F, -6.0F, 0.0F, -1.2654F, 0.0F));
        bone.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(20, 29).addBox(-0.3F, -0.5F, -1.4F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.0F, 0.0F, -2.0F, 0.0F, 1.7453F, 0.0F));
        bone.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(10, 27).addBox(-0.3F, -0.5F, -2.6F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.0F, 0.0F, -4.0F, 0.0F, -1.7453F, 0.0F));
        bone.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(0, 27).addBox(-0.7F, -0.5F, -1.4F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.0F, 0.0F, -2.0F, 0.0F, -1.7453F, 0.0F));
        bone.addOrReplaceChild("cube_r9", CubeListBuilder.create().texOffs(0, 32).addBox(-0.5F, -0.5F, -1.5F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.0F, 0.0F, -3.0F, 0.0F, -1.5708F, 0.0F));
        bone.addOrReplaceChild("cube_r10", CubeListBuilder.create().texOffs(30, 29).addBox(-0.5F, -0.5F, -1.5F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.0F, 0.0F, -3.0F, 0.0F, -1.5708F, 0.0F));
        bone.addOrReplaceChild("cube_r11", CubeListBuilder.create().texOffs(22, 24).addBox(-0.7F, -0.5F, -2.6F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.0F, 0.0F, -4.0F, 0.0F, 1.7453F, 0.0F));
        bone.addOrReplaceChild("cube_r12", CubeListBuilder.create().texOffs(24, 11).addBox(-0.5F, -0.5F, -2.5F, 1.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, -3.0F, 0.0F, -1.5708F, 0.0F));
        root.addOrReplaceChild("bb_main", CubeListBuilder.create().texOffs(0, 4).addBox(-11.0F, -17.0F, 2.0F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));
        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        this.root.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
