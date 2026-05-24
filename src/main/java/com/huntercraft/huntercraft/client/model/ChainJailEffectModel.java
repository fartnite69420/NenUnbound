package com.huntercraft.huntercraft.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public class ChainJailEffectModel extends EntityModel<Entity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation("huntercraft", "chain_jail_effect"), "main");
	private final ModelPart bone;
	private final ModelPart bone2;
	private final ModelPart bone3;
	private final ModelPart bone4;
	private final ModelPart bone5;
	private final ModelPart bone6;
	private final ModelPart bone7;
	private final ModelPart bone8;
	private final ModelPart bone9;
	private final ModelPart bone10;
	private final ModelPart bone11;
	private final ModelPart bone12;
	private final ModelPart bone13;
	private final ModelPart bone14;
	private final ModelPart bone15;
	private final ModelPart bone16;
	private final ModelPart bone18;
	private final ModelPart bone19;
	private final ModelPart bone20;
	private final ModelPart bone21;
	private final ModelPart bone22;
	private final ModelPart bone17;

	public ChainJailEffectModel(ModelPart root) {
		this.bone = root.getChild("bone");
		this.bone2 = root.getChild("bone2");
		this.bone3 = root.getChild("bone3");
		this.bone4 = root.getChild("bone4");
		this.bone5 = root.getChild("bone5");
		this.bone6 = root.getChild("bone6");
		this.bone7 = root.getChild("bone7");
		this.bone8 = root.getChild("bone8");
		this.bone9 = root.getChild("bone9");
		this.bone10 = root.getChild("bone10");
		this.bone11 = root.getChild("bone11");
		this.bone12 = root.getChild("bone12");
		this.bone13 = root.getChild("bone13");
		this.bone14 = root.getChild("bone14");
		this.bone15 = root.getChild("bone15");
		this.bone16 = root.getChild("bone16");
		this.bone18 = root.getChild("bone18");
		this.bone19 = root.getChild("bone19");
		this.bone20 = root.getChild("bone20");
		this.bone21 = root.getChild("bone21");
		this.bone22 = root.getChild("bone22");
		this.bone17 = root.getChild("bone17");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition bone = partdefinition.addOrReplaceChild("bone", CubeListBuilder.create().texOffs(0, 0).addBox(-0.5F, -0.5F, -2.5F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(4, 6).addBox(-0.5F, -0.5F, 3.5F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-6.5F, 9.5F, 26.5F, -1.3315F, 0.3188F, -2.319F));

		PartDefinition cube_r1 = bone.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(0, 6).addBox(-0.5F, -3.5F, -0.5F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 4.0F, 0.0F, -1.5708F, 0.0F, 0.0F));

		PartDefinition cube_r2 = bone.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(4, 0).addBox(-0.5F, -3.5F, -0.5F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -1.5708F, 0.0F, 0.0F));

		PartDefinition bone2 = partdefinition.addOrReplaceChild("bone2", CubeListBuilder.create(), PartPose.offsetAndRotation(-8.5F, 11.5F, 22.5F, 0.5549F, -0.3776F, 1.4486F));

		PartDefinition cube_r3 = bone2.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(12, 0).addBox(-0.5F, -2.5F, -3.5F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(8, 0).addBox(-0.5F, -2.5F, 2.5F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.5708F));

		PartDefinition cube_r4 = bone2.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(0, 12).addBox(-0.5F, -2.5F, -2.5F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(8, 6).addBox(-0.5F, -2.5F, 1.5F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -1.5708F, 0.0F, 1.5708F));

		PartDefinition bone3 = partdefinition.addOrReplaceChild("bone3", CubeListBuilder.create(), PartPose.offsetAndRotation(-1.5F, 5.5F, 25.5F, -1.6869F, 0.3219F, -2.1863F));

		PartDefinition cube_r5 = bone3.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(12, 12).addBox(-0.5F, -2.5F, -3.5F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(4, 12).addBox(-0.5F, -2.5F, 2.5F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.5708F));

		PartDefinition cube_r6 = bone3.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(8, 12).addBox(-0.5F, -2.5F, -2.5F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(12, 6).addBox(-0.5F, -2.5F, 1.5F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -1.5708F, 0.0F, 1.5708F));

		PartDefinition bone4 = partdefinition.addOrReplaceChild("bone4", CubeListBuilder.create().texOffs(16, 6).addBox(-0.5F, -0.5F, 3.5F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(16, 12).addBox(-0.5F, -0.5F, -2.5F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.5F, 3.5F, 23.5F, 1.9632F, -0.0167F, 1.0002F));

		PartDefinition cube_r7 = bone4.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(0, 18).addBox(-0.5F, -3.5F, -0.5F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 4.0F, 0.0F, -1.5708F, 0.0F, 0.0F));

		PartDefinition cube_r8 = bone4.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(16, 0).addBox(-0.5F, -3.5F, -0.5F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -1.5708F, 0.0F, 0.0F));

		PartDefinition bone5 = partdefinition.addOrReplaceChild("bone5", CubeListBuilder.create().texOffs(8, 18).addBox(-0.5F, -0.5F, 3.5F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(12, 18).addBox(-0.5F, -0.5F, -2.5F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-10.5F, 12.5F, 17.5F, 0.48F, 0.3927F, -0.9599F));

		PartDefinition cube_r9 = bone5.addOrReplaceChild("cube_r9", CubeListBuilder.create().texOffs(16, 18).addBox(-0.5F, -3.5F, -0.5F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 4.0F, 0.0F, -1.5708F, 0.0F, 0.0F));

		PartDefinition cube_r10 = bone5.addOrReplaceChild("cube_r10", CubeListBuilder.create().texOffs(4, 18).addBox(-0.5F, -3.5F, -0.5F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -1.5708F, 0.0F, 0.0F));

		PartDefinition bone6 = partdefinition.addOrReplaceChild("bone6", CubeListBuilder.create(), PartPose.offsetAndRotation(-7.5F, 15.5F, 15.5F, 0.7915F, 0.3567F, -1.0637F));

		PartDefinition cube_r11 = bone6.addOrReplaceChild("cube_r11", CubeListBuilder.create().texOffs(20, 18).addBox(-0.5F, -2.5F, 1.5F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(20, 12).addBox(-0.5F, -2.5F, -2.5F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -1.5708F, 0.0F, 1.5708F));

		PartDefinition cube_r12 = bone6.addOrReplaceChild("cube_r12", CubeListBuilder.create().texOffs(20, 6).addBox(-0.5F, -2.5F, -3.5F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(20, 0).addBox(-0.5F, -2.5F, 2.5F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.5708F));

		PartDefinition bone7 = partdefinition.addOrReplaceChild("bone7", CubeListBuilder.create(), PartPose.offsetAndRotation(5.5F, -0.5F, 23.5F, 1.152F, 0.1527F, -1.8344F));

		PartDefinition cube_r13 = bone7.addOrReplaceChild("cube_r13", CubeListBuilder.create().texOffs(24, 6).addBox(-0.5F, -2.5F, 1.5F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(4, 24).addBox(-0.5F, -2.5F, -2.5F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -1.5708F, 0.0F, 1.5708F));

		PartDefinition cube_r14 = bone7.addOrReplaceChild("cube_r14", CubeListBuilder.create().texOffs(24, 0).addBox(-0.5F, -2.5F, -3.5F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 24).addBox(-0.5F, -2.5F, 2.5F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.5708F));

		PartDefinition bone8 = partdefinition.addOrReplaceChild("bone8", CubeListBuilder.create().texOffs(12, 24).addBox(-0.5F, -0.5F, 3.5F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(24, 12).addBox(-0.5F, -0.5F, -2.5F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(11.5F, -1.5F, 21.5F, -0.6981F, 0.0F, 1.4835F));

		PartDefinition cube_r15 = bone8.addOrReplaceChild("cube_r15", CubeListBuilder.create().texOffs(16, 24).addBox(-0.5F, -3.5F, -0.5F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 4.0F, 0.0F, -1.5708F, 0.0F, 0.0F));

		PartDefinition cube_r16 = bone8.addOrReplaceChild("cube_r16", CubeListBuilder.create().texOffs(8, 24).addBox(-0.5F, -3.5F, -0.5F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -1.5708F, 0.0F, 0.0F));

		PartDefinition bone9 = partdefinition.addOrReplaceChild("bone9", CubeListBuilder.create().texOffs(20, 24).addBox(-0.5F, -2.5F, 2.5F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(24, 24).addBox(-0.5F, -2.5F, -3.5F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.5F, 17.5F, 14.5F, -1.5708F, -0.5236F, 1.9199F));

		PartDefinition cube_r17 = bone9.addOrReplaceChild("cube_r17", CubeListBuilder.create().texOffs(28, 0).addBox(-0.5F, -3.5F, -0.5F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 2.0F, -1.0F, -1.5708F, 0.0F, 0.0F));

		PartDefinition cube_r18 = bone9.addOrReplaceChild("cube_r18", CubeListBuilder.create().texOffs(24, 18).addBox(-0.5F, -3.5F, -0.5F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -2.0F, -1.0F, -1.5708F, 0.0F, 0.0F));

		PartDefinition bone10 = partdefinition.addOrReplaceChild("bone10", CubeListBuilder.create(), PartPose.offsetAndRotation(0.5F, 18.5F, 14.5F, -1.5141F, -0.5431F, 1.7174F));

		PartDefinition cube_r19 = bone10.addOrReplaceChild("cube_r19", CubeListBuilder.create().texOffs(28, 24).addBox(-0.5F, -2.5F, 1.5F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(28, 18).addBox(-0.5F, -2.5F, -2.5F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -1.5708F, 0.0F, 1.5708F));

		PartDefinition cube_r20 = bone10.addOrReplaceChild("cube_r20", CubeListBuilder.create().texOffs(28, 12).addBox(-0.5F, -2.5F, -3.5F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(28, 6).addBox(-0.5F, -2.5F, 2.5F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.5708F));

		PartDefinition bone11 = partdefinition.addOrReplaceChild("bone11", CubeListBuilder.create(), PartPose.offsetAndRotation(-1.5F, 9.5F, 13.5F, -1.2141F, -0.1947F, 0.4347F));

		PartDefinition cube_r21 = bone11.addOrReplaceChild("cube_r21", CubeListBuilder.create().texOffs(12, 30).addBox(-0.5F, -2.5F, 1.5F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(8, 30).addBox(-0.5F, -2.5F, -2.5F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -1.5708F, 0.0F, 1.5708F));

		PartDefinition cube_r22 = bone11.addOrReplaceChild("cube_r22", CubeListBuilder.create().texOffs(4, 30).addBox(-0.5F, -2.5F, -3.5F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 30).addBox(-0.5F, -2.5F, 2.5F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.5708F));

		PartDefinition bone12 = partdefinition.addOrReplaceChild("bone12", CubeListBuilder.create(), PartPose.offsetAndRotation(0.5F, 5.5F, 12.5F, 2.0942F, 0.9867F, 1.2122F));

		PartDefinition cube_r23 = bone12.addOrReplaceChild("cube_r23", CubeListBuilder.create().texOffs(16, 30).addBox(-0.5F, -2.5F, 1.5F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(20, 30).addBox(-0.5F, -2.5F, -2.5F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -1.5708F, 0.0F, 1.5708F));

		PartDefinition cube_r24 = bone12.addOrReplaceChild("cube_r24", CubeListBuilder.create().texOffs(24, 30).addBox(-0.5F, -2.5F, -3.5F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(28, 30).addBox(-0.5F, -2.5F, 2.5F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.5708F));

		PartDefinition bone13 = partdefinition.addOrReplaceChild("bone13", CubeListBuilder.create(), PartPose.offsetAndRotation(6.5F, 0.5F, 14.5F, 0.0063F, 0.7388F, -0.833F));

		PartDefinition cube_r25 = bone13.addOrReplaceChild("cube_r25", CubeListBuilder.create().texOffs(32, 0).addBox(-0.5F, -2.5F, 1.5F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(32, 6).addBox(-0.5F, -2.5F, -2.5F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -1.5708F, 0.0F, 1.5708F));

		PartDefinition cube_r26 = bone13.addOrReplaceChild("cube_r26", CubeListBuilder.create().texOffs(32, 12).addBox(-0.5F, -2.5F, -3.5F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(32, 18).addBox(-0.5F, -2.5F, 2.5F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.5708F));

		PartDefinition bone14 = partdefinition.addOrReplaceChild("bone14", CubeListBuilder.create(), PartPose.offsetAndRotation(9.5F, -1.5F, 17.5F, 0.6225F, 0.008F, 1.0636F));

		PartDefinition cube_r27 = bone14.addOrReplaceChild("cube_r27", CubeListBuilder.create().texOffs(32, 24).addBox(-0.5F, -2.5F, 1.5F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(32, 30).addBox(-0.5F, -2.5F, -2.5F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -1.5708F, 0.0F, 1.5708F));

		PartDefinition cube_r28 = bone14.addOrReplaceChild("cube_r28", CubeListBuilder.create().texOffs(0, 36).addBox(-0.5F, -2.5F, -3.5F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(36, 0).addBox(-0.5F, -2.5F, 2.5F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.5708F));

		PartDefinition bone15 = partdefinition.addOrReplaceChild("bone15", CubeListBuilder.create(), PartPose.offsetAndRotation(3.5F, 2.5F, 12.5F, -1.2858F, 0.3796F, -2.074F));

		PartDefinition cube_r29 = bone15.addOrReplaceChild("cube_r29", CubeListBuilder.create().texOffs(4, 36).addBox(-0.5F, -2.5F, 1.5F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(36, 6).addBox(-0.5F, -2.5F, -2.5F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -1.5708F, 0.0F, 1.5708F));

		PartDefinition cube_r30 = bone15.addOrReplaceChild("cube_r30", CubeListBuilder.create().texOffs(8, 36).addBox(-0.5F, -2.5F, -3.5F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(12, 36).addBox(-0.5F, -2.5F, 2.5F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.5708F));

		PartDefinition bone16 = partdefinition.addOrReplaceChild("bone16", CubeListBuilder.create(), PartPose.offset(11.5F, 11.5F, -47.5F));

		PartDefinition bone18 = partdefinition.addOrReplaceChild("bone18", CubeListBuilder.create(), PartPose.offsetAndRotation(5.5F, 1.5F, 23.5F, -1.3833F, -0.0134F, 2.8927F));

		PartDefinition cube_r31 = bone18.addOrReplaceChild("cube_r31", CubeListBuilder.create().texOffs(20, 36).addBox(-0.5F, -2.5F, 1.5F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(36, 18).addBox(-0.5F, -2.5F, -2.5F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -1.5708F, 0.0F, 1.5708F));

		PartDefinition cube_r32 = bone18.addOrReplaceChild("cube_r32", CubeListBuilder.create().texOffs(16, 36).addBox(-0.5F, -2.5F, -3.5F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(36, 12).addBox(-0.5F, -2.5F, 2.5F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.5708F));

		PartDefinition bone19 = partdefinition.addOrReplaceChild("bone19", CubeListBuilder.create(), PartPose.offsetAndRotation(7.5F, 11.5F, 21.5F, 1.1401F, 0.5024F, -0.1284F));

		PartDefinition cube_r33 = bone19.addOrReplaceChild("cube_r33", CubeListBuilder.create().texOffs(40, 6).addBox(-0.5F, -2.5F, 1.5F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(40, 0).addBox(-0.5F, -2.5F, -2.5F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -1.5708F, 0.0F, 1.5708F));

		PartDefinition cube_r34 = bone19.addOrReplaceChild("cube_r34", CubeListBuilder.create().texOffs(36, 36).addBox(-0.5F, -2.5F, -3.5F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(32, 36).addBox(-0.5F, -2.5F, 2.5F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.5708F));

		PartDefinition bone20 = partdefinition.addOrReplaceChild("bone20", CubeListBuilder.create(), PartPose.offsetAndRotation(6.5F, 6.5F, 22.5F, 1.1781F, -1.0908F, 0.0F));

		PartDefinition cube_r35 = bone20.addOrReplaceChild("cube_r35", CubeListBuilder.create().texOffs(40, 30).addBox(-0.5F, -2.5F, 1.5F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(40, 24).addBox(-0.5F, -2.5F, -2.5F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -1.5708F, 0.0F, 1.5708F));

		PartDefinition cube_r36 = bone20.addOrReplaceChild("cube_r36", CubeListBuilder.create().texOffs(40, 18).addBox(-0.5F, -2.5F, -3.5F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(40, 12).addBox(-0.5F, -2.5F, 2.5F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.5708F));

		PartDefinition bone21 = partdefinition.addOrReplaceChild("bone21", CubeListBuilder.create(), PartPose.offsetAndRotation(6.5F, 15.5F, 18.5F, 0.5652F, -0.4345F, 0.8217F));

		PartDefinition cube_r37 = bone21.addOrReplaceChild("cube_r37", CubeListBuilder.create().texOffs(8, 42).addBox(-0.5F, -2.5F, 1.5F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(4, 42).addBox(-0.5F, -2.5F, -2.5F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -1.5708F, 0.0F, 1.5708F));

		PartDefinition cube_r38 = bone21.addOrReplaceChild("cube_r38", CubeListBuilder.create().texOffs(0, 42).addBox(-0.5F, -2.5F, -3.5F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(40, 36).addBox(-0.5F, -2.5F, 2.5F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.5708F));

		PartDefinition bone22 = partdefinition.addOrReplaceChild("bone22", CubeListBuilder.create(), PartPose.offsetAndRotation(4.5F, 17.5F, 14.5F, -0.0533F, 1.0824F, -0.6575F));

		PartDefinition cube_r39 = bone22.addOrReplaceChild("cube_r39", CubeListBuilder.create().texOffs(24, 42).addBox(-0.5F, -2.5F, 1.5F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(20, 42).addBox(-0.5F, -2.5F, -2.5F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -1.5708F, 0.0F, 1.5708F));

		PartDefinition cube_r40 = bone22.addOrReplaceChild("cube_r40", CubeListBuilder.create().texOffs(16, 42).addBox(-0.5F, -2.5F, -3.5F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(12, 42).addBox(-0.5F, -2.5F, 2.5F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.5708F));

		PartDefinition bone17 = partdefinition.addOrReplaceChild("bone17", CubeListBuilder.create(), PartPose.offset(-5.5F, 12.5F, 14.5F));

		PartDefinition cube_r41 = bone17.addOrReplaceChild("cube_r41", CubeListBuilder.create().texOffs(36, 30).addBox(-0.5F, -2.5F, 1.5F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(28, 36).addBox(-0.5F, -2.5F, -2.5F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.3423F, -0.2248F, 0.6584F));

		PartDefinition cube_r42 = bone17.addOrReplaceChild("cube_r42", CubeListBuilder.create().texOffs(36, 24).addBox(-0.5F, -2.5F, -3.5F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(24, 36).addBox(-0.5F, -2.5F, 2.5F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 1.9131F, -0.2248F, 0.6584F));

		return LayerDefinition.create(meshdefinition, 64, 64);
	}

	@Override
	public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		bone.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		bone2.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		bone3.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		bone4.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		bone5.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		bone6.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		bone7.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		bone8.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		bone9.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		bone10.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		bone11.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		bone12.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		bone13.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		bone14.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		bone15.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		bone16.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		bone18.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		bone19.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		bone20.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		bone21.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		bone22.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		bone17.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}
}

