package com.huntercraft.huntercraft.entity;

import com.huntercraft.huntercraft.data.HunterPlayerData;
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

public class NenAuraEffectEntity extends Entity {
    public static final int STYLE_TEN = 0;
    public static final int STYLE_REN = 1;
    public static final int STYLE_KEN = 2;

    private static final EntityDataAccessor<String> OWNER_UUID = SynchedEntityData.defineId(NenAuraEffectEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Integer> STYLE = SynchedEntityData.defineId(NenAuraEffectEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> COLOR = SynchedEntityData.defineId(NenAuraEffectEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> NEN_LEVEL = SynchedEntityData.defineId(NenAuraEffectEntity.class, EntityDataSerializers.INT);

    public NenAuraEffectEntity(EntityType<? extends NenAuraEffectEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
        this.noCulling = true;
    }

    public static void spawn(ServerLevel level, LivingEntity owner, HunterPlayerData data, int style) {
        NenAuraEffectEntity entity = HunterEntityTypes.NEN_AURA_EFFECT.get().create(level);
        if (entity == null) {
            return;
        }
        entity.setOwnerUuid(owner.getUUID());
        entity.setStyle(style);
        entity.setColor(data.getNenAuraColor());
        entity.setNenLevel(data.getNenLevel());
        entity.moveTo(owner.getX(), owner.getY(), owner.getZ(), owner.getYRot(), 0.0F);
        level.addFreshEntity(entity);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(OWNER_UUID, "");
        this.entityData.define(STYLE, STYLE_TEN);
        this.entityData.define(COLOR, 0x5BE5FF);
        this.entityData.define(NEN_LEVEL, 1);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        this.setOwnerUuid(tag.hasUUID("Owner") ? tag.getUUID("Owner") : null);
        this.setStyle(tag.getInt("Style"));
        this.setColor(tag.getInt("Color"));
        this.setNenLevel(tag.getInt("NenLevel"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        UUID ownerUuid = this.getOwnerUuid();
        if (ownerUuid != null) {
            tag.putUUID("Owner", ownerUuid);
        }
        tag.putInt("Style", this.getStyle());
        tag.putInt("Color", this.getColor());
        tag.putInt("NenLevel", this.getNenLevel());
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide || !(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        UUID ownerUuid = this.getOwnerUuid();
        if (ownerUuid == null || !(serverLevel.getEntity(ownerUuid) instanceof LivingEntity owner) || !owner.isAlive()) {
            this.discard();
            return;
        }
        if (owner instanceof ServerPlayer player) {
            HunterPlayerData data = player.getCapability(HunterPlayerDataProvider.CAPABILITY).orElse(null);
            if (data == null || !isStyleStillActive(data)) {
                this.discard();
                return;
            }
            this.setColor(data.getNenAuraColor());
            this.setNenLevel(data.getNenLevel());
        }
        this.moveTo(owner.getX(), owner.getY(), owner.getZ(), owner.getYRot(), 0.0F);
    }

    private boolean isStyleStillActive(HunterPlayerData data) {
        return switch (this.getStyle()) {
            case STYLE_REN -> data.isRenActive();
            case STYLE_KEN -> data.isKenActive();
            default -> data.isTenActive();
        };
    }

    public int getStyle() {
        return this.entityData.get(STYLE);
    }

    private void setStyle(int style) {
        if (style == STYLE_REN || style == STYLE_KEN) {
            this.entityData.set(STYLE, style);
        } else {
            this.entityData.set(STYLE, STYLE_TEN);
        }
    }

    public int getColor() {
        return this.entityData.get(COLOR);
    }

    private void setColor(int color) {
        this.entityData.set(COLOR, color & 0xFFFFFF);
    }

    public int getNenLevel() {
        return this.entityData.get(NEN_LEVEL);
    }

    private void setNenLevel(int nenLevel) {
        this.entityData.set(NEN_LEVEL, Math.max(1, Math.min(HunterPlayerData.MAX_NEN_LEVEL, nenLevel)));
    }

    public UUID getOwnerUuid() {
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

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < 1600.0D;
    }
}
