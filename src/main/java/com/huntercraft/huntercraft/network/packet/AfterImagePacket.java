package com.huntercraft.huntercraft.network.packet;

import com.huntercraft.huntercraft.client.AfterImageStore;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class AfterImagePacket {
    private final UUID playerUuid;
    private final double x;
    private final double y;
    private final double z;
    private final float yaw;
    private final float pitch;
    private final int ticks;

    public AfterImagePacket(UUID playerUuid, double x, double y, double z, float yaw, float pitch, int ticks) {
        this.playerUuid = playerUuid;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.ticks = ticks;
    }

    public static void encode(AfterImagePacket packet, FriendlyByteBuf buffer) {
        buffer.writeUUID(packet.playerUuid);
        buffer.writeDouble(packet.x);
        buffer.writeDouble(packet.y);
        buffer.writeDouble(packet.z);
        buffer.writeFloat(packet.yaw);
        buffer.writeFloat(packet.pitch);
        buffer.writeInt(packet.ticks);
    }

    public static AfterImagePacket decode(FriendlyByteBuf buffer) {
        return new AfterImagePacket(
                buffer.readUUID(),
                buffer.readDouble(),
                buffer.readDouble(),
                buffer.readDouble(),
                buffer.readFloat(),
                buffer.readFloat(),
                buffer.readInt()
        );
    }

    public static void handle(AfterImagePacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                AfterImageStore.INSTANCE.addSnapshot(new AfterImageStore.Snapshot(
                        packet.playerUuid,
                        packet.x,
                        packet.y,
                        packet.z,
                        packet.yaw,
                        packet.pitch,
                        packet.ticks
                ))
        ));
        context.setPacketHandled(true);
    }
}
