package com.huntercraft.huntercraft.network.packet;

import com.huntercraft.huntercraft.HunterConfig;
import com.huntercraft.huntercraft.data.HunterPlayerData;
import com.huntercraft.huntercraft.entity.WingEntity;
import com.huntercraft.huntercraft.quest.NenQuestStage;
import com.huntercraft.huntercraft.quest.NenQuestUtil;
import com.huntercraft.huntercraft.quest.NenType;
import com.huntercraft.huntercraft.util.HunterDataUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class AdvanceNenQuestPacket {
    private final String action;
    private final int entityId;
    private final String choice;

    public AdvanceNenQuestPacket(String action, int entityId, String choice) {
        this.action = action;
        this.entityId = entityId;
        this.choice = choice;
    }

    public static void encode(AdvanceNenQuestPacket packet, FriendlyByteBuf buffer) {
        buffer.writeUtf(packet.action);
        buffer.writeInt(packet.entityId);
        buffer.writeUtf(packet.choice);
    }

    public static AdvanceNenQuestPacket decode(FriendlyByteBuf buffer) {
        return new AdvanceNenQuestPacket(buffer.readUtf(), buffer.readInt(), buffer.readUtf());
    }

    public static void handle(AdvanceNenQuestPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        ServerPlayer player = context.getSender();
        if (player == null) {
            context.setPacketHandled(true);
            return;
        }

        context.enqueueWork(() -> {
            HunterPlayerData data = HunterDataUtil.get(player);
            switch (packet.action) {
                case "begin" -> {
                    if (data.getNenQuestStage() == NenQuestStage.NOT_STARTED) {
                        data.setNenQuestStage(NenQuestStage.FEEL_THE_AURA);
                    }
                }
                case "start_meditation" -> {
                    if (data.getNenQuestStage() == NenQuestStage.FEEL_THE_AURA && !NenQuestUtil.isStageComplete(data)) {
                        MeditationPromptInputPacket.startMeditation(player, data);
                    }
                }
                case "claim" -> claimStage(player, data);
                case "start_zetsu" -> {
                    if (data.getNenQuestStage() == NenQuestStage.ZETSU_DISAPPEAR && player.level().getEntity(packet.entityId) instanceof WingEntity wing) {
                        data.resetZetsuTrial();
                        data.setZetsuTrialPrepTicks(NenQuestUtil.ZETSU_HIDE_PREP_TICKS);
                        data.setZetsuTrialWingEntityId(packet.entityId);
                        data.setZetsuTrialOrigin(player.blockPosition());
                        wing.startHideTrial(player, player.blockPosition());
                        player.sendSystemMessage(Component.literal("Wing: Thirty seconds. Hide."));
                    }
                }
                case "open_nodes" -> {
                    if (data.getNenQuestStage() == NenQuestStage.OPEN_THE_NODES && player.level().getEntity(packet.entityId) instanceof WingEntity wing) {
                        player.hurt(wing.damageSources().mobAttack(wing), 70.0F);
                        data.setNodeDamageTaken(true);
                    }
                }
                case "choose_type" -> {
                    if (data.getNenQuestStage() == NenQuestStage.HATSU_CHOOSE_TYPE && !packet.choice.isBlank()) {
                        NenType type = HunterConfig.RANDOM_HATSU_TYPE.get()
                                ? getRandomNonSpecialistType(player)
                                : NenType.byName(packet.choice);
                        if (type != NenType.SPECIALIZATION) {
                            data.setNenType(type);
                            if (HunterConfig.RANDOM_HATSU_TYPE.get()) {
                                player.sendSystemMessage(Component.literal("Wing: Your aura settled into " + type.displayName() + "."));
                            }
                        }
                    }
                }
                case "start_ryu" -> {
                    if (data.getNenQuestStage() == NenQuestStage.RYU_SHIFT_AURA && player.level().getEntity(packet.entityId) instanceof WingEntity wing) {
                        wing.startSpar(player);
                        data.setRyuFightStarted(true);
                        player.sendSystemMessage(Component.literal("Wing takes a stance. The Ryu trial begins."));
                    }
                }
                default -> {
                }
            }
            HunterDataUtil.sync(player);
        });
        context.setPacketHandled(true);
    }

    private static NenType getRandomNonSpecialistType(ServerPlayer player) {
        NenType[] types = {
                NenType.ENHANCEMENT,
                NenType.EMISSION,
                NenType.TRANSMUTATION,
                NenType.CONJURATION,
                NenType.MANIPULATION
        };
        return types[player.getRandom().nextInt(types.length)];
    }

    private static void claimStage(ServerPlayer player, HunterPlayerData data) {
        NenQuestStage stage = data.getNenQuestStage();
        if (!NenQuestUtil.isStageComplete(data)) {
            return;
        }

        switch (stage) {
            case FEEL_THE_AURA -> data.setNenQuestStage(NenQuestStage.OPEN_THE_NODES);
            case OPEN_THE_NODES -> {
                data.setNenLevel(Math.max(1, data.getNenLevel()));
                data.setCurrentNen(data.getMaxNen());
                data.setGyoUnlocked(true);
                data.setNenQuestStage(NenQuestStage.TEN_ENDURE);
                player.sendSystemMessage(Component.literal("Your aura nodes open. Nen level 1 awakened."));
            }
            case TEN_ENDURE -> {
                data.setTenUnlocked(true);
                data.setNenQuestStage(NenQuestStage.ZETSU_DISAPPEAR);
            }
            case ZETSU_DISAPPEAR -> {
                data.setZetsuUnlocked(true);
                data.setNenQuestStage(NenQuestStage.REN_OVERFLOW);
            }
            case REN_OVERFLOW -> data.setNenQuestStage(NenQuestStage.REN_AURA_BURST);
            case REN_AURA_BURST -> {
                data.setRenUnlocked(true);
                data.setNenQuestStage(NenQuestStage.HATSU_CHOOSE_TYPE);
            }
            case HATSU_CHOOSE_TYPE -> {
                data.setNenLevel(Math.max(2, data.getNenLevel()));
                data.setCurrentNen(data.getMaxNen());
                data.setNenQuestStage(NenQuestStage.EN_LOOK);
                player.sendSystemMessage(Component.literal("Nen level 2 attained."));
            }
            case EN_LOOK -> {
                data.setEnUnlocked(true);
                data.setNenQuestStage(NenQuestStage.SHU_REN_WEAPON);
            }
            case SHU_REN_WEAPON -> {
                data.setShuUnlocked(true);
                data.setNenQuestStage(NenQuestStage.KO_ONE_SHOT);
            }
            case KO_ONE_SHOT -> {
                data.setKoUnlocked(true);
                data.setNenQuestStage(NenQuestStage.KEN_BALANCE);
            }
            case KEN_BALANCE -> {
                data.setKenUnlocked(true);
                data.setNenQuestStage(NenQuestStage.RYU_SHIFT_AURA);
            }
            case RYU_SHIFT_AURA -> {
                data.setRyuUnlocked(true);
                data.setNenQuestStage(NenQuestStage.COMPLETED);
            }
            default -> {
            }
        }
    }
}
