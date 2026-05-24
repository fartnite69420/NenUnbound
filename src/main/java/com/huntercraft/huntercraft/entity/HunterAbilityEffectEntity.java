package com.huntercraft.huntercraft.entity;

import com.huntercraft.huntercraft.animation.AnimationType;
import com.huntercraft.huntercraft.data.HunterPlayerDataProvider;
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
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class HunterAbilityEffectEntity extends Entity implements GeoEntity {
    private static final EntityDataAccessor<String> EFFECT_TYPE = SynchedEntityData.defineId(HunterAbilityEffectEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Optional<UUID>> OWNER_ID = SynchedEntityData.defineId(HunterAbilityEffectEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final RawAnimation DASH_ANIMATION = RawAnimation.begin().thenPlay("dash");
    private static final RawAnimation DOUBLE_JUMP_ANIMATION = RawAnimation.begin().thenPlay("double_jump");
    private static final RawAnimation GUARD_ANIMATION = RawAnimation.begin().thenLoop("guard");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private int lifeTicks;

    public HunterAbilityEffectEntity(EntityType<? extends HunterAbilityEffectEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    public static void spawn(ServerPlayer owner, AnimationType animationType) {
        if (!(owner.level() instanceof ServerLevel serverLevel) || animationType == AnimationType.NONE) {
            return;
        }

        if (animationType == AnimationType.GUARD) {
            List<HunterAbilityEffectEntity> existing = serverLevel.getEntitiesOfClass(HunterAbilityEffectEntity.class,
                    owner.getBoundingBox().inflate(2.0D),
                    entity -> entity.isOwnedBy(owner) && entity.getEffectType() == AnimationType.GUARD);
            if (!existing.isEmpty()) {
                return;
            }
        }

        HunterAbilityEffectEntity effect = HunterEntityTypes.ABILITY_EFFECT.get().create(serverLevel);
        if (effect == null) {
            return;
        }
        effect.setOwner(owner);
        effect.setEffectType(animationType);
        effect.lifeTicks = animationType == AnimationType.GUARD ? Integer.MAX_VALUE : Math.max(6, animationType.durationTicks() + 4);
        effect.moveTo(owner.getX(), owner.getY(), owner.getZ(), owner.getYRot(), owner.getXRot());
        serverLevel.addFreshEntity(effect);
    }

    public static void stopGuard(ServerPlayer owner) {
        if (!(owner.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        List<HunterAbilityEffectEntity> existing = serverLevel.getEntitiesOfClass(HunterAbilityEffectEntity.class,
                owner.getBoundingBox().inflate(4.0D),
                entity -> entity.isOwnedBy(owner) && entity.getEffectType() == AnimationType.GUARD);
        existing.forEach(Entity::discard);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(EFFECT_TYPE, AnimationType.NONE.name());
        this.entityData.define(OWNER_ID, Optional.empty());
    }

    @Override
    protected void readAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {
        this.entityData.set(EFFECT_TYPE, tag.getString("EffectType"));
        if (tag.hasUUID("Owner")) {
            this.entityData.set(OWNER_ID, Optional.of(tag.getUUID("Owner")));
        }
        this.lifeTicks = tag.getInt("LifeTicks");
    }

    @Override
    protected void addAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {
        tag.putString("EffectType", this.entityData.get(EFFECT_TYPE));
        this.entityData.get(OWNER_ID).ifPresent(uuid -> tag.putUUID("Owner", uuid));
        tag.putInt("LifeTicks", this.lifeTicks);
    }

    @Override
    public void tick() {
        super.tick();
        Entity owner = this.getOwner();
        if (owner == null || !owner.isAlive()) {
            this.discard();
            return;
        }

        if (this.getEffectType() == AnimationType.GUARD) {
            boolean guarding = owner.getCapability(HunterPlayerDataProvider.CAPABILITY).map(data -> data.isGuarding()).orElse(false);
            if (!guarding) {
                this.discard();
                return;
            }
        } else {
            this.lifeTicks--;
            if (this.lifeTicks <= 0) {
                this.discard();
                return;
            }
        }

        Vec3 offset = getOffset(this.getEffectType(), owner);
        this.moveTo(owner.getX() + offset.x, owner.getY() + offset.y, owner.getZ() + offset.z, owner.getYRot(), owner.getXRot());
    }

    private static Vec3 getOffset(AnimationType animationType, Entity owner) {
        return switch (animationType) {
            case DASH -> owner.getLookAngle().normalize().scale(0.85D).add(0.0D, 1.05D, 0.0D);
            case DOUBLE_JUMP -> new Vec3(0.0D, 0.2D, 0.0D);
            case GUARD -> new Vec3(0.0D, 0.95D, 0.0D);
            default -> Vec3.ZERO;
        };
    }

    public AnimationType getEffectType() {
        return AnimationType.byName(this.entityData.get(EFFECT_TYPE));
    }

    public void setEffectType(AnimationType effectType) {
        this.entityData.set(EFFECT_TYPE, effectType.name());
    }

    public void setOwner(Entity owner) {
        this.entityData.set(OWNER_ID, Optional.of(owner.getUUID()));
    }

    public Entity getOwner() {
        return this.entityData.get(OWNER_ID).map(uuid -> this.level().getPlayerByUUID(uuid)).orElse(null);
    }

    public boolean isOwnedBy(Entity owner) {
        return owner.getUUID().equals(this.entityData.get(OWNER_ID).orElse(null));
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < 256.0D;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "effect_controller", 0, state -> {
            return switch (this.getEffectType()) {
                case DASH -> state.setAndContinue(DASH_ANIMATION);
                case DOUBLE_JUMP -> state.setAndContinue(DOUBLE_JUMP_ANIMATION);
                case GUARD -> state.setAndContinue(GUARD_ANIMATION);
                default -> software.bernie.geckolib.core.object.PlayState.STOP;
            };
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public double getTick(Object object) {
        return this.tickCount;
    }
}
