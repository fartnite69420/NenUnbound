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

public class SpeedBladeTrailEntity extends Entity {
    private static final EntityDataAccessor<Float> LENGTH = SynchedEntityData.defineId(SpeedBladeTrailEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> WIDTH = SynchedEntityData.defineId(SpeedBladeTrailEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> LIFE_TICKS = SynchedEntityData.defineId(SpeedBladeTrailEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> MAX_LIFE_TICKS = SynchedEntityData.defineId(SpeedBladeTrailEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> COLOR = SynchedEntityData.defineId(SpeedBladeTrailEntity.class, EntityDataSerializers.INT);

    public SpeedBladeTrailEntity(EntityType<? extends SpeedBladeTrailEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    public static void spawn(ServerLevel level, Vec3 position, Vec3 direction, float length, float width, int lifeTicks, int color) {
        SpeedBladeTrailEntity entity = HunterEntityTypes.SPEED_BLADE_TRAIL.get().create(level);
        if (entity == null) {
            return;
        }
        Vec3 look = direction.lengthSqr() > 1.0E-5D ? direction.normalize() : Vec3.directionFromRotation(0.0F, 0.0F);
        float yaw = (float) Math.toDegrees(Math.atan2(look.x, look.z));
        float pitch = (float) -Math.toDegrees(Math.atan2(look.y, Math.sqrt((look.x * look.x) + (look.z * look.z))));
        entity.moveTo(position.x, position.y, position.z, yaw, pitch);
        entity.setLength(length);
        entity.setWidth(width);
        entity.setLifeTicks(lifeTicks);
        entity.setColor(color);
        level.addFreshEntity(entity);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(LENGTH, 5.0F);
        this.entityData.define(WIDTH, 0.8F);
        this.entityData.define(LIFE_TICKS, 12);
        this.entityData.define(MAX_LIFE_TICKS, 12);
        this.entityData.define(COLOR, 0x7BE8FF);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        this.setLength(tag.getFloat("Length"));
        this.setWidth(tag.getFloat("Width"));
        this.setLifeTicks(tag.getInt("LifeTicks"));
        this.entityData.set(MAX_LIFE_TICKS, Math.max(1, tag.getInt("MaxLifeTicks")));
        this.setColor(tag.contains("Color") ? tag.getInt("Color") : 0x7BE8FF);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putFloat("Length", this.getLength());
        tag.putFloat("Width", this.getWidth());
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

    public float getLength() {
        return this.entityData.get(LENGTH);
    }

    private void setLength(float length) {
        this.entityData.set(LENGTH, Math.max(0.1F, length));
    }

    public float getWidth() {
        return this.entityData.get(WIDTH);
    }

    private void setWidth(float width) {
        this.entityData.set(WIDTH, Math.max(0.05F, width));
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
        return distance < 1024.0D;
    }
}
