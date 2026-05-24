package com.huntercraft.huntercraft.network.packet;

import com.huntercraft.huntercraft.abilities.HunterAbilities;
import com.huntercraft.huntercraft.data.HunterPlayerData;
import com.huntercraft.huntercraft.util.HunterDataUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class UpdateCombatSlotPacket {
    private final int barIndex;
    private final int slotIndex;
    private final String abilityId;

    public UpdateCombatSlotPacket(int barIndex, int slotIndex, String abilityId) {
        this.barIndex = barIndex;
        this.slotIndex = slotIndex;
        this.abilityId = abilityId;
    }

    public static void encode(UpdateCombatSlotPacket packet, FriendlyByteBuf buffer) {
        buffer.writeInt(packet.barIndex);
        buffer.writeInt(packet.slotIndex);
        buffer.writeUtf(packet.abilityId);
    }

    public static UpdateCombatSlotPacket decode(FriendlyByteBuf buffer) {
        return new UpdateCombatSlotPacket(buffer.readInt(), buffer.readInt(), buffer.readUtf());
    }

    public static void handle(UpdateCombatSlotPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        ServerPlayer player = context.getSender();
        if (player == null) {
            context.setPacketHandled(true);
            return;
        }

        context.enqueueWork(() -> {
            HunterPlayerData data = HunterDataUtil.get(player);
            String abilityId = packet.abilityId.isBlank()
                    || HunterAbilities.canEquipToCombatBar(packet.abilityId) && HunterAbilities.isCombatAbilityUnlocked(data, packet.abilityId)
                    ? packet.abilityId
                    : "";
            data.setCombatSlot(packet.barIndex, packet.slotIndex, abilityId);
            HunterDataUtil.sync(player);
        });
        context.setPacketHandled(true);
    }
}
