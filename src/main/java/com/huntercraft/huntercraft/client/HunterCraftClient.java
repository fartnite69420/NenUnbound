package com.huntercraft.huntercraft.client;

import com.huntercraft.huntercraft.client.model.ChainJailEffectModel;
import com.huntercraft.huntercraft.client.model.ChainEndTipModel;
import com.huntercraft.huntercraft.client.model.GreatStampPigModel;
import com.huntercraft.huntercraft.client.model.ElasticAuraConstructModel;
import com.huntercraft.huntercraft.client.model.ElasticAuraProjectileModel;
import com.huntercraft.huntercraft.client.model.ElasticAuraShieldModel;
import com.huntercraft.huntercraft.client.particle.ElasticReflectParticle;
import com.huntercraft.huntercraft.client.particle.MorelSmokeParticle;
import com.huntercraft.huntercraft.client.particle.SmokyChainSmokeParticle;
import com.huntercraft.huntercraft.client.model.SharedChainProjectileModel;
import com.huntercraft.huntercraft.client.model.SlashEffectModel;
import com.huntercraft.huntercraft.client.render.BanditRenderer;
import com.huntercraft.huntercraft.client.render.BoxingBloodBurstRenderer;
import com.huntercraft.huntercraft.client.render.BoxingShockwaveProjectileRenderer;
import com.huntercraft.huntercraft.client.render.BungeeGumReflectRenderer;
import com.huntercraft.huntercraft.client.render.AegisSlamEffectRenderer;
import com.huntercraft.huntercraft.client.render.AnkleSweepEffectRenderer;
import com.huntercraft.huntercraft.client.render.CrossfireBarrageEffectRenderer;
import com.huntercraft.huntercraft.client.render.ChainWrapVisualRenderer;
import com.huntercraft.huntercraft.client.render.DefensePulseRenderer;
import com.huntercraft.huntercraft.client.render.DowsingChainRenderer;
import com.huntercraft.huntercraft.client.render.ElasticAuraConstructRenderer;
import com.huntercraft.huntercraft.client.render.ElasticAuraProjectileRenderer;
import com.huntercraft.huntercraft.client.render.GreatStampPigRenderer;
import com.huntercraft.huntercraft.client.render.HammerShockwaveRenderer;
import com.huntercraft.huntercraft.client.render.HeavenSplitterSlashRenderer;
import com.huntercraft.huntercraft.client.render.HunterAbilityEffectRenderer;
import com.huntercraft.huntercraft.client.render.LiverBreakEffectRenderer;
import com.huntercraft.huntercraft.client.render.MartialWhirlwindEffectRenderer;
import com.huntercraft.huntercraft.client.render.MirrorReprisalEffectRenderer;
import com.huntercraft.huntercraft.client.render.NenAuraEffectRenderer;
import com.huntercraft.huntercraft.client.render.ParrySparkEffectRenderer;
import com.huntercraft.huntercraft.client.render.RisingShotEffectRenderer;
import com.huntercraft.huntercraft.client.render.SmokeCloneRenderer;
import com.huntercraft.huntercraft.client.render.SmokeSoldierRenderer;
import com.huntercraft.huntercraft.client.render.SmokyJailBarrierRenderer;
import com.huntercraft.huntercraft.client.render.SmokeyChainProjectileRenderer;
import com.huntercraft.huntercraft.client.render.SlashEffectRenderer;
import com.huntercraft.huntercraft.client.render.SpeedBladeTrailRenderer;
import com.huntercraft.huntercraft.client.render.TonpaRenderer;
import com.huntercraft.huntercraft.client.render.ToraHuntEffectRenderer;
import com.huntercraft.huntercraft.client.render.TitanCannonWaveRenderer;
import com.huntercraft.huntercraft.client.render.WhirlwindSlashRenderer;
import com.huntercraft.huntercraft.client.render.WingRenderer;
import com.huntercraft.huntercraft.entity.HunterEntityTypes;
import com.huntercraft.huntercraft.item.HunterItems;
import com.huntercraft.huntercraft.particle.HunterParticles;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.IEventBus;

public final class HunterCraftClient {
    private HunterCraftClient() {
    }

