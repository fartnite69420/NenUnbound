package com.huntercraft.huntercraft.client.render;

import com.huntercraft.huntercraft.abilities.HunterAbilities;
import com.huntercraft.huntercraft.abilities.nenability.NenTechniqueAbility;
import com.huntercraft.huntercraft.client.model.ChainEndTipModel;
import com.huntercraft.huntercraft.client.model.ChainJailEffectModel;
import com.huntercraft.huntercraft.client.model.ElasticAuraShieldModel;
import com.huntercraft.huntercraft.client.model.SharedChainProjectileModel;
import com.huntercraft.huntercraft.data.HunterPlayerData;
import com.huntercraft.huntercraft.data.HunterPlayerDataProvider;
import com.huntercraft.huntercraft.effect.HunterMobEffects;
import com.huntercraft.huntercraft.entity.SmokeyChainProjectileEntity;
import com.huntercraft.huntercraft.entity.SmokeCloneEntity;
import com.huntercraft.huntercraft.entity.SmokeSoldierEntity;
import com.huntercraft.huntercraft.entity.WingEntity;
import com.huntercraft.huntercraft.faction.FactionUtil;
import net.minecraft.world.effect.MobEffectInstance;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.OutlineBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;
import org.joml.Matrix3f;

import java.util.UUID;
import java.util.HashSet;
import java.util.Set;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class NenAuraRenderHandler {
    private static final ResourceLocation AURA_TEXTURE = new ResourceLocation("minecraft", "textures/misc/white.png");
    private static final ResourceLocation EN_SPHERE_TEXTURE = new ResourceLocation("huntercraft", "textures/effects/solid_white.png");
    private static final ResourceLocation ELASTIC_REFLECT_TEXTURE = new ResourceLocation("huntercraft", "textures/effects/elastic_aura_reflect.png");
    private static final ResourceLocation SCARLET_EYES_TEXTURE = new ResourceLocation("huntercraft", "textures/effects/solid_white.png");
    private static final ResourceLocation CHAIN_JAIL_TEXTURE = new ResourceLocation("huntercraft", "textures/effects/chain_jail_effect_texture.png");
    private static final ResourceLocation CHAIN_TEXTURE = new ResourceLocation("huntercraft", "textures/entity/chainprojectile_texture.png");
    private static final ResourceLocation DOWSING_CHAIN_END_TEXTURE = new ResourceLocation("huntercraft", "textures/entity/dowsing_chain_end_texture.png");
    private static final ResourceLocation CHAIN_JAIL_END_TEXTURE = new ResourceLocation("huntercraft", "textures/entity/chain_jail_end.png");
    private static final ResourceLocation HOLY_CHAIN_END_TEXTURE = new ResourceLocation("huntercraft", "textures/entity/holy_chain_end_texture.png");
    private static final ResourceLocation JUDGMENT_CHAIN_END_TEXTURE = new ResourceLocation("huntercraft", "textures/entity/judgement_chain_end_texture.png");
    private static final ResourceLocation STEAL_CHAIN_END_TEXTURE = new ResourceLocation("huntercraft", "textures/entity/steal_chain_end.png");
    private static ElasticAuraShieldModel elasticAuraShieldModel;
    private static ChainJailEffectModel chainJailEffectModel;
    private static SharedChainProjectileModel<?> holyChainModel;
    private static ChainEndTipModel dowsingChainEndModel;
    private static ChainEndTipModel chainJailEndModel;
    private static ChainEndTipModel holyChainEndModel;
    private static ChainEndTipModel judgmentChainEndModel;
    private static ChainEndTipModel stealChainEndModel;
    private static final int EN_SPHERE_DETAIL = 8;
    private static final int EN_HIGHLIGHT_REFRESH_TICKS = 2;
    private static final SphereVertex[] EN_SPHERE_MESH = buildSphereMesh(EN_SPHERE_DETAIL);
    private static final Set<Integer> TRACKED_EN_ENTITIES = new HashSet<>();

    private NenAuraRenderHandler() {
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer localPlayer = minecraft.player;
        if (localPlayer == null || minecraft.level == null || minecraft.isPaused()) {
            clearTrackedEnHighlights(minecraft.level);
            return;
        }

        HunterPlayerData localData = localPlayer.getCapability(HunterPlayerDataProvider.CAPABILITY).orElse(null);
        if (localData == null) {
            clearTrackedEnHighlights(minecraft.level);
            return;
        }

        if (!localData.isEnActive()) {
            clearTrackedEnHighlights(minecraft.level);
            return;
        }
        if ((localPlayer.tickCount % EN_HIGHLIGHT_REFRESH_TICKS) != 0) {
            return;
        }

        double maxDistanceSqr = HunterAbilities.EN.getRadius(localData);
        maxDistanceSqr *= maxDistanceSqr;
        Set<Integer> newTrackedEntities = new HashSet<>();
        for (Entity entity : minecraft.level.entitiesForRendering()) {
            if (entity == localPlayer || localPlayer.distanceToSqr(entity) > maxDistanceSqr) {
                continue;
            }
            HunterPlayerData targetData = entity.getCapability(HunterPlayerDataProvider.CAPABILITY).orElse(null);
            if (targetData != null && HunterAbilities.ZETSU.isActive(targetData)) {
                continue;
            }
            entity.setGlowingTag(true);
            newTrackedEntities.add(entity.getId());
        }

        for (int entityId : new HashSet<>(TRACKED_EN_ENTITIES)) {
            if (!newTrackedEntities.contains(entityId)) {
                Entity entity = minecraft.level.getEntity(entityId);
                if (entity != null) {
                    entity.setGlowingTag(false);
                }
            }
        }
        TRACKED_EN_ENTITIES.clear();
        TRACKED_EN_ENTITIES.addAll(newTrackedEntities);
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        LocalPlayer localPlayer = minecraft.player;
        if (level == null || localPlayer == null) {
            return;
        }
        HunterPlayerData localData = localPlayer.getCapability(HunterPlayerDataProvider.CAPABILITY).orElse(null);
        boolean canSeeNenAuras = localData != null && localData.hasGyoUnlocked();

        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource bufferSource = minecraft.renderBuffers().bufferSource();
        Vec3 camera = minecraft.gameRenderer.getMainCamera().getPosition();
        if (canSeeNenAuras && localData.isEnActive()) {
            renderEnHighlights(minecraft, level, localPlayer, poseStack, camera, event.getPartialTick());
            renderEnSphere(poseStack, camera, localPlayer, event.getPartialTick(), localData);
        }
        for (Player player : level.players()) {
            if (!(player instanceof AbstractClientPlayer clientPlayer)) {
                continue;
            }
            if (player == localPlayer && minecraft.options.getCameraType().isFirstPerson()) {
                continue;
            }
            HunterPlayerData targetData = player.getCapability(HunterPlayerDataProvider.CAPABILITY).orElse(null);
            boolean visibleSpecialEffect = targetData != null && (targetData.isActiveAbility("holy_chain") || targetData.isEmperorTimeActive());
            boolean canSeeTargetAura = canSeeNenAuras && NenTechniqueAbility.canSeeNenAura(localData, targetData);
            if (!canSeeTargetAura && !visibleSpecialEffect) {
                continue;
            }

            EntityRenderer<? super AbstractClientPlayer> rawRenderer = minecraft.getEntityRenderDispatcher().getRenderer(clientPlayer);
            if (!(rawRenderer instanceof PlayerRenderer renderer)) {
                continue;
            }

            PlayerModel<AbstractClientPlayer> model = renderer.getModel();
            if (canSeeTargetAura && targetData != null) {
                float red = ((targetData.getNenAuraColor() >> 16) & 0xFF) / 255.0F;
                float green = ((targetData.getNenAuraColor() >> 8) & 0xFF) / 255.0F;
                float blue = (targetData.getNenAuraColor() & 0xFF) / 255.0F;
                if (targetData.isEnActive()) {
                    renderEnSphere(poseStack, camera, clientPlayer, event.getPartialTick(), targetData);
                }
                renderAuraCoating(poseStack, bufferSource, camera, event.getPartialTick(), clientPlayer, model, red, green, blue, targetData);
            }
            if (targetData != null && targetData.isActiveAbility("holy_chain")) {
                renderHolyChainOverlay(poseStack, bufferSource, camera, event.getPartialTick(), clientPlayer, model);
            }
            if (targetData != null && targetData.isEmperorTimeActive()) {
                renderScarletEyes(poseStack, bufferSource, camera, event.getPartialTick(), clientPlayer, model, targetData);
            }
        }

        for (Entity entity : level.entitiesForRendering()) {
            if (!(entity instanceof WingEntity wing)) {
                continue;
            }
            HunterPlayerData wingData = wing.getTrainerNenData();
            if (!(canSeeNenAuras && NenTechniqueAbility.canSeeNenAura(localData, wingData))) {
                continue;
            }
            EntityRenderer<? super WingEntity> rawRenderer = minecraft.getEntityRenderDispatcher().getRenderer(wing);
            if (!(rawRenderer instanceof WingRenderer renderer)) {
                continue;
            }
            PlayerModel<WingEntity> model = renderer.getModel();
            float red = ((wingData.getNenAuraColor() >> 16) & 0xFF) / 255.0F;
            float green = ((wingData.getNenAuraColor() >> 8) & 0xFF) / 255.0F;
            float blue = (wingData.getNenAuraColor() & 0xFF) / 255.0F;
            if (wingData.isEnActive()) {
                renderEnSphere(poseStack, camera, wing, event.getPartialTick(), wingData);
            }
            renderWingAuraCoating(poseStack, bufferSource, camera, event.getPartialTick(), wing, model, red, green, blue, wingData);
        }

        for (Entity entity : level.entitiesForRendering()) {
            if (!(entity instanceof SmokeSoldierEntity soldier)) {
                continue;
            }
            HunterPlayerData soldierData = soldier.getSoldierNenData();
            if (!(canSeeNenAuras && NenTechniqueAbility.canSeeNenAura(localData, soldierData))) {
                continue;
            }
            EntityRenderer<? super SmokeSoldierEntity> rawRenderer = minecraft.getEntityRenderDispatcher().getRenderer(soldier);
            if (!(rawRenderer instanceof SmokeSoldierRenderer renderer)) {
                continue;
            }
            PlayerModel<SmokeSoldierEntity> model = renderer.getSmokeSoldierModel();
            float red = ((soldierData.getNenAuraColor() >> 16) & 0xFF) / 255.0F;
            float green = ((soldierData.getNenAuraColor() >> 8) & 0xFF) / 255.0F;
            float blue = (soldierData.getNenAuraColor() & 0xFF) / 255.0F;
            if (soldierData.isEnActive()) {
                renderEnSphere(poseStack, camera, soldier, event.getPartialTick(), soldierData);
            }
            renderConstructAuraCoating(poseStack, bufferSource, camera, event.getPartialTick(), soldier, model, red, green, blue, soldierData);
        }

        for (Entity entity : level.entitiesForRendering()) {
            if (!(entity instanceof SmokeCloneEntity clone)) {
                continue;
            }
            HunterPlayerData cloneData = clone.getCloneNenData();
            if (!(canSeeNenAuras && NenTechniqueAbility.canSeeNenAura(localData, cloneData))) {
                continue;
            }
            EntityRenderer<? super SmokeCloneEntity> rawRenderer = minecraft.getEntityRenderDispatcher().getRenderer(clone);
            if (!(rawRenderer instanceof SmokeCloneRenderer renderer)) {
                continue;
            }
            PlayerModel<SmokeCloneEntity> model = renderer.getModelFor(clone);
            float red = ((cloneData.getNenAuraColor() >> 16) & 0xFF) / 255.0F;
            float green = ((cloneData.getNenAuraColor() >> 8) & 0xFF) / 255.0F;
            float blue = (cloneData.getNenAuraColor() & 0xFF) / 255.0F;
            if (cloneData.isEnActive()) {
                renderEnSphere(poseStack, camera, clone, event.getPartialTick(), cloneData);
            }
            renderConstructAuraCoating(poseStack, bufferSource, camera, event.getPartialTick(), clone, model, red, green, blue, cloneData);
        }

        for (Entity entity : level.entitiesForRendering()) {
            if (!(entity instanceof LivingEntity living)) {
                continue;
            }
            if (!living.hasEffect(HunterMobEffects.CHAIN_JAIL.get()) && living.hasEffect(HunterMobEffects.DOWSING_CHAINED.get())) {
                renderChainWrapLinks(poseStack, bufferSource, camera, event.getPartialTick(), living);
            }
        }

        for (Entity entity : level.entitiesForRendering()) {
            if (entity instanceof SmokeyChainProjectileEntity projectile) {
                renderLiveChainProjectileTether(poseStack, bufferSource, camera, event.getPartialTick(), projectile);
            }
        }

        for (Player player : level.players()) {
            HunterPlayerData data = player.getCapability(HunterPlayerDataProvider.CAPABILITY).orElse(null);
            if (data == null || (!data.isActiveAbility("dowsing_chain") && !data.isActiveAbility("chain_jail"))) {
                continue;
            }
            LivingEntity target = resolveLivingByUuid(level, data.getActiveAbilityTargetUuid());
            if (target != null && target.isAlive()) {
                renderTrackedChainTether(poseStack, bufferSource, camera, event.getPartialTick(), player, target, data.getActiveAbilityId());
                if (data.isActiveAbility("chain_jail")) {
                    renderChainWrapLinks(poseStack, bufferSource, camera, event.getPartialTick(), target);
                }
            }
        }

        bufferSource.endBatch();
    }

    private static void renderEnHighlights(Minecraft minecraft, ClientLevel level, LocalPlayer localPlayer, PoseStack poseStack, Vec3 camera, float partialTick) {
        HunterPlayerData localData = localPlayer.getCapability(HunterPlayerDataProvider.CAPABILITY).orElse(null);
        if (localData == null) {
            return;
        }
        double maxDistanceSqr = HunterAbilities.EN.getRadius(localData);
        maxDistanceSqr *= maxDistanceSqr;
        EntityRenderDispatcher dispatcher = minecraft.getEntityRenderDispatcher();
        OutlineBufferSource outlineSource = minecraft.renderBuffers().outlineBufferSource();

        for (int entityId : new HashSet<>(TRACKED_EN_ENTITIES)) {
            Entity entity = level.getEntity(entityId);
            if (entity == null || entity == localPlayer || localPlayer.distanceToSqr(entity) > maxDistanceSqr) {
                TRACKED_EN_ENTITIES.remove(entityId);
                continue;
            }
            HunterPlayerData targetData = entity.getCapability(HunterPlayerDataProvider.CAPABILITY).orElse(null);
            if (targetData != null && HunterAbilities.ZETSU.isActive(targetData)) {
                continue;
            }
            int[] outlineColor = getEnOutlineColor(localPlayer, localData, entity, targetData);
            outlineSource.setColor(outlineColor[0], outlineColor[1], outlineColor[2], 180);

            poseStack.pushPose();
            double x = Mth.lerp(partialTick, entity.xOld, entity.getX()) - camera.x;
            double y = Mth.lerp(partialTick, entity.yOld, entity.getY()) - camera.y;
            double z = Mth.lerp(partialTick, entity.zOld, entity.getZ()) - camera.z;
            float yaw = Mth.rotLerp(partialTick, entity.yRotO, entity.getYRot());
            dispatcher.render(entity, x, y, z, yaw, partialTick, poseStack, outlineSource, 15728880);
            poseStack.popPose();
        }

        outlineSource.endOutlineBatch();
    }

    private static int[] getEnOutlineColor(LocalPlayer localPlayer, HunterPlayerData localData, Entity entity, HunterPlayerData targetData) {
        if (entity instanceof Player targetPlayer) {
            if (FactionUtil.areFactionMates(localPlayer, targetPlayer) || areFactionMates(localData, targetData)) {
                return new int[]{70, 230, 90};
            }
            return new int[]{255, 70, 70};
        }
        if (entity instanceof SmokeSoldierEntity soldier) {
            Player ownerPlayer = resolveSmokeSoldierOwner(localPlayer, soldier);
            HunterPlayerData ownerData = ownerPlayer != null
                    ? ownerPlayer.getCapability(HunterPlayerDataProvider.CAPABILITY).orElse(null)
                    : null;
            if (isFriendlyOwnedConstruct(localPlayer, localData, ownerPlayer, ownerData)) {
                return new int[]{70, 230, 90};
            }
            return new int[]{255, 70, 70};
        }
        if (entity instanceof SmokeCloneEntity clone) {
            Player ownerPlayer = resolveConstructOwner(localPlayer, clone.getOwnerUuid());
            HunterPlayerData ownerData = ownerPlayer != null
                    ? ownerPlayer.getCapability(HunterPlayerDataProvider.CAPABILITY).orElse(null)
                    : null;
            if (isFriendlyOwnedConstruct(localPlayer, localData, ownerPlayer, ownerData)) {
                return new int[]{70, 230, 90};
            }
            return new int[]{255, 70, 70};
        }
        return new int[]{255, 230, 70};
    }

    private static Player resolveSmokeSoldierOwner(LocalPlayer localPlayer, SmokeSoldierEntity soldier) {
        return resolveConstructOwner(localPlayer, soldier.getOwnerUuid());
    }

    private static Player resolveConstructOwner(LocalPlayer localPlayer, UUID ownerUuid) {
        if (ownerUuid == null || localPlayer.level() == null) {
            return null;
        }
        return localPlayer.level().getPlayerByUUID(ownerUuid);
    }

    private static boolean isFriendlyOwnedConstruct(LocalPlayer localPlayer, HunterPlayerData localData, Player ownerPlayer, HunterPlayerData ownerData) {
        if (ownerPlayer == null) {
            return false;
        }
        if (ownerPlayer.getUUID().equals(localPlayer.getUUID())) {
            return true;
        }
        return FactionUtil.areFactionMates(localPlayer, ownerPlayer) || areFactionMates(localData, ownerData);
    }

    private static boolean areFactionMates(HunterPlayerData localData, HunterPlayerData targetData) {
        if (localData == null || targetData == null) {
            return false;
        }
        String localFaction = localData.getFactionName();
        String targetFaction = targetData.getFactionName();
        return !localFaction.isBlank() && localFaction.equalsIgnoreCase(targetFaction);
    }

    private static void renderEnSphere(PoseStack poseStack, Vec3 camera, Entity entity, float partialTick, HunterPlayerData data) {
        float radius = HunterAbilities.EN.getRadius(data);
        if (radius <= 0.0F) {
            return;
        }
        float red = ((data.getNenAuraColor() >> 16) & 0xFF) / 255.0F;
        float green = ((data.getNenAuraColor() >> 8) & 0xFF) / 255.0F;
        float blue = (data.getNenAuraColor() & 0xFF) / 255.0F;
        float alpha = entity == Minecraft.getInstance().player ? 0.13F : 0.17F;
        double x = Mth.lerp(partialTick, entity.xOld, entity.getX());
        double y = Mth.lerp(partialTick, entity.yOld, entity.getY()) + (entity.getBbHeight() * 0.5D);
        double z = Mth.lerp(partialTick, entity.zOld, entity.getZ());
        double dx = camera.x - x;
        double dy = camera.y - y;
        double dz = camera.z - z;
        boolean insideSphere = (dx * dx) + (dy * dy) + (dz * dz) < (radius * radius);
        float worldX = insideSphere ? (float) (x - camera.x) : 0.0F;
        float worldY = insideSphere ? (float) (y - camera.y) : 0.0F;
        float worldZ = insideSphere ? (float) (z - camera.z) : 0.0F;

        poseStack.pushPose();
        renderSphereShell(poseStack, worldX, worldY, worldZ, radius, EN_SPHERE_DETAIL, red, green, blue, alpha, insideSphere, entity.tickCount + partialTick);
        poseStack.popPose();
    }

    private static void renderElasticReflectShield(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, Vec3 camera, float partialTick, LivingEntity entity, float red, float green, float blue) {
        Minecraft minecraft = Minecraft.getInstance();
        if (elasticAuraShieldModel == null && minecraft.getEntityModels() != null) {
            elasticAuraShieldModel = new ElasticAuraShieldModel(minecraft.getEntityModels().bakeLayer(ElasticAuraShieldModel.LAYER_LOCATION));
        }
        if (elasticAuraShieldModel == null) {
            return;
        }
        double x = Mth.lerp(partialTick, entity.xOld, entity.getX());
        double y = Mth.lerp(partialTick, entity.yOld, entity.getY());
        double z = Mth.lerp(partialTick, entity.zOld, entity.getZ());
        float bodyYaw = Mth.rotLerp(partialTick, entity.yBodyRotO, entity.yBodyRot);
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityTranslucent(ELASTIC_REFLECT_TEXTURE));

        poseStack.pushPose();
        // Center on the player's body midpoint, push shield in front
        poseStack.translate(x - camera.x, y - camera.y + (entity.getBbHeight() * 0.5D), z - camera.z);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - bodyYaw));
        poseStack.translate(0.0D, 0.0D, -(entity.getBbWidth() * 0.5D + 0.15D));
        // Model is 16x28 units at 1/16 block per unit = 1.0 x 1.75 blocks at scale 1.0
        // Use scale 1.2 so shield is ~1.2 x 2.1 blocks — clearly covers the player front
        poseStack.scale(1.2F, 1.2F, 1.2F);
        elasticAuraShieldModel.setupAnim(entity.tickCount + partialTick);
        elasticAuraShieldModel.renderShield(poseStack, consumer, 15728880, red, green, blue, 0.9F);
        poseStack.popPose();
    }

    private static void renderChainJailOverlay(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, Vec3 camera, float partialTick, LivingEntity entity) {
        Minecraft minecraft = Minecraft.getInstance();
        if (chainJailEffectModel == null && minecraft.getEntityModels() != null) {
            chainJailEffectModel = new ChainJailEffectModel(minecraft.getEntityModels().bakeLayer(ChainJailEffectModel.LAYER_LOCATION));
        }
        if (chainJailEffectModel == null) {
            return;
        }
        double x = Mth.lerp(partialTick, entity.xOld, entity.getX());
        double y = Mth.lerp(partialTick, entity.yOld, entity.getY());
        double z = Mth.lerp(partialTick, entity.zOld, entity.getZ());
        float scale = Math.max(entity.getBbWidth(), entity.getBbHeight()) * 0.14F;
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(CHAIN_JAIL_TEXTURE));
        poseStack.pushPose();
        poseStack.translate(x - camera.x, y - camera.y + (entity.getBbHeight() * 0.5D), z - camera.z);
        poseStack.mulPose(Axis.YP.rotationDegrees((entity.tickCount + partialTick) * 4.0F));
        poseStack.scale(scale, scale, scale);
        chainJailEffectModel.renderToBuffer(poseStack, consumer, 15728880, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 0.85F);
        poseStack.popPose();
    }

    private static SharedChainProjectileModel<?> getSharedChainModel() {
        Minecraft minecraft = Minecraft.getInstance();
        if (holyChainModel == null && minecraft.getEntityModels() != null) {
            holyChainModel = new SharedChainProjectileModel<>(minecraft.getEntityModels().bakeLayer(SharedChainProjectileModel.LAYER_LOCATION));
        }
        return holyChainModel;
    }

    private static ChainEndTipModel getChainEndTipModel(String abilityId) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.getEntityModels() == null) {
            return null;
        }
        return switch (abilityId) {
            case "dowsing_chain" -> {
                if (dowsingChainEndModel == null) {
                    dowsingChainEndModel = new ChainEndTipModel(minecraft.getEntityModels().bakeLayer(ChainEndTipModel.DOWSING_LAYER));
                }
                yield dowsingChainEndModel;
            }
            case "chain_jail" -> {
                if (chainJailEndModel == null) {
                    chainJailEndModel = new ChainEndTipModel(minecraft.getEntityModels().bakeLayer(ChainEndTipModel.CHAIN_JAIL_LAYER));
                }
                yield chainJailEndModel;
            }
            case "holy_chain" -> {
                if (holyChainEndModel == null) {
                    holyChainEndModel = new ChainEndTipModel(minecraft.getEntityModels().bakeLayer(ChainEndTipModel.HOLY_LAYER));
                }
                yield holyChainEndModel;
            }
            case "judgment_chain" -> {
                if (judgmentChainEndModel == null) {
                    judgmentChainEndModel = new ChainEndTipModel(minecraft.getEntityModels().bakeLayer(ChainEndTipModel.JUDGMENT_LAYER));
                }
                yield judgmentChainEndModel;
            }
            case "steal_chain" -> {
                if (stealChainEndModel == null) {
                    stealChainEndModel = new ChainEndTipModel(minecraft.getEntityModels().bakeLayer(ChainEndTipModel.STEAL_LAYER));
                }
                yield stealChainEndModel;
            }
            default -> null;
        };
    }

    private static ResourceLocation getChainEndTipTexture(String abilityId) {
        return switch (abilityId) {
            case "dowsing_chain" -> DOWSING_CHAIN_END_TEXTURE;
            case "chain_jail" -> CHAIN_JAIL_END_TEXTURE;
            case "holy_chain" -> HOLY_CHAIN_END_TEXTURE;
            case "judgment_chain" -> JUDGMENT_CHAIN_END_TEXTURE;
            case "steal_chain" -> STEAL_CHAIN_END_TEXTURE;
            default -> null;
        };
    }

    private static void renderChainEndTip(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, Vec3 camera, Vec3 start, Vec3 end, String abilityId) {
        ChainEndTipModel tipModel = getChainEndTipModel(abilityId);
        ResourceLocation texture = getChainEndTipTexture(abilityId);
        if (tipModel == null || texture == null) {
            return;
        }
        Vec3 delta = end.subtract(start);
        if (delta.lengthSqr() <= 0.0025D) {
            return;
        }
        Vec3 direction = delta.normalize();
        Vec3 tipAnchor = end.subtract(direction.scale(getChainEndTipBackOffset(abilityId)));
        float yaw = (float) Math.atan2(delta.x, delta.z);
        float pitch = (float) Math.atan2(delta.y, Math.sqrt((delta.x * delta.x) + (delta.z * delta.z)));
        poseStack.pushPose();
        poseStack.translate(tipAnchor.x - camera.x, tipAnchor.y - camera.y, tipAnchor.z - camera.z);
        poseStack.mulPose(Axis.YP.rotation(yaw));
        poseStack.mulPose(Axis.XP.rotation(-pitch));
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        Vec3 correction = getChainEndTipOriginCorrection(abilityId);
        poseStack.translate(correction.x, correction.y, correction.z);
        float scale = getChainEndTipScale(abilityId);
        poseStack.scale(scale, scale, scale);
        tipModel.renderToBuffer(poseStack, bufferSource.getBuffer(RenderType.entityCutoutNoCull(texture)), 15728880, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
        poseStack.popPose();
    }

    private static Vec3 getChainEndTipOriginCorrection(String abilityId) {
        return switch (abilityId) {
            case "dowsing_chain" -> new Vec3(0.54D, -0.38D, 0.44D);
            case "chain_jail" -> new Vec3(0.54D, -0.52D, 0.40D);
            case "holy_chain" -> new Vec3(0.51D, -0.58D, -0.15D);
            case "judgment_chain", "steal_chain" -> new Vec3(0.54D, -0.53D, 0.40D);
            default -> Vec3.ZERO;
        };
    }

    private static float getChainEndTipScale(String abilityId) {
        return switch (abilityId) {
            case "holy_chain" -> 0.72F;
            case "judgment_chain", "chain_jail", "steal_chain" -> 0.82F;
            default -> 0.78F;
        };
    }

    private static double getChainEndTipBackOffset(String abilityId) {
        return switch (abilityId) {
            case "holy_chain" -> 0.08D;
            case "chain_jail", "judgment_chain", "steal_chain" -> 0.04D;
            default -> 0.02D;
        };
    }

    private static void renderTrackedChainTether(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, Vec3 camera, float partialTick, Player owner, LivingEntity target, String abilityId) {
        SharedChainProjectileModel<?> model = getSharedChainModel();
        if (model == null) {
            return;
        }
        Vec3 start = getRightHandAnchor(owner, partialTick);
        Vec3 end = getEntityCenter(target, partialTick).add(0.0D, target.getBbHeight() * 0.08D, 0.0D);
        renderChainBetween(poseStack, bufferSource.getBuffer(RenderType.entityCutoutNoCull(CHAIN_TEXTURE)), camera, start, end, 0.95F, model, owner.tickCount + partialTick);
        renderChainEndTip(poseStack, bufferSource, camera, start, end, abilityId);
    }

    private static void renderLiveChainProjectileTether(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, Vec3 camera, float partialTick, SmokeyChainProjectileEntity projectile) {
        Player owner = projectile.getOwnerPlayer();
        SharedChainProjectileModel<?> model = getSharedChainModel();
        if (owner == null || model == null || "smokey_chain".equals(projectile.getAbilityId())) {
            return;
        }
        Vec3 start = getRightHandAnchor(owner, partialTick);
        Vec3 end = projectile.getPosition(partialTick);
        renderChainBetween(poseStack, bufferSource.getBuffer(RenderType.entityCutoutNoCull(CHAIN_TEXTURE)), camera, start, end, 0.95F, model, projectile.tickCount + partialTick);
        renderChainEndTip(poseStack, bufferSource, camera, start, end, projectile.getAbilityId());
    }

    private static void renderChainWrapLinks(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, Vec3 camera, float partialTick, LivingEntity entity) {
        SharedChainProjectileModel<?> model = getSharedChainModel();
        if (model == null) {
            return;
        }
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(CHAIN_TEXTURE));
        Vec3 center = getEntityCenter(entity, partialTick);
        double x = center.x - camera.x;
        double y = center.y - camera.y;
        double z = center.z - camera.z;
        float bodyYaw = Mth.rotLerp(partialTick, entity.yBodyRotO, entity.yBodyRot);
        float radius = Math.max(0.48F, entity.getBbWidth() * 0.86F);
        float height = Math.max(1.05F, entity.getBbHeight() * 0.76F);
        float linkScale = Mth.clamp(Math.max(entity.getBbWidth(), entity.getBbHeight()) * 0.32F, 0.72F, 1.18F);
        float time = entity.tickCount + partialTick;

        poseStack.pushPose();
        poseStack.translate(x, y, z);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - bodyYaw));
        renderChainBand(poseStack, consumer, model, radius, height, -24.0F, 0.0F, time, linkScale);
        renderChainBand(poseStack, consumer, model, radius * 1.04F, height, 24.0F, Mth.PI, time, linkScale);
        renderChainBand(poseStack, consumer, model, radius * 0.98F, height * 0.52F, 0.0F, time * 0.12F, time, linkScale * 0.92F);
        poseStack.popPose();
    }

    private static void renderChainBand(PoseStack poseStack, VertexConsumer consumer, SharedChainProjectileModel<?> model, float radius, float height, float tiltDegrees, float offset, float time, float linkScale) {
        int links = Math.max(18, Mth.ceil((radius * Mth.TWO_PI) / Math.max(0.18F, SharedChainProjectileModel.SEGMENT_SPACING * linkScale)));
        for (int i = 0; i < links; i++) {
            float t = i / (float) links;
            float angle = (t * Mth.TWO_PI) + offset;
            float wave = Mth.sin(time * 0.09F + i * 0.37F) * 0.015F;
            float y = Mth.lerp(t, height * 0.5F, -height * 0.5F);
            if (tiltDegrees == 0.0F) {
                y = -height * 0.14F + Mth.sin(angle * 2.0F) * 0.08F;
            }
            poseStack.pushPose();
            poseStack.translate(Mth.sin(angle) * (radius + wave), y, Mth.cos(angle) * (radius + wave));
            poseStack.mulPose(Axis.YP.rotation(angle + Mth.HALF_PI));
            poseStack.mulPose(Axis.ZP.rotationDegrees(tiltDegrees == 0.0F ? 90.0F : 90.0F + tiltDegrees));
            poseStack.scale(linkScale, linkScale, linkScale);
            model.renderSegment(poseStack, consumer, 15728880, OverlayTexture.NO_OVERLAY, i);
            poseStack.popPose();
        }
    }

    private static void renderChainBetween(PoseStack poseStack, VertexConsumer consumer, Vec3 camera, Vec3 start, Vec3 end, float scale, SharedChainProjectileModel<?> model, float time) {
        Vec3 delta = end.subtract(start);
        float length = (float) delta.length();
        if (length <= 0.05F) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(start.x - camera.x, start.y - camera.y, start.z - camera.z);
        float yaw = (float) Math.atan2(delta.x, delta.z);
        float pitch = (float) Math.atan2(delta.y, Math.sqrt((delta.x * delta.x) + (delta.z * delta.z)));
        poseStack.mulPose(Axis.YP.rotation(yaw));
        poseStack.mulPose(Axis.XP.rotation(-pitch));
        int segments = Math.max(2, Mth.ceil(length / (SharedChainProjectileModel.SEGMENT_SPACING * scale)) + 1);
        for (int i = 0; i < segments; i++) {
            float z = Math.min(length, i * SharedChainProjectileModel.SEGMENT_SPACING * scale);
            float sway = Mth.sin(time * 0.22F + i * 0.64F) * 0.018F;
            poseStack.pushPose();
            poseStack.translate(sway, Mth.cos(time * 0.18F + i * 0.41F) * 0.01F, z);
            poseStack.scale(scale, scale, scale);
            model.renderSegment(poseStack, consumer, 15728880, OverlayTexture.NO_OVERLAY, i);
            poseStack.popPose();
        }
        poseStack.popPose();
    }

    private static Vec3 getRightHandAnchor(Player player, float partialTick) {
        double x = Mth.lerp(partialTick, player.xOld, player.getX());
        double y = Mth.lerp(partialTick, player.yOld, player.getY());
        double z = Mth.lerp(partialTick, player.zOld, player.getZ());
        float yaw = Mth.rotLerp(partialTick, player.yBodyRotO, player.yBodyRot);
        double radians = Math.toRadians(yaw);
        double rightX = -Math.cos(radians) * 0.36D;
        double rightZ = -Math.sin(radians) * 0.36D;
        Vec3 look = player.getLookAngle();
        return new Vec3(x + rightX + look.x * 0.22D, y + 1.05D + (player.isCrouching() ? -0.16D : 0.0D), z + rightZ + look.z * 0.22D);
    }

    private static Vec3 getEntityCenter(LivingEntity entity, float partialTick) {
        return new Vec3(
                Mth.lerp(partialTick, entity.xOld, entity.getX()),
                Mth.lerp(partialTick, entity.yOld, entity.getY()) + (entity.getBbHeight() * 0.5D),
                Mth.lerp(partialTick, entity.zOld, entity.getZ())
        );
    }

    private static LivingEntity resolveLivingByUuid(ClientLevel level, String uuidString) {
        if (uuidString == null || uuidString.isBlank()) {
            return null;
        }
        try {
            UUID uuid = UUID.fromString(uuidString);
            for (Entity entity : level.entitiesForRendering()) {
                if (entity instanceof LivingEntity living && living.getUUID().equals(uuid)) {
                    return living;
                }
            }
        } catch (IllegalArgumentException ignored) {
        }
        return null;
    }

    private static void renderHolyChainOverlay(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, Vec3 camera, float partialTick, AbstractClientPlayer player, PlayerModel<AbstractClientPlayer> model) {
        SharedChainProjectileModel<?> chainModel = getSharedChainModel();
        setupModelPose(model, player, partialTick);
        float time = player.tickCount + partialTick;
        float pulse = 0.5F + (Mth.sin(time * 0.34F) * 0.5F);
        float red = 0.12F;
        float green = 1.0F;
        float blue = 0.42F;

        poseStack.pushPose();
        applyAuraTransform(poseStack, camera, player, partialTick, 1.018F);
        VertexConsumer auraConsumer = bufferSource.getBuffer(RenderType.lightning());
        renderHolyChainHandAura(model.rightArm, poseStack, auraConsumer, time, 0.08F, 1.0F, 0.36F, 0.96F);
        if (chainModel != null) {
            VertexConsumer chainConsumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(CHAIN_TEXTURE));
            renderHolyChainLinks(model.rightArm, poseStack, chainConsumer, time, chainModel);
        }
        poseStack.popPose();
    }

    private static void renderAuraCoating(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, Vec3 camera, float partialTick, AbstractClientPlayer player, PlayerModel<AbstractClientPlayer> model, float red, float green, float blue, HunterPlayerData data) {
        boolean bodyAura = NenTechniqueAbility.hasAnyVisibleBodyAura(data);
        boolean fistAura = NenTechniqueAbility.hasAnyVisibleFistAura(data);
        if (!bodyAura && !fistAura) {
            return;
        }

        setupModelPose(model, player, partialTick);

        if (bodyAura) {
            poseStack.pushPose();
            applyAuraTransform(poseStack, camera, player, partialTick, 1.008F);
            VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityTranslucent(AURA_TEXTURE));
            renderBodyParts(model, poseStack, consumer, red, green, blue, getBodyAuraAlpha(data));
            if (data.isKenActive()) {
                VertexConsumer outerConsumer = bufferSource.getBuffer(RenderType.entityTranslucent(AURA_TEXTURE));
                renderBodyParts(model, poseStack, outerConsumer, red, green, blue, getBodyAuraAlpha(data) * 0.34F);
            }
            poseStack.popPose();
        }

        if (fistAura) {
            poseStack.pushPose();
            applyAuraTransform(poseStack, camera, player, partialTick, bodyAura ? 1.012F : 1.01F);
            if (data.isKoActive()) {
                VertexConsumer consumer = bufferSource.getBuffer(RenderType.lightning());
                renderAnchoredKoAura(model, poseStack, consumer, player.tickCount + partialTick, data, brighten(red), brighten(green), brighten(blue));
            } else if (data.isRenActive()) {
                VertexConsumer consumer = bufferSource.getBuffer(RenderType.lightning());
                renderAnchoredRenAura(model, poseStack, consumer, player.tickCount + partialTick, data, brighten(red), brighten(green), brighten(blue));
            } else if (data.isKenActive()) {
                VertexConsumer consumer = bufferSource.getBuffer(RenderType.lightning());
                renderAnchoredKenAura(model, poseStack, consumer, player.tickCount + partialTick, data, brighten(red), brighten(green), brighten(blue));
            } else {
                poseStack.translate(0.0D, -0.04D, 0.0D);
                VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityTranslucent(AURA_TEXTURE));
                renderArmParts(model, poseStack, consumer, brighten(red), brighten(green), brighten(blue), getFistAuraAlpha(data));
            }
            poseStack.popPose();
        }
    }

    private static void renderScarletEyes(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, Vec3 camera, float partialTick, AbstractClientPlayer player, PlayerModel<AbstractClientPlayer> model, HunterPlayerData data) {
        setupModelPose(model, player, partialTick);
        poseStack.pushPose();
        applyAuraTransform(poseStack, camera, player, partialTick, 1.002F);
        model.head.translateAndRotate(poseStack);
        poseStack.translate(0.0D, 0.0D, -0.255D);
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityTranslucent(SCARLET_EYES_TEXTURE));
        renderEyeQuad(poseStack, consumer,
                data.getScarletLeftEyeOffsetX() * 0.03125F,
                -0.25F + (data.getScarletLeftEyeOffsetY() * 0.03125F),
                data.getScarletLeftEyeLength() * 0.0625F,
                data.getScarletLeftEyeVerticalLength() * 0.0625F);
        renderEyeQuad(poseStack, consumer,
                data.getScarletRightEyeOffsetX() * 0.03125F,
                -0.25F + (data.getScarletRightEyeOffsetY() * 0.03125F),
                data.getScarletRightEyeLength() * 0.0625F,
                data.getScarletRightEyeVerticalLength() * 0.0625F);
        poseStack.popPose();
    }

    private static void renderWingAuraCoating(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, Vec3 camera, float partialTick, WingEntity wing, PlayerModel<WingEntity> model, float red, float green, float blue, HunterPlayerData data) {
        boolean bodyAura = NenTechniqueAbility.hasAnyVisibleBodyAura(data);
        boolean fistAura = NenTechniqueAbility.hasAnyVisibleFistAura(data);
        if (!bodyAura && !fistAura) {
            return;
        }

        setupWingModelPose(model, wing, partialTick);

        if (bodyAura) {
            poseStack.pushPose();
            applyAuraTransform(poseStack, camera, wing, partialTick, 1.008F);
            VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityTranslucent(AURA_TEXTURE));
            renderBodyParts(model, poseStack, consumer, red, green, blue, getBodyAuraAlpha(data));
            if (data.isKenActive()) {
                VertexConsumer outerConsumer = bufferSource.getBuffer(RenderType.entityTranslucent(AURA_TEXTURE));
                renderBodyParts(model, poseStack, outerConsumer, red, green, blue, getBodyAuraAlpha(data) * 0.34F);
            }
            poseStack.popPose();
        }

        if (fistAura) {
            poseStack.pushPose();
            applyAuraTransform(poseStack, camera, wing, partialTick, bodyAura ? 1.012F : 1.01F);
            if (data.isKoActive()) {
                VertexConsumer consumer = bufferSource.getBuffer(RenderType.lightning());
                renderAnchoredKoAura(model, poseStack, consumer, wing.tickCount + partialTick, data, brighten(red), brighten(green), brighten(blue));
            } else if (data.isKenActive()) {
                VertexConsumer consumer = bufferSource.getBuffer(RenderType.lightning());
                renderAnchoredKenAura(model, poseStack, consumer, wing.tickCount + partialTick, data, brighten(red), brighten(green), brighten(blue));
            } else {
                poseStack.translate(0.0D, -0.04D, 0.0D);
                VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityTranslucent(AURA_TEXTURE));
                renderArmParts(model, poseStack, consumer, brighten(red), brighten(green), brighten(blue), getFistAuraAlpha(data));
            }
            poseStack.popPose();
        }
    }

    private static <T extends LivingEntity> void renderConstructAuraCoating(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, Vec3 camera, float partialTick, T entity, PlayerModel<T> model, float red, float green, float blue, HunterPlayerData data) {
        boolean bodyAura = NenTechniqueAbility.hasAnyVisibleBodyAura(data);
        boolean fistAura = NenTechniqueAbility.hasAnyVisibleFistAura(data);
        if (!bodyAura && !fistAura) {
            return;
        }

        setupConstructModelPose(model, entity, partialTick);

        if (bodyAura) {
            poseStack.pushPose();
            applyAuraTransform(poseStack, camera, entity, partialTick, 1.008F);
            VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityTranslucent(AURA_TEXTURE));
            renderBodyParts(model, poseStack, consumer, red, green, blue, getBodyAuraAlpha(data));
            if (data.isKenActive()) {
                VertexConsumer outerConsumer = bufferSource.getBuffer(RenderType.entityTranslucent(AURA_TEXTURE));
                renderBodyParts(model, poseStack, outerConsumer, red, green, blue, getBodyAuraAlpha(data) * 0.34F);
            }
            poseStack.popPose();
        }

        if (fistAura) {
            poseStack.pushPose();
            applyAuraTransform(poseStack, camera, entity, partialTick, bodyAura ? 1.012F : 1.01F);
            if (data.isKoActive()) {
                VertexConsumer consumer = bufferSource.getBuffer(RenderType.lightning());
                renderAnchoredKoAura(model, poseStack, consumer, entity.tickCount + partialTick, data, brighten(red), brighten(green), brighten(blue));
            } else if (data.isKenActive()) {
                VertexConsumer consumer = bufferSource.getBuffer(RenderType.lightning());
                renderAnchoredKenAura(model, poseStack, consumer, entity.tickCount + partialTick, data, brighten(red), brighten(green), brighten(blue));
            } else {
                poseStack.translate(0.0D, -0.04D, 0.0D);
                VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityTranslucent(AURA_TEXTURE));
                renderArmParts(model, poseStack, consumer, brighten(red), brighten(green), brighten(blue), getFistAuraAlpha(data));
            }
            poseStack.popPose();
        }
    }

    private static void setupModelPose(PlayerModel<AbstractClientPlayer> model, AbstractClientPlayer player, float partialTick) {
        float limbSwing = player.walkAnimation.position(partialTick);
        float limbSwingAmount = player.walkAnimation.speed(partialTick);
        model.young = false;
        model.crouching = player.isCrouching();
        model.riding = player.isPassenger();
        model.attackTime = player.getAttackAnim(partialTick);
        HumanoidModel.ArmPose mainPose = getArmPose(player, InteractionHand.MAIN_HAND);
        HumanoidModel.ArmPose offPose = getArmPose(player, InteractionHand.OFF_HAND);
        if (player.getMainArm() == HumanoidArm.RIGHT) {
            model.rightArmPose = mainPose;
            model.leftArmPose = offPose;
        } else {
            model.rightArmPose = offPose;
            model.leftArmPose = mainPose;
        }
        model.prepareMobModel(player, limbSwing, limbSwingAmount, partialTick);
        float animationTime = player.tickCount + partialTick;
        float bodyYaw = Mth.rotLerp(partialTick, player.yBodyRotO, player.yBodyRot);
        float headYaw = Mth.rotLerp(partialTick, player.yHeadRotO, player.yHeadRot) - bodyYaw;
        float pitch = Mth.lerp(partialTick, player.xRotO, player.getXRot());
        model.setupAnim(player, limbSwing, limbSwingAmount, animationTime, headYaw, pitch);
    }

    private static void setupWingModelPose(PlayerModel<WingEntity> model, WingEntity wing, float partialTick) {
        float limbSwing = wing.walkAnimation.position(partialTick);
        float limbSwingAmount = wing.walkAnimation.speed(partialTick);
        model.young = false;
        model.crouching = wing.isCrouching();
        model.riding = wing.isPassenger();
        model.attackTime = 0.0F;
        model.rightArmPose = HumanoidModel.ArmPose.EMPTY;
        model.leftArmPose = HumanoidModel.ArmPose.EMPTY;
        model.prepareMobModel(wing, limbSwing, limbSwingAmount, partialTick);
        float animationTime = wing.tickCount + partialTick;
        float bodyYaw = Mth.rotLerp(partialTick, wing.yBodyRotO, wing.yBodyRot);
        float headYaw = Mth.rotLerp(partialTick, wing.yHeadRotO, wing.yHeadRot) - bodyYaw;
        float pitch = Mth.lerp(partialTick, wing.xRotO, wing.getXRot());
        model.setupAnim(wing, limbSwing, limbSwingAmount, animationTime, headYaw, pitch);
    }

    private static <T extends LivingEntity> void setupConstructModelPose(PlayerModel<T> model, T entity, float partialTick) {
        float limbSwing = entity.walkAnimation.position(partialTick);
        float limbSwingAmount = entity.walkAnimation.speed(partialTick);
        model.young = false;
        model.crouching = entity.isCrouching();
        model.riding = entity.isPassenger();
        model.attackTime = entity.getAttackAnim(partialTick);
        HumanoidModel.ArmPose mainPose = getArmPose(entity, InteractionHand.MAIN_HAND);
        HumanoidModel.ArmPose offPose = getArmPose(entity, InteractionHand.OFF_HAND);
        if (entity.getMainArm() == HumanoidArm.RIGHT) {
            model.rightArmPose = mainPose;
            model.leftArmPose = offPose;
        } else {
            model.rightArmPose = offPose;
            model.leftArmPose = mainPose;
        }
        model.prepareMobModel(entity, limbSwing, limbSwingAmount, partialTick);
        float animationTime = entity.tickCount + partialTick;
        float bodyYaw = Mth.rotLerp(partialTick, entity.yBodyRotO, entity.yBodyRot);
        float headYaw = Mth.rotLerp(partialTick, entity.yHeadRotO, entity.yHeadRot) - bodyYaw;
        float pitch = Mth.lerp(partialTick, entity.xRotO, entity.getXRot());
        model.setupAnim(entity, limbSwing, limbSwingAmount, animationTime, headYaw, pitch);
    }

    private static HumanoidModel.ArmPose getArmPose(AbstractClientPlayer player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.isEmpty()) {
            return HumanoidModel.ArmPose.EMPTY;
        }
        if (player.getUsedItemHand() == hand && player.getUseItemRemainingTicks() > 0) {
            UseAnim useAnim = stack.getUseAnimation();
            return switch (useAnim) {
                case BLOCK -> HumanoidModel.ArmPose.BLOCK;
                case BOW -> HumanoidModel.ArmPose.BOW_AND_ARROW;
                case SPEAR -> HumanoidModel.ArmPose.THROW_SPEAR;
                case CROSSBOW -> hand == player.getUsedItemHand()
                        ? HumanoidModel.ArmPose.CROSSBOW_CHARGE
                        : HumanoidModel.ArmPose.ITEM;
                case SPYGLASS -> HumanoidModel.ArmPose.SPYGLASS;
                case TOOT_HORN -> HumanoidModel.ArmPose.TOOT_HORN;
                case BRUSH -> HumanoidModel.ArmPose.BRUSH;
                default -> HumanoidModel.ArmPose.ITEM;
            };
        }
        if (!player.swinging && stack.getItem() instanceof CrossbowItem && CrossbowItem.isCharged(stack)) {
            return HumanoidModel.ArmPose.CROSSBOW_HOLD;
        }
        return HumanoidModel.ArmPose.ITEM;
    }

    private static HumanoidModel.ArmPose getArmPose(LivingEntity entity, InteractionHand hand) {
        ItemStack stack = entity.getItemInHand(hand);
        if (stack.isEmpty()) {
            return HumanoidModel.ArmPose.EMPTY;
        }
        if (entity.getUsedItemHand() == hand && entity.getUseItemRemainingTicks() > 0) {
            UseAnim useAnim = stack.getUseAnimation();
            return switch (useAnim) {
                case BLOCK -> HumanoidModel.ArmPose.BLOCK;
                case BOW -> HumanoidModel.ArmPose.BOW_AND_ARROW;
                case SPEAR -> HumanoidModel.ArmPose.THROW_SPEAR;
                case CROSSBOW -> hand == entity.getUsedItemHand()
                        ? HumanoidModel.ArmPose.CROSSBOW_CHARGE
                        : HumanoidModel.ArmPose.ITEM;
                case SPYGLASS -> HumanoidModel.ArmPose.SPYGLASS;
                case TOOT_HORN -> HumanoidModel.ArmPose.TOOT_HORN;
                case BRUSH -> HumanoidModel.ArmPose.BRUSH;
                default -> HumanoidModel.ArmPose.ITEM;
            };
        }
        if (stack.getItem() instanceof CrossbowItem && CrossbowItem.isCharged(stack)) {
            return HumanoidModel.ArmPose.CROSSBOW_HOLD;
        }
        return HumanoidModel.ArmPose.ITEM;
    }

    private static void applyAuraTransform(PoseStack poseStack, Vec3 camera, LivingEntity entity, float partialTick, float scale) {
        double x = Mth.lerp(partialTick, entity.xOld, entity.getX());
        double y = Mth.lerp(partialTick, entity.yOld, entity.getY());
        double z = Mth.lerp(partialTick, entity.zOld, entity.getZ());
        float bodyYaw = Mth.rotLerp(partialTick, entity.yBodyRotO, entity.yBodyRot);
        poseStack.translate(x - camera.x, y - camera.y, z - camera.z);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - bodyYaw));
        poseStack.scale(-scale, -scale, scale);
        poseStack.translate(0.0D, -1.39D, 0.0D);
    }

    private static <T extends LivingEntity> void renderBodyParts(PlayerModel<T> model, PoseStack poseStack, VertexConsumer consumer, float red, float green, float blue, float alpha) {
        model.head.render(poseStack, consumer, 15728880, OverlayTexture.NO_OVERLAY, red, green, blue, alpha);
        model.hat.render(poseStack, consumer, 15728880, OverlayTexture.NO_OVERLAY, red, green, blue, alpha);
        model.body.render(poseStack, consumer, 15728880, OverlayTexture.NO_OVERLAY, red, green, blue, alpha);
        model.jacket.render(poseStack, consumer, 15728880, OverlayTexture.NO_OVERLAY, red, green, blue, alpha);
        renderArmParts(model, poseStack, consumer, red, green, blue, alpha);
        model.rightLeg.render(poseStack, consumer, 15728880, OverlayTexture.NO_OVERLAY, red, green, blue, alpha);
        model.leftLeg.render(poseStack, consumer, 15728880, OverlayTexture.NO_OVERLAY, red, green, blue, alpha);
        model.rightPants.render(poseStack, consumer, 15728880, OverlayTexture.NO_OVERLAY, red, green, blue, alpha);
        model.leftPants.render(poseStack, consumer, 15728880, OverlayTexture.NO_OVERLAY, red, green, blue, alpha);
    }

    private static <T extends LivingEntity> void renderArmParts(PlayerModel<T> model, PoseStack poseStack, VertexConsumer consumer, float red, float green, float blue, float alpha) {
        model.rightArm.render(poseStack, consumer, 15728880, OverlayTexture.NO_OVERLAY, red, green, blue, alpha);
        model.leftArm.render(poseStack, consumer, 15728880, OverlayTexture.NO_OVERLAY, red, green, blue, alpha);
        model.rightSleeve.render(poseStack, consumer, 15728880, OverlayTexture.NO_OVERLAY, red, green, blue, alpha);
        model.leftSleeve.render(poseStack, consumer, 15728880, OverlayTexture.NO_OVERLAY, red, green, blue, alpha);
    }

    private static <T extends LivingEntity> void renderAnchoredRenAura(PlayerModel<T> model, PoseStack poseStack, VertexConsumer consumer, float time, HunterPlayerData data, float red, float green, float blue) {
        float level = Mth.clamp(data.getNenLevel() / 10.0F, 0.1F, 1.0F);
        float alpha = 0.54F + level * 0.32F;
        renderRenArmAura(model.rightArm, poseStack, consumer, time, level, red, green, blue, alpha, 0.0F);
        renderRenArmAura(model.leftArm, poseStack, consumer, time, level, red, green, blue, alpha, 2.7F);
    }

    private static <T extends LivingEntity> void renderAnchoredKenAura(PlayerModel<T> model, PoseStack poseStack, VertexConsumer consumer, float time, HunterPlayerData data, float red, float green, float blue) {
        float level = Mth.clamp(data.getNenLevel() / 10.0F, 0.1F, 1.0F);
        float kenLevel = 0.42F + level * 0.28F;
        float alpha = 0.28F + level * 0.18F;
        renderRenArmAura(model.rightArm, poseStack, consumer, time * 0.82F, kenLevel, red, green, blue, alpha, 0.0F);
        renderRenArmAura(model.leftArm, poseStack, consumer, time * 0.82F, kenLevel, red, green, blue, alpha, 2.7F);
    }

    private static <T extends LivingEntity> void renderAnchoredKoAura(PlayerModel<T> model, PoseStack poseStack, VertexConsumer consumer, float time, HunterPlayerData data, float red, float green, float blue) {
        float level = Mth.clamp(data.getNenLevel() / 10.0F, 0.2F, 1.0F);
        renderKoHandAura(model.rightArm, poseStack, consumer, time, level, red, green, blue, 0.92F + level * 0.34F);
    }

    private static void renderKoHandAura(ModelPart arm, PoseStack poseStack, VertexConsumer consumer, float time, float level, float red, float green, float blue, float alpha) {
        poseStack.pushPose();
        arm.translateAndRotate(poseStack);
        Matrix4f matrix = poseStack.last().pose();

        renderRenHandGlow(matrix, consumer, 0.0F, 0.7F, 0.0F, 0.22F + level * 0.09F, mix(red, 1.0F, 0.45F), mix(green, 1.0F, 0.45F), mix(blue, 1.0F, 0.45F), alpha * 0.72F);
        renderRenHandGlow(matrix, consumer, 0.0F, 0.64F, 0.0F, 0.34F + level * 0.12F, red, green, blue, alpha * 0.34F);

        int strands = 34 + Math.round(level * 20.0F);
        for (int i = 0; i < strands; i++) {
            float angle = i * Mth.TWO_PI / strands + time * (0.34F + level * 0.14F);
            float swirl = angle + Mth.sin(time * 0.21F + i * 0.61F) * 0.34F;
            float radius = 0.22F + level * 0.16F;
            float height = 0.5F + (i % 6) * 0.055F + level * 0.34F;
            float x0 = Mth.sin(swirl) * radius * 0.34F;
            float z0 = Mth.cos(swirl) * radius * 0.34F;
            float x1 = Mth.sin(swirl + 0.86F) * (radius + 0.16F);
            float z1 = Mth.cos(swirl + 0.86F) * (radius + 0.16F);
            float y0 = 0.76F + Mth.sin(time * 0.28F + i) * 0.035F;
            float y1 = 0.62F - height;
            float core = i % 2 == 0 ? 0.55F : 0.18F;
            renderRenWisp(matrix, consumer, x0, y0, z0, x1, y1, z1, 0.055F + level * 0.04F, mix(red, 1.0F, core), mix(green, 1.0F, core), mix(blue, 1.0F, core), alpha * 0.88F);
        }

        for (int i = 0; i < 9; i++) {
            float angle = i * Mth.TWO_PI / 9.0F - time * 0.48F;
            float radius = 0.13F + level * 0.07F;
            renderRenHandGlow(matrix, consumer, Mth.sin(angle) * radius, 0.68F - i * 0.026F, Mth.cos(angle) * radius, 0.06F + level * 0.03F, red, green, blue, alpha * 0.5F);
        }
        poseStack.popPose();
    }

    private static void renderHolyChainHandAura(ModelPart arm, PoseStack poseStack, VertexConsumer consumer, float time, float red, float green, float blue, float alpha) {
        poseStack.pushPose();
        arm.translateAndRotate(poseStack);
        Matrix4f matrix = poseStack.last().pose();

        float pulse = 0.5F + Mth.sin(time * 0.55F) * 0.5F;
        renderRenHandGlow(matrix, consumer, 0.0F, 0.72F, 0.0F, 0.18F + pulse * 0.05F, 0.66F, 1.0F, 0.78F, alpha * 0.88F);
        renderRenHandGlow(matrix, consumer, 0.0F, 0.55F, 0.0F, 0.27F + pulse * 0.08F, red, green, blue, alpha * 0.48F);

        for (int i = 0; i < 36; i++) {
            float angle = i * Mth.TWO_PI / 36.0F + time * 0.46F;
            float coil = angle + Mth.sin(time * 0.2F + i * 0.41F) * 0.22F;
            float radius = 0.16F + Mth.sin(time * 0.18F + i) * 0.025F;
            float height = 0.16F + (i % 9) * 0.055F;
            float x0 = Mth.sin(coil) * radius * 0.32F;
            float z0 = Mth.cos(coil) * radius * 0.32F;
            float x1 = Mth.sin(coil + 0.68F) * (radius + 0.18F);
            float z1 = Mth.cos(coil + 0.68F) * (radius + 0.18F);
            float y0 = 0.72F - height * 0.4F;
            float y1 = 0.56F - height;
            float core = i % 3 == 0 ? 0.62F : 0.18F;
            renderRenWisp(matrix, consumer, x0, y0, z0, x1, y1, z1, 0.042F, mix(red, 1.0F, core), 1.0F, mix(blue, 1.0F, core), alpha * 0.78F);
        }

        for (int i = 0; i < 10; i++) {
            float angle = i * Mth.TWO_PI / 10.0F - time * 0.62F;
            renderRenHandGlow(matrix, consumer, Mth.sin(angle) * 0.13F, 0.66F - i * 0.022F, Mth.cos(angle) * 0.13F, 0.055F, 0.76F, 1.0F, 0.82F, alpha * 0.54F);
        }
        poseStack.popPose();
    }

    private static void renderHolyChainLinks(ModelPart arm, PoseStack poseStack, VertexConsumer consumer, float time, SharedChainProjectileModel<?> model) {
        poseStack.pushPose();
        arm.translateAndRotate(poseStack);
        for (int i = 0; i < 24; i++) {
            float t = i / 23.0F;
            float angle = (t * Mth.TWO_PI * 2.4F) - time * 0.34F;
            float radius = 0.17F + Mth.sin(time * 0.28F + i * 0.7F) * 0.018F;
            float y = 0.72F - (t * 0.66F);
            poseStack.pushPose();
            poseStack.translate(Mth.sin(angle) * radius, y, Mth.cos(angle) * radius);
            poseStack.mulPose(Axis.YP.rotation(angle + Mth.HALF_PI));
            poseStack.mulPose(Axis.ZP.rotationDegrees(90.0F));
            poseStack.scale(0.46F, 0.46F, 0.46F);
            model.renderSegment(poseStack, consumer, 15728880, OverlayTexture.NO_OVERLAY, i);
            poseStack.popPose();
        }
        poseStack.popPose();
    }

    private static void renderRenArmAura(ModelPart arm, PoseStack poseStack, VertexConsumer consumer, float time, float level, float red, float green, float blue, float alpha, float offset) {
        poseStack.pushPose();
        arm.translateAndRotate(poseStack);
        Matrix4f matrix = poseStack.last().pose();

        renderRenHandGlow(matrix, consumer, 0.0F, 0.69F, 0.0F, 0.105F + level * 0.04F, red, green, blue, alpha * 0.5F);
        for (int i = 0; i < 16 + Math.round(level * 10.0F); i++) {
            float angle = i * Mth.TWO_PI / (16.0F + level * 10.0F) + time * (0.18F + level * 0.08F) + offset;
            float radius = 0.16F + level * 0.08F;
            float height = 0.28F + (i % 5) * 0.05F + level * 0.18F;
            float x0 = Mth.sin(angle) * radius * 0.45F;
            float z0 = Mth.cos(angle) * radius * 0.45F;
            float x1 = Mth.sin(angle + 0.64F) * (radius + 0.08F);
            float z1 = Mth.cos(angle + 0.64F) * (radius + 0.08F);
            float y0 = 0.74F + Mth.sin(time * 0.18F + i) * 0.025F;
            float y1 = 0.66F - height;
            float core = i % 3 == 0 ? 0.45F : 0.0F;
            renderRenWisp(matrix, consumer, x0, y0, z0, x1, y1, z1, 0.035F + level * 0.022F, mix(red, 1.0F, core), mix(green, 1.0F, core), mix(blue, 1.0F, core), alpha * 0.72F);
        }
        for (int i = 0; i < 5; i++) {
            float angle = i * Mth.TWO_PI / 5.0F - time * 0.24F + offset;
            renderRenHandGlow(matrix, consumer, Mth.sin(angle) * 0.1F, 0.68F - i * 0.035F, Mth.cos(angle) * 0.1F, 0.035F + level * 0.018F, red, green, blue, alpha * 0.42F);
        }
        poseStack.popPose();
    }

    private static void renderRenWisp(Matrix4f matrix, VertexConsumer consumer, float x0, float y0, float z0, float x1, float y1, float z1, float halfWidth, float red, float green, float blue, float alpha) {
        int segments = 5;
        for (int i = 0; i < segments; i++) {
            float t0 = i / (float) segments;
            float t1 = (i + 1) / (float) segments;
            float c0 = Mth.sin(t0 * Mth.PI);
            float c1 = Mth.sin(t1 * Mth.PI);
            float px0 = Mth.lerp(t0, x0, x1) + c0 * 0.035F * Mth.sin(t0 * 7.0F + x0 * 9.0F);
            float py0 = Mth.lerp(t0, y0, y1);
            float pz0 = Mth.lerp(t0, z0, z1) + c0 * 0.035F * Mth.cos(t0 * 7.0F + z0 * 9.0F);
            float px1 = Mth.lerp(t1, x0, x1) + c1 * 0.035F * Mth.sin(t1 * 7.0F + x0 * 9.0F);
            float py1 = Mth.lerp(t1, y0, y1);
            float pz1 = Mth.lerp(t1, z0, z1) + c1 * 0.035F * Mth.cos(t1 * 7.0F + z0 * 9.0F);
            float w0 = halfWidth * auraTaper(t0);
            float w1 = halfWidth * auraTaper(t1);
            float a0 = alpha * (1.0F - t0) * auraTaper(t0);
            float a1 = alpha * (1.0F - t1) * auraTaper(t1);
            renderRenSegment(matrix, consumer, px0, py0, pz0, px1, py1, pz1, w0, w1, red, green, blue, a0, a1);
        }
    }

    private static void renderRenSegment(Matrix4f matrix, VertexConsumer consumer, float x0, float y0, float z0, float x1, float y1, float z1, float w0, float w1, float red, float green, float blue, float alpha0, float alpha1) {
        float dx = x1 - x0;
        float dz = z1 - z0;
        float length = Mth.sqrt(dx * dx + dz * dz);
        float nx = length > 1.0E-4F ? -dz / length : 1.0F;
        float nz = length > 1.0E-4F ? dx / length : 0.0F;
        vertexAura(matrix, consumer, x0 - nx * w0, y0, z0 - nz * w0, red, green, blue, alpha0 * 0.2F);
        vertexAura(matrix, consumer, x0 + nx * w0, y0, z0 + nz * w0, red, green, blue, alpha0);
        vertexAura(matrix, consumer, x1 + nx * w1, y1, z1 + nz * w1, red, green, blue, alpha1);
        vertexAura(matrix, consumer, x1 - nx * w1, y1, z1 - nz * w1, red, green, blue, alpha1 * 0.2F);
    }

    private static void renderRenHandGlow(Matrix4f matrix, VertexConsumer consumer, float x, float y, float z, float size, float red, float green, float blue, float alpha) {
        vertexAura(matrix, consumer, x - size, y, z, red, green, blue, 0.0F);
        vertexAura(matrix, consumer, x, y + size, z, red, green, blue, alpha);
        vertexAura(matrix, consumer, x + size, y, z, red, green, blue, 0.0F);
        vertexAura(matrix, consumer, x, y - size, z, red, green, blue, alpha * 0.55F);
    }

    private static float auraTaper(float t) {
        return Mth.clamp(Mth.sin(t * Mth.PI) * 1.25F, 0.0F, 1.0F);
    }

    private static float mix(float a, float b, float t) {
        return a + (b - a) * Mth.clamp(t, 0.0F, 1.0F);
    }

    private static void vertexAura(Matrix4f matrix, VertexConsumer consumer, float x, float y, float z, float red, float green, float blue, float alpha) {
        consumer.vertex(matrix, x, y, z).color(red, green, blue, alpha).endVertex();
    }

    private static void renderEyeQuad(PoseStack poseStack, VertexConsumer consumer, float x, float y, float width, float height) {
        Matrix4f matrix = poseStack.last().pose();
        Matrix3f normal = poseStack.last().normal();
        float x2 = x + width;
        float y2 = y + height;
        consumer.vertex(matrix, x, y, 0.0F).color(255, 0, 0, 145).uv(0.0F, 1.0F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(normal, 0.0F, 0.0F, -1.0F).endVertex();
        consumer.vertex(matrix, x2, y, 0.0F).color(255, 0, 0, 145).uv(1.0F, 1.0F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(normal, 0.0F, 0.0F, -1.0F).endVertex();
        consumer.vertex(matrix, x2, y2, 0.0F).color(255, 0, 0, 145).uv(1.0F, 0.0F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(normal, 0.0F, 0.0F, -1.0F).endVertex();
        consumer.vertex(matrix, x, y2, 0.0F).color(255, 0, 0, 145).uv(0.0F, 0.0F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(normal, 0.0F, 0.0F, -1.0F).endVertex();
    }

    private static void renderSphereShell(PoseStack poseStack, float centerX, float centerY, float centerZ, float radius, int detail, float red, float green, float blue, float alpha, boolean insideSphere, float animationTime) {
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.depthMask(false);
        if (!insideSphere) {
            RenderSystem.disableCull();
        }
        RenderSystem.setShader(GameRenderer::getPositionColorTexLightmapShader);
        RenderSystem.setShaderTexture(0, EN_SPHERE_TEXTURE);

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();
        buffer.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP);

        Matrix4f matrix = poseStack.last().pose();
        float textureOffset = ((animationTime % 10000.0F) / 10000.0F) + 1.0F;

        float direction = insideSphere ? -radius : radius;
        for (SphereVertex vertex : EN_SPHERE_MESH) {
            vertexSphere(
                    buffer,
                    matrix,
                    centerX + (vertex.x * direction),
                    centerY + (vertex.y * direction),
                    centerZ + (vertex.z * direction),
                    red,
                    green,
                    blue,
                    alpha,
                    vertex.u + textureOffset,
                    vertex.v
            );
        }

        BufferUploader.drawWithShader(buffer.end());
        if (!insideSphere) {
            RenderSystem.enableCull();
        }
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
    }

    private static SphereVertex[] buildSphereMesh(int detail) {
        float tau = Mth.TWO_PI;
        float pi = (float) Math.PI;
        float yawStep = tau / detail;
        float pitchStep = pi / detail;
        SphereVertex[] mesh = new SphereVertex[detail * detail * 6];
        int index = 0;
        for (int yawIndex = 0; yawIndex < detail; yawIndex++) {
            float yaw0 = yawIndex * yawStep;
            float yaw1 = (yawIndex + 1 == detail) ? tau : ((yawIndex + 1) * yawStep);
            float cosYaw0 = Mth.cos(yaw0);
            float sinYaw0 = Mth.sin(yaw0);
            float cosYaw1 = Mth.cos(yaw1);
            float sinYaw1 = Mth.sin(yaw1);
            for (int pitchIndex = 0; pitchIndex < detail; pitchIndex++) {
                float pitch0 = pitchIndex * pitchStep;
                float pitch1 = (pitchIndex + 1 == detail) ? pi : ((pitchIndex + 1) * pitchStep);
                float sinPitch0 = Mth.sin(pitch0);
                float cosPitch0 = Mth.cos(pitch0);
                float sinPitch1 = Mth.sin(pitch1);
                float cosPitch1 = Mth.cos(pitch1);
                float u0 = yaw0 / tau;
                float u1 = yaw1 / tau;
                float v0 = pitch0 / pi;
                float v1 = pitch1 / pi;

                SphereVertex p00 = new SphereVertex(cosYaw0 * sinPitch0, cosPitch0, sinYaw0 * sinPitch0, u0, v0);
                SphereVertex p01 = new SphereVertex(cosYaw0 * sinPitch1, cosPitch1, sinYaw0 * sinPitch1, u0, v1);
                SphereVertex p10 = new SphereVertex(cosYaw1 * sinPitch0, cosPitch0, sinYaw1 * sinPitch0, u1, v0);
                SphereVertex p11 = new SphereVertex(cosYaw1 * sinPitch1, cosPitch1, sinYaw1 * sinPitch1, u1, v1);

                mesh[index++] = p00;
                mesh[index++] = p10;
                mesh[index++] = p01;
                mesh[index++] = p11;
                mesh[index++] = p01;
                mesh[index++] = p10;
            }
        }
        return mesh;
    }

    private static void clearTrackedEnHighlights(ClientLevel level) {
        if (level == null || TRACKED_EN_ENTITIES.isEmpty()) {
            TRACKED_EN_ENTITIES.clear();
            return;
        }
        for (int entityId : TRACKED_EN_ENTITIES) {
            Entity entity = level.getEntity(entityId);
            if (entity != null) {
                entity.setGlowingTag(false);
            }
        }
        TRACKED_EN_ENTITIES.clear();
    }

    private static void vertexSphere(BufferBuilder buffer, Matrix4f matrix, float x, float y, float z, float red, float green, float blue, float alpha, float u, float v) {
        buffer.vertex(matrix, x, y, z)
                .color((int) (red * 255.0F), (int) (green * 255.0F), (int) (blue * 255.0F), (int) (alpha * 255.0F))
                .uv(u, v)
                .uv2(15728880)
                .endVertex();
    }

    private static float getBodyAuraAlpha(HunterPlayerData data) {
        if (data.isKenActive()) {
            return 0.24F;
        }
        if (data.isRyuActive()) {
            return 0.34F;
        }
        return 0.26F;
    }

    private static float getFistAuraAlpha(HunterPlayerData data) {
        if (data.isKoActive()) {
            return 0.58F;
        }
        if (data.isKenActive()) {
            return 0.32F;
        }
        if (data.isRyuActive()) {
            return 0.46F;
        }
        return 0.4F;
    }

    private static float brighten(float channel) {
        return Math.min(1.0F, channel + 0.18F);
    }

    private static final class SphereVertex {
        private final float x;
        private final float y;
        private final float z;
        private final float u;
        private final float v;

        private SphereVertex(float x, float y, float z, float u, float v) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.u = u;
            this.v = v;
        }
    }
}
