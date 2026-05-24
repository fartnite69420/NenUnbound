package com.huntercraft.huntercraft.entity;

import com.huntercraft.huntercraft.data.HunterPlayerDataProvider;
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
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

import java.util.UUID;

public class MartialWhirlwindEffectEntity extends Entity {
    private static final String ABILITY_ID = "whirlwind_arc";
    private static final EntityDataAccessor<Float> SCALE = SynchedEntityData.defineId(MartialWhirlwindEffectEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> LIFE_TICKS = SynchedEntityData.defineId(MartialWhirlwindEffectEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> MAX_LIFE_TICKS = SynchedEntityData.defineId(MartialWhirlwindEffectEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<String> OWNER_UUID = SynchedEntityData.defineId(MartialWhirlwindEffectEntity.class, EntityDataSerializers.STRING);

    public MartialWhirlwindEffectEntity(EntityType<? extends MartialWhirlwindEffectEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
        this.noCulling = true;
    }

    public static void spawn(ServerLevel level, LivingEntity owner, Vec3 direction, float scale, int lifeTicks) {
        MartialWhirlwindEffectEntity entity = HunterEntityTypes.MARTIAL_WHIRLWIND_EFFECT.get().create(level);
        if (entity == null) {
            return;
        }
        Vec3 look = direction.lengthSqr() > 1.0E-5D ? direction.normalize() : Vec3.directionFromRotation(0.0F, 0.0F);
        float yaw = (float) -Math.toDegrees(Math.atan2(look.x, look.z));
        Vec3 position = owner.position().add(0.0D, 0.55D, 0.0D);
        entity.moveTo(position.x, position.y, position.z, yaw, 0.0F);
        entity.setOwnerUuid(owner.getUUID());
        entity.setScale(scale);
        entity.setLifeTicks(lifeTicks);
        level.addFreshEntity(entity);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(SCALE, 1.0F);
        this.entityData.define(LIFE_TICKS, 10);
        this.entityData.define(MAX_LIFE_TICKS, 10);
        this.entityData.define(OWNER_UUID, "");
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        this.setScale(tag.getFloat("Scale"));
        this.setOwnerUuid(tag.hasUUID("Owner") ? tag.getUUID("Owner") : null);
        int lifeTicks = tag.getInt("LifeTicks");
        this.entityData.set(LIFE_TICKS, lifeTicks);
        this.entityData.set(MAX_LIFE_TICKS, Math.max(1, tag.getInt("MaxLifeTicks")));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putFloat("Scale", this.getScale());
        UUID ownerUuid = this.getOwnerUuid();
        if (ownerUuid != null) {
            tag.putUUID("Owner", ownerUuid);
        }
        tag.putInt("LifeTicks", this.getLifeTicks());
        tag.putInt("MaxLifeTicks", this.getMaxLifeTicks());
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide && this.level() instanceof ServerLevel serverLevel) {
            UUID ownerUuid = this.getOwnerUuid();
            if (ownerUuid != null && serverLevel.getEntity(ownerUuid) instanceof LivingEntity owner && owner.isAlive()) {
                if (owner instanceof ServerPlayer player && !isWhirlwindActive(player)) {
                    this.discard();
                    return;
                }
                Vec3 forward = Vec3.directionFromRotation(0.0F, owner.getYRot()).multiply(1.0D, 0.0D, 1.0D);
                if (forward.lengthSqr() < 1.0E-5D) {
                    forward = new Vec3(0.0D, 0.0D, 1.0D);
                }
                forward = forward.normalize();
                float yaw = (float) -Math.toDegrees(Math.atan2(forward.x, forward.z));
                Vec3 position = owner.position().add(0.0D, 0.55D, 0.0D);
                this.moveTo(position.x, position.y, position.z, yaw, 0.0F);
            } else {
                this.discard();
                return;
            }
        }
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

    private UUID getOwnerUuid() {
        String value = this.entityData.get(OWNER_UUID);
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private void setOwnerUuid(UUID ownerUuid) {
        this.entityData.set(OWNER_UUID, ownerUuid == null ? "" : ownerUuid.toString());
    }

    private static boolean isWhirlwindActive(ServerPlayer player) {
        return player.getCapability(HunterPlayerDataProvider.CAPABILITY)
                .map(data -> data.isActiveAbility(ABILITY_ID) && data.getActiveAbilityTargetUuid().isBlank())
                .orElse(false);
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
