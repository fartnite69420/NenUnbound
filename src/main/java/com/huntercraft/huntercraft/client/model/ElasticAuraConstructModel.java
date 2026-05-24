package com.huntercraft.huntercraft.client.model;

import com.huntercraft.huntercraft.HunterCraftMod;
import com.huntercraft.huntercraft.entity.ElasticAuraConstructEntity;
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

public class ElasticAuraConstructModel<T extends ElasticAuraConstructEntity> extends EntityModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation(HunterCraftMod.MODID, "elastic_aura_construct"), "main");
    private final ModelPart middle;
    private final ModelPart edge;

    public ElasticAuraConstructModel(ModelPart root) {
        this.middle = root.getChild("middle");
        this.edge = root.getChild("edge");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("middle", CubeListBuilder.create().texOffs(0, 0).addBox(-3.0F, -3.0F, -3.0F, 6.0F, 3.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 19.0F, 0.0F));
        root.addOrReplaceChild("edge", CubeListBuilder.create().texOffs(0, 9).addBox(-2.5F, -4.0F, -2.5F, 5.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 19.0F, 0.0F));
        return LayerDefinition.create(mesh, 32, 32);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        float pulse = 1.0F + (Mth.sin((entity.tickCount + ageInTicks) * 0.18F) * 0.06F);
        this.middle.xScale = pulse;
        this.middle.yScale = pulse;
        this.middle.zScale = pulse;
        this.edge.xScale = pulse * 1.04F;
        this.edge.yScale = pulse * 1.04F;
        this.edge.zScale = pulse * 1.04F;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        this.middle.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        this.edge.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
