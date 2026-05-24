package com.huntercraft.huntercraft.entity;

import com.huntercraft.huntercraft.abilities.HunterAbilities;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

import java.util.Optional;
import java.util.UUID;

public class BungeeGumReflectEntity extends Entity {
    private static final EntityDataAccessor<Optional<UUID>> OWNER_ID = SynchedEntityData.defineId(BungeeGumReflectEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Integer> COLOR = SynchedEntityData.defineId(BungeeGumReflectEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> HIDDEN = SynchedEntityData.defineId(BungeeGumReflectEntity.class, EntityDataSerializers.BOOLEAN);
    private int lifeTicks = 54;

    public BungeeGumReflectEntity(EntityType<? extends BungeeGumReflectEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
        this.noCulling = true;
    }

    public static void spawn(ServerLevel level, ServerPlayer owner, int color, boolean hidden) {
        BungeeGumReflectEntity effect = HunterEntityTypes.BUNGEE_GUM_REFLECT.get().create(level);
        if (effect == null) {
            return;
        }
        effect.setOwner(owner);
        effect.setColor(color);
        effect.setHidden(hidden);
        effect.lifeTicks = 54;
        Vec3 front = owner.getLookAngle().normalize().scale(0.45D);
        effect.moveTo(owner.getX() + front.x, owner.getY() + 0.05D, owner.getZ() + front.z, owner.getYRot(), owner.getXRot());
        level.addFreshEntity(effect);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(OWNER_ID, Optional.empty());
        this.entityData.define(COLOR, 0xFF4EDB);
        this.entityData.define(HIDDEN, false);
    }

    public void setOwner(ServerPlayer owner) {
        this.entityData.set(OWNER_ID, Optional.of(owner.getUUID()));
    }

    public Entity getOwner() {
        return this.entityData.get(OWNER_ID).map(uuid -> this.level().getPlayerByUUID(uuid)).orElse(null);
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

    @Override
    public void tick() {
        super.tick();
        Entity owner = this.getOwner();
        if (owner == null || !owner.isAlive()) {
            this.discard();
            return;
        }
        if (!this.level().isClientSide() && owner instanceof ServerPlayer player) {
            HunterPlayerData data = HunterDataUtil.getOptional(player).orElse(null);
            if (data == null || !HunterAbilities.ELASTIC_REFLECT.isActive(data)) {
                this.discard();
                return;
            }
        }
        if (!this.level().isClientSide()) {
            if (--this.lifeTicks <= 0) {
                this.discard();
                return;
            }
        }

        Vec3 front = owner.getLookAngle().normalize().scale(0.45D);
        this.moveTo(owner.getX() + front.x, owner.getY() + 0.05D, owner.getZ() + front.z, owner.getYRot(), owner.getXRot());
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.hasUUID("Owner")) {
            this.entityData.set(OWNER_ID, Optional.of(tag.getUUID("Owner")));
        }
        this.entityData.set(COLOR, tag.getInt("Color"));
        this.entityData.set(HIDDEN, tag.getBoolean("Hidden"));
        this.lifeTicks = tag.getInt("LifeTicks");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        this.entityData.get(OWNER_ID).ifPresent(uuid -> tag.putUUID("Owner", uuid));
        tag.putInt("Color", this.getColor());
        tag.putBoolean("Hidden", this.isHidden());
        tag.putInt("LifeTicks", this.lifeTicks);
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
