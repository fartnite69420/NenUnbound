package com.huntercraft.huntercraft.entity;

import com.huntercraft.huntercraft.abilities.HunterAbilities;
import com.huntercraft.huntercraft.abilities.SkillTreeCombatAbility;
import com.huntercraft.huntercraft.abilities.AbilitySourceType;
import com.huntercraft.huntercraft.abilities.nenability.NenTechniqueAbility;
import com.huntercraft.huntercraft.abilities.deeppurple.SmokeyChainAbility;
import com.huntercraft.huntercraft.damage.HunterDamageSources;
import com.huntercraft.huntercraft.data.HunterPlayerData;
import com.huntercraft.huntercraft.effect.HunterMobEffects;
import com.huntercraft.huntercraft.particle.HunterParticles;
import com.huntercraft.huntercraft.util.HunterDataUtil;
import com.huntercraft.huntercraft.util.NenVowUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class SmokeyChainProjectileEntity extends Entity {
    private static final EntityDataAccessor<Optional<UUID>> OWNER_ID =
            SynchedEntityData.defineId(SmokeyChainProjectileEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<String> ABILITY_ID =
            SynchedEntityData.defineId(SmokeyChainProjectileEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Float> ORIGIN_X =
            SynchedEntityData.defineId(SmokeyChainProjectileEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> ORIGIN_Y =
            SynchedEntityData.defineId(SmokeyChainProjectileEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> ORIGIN_Z =
            SynchedEntityData.defineId(SmokeyChainProjectileEntity.class, EntityDataSerializers.FLOAT);

    private int lifeTicks = 10;

    public SmokeyChainProjectileEntity(EntityType<? extends SmokeyChainProjectileEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public static SmokeyChainProjectileEntity create(ServerLevel level, ServerPlayer owner, Vec3 direction, String abilityId) {
        SmokeyChainProjectileEntity projectile = HunterEntityTypes.SMOKEY_CHAIN_PROJECTILE.get().create(level);
        if (projectile == null) {
            return null;
        }
        projectile.setOwner(owner);
        projectile.setAbilityId(abilityId);
        Vec3 launchDirection = direction.lengthSqr() > 1.0E-5D ? direction.normalize() : owner.getLookAngle().normalize();
        Vec3 hand = resolveChainStart(owner, launchDirection);
        Vec3 head = hand.add(launchDirection.scale(0.18D));
        projectile.moveTo(head.x, head.y, head.z, owner.getYRot(), owner.getXRot());
        projectile.setOrigin(hand);
        projectile.setDeltaMovement(launchDirection.scale(2.85D));
        return projectile;
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(OWNER_ID, Optional.empty());
        this.entityData.define(ABILITY_ID, "");
        this.entityData.define(ORIGIN_X, 0.0F);
        this.entityData.define(ORIGIN_Y, 0.0F);
        this.entityData.define(ORIGIN_Z, 0.0F);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.hasUUID("Owner")) {
            this.entityData.set(OWNER_ID, Optional.of(tag.getUUID("Owner")));
        }
        this.entityData.set(ABILITY_ID, tag.getString("AbilityId"));
        this.entityData.set(ORIGIN_X, tag.getFloat("OriginX"));
        this.entityData.set(ORIGIN_Y, tag.getFloat("OriginY"));
        this.entityData.set(ORIGIN_Z, tag.getFloat("OriginZ"));
        this.lifeTicks = tag.getInt("LifeTicks");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        this.entityData.get(OWNER_ID).ifPresent(uuid -> tag.putUUID("Owner", uuid));
        tag.putString("AbilityId", this.getAbilityId());
        tag.putFloat("OriginX", this.entityData.get(ORIGIN_X));
        tag.putFloat("OriginY", this.entityData.get(ORIGIN_Y));
        tag.putFloat("OriginZ", this.entityData.get(ORIGIN_Z));
        tag.putInt("LifeTicks", this.lifeTicks);
    }

    @Override
    public void tick() {
        super.tick();
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        ServerPlayer owner = getOwner();
        if (owner == null || !owner.isAlive()) {
            this.discard();
            return;
        }

        Vec3 movement = this.getDeltaMovement();
        Vec3 direction = movement.lengthSqr() > 1.0E-5D ? movement.normalize() : owner.getLookAngle().normalize();
        Vec3 chainStart = resolveChainStart(owner, direction);
        this.setOrigin(chainStart);
        Vec3 start = this.position();
        Vec3 end = start.add(movement);

        BlockHitResult blockHit = this.level().clip(new ClipContext(chainStart, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
        EntityHitResult entityHit = findEntityHit(chainStart, end, owner);
        if (entityHit != null && (blockHit.getType() == HitResult.Type.MISS || chainStart.distanceToSqr(entityHit.getLocation()) <= chainStart.distanceToSqr(blockHit.getLocation()))) {
            onEntityHit(owner, entityHit);
            return;
        }
        if (blockHit.getType() != HitResult.Type.MISS) {
            this.discard();
            return;
        }

        this.setPos(end.x, end.y, end.z);
        Vec3 side = movement.lengthSqr() > 1.0E-5D
                ? new Vec3(-movement.z, 0.0D, movement.x).normalize().scale(0.14D)
                : Vec3.ZERO;
        Vec3 mid = chainStart.lerp(end, 0.5D);
        if ("smokey_chain".equals(this.getAbilityId())) {
            serverLevel.sendParticles(HunterParticles.SMOKY_CHAIN_SMOKE.get(), this.getX(), this.getY(), this.getZ(), 12, 0.11D, 0.11D, 0.11D, 0.016D);
            serverLevel.sendParticles(HunterParticles.SMOKY_CHAIN_SMOKE.get(), mid.x + side.x, mid.y, mid.z + side.z, 8, 0.06D, 0.06D, 0.06D, 0.01D);
            serverLevel.sendParticles(HunterParticles.SMOKY_CHAIN_SMOKE.get(), mid.x - side.x, mid.y, mid.z - side.z, 8, 0.06D, 0.06D, 0.06D, 0.01D);
        }
        if (--this.lifeTicks <= 0) {
            this.discard();
        }
    }

    private EntityHitResult findEntityHit(Vec3 start, Vec3 end, ServerPlayer owner) {
        AABB searchBox = new AABB(start, end).inflate(0.75D);
        return this.level().getEntitiesOfClass(LivingEntity.class, searchBox, target -> target.isAlive() && target != owner)
                .stream()
                .map(target -> {
                    Vec3 clip = target.getBoundingBox().inflate(0.3D).clip(start, end).orElse(null);
                    return clip == null ? null : new EntityHitResult(target, clip);
                })
                .filter(result -> result != null)
                .min(Comparator.comparingDouble(result -> start.distanceToSqr(result.getLocation())))
                .orElse(null);
    }

    private static Vec3 resolveChainStart(Player owner, Vec3 launchDirection) {
        double bodyYaw = Math.toRadians(owner.yBodyRot);
        double rightX = -Math.cos(bodyYaw) * 0.36D;
        double rightZ = -Math.sin(bodyYaw) * 0.36D;
        return owner.position().add(
                rightX + (launchDirection.x * 0.22D),
                1.05D + (owner.isCrouching() ? -0.16D : 0.0D),
                rightZ + (launchDirection.z * 0.22D)
        );
    }

    private void onEntityHit(ServerPlayer owner, EntityHitResult hitResult) {
        if (!(hitResult.getEntity() instanceof LivingEntity target)) {
            this.discard();
            return;
        }
        HunterPlayerData data = HunterDataUtil.get(owner);
        switch (this.getAbilityId()) {
            case "smokey_chain" -> {
                SmokeyChainAbility ability = HunterAbilities.SMOKEY_CHAIN;
                ability.attachTarget(owner, data, target);
            }
            case "dowsing_chain" -> HunterAbilities.DOWSING_CHAIN.attachTarget(owner, data, target);
            case "chain_jail" -> {
                if (applyChainJail(owner, target)) {
                    HunterAbilities.CHAIN_JAIL.attachTarget(owner, data, target);
                }
            }
            case "steal_chain" -> applyStealChain(owner, target);
            case "judgment_chain" -> applyJudgmentChain(owner, data, target);
            default -> target.hurt(owner.damageSources().playerAttack(owner), 4.0F + NenTechniqueAbility.getPassiveRenDamageBonus(data));
        }
        target.hurtMarked = true;
        this.discard();
    }

    private boolean applyChainJail(ServerPlayer owner, LivingEntity target) {
        if (target instanceof ServerPlayer targetPlayer) {
            targetPlayer.sendSystemMessage(net.minecraft.network.chat.Component.literal("Chain Jail locked you in place. Time the bar with Space to break free."));
        }
        if (!target.hurt(HunterDamageSources.nen(owner.level(), owner), 6.0F + NenTechniqueAbility.getPassiveRenDamageBonus(HunterDataUtil.get(owner)))) {
            return false;
        }
        if (owner.level() instanceof ServerLevel level) {
            ChainWrapVisualEntity.spawn(level, owner, target, 20 * 60 * 30);
        }
        return true;
    }

    private void applyStealChain(ServerPlayer owner, LivingEntity target) {
        if (!(target instanceof ServerPlayer targetPlayer)) {
            target.hurt(HunterDamageSources.nen(owner.level(), owner), 5.0F + NenTechniqueAbility.getPassiveRenDamageBonus(HunterDataUtil.get(owner)));
            return;
        }
        if (!target.hurt(HunterDamageSources.nen(owner.level(), owner), 4.0F + NenTechniqueAbility.getPassiveRenDamageBonus(HunterDataUtil.get(owner)))) {
            return;
        }
        HunterPlayerData targetData = HunterDataUtil.getOptional(targetPlayer).orElse(null);
        if (targetData != null) {
            List<SkillTreeCombatAbility> candidates = HunterAbilities.unlockedCombatAbilities(targetData).stream()
                    .filter(HunterAbilities.SKILL_TREE_COMBAT_ABILITIES::contains)
                    .filter(ability -> ability.hasSourceType(AbilitySourceType.NEN))
                    .filter(ability -> !"judgment_chain".equals(ability.id()))
                    .filter(ability -> targetData.getAbilityCooldown(ability.id()) <= 0)
                    .toList();
            if (!candidates.isEmpty()) {
                SkillTreeCombatAbility disabled = candidates.get(owner.getRandom().nextInt(candidates.size()));
                targetData.setAbilityCooldown(disabled.id(), 20 * 10);
                targetPlayer.sendSystemMessage(net.minecraft.network.chat.Component.literal("Steal Chain disabled " + disabled.displayName() + " for 10 seconds."));
            }
            HunterDataUtil.sync(targetPlayer);
        }
    }

    private void applyJudgmentChain(ServerPlayer owner, HunterPlayerData ownerData, LivingEntity target) {
        if (!(target instanceof ServerPlayer targetPlayer)) {
            owner.sendSystemMessage(net.minecraft.network.chat.Component.literal("Judgment Chain only works on players."));
            target.hurt(HunterDamageSources.nen(owner.level(), owner), 2.0F + NenTechniqueAbility.getPassiveRenDamageBonus(ownerData));
            return;
        }
        if (!NenVowUtil.matchesRequiredVow(owner, ownerData, "judgment_chain", targetPlayer)) {
            owner.sendSystemMessage(net.minecraft.network.chat.Component.literal("Judgment Chain only works on a target covered by your vow."));
            target.hurt(HunterDamageSources.nen(owner.level(), owner), 2.0F + NenTechniqueAbility.getPassiveRenDamageBonus(ownerData));
            return;
        }
        if (!target.hurt(HunterDamageSources.nen(owner.level(), owner), 5.0F + NenTechniqueAbility.getPassiveRenDamageBonus(ownerData))) {
            return;
        }
        ownerData.setPendingJudgmentChainTargetUuid(targetPlayer.getUUID().toString());
        HunterDataUtil.sync(owner);
        owner.sendSystemMessage(net.minecraft.network.chat.Component.literal("Judgment Chain hit. Open Nen Vows and choose a Hatsu ability to bind."));
    }

    public void setOwner(ServerPlayer owner) {
        this.entityData.set(OWNER_ID, Optional.of(owner.getUUID()));
    }

    public ServerPlayer getOwner() {
        return this.entityData.get(OWNER_ID)
                .map(uuid -> this.level().getPlayerByUUID(uuid) instanceof ServerPlayer player ? player : null)
                .orElse(null);
    }

    public Player getOwnerPlayer() {
        return this.entityData.get(OWNER_ID).map(this.level()::getPlayerByUUID).orElse(null);
    }

    public void setAbilityId(String abilityId) {
        this.entityData.set(ABILITY_ID, abilityId == null ? "" : abilityId);
    }

    public String getAbilityId() {
        return this.entityData.get(ABILITY_ID);
    }

    public int getLifeTicks() {
        return this.lifeTicks;
    }

    public void setOrigin(Vec3 origin) {
        this.entityData.set(ORIGIN_X, (float) origin.x);
        this.entityData.set(ORIGIN_Y, (float) origin.y);
        this.entityData.set(ORIGIN_Z, (float) origin.z);
    }

    public Vec3 getOrigin() {
        return new Vec3(this.entityData.get(ORIGIN_X), this.entityData.get(ORIGIN_Y), this.entityData.get(ORIGIN_Z));
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
