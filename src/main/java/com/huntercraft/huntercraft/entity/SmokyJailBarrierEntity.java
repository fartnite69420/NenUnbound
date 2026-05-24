package com.huntercraft.huntercraft.entity;

import com.huntercraft.huntercraft.data.HunterPlayerData;
import com.huntercraft.huntercraft.entity.SmokeCloneEntity;
import com.huntercraft.huntercraft.entity.SmokeSoldierEntity;
import com.huntercraft.huntercraft.faction.FactionUtil;
import com.huntercraft.huntercraft.particle.HunterParticles;
import com.huntercraft.huntercraft.util.HunterDataUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SmokyJailBarrierEntity extends Entity {
    private static final EntityDataAccessor<Float> RADIUS = SynchedEntityData.defineId(SmokyJailBarrierEntity.class, EntityDataSerializers.FLOAT);
    private static final double WALL_THICKNESS = 0.75D;
    private static final double PUSH_EPS = 0.16D;
    private static final double SOFT_PUSH_BASE = 0.03D;
    private static final double SOFT_PUSH_SCALE = 0.2D;
    private static final double SOFT_PUSH_MAX = 0.1D;
    private static final double SEPARATION_IMPULSE = 5.18D;
    private static final double PARTICLE_DENSITY_FACTOR = 0.12;
    private static final int MAX_PARTICLES_PER_TICK = 170;
    private static final int PARTICLE_SPAWN_INTERVAL = 14;
    private static final double PARTICLE_SPAWN_MULTIPLIER = 1.0;

    private final Map<UUID, Double> lastMeasure = new HashMap<>();
    private final Map<UUID, Vec3> lastSafePos = new HashMap<>();
    private double lastRadiusTick = -1.0D;
    private int durationTicks;
    @Nullable
    private UUID ownerUuid;

    public SmokyJailBarrierEntity(EntityType<? extends SmokyJailBarrierEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
        this.noCulling = true;
    }

    public void setOwner(LivingEntity owner) {
        this.ownerUuid = owner.getUUID();
    }

    public boolean isOwnedBy(ServerPlayer player) {
        return this.ownerUuid != null && this.ownerUuid.equals(player.getUUID());
    }

    public void setRadius(float radius) {
        this.entityData.set(RADIUS, radius);
    }

    public float getRadius() {
        return this.entityData.get(RADIUS);
    }

    public void setDurationTicks(int durationTicks) {
        this.durationTicks = Math.max(0, durationTicks);
    }

    @Nullable
    public ServerPlayer getOwnerPlayer() {
        if (this.ownerUuid == null || !(this.level() instanceof ServerLevel serverLevel)) {
            return null;
        }
        return serverLevel.getServer().getPlayerList().getPlayer(this.ownerUuid);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(RADIUS, 0.0F);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            if ((this.tickCount % PARTICLE_SPAWN_INTERVAL) == 0) {
                spawnClientBarrierParticles();
            }
            return;
        }

        ServerPlayer owner = this.getOwnerPlayer();
        if (owner == null || !owner.isAlive()) {
            this.discard();
            return;
        }

        if (this.durationTicks > 0) {
            this.durationTicks--;
        }
        if (this.durationTicks <= 0) {
            // Route through SmokeyJailAbility.stop() so cooldown is applied correctly
            HunterPlayerData ownerData = HunterDataUtil.getOptional(owner).orElse(null);
            if (ownerData != null) {
                com.huntercraft.huntercraft.abilities.HunterAbilities.SKILL_TREE_COMBAT_ABILITIES.stream()
                        .filter(a -> a.id().equals("smokey_jail"))
                        .findFirst()
                        .ifPresent(a -> a.stop(owner, ownerData));
                HunterDataUtil.sync(owner);
            }
            this.discard();
            return;
        }

        tickProjectileBarrier();
        tickBarrierCollision();
    }

    private void tickProjectileBarrier() {
        float radius = this.getRadius();
        if (radius <= 1.0F) {
            return;
        }
        Vec3 center = this.position();
        AABB box = new AABB(
                center.x - (radius + 4.0D), center.y - (radius + 4.0D), center.z - (radius + 4.0D),
                center.x + (radius + 4.0D), center.y + (radius + 4.0D), center.z + (radius + 4.0D)
        );
        List<Projectile> projectiles = this.level().getEntitiesOfClass(Projectile.class, box, projectile -> projectile.isAlive() && projectile.getOwner() != this.getOwnerPlayer());
        for (Projectile projectile : projectiles) {
            double distance = projectile.position().distanceTo(center);
            if (Math.abs(distance - radius) <= WALL_THICKNESS + Math.max(projectile.getBbWidth(), projectile.getBbHeight())) {
                projectile.discard();
            }
        }
    }

    private void tickBarrierCollision() {
        float radiusFloat = this.getRadius();
        if (radiusFloat <= 1.0F) {
            this.lastMeasure.clear();
            this.lastSafePos.clear();
            this.lastRadiusTick = radiusFloat;
            return;
        }

        double radius = radiusFloat;
        double previousRadius = this.lastRadiusTick > 0.0D ? this.lastRadiusTick : radius;
        Vec3 center = this.position();
        AABB box = new AABB(
                center.x - (radius + 3.0D), center.y - (radius + 3.0D), center.z - (radius + 3.0D),
                center.x + (radius + 3.0D), center.y + (radius + 3.0D), center.z + (radius + 3.0D)
        );
        List<LivingEntity> entities = this.level().getEntitiesOfClass(LivingEntity.class, box, entity -> entity.isAlive());

        for (LivingEntity entity : entities) {
            if (isExemptFromBarrier(entity)) continue;
            double pad = entity.getBbWidth() * 0.5D + 0.02D;
            Vec3 position = entity.position();
            double dx = position.x - center.x;
            double dy = position.y - center.y;
            double dz = position.z - center.z;
            double measure = Math.sqrt((dx * dx) + (dy * dy) + (dz * dz));
            if (measure < 1.0E-6D) {
                measure = 1.0E-6D;
            }

            double previous = this.lastMeasure.getOrDefault(entity.getUUID(), measure);
            boolean wasInside = previous <= previousRadius - pad;
            double insideLimit = radius - PUSH_EPS - pad;
            double outsideLimit = radius + PUSH_EPS + pad;
            Vec3 outwardNormal = wallNormal(dx, dy, dz);

            if (measure < radius - WALL_THICKNESS - pad - 0.4D) {
                this.lastSafePos.put(entity.getUUID(), entity.position());
            }

            if (wasInside && measure > radius + pad + 0.5D) {
                Vec3 safePos = this.lastSafePos.get(entity.getUUID());
                if (safePos != null) {
                    if (entity instanceof ServerPlayer serverPlayer) {
                        serverPlayer.connection.teleport(safePos.x, safePos.y, safePos.z, serverPlayer.getYRot(), serverPlayer.getXRot());
                    } else {
                        entity.teleportTo(safePos.x, safePos.y, safePos.z);
                    }
                    entity.setDeltaMovement(Vec3.ZERO);
                    measure = measureAt(entity.position());
                }
            }

            if (wasInside) {
                if (measure > insideLimit) {
                    hardCorrect(entity, true, insideLimit, outwardNormal);
                    measure = insideLimit;
                } else if (Math.abs(measure - radius) <= WALL_THICKNESS + pad) {
                    applySoftPush(entity, outwardNormal, true, radius, measure);
                }
            } else {
                if (measure < outsideLimit) {
                    hardCorrect(entity, false, outsideLimit, outwardNormal);
                    measure = outsideLimit;
                } else if (Math.abs(measure - radius) <= WALL_THICKNESS + pad) {
                    applySoftPush(entity, outwardNormal, false, radius, measure);
                }
            }

            this.lastMeasure.put(entity.getUUID(), measure);
        }

        this.lastRadiusTick = radius;
    }

    private void hardCorrect(Entity entity, boolean inside, double boundary, Vec3 outwardNormal) {
        double currentMeasure = measureAt(entity.position());
        double penetration = inside
                ? Math.min(0.8D, currentMeasure - boundary + 0.002D)
                : Math.min(0.8D, boundary - currentMeasure + 0.002D);
        if (penetration < 0.0D) {
            penetration = 0.0D;
        }

        Vec3 delta = outwardNormal.scale(inside ? -penetration : penetration);
        entity.move(MoverType.SELF, delta);
        entity.hasImpulse = true;
        cancelIntoWallVelocity(entity, outwardNormal, inside);
        addSeparationImpulse(entity, outwardNormal, inside);
    }

    private double measureAt(Vec3 position) {
        Vec3 center = this.position();
        double dx = position.x - center.x;
        double dy = position.y - center.y;
        double dz = position.z - center.z;
        double measure = Math.sqrt((dx * dx) + (dy * dy) + (dz * dz));
        return measure < 1.0E-6D ? 1.0E-6D : measure;
    }

    private Vec3 wallNormal(double dx, double dy, double dz) {
        Vec3 vector = new Vec3(dx, dy, dz);
        return vector.lengthSqr() < 1.0E-8D ? new Vec3(1.0D, 0.0D, 0.0D) : vector.normalize();
    }

    private void cancelIntoWallVelocity(Entity entity, Vec3 outwardNormal, boolean inside) {
        Vec3 velocity = entity.getDeltaMovement();
        double radial = (velocity.x * outwardNormal.x) + (velocity.y * outwardNormal.y) + (velocity.z * outwardNormal.z);
        if (inside && radial > 0.0D) {
            velocity = velocity.subtract(outwardNormal.scale(radial));
        } else if (!inside && radial < 0.0D) {
            velocity = velocity.subtract(outwardNormal.scale(radial));
        }
        entity.setDeltaMovement(velocity);
        entity.hasImpulse = true;
    }

    private void addSeparationImpulse(Entity entity, Vec3 outwardNormal, boolean inside) {
        Vec3 velocity = entity.getDeltaMovement();
        Vec3 impulse = outwardNormal.scale(inside ? -SEPARATION_IMPULSE : SEPARATION_IMPULSE);
        entity.setDeltaMovement(velocity.add(impulse));
        entity.hasImpulse = true;
    }

    private void applySoftPush(Entity entity, Vec3 normal, boolean inside, double radius, double measure) {
        Vec3 velocity = entity.getDeltaMovement();
        double radial = (velocity.x * normal.x) + (velocity.y * normal.y) + (velocity.z * normal.z);
        double penetration = inside
                ? Math.max(0.0D, measure - (radius - PUSH_EPS))
                : Math.max(0.0D, (radius + PUSH_EPS) - measure);
        boolean pressing = inside ? radial > 0.0D : radial < 0.0D;
        if (pressing) {
            velocity = velocity.subtract(normal.scale(radial));
        }
        double push = Math.min(SOFT_PUSH_MAX, SOFT_PUSH_BASE + (penetration * SOFT_PUSH_SCALE));
        velocity = velocity.add(normal.scale(inside ? -push : push));
        entity.setDeltaMovement(velocity);
        entity.hasImpulse = true;
    }

    private boolean isExemptFromBarrier(LivingEntity entity) {
        if (this.ownerUuid == null) return false;
        if (entity.getUUID().equals(this.ownerUuid)) return true;
        ServerPlayer owner = this.getOwnerPlayer();
        if (owner != null && entity instanceof ServerPlayer sp && FactionUtil.areFactionMates(owner, sp)) return true;
        if (entity instanceof SmokeCloneEntity clone && this.ownerUuid.equals(clone.getOwnerUuid())) return true;
        if (entity instanceof SmokeSoldierEntity soldier && this.ownerUuid.equals(soldier.getOwnerUuid())) return true;
        return false;
    }

    private void spawnClientBarrierParticles() {
        float radius = this.getRadius();
        if (radius <= 1.0F) {
            return;
        }
        Vec3 center = this.position();
        double time = this.tickCount * 0.08D;

        int bands = Math.max(10, (int)(Math.PI * radius * 0.55D));
        // Multiply by spawn interval so total coverage per second stays the same,
        // but particles are spawned in larger bursts less frequently — this lets
        // each particle fade in and out naturally without constant churn.
        int totalParticles = Math.min((int)(MAX_PARTICLES_PER_TICK * PARTICLE_SPAWN_MULTIPLIER),
                (int)(4.0 * Math.PI * radius * radius * PARTICLE_DENSITY_FACTOR * PARTICLE_SPAWN_MULTIPLIER));

        // Pre-compute sum of sinPitch for proportional distribution
        double sumOfSinPitches = 0.0;
        for (int band = 0; band < bands; band++) {
            double bandT = bands == 1 ? 0.5D : (double) band / (bands - 1);
            double pitch = Mth.lerp(bandT, 0.03D, Math.PI - 0.03D);
            sumOfSinPitches += Math.sin(pitch);
        }

        for (int band = 0; band < bands; band++) {
            double bandT = bands == 1 ? 0.5D : (double) band / (bands - 1);
            double pitch = Mth.lerp(bandT, 0.03D, Math.PI - 0.03D);
            double sinPitch = Math.sin(pitch);
            double cosPitch = Math.cos(pitch);
            double bandRadius = radius * sinPitch;
            double bandY = center.y + (cosPitch * radius);
            int bandParticles = Math.max(1, (int)(totalParticles * sinPitch / sumOfSinPitches));
            for (int i = 0; i < bandParticles; i++) {
                double angle = ((i / (double) bandParticles) * Mth.TWO_PI) + time + (band * 0.6D);
                double jitter = (this.random.nextDouble() - 0.5D) * 0.34D;
                double x = center.x + (Math.cos(angle + jitter) * bandRadius);
                double z = center.z + (Math.sin(angle + jitter) * bandRadius);
                double y = bandY + ((this.random.nextDouble() - 0.5D) * 0.72D);
                double tangentX = -Math.sin(angle) * 0.006D;
                double tangentZ = Math.cos(angle) * 0.006D;
                double vertical = (this.random.nextDouble() * 0.004D) + 0.002D;
                this.level().addParticle(HunterParticles.SMOKY_JAIL_SMOKE.get(), x, y, z, tangentX, vertical, tangentZ);
            }
        }

        spawnTopCapParticles(center, radius, time);

        int randomShellBursts = Math.max(12, (int)(radius * radius * 0.018D * PARTICLE_SPAWN_MULTIPLIER));
        for (int i = 0; i < randomShellBursts; i++) {
            double yaw = this.random.nextDouble() * Mth.TWO_PI;
            double pitch = Math.acos((this.random.nextDouble() * 2.0D) - 1.0D);
            double sinPitch = Math.sin(pitch);
            double normalX = Math.cos(yaw) * sinPitch;
            double normalY = Math.cos(pitch);
            double normalZ = Math.sin(yaw) * sinPitch;
            double x = center.x + (normalX * radius);
            double y = center.y + (normalY * radius);
            double z = center.z + (normalZ * radius);
            this.level().addParticle(HunterParticles.SMOKY_JAIL_SMOKE.get(), x, y, z,
                normalX * 0.004D, (normalY * 0.004D) + 0.002D, normalZ * 0.004D);
        }
    }

    private void spawnTopCapParticles(Vec3 center, double radius, double time) {
        int capRings = 4;
        double topY = center.y + radius;
        for (int ring = 0; ring < capRings; ring++) {
            double ringT = (ring + 0.5D) / capRings;
            double capRadius = radius * 0.42D * ringT;
            double y = topY - (ringT * ringT * radius * 0.11D);
            int particles = Math.max(5, (int) (capRadius * 0.9D));
            if (ring == 0) {
                particles = 6;
            }
            for (int i = 0; i < particles; i++) {
                double angle = ((i / (double) particles) * Mth.TWO_PI) - (time * 0.45D) + (ring * 0.42D);
                double jitter = (this.random.nextDouble() - 0.5D) * 0.45D;
                double x = center.x + Math.cos(angle + jitter) * capRadius;
                double z = center.z + Math.sin(angle + jitter) * capRadius;
                this.level().addParticle(HunterParticles.SMOKY_JAIL_SMOKE.get(), x, y + ((this.random.nextDouble() - 0.5D) * 0.45D), z,
                        0.0D, 0.001D + this.random.nextDouble() * 0.002D, 0.0D);
            }
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        this.setRadius(tag.getFloat("Radius"));
        this.durationTicks = tag.getInt("DurationTicks");
        this.ownerUuid = tag.hasUUID("OwnerUuid") ? tag.getUUID("OwnerUuid") : null;
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putFloat("Radius", this.getRadius());
        tag.putInt("DurationTicks", this.durationTicks);
        if (this.ownerUuid != null) {
            tag.putUUID("OwnerUuid", this.ownerUuid);
        }
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        double radius = this.getRadius() + 16.0F;
        return distance < (radius * radius * 16.0D);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
