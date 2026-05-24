package com.huntercraft.huntercraft.entity;

import com.huntercraft.huntercraft.damage.HunterDamageSources;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class DefensePulseEntity extends Entity {
    public static final int MODE_ICHIMONJI = 0;
    public static final int MODE_PROJECTILE = 1;
    private static final EntityDataAccessor<Float> SCALE = SynchedEntityData.defineId(DefensePulseEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> LIFE_TICKS = SynchedEntityData.defineId(DefensePulseEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> MAX_LIFE_TICKS = SynchedEntityData.defineId(DefensePulseEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> COLOR = SynchedEntityData.defineId(DefensePulseEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> MODE = SynchedEntityData.defineId(DefensePulseEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> DIR_X = SynchedEntityData.defineId(DefensePulseEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DIR_Y = SynchedEntityData.defineId(DefensePulseEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DIR_Z = SynchedEntityData.defineId(DefensePulseEntity.class, EntityDataSerializers.FLOAT);
    private UUID ownerUuid;
    private float damage;
    private double speed;
    private final Set<UUID> hitEntities = new HashSet<>();

    public DefensePulseEntity(EntityType<? extends DefensePulseEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    public static void spawnImpact(ServerLevel level, Vec3 position, float yaw, float scale, int lifeTicks, int color) {
        DefensePulseEntity entity = HunterEntityTypes.DEFENSE_PULSE.get().create(level);
        if (entity == null) {
            return;
        }
        entity.moveTo(position.x, position.y, position.z, yaw, 0.0F);
        entity.setScale(scale);
        entity.setLifeTicks(lifeTicks);
        entity.setColor(color);
        entity.setMode(MODE_ICHIMONJI);
        entity.setDirection(Vec3.directionFromRotation(0.0F, yaw));
        level.addFreshEntity(entity);
    }

    public static void spawnProjectile(ServerLevel level, ServerPlayer owner, Vec3 position, Vec3 direction, float damage, double speed, int lifeTicks, int color) {
        DefensePulseEntity entity = HunterEntityTypes.DEFENSE_PULSE.get().create(level);
        if (entity == null) {
            return;
        }
        Vec3 look = direction.lengthSqr() > 1.0E-5D ? direction.normalize() : Vec3.directionFromRotation(0.0F, owner.getYRot());
        float yaw = (float) Math.toDegrees(Math.atan2(look.x, look.z));
        float pitch = (float) -Math.toDegrees(Math.atan2(look.y, Math.sqrt((look.x * look.x) + (look.z * look.z))));
        entity.moveTo(position.x, position.y, position.z, yaw, pitch);
        entity.ownerUuid = owner.getUUID();
        entity.damage = damage;
        entity.speed = speed;
        entity.setDirection(look);
        entity.setScale(1.0F);
        entity.setLifeTicks(lifeTicks);
        entity.setColor(color);
        entity.setMode(MODE_PROJECTILE);
        level.addFreshEntity(entity);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(SCALE, 1.0F);
        this.entityData.define(LIFE_TICKS, 12);
        this.entityData.define(MAX_LIFE_TICKS, 12);
        this.entityData.define(COLOR, 0xEDE7FF);
        this.entityData.define(MODE, MODE_ICHIMONJI);
        this.entityData.define(DIR_X, 0.0F);
        this.entityData.define(DIR_Y, 0.0F);
        this.entityData.define(DIR_Z, 1.0F);
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide && this.getMode() == MODE_PROJECTILE) {
            tickProjectile();
        }
        int remaining = this.getLifeTicks() - 1;
        this.entityData.set(LIFE_TICKS, remaining);
        if (remaining <= 0) {
            this.discard();
        }
    }

    private void tickProjectile() {
        Vec3 forward = getPulseForward();
        Vec3 start = this.position();
        Vec3 motion = forward.scale(this.speed);
        this.setPos(start.add(motion));
        AABB hitBox = this.getBoundingBox().inflate(1.35D, 0.8D, 1.35D).expandTowards(motion);
        ServerPlayer owner = getOwner();
        for (LivingEntity target : this.level().getEntitiesOfClass(LivingEntity.class, hitBox, entity -> entity.isAlive() && entity != owner && !this.hitEntities.contains(entity.getUUID()))) {
            this.hitEntities.add(target.getUUID());
            target.hurt(HunterDamageSources.weapon(this.level(), this, owner != null ? owner : this), this.damage);
            target.setDeltaMovement(forward.x * 2.25D, 0.36D, forward.z * 2.25D);
            target.hurtMarked = true;
            if (this.level() instanceof ServerLevel serverLevel) {
                DefensePulseEntity.spawnImpact(serverLevel, target.position().add(0.0D, 0.15D, 0.0D), this.getYRot(), 0.72F, 10, this.getColor());
            }
            this.discard();
            return;
        }
    }

    private ServerPlayer getOwner() {
        if (this.ownerUuid == null || !(this.level() instanceof ServerLevel serverLevel)) {
            return null;
        }
        return serverLevel.getPlayerByUUID(this.ownerUuid) instanceof ServerPlayer player ? player : null;
    }

    private Vec3 getPulseForward() {
        Vec3 forward = this.getPulseDirection();
        return forward.lengthSqr() > 1.0E-5D ? forward.normalize() : new Vec3(0.0D, 0.0D, 1.0D);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        this.setScale(tag.getFloat("Scale"));
        this.setLifeTicks(tag.getInt("LifeTicks"));
        this.entityData.set(MAX_LIFE_TICKS, Math.max(1, tag.getInt("MaxLifeTicks")));
        this.setColor(tag.contains("Color") ? tag.getInt("Color") : 0xEDE7FF);
        this.setMode(tag.getInt("Mode"));
        this.setDirection(new Vec3(tag.getFloat("DirX"), tag.getFloat("DirY"), tag.getFloat("DirZ")));
        this.damage = tag.getFloat("Damage");
        this.speed = tag.getDouble("Speed");
        if (tag.hasUUID("Owner")) {
            this.ownerUuid = tag.getUUID("Owner");
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putFloat("Scale", this.getScale());
        tag.putInt("LifeTicks", this.getLifeTicks());
        tag.putInt("MaxLifeTicks", this.getMaxLifeTicks());
        tag.putInt("Color", this.getColor());
        tag.putInt("Mode", this.getMode());
        tag.putFloat("DirX", this.entityData.get(DIR_X));
        tag.putFloat("DirY", this.entityData.get(DIR_Y));
        tag.putFloat("DirZ", this.entityData.get(DIR_Z));
        tag.putFloat("Damage", this.damage);
        tag.putDouble("Speed", this.speed);
        if (this.ownerUuid != null) {
            tag.putUUID("Owner", this.ownerUuid);
        }
    }

    public float getScale() {
        return this.entityData.get(SCALE);
    }

    private void setScale(float scale) {
        this.entityData.set(SCALE, Math.max(0.05F, scale));
    }

    public int getLifeTicks() {
        return this.entityData.get(LIFE_TICKS);
    }

    private void setLifeTicks(int lifeTicks) {
        int clampedLife = Math.max(1, lifeTicks);
        this.entityData.set(LIFE_TICKS, clampedLife);
        this.entityData.set(MAX_LIFE_TICKS, clampedLife);
    }

    public int getMaxLifeTicks() {
        return this.entityData.get(MAX_LIFE_TICKS);
    }

    public int getColor() {
        return this.entityData.get(COLOR);
    }

    private void setColor(int color) {
        this.entityData.set(COLOR, color & 0xFFFFFF);
    }

    public int getMode() {
        return this.entityData.get(MODE);
    }

    private void setMode(int mode) {
        this.entityData.set(MODE, mode);
    }

    public Vec3 getPulseDirection() {
        return new Vec3(this.entityData.get(DIR_X), this.entityData.get(DIR_Y), this.entityData.get(DIR_Z));
    }

    private void setDirection(Vec3 direction) {
        Vec3 normalized = direction.lengthSqr() > 1.0E-5D ? direction.normalize() : new Vec3(0.0D, 0.0D, 1.0D);
        this.entityData.set(DIR_X, (float) normalized.x);
        this.entityData.set(DIR_Y, (float) normalized.y);
        this.entityData.set(DIR_Z, (float) normalized.z);
    }

    public float getFade(float partialTick) {
        float remaining = Math.max(0.0F, this.getLifeTicks() - partialTick);
        return Math.min(1.0F, remaining / Math.max(1.0F, this.getMaxLifeTicks()));
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < 1024.0D;
    }
}
