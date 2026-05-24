package com.huntercraft.huntercraft.network;

import com.huntercraft.huntercraft.HunterCraftMod;
import com.huntercraft.huntercraft.network.packet.AdjustSkillPointsPacket;
import com.huntercraft.huntercraft.network.packet.AcceptPhoneQuestPacket;
import com.huntercraft.huntercraft.network.packet.AdvanceNenQuestPacket;
import com.huntercraft.huntercraft.network.packet.AfterImagePacket;
import com.huntercraft.huntercraft.network.packet.CancelPhoneQuestPacket;
import com.huntercraft.huntercraft.network.packet.ChainJailMinigameInputPacket;
import com.huntercraft.huntercraft.network.packet.CreateFactionPacket;
import com.huntercraft.huntercraft.network.packet.DisbandFactionPacket;
import com.huntercraft.huntercraft.network.packet.InviteFactionPlayerPacket;
import com.huntercraft.huntercraft.network.packet.LeaveFactionPacket;
import com.huntercraft.huntercraft.network.packet.MeditationPromptInputPacket;
import com.huntercraft.huntercraft.network.packet.RequestFactionViewPacket;
import com.huntercraft.huntercraft.network.packet.RespondFactionInvitePacket;
import com.huntercraft.huntercraft.network.packet.SetNenAuraColorPacket;
import com.huntercraft.huntercraft.network.packet.SetNenStatePacket;
import com.huntercraft.huntercraft.network.packet.SelectJudgmentAbilityPacket;
import com.huntercraft.huntercraft.network.packet.SetCombatVowPacket;
import com.huntercraft.huntercraft.network.packet.SetScarletEyesOffsetPacket;
import com.huntercraft.huntercraft.network.packet.SetVowFactionPacket;
import com.huntercraft.huntercraft.network.packet.SyncHunterDataPacket;
import com.huntercraft.huntercraft.network.packet.SwitchCombatBarPacket;
import com.huntercraft.huntercraft.network.packet.ToggleEmptyHandsPickupPacket;
import com.huntercraft.huntercraft.network.packet.ToggleCombatBarPacket;
import com.huntercraft.huntercraft.network.packet.UnlockNenTechniqueNodePacket;
import com.huntercraft.huntercraft.network.packet.UpdateCombatSlotPacket;
import com.huntercraft.huntercraft.network.packet.UseCombatAbilityPacket;
import com.huntercraft.huntercraft.network.packet.UseTechniquePacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public final class HunterNetwork {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(HunterCraftMod.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int packetId;

    private HunterNetwork() {
    }

    public static void register() {
        CHANNEL.messageBuilder(SyncHunterDataPacket.class, packetId++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(SyncHunterDataPacket::encode)
                .decoder(SyncHunterDataPacket::decode)
                .consumerMainThread(SyncHunterDataPacket::handle)
                .add();

        CHANNEL.messageBuilder(AfterImagePacket.class, packetId++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(AfterImagePacket::encode)
                .decoder(AfterImagePacket::decode)
                .consumerMainThread(AfterImagePacket::handle)
                .add();

        CHANNEL.messageBuilder(UseTechniquePacket.class, packetId++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(UseTechniquePacket::encode)
                .decoder(UseTechniquePacket::decode)
                .consumerMainThread(UseTechniquePacket::handle)
                .add();

        CHANNEL.messageBuilder(AdjustSkillPointsPacket.class, packetId++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(AdjustSkillPointsPacket::encode)
                .decoder(AdjustSkillPointsPacket::decode)
                .consumerMainThread(AdjustSkillPointsPacket::handle)
                .add();

        CHANNEL.messageBuilder(UnlockNenTechniqueNodePacket.class, packetId++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(UnlockNenTechniqueNodePacket::encode)
                .decoder(UnlockNenTechniqueNodePacket::decode)
                .consumerMainThread(UnlockNenTechniqueNodePacket::handle)
                .add();

        CHANNEL.messageBuilder(UseCombatAbilityPacket.class, packetId++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(UseCombatAbilityPacket::encode)
                .decoder(UseCombatAbilityPacket::decode)
                .consumerMainThread(UseCombatAbilityPacket::handle)
                .add();

        CHANNEL.messageBuilder(UpdateCombatSlotPacket.class, packetId++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(UpdateCombatSlotPacket::encode)
                .decoder(UpdateCombatSlotPacket::decode)
                .consumerMainThread(UpdateCombatSlotPacket::handle)
                .add();

        CHANNEL.messageBuilder(SwitchCombatBarPacket.class, packetId++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(SwitchCombatBarPacket::encode)
                .decoder(SwitchCombatBarPacket::decode)
                .consumerMainThread(SwitchCombatBarPacket::handle)
                .add();

        CHANNEL.messageBuilder(ToggleEmptyHandsPickupPacket.class, packetId++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(ToggleEmptyHandsPickupPacket::encode)
                .decoder(ToggleEmptyHandsPickupPacket::decode)
                .consumerMainThread(ToggleEmptyHandsPickupPacket::handle)
                .add();

        CHANNEL.messageBuilder(SetNenStatePacket.class, packetId++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(SetNenStatePacket::encode)
                .decoder(SetNenStatePacket::decode)
                .consumerMainThread(SetNenStatePacket::handle)
                .add();

        CHANNEL.messageBuilder(SetNenAuraColorPacket.class, packetId++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(SetNenAuraColorPacket::encode)
                .decoder(SetNenAuraColorPacket::decode)
                .consumerMainThread(SetNenAuraColorPacket::handle)
                .add();

        CHANNEL.messageBuilder(SetScarletEyesOffsetPacket.class, packetId++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(SetScarletEyesOffsetPacket::encode)
                .decoder(SetScarletEyesOffsetPacket::decode)
                .consumerMainThread(SetScarletEyesOffsetPacket::handle)
                .add();

        CHANNEL.messageBuilder(SetVowFactionPacket.class, packetId++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(SetVowFactionPacket::encode)
                .decoder(SetVowFactionPacket::decode)
                .consumerMainThread(SetVowFactionPacket::handle)
                .add();

        CHANNEL.messageBuilder(SetCombatVowPacket.class, packetId++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(SetCombatVowPacket::encode)
                .decoder(SetCombatVowPacket::decode)
                .consumerMainThread(SetCombatVowPacket::handle)
                .add();

        CHANNEL.messageBuilder(ToggleCombatBarPacket.class, packetId++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(ToggleCombatBarPacket::encode)
                .decoder(ToggleCombatBarPacket::decode)
                .consumerMainThread(ToggleCombatBarPacket::handle)
                .add();

        CHANNEL.messageBuilder(CreateFactionPacket.class, packetId++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(CreateFactionPacket::encode)
                .decoder(CreateFactionPacket::decode)
                .consumerMainThread(CreateFactionPacket::handle)
                .add();

        CHANNEL.messageBuilder(InviteFactionPlayerPacket.class, packetId++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(InviteFactionPlayerPacket::encode)
                .decoder(InviteFactionPlayerPacket::decode)
                .consumerMainThread(InviteFactionPlayerPacket::handle)
                .add();

        CHANNEL.messageBuilder(LeaveFactionPacket.class, packetId++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(LeaveFactionPacket::encode)
                .decoder(LeaveFactionPacket::decode)
                .consumerMainThread(LeaveFactionPacket::handle)
                .add();

        CHANNEL.messageBuilder(DisbandFactionPacket.class, packetId++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(DisbandFactionPacket::encode)
                .decoder(DisbandFactionPacket::decode)
                .consumerMainThread(DisbandFactionPacket::handle)
                .add();

        CHANNEL.messageBuilder(RespondFactionInvitePacket.class, packetId++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(RespondFactionInvitePacket::encode)
                .decoder(RespondFactionInvitePacket::decode)
                .consumerMainThread(RespondFactionInvitePacket::handle)
                .add();

        CHANNEL.messageBuilder(RequestFactionViewPacket.class, packetId++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(RequestFactionViewPacket::encode)
                .decoder(RequestFactionViewPacket::decode)
                .consumerMainThread(RequestFactionViewPacket::handle)
                .add();

        CHANNEL.messageBuilder(AcceptPhoneQuestPacket.class, packetId++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(AcceptPhoneQuestPacket::encode)
                .decoder(AcceptPhoneQuestPacket::decode)
                .consumerMainThread(AcceptPhoneQuestPacket::handle)
                .add();

        CHANNEL.messageBuilder(AdvanceNenQuestPacket.class, packetId++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(AdvanceNenQuestPacket::encode)
                .decoder(AdvanceNenQuestPacket::decode)
                .consumerMainThread(AdvanceNenQuestPacket::handle)
                .add();

        CHANNEL.messageBuilder(MeditationPromptInputPacket.class, packetId++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(MeditationPromptInputPacket::encode)
                .decoder(MeditationPromptInputPacket::decode)
                .consumerMainThread(MeditationPromptInputPacket::handle)
                .add();

        CHANNEL.messageBuilder(CancelPhoneQuestPacket.class, packetId++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(CancelPhoneQuestPacket::encode)
                .decoder(CancelPhoneQuestPacket::decode)
                .consumerMainThread(CancelPhoneQuestPacket::handle)
                .add();

        CHANNEL.messageBuilder(ChainJailMinigameInputPacket.class, packetId++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(ChainJailMinigameInputPacket::encode)
                .decoder(ChainJailMinigameInputPacket::decode)
                .consumerMainThread(ChainJailMinigameInputPacket::handle)
                .add();

        CHANNEL.messageBuilder(SelectJudgmentAbilityPacket.class, packetId++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(SelectJudgmentAbilityPacket::encode)
                .decoder(SelectJudgmentAbilityPacket::decode)
                .consumerMainThread(SelectJudgmentAbilityPacket::handle)
                .add();
    }

    public static void sendToPlayer(ServerPlayer player, Object packet) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }

    public static void sendToServer(Object packet) {
        CHANNEL.sendToServer(packet);
    }

    public static void sendToTrackingAndSelf(Entity entity, Object packet) {
        CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity), packet);
    }
}
