package com.huntercraft.huntercraft.entity;

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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

import java.util.Optional;
import java.util.UUID;

public class ChainWrapVisualEntity extends Entity {
    private static final EntityDataAccessor<Optional<UUID>> OWNER_ID =
            SynchedEntityData.defineId(ChainWrapVisualEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Optional<UUID>> TARGET_ID =
            SynchedEntityData.defineId(ChainWrapVisualEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Float> TARGET_WIDTH =
            SynchedEntityData.defineId(ChainWrapVisualEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> TARGET_HEIGHT =
            SynchedEntityData.defineId(ChainWrapVisualEntity.class, EntityDataSerializers.FLOAT);

    private int lifeTicks = 90;

    public ChainWrapVisualEntity(EntityType<? extends ChainWrapVisualEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public static void spawn(ServerLevel level, LivingEntity target, int durationTicks) {
        spawn(level, null, target, durationTicks);
    }

    public static void spawn(ServerLevel level, ServerPlayer owner, LivingEntity target, int durationTicks) {
        ChainWrapVisualEntity visual = HunterEntityTypes.CHAIN_WRAP_VISUAL.get().create(level);
        if (visual == null) {
            return;
        }
        visual.setOwner(owner);
        visual.setTarget(target);
        visual.lifeTicks = Math.max(1, durationTicks);
        Vec3 center = target.position().add(0.0D, target.getBbHeight() * 0.5D, 0.0D);
        visual.moveTo(center.x, center.y, center.z, target.getYRot(), target.getXRot());
        level.addFreshEntity(visual);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level() instanceof ServerLevel level) {
            LivingEntity target = this.getTarget(level);
            if (target == null || !target.isAlive()) {
                this.discard();
                return;
            }
            Vec3 center = target.position().add(0.0D, target.getBbHeight() * 0.5D, 0.0D);
            this.setPos(center.x, center.y, center.z);
            this.setYRot(target.getYRot());
            this.entityData.set(TARGET_WIDTH, target.getBbWidth());
            this.entityData.set(TARGET_HEIGHT, target.getBbHeight());
        }
        if (--this.lifeTicks <= 0) {
            this.discard();
        }
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(OWNER_ID, Optional.empty());
        this.entityData.define(TARGET_ID, Optional.empty());
        this.entityData.define(TARGET_WIDTH, 0.6F);
        this.entityData.define(TARGET_HEIGHT, 1.8F);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.hasUUID("Owner")) {
            this.entityData.set(OWNER_ID, Optional.of(tag.getUUID("Owner")));
        }
        if (tag.hasUUID("Target")) {
            this.entityData.set(TARGET_ID, Optional.of(tag.getUUID("Target")));
        }
        this.entityData.set(TARGET_WIDTH, tag.getFloat("TargetWidth"));
        this.entityData.set(TARGET_HEIGHT, tag.getFloat("TargetHeight"));
        this.lifeTicks = tag.getInt("LifeTicks");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        this.entityData.get(OWNER_ID).ifPresent(uuid -> tag.putUUID("Owner", uuid));
        this.entityData.get(TARGET_ID).ifPresent(uuid -> tag.putUUID("Target", uuid));
        tag.putFloat("TargetWidth", this.getTargetWidth());
        tag.putFloat("TargetHeight", this.getTargetHeight());
        tag.putInt("LifeTicks", this.lifeTicks);
    }

    public Optional<UUID> getTargetId() {
        return this.entityData.get(TARGET_ID);
    }

    public Optional<UUID> getOwnerId() {
        return this.entityData.get(OWNER_ID);
    }

    public float getTargetWidth() {
        return this.entityData.get(TARGET_WIDTH);
    }

    public float getTargetHeight() {
        return this.entityData.get(TARGET_HEIGHT);
    }

    public LivingEntity getTarget(Level level) {
        return this.getTargetId().map(uuid -> {
            LivingEntity player = level.getPlayerByUUID(uuid);
            if (player != null) {
                return player;
            }
            return level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(32.0D), candidate -> candidate.getUUID().equals(uuid))
                    .stream()
                    .findFirst()
                    .orElse(null);
        }).orElse(null);
    }

    public Player getOwnerPlayer(Level level) {
        return this.getOwnerId()
                .map(level::getPlayerByUUID)
                .orElse(null);
    }

    private void setOwner(ServerPlayer owner) {
        this.entityData.set(OWNER_ID, owner == null ? Optional.empty() : Optional.of(owner.getUUID()));
    }

    private void setTarget(LivingEntity target) {
        this.entityData.set(TARGET_ID, Optional.of(target.getUUID()));
        this.entityData.set(TARGET_WIDTH, target.getBbWidth());
        this.entityData.set(TARGET_HEIGHT, target.getBbHeight());
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
