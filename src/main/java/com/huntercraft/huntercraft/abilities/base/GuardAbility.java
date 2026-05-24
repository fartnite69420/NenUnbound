package com.huntercraft.huntercraft.abilities.base;

import com.huntercraft.huntercraft.data.HunterPlayerData;
import com.huntercraft.huntercraft.network.HunterNetwork;
import com.huntercraft.huntercraft.network.packet.AfterImagePacket;
import com.huntercraft.huntercraft.sound.HunterSoundEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

public class GuardAbility extends BaseTechniqueAbility {
    public static final int MAX_ACTIVE_TICKS = 40;
    private static final int COOLDOWN_TICKS = 160;

    public GuardAbility() {
        super("guard", "Dodge", "Enter a 2 second dodge stance that avoids incoming damage.", "textures/gui/abilities/guard.png");
    }

    @Override
    public int getMaxCooldownTicks() {
        return COOLDOWN_TICKS;
    }

    @Override
    public boolean isContinuous() {
        return true;
    }

    @Override
    public int getStaminaCost(HunterPlayerData data) {
        return data.getReducedStaminaCost(45);
    }

    @Override
    public boolean isActive(HunterPlayerData data) {
        return data.isGuarding();
    }

    @Override
    public int getActiveTicks(HunterPlayerData data) {
        return data.getGuardTicks();
    }

    @Override
    public void use(ServerPlayer player, HunterPlayerData data, Vec3 direction) {
        if (data.isGuarding()) {
            this.stop(player, data);
            return;
        }
        if (this.getCurrentCooldown(data) > 0) {
            return;
        }
        data.setGuarding(true);
        spawnAfterImages(player, 14);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), HunterSoundEvents.TELEPORT.get(), SoundSource.PLAYERS, 0.7F, 1.18F);
    }

    @Override
    public void stop(ServerPlayer player, HunterPlayerData data) {
        if (data.isGuarding()) {
            int activeTicks = Math.max(1, Math.min(MAX_ACTIVE_TICKS, data.getGuardTicks()));
            int scaledCooldown = (int) Math.ceil(COOLDOWN_TICKS * (activeTicks / (double) MAX_ACTIVE_TICKS));
            data.setGuarding(false);
            this.startCooldown(data, scaledCooldown);
        }
    }

    public static void spawnAfterImages(ServerPlayer player, int lifeTicks) {
        Vec3 look = player.getLookAngle().multiply(1.0D, 0.0D, 1.0D);
        if (look.lengthSqr() < 1.0E-4D) {
            look = Vec3.directionFromRotation(0.0F, player.getYRot()).multiply(1.0D, 0.0D, 1.0D);
        }
        Vec3 back = look.normalize().reverse();
        Vec3 right = new Vec3(-back.z, 0.0D, back.x);
        Vec3 origin = player.position();
        sendAfterImage(player, origin.add(back.scale(0.34D)), lifeTicks);
        sendAfterImage(player, origin.add(back.scale(0.58D)).add(right.scale(0.16D)), Math.max(6, lifeTicks - 3));
        sendAfterImage(player, origin.add(back.scale(0.78D)).subtract(right.scale(0.12D)), Math.max(5, lifeTicks - 5));
    }

    private static void sendAfterImage(ServerPlayer player, Vec3 position, int lifeTicks) {
        HunterNetwork.sendToTrackingAndSelf(player, new AfterImagePacket(
                player.getUUID(),
                position.x,
                position.y,
                position.z,
                player.getYRot(),
                player.getXRot(),
                lifeTicks
        ));
    }
}
