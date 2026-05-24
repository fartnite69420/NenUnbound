package com.huntercraft.huntercraft.util;

import com.huntercraft.huntercraft.abilities.HunterAbilities;
import com.huntercraft.huntercraft.HunterCraftMod;
import com.huntercraft.huntercraft.data.HunterPlayerData;
import com.huntercraft.huntercraft.data.HunterPlayerDataProvider;
import com.huntercraft.huntercraft.effect.HunterMobEffects;
import com.huntercraft.huntercraft.faction.FactionUtil;
import com.huntercraft.huntercraft.network.HunterNetwork;
import com.huntercraft.huntercraft.network.packet.SyncHunterDataPacket;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

import java.util.Optional;
import java.util.UUID;

public final class HunterDataUtil {
    public static final UUID HEALTH_MODIFIER_ID = UUID.fromString("fdab2e09-3dfd-4375-9b58-d71b72f84bf6");
    public static final UUID ARMOR_MODIFIER_ID = UUID.fromString("0b3ddc1f-47ef-4f8b-9d2d-b3ad51393a34");
    public static final UUID NEN_TOUGHNESS_MODIFIER_ID = UUID.fromString("8f3f8f7b-3fbf-4b5b-9c50-0e5a8bd0a4e1");
    public static final UUID ATTACK_SPEED_MODIFIER_ID = UUID.fromString("54a39cef-5718-4c50-a89f-9856d2dd6c7e");
    private static final String PARRIED_SOURCE_UUID_TAG = "HuntercraftParriedSourceUuid";
    private static final String PARRIED_SOURCE_TICK_TAG = "HuntercraftParriedSourceTick";

    private HunterDataUtil() {
    }

    public static Optional<HunterPlayerData> getOptional(Player player) {
        return player.getCapability(HunterPlayerDataProvider.CAPABILITY).resolve();
    }

    public static HunterPlayerData get(Player player) {
        return getOptional(player)
                .orElseThrow(() -> new IllegalStateException("Hunter player data missing for " + player.getScoreboardName()));
    }

    public static void sync(ServerPlayer player) {
        getOptional(player).ifPresent(data -> {
            FactionUtil.populateClientView(player, data);
            HunterNetwork.sendToTrackingAndSelf(player, new SyncHunterDataPacket(player.getId(), data.serializeNBT()));
        });
    }

    public static void syncAndRefresh(ServerPlayer player) {
        applyLevelBonuses(player);
        sync(player);
    }

    public static void applyLevelBonuses(ServerPlayer player) {
        HunterPlayerData data = getOptional(player).orElse(null);
        if (data == null) {
            return;
        }
        AttributeInstance health = player.getAttribute(Attributes.MAX_HEALTH);
        if (health != null) {
            health.removeModifier(HEALTH_MODIFIER_ID);
            if (data.getBonusHealth() > 0) {
                health.addPermanentModifier(new AttributeModifier(HEALTH_MODIFIER_ID, HunterCraftMod.MODID + "_level_health", data.getBonusHealth(), AttributeModifier.Operation.ADDITION));
            }
        }
        AttributeInstance armor = player.getAttribute(Attributes.ARMOR);
        if (armor != null) {
            armor.removeModifier(ARMOR_MODIFIER_ID);
            double passiveArmor = data.getPassiveArmor();
            double wornArmor = Math.max(0.0D, player.getArmorValue());
            double missingPassiveArmor = Math.max(0.0D, passiveArmor - wornArmor);
            if (missingPassiveArmor > 0.0D) {
                armor.addPermanentModifier(new AttributeModifier(ARMOR_MODIFIER_ID, HunterCraftMod.MODID + "_passive_armor", missingPassiveArmor, AttributeModifier.Operation.ADDITION));
            }
        }
        AttributeInstance toughness = player.getAttribute(Attributes.ARMOR_TOUGHNESS);
        if (toughness != null) {
            toughness.removeModifier(NEN_TOUGHNESS_MODIFIER_ID);
            double nenToughness = getActiveNenToughnessBonus(data);
            if (nenToughness > 0.0D) {
                toughness.addPermanentModifier(new AttributeModifier(NEN_TOUGHNESS_MODIFIER_ID, HunterCraftMod.MODID + "_nen_toughness", nenToughness, AttributeModifier.Operation.ADDITION));
            }
        }
        AttributeInstance attackSpeed = player.getAttribute(Attributes.ATTACK_SPEED);
        if (attackSpeed != null) {
            attackSpeed.removeModifier(ATTACK_SPEED_MODIFIER_ID);
            attackSpeed.addPermanentModifier(new AttributeModifier(ATTACK_SPEED_MODIFIER_ID, HunterCraftMod.MODID + "_slower_attack_speed", -1.0D / 3.0D, AttributeModifier.Operation.MULTIPLY_TOTAL));
        }
        if (player.getHealth() > player.getMaxHealth()) {
            player.setHealth(player.getMaxHealth());
        }
    }

