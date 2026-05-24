package com.huntercraft.huntercraft.entity;

import com.huntercraft.huntercraft.abilities.bungeegum.ElasticAuraManager;
import com.huntercraft.huntercraft.faction.FactionUtil;
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
import net.minecraft.world.phys.AABB;
import net.minecraftforge.network.NetworkHooks;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ElasticAuraConstructEntity extends Entity {
    public static final String TYPE_ANCHOR = "anchor";
    public static final String TYPE_TRAP = "trap";
    public static final String TYPE_ENTITY_TAG = "entity_tag";
    public static final String TYPE_BIND_STRING = "bind_string";
    public static final String TYPE_PULL_STRING = "pull_string";
    private static final EntityDataAccessor<Optional<UUID>> OWNER_ID = SynchedEntityData.defineId(ElasticAuraConstructEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<String> CONSTRUCT_TYPE = SynchedEntityData.defineId(ElasticAuraConstructEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Integer> COLOR = SynchedEntityData.defineId(ElasticAuraConstructEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> HIDDEN = SynchedEntityData.defineId(ElasticAuraConstructEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> TRIGGERED = SynchedEntityData.defineId(ElasticAuraConstructEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<String> HELD_TARGET_UUID = SynchedEntityData.defineId(ElasticAuraConstructEntity.class, EntityDataSerializers.STRING);
    private int durationTicks = ElasticAuraManager.TRAP_DURATION_TICKS;
    private int triggeredTicksRemaining;

    public ElasticAuraConstructEntity(EntityType<? extends ElasticAuraConstructEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
        this.noCulling = true;
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(OWNER_ID, Optional.empty());
        this.entityData.define(CONSTRUCT_TYPE, TYPE_TRAP);
        this.entityData.define(COLOR, 0xFFFFFF);
        this.entityData.define(HIDDEN, false);
        this.entityData.define(TRIGGERED, false);
        this.entityData.define(HELD_TARGET_UUID, "");
    }

    public void setOwner(ServerPlayer owner) {
        this.entityData.set(OWNER_ID, Optional.of(owner.getUUID()));
    }

    public ServerPlayer getOwnerPlayer() {
        return this.entityData.get(OWNER_ID)
                .map(uuid -> this.level().getPlayerByUUID(uuid) instanceof ServerPlayer player ? player : null)
                .orElse(null);
    }

    public Player getOwnerPlayerEntity() {
        return this.entityData.get(OWNER_ID)
                .map(this.level()::getPlayerByUUID)
                .orElse(null);
    }

    public boolean isOwnedBy(ServerPlayer player) {
        return player != null && player.getUUID().equals(this.entityData.get(OWNER_ID).orElse(null));
    }

    public void setConstructType(String type) {
        this.entityData.set(CONSTRUCT_TYPE, type);
    }

    public boolean isTrap() {
        return TYPE_TRAP.equals(this.entityData.get(CONSTRUCT_TYPE));
    }

    public boolean isStringVisual() {
        String type = this.entityData.get(CONSTRUCT_TYPE);
        return TYPE_ENTITY_TAG.equals(type) || TYPE_BIND_STRING.equals(type) || TYPE_PULL_STRING.equals(type);
    }

    public String getConstructType() {
        return this.entityData.get(CONSTRUCT_TYPE);
    }

    public void setColor(int color) {
        this.entityData.set(COLOR, color);
    }

    public int getColor() {
        return this.entityData.get(COLOR);
    }

    public void setHidden(boolean hidden) {
        this.entityData.set(HIDDEN, hidden);
    }

    public boolean isHidden() {
        return this.entityData.get(HIDDEN);
    }

    public boolean isTriggered() {
        return this.entityData.get(TRIGGERED);
    }

    public void setDurationTicks(int durationTicks) {
        this.durationTicks = durationTicks;
    }

    public void onTriggered(int lingerTicks) {
        this.entityData.set(TRIGGERED, true);
        this.triggeredTicksRemaining = Math.max(1, lingerTicks);
    }

    public void setHeldTargetUuid(String uuid) {
        this.entityData.set(HELD_TARGET_UUID, uuid == null ? "" : uuid);
    }

    public String getHeldTargetUuid() {
        return this.entityData.get(HELD_TARGET_UUID);
    }

    @Override
    public void tick() {
        super.tick();
        if (!(this.level() instanceof ServerLevel level)) {
            return;
        }
        ServerPlayer owner = this.getOwnerPlayer();
        if (owner == null || !owner.isAlive()) {
            this.discard();
            return;
        }
        if (this.isTriggered()) {
            LivingEntity heldTarget = ElasticAuraManager.resolveLiving(level, this.getHeldTargetUuid());
            if (heldTarget != null && heldTarget.isAlive()) {
                heldTarget.teleportTo(this.getX(), this.getY() + 0.05D, this.getZ());
                heldTarget.setDeltaMovement(0.0D, Math.min(0.08D, heldTarget.getDeltaMovement().y), 0.0D);
                heldTarget.hurtMarked = true;
            }
            if (--this.triggeredTicksRemaining <= 0) {
                this.discard();
            }
            return;
        }
        if (--this.durationTicks <= 0) {
            this.discard();
            return;
        }
        if (!this.isTrap()) {
            return;
        }
        AABB box = this.getBoundingBox().inflate(0.9D, 0.45D, 0.9D);
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, box, entity -> entity.isAlive() && entity != owner);
        for (LivingEntity target : targets) {
            if (target instanceof Player player && FactionUtil.areFactionMates(owner, player)) {
                continue;
            }
            ElasticAuraManager.triggerTrap(owner, target, this);
            break;
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.hasUUID("Owner")) {
            this.entityData.set(OWNER_ID, Optional.of(tag.getUUID("Owner")));
        }
        this.entityData.set(CONSTRUCT_TYPE, tag.getString("ConstructType"));
        this.entityData.set(COLOR, tag.getInt("Color"));
        this.entityData.set(HIDDEN, tag.getBoolean("Hidden"));
        this.entityData.set(TRIGGERED, tag.getBoolean("Triggered"));
        this.entityData.set(HELD_TARGET_UUID, tag.getString("HeldTargetUuid"));
        this.durationTicks = tag.getInt("DurationTicks");
        this.triggeredTicksRemaining = tag.getInt("TriggeredTicksRemaining");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        this.entityData.get(OWNER_ID).ifPresent(uuid -> tag.putUUID("Owner", uuid));
        tag.putString("ConstructType", this.getConstructType());
        tag.putInt("Color", this.getColor());
        tag.putBoolean("Hidden", this.isHidden());
        tag.putBoolean("Triggered", this.isTriggered());
        tag.putString("HeldTargetUuid", this.getHeldTargetUuid());
        tag.putInt("DurationTicks", this.durationTicks);
        tag.putInt("TriggeredTicksRemaining", this.triggeredTicksRemaining);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < 4096.0D;
    }
}
