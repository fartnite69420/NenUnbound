package com.huntercraft.huntercraft.network.packet;

import com.huntercraft.huntercraft.abilities.AbilitySourceType;
import com.huntercraft.huntercraft.abilities.HunterAbilities;
import com.huntercraft.huntercraft.abilities.SkillTreeCombatAbility;
import com.huntercraft.huntercraft.data.HunterPlayerData;
import com.huntercraft.huntercraft.util.HunterDataUtil;
import com.huntercraft.huntercraft.util.NenVowUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public record SelectJudgmentAbilityPacket(String abilityId) {
    public static void encode(SelectJudgmentAbilityPacket packet, FriendlyByteBuf buffer) {
        buffer.writeUtf(packet.abilityId == null ? "" : packet.abilityId, 64);
    }

    public static SelectJudgmentAbilityPacket decode(FriendlyByteBuf buffer) {
        return new SelectJudgmentAbilityPacket(buffer.readUtf(64));
    }

    public static void handle(SelectJudgmentAbilityPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        ServerPlayer owner = context.getSender();
        if (owner == null) {
            context.setPacketHandled(true);
            return;
        }
        context.enqueueWork(() -> {
            HunterPlayerData ownerData = HunterDataUtil.get(owner);
            String targetUuid = ownerData.getPendingJudgmentChainTargetUuid();
            if (targetUuid.isBlank()) {
                return;
            }
            ServerPlayer target;
            try {
                target = owner.serverLevel().getPlayerByUUID(UUID.fromString(targetUuid)) instanceof ServerPlayer serverPlayer ? serverPlayer : null;
            } catch (IllegalArgumentException exception) {
                target = null;
            }
            if (target == null || !NenVowUtil.matchesRequiredVow(owner, ownerData, "judgment_chain", target)) {
                ownerData.clearPendingJudgmentChainTarget();
                HunterDataUtil.sync(owner);
                return;
            }
            if (!(HunterAbilities.byId(packet.abilityId) instanceof SkillTreeCombatAbility ability)
                    || !ability.hasSourceType(AbilitySourceType.NEN)) {
                return;
            }
            HunterPlayerData targetData = HunterDataUtil.get(target);
            if (!ability.isUnlocked(targetData)) {
                owner.sendSystemMessage(Component.literal("That target does not have " + ability.displayName() + " unlocked."));
                return;
            }
            targetData.addJudgmentDisabledAbility(ability.id());
            ownerData.clearPendingJudgmentChainTarget();
            owner.sendSystemMessage(Component.literal("Judgment Chain bound " + ability.displayName() + "."));
            target.sendSystemMessage(Component.literal("Judgment Chain sealed " + ability.displayName() + " until death."));
            HunterDataUtil.sync(owner);
            HunterDataUtil.sync(target);
        });
        context.setPacketHandled(true);
    }
}
