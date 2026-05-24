package com.huntercraft.huntercraft.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

public class WhirlwindSlashEntity extends Entity {
    private static final EntityDataAccessor<Float> RADIUS = SynchedEntityData.defineId(WhirlwindSlashEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> HEIGHT = SynchedEntityData.defineId(WhirlwindSlashEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> START_ANGLE = SynchedEntityData.defineId(WhirlwindSlashEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> ARC_LENGTH = SynchedEntityData.defineId(WhirlwindSlashEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> SPIN_SPEED = SynchedEntityData.defineId(WhirlwindSlashEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> LIFE_TICKS = SynchedEntityData.defineId(WhirlwindSlashEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> MAX_LIFE_TICKS = SynchedEntityData.defineId(WhirlwindSlashEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> COLOR = SynchedEntityData.defineId(WhirlwindSlashEntity.class, EntityDataSerializers.INT);

    public WhirlwindSlashEntity(EntityType<? extends WhirlwindSlashEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    public static void spawn(ServerLevel level, Vec3 center, float radius, float height, float startAngle, float arcLength, float spinSpeed, int lifeTicks, int color) {
        WhirlwindSlashEntity entity = HunterEntityTypes.WHIRLWIND_SLASH.get().create(level);
        if (entity == null) {
            return;
        }
        entity.moveTo(center.x, center.y, center.z, 0.0F, 0.0F);
        entity.setRadius(radius);
        entity.setHeight(height);
        entity.setStartAngle(startAngle);
        entity.setArcLength(arcLength);
        entity.setSpinSpeed(spinSpeed);
        entity.setLifeTicks(lifeTicks);
        entity.setColor(color);
        level.addFreshEntity(entity);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(RADIUS, 2.0F);
        this.entityData.define(HEIGHT, 1.0F);
        this.entityData.define(START_ANGLE, 0.0F);
        this.entityData.define(ARC_LENGTH, 1.2F);
        this.entityData.define(SPIN_SPEED, 0.0F);
        this.entityData.define(LIFE_TICKS, 12);
        this.entityData.define(MAX_LIFE_TICKS, 12);
        this.entityData.define(COLOR, 0xFF2424);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        this.setRadius(tag.getFloat("Radius"));
        this.setHeight(tag.getFloat("Height"));
        this.setStartAngle(tag.getFloat("StartAngle"));
        this.setArcLength(tag.getFloat("ArcLength"));
        this.setSpinSpeed(tag.getFloat("SpinSpeed"));
        this.entityData.set(LIFE_TICKS, tag.getInt("LifeTicks"));
        this.entityData.set(MAX_LIFE_TICKS, Math.max(1, tag.getInt("MaxLifeTicks")));
        this.setColor(tag.contains("Color") ? tag.getInt("Color") : 0xFF2424);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putFloat("Radius", this.getRadius());
        tag.putFloat("Height", this.getHeight());
        tag.putFloat("StartAngle", this.getStartAngle());
        tag.putFloat("ArcLength", this.getArcLength());
        tag.putFloat("SpinSpeed", this.getSpinSpeed());
        tag.putInt("LifeTicks", this.getLifeTicks());
        tag.putInt("MaxLifeTicks", this.getMaxLifeTicks());
        tag.putInt("Color", this.getColor());
    }

    @Override
    public void tick() {
        super.tick();
        int remaining = this.getLifeTicks() - 1;
        this.entityData.set(LIFE_TICKS, remaining);
        if (remaining <= 0) {
            this.discard();
        }
    }

    public float getRadius() {
        return this.entityData.get(RADIUS);
    }

    private void setRadius(float radius) {
        this.entityData.set(RADIUS, radius);
    }

    public float getHeight() {
        return this.entityData.get(HEIGHT);
    }

    private void setHeight(float height) {
        this.entityData.set(HEIGHT, height);
    }

    public float getStartAngle() {
        return this.entityData.get(START_ANGLE);
    }

    private void setStartAngle(float startAngle) {
        this.entityData.set(START_ANGLE, startAngle);
    }

    public float getArcLength() {
        return this.entityData.get(ARC_LENGTH);
    }

    private void setArcLength(float arcLength) {
        this.entityData.set(ARC_LENGTH, arcLength);
    }

    public float getSpinSpeed() {
        return this.entityData.get(SPIN_SPEED);
    }

    private void setSpinSpeed(float spinSpeed) {
        this.entityData.set(SPIN_SPEED, spinSpeed);
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
        return distance < 768.0D;
    }
}
