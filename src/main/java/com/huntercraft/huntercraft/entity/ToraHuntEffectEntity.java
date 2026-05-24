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

public class ToraHuntEffectEntity extends Entity {
    public static final int STYLE_CHARGE = 0;
    public static final int STYLE_STRIKE = 1;
    public static final int STYLE_FINISHER = 2;

    private static final EntityDataAccessor<Float> SCALE = SynchedEntityData.defineId(ToraHuntEffectEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> LIFE_TICKS = SynchedEntityData.defineId(ToraHuntEffectEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> MAX_LIFE_TICKS = SynchedEntityData.defineId(ToraHuntEffectEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> STYLE = SynchedEntityData.defineId(ToraHuntEffectEntity.class, EntityDataSerializers.INT);

    public ToraHuntEffectEntity(EntityType<? extends ToraHuntEffectEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
        this.noCulling = true;
    }

    public static void spawn(ServerLevel level, Vec3 position, Vec3 direction, float scale, int lifeTicks, int style) {
        ToraHuntEffectEntity entity = HunterEntityTypes.TORA_HUNT_EFFECT.get().create(level);
        if (entity == null) {
            return;
        }
        Vec3 look = direction.lengthSqr() > 1.0E-5D ? direction.normalize() : Vec3.directionFromRotation(0.0F, 0.0F);
        float yaw = (float) -Math.toDegrees(Math.atan2(look.x, look.z));
        entity.moveTo(position.x, position.y, position.z, yaw, 0.0F);
        entity.setScale(scale);
        entity.setLifeTicks(lifeTicks);
        entity.setStyle(style);
        level.addFreshEntity(entity);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(SCALE, 1.0F);
        this.entityData.define(LIFE_TICKS, 12);
        this.entityData.define(MAX_LIFE_TICKS, 12);
        this.entityData.define(STYLE, STYLE_STRIKE);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        this.setScale(tag.getFloat("Scale"));
        int lifeTicks = tag.getInt("LifeTicks");
        this.entityData.set(LIFE_TICKS, lifeTicks);
        this.entityData.set(MAX_LIFE_TICKS, Math.max(1, tag.getInt("MaxLifeTicks")));
        this.setStyle(tag.getInt("Style"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putFloat("Scale", this.getScale());
        tag.putInt("LifeTicks", this.getLifeTicks());
        tag.putInt("MaxLifeTicks", this.getMaxLifeTicks());
        tag.putInt("Style", this.getStyle());
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

    public int getStyle() {
        return this.entityData.get(STYLE);
    }

    private void setStyle(int style) {
        if (style == STYLE_CHARGE || style == STYLE_FINISHER) {
            this.entityData.set(STYLE, style);
        } else {
            this.entityData.set(STYLE, STYLE_STRIKE);
        }
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
