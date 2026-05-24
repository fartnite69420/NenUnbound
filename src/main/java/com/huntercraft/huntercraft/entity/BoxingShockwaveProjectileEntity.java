package com.huntercraft.huntercraft.entity;

import com.huntercraft.huntercraft.damage.HunterDamageSources;
import com.huntercraft.huntercraft.util.HunterDataUtil;
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

public class BoxingShockwaveProjectileEntity extends Entity {
    private static final EntityDataAccessor<Float> SCALE = SynchedEntityData.defineId(BoxingShockwaveProjectileEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> LIFE_TICKS = SynchedEntityData.defineId(BoxingShockwaveProjectileEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> MAX_LIFE_TICKS = SynchedEntityData.defineId(BoxingShockwaveProjectileEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> DIR_X = SynchedEntityData.defineId(BoxingShockwaveProjectileEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DIR_Y = SynchedEntityData.defineId(BoxingShockwaveProjectileEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DIR_Z = SynchedEntityData.defineId(BoxingShockwaveProjectileEntity.class, EntityDataSerializers.FLOAT);
    private UUID ownerUuid;
    private float damage;
    private double speed;
    private double knockback;
    private final Set<UUID> hitEntities = new HashSet<>();

    public BoxingShockwaveProjectileEntity(EntityType<? extends BoxingShockwaveProjectileEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    public static void spawn(ServerLevel level, ServerPlayer owner, Vec3 position, Vec3 direction, float damage, double speed, double knockback, float scale, int lifeTicks) {
        BoxingShockwaveProjectileEntity entity = HunterEntityTypes.BOXING_SHOCKWAVE_PROJECTILE.get().create(level);
        if (entity == null) {
            return;
        }
        Vec3 look = direction.lengthSqr() > 1.0E-5D ? direction.normalize() : owner.getLookAngle().normalize();
        float yaw = (float) Math.toDegrees(Math.atan2(look.x, look.z));
        float pitch = (float) -Math.toDegrees(Math.atan2(look.y, Math.sqrt((look.x * look.x) + (look.z * look.z))));
        entity.moveTo(position.x, position.y, position.z, yaw, pitch);
        entity.ownerUuid = owner.getUUID();
        entity.damage = damage;
        entity.speed = speed;
        entity.knockback = knockback;
        entity.setDirection(look);
        entity.setScale(scale);
        entity.setLifeTicks(lifeTicks);
        level.addFreshEntity(entity);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(SCALE, 1.0F);
        this.entityData.define(LIFE_TICKS, 24);
        this.entityData.define(MAX_LIFE_TICKS, 24);
        this.entityData.define(DIR_X, 0.0F);
        this.entityData.define(DIR_Y, 0.0F);
        this.entityData.define(DIR_Z, 1.0F);
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide) {
            Vec3 forward = getPulseDirection();
            Vec3 motion = forward.scale(this.speed);
            if (this.level() instanceof ServerLevel serverLevel) {
                float trailScale = this.getScale() * (this.tickCount % 2 == 0 ? 1.0F : 0.82F);
                TitanCannonWaveEntity.spawn(serverLevel, this.position().subtract(forward.scale(0.35D)), forward, trailScale, 9);
            }
            this.setPos(this.position().add(motion));
            AABB hitBox = this.getBoundingBox().inflate(1.55D, 0.9D, 1.55D).expandTowards(motion);
            ServerPlayer owner = getOwner();
            for (LivingEntity target : this.level().getEntitiesOfClass(LivingEntity.class, hitBox, entity -> entity.isAlive() && entity != owner && !this.hitEntities.contains(entity.getUUID()))) {
                this.hitEntities.add(target.getUUID());
                target.hurt(HunterDamageSources.physical(this.level(), this, owner != null ? owner : this), this.damage);
                HunterDataUtil.applyStun(target, owner, 8);
                target.setDeltaMovement(forward.x * this.knockback, 0.35D, forward.z * this.knockback);
                target.hurtMarked = true;
            }
        }
        int remaining = this.getLifeTicks() - 1;
        this.entityData.set(LIFE_TICKS, remaining);
        if (remaining <= 0) {
            this.discard();
        }
    }

    private ServerPlayer getOwner() {
        if (this.ownerUuid == null || !(this.level() instanceof ServerLevel serverLevel)) {
            return null;
        }
        return serverLevel.getPlayerByUUID(this.ownerUuid) instanceof ServerPlayer player ? player : null;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        this.setScale(tag.getFloat("Scale"));
        this.setLifeTicks(tag.getInt("LifeTicks"));
        this.entityData.set(MAX_LIFE_TICKS, Math.max(1, tag.getInt("MaxLifeTicks")));
        this.setDirection(new Vec3(tag.getFloat("DirX"), tag.getFloat("DirY"), tag.getFloat("DirZ")));
        this.damage = tag.getFloat("Damage");
        this.speed = tag.getDouble("Speed");
        this.knockback = tag.getDouble("Knockback");
        if (tag.hasUUID("Owner")) {
            this.ownerUuid = tag.getUUID("Owner");
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putFloat("Scale", this.getScale());
        tag.putInt("LifeTicks", this.getLifeTicks());
        tag.putInt("MaxLifeTicks", this.getMaxLifeTicks());
        tag.putFloat("DirX", this.entityData.get(DIR_X));
        tag.putFloat("DirY", this.entityData.get(DIR_Y));
        tag.putFloat("DirZ", this.entityData.get(DIR_Z));
        tag.putFloat("Damage", this.damage);
        tag.putDouble("Speed", this.speed);
        tag.putDouble("Knockback", this.knockback);
        if (this.ownerUuid != null) {
            tag.putUUID("Owner", this.ownerUuid);
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
}
