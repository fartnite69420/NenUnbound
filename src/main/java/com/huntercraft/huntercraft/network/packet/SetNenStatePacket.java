package com.huntercraft.huntercraft.network.packet;

import com.huntercraft.huntercraft.abilities.HunterAbilities;
import com.huntercraft.huntercraft.data.HunterPlayerData;
import com.huntercraft.huntercraft.effect.HunterMobEffects;
import com.huntercraft.huntercraft.util.HunterDataUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SetNenStatePacket {
    private final String state;
    private final boolean active;

    public SetNenStatePacket(String state, boolean active) {
        this.state = state;
        this.active = active;
    }

    public static void encode(SetNenStatePacket packet, FriendlyByteBuf buffer) {
        buffer.writeUtf(packet.state);
        buffer.writeBoolean(packet.active);
    }

    public static SetNenStatePacket decode(FriendlyByteBuf buffer) {
        return new SetNenStatePacket(buffer.readUtf(), buffer.readBoolean());
    }

    public static void handle(SetNenStatePacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        ServerPlayer player = context.getSender();
        if (player == null) {
            context.setPacketHandled(true);
            return;
        }

        context.enqueueWork(() -> {
            HunterPlayerData data = HunterDataUtil.get(player);
            if (player.hasEffect(HunterMobEffects.ZETSU.get()) && !(packet.state.equals("zetsu") && !packet.active && data.isZetsuActive())) {
                return;
            }
            int staminaCost = getToggleStaminaCost(packet.state, packet.active, data);
            if (staminaCost > 0 && !data.consumeStamina(staminaCost)) {
                return;
            }
            switch (packet.state) {
                case "ten" -> {
                    if (packet.active) {
                        HunterAbilities.TEN.use(player, data, Vec3.ZERO);
                    } else {
                        HunterAbilities.TEN.stop(player, data);
                    }
                }
                case "zetsu" -> {
                    if (packet.active) {
                        HunterAbilities.ZETSU.use(player, data, Vec3.ZERO);
                    } else {
                        HunterAbilities.ZETSU.stop(player, data);
                    }
                }
                case "ren" -> {
                    if (packet.active) {
                        HunterAbilities.REN.use(player, data, Vec3.ZERO);
                    } else {
                        HunterAbilities.REN.stop(player, data);
                    }
                }
                case "en" -> {
                    if (packet.active) {
                        HunterAbilities.EN.use(player, data, Vec3.ZERO);
                    } else {
                        HunterAbilities.EN.stop(player, data);
                    }
                }
                case "ko" -> {
                    if (packet.active) {
                        HunterAbilities.KO.use(player, data, Vec3.ZERO);
                    } else {
                        HunterAbilities.KO.stop(player, data);
                    }
                }
                case "ken" -> {
                    if (packet.active) {
                        HunterAbilities.KEN.use(player, data, Vec3.ZERO);
                    } else {
                        HunterAbilities.KEN.stop(player, data);
                    }
                }
                case "ryu" -> data.setRyuActive(false);
                default -> {
                }
            }
            HunterDataUtil.sync(player);
        });
        context.setPacketHandled(true);
    }

    private static int getToggleStaminaCost(String state, boolean active, HunterPlayerData data) {
        if (!active) {
            return 0;
        }
        return switch (state) {
            case "ten" -> HunterAbilities.TEN.isActive(data) ? 0 : data.getReducedNenStaminaCost(8);
            case "ren" -> HunterAbilities.REN.isActive(data) ? 0 : data.getReducedNenStaminaCost(8);
            case "en" -> HunterAbilities.EN.isActive(data) ? 0 : data.getReducedNenStaminaCost(8);
            case "ko" -> HunterAbilities.KO.isActive(data) ? 0 : data.getReducedNenStaminaCost(8);
            case "ken" -> HunterAbilities.KEN.isActive(data) ? 0 : data.getReducedNenStaminaCost(8);
            default -> 0;
        };
    }
}
