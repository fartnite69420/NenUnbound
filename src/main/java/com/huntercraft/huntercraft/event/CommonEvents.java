package com.huntercraft.huntercraft.event;

import com.huntercraft.huntercraft.abilities.deeppurple.SmokeyJailAbility;
import com.huntercraft.huntercraft.abilities.defensetree.SkybreakerDiveAbility;
import com.huntercraft.huntercraft.abilities.bungeegum.ElasticAuraManager;
import com.huntercraft.huntercraft.entity.SmokyJailBarrierEntity;
import com.huntercraft.huntercraft.animation.AnimationType;
import com.huntercraft.huntercraft.abilities.HunterAbilities;
import com.huntercraft.huntercraft.abilities.SkillTreeCombatAbility;
import com.huntercraft.huntercraft.abilities.nenability.NenTechniqueAbility;
import com.huntercraft.huntercraft.abilities.martialartstree.MartialArtsGrabHelper;
import com.huntercraft.huntercraft.api.blacklist.HunterBlacklistHandler;
import com.huntercraft.huntercraft.api.blacklist.HunterBlacklistManager;
import com.huntercraft.huntercraft.data.HunterPlayerData;
import com.huntercraft.huntercraft.data.HunterPlayerDataProvider;
import com.huntercraft.huntercraft.data.TraitType;
import com.huntercraft.huntercraft.damage.HunterDamageTypes;
import com.huntercraft.huntercraft.entity.HunterEntityTypes;
import com.huntercraft.huntercraft.entity.ParrySparkEffectEntity;
import com.huntercraft.huntercraft.entity.WingEntity;
import com.huntercraft.huntercraft.effect.HunterMobEffects;
import com.huntercraft.huntercraft.network.packet.MeditationPromptInputPacket;
import com.huntercraft.huntercraft.faction.FactionUtil;
import com.huntercraft.huntercraft.quest.NenQuestStage;
import com.huntercraft.huntercraft.quest.NenType;
import com.huntercraft.huntercraft.quest.NenQuestUtil;
import com.huntercraft.huntercraft.quest.PhoneQuestRegistry;
import com.huntercraft.huntercraft.quest.QuestDefinition;
import com.huntercraft.huntercraft.quest.QuestObjectiveType;
import com.huntercraft.huntercraft.quest.QuestRegistry;
import com.huntercraft.huntercraft.sound.HunterSoundEvents;
import com.huntercraft.huntercraft.util.HunterDataUtil;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import static net.minecraft.commands.Commands.argument;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashSet;
import java.util.Set;

public class CommonEvents {
    private static final String[] HATSU_TYPE_SUGGESTIONS = {
            "enhancement",
            "emission",
            "transmutation",
            "conjuration",
            "manipulation",
            "specialization"
    };
    private static final String[] HATSU_TECHNIQUE_SUGGESTIONS = {
            "bungee_gum",
            "deep_purple",
            "chain_style"
    };
    private static final String[] TRAIT_SUGGESTIONS = {
            "none",
            "scarlet_eyes"
    };

