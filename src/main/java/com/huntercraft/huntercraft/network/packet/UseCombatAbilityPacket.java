package com.huntercraft.huntercraft.network.packet;

import com.huntercraft.huntercraft.abilities.HunterAbilities;
import com.huntercraft.huntercraft.abilities.AbilitySourceType;
import com.huntercraft.huntercraft.abilities.SkillTreeCombatAbility;
import com.huntercraft.huntercraft.abilities.nenability.NenTechniqueAbility;
import com.huntercraft.huntercraft.data.HunterPlayerData;
import com.huntercraft.huntercraft.effect.HunterMobEffects;
import com.huntercraft.huntercraft.sound.HunterSoundEvents;
import com.huntercraft.huntercraft.util.HunterDataUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class UseCombatAbilityPacket {
    private final String abilityId;
    private final Vec3 direction;

    public UseCombatAbilityPacket(String abilityId, Vec3 direction) {
        this.abilityId = abilityId;
        this.direction = direction;
    }

    public static void encode(UseCombatAbilityPacket packet, FriendlyByteBuf buffer) {
        buffer.writeUtf(packet.abilityId);
        buffer.writeDouble(packet.direction.x);
        buffer.writeDouble(packet.direction.y);
        buffer.writeDouble(packet.direction.z);
    }

    public static UseCombatAbilityPacket decode(FriendlyByteBuf buffer) {
        return new UseCombatAbilityPacket(
                buffer.readUtf(),
                new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble())
        );
    }

    public static void handle(UseCombatAbilityPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        ServerPlayer player = context.getSender();
        if (player == null) {
            context.setPacketHandled(true);
            return;
        }

        context.enqueueWork(() -> {
            if (player.isSpectator()) {
                return;
            }
            HunterPlayerData data = HunterDataUtil.get(player);
            if (!(HunterAbilities.byId(packet.abilityId) instanceof SkillTreeCombatAbility ability)) {
                return;
            }
            if (player.hasEffect(HunterMobEffects.STUNNED.get()) && !isAllowedWhileStunned(ability)) {
                return;
            }
            if (player.hasEffect(HunterMobEffects.PARRY_STUNNED.get())) {
                return;
            }
            if (data.isJudgmentDisabledAbility(packet.abilityId)) {
                player.sendSystemMessage(Component.literal("Judgment Chain is binding that ability."));
                return;
            }
            if (!ability.canUse(player, data)) {
                Component failureMessage = ability.getUseFailureMessage(player, data);
                if (failureMessage != null) {
                    player.sendSystemMessage(failureMessage);
                }
                return;
            }
            int staminaCost = ability.getStaminaCost(data);
            int previousCooldown = ability.getCurrentCooldown(data);
            if (staminaCost > 0 && !data.consumeStamina(staminaCost)) {
                player.sendSystemMessage(Component.literal("You need more stamina to use " + ability.displayName() + "."));
                return;
            }
            String activeAbilityId = data.getActiveAbilityId();
            if (!activeAbilityId.isBlank() && !activeAbilityId.equals(packet.abilityId)
                    && HunterAbilities.byId(activeAbilityId) instanceof SkillTreeCombatAbility activeAbility
                    && !activeAbility.isPassiveWhileActive()) {
                activeAbility.stop(player, data);
                if (data.isActiveAbility(activeAbilityId)) {
                    data.clearActiveAbility();
                    if (activeAbility.getCurrentCooldown(data) <= 0) {
                        data.setAbilityCooldown(activeAbilityId, activeAbility.getMaxCooldownTicks());
                    }
                }
            }
            ability.use(player, data, packet.direction);
            boolean started = ability.getCurrentCooldown(data) > previousCooldown
                    || data.isActiveAbility(packet.abilityId)
                    || data.isChargingAbility(packet.abilityId);
            if (!started && staminaCost > 0) {
                data.setAbilityCooldown(packet.abilityId, 10);
            } else if (started) {
                playAbilitySound(player, ability);
            }
            HunterDataUtil.sync(player);
        });

        context.setPacketHandled(true);
    }

    private static boolean isAllowedWhileStunned(SkillTreeCombatAbility ability) {
        return ability instanceof NenTechniqueAbility;
    }

    private static void playAbilitySound(ServerPlayer player, SkillTreeCombatAbility ability) {
        if (suppressesAutoAbilitySound(ability.id())) {
            return;
        }
        SoundEvent sound = ability.hasSourceType(AbilitySourceType.SHARP)
                ? HunterSoundEvents.SLASH.get()
                : HunterSoundEvents.PUNCH.get();
        float pitch = ability.hasSourceType(AbilitySourceType.NEN) ? 0.82F : 1.0F;
        if (usesTeleportSound(ability.id())) {
            sound = HunterSoundEvents.TELEPORT.get();
            pitch = 1.05F;
        } else if (usesMovementDashSound(ability.id())) {
            sound = HunterSoundEvents.DASH.get();
            pitch = 1.0F;
        } else if (ability.id().contains("dive") || ability.id().contains("heel") || ability.id().contains("drop")) {
            sound = HunterSoundEvents.GROUND_SMASH.get();
            pitch = 0.8F;
        }
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), sound, SoundSource.PLAYERS, 0.85F, pitch);
    }

    private static boolean usesTeleportSound(String abilityId) {
        return "ghost_step".equals(abilityId)
                || "void_rend".equals(abilityId)
                || "phantom_ring".equals(abilityId);
    }

    private static boolean usesMovementDashSound(String abilityId) {
        return abilityId.contains("dash")
                || abilityId.contains("rush")
                || abilityId.contains("arc")
                || "acute".equals(abilityId)
                || "unseen_blade".equals(abilityId)
                || "lion_fang_draw".equals(abilityId)
                || "skybreaker_dive".equals(abilityId)
                || "meteor_heel".equals(abilityId)
                || "tora_hunt".equals(abilityId);
    }

    private static boolean suppressesAutoAbilitySound(String abilityId) {
        return switch (abilityId) {
            case "smokey_chain", "smoke_soldier", "smoke_clone", "smokey_jail",
                    "gum_attach", "elastic_pull", "elastic_trap", "elastic_reflect", "texture_surprise",
                    "dowsing_chain", "holy_chain", "chain_jail", "judgment_chain", "steal_chain" -> true;
            default -> false;
        };
    }
}