    public static void init(IEventBus modBus) {
        modBus.addListener(ClientHooks::registerKeyMappings);
        modBus.addListener(ClientHooks::registerOverlays);
        modBus.addListener(HunterCraftClient::registerEntityRenderers);
        modBus.addListener(HunterCraftClient::registerLayerDefinitions);
        modBus.addListener(HunterCraftClient::registerParticleProviders);
        modBus.addListener(HunterCraftClient::onClientSetup);
        PlayerAbilityAnimationController.init();
        MinecraftForge.EVENT_BUS.register(ClientHooks.class);
    }

    private static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> ItemProperties.register(HunterItems.SMOKING_PIPE.get(),
                new ResourceLocation("huntercraft", "smoking"),
                HunterCraftClient::getSmokingPredicate));
    }

    private static float getSmokingPredicate(ItemStack stack, net.minecraft.client.multiplayer.ClientLevel level, LivingEntity livingEntity, int seed) {
        if (livingEntity == null) {
            return 0.0F;
        }
        return livingEntity.isUsingItem() && livingEntity.getUseItem() == stack ? 1.0F : 0.0F;
    }

    private static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(HunterEntityTypes.ABILITY_EFFECT.get(), HunterAbilityEffectRenderer::new);
        event.registerEntityRenderer(HunterEntityTypes.AEGIS_SLAM_EFFECT.get(), AegisSlamEffectRenderer::new);
        event.registerEntityRenderer(HunterEntityTypes.SLASH_EFFECT.get(), SlashEffectRenderer::new);
        event.registerEntityRenderer(HunterEntityTypes.SPEED_BLADE_TRAIL.get(), SpeedBladeTrailRenderer::new);
        event.registerEntityRenderer(HunterEntityTypes.DEFENSE_PULSE.get(), DefensePulseRenderer::new);
        event.registerEntityRenderer(HunterEntityTypes.HEAVEN_SPLITTER_SLASH.get(), HeavenSplitterSlashRenderer::new);
        event.registerEntityRenderer(HunterEntityTypes.MIRROR_REPRISAL_EFFECT.get(), MirrorReprisalEffectRenderer::new);
        event.registerEntityRenderer(HunterEntityTypes.HAMMER_SHOCKWAVE.get(), HammerShockwaveRenderer::new);
        event.registerEntityRenderer(HunterEntityTypes.BOXING_BLOOD_BURST.get(), BoxingBloodBurstRenderer::new);
        event.registerEntityRenderer(HunterEntityTypes.BOXING_SHOCKWAVE_PROJECTILE.get(), BoxingShockwaveProjectileRenderer::new);
        event.registerEntityRenderer(HunterEntityTypes.TITAN_CANNON_WAVE.get(), TitanCannonWaveRenderer::new);
        event.registerEntityRenderer(HunterEntityTypes.CROSSFIRE_BARRAGE_EFFECT.get(), CrossfireBarrageEffectRenderer::new);
        event.registerEntityRenderer(HunterEntityTypes.LIVER_BREAK_EFFECT.get(), LiverBreakEffectRenderer::new);
        event.registerEntityRenderer(HunterEntityTypes.PARRY_SPARK_EFFECT.get(), ParrySparkEffectRenderer::new);
        event.registerEntityRenderer(HunterEntityTypes.ANKLE_SWEEP_EFFECT.get(), AnkleSweepEffectRenderer::new);
        event.registerEntityRenderer(HunterEntityTypes.RISING_SHOT_EFFECT.get(), RisingShotEffectRenderer::new);
        event.registerEntityRenderer(HunterEntityTypes.MARTIAL_WHIRLWIND_EFFECT.get(), MartialWhirlwindEffectRenderer::new);
        event.registerEntityRenderer(HunterEntityTypes.TORA_HUNT_EFFECT.get(), ToraHuntEffectRenderer::new);
        event.registerEntityRenderer(HunterEntityTypes.NEN_AURA_EFFECT.get(), NenAuraEffectRenderer::new);
        event.registerEntityRenderer(HunterEntityTypes.WHIRLWIND_SLASH.get(), WhirlwindSlashRenderer::new);
        event.registerEntityRenderer(HunterEntityTypes.DOWSING_CHAIN.get(), DowsingChainRenderer::new);
        event.registerEntityRenderer(HunterEntityTypes.SMOKEY_CHAIN_PROJECTILE.get(), SmokeyChainProjectileRenderer::new);
        event.registerEntityRenderer(HunterEntityTypes.CHAIN_WRAP_VISUAL.get(), ChainWrapVisualRenderer::new);
        event.registerEntityRenderer(HunterEntityTypes.ELASTIC_AURA_PROJECTILE.get(), ElasticAuraProjectileRenderer::new);
        event.registerEntityRenderer(HunterEntityTypes.ELASTIC_AURA_CONSTRUCT.get(), ElasticAuraConstructRenderer::new);
        event.registerEntityRenderer(HunterEntityTypes.BUNGEE_GUM_REFLECT.get(), BungeeGumReflectRenderer::new);
        event.registerEntityRenderer(HunterEntityTypes.SMOKE_SOLDIER.get(), SmokeSoldierRenderer::new);
        event.registerEntityRenderer(HunterEntityTypes.SMOKE_CLONE.get(), SmokeCloneRenderer::new);
        event.registerEntityRenderer(HunterEntityTypes.SMOKY_JAIL_BARRIER.get(), SmokyJailBarrierRenderer::new);
        event.registerEntityRenderer(HunterEntityTypes.GREAT_STAMP_PIG.get(), GreatStampPigRenderer::new);
        event.registerEntityRenderer(HunterEntityTypes.BANDIT.get(), BanditRenderer::new);
        event.registerEntityRenderer(HunterEntityTypes.TONPA.get(), TonpaRenderer::new);
        event.registerEntityRenderer(HunterEntityTypes.WING.get(), WingRenderer::new);
    }

    private static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(GreatStampPigModel.LAYER_LOCATION, GreatStampPigModel::createBodyLayer);
        event.registerLayerDefinition(ElasticAuraProjectileModel.LAYER_LOCATION, ElasticAuraProjectileModel::createBodyLayer);
        event.registerLayerDefinition(ElasticAuraConstructModel.LAYER_LOCATION, ElasticAuraConstructModel::createBodyLayer);
        event.registerLayerDefinition(ElasticAuraShieldModel.LAYER_LOCATION, ElasticAuraShieldModel::createBodyLayer);
        event.registerLayerDefinition(SharedChainProjectileModel.LAYER_LOCATION, SharedChainProjectileModel::createBodyLayer);
        event.registerLayerDefinition(ChainEndTipModel.DOWSING_LAYER, ChainEndTipModel::createDowsingLayer);
        event.registerLayerDefinition(ChainEndTipModel.CHAIN_JAIL_LAYER, ChainEndTipModel::createChainJailLayer);
        event.registerLayerDefinition(ChainEndTipModel.HOLY_LAYER, ChainEndTipModel::createHolyLayer);
        event.registerLayerDefinition(ChainEndTipModel.JUDGMENT_LAYER, ChainEndTipModel::createJudgmentLayer);
        event.registerLayerDefinition(ChainEndTipModel.STEAL_LAYER, ChainEndTipModel::createStealLayer);
        event.registerLayerDefinition(SlashEffectModel.LAYER_LOCATION, SlashEffectModel::createBodyLayer);
        event.registerLayerDefinition(ChainJailEffectModel.LAYER_LOCATION, ChainJailEffectModel::createBodyLayer);
    }

    private static void registerParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(HunterParticles.MOREL_SMOKE.get(), MorelSmokeParticle.Provider::new);
        event.registerSpriteSet(HunterParticles.SMOKY_JAIL_SMOKE.get(), com.huntercraft.huntercraft.client.particle.SmokyJailSmokeParticle.Provider::new);
        event.registerSpriteSet(HunterParticles.SMOKY_CHAIN_SMOKE.get(), SmokyChainSmokeParticle.Provider::new);
        event.registerSpriteSet(HunterParticles.ELASTIC_REFLECT.get(), ElasticReflectParticle.Provider::new);
    }
}
