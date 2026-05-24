package com.huntercraft.huntercraft.abilities.defensetree;

import com.huntercraft.huntercraft.abilities.SkillTreeCombatAbility;
import com.huntercraft.huntercraft.animation.AnimationType;
import com.huntercraft.huntercraft.damage.HunterDamageSources;
import com.huntercraft.huntercraft.data.HunterPlayerData;
import com.huntercraft.huntercraft.entity.AegisSlamEffectEntity;
import com.huntercraft.huntercraft.progression.SkillNode;
import com.huntercraft.huntercraft.util.HunterDataUtil;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public class SkybreakerDiveAbility extends SkillTreeCombatAbility {
    private static final int CHARGE_TICKS = 10;
    private static final int ACTIVE_TICKS = 34;
    private static final int DROP_REMAINING_TICKS = 24;
    private static final float BASE_DAMAGE = 44.0F;
    private static final int STUN_TICKS = 30;
    private static final double IMPACT_RADIUS = 4.15D;
    private static final int MAX_FALLING_BLOCKS = 34;
    private static final int TERRAIN_RESTORE_TICKS = 38;
    private static final int SKYBREAKER_COLOR = 0xDDEEFF;
    private static final double DROP_HORIZONTAL_SPEED = 0.18D;
    private static final double DROP_VERTICAL_VELOCITY = -4.25D;
    private static final String SKYBREAKER_FALLING_BLOCK_TAG = "HunterSkybreakerTemporaryBlock";
    private static final List<TerrainRestore> TERRAIN_RESTORE_QUEUE = new ArrayList<>();

    public SkybreakerDiveAbility() {
        super("skybreaker_dive", "Skybreaker Dive", "Launch skyward, then crash down in a heavy descending slash that drives enemies into the ground.", "textures/gui/abilities/skybreaker_dive.png", SkillNode.DEFENSE, 20);
    }

    @Override
    public int getMaxCooldownTicks() {
        return 120;
    }

    @Override
    public int getChargeTicks(HunterPlayerData data) {
        return data.isChargingAbility(this.id()) ? CHARGE_TICKS - data.getChargeTicksRemaining() : 0;
    }

    @Override
    public void use(ServerPlayer player, HunterPlayerData data, Vec3 direction) {
        if (data.isActiveAbility(this.id()) || data.isChargingAbility(this.id())) {
            return;
        }

        Vec3 forward = direction.lengthSqr() > 1.0E-4D
                ? new Vec3(direction.x, 0.0D, direction.z).normalize()
                : player.getLookAngle().multiply(1.0D, 0.0D, 1.0D).normalize();
        if (forward.lengthSqr() < 1.0E-4D) {
            return;
        }

        data.startChargingAbility(this.id(), CHARGE_TICKS, forward);
        data.triggerAnimation(AnimationType.SKYBREAKER_ASCENT);
        HunterDataUtil.sync(player);
    }

    private void launchDive(ServerPlayer player, HunterPlayerData data, Vec3 forward) {
        LivingEntity target = findTarget(player, forward);
        data.startActiveAbility(this.id(), ACTIVE_TICKS, forward);
        data.setActiveAbilityTargetUuid(target != null ? target.getUUID().toString() : "");
        data.triggerAnimation(AnimationType.SKYBREAKER_ASCENT);
        playDashReleaseSound(player, 0.78F);
        player.setNoGravity(false);
        player.setDeltaMovement(forward.x * 1.1D, 3.5D, forward.z * 1.1D);
        player.hasImpulse = true;
        player.fallDistance = 0.0F;
    }

    @Override
    public void tick(ServerPlayer player, HunterPlayerData data) {
        if (data.isChargingAbility(this.id())) {
            player.setDeltaMovement(Vec3.ZERO);
            player.hasImpulse = true;
            player.fallDistance = 0.0F;
            data.tickChargingAbility();
            if (data.getChargeTicksRemaining() > 0) {
                return;
            }
            Vec3 forward = data.getChargeDirection();
            if (forward.lengthSqr() < 1.0E-4D) {
                forward = player.getLookAngle().multiply(1.0D, 0.0D, 1.0D).normalize();
            }
            data.clearChargingAbility();
            launchDive(player, data, forward);
            return;
        }

        if (!data.isActiveAbility(this.id())) {
            return;
        }

        int remaining = data.getActiveAbilityTicksRemaining();
        LivingEntity target = resolveTarget(player, data.getActiveAbilityTargetUuid());
        Vec3 forward = data.getActiveAbilityDirection();
        if (forward.lengthSqr() < 1.0E-4D) {
            forward = player.getLookAngle().multiply(1.0D, 0.0D, 1.0D).normalize();
        }

        if (remaining == DROP_REMAINING_TICKS) {
            Vec3 dropPoint = target != null && target.isAlive()
                    ? target.position().subtract(forward.scale(0.45D)).add(0.0D, 13.0D, 0.0D)
                    : player.position().add(forward.scale(2.2D)).add(0.0D, 11.0D, 0.0D);
            player.teleportTo(dropPoint.x, dropPoint.y, dropPoint.z);
            player.setOnGround(false);
            if (target != null && target.isAlive()) {
                player.lookAt(EntityAnchorArgument.Anchor.EYES, target.getEyePosition());
            }
            data.triggerAnimation(AnimationType.SKYBREAKER_DROP);
            forceDiveMovement(player, forward);
            if (player.level() instanceof ServerLevel serverLevel) {
                Vec3 effectPos = player.position().add(0.0D, -0.65D, 0.0D);
                AegisSlamEffectEntity.spawn(serverLevel, effectPos, player.getYRot(), 0.72F, 12, SKYBREAKER_COLOR);
                serverLevel.sendParticles(ParticleTypes.CLOUD, effectPos.x, effectPos.y, effectPos.z, 16, 0.4D, 0.12D, 0.4D, 0.04D);
            }
        } else if (remaining > DROP_REMAINING_TICKS) {
            player.fallDistance = 0.0F;
            if (player.level() instanceof ServerLevel serverLevel && remaining % 3 == 0) {
                Vec3 trail = player.position().add(0.0D, 0.25D, 0.0D);
                serverLevel.sendParticles(ParticleTypes.CLOUD, trail.x, trail.y, trail.z, 7, 0.22D, 0.08D, 0.22D, 0.015D);
                serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK, trail.x, trail.y, trail.z, 5, 0.18D, 0.18D, 0.18D, 0.05D);
            }
        } else {
            forceDiveMovement(player, forward);
            if (player.level() instanceof ServerLevel serverLevel && remaining % 2 == 0) {
                Vec3 trail = player.position().add(0.0D, 0.4D, 0.0D);
                serverLevel.sendParticles(ParticleTypes.SWEEP_ATTACK, trail.x, trail.y, trail.z, 1, 0.18D, 0.04D, 0.18D, 0.0D);
                serverLevel.sendParticles(ParticleTypes.CRIT, trail.x, trail.y, trail.z, 7, 0.26D, 0.22D, 0.26D, 0.05D);
            }
        }

        data.tickActiveAbility();
        if (remaining < DROP_REMAINING_TICKS) {
            if (player.onGround()) {
                impact(player, target);
                data.clearActiveAbility();
                this.startCooldown(data, this.getMaxCooldownTicks());
                HunterDataUtil.sync(player);
                return;
            }
            forceDiveMovement(player, forward);
            player.fallDistance = 0.0F;
            if (data.getActiveAbilityTicksRemaining() <= 0) {
                cancelDive(player, data);
                this.startCooldown(data, this.getMaxCooldownTicks());
                HunterDataUtil.sync(player);
            }
            return;
        }
    }

    private void forceDiveMovement(ServerPlayer player, Vec3 forward) {
        player.setDeltaMovement(forward.x * DROP_HORIZONTAL_SPEED, DROP_VERTICAL_VELOCITY, forward.z * DROP_HORIZONTAL_SPEED);
        player.hasImpulse = true;
        player.hurtMarked = true;
        player.fallDistance = 0.0F;
    }

    private void cancelDive(ServerPlayer player, HunterPlayerData data) {
        data.clearActiveAbility();
        player.setNoGravity(false);
        player.setDeltaMovement(Vec3.ZERO);
        player.fallDistance = 0.0F;
    }

    private void impact(ServerPlayer player, LivingEntity target) {
        Vec3 center = player.position().add(0.0D, 0.6D, 0.0D);
        playGroundSmashReleaseSound(player, 0.78F);
        AABB hitBox = new AABB(center.x - IMPACT_RADIUS, center.y - 1.0D, center.z - IMPACT_RADIUS, center.x + IMPACT_RADIUS, center.y + 1.75D, center.z + IMPACT_RADIUS);
        for (LivingEntity entity : player.level().getEntitiesOfClass(LivingEntity.class, hitBox, living -> living != player && living.isAlive())) {
            entity.hurt(HunterDamageSources.weapon(player.level(), player), this.getWeaponScaledDamage(player, BASE_DAMAGE + (entity == target ? 2.0F : 0.0F)));
            HunterDataUtil.applyStun(entity, player, STUN_TICKS);
            Vec3 knock = entity.position().subtract(center).multiply(1.0D, 0.0D, 1.0D);
            if (knock.lengthSqr() > 1.0E-4D) {
                knock = knock.normalize().scale(0.42D);
            }
            entity.setDeltaMovement(entity.getDeltaMovement().x + knock.x, -1.15D, entity.getDeltaMovement().z + knock.z);
            entity.hurtMarked = true;
        }

        if (player.level() instanceof ServerLevel serverLevel) {
            AegisSlamEffectEntity.spawn(serverLevel, center.add(0.0D, -0.55D, 0.0D), player.getYRot(), 1.38F, 20, SKYBREAKER_COLOR);
            launchImpactBlocks(serverLevel, center);
            serverLevel.sendParticles(ParticleTypes.EXPLOSION, center.x, center.y, center.z, 4, 0.3D, 0.0D, 0.3D, 0.0D);
            serverLevel.sendParticles(ParticleTypes.SWEEP_ATTACK, center.x, center.y + 0.45D, center.z, 11, 1.65D, 0.2D, 1.65D, 0.0D);
            serverLevel.sendParticles(ParticleTypes.CLOUD, center.x, center.y - 0.15D, center.z, 52, 1.85D, 0.16D, 1.85D, 0.08D);
            serverLevel.sendParticles(ParticleTypes.CRIT, center.x, center.y + 0.4D, center.z, 38, 1.15D, 0.45D, 1.15D, 0.08D);
            serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK, center.x, center.y + 0.35D, center.z, 30, 1.2D, 0.28D, 1.2D, 0.09D);
        }

        player.setDeltaMovement(Vec3.ZERO);
        player.setNoGravity(false);
        player.fallDistance = 0.0F;
    }

    public static void launchImpactBlocks(ServerLevel serverLevel, Vec3 center) {
        BlockPos origin = BlockPos.containing(center.x, center.y - 0.55D, center.z);
        int launched = 0;
        int radius = (int) Math.ceil(IMPACT_RADIUS);
        for (int ring = 1; ring <= radius && launched < MAX_FALLING_BLOCKS; ring++) {
            for (int x = -ring; x <= ring && launched < MAX_FALLING_BLOCKS; x++) {
                for (int z = -ring; z <= ring && launched < MAX_FALLING_BLOCKS; z++) {
                    double distance = Math.sqrt(x * x + z * z);
                    if (distance < ring - 0.45D || distance > ring + 0.55D || distance > IMPACT_RADIUS) {
                        continue;
                    }
                    if ((x + z + ring) % 2 != 0 && distance < IMPACT_RADIUS - 0.65D) {
                        continue;
                    }

                    BlockPos surface = findBreakableSurface(serverLevel, origin.offset(x, 0, z));
                    if (surface == null) {
                        continue;
                    }

                    BlockState state = serverLevel.getBlockState(surface);
                    FallingBlockEntity falling = FallingBlockEntity.fall(serverLevel, surface, state);
                    falling.dropItem = false;
                    falling.getPersistentData().putBoolean(SKYBREAKER_FALLING_BLOCK_TAG, true);
                    TERRAIN_RESTORE_QUEUE.add(new TerrainRestore(serverLevel.dimension(), surface.immutable(), state, falling.getUUID(), TERRAIN_RESTORE_TICKS));
                    Vec3 outward = new Vec3(surface.getX() + 0.5D - center.x, 0.0D, surface.getZ() + 0.5D - center.z);
                    if (outward.lengthSqr() < 1.0E-4D) {
                        outward = new Vec3(serverLevel.random.nextDouble() - 0.5D, 0.0D, serverLevel.random.nextDouble() - 0.5D);
                    }
                    outward = outward.normalize();
                    double lift = 0.48D + serverLevel.random.nextDouble() * 0.24D + Math.max(0.0D, IMPACT_RADIUS - distance) * 0.04D;
                    double speed = 0.055D + serverLevel.random.nextDouble() * 0.035D;
                    falling.setDeltaMovement(outward.x * speed, lift, outward.z * speed);
                    falling.hurtMarked = true;
                    launched++;
                }
            }
        }
    }

    private static BlockPos findBreakableSurface(ServerLevel serverLevel, BlockPos base) {
        for (int y = 2; y >= -3; y--) {
            BlockPos pos = base.offset(0, y, 0);
            BlockState state = serverLevel.getBlockState(pos);
            if (state.isAir() || state.getFluidState().isSource() || state.getBlock() == Blocks.BEDROCK || serverLevel.getBlockEntity(pos) != null) {
                continue;
            }
            float hardness = state.getDestroySpeed(serverLevel, pos);
            if (hardness >= 0.0F && hardness <= 3.5F && serverLevel.isEmptyBlock(pos.above())) {
                return pos;
            }
        }
        return null;
    }

    public static void tickTerrainRestoration(ServerLevel serverLevel) {
        if (TERRAIN_RESTORE_QUEUE.isEmpty()) {
            return;
        }

        ResourceKey<Level> dimension = serverLevel.dimension();
        Iterator<TerrainRestore> iterator = TERRAIN_RESTORE_QUEUE.iterator();
        while (iterator.hasNext()) {
            TerrainRestore restore = iterator.next();
            if (!restore.dimension().equals(dimension)) {
                continue;
            }

            int ticksRemaining = restore.ticksRemaining() - 1;
            if (ticksRemaining > 0) {
                restore.setTicksRemaining(ticksRemaining);
                continue;
            }

            if (serverLevel.getEntity(restore.fallingBlockUuid()) instanceof FallingBlockEntity falling
                    && falling.getPersistentData().getBoolean(SKYBREAKER_FALLING_BLOCK_TAG)) {
                falling.discard();
            }

            if (serverLevel.getBlockEntity(restore.pos()) == null) {
                serverLevel.setBlockAndUpdate(restore.pos(), restore.state());
                serverLevel.levelEvent(2001, restore.pos(), net.minecraft.world.level.block.Block.getId(restore.state()));
            }
            iterator.remove();
        }
    }

    private static final class TerrainRestore {
        private final ResourceKey<Level> dimension;
        private final BlockPos pos;
        private final BlockState state;
        private final UUID fallingBlockUuid;
        private int ticksRemaining;

        private TerrainRestore(ResourceKey<Level> dimension, BlockPos pos, BlockState state, UUID fallingBlockUuid, int ticksRemaining) {
            this.dimension = dimension;
            this.pos = pos;
            this.state = state;
            this.fallingBlockUuid = fallingBlockUuid;
            this.ticksRemaining = ticksRemaining;
        }

        private ResourceKey<Level> dimension() {
            return this.dimension;
        }

        private BlockPos pos() {
            return this.pos;
        }

        private BlockState state() {
            return this.state;
        }

        private UUID fallingBlockUuid() {
            return this.fallingBlockUuid;
        }

        private int ticksRemaining() {
            return this.ticksRemaining;
        }

        private void setTicksRemaining(int ticksRemaining) {
            this.ticksRemaining = ticksRemaining;
        }
    }

    private LivingEntity findTarget(ServerPlayer player, Vec3 forward) {
        Vec3 eye = player.getEyePosition();
        Vec3 end = eye.add(forward.scale(7.5D));
        AABB searchBox = player.getBoundingBox().expandTowards(forward.scale(7.5D)).inflate(1.4D);
        LivingEntity closest = null;
        double closestDistance = Double.MAX_VALUE;
        for (LivingEntity target : player.level().getEntitiesOfClass(LivingEntity.class, searchBox, entity -> entity != player && entity.isAlive())) {
            Vec3 hit = target.getBoundingBox().inflate(0.35D).clip(eye, end).orElse(null);
            if (hit == null) {
                continue;
            }
            double distance = eye.distanceToSqr(hit);
            if (distance < closestDistance) {
                closestDistance = distance;
                closest = target;
            }
        }
        return closest;
    }

    private LivingEntity resolveTarget(ServerPlayer player, String uuidString) {
        if (!(player.level() instanceof ServerLevel serverLevel) || uuidString == null || uuidString.isBlank()) {
            return null;
        }
        try {
            return serverLevel.getEntity(UUID.fromString(uuidString)) instanceof LivingEntity living ? living : null;
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }
}
