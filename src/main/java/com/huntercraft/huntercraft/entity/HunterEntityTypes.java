package com.huntercraft.huntercraft.entity;

import com.huntercraft.huntercraft.HunterCraftMod;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class HunterEntityTypes {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, HunterCraftMod.MODID);
    private static final float VFX_HITBOX_SIZE = 0.01F;

    public static final RegistryObject<EntityType<DowsingChainEntity>> DOWSING_CHAIN = ENTITY_TYPES.register("dowsing_chain",
            () -> EntityType.Builder.<DowsingChainEntity>of(DowsingChainEntity::new, MobCategory.MISC)
                    .sized(VFX_HITBOX_SIZE, VFX_HITBOX_SIZE)
                    .noSummon()
                    .fireImmune()
                    .clientTrackingRange(8)
                    .updateInterval(1)
                    .build("dowsing_chain"));

    public static final RegistryObject<EntityType<HunterAbilityEffectEntity>> ABILITY_EFFECT = ENTITY_TYPES.register("ability_effect",
            () -> EntityType.Builder.<HunterAbilityEffectEntity>of(HunterAbilityEffectEntity::new, MobCategory.MISC)
                    .sized(VFX_HITBOX_SIZE, VFX_HITBOX_SIZE)
                    .clientTrackingRange(8)
                    .updateInterval(1)
                    .build("ability_effect"));
    public static final RegistryObject<EntityType<AegisSlamEffectEntity>> AEGIS_SLAM_EFFECT = ENTITY_TYPES.register("aegis_slam_effect",
            () -> EntityType.Builder.<AegisSlamEffectEntity>of(AegisSlamEffectEntity::new, MobCategory.MISC)
                    .sized(VFX_HITBOX_SIZE, VFX_HITBOX_SIZE)
                    .noSummon()
                    .fireImmune()
                    .clientTrackingRange(10)
                    .updateInterval(1)
                    .build("aegis_slam_effect"));
    public static final RegistryObject<EntityType<SlashEffectEntity>> SLASH_EFFECT = ENTITY_TYPES.register("slash_effect",
            () -> EntityType.Builder.<SlashEffectEntity>of(SlashEffectEntity::new, MobCategory.MISC)
                    .sized(VFX_HITBOX_SIZE, VFX_HITBOX_SIZE)
                    .noSummon()
                    .fireImmune()
                    .clientTrackingRange(10)
                    .updateInterval(1)
                    .build("slash_effect"));
    public static final RegistryObject<EntityType<SpeedBladeTrailEntity>> SPEED_BLADE_TRAIL = ENTITY_TYPES.register("speed_blade_trail",
            () -> EntityType.Builder.<SpeedBladeTrailEntity>of(SpeedBladeTrailEntity::new, MobCategory.MISC)
                    .sized(VFX_HITBOX_SIZE, VFX_HITBOX_SIZE)
                    .noSummon()
                    .fireImmune()
                    .clientTrackingRange(14)
                    .updateInterval(1)
                    .build("speed_blade_trail"));
    public static final RegistryObject<EntityType<DefensePulseEntity>> DEFENSE_PULSE = ENTITY_TYPES.register("defense_pulse",
            () -> EntityType.Builder.<DefensePulseEntity>of(DefensePulseEntity::new, MobCategory.MISC)
                    .sized(VFX_HITBOX_SIZE, VFX_HITBOX_SIZE)
                    .noSummon()
                    .fireImmune()
                    .clientTrackingRange(14)
                    .updateInterval(1)
                    .build("defense_pulse"));
    public static final RegistryObject<EntityType<HeavenSplitterSlashEntity>> HEAVEN_SPLITTER_SLASH = ENTITY_TYPES.register("heaven_splitter_slash",
            () -> EntityType.Builder.<HeavenSplitterSlashEntity>of(HeavenSplitterSlashEntity::new, MobCategory.MISC)
                    .sized(VFX_HITBOX_SIZE, VFX_HITBOX_SIZE)
                    .noSummon()
                    .fireImmune()
                    .clientTrackingRange(12)
                    .updateInterval(1)
                    .build("heaven_splitter_slash"));
    public static final RegistryObject<EntityType<MirrorReprisalEffectEntity>> MIRROR_REPRISAL_EFFECT = ENTITY_TYPES.register("mirror_reprisal_effect",
            () -> EntityType.Builder.<MirrorReprisalEffectEntity>of(MirrorReprisalEffectEntity::new, MobCategory.MISC)
                    .sized(VFX_HITBOX_SIZE, VFX_HITBOX_SIZE)
                    .noSummon()
                    .fireImmune()
                    .clientTrackingRange(12)
                    .updateInterval(1)
                    .build("mirror_reprisal_effect"));
    public static final RegistryObject<EntityType<HammerShockwaveEntity>> HAMMER_SHOCKWAVE = ENTITY_TYPES.register("hammer_shockwave",
            () -> EntityType.Builder.<HammerShockwaveEntity>of(HammerShockwaveEntity::new, MobCategory.MISC)
                    .sized(VFX_HITBOX_SIZE, VFX_HITBOX_SIZE)
                    .noSummon()
                    .fireImmune()
                    .clientTrackingRange(12)
                    .updateInterval(1)
                    .build("hammer_shockwave"));
    public static final RegistryObject<EntityType<BoxingBloodBurstEntity>> BOXING_BLOOD_BURST = ENTITY_TYPES.register("boxing_blood_burst",
            () -> EntityType.Builder.<BoxingBloodBurstEntity>of(BoxingBloodBurstEntity::new, MobCategory.MISC)
                    .sized(VFX_HITBOX_SIZE, VFX_HITBOX_SIZE)
                    .noSummon()
                    .fireImmune()
                    .clientTrackingRange(12)
                    .updateInterval(1)
                    .build("boxing_blood_burst"));
    public static final RegistryObject<EntityType<BoxingShockwaveProjectileEntity>> BOXING_SHOCKWAVE_PROJECTILE = ENTITY_TYPES.register("boxing_shockwave_projectile",
            () -> EntityType.Builder.<BoxingShockwaveProjectileEntity>of(BoxingShockwaveProjectileEntity::new, MobCategory.MISC)
                    .sized(VFX_HITBOX_SIZE, VFX_HITBOX_SIZE)
                    .noSummon()
                    .fireImmune()
                    .clientTrackingRange(14)
                    .updateInterval(1)
                    .build("boxing_shockwave_projectile"));
    public static final RegistryObject<EntityType<TitanCannonWaveEntity>> TITAN_CANNON_WAVE = ENTITY_TYPES.register("titan_cannon_wave",
            () -> EntityType.Builder.<TitanCannonWaveEntity>of(TitanCannonWaveEntity::new, MobCategory.MISC)
                    .sized(VFX_HITBOX_SIZE, VFX_HITBOX_SIZE)
                    .noSummon()
                    .fireImmune()
                    .clientTrackingRange(14)
                    .updateInterval(1)
                    .build("titan_cannon_wave"));
    public static final RegistryObject<EntityType<CrossfireBarrageEffectEntity>> CROSSFIRE_BARRAGE_EFFECT = ENTITY_TYPES.register("crossfire_barrage_effect",
            () -> EntityType.Builder.<CrossfireBarrageEffectEntity>of(CrossfireBarrageEffectEntity::new, MobCategory.MISC)
                    .sized(VFX_HITBOX_SIZE, VFX_HITBOX_SIZE)
                    .noSummon()
                    .fireImmune()
                    .clientTrackingRange(12)
                    .updateInterval(1)
                    .build("crossfire_barrage_effect"));
    public static final RegistryObject<EntityType<LiverBreakEffectEntity>> LIVER_BREAK_EFFECT = ENTITY_TYPES.register("liver_break_effect",
            () -> EntityType.Builder.<LiverBreakEffectEntity>of(LiverBreakEffectEntity::new, MobCategory.MISC)
                    .sized(VFX_HITBOX_SIZE, VFX_HITBOX_SIZE)
                    .noSummon()
                    .fireImmune()
                    .clientTrackingRange(12)
                    .updateInterval(1)
                    .build("liver_break_effect"));
    public static final RegistryObject<EntityType<ParrySparkEffectEntity>> PARRY_SPARK_EFFECT = ENTITY_TYPES.register("parry_spark_effect",
            () -> EntityType.Builder.<ParrySparkEffectEntity>of(ParrySparkEffectEntity::new, MobCategory.MISC)
                    .sized(VFX_HITBOX_SIZE, VFX_HITBOX_SIZE)
                    .noSummon()
                    .fireImmune()
                    .clientTrackingRange(12)
                    .updateInterval(1)
                    .build("parry_spark_effect"));
    public static final RegistryObject<EntityType<AnkleSweepEffectEntity>> ANKLE_SWEEP_EFFECT = ENTITY_TYPES.register("ankle_sweep_effect",
            () -> EntityType.Builder.<AnkleSweepEffectEntity>of(AnkleSweepEffectEntity::new, MobCategory.MISC)
                    .sized(VFX_HITBOX_SIZE, VFX_HITBOX_SIZE)
                    .noSummon()
                    .fireImmune()
                    .clientTrackingRange(12)
                    .updateInterval(1)
                    .build("ankle_sweep_effect"));
    public static final RegistryObject<EntityType<RisingShotEffectEntity>> RISING_SHOT_EFFECT = ENTITY_TYPES.register("rising_shot_effect",
            () -> EntityType.Builder.<RisingShotEffectEntity>of(RisingShotEffectEntity::new, MobCategory.MISC)
                    .sized(VFX_HITBOX_SIZE, VFX_HITBOX_SIZE)
                    .noSummon()
                    .fireImmune()
                    .clientTrackingRange(12)
                    .updateInterval(1)
                    .build("rising_shot_effect"));
    public static final RegistryObject<EntityType<MartialWhirlwindEffectEntity>> MARTIAL_WHIRLWIND_EFFECT = ENTITY_TYPES.register("martial_whirlwind_effect",
            () -> EntityType.Builder.<MartialWhirlwindEffectEntity>of(MartialWhirlwindEffectEntity::new, MobCategory.MISC)
                    .sized(VFX_HITBOX_SIZE, VFX_HITBOX_SIZE)
                    .noSummon()
                    .fireImmune()
                    .clientTrackingRange(12)
                    .updateInterval(1)
                    .build("martial_whirlwind_effect"));
    public static final RegistryObject<EntityType<ToraHuntEffectEntity>> TORA_HUNT_EFFECT = ENTITY_TYPES.register("tora_hunt_effect",
            () -> EntityType.Builder.<ToraHuntEffectEntity>of(ToraHuntEffectEntity::new, MobCategory.MISC)
                    .sized(VFX_HITBOX_SIZE, VFX_HITBOX_SIZE)
                    .noSummon()
                    .fireImmune()
                    .clientTrackingRange(12)
                    .updateInterval(1)
                    .build("tora_hunt_effect"));
    public static final RegistryObject<EntityType<NenAuraEffectEntity>> NEN_AURA_EFFECT = ENTITY_TYPES.register("nen_aura_effect",
            () -> EntityType.Builder.<NenAuraEffectEntity>of(NenAuraEffectEntity::new, MobCategory.MISC)
                    .sized(VFX_HITBOX_SIZE, VFX_HITBOX_SIZE)
                    .noSummon()
                    .fireImmune()
                    .clientTrackingRange(12)
                    .updateInterval(1)
                    .build("nen_aura_effect"));
    public static final RegistryObject<EntityType<WhirlwindSlashEntity>> WHIRLWIND_SLASH = ENTITY_TYPES.register("whirlwind_slash",
            () -> EntityType.Builder.<WhirlwindSlashEntity>of(WhirlwindSlashEntity::new, MobCategory.MISC)
                    .sized(VFX_HITBOX_SIZE, VFX_HITBOX_SIZE)
                    .noSummon()
                    .fireImmune()
                    .clientTrackingRange(10)
                    .updateInterval(1)
                    .build("whirlwind_slash"));
    public static final RegistryObject<EntityType<SmokeyChainProjectileEntity>> SMOKEY_CHAIN_PROJECTILE = ENTITY_TYPES.register("smokey_chain_projectile",
            () -> EntityType.Builder.<SmokeyChainProjectileEntity>of(SmokeyChainProjectileEntity::new, MobCategory.MISC)
                    .sized(1.0F, 1.0F)
                    .clientTrackingRange(8)
                    .updateInterval(1)
                    .build("smokey_chain_projectile"));
    public static final RegistryObject<EntityType<ChainWrapVisualEntity>> CHAIN_WRAP_VISUAL = ENTITY_TYPES.register("chain_wrap_visual",
            () -> EntityType.Builder.<ChainWrapVisualEntity>of(ChainWrapVisualEntity::new, MobCategory.MISC)
                    .sized(VFX_HITBOX_SIZE, VFX_HITBOX_SIZE)
                    .noSummon()
                    .fireImmune()
                    .clientTrackingRange(10)
                    .updateInterval(1)
                    .build("chain_wrap_visual"));
    public static final RegistryObject<EntityType<ElasticAuraProjectileEntity>> ELASTIC_AURA_PROJECTILE = ENTITY_TYPES.register("elastic_aura_projectile",
            () -> EntityType.Builder.<ElasticAuraProjectileEntity>of(ElasticAuraProjectileEntity::new, MobCategory.MISC)
                    .sized(1.4F, 1.4F)
                    .clientTrackingRange(8)
                    .updateInterval(1)
                    .build("elastic_aura_projectile"));
    public static final RegistryObject<EntityType<ElasticAuraConstructEntity>> ELASTIC_AURA_CONSTRUCT = ENTITY_TYPES.register("elastic_aura_construct",
            () -> EntityType.Builder.<ElasticAuraConstructEntity>of(ElasticAuraConstructEntity::new, MobCategory.MISC)
                    .sized(VFX_HITBOX_SIZE, VFX_HITBOX_SIZE)
                    .clientTrackingRange(8)
                    .updateInterval(1)
                    .build("elastic_aura_construct"));
    public static final RegistryObject<EntityType<BungeeGumReflectEntity>> BUNGEE_GUM_REFLECT = ENTITY_TYPES.register("bungee_gum_reflect",
            () -> EntityType.Builder.<BungeeGumReflectEntity>of(BungeeGumReflectEntity::new, MobCategory.MISC)
                    .sized(VFX_HITBOX_SIZE, VFX_HITBOX_SIZE)
                    .noSummon()
                    .fireImmune()
                    .clientTrackingRange(10)
                    .updateInterval(1)
                    .build("bungee_gum_reflect"));
    public static final RegistryObject<EntityType<SmokeSoldierEntity>> SMOKE_SOLDIER = ENTITY_TYPES.register("smoke_soldier",
            () -> EntityType.Builder.<SmokeSoldierEntity>of(SmokeSoldierEntity::new, MobCategory.MONSTER)
                    .sized(0.9F, 2.1F)
                    .clientTrackingRange(10)
                    .updateInterval(1)
                    .build("smoke_soldier"));
    public static final RegistryObject<EntityType<SmokeCloneEntity>> SMOKE_CLONE = ENTITY_TYPES.register("smoke_clone",
            () -> EntityType.Builder.<SmokeCloneEntity>of(SmokeCloneEntity::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.95F)
                    .clientTrackingRange(10)
                    .updateInterval(1)
                    .build("smoke_clone"));
    public static final RegistryObject<EntityType<SmokyJailBarrierEntity>> SMOKY_JAIL_BARRIER = ENTITY_TYPES.register("smoky_jail_barrier",
            () -> EntityType.Builder.<SmokyJailBarrierEntity>of(SmokyJailBarrierEntity::new, MobCategory.MISC)
                    .sized(VFX_HITBOX_SIZE, VFX_HITBOX_SIZE)
                    .clientTrackingRange(10)
                    .updateInterval(1)
                    .build("smoky_jail_barrier"));
    public static final RegistryObject<EntityType<GreatStampPigEntity>> GREAT_STAMP_PIG = ENTITY_TYPES.register("great_stamp_pig",
            () -> EntityType.Builder.<GreatStampPigEntity>of(GreatStampPigEntity::new, MobCategory.MONSTER)
                    .sized(3.8F, 3.4F)
                    .clientTrackingRange(10)
                    .build("great_stamp_pig"));
    public static final RegistryObject<EntityType<BanditEntity>> BANDIT = ENTITY_TYPES.register("bandit",
            () -> EntityType.Builder.<BanditEntity>of(BanditEntity::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.95F)
                    .clientTrackingRange(10)
                    .build("bandit"));
    public static final RegistryObject<EntityType<TonpaEntity>> TONPA = ENTITY_TYPES.register("tonpa",
            () -> EntityType.Builder.<TonpaEntity>of(TonpaEntity::new, MobCategory.CREATURE)
                    .sized(0.6F, 1.95F)
                    .clientTrackingRange(10)
                    .build("tonpa"));
    public static final RegistryObject<EntityType<WingEntity>> WING = ENTITY_TYPES.register("wing",
            () -> EntityType.Builder.<WingEntity>of(WingEntity::new, MobCategory.CREATURE)
                    .sized(0.6F, 1.95F)
                    .clientTrackingRange(10)
                    .build("wing"));

    private HunterEntityTypes() {
    }

    public static void register(IEventBus modBus) {
        ENTITY_TYPES.register(modBus);
    }
}
