package com.huntercraft.huntercraft.entity;

import com.huntercraft.huntercraft.abilities.HunterAbilities;
import com.huntercraft.huntercraft.abilities.bungeegum.ElasticAuraManager;
import com.huntercraft.huntercraft.data.HunterPlayerData;
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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

import java.util.Comparator;
import java.util.Optional;
import java.util.UUID;

public class ElasticAuraProjectileEntity extends Entity {
    public static final String MODE_ATTACH = "attach";
    public static final String MODE_PULL = "pull";
    public static final String MODE_TRAP = "trap";
    private static final EntityDataAccessor<Optional<UUID>> OWNER_ID = SynchedEntityData.defineId(ElasticAuraProjectileEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<String> MODE = SynchedEntityData.defineId(ElasticAuraProjectileEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Integer> COLOR = SynchedEntityData.defineId(ElasticAuraProjectileEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> HIDDEN = SynchedEntityData.defineId(ElasticAuraProjectileEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<String> TAGGED_ENTITY_UUID = SynchedEntityData.defineId(ElasticAuraProjectileEntity.class, EntityDataSerializers.STRING);
    private int lifeTicks = 8;

    public ElasticAuraProjectileEntity(EntityType<? extends ElasticAuraProjectileEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(OWNER_ID, Optional.empty());
        this.entityData.define(MODE, MODE_ATTACH);
        this.entityData.define(COLOR, 0xFFFFFF);
        this.entityData.define(HIDDEN, false);
        this.entityData.define(TAGGED_ENTITY_UUID, "");
    }

    public String getTaggedEntityUuid() {
        return this.entityData.get(TAGGED_ENTITY_UUID);
    }

    public void configure(ServerPlayer owner, String mode, boolean hidden) {
        this.entityData.set(OWNER_ID, Optional.of(owner.getUUID()));
        this.entityData.set(MODE, mode);
        this.entityData.set(COLOR, HunterDataUtil.get(owner).getNenAuraColor());
        this.entityData.set(HIDDEN, hidden);
    }

    public int getColor() {
        return this.entityData.get(COLOR);
    }

    public boolean isHidden() {
        return this.entityData.get(HIDDEN);
    }

    public String getMode() {
        return this.entityData.get(MODE);
    }

    public ServerPlayer getOwner() {
        return this.entityData.get(OWNER_ID)
                .map(uuid -> this.level().getPlayerByUUID(uuid) instanceof ServerPlayer player ? player : null)
                .orElse(null);
    }

    public Player getOwnerPlayerEntity() {
        return this.entityData.get(OWNER_ID)
                .map(this.level()::getPlayerByUUID)
                .orElse(null);
    }

    @Override
    public void tick() {
        super.tick();
        if (!(this.level() instanceof ServerLevel level)) {
            return;
        }
        ServerPlayer owner = this.getOwner();
        if (owner == null || !owner.isAlive()) {
            this.discard();
            return;
        }

        Vec3 start = this.position();
        Vec3 end = start.add(this.getDeltaMovement());
        BlockHitResult blockHit = level.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
        EntityHitResult entityHit = this.findEntityHit(start, end, owner);
        if (entityHit != null && (blockHit.getType() == HitResult.Type.MISS || start.distanceToSqr(entityHit.getLocation()) <= start.distanceToSqr(blockHit.getLocation()))) {
            this.onEntityHit(owner, entityHit);
            return;
        }
        if (blockHit.getType() != HitResult.Type.MISS) {
            this.onBlockHit(owner, blockHit);
            return;
        }

        this.setPos(end.x, end.y, end.z);
        if (--this.lifeTicks <= 0) {
            this.discard();
        }
    }

    private EntityHitResult findEntityHit(Vec3 start, Vec3 end, ServerPlayer owner) {
        AABB box = this.getBoundingBox().expandTowards(this.getDeltaMovement()).inflate(0.45D);
        return this.level().getEntitiesOfClass(LivingEntity.class, box, target -> target.isAlive() && target != owner)
                .stream()
                .map(target -> {
                    Vec3 clip = target.getBoundingBox().inflate(0.25D).clip(start, end).orElse(null);
                    return clip == null ? null : new EntityHitResult(target, clip);
                })
                .filter(result -> result != null)
                .min(Comparator.comparingDouble(result -> start.distanceToSqr(result.getLocation())))
                .orElse(null);
    }

    private void onBlockHit(ServerPlayer owner, BlockHitResult result) {
        if (MODE_ATTACH.equals(this.getMode())) {
            ElasticAuraManager.spawnAnchorConstruct(owner.serverLevel(), owner, result.getLocation(), this.isHidden());
        } else if (MODE_TRAP.equals(this.getMode())) {
            ElasticAuraManager.placeTrap(owner.serverLevel(), owner, result.getLocation().add(0.0D, 0.05D, 0.0D), this.isHidden());
        }
        this.discard();
    }

    private void onEntityHit(ServerPlayer owner, EntityHitResult result) {
        if (!(result.getEntity() instanceof LivingEntity target)) {
            this.discard();
            return;
        }
        HunterPlayerData data = HunterDataUtil.get(owner);
        if (MODE_ATTACH.equals(this.getMode())) {
            // Tag the entity directly instead of creating a floor anchor
            ElasticAuraManager.addEntityTag(owner, target, this.isHidden());
            // Sync the tagged entity to the client so the renderer draws the string to it
            this.entityData.set(TAGGED_ENTITY_UUID, target.getStringUUID());
        } else if (MODE_PULL.equals(this.getMode())) {
            HunterAbilities.ELASTIC_PULL.startSequence(owner, data, target, this.isHidden());
        } else if (MODE_TRAP.equals(this.getMode())) {
            ElasticAuraManager.placeTrap(owner.serverLevel(), owner, result.getLocation().add(0.0D, 0.05D, 0.0D), this.isHidden());
        }
        this.discard();
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.hasUUID("Owner")) {
            this.entityData.set(OWNER_ID, Optional.of(tag.getUUID("Owner")));
        }
        this.entityData.set(MODE, tag.getString("Mode"));
        this.entityData.set(COLOR, tag.getInt("Color"));
        this.entityData.set(HIDDEN, tag.getBoolean("Hidden"));
        this.entityData.set(TAGGED_ENTITY_UUID, tag.getString("TaggedEntityUuid"));
        this.lifeTicks = tag.getInt("LifeTicks");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        this.entityData.get(OWNER_ID).ifPresent(uuid -> tag.putUUID("Owner", uuid));
        tag.putString("Mode", this.getMode());
        tag.putInt("Color", this.getColor());
        tag.putBoolean("Hidden", this.isHidden());
        tag.putString("TaggedEntityUuid", this.getTaggedEntityUuid());
        tag.putInt("LifeTicks", this.lifeTicks);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
