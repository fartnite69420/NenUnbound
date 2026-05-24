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

public class SlashEffectEntity extends Entity {
    private static final EntityDataAccessor<Float> ROLL = SynchedEntityData.defineId(SlashEffectEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> SCALE = SynchedEntityData.defineId(SlashEffectEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> LIFE_TICKS = SynchedEntityData.defineId(SlashEffectEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> MAX_LIFE_TICKS = SynchedEntityData.defineId(SlashEffectEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> COLOR = SynchedEntityData.defineId(SlashEffectEntity.class, EntityDataSerializers.INT);

    public SlashEffectEntity(EntityType<? extends SlashEffectEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    public static void spawn(ServerLevel level, Vec3 position, Vec3 direction, float roll, float scale, int lifeTicks) {
        spawn(level, position, direction, roll, scale, lifeTicks, 0xFFFFFF);
    }

    public static void spawn(ServerLevel level, Vec3 position, Vec3 direction, float roll, float scale, int lifeTicks, int color) {
        SlashEffectEntity entity = HunterEntityTypes.SLASH_EFFECT.get().create(level);
        if (entity == null) {
            return;
        }
        Vec3 look = direction.lengthSqr() > 1.0E-5D ? direction.normalize() : Vec3.directionFromRotation(0.0F, 0.0F);
        float yaw = (float) Math.toDegrees(Math.atan2(look.x, look.z));
        float pitch = (float) -Math.toDegrees(Math.atan2(look.y, Math.sqrt((look.x * look.x) + (look.z * look.z))));
        entity.moveTo(position.x, position.y, position.z, yaw, pitch);
        entity.setRoll(roll);
        entity.setScale(scale);
        entity.setLifeTicks(lifeTicks);
        entity.setColor(color);
        level.addFreshEntity(entity);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(ROLL, 0.0F);
        this.entityData.define(SCALE, 1.0F);
        this.entityData.define(LIFE_TICKS, 8);
        this.entityData.define(MAX_LIFE_TICKS, 8);
        this.entityData.define(COLOR, 0xFFFFFF);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        this.setRoll(tag.getFloat("Roll"));
        this.setScale(tag.getFloat("Scale"));
        int lifeTicks = tag.getInt("LifeTicks");
        this.entityData.set(LIFE_TICKS, lifeTicks);
        this.entityData.set(MAX_LIFE_TICKS, Math.max(1, tag.getInt("MaxLifeTicks")));
        this.setColor(tag.contains("Color") ? tag.getInt("Color") : 0xFFFFFF);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putFloat("Roll", this.getRoll());
        tag.putFloat("Scale", this.getScale());
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

    public float getRoll() {
        return this.entityData.get(ROLL);
    }

    private void setRoll(float roll) {
        this.entityData.set(ROLL, roll);
    }

    public float getScale() {
        return this.entityData.get(SCALE);
    }

    private void setScale(float scale) {
        this.entityData.set(SCALE, scale);
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
        return distance < 512.0D;
    }
}
