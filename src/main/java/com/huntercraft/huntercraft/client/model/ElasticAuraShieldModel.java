package com.huntercraft.huntercraft.client.model;

import com.huntercraft.huntercraft.HunterCraftMod;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.Model;
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
import net.minecraft.client.renderer.RenderType;

public class ElasticAuraShieldModel extends Model {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation(HunterCraftMod.MODID, "elastic_aura_shield"), "main");
    private final ModelPart bone;

    public ElasticAuraShieldModel(ModelPart root) {
        super(RenderType::entityTranslucent);
        this.bone = root.getChild("bone");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        // Shield panel: 16x28 units, centered at origin, slight depth
        root.addOrReplaceChild("bone", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-8.0F, -14.0F, -0.5F, 16.0F, 28.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.ZERO);
        return LayerDefinition.create(mesh, 64, 64);
    }

    public void setupAnim(float ageInTicks) {
        float pulse = 1.0F + (Mth.sin(ageInTicks * 0.28F) * 0.05F);
        this.bone.xScale = pulse;
        this.bone.yScale = pulse;
        this.bone.zScale = 1.0F;
    }

    public void renderShield(PoseStack poseStack, VertexConsumer consumer, int packedLight, float red, float green, float blue, float alpha) {
        this.bone.render(poseStack, consumer, packedLight, 0, red, green, blue, alpha);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer consumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        this.renderShield(poseStack, consumer, packedLight, red, green, blue, alpha);
    }
}