    private static double getActiveNenToughnessBonus(HunterPlayerData data) {
        if (data == null || data.isZetsuActive()) {
            return 0.0D;
        }
        double levelScale = Math.max(0.0D, Math.min(1.0D, (data.getNenLevel() - 1) / 9.0D));
        if (data.isTenActive()) {
            return 8.0D + (16.0D * levelScale);
        }
        if (data.isKenActive()) {
            return 5.0D + (11.0D * levelScale);
        }
        return 0.0D;
    }

    public static void grantQuestXp(ServerPlayer player, int xp, Component questName) {
        HunterPlayerData data = getOptional(player).orElse(null);
        if (data == null) {
            return;
        }
        data.addXp(xp);
        player.sendSystemMessage(Component.translatable("message.huntercraft.quest_complete", questName, xp));
        sync(player);
    }

    public static void applyStun(LivingEntity target, ServerPlayer source, int durationTicks) {
        applyStun(target, source, durationTicks, false);
    }

    public static void applyParryStun(LivingEntity target, ServerPlayer source, int durationTicks) {
        applyStun(target, source, durationTicks, true);
    }

    private static void applyStun(LivingEntity target, ServerPlayer source, int durationTicks, boolean parryStun) {
        if (source != null && target instanceof Player targetPlayer && FactionUtil.areFactionMates(source, targetPlayer)) {
            return;
        }
        if (wasRecentlyParried(target, source)) {
            return;
        }
        int adjustedDuration = Math.max(1, durationTicks);
        if (source != null && target instanceof ServerPlayer stunnedPlayer && source != stunnedPlayer) {
            HunterPlayerData targetData = getOptional(stunnedPlayer).orElse(null);
            if (targetData != null) {
                adjustedDuration = Math.max(1, Math.round(durationTicks * HunterAbilities.FLOW_STATE.getStunFactor(targetData)));
            }
        }
        target.addEffect(new MobEffectInstance(parryStun ? HunterMobEffects.PARRY_STUNNED.get() : HunterMobEffects.STUNNED.get(), adjustedDuration, 0, false, false, true));
    }

    public static void markParriedHit(LivingEntity target, Entity attacker) {
        if (target == null || attacker == null) {
            return;
        }
        target.getPersistentData().putUUID(PARRIED_SOURCE_UUID_TAG, attacker.getUUID());
        target.getPersistentData().putLong(PARRIED_SOURCE_TICK_TAG, target.level().getGameTime());
    }

    public static boolean wasRecentlyParried(LivingEntity target, Entity attacker) {
        if (target == null || attacker == null || !target.getPersistentData().hasUUID(PARRIED_SOURCE_UUID_TAG)) {
            return false;
        }
        if (!target.getPersistentData().getUUID(PARRIED_SOURCE_UUID_TAG).equals(attacker.getUUID())) {
            return false;
        }
        long parryTick = target.getPersistentData().getLong(PARRIED_SOURCE_TICK_TAG);
        return target.level().getGameTime() - parryTick <= 2L;
    }
}
