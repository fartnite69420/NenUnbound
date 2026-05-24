package com.huntercraft.huntercraft.util;

import com.huntercraft.huntercraft.data.HunterPlayerData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

public final class NenVowUtil {
    private NenVowUtil() {
    }

    public static boolean hasVow(HunterPlayerData data, String abilityId) {
        return data != null && abilityId != null && !data.getAbilityVowFaction(abilityId).isBlank();
    }

    public static float getEffectiveness(ServerPlayer source, HunterPlayerData sourceData, String abilityId, LivingEntity target) {
        if (source == null || sourceData == null || abilityId == null || target == null) {
            return 1.0F;
        }
        String vowedPlayers = sourceData.getAbilityVowFaction(abilityId);
        if (vowedPlayers.isBlank() || !(target instanceof ServerPlayer targetPlayer)) {
            return 1.0F;
        }
        return sourceData.isAbilityVowedAgainstPlayer(abilityId, targetPlayer.getGameProfile().getName()) ? 1.5F : 0.5F;
    }

    public static boolean matchesRequiredVow(ServerPlayer source, HunterPlayerData sourceData, String abilityId, LivingEntity target) {
        if (source == null || sourceData == null || abilityId == null || !(target instanceof ServerPlayer targetPlayer)) {
            return false;
        }
        String vowedPlayers = sourceData.getAbilityVowFaction(abilityId);
        if (vowedPlayers.isBlank()) {
            return false;
        }
        return sourceData.isAbilityVowedAgainstPlayer(abilityId, targetPlayer.getGameProfile().getName());
    }
}
