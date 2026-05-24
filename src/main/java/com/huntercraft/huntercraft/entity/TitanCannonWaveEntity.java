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

public class TitanCannonWaveEntity extends Entity {
    private static final EntityDataAccessor<Float> SCALE = SynchedEntityData.defineId(TitanCannonWaveEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> LIFE_TICKS = SynchedEntityData.defineId(TitanCannonWaveEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> MAX_LIFE_TICKS = SynchedEntityData.defineId(TitanCannonWaveEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> DIR_X = SynchedEntityData.defineId(TitanCannonWaveEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DIR_Y = SynchedEntityData.defineId(TitanCannonWaveEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DIR_Z = SynchedEntityData.defineId(TitanCannonWaveEntity.class, EntityDataSerializers.FLOAT);

    public TitanCannonWaveEntity(EntityType<? extends TitanCannonWaveEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
        this.noCulling = true;
    }

    public static void spawn(ServerLevel level, Vec3 position, Vec3 direction, float scale, int lifeTicks) {
        TitanCannonWaveEntity entity = HunterEntityTypes.TITAN_CANNON_WAVE.get().create(level);
        if (entity == null) {
            return;
        }
        Vec3 look = direction.lengthSqr() > 1.0E-5D ? direction.normalize() : new Vec3(0.0D, 0.0D, 1.0D);
        float yaw = (float) Math.toDegrees(Math.atan2(look.x, look.z));
        float pitch = (float) -Math.toDegrees(Math.atan2(look.y, Math.sqrt((look.x * look.x) + (look.z * look.z))));
        entity.moveTo(position.x, position.y, position.z, yaw, pitch);
        entity.setDirection(look);
        entity.setScale(scale);
        entity.setLifeTicks(lifeTicks);
        level.addFreshEntity(entity);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(SCALE, 1.0F);
        this.entityData.define(LIFE_TICKS, 8);
        this.entityData.define(MAX_LIFE_TICKS, 8);
        this.entityData.define(DIR_X, 0.0F);
        this.entityData.define(DIR_Y, 0.0F);
        this.entityData.define(DIR_Z, 1.0F);
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

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        this.setScale(tag.getFloat("Scale"));
        this.setLifeTicks(tag.getInt("LifeTicks"));
        this.entityData.set(MAX_LIFE_TICKS, Math.max(1, tag.getInt("MaxLifeTicks")));
        this.setDirection(new Vec3(tag.getFloat("DirX"), tag.getFloat("DirY"), tag.getFloat("DirZ")));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putFloat("Scale", this.getScale());
        tag.putInt("LifeTicks", this.getLifeTicks());
        tag.putInt("MaxLifeTicks", this.getMaxLifeTicks());
        tag.putFloat("DirX", this.entityData.get(DIR_X));
        tag.putFloat("DirY", this.entityData.get(DIR_Y));
        tag.putFloat("DirZ", this.entityData.get(DIR_Z));
    }

    public float getScale() {
        return this.entityData.get(SCALE);
    }

    private void setScale(float scale) {
        this.entityData.set(SCALE, Math.max(0.1F, scale));
    }

    public int getLifeTicks() {
        return this.entityData.get(LIFE_TICKS);
    }

    public int getMaxLifeTicks() {
        return this.entityData.get(MAX_LIFE_TICKS);
    }

    private void setLifeTicks(int lifeTicks) {
        int clampedLife = Math.max(1, lifeTicks);
        this.entityData.set(LIFE_TICKS, clampedLife);
        this.entityData.set(MAX_LIFE_TICKS, clampedLife);
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
        return distance < 1600.0D;
    }
}
