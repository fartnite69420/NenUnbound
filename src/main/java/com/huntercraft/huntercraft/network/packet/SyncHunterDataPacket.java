package com.huntercraft.huntercraft.network.packet;

import com.huntercraft.huntercraft.data.HunterPlayerDataProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncHunterDataPacket {
    private final int entityId;
    private final CompoundTag tag;

    public SyncHunterDataPacket(int entityId, CompoundTag tag) {
        this.entityId = entityId;
        this.tag = tag;
    }

    public static void encode(SyncHunterDataPacket packet, FriendlyByteBuf buffer) {
        buffer.writeInt(packet.entityId);
        buffer.writeNbt(packet.tag);
    }

    public static SyncHunterDataPacket decode(FriendlyByteBuf buffer) {
        int entityId = buffer.readInt();
        CompoundTag tag = buffer.readNbt();
        return new SyncHunterDataPacket(entityId, tag == null ? new CompoundTag() : tag);
    }

    public static void handle(SyncHunterDataPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.level != null && minecraft.level.getEntity(packet.entityId) instanceof Player player) {
                player.getCapability(HunterPlayerDataProvider.CAPABILITY).ifPresent(data -> data.deserializeNBT(packet.tag));
            }
        });
        context.setPacketHandled(true);
    }
}