    private static final int SAME_ATTACKER_IFRAME_TICKS = 6;
    private static final String SAME_ATTACKER_IFRAME_TAG = "HunterSameAttackerIFrames";

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        HunterBlacklistManager.loadBlacklist();
    }

    @SubscribeEvent
    public void attachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player) {
            event.addCapability(HunterPlayerDataProvider.ID, new HunterPlayerDataProvider());
        }
    }

    @SubscribeEvent
    public void onClone(PlayerEvent.Clone event) {
        event.getOriginal().reviveCaps();
        HunterDataUtil.getOptional(event.getOriginal()).ifPresent(original ->
                HunterDataUtil.getOptional(event.getEntity()).ifPresent(clone -> clone.copyFrom(original))
        );
        event.getOriginal().invalidateCaps();
        if (event.isWasDeath() && event.getEntity() instanceof ServerPlayer player) {
            HunterDataUtil.applyLevelBonuses(player);
            player.setHealth(player.getMaxHealth());
            HunterDataUtil.sync(player);
        }
    }

    @SubscribeEvent
    public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        HunterPlayerData data = HunterDataUtil.getOptional(player).orElse(null);

        // Ryu fight reset block (runs only when conditions are met)
        if (data != null && data.getNenQuestStage() == NenQuestStage.RYU_SHIFT_AURA && data.isRyuFightStarted() && !data.isRyuFightFinished()) {
            data.setRyuFightStarted(false);
            data.setRyuFightFinished(false);
            player.level().getEntitiesOfClass(WingEntity.class, player.getBoundingBox().inflate(128.0D),
                    wing -> wing.isSparringWith(player)).forEach(WingEntity::clearSpar);
            HunterDataUtil.sync(player);
        }

        // Discard any active Smokey Jail barrier owned by this player
        if (data != null) {
            float searchRadius = SmokeyJailAbility.BARRIER_RADIUS + 24.0F;
            net.minecraft.world.phys.AABB barrierBox = player.getBoundingBox().inflate(searchRadius);
            player.level().getEntitiesOfClass(SmokyJailBarrierEntity.class, barrierBox,
                    barrier -> barrier.isOwnedBy(player))
                .forEach(SmokyJailBarrierEntity::discard);
            if (data.isActiveAbility("smokey_jail")) {
                data.clearActiveAbility();
                com.huntercraft.huntercraft.util.HunterDataUtil.sync(player);
            }
        }
    }

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            HunterBlacklistHandler.handleBlacklist(player);
        }
    }

    @SubscribeEvent
    public void onJoin(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            HunterDataUtil.getOptional(player).ifPresent(data -> {
                data.ensureStarterQuests();
                data.ensurePhoneQuestRefreshStarted(player.level().getGameTime());
                HunterDataUtil.syncAndRefresh(player);
            });
        }
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide() || !(event.player instanceof ServerPlayer player)) {
            return;
        }

        HunterPlayerData data = HunterDataUtil.getOptional(player).orElse(null);
        if (data == null) {
            return;
        }
        int previousNen = data.getCurrentNen();
        boolean questRefreshChanged = data.ensurePhoneQuestRefreshStarted(player.level().getGameTime());
        questRefreshChanged |= data.refreshPhoneQuestsIfReady(player.level().getGameTime());
        data.tickCooldowns();
        data.tickGuard();
        data.tickVisualAnimations();
        tickZetsuSuppression(player, data);
        tickZetsuStealth(player, data);
        NenTechniqueAbility.tickNen(player, data);
        HunterDataUtil.applyLevelBonuses(player);
        tickDeepPurpleDrowningPassive(player, data);
        boolean deepPurplePingChanged = false;
        if (!data.hasDeepPurpleTechnique() && data.getDeepPurpleSpottedTargetPos() != null) {
            data.clearDeepPurplePing();
            deepPurplePingChanged = true;
        } else if (player.tickCount % 5 == 0 && data.getDeepPurpleSpottedTargetTicks() > 0) {
            deepPurplePingChanged = data.tickDeepPurplePing();
        }
        if (isMovementStunned(player)) {
            player.setSprinting(false);
        }
        if (ElasticAuraManager.hasElasticAura(player)) {
            ElasticAuraManager.tick(player);
        }
        for (var ability : HunterAbilities.COMBAT_ABILITIES) {
            ability.tick(player, data);
        }
        if (data.isMartialArtsGrabActive()) {
            MartialArtsGrabHelper.sustainGrab(player, data);
        }
        if (data.isGuarding() && data.getGuardTicks() % 6 == 0) {
            com.huntercraft.huntercraft.abilities.base.GuardAbility.spawnAfterImages(player, 10);
        }
        if (data.isGuarding() && data.getGuardTicks() >= com.huntercraft.huntercraft.abilities.base.GuardAbility.MAX_ACTIVE_TICKS) {
            com.huntercraft.huntercraft.abilities.HunterAbilities.GUARD.stop(player, data);
            HunterDataUtil.sync(player);
            return;
        }

        if (player.onGround()) {
            data.setAirJumpsUsed(0);
            if (player.fallDistance <= 0.0F) {
                data.setAirLaunchFallProtection(false);
            }
        }

        tickWingTrainerSpawns(player, data);

        if (player.tickCount % 20 == 0) {
            updateCollectQuests(player, data);
        }
        handleEnIntrusionAlerts(player, data);
        boolean nenQuestChanged = tickNenQuestProgress(player, data);

        if (data.hasShuUnlocked() && player.tickCount % 5 == 0) {
            player.getAllSlots().forEach(stack -> {
                if (!stack.isEmpty() && stack.isDamaged()) {
                    stack.setDamageValue(Math.max(0, stack.getDamageValue() - 1));
                }
            });
        }

        if (data.getChargeTicksRemaining() > 0
                || data.getActiveAbilityTicksRemaining() > 0
                || data.isMartialArtsGrabActive()
                || data.isMeditationCountdownActive()
                || data.isMeditationActive()
                || data.getCurrentAnimation() == AnimationType.LION_FANG_DRAW_CHARGE
                || data.getCurrentNen() != previousNen
                || nenQuestChanged
                || deepPurplePingChanged
                || questRefreshChanged
                || player.tickCount % 20 == 0) {
            HunterDataUtil.sync(player);
        }
    }

    private static void tickZetsuSuppression(ServerPlayer player, HunterPlayerData data) {
        if (data.isZetsuActive()) {
            player.addEffect(new MobEffectInstance(HunterMobEffects.ZETSU.get(), com.huntercraft.huntercraft.abilities.nenability.ZetsuAbility.EFFECT_REFRESH_TICKS, 0, false, false, true));
        }
        if (!player.hasEffect(HunterMobEffects.ZETSU.get())) {
            return;
        }
        data.disableNenDrains();
        String activeAbilityId = data.getActiveAbilityId();
        if (!activeAbilityId.isBlank()
                && HunterAbilities.byId(activeAbilityId) instanceof com.huntercraft.huntercraft.abilities.SkillTreeCombatAbility ability
                && ability.isSuppressedByZetsu()) {
            ability.stop(player, data);
            data.clearActiveAbility();
        }
    }

    private static void tickZetsuStealth(ServerPlayer player, HunterPlayerData data) {
        boolean zetsuStealth = player.hasEffect(HunterMobEffects.ZETSU.get()) || data.isZetsuActive();
        if (zetsuStealth) {
            if (data.isZetsuForcedInvisibility()) {
                player.setInvisible(false);
                data.setZetsuForcedInvisibility(false);
            }
            return;
        }
        if (data.isZetsuForcedInvisibility()) {
            player.setInvisible(false);
            data.setZetsuForcedInvisibility(false);
        }
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        for (ServerLevel level : event.getServer().getAllLevels()) {
            SkybreakerDiveAbility.tickTerrainRestoration(level);
        }
    }

    @SubscribeEvent
    public void onLivingHurt(LivingHurtEvent event) {
        LivingEntity victim = event.getEntity();
        if (victim instanceof WingEntity wing && event.getSource().getEntity() instanceof ServerPlayer player && wing.isSparringWith(player)) {
            if (wing.getHealth() - event.getAmount() <= wing.getMaxHealth() * 0.25F) {
                event.setCanceled(true);
                completeRyuTrial(player, wing);
                return;
            }
        }
        String attackerId = event.getSource().getEntity() instanceof LivingEntity livingAttacker
                ? livingAttacker.getStringUUID()
                : "";
        boolean unavoidableWeaponDamage = event.getSource().typeHolder().is(HunterDamageTypes.UNAVOIDABLE_WEAPON);
        if (!unavoidableWeaponDamage && hasSameAttackerIFrame(victim, attackerId)) {
            event.setCanceled(true);
            return;
        }
        if (!(victim instanceof ServerPlayer player)) {
            if (event.getAmount() > 0.0F) {
                triggerSameAttackerIFrame(victim, attackerId, SAME_ATTACKER_IFRAME_TICKS);
            }
            return;
        }
        if (event.getSource().getEntity() instanceof Player attacker && FactionUtil.areFactionMates(attacker, player)) {
            event.setCanceled(true);
            return;
        }

        HunterPlayerData data = HunterDataUtil.getOptional(player).orElse(null);
        if (data == null) {
            return;
        }
        if (HunterAbilities.ELASTIC_REFLECT.isActive(data) && event.getSource().getDirectEntity() instanceof Projectile projectile) {
            if (ElasticAuraManager.reflectProjectile(player, projectile, event.getSource().getEntity())) {
                event.setCanceled(true);
                return;
            }
        }
        if (event.getSource().getEntity() instanceof WingEntity wing && data.getNenQuestStage() == NenQuestStage.RYU_SHIFT_AURA && wing.isSparringWith(player)) {
            if (player.getHealth() - event.getAmount() <= player.getMaxHealth() * 0.25F) {
                event.setCanceled(true);
                completeRyuTrial(player, wing);
                return;
            }
        }
        if (event.getSource() == player.damageSources().fall() && data.hasAirLaunchFallProtection()) {
            event.setCanceled(true);
            return;
        }
        if (!unavoidableWeaponDamage && HunterAbilities.SLIP_GUARD.shouldDodgeMelee(data, player, event.getSource())) {
            event.setCanceled(true);
            data.triggerAnimation(AnimationType.PARRY);
            HunterDataUtil.sync(player);
            return;
        }
        if (HunterAbilities.LIVER_BREAK_COUNTER.tryCounter(player, data, event.getSource().getEntity() instanceof LivingEntity attacker ? attacker : null)) {
            event.setCanceled(true);
            return;
        }
        if (HunterAbilities.MIRROR_REPRISAL.tryCounter(player, data, event.getSource().getEntity() instanceof LivingEntity attacker ? attacker : null)) {
            event.setCanceled(true);
            return;
        }
        if (!unavoidableWeaponDamage && data.getDashIFrameTicks() > 0) {
            event.setCanceled(true);
            return;
        }
        if (data.isGuarding() && !event.getSource().typeHolder().is(net.minecraft.tags.DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            com.huntercraft.huntercraft.abilities.base.GuardAbility.spawnAfterImages(player, 12);
            HunterDataUtil.sync(player);
            event.setCanceled(true);
            return;
        }
        if (!unavoidableWeaponDamage && data.hasAttackerIFrame(attackerId)) {
            event.setCanceled(true);
            return;
        }
        float amount = compensateProtectionEnchantmentsForRyu(player, data, event.getAmount(), event.getSource());
        float nenReduction = player.hasEffect(HunterMobEffects.ZETSU.get()) ? 0.0F : NenTechniqueAbility.getIncomingReduction(data);
        float reduction = Math.min(0.85F, data.getPassiveToughnessReduction() + nenReduction);
        event.setAmount(amount * (1.0F - reduction));
        if (event.getAmount() > 0.0F) {
            data.triggerAttackerIFrame(attackerId, SAME_ATTACKER_IFRAME_TICKS);
            triggerSameAttackerIFrame(player, attackerId, SAME_ATTACKER_IFRAME_TICKS);
        }
    }

    @SubscribeEvent
    public void onLivingKnockBack(LivingKnockBackEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        HunterPlayerData data = HunterDataUtil.getOptional(player).orElse(null);
        if (data != null && (data.isGuarding() || data.isGuardParrying())) {
            event.setCanceled(true);
            player.setDeltaMovement(0.0D, player.getDeltaMovement().y, 0.0D);
            player.hurtMarked = true;
        }
    }

    private static void finishGuardParry(ServerPlayer player, HunterPlayerData data, Entity attacker) {
        if (attacker instanceof LivingEntity livingAttacker) {
            HunterDataUtil.applyParryStun(livingAttacker, player, 6);
        }
        data.setGuarding(false);
        data.setAbilityCooldown(HunterAbilities.GUARD.id(), 6);
    }

    private static void spawnParrySpark(ServerPlayer player, Entity attacker) {
        if (!(player.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        Vec3 direction = attacker != null
                ? attacker.position().subtract(player.position()).multiply(1.0D, 0.0D, 1.0D)
                : player.getLookAngle().multiply(1.0D, 0.0D, 1.0D);
        if (direction.lengthSqr() < 1.0E-4D) {
            direction = Vec3.directionFromRotation(0.0F, player.getYRot());
        }
        Vec3 position = player.position().add(direction.normalize().scale(0.18D)).add(0.0D, 0.88D, 0.0D);
        ParrySparkEffectEntity.spawn(serverLevel, position, direction, 1.0F, 12);
        serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.ELECTRIC_SPARK, position.x, position.y, position.z, 20, 0.28D, 0.22D, 0.28D, 0.045D);
        serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.CRIT, position.x, position.y, position.z, 10, 0.18D, 0.16D, 0.18D, 0.035D);
        serverLevel.playSound(null, position.x, position.y, position.z, HunterSoundEvents.PARRY.get(), SoundSource.PLAYERS, 0.95F, 1.0F);
    }

    private static void cancelParriedContinuousAbility(Entity attackerEntity) {
        if (!(attackerEntity instanceof ServerPlayer attacker)) {
            return;
        }
        HunterPlayerData attackerData = HunterDataUtil.getOptional(attacker).orElse(null);
        if (attackerData == null) {
            return;
        }
        String activeAbilityId = attackerData.getActiveAbilityId();
        if (activeAbilityId.isBlank() || !(HunterAbilities.byId(activeAbilityId) instanceof SkillTreeCombatAbility ability)) {
            return;
        }
        if (!ability.isContinuous() || ability.isPassiveWhileActive()) {
            return;
        }
        ability.stop(attacker, attackerData);
        if (attackerData.isActiveAbility(activeAbilityId)) {
            attackerData.clearActiveAbility();
        }
        attackerData.setAbilityCooldown(activeAbilityId, ability.getMaxCooldownTicks());
        HunterDataUtil.sync(attacker);
    }

    @SubscribeEvent
    public void onOutgoingHurt(LivingHurtEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer attacker)) {
            return;
        }
        if (event.getSource().getDirectEntity() != attacker) {
            return;
        }
        HunterPlayerData data = HunterDataUtil.getOptional(attacker).orElse(null);
        if (data == null) {
            return;
        }
        if (attacker.getMainHandItem().isEmpty()) {
            float punchMultiplier = HunterAbilities.PHYSICAL_POWER.getPunchDamageMultiplier(data);
            event.setAmount(event.getAmount() * punchMultiplier);
        } else if (isGuardWeapon(attacker.getMainHandItem())) {
            float weaponMultiplier = HunterAbilities.WEAPON_MASTERY.getWeaponDamageMultiplier(data);
            if (weaponMultiplier > 1.0F) {
                event.setAmount(event.getAmount() * weaponMultiplier);
            }
        }
        float nenDamageBonus = NenTechniqueAbility.getOutgoingDamageBonusTotal(data);
        if (nenDamageBonus > 0.0F) {
            nenDamageBonus *= getNenMeleeBonusScale(attacker, data);
            event.setAmount(event.getAmount() + nenDamageBonus);
        }
        if (isNenAbilityDamage(data, event) && !data.isRenActive() && !data.isKenActive()) {
            event.setAmount(event.getAmount() + getPassiveNenAbilityDamageBonus(data));
        }

        if (data.getNenQuestStage() == NenQuestStage.REN_OVERFLOW) {
            data.setRenOverflowDamageWindow(data.getRenOverflowDamageWindow() + event.getAmount());
            data.setRenOverflowTicks(NenQuestUtil.REN_OVERFLOW_WINDOW_TICKS);
        }
        if (data.getNenQuestStage() == NenQuestStage.SHU_REN_WEAPON && data.isRenActive() && isGuardWeapon(attacker.getMainHandItem())) {
            data.setShuWeaponRenTicks(Math.min(NenQuestUtil.SHU_REN_WEAPON_TICKS_REQUIRED, data.getShuWeaponRenTicks() + 20));
        }
        if (data.getNenQuestStage() == NenQuestStage.KO_ONE_SHOT && event.getEntity().getHealth() <= event.getAmount() + 0.01F) {
            data.setKoOneShotKills(Math.min(NenQuestUtil.KO_ONE_SHOT_REQUIRED, data.getKoOneShotKills() + 1));
        }
        if (data.isKoActive()) {
            event.setAmount(event.getAmount() * NenTechniqueAbility.getKoStrikeMultiplier(data, attacker));
        }
        event.setAmount(event.getAmount() * data.getCombatVowDamageMultiplier());
    }

    private static float compensateProtectionEnchantmentsForRyu(ServerPlayer player, HunterPlayerData data, float amount, net.minecraft.world.damagesource.DamageSource source) {
        if (!data.hasRyuUnlocked() || data.isKoActive() || player.hasEffect(HunterMobEffects.ZETSU.get())) {
            return amount;
        }
        int protection = EnchantmentHelper.getDamageProtection(player.getArmorSlots(), source);
        if (protection <= 0) {
            return amount;
        }
        float protectionFactor = 1.0F - (Math.min(20, protection) * 0.04F);
        return protectionFactor > 0.0F ? amount / protectionFactor : amount;
    }

    private static boolean isNenAbilityDamage(HunterPlayerData data, LivingHurtEvent event) {
        String activeAbilityId = data.getActiveAbilityId();
        return !activeAbilityId.isBlank() && HunterAbilities.isNenAbility(activeAbilityId);
    }

    private static float getPassiveNenAbilityDamageBonus(HunterPlayerData data) {
        return NenTechniqueAbility.getPassiveRenDamageBonus(data);
    }

    @SubscribeEvent
    public void onLivingFall(LivingFallEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        HunterPlayerData data = HunterDataUtil.getOptional(player).orElse(null);
        if (data != null && data.hasAirLaunchFallProtection()) {
            event.setCanceled(true);
            event.setDistance(0.0F);
        }
    }

    @SubscribeEvent
    public void onLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity living = event.getEntity();
        tickSameAttackerIFrames(living);
        if (!isMovementStunned(living)) {
            return;
        }
        living.setJumping(false);
        living.setSprinting(false);
    }

    @SubscribeEvent
    public void onLivingJump(LivingEvent.LivingJumpEvent event) {
        LivingEntity living = event.getEntity();
        if (!isMovementStunned(living)) {
            return;
        }
        Vec3 movement = living.getDeltaMovement();
        if (movement.y > 0.0D) {
            living.setDeltaMovement(movement.x, 0.0D, movement.z);
            living.hurtMarked = true;
        }
        living.setJumping(false);
    }

    private static boolean isMovementStunned(LivingEntity living) {
        return living.hasEffect(HunterMobEffects.STUNNED.get()) || living.hasEffect(HunterMobEffects.PARRY_STUNNED.get());
    }

    @SubscribeEvent
    public void onAttackEntity(AttackEntityEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !(event.getTarget() instanceof LivingEntity target)) {
            return;
        }
        if (isMovementStunned(player)) {
            event.setCanceled(true);
            return;
        }
        if (target instanceof ServerPlayer targetPlayer) {
            HunterPlayerData targetData = HunterDataUtil.getOptional(targetPlayer).orElse(null);
            if (targetData != null && targetData.isGuardParrying()) {
                return;
            }
        }
        if (!ElasticAuraManager.hasElasticAura(player)) {
            return;
        }
        if (ElasticAuraManager.handleTaggedMelee(player, target)) {
            event.setCanceled(true);
            HunterDataUtil.sync(player);
        }
    }

    @SubscribeEvent
    public void onProjectileImpact(ProjectileImpactEvent event) {
        if (!(event.getRayTraceResult() instanceof net.minecraft.world.phys.EntityHitResult hitResult)) {
            return;
        }
        if (!(hitResult.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        HunterPlayerData data = HunterDataUtil.getOptional(player).orElse(null);
        if (data == null || !HunterAbilities.ELASTIC_REFLECT.isActive(data)) {
            return;
        }
        Projectile projectile = event.getProjectile();
        if (ElasticAuraManager.reflectProjectile(player, projectile, projectile.getOwner())) {
            event.setImpactResult(ProjectileImpactEvent.ImpactResult.STOP_AT_CURRENT_NO_DAMAGE);
        }
    }

    @SubscribeEvent
    public void onKill(LivingDeathEvent event) {
        LivingEntity living = event.getEntity();
        if (living instanceof ServerPlayer deadPlayer) {
            HunterPlayerData deadData = HunterDataUtil.getOptional(deadPlayer).orElse(null);
            if (deadData != null && !deadData.getJudgmentDisabledAbilities().isEmpty()) {
                deadData.clearJudgmentDisabledAbilities();
                HunterDataUtil.sync(deadPlayer);
            }
        }
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) {
            return;
        }

        HunterPlayerData data = HunterDataUtil.getOptional(player).orElse(null);
        if (data == null) {
            return;
        }
        ResourceLocation entityId = ForgeRegistries.ENTITY_TYPES.getKey(living.getType());
        if (entityId == null) {
            return;
        }

        if (data.getNenQuestStage() == NenQuestStage.REN_AURA_BURST) {
            data.setAuraBurstKills(Math.min(NenQuestUtil.AURA_BURST_KILLS_REQUIRED, data.getAuraBurstKills() + 1));
            data.setAuraBurstTicks(NenQuestUtil.AURA_BURST_WINDOW_TICKS);
        }

        for (QuestDefinition quest : QuestRegistry.all()) {
            if (quest.objectiveType() != QuestObjectiveType.KILL_ENTITY || !quest.targetId().equals(entityId.toString())) {
                continue;
            }
            if (data.incrementQuestProgress(quest.id(), 1) && data.tryCompleteQuest(quest.id())) {
                if (PhoneQuestRegistry.isPhoneQuest(quest.id())) {
                    data.startPhoneQuestRefresh(player.level().getGameTime());
                }
                HunterDataUtil.grantQuestXp(player, data.getQuestRewardXp(quest.id()), Component.translatable(quest.titleKey()));
            } else {
                HunterDataUtil.sync(player);
            }
        }
    }

    @SubscribeEvent
    public void onItemPickup(EntityItemPickupEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            HunterDataUtil.getOptional(player).ifPresent(data -> {
                if (data.isEmptyHandsPickupEnabled() && !routePickupIntoMainInventory(player, event.getItem())) {
                    event.setCanceled(true);
                    return;
                }
                updateCollectQuests(player, data);
            });
        }
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        registerHunterCommands(event.getDispatcher());
    }

    private void tickDeepPurpleDrowningPassive(ServerPlayer player, HunterPlayerData data) {
        double slowMultiplier = data.getDeepPurpleDrowningSlowMultiplier();
        if (slowMultiplier <= 1.0D) {
            data.resetDeepPurpleDrowningRecoveryProgress();
            return;
        }
        if (!player.isUnderWater() || player.canBreatheUnderwater()) {
            data.resetDeepPurpleDrowningRecoveryProgress();
            return;
        }

        double recoveryPerTick = 1.0D - (1.0D / slowMultiplier);
        data.addDeepPurpleDrowningRecoveryProgress(recoveryPerTick);
        while (data.consumeDeepPurpleDrowningRecoveryStep() && player.getAirSupply() < player.getMaxAirSupply()) {
            player.setAirSupply(Math.min(player.getMaxAirSupply(), player.getAirSupply() + 1));
        }
    }

    private static void registerHunterCommands(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("hunter")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("level")
                        .then(argument("level", IntegerArgumentType.integer(1, HunterPlayerData.MAX_LEVEL))
                                .executes(context -> setHunterLevel(context.getSource(), context.getSource().getPlayerOrException(), IntegerArgumentType.getInteger(context, "level")))
                                .then(argument("player", EntityArgument.player())
                                        .executes(context -> setHunterLevel(context.getSource(), EntityArgument.getPlayer(context, "player"), IntegerArgumentType.getInteger(context, "level"))))))
                .then(Commands.literal("nen")
                        .then(Commands.literal("level")
                                .then(argument("level", IntegerArgumentType.integer(1, HunterPlayerData.MAX_NEN_LEVEL))
                                        .executes(context -> setNenLevel(context.getSource(), context.getSource().getPlayerOrException(), IntegerArgumentType.getInteger(context, "level")))
                                        .then(argument("player", EntityArgument.player())
                                                .executes(context -> setNenLevel(context.getSource(), EntityArgument.getPlayer(context, "player"), IntegerArgumentType.getInteger(context, "level"))))))
                        .then(Commands.literal("unlockbaseabilities")
                                .executes(context -> {
                                    ServerPlayer player = context.getSource().getPlayerOrException();
                                    HunterPlayerData data = HunterDataUtil.getOptional(player).orElse(null);
                                    if (data == null) {
                                        return 0;
                                    }
                                    unlockBaseNenAbilities(data);
                                    if (data.hasNen()) {
                                        data.setCurrentNen(data.getMaxNen());
                                    }
                                    HunterDataUtil.sync(player);
                                    context.getSource().sendSuccess(() -> Component.literal("Unlocked all basic Nen abilities."), true);
                                    return 1;
                                })
                                .then(argument("player", EntityArgument.player())
                                        .executes(context -> {
                                            ServerPlayer target = EntityArgument.getPlayer(context, "player");
                                            HunterPlayerData data = HunterDataUtil.getOptional(target).orElse(null);
                                            if (data == null) {
                                                return 0;
                                            }
                                            unlockBaseNenAbilities(data);
                                            if (data.hasNen()) {
                                                data.setCurrentNen(data.getMaxNen());
                                            }
                                            HunterDataUtil.sync(target);
                                            context.getSource().sendSuccess(() -> Component.literal("Unlocked all basic Nen abilities for " + target.getScoreboardName() + "."), true);
                                            return 1;
                                        }))))
                .then(Commands.literal("resetcooldowns")
                        .executes(context -> resetCooldowns(context.getSource(), context.getSource().getPlayerOrException()))
                        .then(argument("player", EntityArgument.player())
                                .executes(context -> resetCooldowns(context.getSource(), EntityArgument.getPlayer(context, "player")))))
                .then(Commands.literal("resetprogress")
                        .executes(context -> resetProgress(context.getSource(), context.getSource().getPlayerOrException()))
                        .then(argument("player", EntityArgument.player())
                                .executes(context -> {
                                    return resetProgress(context.getSource(), EntityArgument.getPlayer(context, "player"));
                                })))
                .then(Commands.literal("blacklist")
                        .then(Commands.literal("reload")
                                .executes(context -> reloadBlacklist(context.getSource())))
                        .then(Commands.literal("check")
                                .then(argument("player", EntityArgument.player())
                                        .executes(context -> checkBlacklist(context.getSource(), EntityArgument.getPlayer(context, "player"))))))
                .then(Commands.literal("hatsu")
                        .then(Commands.literal("type")
                                .then(argument("type", StringArgumentType.word())
                                        .suggests((context, builder) -> SharedSuggestionProvider.suggest(HATSU_TYPE_SUGGESTIONS, builder))
                                        .executes(context -> setHatsuType(context.getSource(), context.getSource().getPlayerOrException(), StringArgumentType.getString(context, "type")))
                                        .then(argument("player", EntityArgument.player())
                                                .executes(context -> setHatsuType(context.getSource(), EntityArgument.getPlayer(context, "player"), StringArgumentType.getString(context, "type"))))))
                        .then(Commands.literal("technique")
                                .then(argument("technique", StringArgumentType.word())
                                        .suggests((context, builder) -> SharedSuggestionProvider.suggest(HATSU_TECHNIQUE_SUGGESTIONS, builder))
                                        .executes(context -> setHatsuTechnique(context.getSource(), context.getSource().getPlayerOrException(), StringArgumentType.getString(context, "technique"))))
                                .then(argument("player", EntityArgument.player())
                                        .then(argument("technique", StringArgumentType.word())
                                                .suggests((context, builder) -> SharedSuggestionProvider.suggest(HATSU_TECHNIQUE_SUGGESTIONS, builder))
                                                .executes(context -> setHatsuTechnique(context.getSource(), EntityArgument.getPlayer(context, "player"), StringArgumentType.getString(context, "technique")))))))
                .then(Commands.literal("trait")
                        .then(argument("trait", StringArgumentType.word())
                                .suggests((context, builder) -> SharedSuggestionProvider.suggest(TRAIT_SUGGESTIONS, builder))
                                .executes(context -> {
                                    ServerPlayer player = context.getSource().getPlayerOrException();
                                    HunterPlayerData data = HunterDataUtil.getOptional(player).orElse(null);
                                    if (data == null) {
                                        return 0;
                                    }
                                    TraitType trait = TraitType.byId(StringArgumentType.getString(context, "trait"));
                                    data.setTrait(trait);
                                    HunterDataUtil.sync(player);
                                    context.getSource().sendSuccess(() -> Component.literal("Set trait to " + trait.displayName() + "."), true);
                                    return 1;
                                })
                                .then(argument("player", EntityArgument.player())
                                        .executes(context -> {
                                            ServerPlayer target = EntityArgument.getPlayer(context, "player");
                                            HunterPlayerData data = HunterDataUtil.getOptional(target).orElse(null);
                                            if (data == null) {
                                                return 0;
                                            }
                                            TraitType trait = TraitType.byId(StringArgumentType.getString(context, "trait"));
                                            data.setTrait(trait);
                                            HunterDataUtil.sync(target);
                                            context.getSource().sendSuccess(() -> Component.literal("Set " + target.getScoreboardName() + "'s trait to " + trait.displayName() + "."), true);
                                            return 1;
                                        })))));
    }

    private static int setHunterLevel(CommandSourceStack source, ServerPlayer target, int targetLevel) {
        HunterPlayerData data = HunterDataUtil.getOptional(target).orElse(null);
        if (data == null) {
            return 0;
        }
        data.setLevel(targetLevel);
        HunterDataUtil.applyLevelBonuses(target);
        target.setHealth(target.getMaxHealth());
        HunterDataUtil.sync(target);
        source.sendSuccess(() -> Component.literal("Set " + target.getScoreboardName() + "'s level to " + data.getLevel() + "."), true);
        return 1;
    }

    private static int reloadBlacklist(CommandSourceStack source) {
        boolean loaded = HunterBlacklistManager.loadBlacklist();
        source.sendSuccess(() -> Component.literal(HunterBlacklistManager.getLastLoadStatus()), true);
        return loaded ? 1 : 0;
    }

    private static int checkBlacklist(CommandSourceStack source, ServerPlayer target) {
        String reason = HunterBlacklistManager.getBlacklistReason(target.getUUID());
        if (reason == null) {
            source.sendSuccess(() -> Component.literal(target.getScoreboardName() + " is not on the HunterCraft blacklist."), false);
            return 0;
        }
        source.sendSuccess(() -> Component.literal(target.getScoreboardName() + " is blacklisted: " + reason), true);
        return 1;
    }

    private static int setNenLevel(CommandSourceStack source, ServerPlayer target, int targetLevel) {
        HunterPlayerData data = HunterDataUtil.getOptional(target).orElse(null);
        if (data == null) {
            return 0;
        }
        data.setNenLevel(targetLevel);
        data.setCurrentNen(data.getMaxNen());
        HunterDataUtil.sync(target);
        source.sendSuccess(() -> Component.literal("Set " + target.getScoreboardName() + "'s Nen level to " + targetLevel + "."), true);
        return 1;
    }

    private static int resetCooldowns(CommandSourceStack source, ServerPlayer target) {
        HunterPlayerData data = HunterDataUtil.getOptional(target).orElse(null);
        if (data == null) {
            return 0;
        }
        data.resetCooldowns();
        data.setCurrentNen(data.getMaxNen());
        HunterDataUtil.sync(target);
        source.sendSuccess(() -> Component.literal("Reset Nen Unbound cooldowns for " + target.getScoreboardName() + "."), true);
        return 1;
    }

    private static int resetProgress(CommandSourceStack source, ServerPlayer target) {
        HunterPlayerData data = HunterDataUtil.getOptional(target).orElse(null);
        if (data == null) {
            return 0;
        }
        data.resetAllProgress();
        data.ensureStarterQuests();
        HunterDataUtil.applyLevelBonuses(target);
        target.setHealth(target.getMaxHealth());
        HunterDataUtil.sync(target);
        source.sendSuccess(() -> Component.literal("Reset all Nen Unbound progression for " + target.getScoreboardName() + "."), true);
        return 1;
    }

    private static int setHatsuType(CommandSourceStack source, ServerPlayer target, String typeName) {
        HunterPlayerData data = HunterDataUtil.getOptional(target).orElse(null);
        if (data == null) {
            return 0;
        }
        NenType nenType = NenType.byNameOrNull(typeName);
        if (nenType == null) {
            source.sendFailure(Component.literal("Invalid Hatsu type. Use enhancement, emission, transmutation, conjuration, manipulation, or specialization."));
            return 0;
        }
        data.setNenType(nenType);
        HunterDataUtil.sync(target);
        source.sendSuccess(() -> Component.literal("Set " + target.getScoreboardName() + "'s Hatsu type to " + nenType.displayName() + "."), true);
        return 1;
    }

    private static int setHatsuTechnique(CommandSourceStack source, ServerPlayer target, String techniqueName) {
        HunterPlayerData data = HunterDataUtil.getOptional(target).orElse(null);
        if (data == null) {
            return 0;
        }
        String techniqueId = normalizeHatsuTechniqueId(techniqueName);
        if (techniqueId.isBlank()) {
            source.sendFailure(Component.literal("Invalid Hatsu technique. Use bungee gum, deep purple, or chain style."));
            return 0;
        }
        data.setNenTechniqueId(techniqueId);
        HunterDataUtil.sync(target);
        source.sendSuccess(() -> Component.literal("Set " + target.getScoreboardName() + "'s Hatsu technique to " + data.getNenTechniqueDisplayName() + "."), true);
        return 1;
    }

    private static String normalizeHatsuTechniqueId(String techniqueName) {
        String normalized = techniqueName == null ? "" : techniqueName.trim().toLowerCase().replace('-', '_').replace(' ', '_');
        return switch (normalized) {
            case "deep_purple", "smoke", "smokey", "smokey_jail" -> "deep_purple";
            case "bungee_gum", "elastic_aura", "gum" -> "elastic_aura";
            case "chain", "chains", "chain_style", "chain_nen", "kurapika" -> "chain_nen";
            default -> "";
        };
    }

    private static void unlockBaseNenAbilities(HunterPlayerData data) {
        data.setTenUnlocked(true);
        data.setZetsuUnlocked(true);
        data.setRenUnlocked(true);
        data.setGyoUnlocked(true);
        data.setEnUnlocked(true);
        data.setShuUnlocked(true);
        data.setKoUnlocked(true);
        data.setKenUnlocked(true);
        data.setRyuUnlocked(true);
    }

    private static void updateCollectQuests(ServerPlayer player, HunterPlayerData data) {
        boolean dirty = false;
        for (QuestDefinition quest : QuestRegistry.all()) {
            if (quest.objectiveType() != QuestObjectiveType.COLLECT_ITEM || !data.getActiveQuests().contains(quest.id())) {
                continue;
            }
            ResourceLocation itemId = ResourceLocation.tryParse(quest.targetId());
            if (itemId == null) {
                continue;
            }
            Item item = ForgeRegistries.ITEMS.getValue(itemId);
            if (item == null) {
                continue;
            }
            int count = player.getInventory().countItem(item);
            int current = data.getQuestProgress().getOrDefault(quest.id(), 0);
            if (count != current) {
                data.getQuestProgress().put(quest.id(), Math.min(count, data.getQuestTargetCount(quest.id())));
                dirty = true;
            }
            if (data.tryCompleteQuest(quest.id())) {
                if (PhoneQuestRegistry.isPhoneQuest(quest.id())) {
                    data.startPhoneQuestRefresh(player.level().getGameTime());
                }
                HunterDataUtil.grantQuestXp(player, data.getQuestRewardXp(quest.id()), Component.translatable(quest.titleKey()));
                dirty = false;
            }
        }
        if (dirty) {
            HunterDataUtil.sync(player);
        }
    }

    private static boolean isGuardWeapon(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        return stack.getItem() instanceof SwordItem
                || stack.getItem() instanceof AxeItem
                || stack.getItem() instanceof TridentItem;
    }

    private static void tickWingTrainerSpawns(ServerPlayer player, HunterPlayerData data) {
        if (player.tickCount % 200 != 0) {
            return;
        }
        if (data.getLevel() < HunterPlayerData.MAX_LEVEL || data.getNenQuestStage() == NenQuestStage.COMPLETED) {
            return;
        }
        AABB searchBox = player.getBoundingBox().inflate(96.0D);
        boolean hasAssignedWing = !player.level().getEntitiesOfClass(WingEntity.class, searchBox,
                wing -> wing.isAutoSpawnedTrainer() && wing.isTrainerAssignedTo(player)).isEmpty();
        if (hasAssignedWing || player.getRandom().nextFloat() > 0.14F) {
            return;
        }

        BlockPos spawnPos = findWingSpawnPos(player);
        if (spawnPos == null) {
            return;
        }

        WingEntity wing = HunterEntityTypes.WING.get().create(player.level());
        if (wing == null) {
            return;
        }
        wing.moveTo(spawnPos.getX() + 0.5D, spawnPos.getY(), spawnPos.getZ() + 0.5D, player.getYRot(), 0.0F);
        wing.setTrainerTarget(player, true);
        player.level().addFreshEntity(wing);
    }

    private static BlockPos findWingSpawnPos(ServerPlayer player) {
        for (int attempt = 0; attempt < 12; attempt++) {
            int xOffset = 18 + player.getRandom().nextInt(28);
            int zOffset = 18 + player.getRandom().nextInt(28);
            if (player.getRandom().nextBoolean()) {
                xOffset *= -1;
            }
            if (player.getRandom().nextBoolean()) {
                zOffset *= -1;
            }
            BlockPos surfacePos = player.level().getHeightmapPos(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    player.blockPosition().offset(xOffset, 0, zOffset));
            BlockPos spawnPos = surfacePos.above();
            BlockState feetState = player.level().getBlockState(spawnPos);
            BlockState headState = player.level().getBlockState(spawnPos.above());
            if (!feetState.isAir() || !headState.isAir()) {
                continue;
            }
            if (!player.level().getBlockState(spawnPos.below()).isFaceSturdy(player.level(), spawnPos.below(), Direction.UP)) {
                continue;
            }
            return spawnPos;
        }
        return null;
    }

    private static boolean routePickupIntoMainInventory(ServerPlayer player, ItemEntity itemEntity) {
        ItemStack entityStack = itemEntity.getItem();
        if (entityStack.isEmpty()) {
            return true;
        }

        Inventory inventory = player.getInventory();
        ItemStack remaining = entityStack.copy();
        for (int slot = 9; slot < inventory.items.size() && !remaining.isEmpty(); slot++) {
            ItemStack slotStack = inventory.items.get(slot);
            if (slotStack.isEmpty() || !ItemStack.isSameItemSameTags(slotStack, remaining)) {
                continue;
            }
            int transfer = Math.min(remaining.getCount(), Math.min(slotStack.getMaxStackSize(), inventory.getMaxStackSize()) - slotStack.getCount());
            if (transfer <= 0) {
                continue;
            }
            slotStack.grow(transfer);
            remaining.shrink(transfer);
        }
        for (int slot = 9; slot < inventory.items.size() && !remaining.isEmpty(); slot++) {
            if (!inventory.items.get(slot).isEmpty()) {
                continue;
            }
            int transfer = Math.min(remaining.getCount(), Math.min(remaining.getMaxStackSize(), inventory.getMaxStackSize()));
            inventory.items.set(slot, remaining.copyWithCount(transfer));
            remaining.shrink(transfer);
        }

        if (!remaining.isEmpty()) {
            return false;
        }

        player.take(itemEntity, entityStack.getCount());
        itemEntity.discard();
        inventory.setChanged();
        return false;
    }

    private static boolean tickNenQuestProgress(ServerPlayer player, HunterPlayerData data) {
        boolean changed = false;
        NenQuestStage stage = data.getNenQuestStage();

        if (data.getRenOverflowTicks() > 0) {
            data.setRenOverflowTicks(data.getRenOverflowTicks() - 1);
        } else if (data.getRenOverflowDamageWindow() > 0.0F && stage == NenQuestStage.REN_OVERFLOW) {
            data.setRenOverflowDamageWindow(0.0F);
            changed = true;
        }
        if (data.getAuraBurstTicks() > 0) {
            data.setAuraBurstTicks(data.getAuraBurstTicks() - 1);
        } else if (data.getAuraBurstKills() > 0 && stage == NenQuestStage.REN_AURA_BURST) {
            data.setAuraBurstKills(0);
            changed = true;
        }
        if (data.getNenLookCooldown() > 0) {
            data.setNenLookCooldown(data.getNenLookCooldown() - 1);
        }

        switch (stage) {
            case FEEL_THE_AURA -> {
                if (data.isMeditationCountdownActive() || data.isMeditationActive()) {
                    player.setDeltaMovement(0.0D, 0.0D, 0.0D);
                    player.hurtMarked = true;
                    MeditationPromptInputPacket.tickMeditation(player, data);
                    changed = true;
                }
            }
            case TEN_ENDURE -> {
                if (player.getHealth() <= player.getMaxHealth() * 0.30F) {
                    data.setEndureLowHpTicks(Math.min(NenQuestUtil.TEN_ENDURE_TICKS_REQUIRED, data.getEndureLowHpTicks() + 1));
                }
                changed = true;
            }
            case ZETSU_DISAPPEAR -> {
                WingEntity wing = player.level().getEntity(data.getZetsuTrialWingEntityId()) instanceof WingEntity wingEntity ? wingEntity : null;
                if (data.getZetsuTrialPrepTicks() > 0) {
                    data.setZetsuTrialPrepTicks(data.getZetsuTrialPrepTicks() - 1);
                    if (data.getZetsuTrialPrepTicks() <= 0) {
                        data.setZetsuTrialSearching(true);
                        data.setZetsuTrialSearchTicks(NenQuestUtil.ZETSU_HIDE_SEARCH_TICKS);
                        if (wing != null) {
                            wing.beginHideSearch();
                        }
                        player.sendSystemMessage(Component.literal("Wing: Time's up. Now I look."));
                    }
                    changed = true;
                } else if (data.isZetsuTrialSearching()) {
                    if (wing != null && wing.isHideTrialTrapped()) {
                        failZetsuTrial(player, data, wing, "Wing: The trial was compromised. Resetting the challenge.");
                        return true;
                    }
                    if (data.getZetsuTrialOrigin() != null && player.blockPosition().distSqr(data.getZetsuTrialOrigin()) > (NenQuestUtil.ZETSU_HIDE_RADIUS * NenQuestUtil.ZETSU_HIDE_RADIUS)) {
                        failZetsuTrial(player, data, wing, "Wing: You ran outside the trial zone. Again.");
                        return true;
                    }
                    if (data.getZetsuTrialOrigin() != null && player.getY() < data.getZetsuTrialOrigin().getY() - 6) {
                        failZetsuTrial(player, data, wing, "Wing: Underground is not the lesson. Hide properly.");
                        return true;
                    }
                    if (wing != null && wing.canSpotHideTarget(player)) {
                        failZetsuTrial(player, data, wing, "Wing: Found you. Again.");
                        return true;
                    }
                    data.setZetsuTrialSearchTicks(data.getZetsuTrialSearchTicks() - 1);
                    if (data.getZetsuTrialSearchTicks() <= 0) {
                        data.setZetsuTrialSearching(false);
                        data.setZetsuTrialComplete(true);
                        if (wing != null) {
                            wing.stopHideTrial();
                        }
                        player.sendSystemMessage(Component.literal("Wing: Good. For once, even I lost your outline."));
                    }
                    changed = true;
                }
            }
            case EN_LOOK -> {
                LivingEntity lookedTarget = getLookedMob(player);
                if (lookedTarget != null && data.getNenLookCooldown() <= 0 && !lookedTarget.getStringUUID().equals(data.getLastNenLookTargetUuid())) {
                    data.setLastNenLookTargetUuid(lookedTarget.getStringUUID());
                    data.setNenLookCooldown(10);
                    data.setEnLookedMobCount(Math.min(NenQuestUtil.EN_LOOK_REQUIRED, data.getEnLookedMobCount() + 1));
                    changed = true;
                }
            }
            case KEN_BALANCE -> {
                if (data.isTenActive() && data.isRenActive()) {
                    data.setKenBalanceTicks(Math.min(NenQuestUtil.KEN_BALANCE_TICKS_REQUIRED, data.getKenBalanceTicks() + 1));
                    changed = true;
                }
            }
            default -> {
            }
        }

        return changed;
    }

    private static LivingEntity getLookedMob(ServerPlayer player) {
        Vec3 eyePosition = player.getEyePosition();
        Vec3 lookVector = player.getLookAngle().normalize();
        AABB searchBox = player.getBoundingBox().expandTowards(lookVector.scale(24.0D)).inflate(12.0D);
        Mob bestTarget = null;
        double bestAlignment = 0.965D;
        double bestDistanceSqr = Double.MAX_VALUE;
        for (Mob mob : player.level().getEntitiesOfClass(Mob.class, searchBox, mob -> mob.isAlive() && player.hasLineOfSight(mob))) {
            Vec3 toTarget = mob.getEyePosition().subtract(eyePosition);
            double distanceSqr = toTarget.lengthSqr();
            if (distanceSqr <= 0.0001D || distanceSqr > (24.0D * 24.0D)) {
                continue;
            }
            Vec3 directionToTarget = toTarget.normalize();
            double alignment = directionToTarget.dot(lookVector);
            if (alignment < bestAlignment) {
                continue;
            }
            if (alignment > bestAlignment || distanceSqr < bestDistanceSqr) {
                bestAlignment = alignment;
                bestDistanceSqr = distanceSqr;
                bestTarget = mob;
            }
        }
        return bestTarget;
    }

    private static void completeRyuTrial(ServerPlayer player, WingEntity wing) {
        HunterPlayerData data = HunterDataUtil.getOptional(player).orElse(null);
        if (data == null || data.getNenQuestStage() != NenQuestStage.RYU_SHIFT_AURA) {
            return;
        }
        data.setRyuFightFinished(true);
        data.setRyuFightStarted(false);
        wing.completeRyuTrial(player);
        player.sendSystemMessage(Component.literal("Wing: Enough. That is the control I wanted to see."));
        HunterDataUtil.sync(player);
    }

    private static void failZetsuTrial(ServerPlayer player, HunterPlayerData data, WingEntity wing, String message) {
        data.resetZetsuTrial();
        if (wing != null) {
            wing.stopHideTrial();
        }
        player.sendSystemMessage(Component.literal(message));
        HunterDataUtil.sync(player);
    }

    private static float getNenMeleeBonusScale(ServerPlayer attacker, HunterPlayerData data) {
        if (data.getChargeTicksRemaining() > 0 || data.getActiveAbilityTicksRemaining() > 0 || data.isMartialArtsGrabActive()) {
            return 1.0F;
        }
        return Math.max(0.0F, Math.min(1.0F, attacker.getAttackStrengthScale(0.0F)));
    }

    private static void handleEnIntrusionAlerts(ServerPlayer player, HunterPlayerData data) {
        if (!data.isEnActive()) {
            data.clearEnTrackedPlayers();
            return;
        }

        double radius = HunterAbilities.EN.getRadius(data);
        double radiusSqr = radius * radius;
        Set<String> currentPlayersInEn = new HashSet<>();
        for (ServerPlayer other : player.serverLevel().players()) {
            if (other == player || !other.isAlive() || other.isSpectator()) {
                continue;
            }
            if (FactionUtil.areFactionMates(player, other)) {
                continue;
            }
            HunterPlayerData otherData = HunterDataUtil.getOptional(other).orElse(null);
            if (otherData != null && otherData.isZetsuActive()) {
                continue;
            }
            if (player.distanceToSqr(other) > radiusSqr) {
                continue;
            }

            String uuid = other.getStringUUID();
            currentPlayersInEn.add(uuid);
            if (data.getEnTrackedPlayers().add(uuid)) {
                player.sendSystemMessage(Component.literal(other.getScoreboardName() + " entered your En."));
                player.level().playSound(null, player.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.45F, 0.6F);
            }
        }
        data.getEnTrackedPlayers().retainAll(currentPlayersInEn);
    }

    private static boolean hasSameAttackerIFrame(LivingEntity victim, String attackerId) {
        if (attackerId == null || attackerId.isBlank()) {
            return false;
        }
        CompoundTag iframeData = victim.getPersistentData().getCompound(SAME_ATTACKER_IFRAME_TAG);
        return iframeData.getInt(attackerId) > 0;
    }

    private static void triggerSameAttackerIFrame(LivingEntity victim, String attackerId, int ticks) {
        if (attackerId == null || attackerId.isBlank() || ticks <= 0) {
            return;
        }
        CompoundTag persistentData = victim.getPersistentData();
        CompoundTag iframeData = persistentData.getCompound(SAME_ATTACKER_IFRAME_TAG);
        iframeData.putInt(attackerId, ticks);
        persistentData.put(SAME_ATTACKER_IFRAME_TAG, iframeData);
    }

    private static void tickSameAttackerIFrames(LivingEntity living) {
        CompoundTag persistentData = living.getPersistentData();
        if (!persistentData.contains(SAME_ATTACKER_IFRAME_TAG, CompoundTag.TAG_COMPOUND)) {
            return;
        }
        CompoundTag iframeData = persistentData.getCompound(SAME_ATTACKER_IFRAME_TAG);
        for (String key : iframeData.getAllKeys().toArray(String[]::new)) {
            int ticks = Math.max(0, iframeData.getInt(key) - 1);
            if (ticks <= 0) {
                iframeData.remove(key);
            } else {
                iframeData.putInt(key, ticks);
            }
        }
        if (iframeData.isEmpty()) {
            persistentData.remove(SAME_ATTACKER_IFRAME_TAG);
        } else {
            persistentData.put(SAME_ATTACKER_IFRAME_TAG, iframeData);
        }
    }

}
