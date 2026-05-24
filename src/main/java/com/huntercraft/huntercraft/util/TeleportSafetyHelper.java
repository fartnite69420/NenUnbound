package com.huntercraft.huntercraft.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public final class TeleportSafetyHelper {
    private TeleportSafetyHelper() {
    }

    public static Vec3 resolveDashTeleport(Player player, Vec3 direction, double range) {
        Vec3 start = player.getEyePosition();
        Vec3 normalized = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : player.getLookAngle().normalize();
        Vec3 end = start.add(normalized.scale(range));
        BlockHitResult hit = player.level().clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        Vec3 target = hit.getType() == HitResult.Type.MISS ? end : Vec3.atBottomCenterOf(hit.getBlockPos());
        return resolveGroundedDestination(player, target);
    }

    public static Vec3 resolveAroundTarget(Player player, Vec3 desired) {
        return resolveGroundedDestination(player, desired);
    }

    private static Vec3 resolveGroundedDestination(Player player, Vec3 target) {
        Vec3 adjusted = adjustToFloor(player.level(), player, target);
        if (isSafe(player.level(), player, adjusted)) {
            return adjusted;
        }

        double[] offsets = new double[] {0.0D, 0.5D, -0.5D, 1.0D, -1.0D};
        for (double xOffset : offsets) {
            for (double zOffset : offsets) {
                Vec3 shifted = adjustToFloor(player.level(), player, target.add(xOffset, 0.0D, zOffset));
                if (isSafe(player.level(), player, shifted)) {
                    return shifted;
                }
            }
        }
        return player.position();
    }

    private static Vec3 adjustToFloor(Level level, Player player, Vec3 target) {
        BlockPos.MutableBlockPos pos = BlockPos.containing(target).mutable();
        double playerY = player.getY();

        for (int i = 0; i < 6; i++) {
            BlockState floor = level.getBlockState(pos.below());
            if (!floor.getCollisionShape(level, pos.below()).isEmpty()) {
                double y = pos.getY();
                if (player.fallDistance > 1.0F && y > playerY) {
                    y = playerY;
                }
                return new Vec3(target.x, y, target.z);
            }
            pos.move(0, -1, 0);
        }

        return new Vec3(target.x, target.y, target.z);
    }

    private static boolean isSafe(Level level, Player player, Vec3 target) {
        return level.noCollision(player, player.getBoundingBox().move(target.subtract(player.position())));
    }
}
