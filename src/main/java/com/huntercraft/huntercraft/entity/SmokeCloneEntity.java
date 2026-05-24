package com.huntercraft.huntercraft.entity;

import com.huntercraft.huntercraft.abilities.HunterAbilities;
import com.huntercraft.huntercraft.abilities.SkillTreeCombatAbility;
import com.huntercraft.huntercraft.abilities.nenability.NenTechniqueAbility;
import com.huntercraft.huntercraft.data.HunterPlayerData;
import com.huntercraft.huntercraft.faction.FactionUtil;
import com.huntercraft.huntercraft.item.HunterItems;
import com.huntercraft.huntercraft.particle.HunterParticles;
import com.huntercraft.huntercraft.util.HunterDataUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class SmokeCloneEntity extends PathfinderMob {
    public static final int NEN_UPKEEP_COST = 500;
    private static final int NEN_UPKEEP_INTERVAL = 40;
    private static final EntityDataAccessor<String> OWNER_UUID_DATA = SynchedEntityData.defineId(SmokeCloneEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> SOURCE_UUID_DATA = SynchedEntityData.defineId(SmokeCloneEntity.class, EntityDataSerializers.STRING);
    private static final double FOLLOW_OWNER_RANGE_SQR = 81.0D;
    private static final double DASH_RANGE_MIN_SQR = 49.0D;
    private static final double DASH_RANGE_MAX_SQR = 196.0D;
    private static final double SEARCH_RANGE = 30.0D;
    private static final double OWNER_DEFENSE_RANGE = 24.0D;

    private final HunterPlayerData cloneData = new HunterPlayerData();
    private UUID ownerUuid;
    private UUID sourceUuid;
    private int upkeepTicks;
    private int dashCooldown;
    private int jumpCooldown;
    private int abilityCooldown;
    private int guardTicks;
    private int nenStateSwapTicks;
    private int renPulseTicks;
    private int enPulseTicks;
    private int koStrikeTicks;
    private int ryuBurstTicks;
    private int spentNen;
    private boolean refundedNen;

    public SmokeCloneEntity(EntityType<? extends SmokeCloneEntity> type, Level level) {
        super(type, level);
        this.xpReward = 0;
        this.setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 18.0D)
                .add(Attributes.ARMOR, 2.0D)
                .add(Attributes.ATTACK_DAMAGE, 5.0D)
                .add(Attributes.ATTACK_SPEED, 1.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.26D)
                .add(Attributes.FOLLOW_RANGE, 40.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.12D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new LookAtPlayerGoal(this, Player.class, 12.0F));
        this.goalSelector.addGoal(2, new RandomLookAroundGoal(this));
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(OWNER_UUID_DATA, "");
        this.entityData.define(SOURCE_UUID_DATA, "");
    }

    public void setOwner(ServerPlayer owner) {
        this.ownerUuid = owner.getUUID();
        this.entityData.set(OWNER_UUID_DATA, owner.getStringUUID());
    }

    public void setCloneSource(ServerPlayer source) {
        this.sourceUuid = source.getUUID();
        this.entityData.set(SOURCE_UUID_DATA, source.getStringUUID());
        copySourceSnapshot(source);
    }

    @Nullable
    public UUID getOwnerUuid() {
        if (this.ownerUuid != null) {
            return this.ownerUuid;
        }
        return parseUuid(this.entityData.get(OWNER_UUID_DATA));
    }

    @Nullable
    public UUID getSourceUuid() {
        if (this.sourceUuid != null) {
            return this.sourceUuid;
        }
        return parseUuid(this.entityData.get(SOURCE_UUID_DATA));
    }

    @Nullable
    public ServerPlayer getOwnerPlayer() {
        UUID uuid = this.getOwnerUuid();
        if (uuid == null || !(this.level() instanceof ServerLevel serverLevel)) {
            return null;
        }
        return serverLevel.getServer().getPlayerList().getPlayer(uuid);
    }

    @Nullable
    public ServerPlayer getSourcePlayer() {
        UUID uuid = this.getSourceUuid();
        if (uuid == null || !(this.level() instanceof ServerLevel serverLevel)) {
            return null;
        }
        return serverLevel.getServer().getPlayerList().getPlayer(uuid);
    }

    public HunterPlayerData getCloneNenData() {
        return this.cloneData;
    }

    @Override
    public void tick() {
        super.tick();
        this.fallDistance = 0.0F;
        if (this.level().isClientSide()) {
            return;
        }

        this.upkeepTicks++;
        this.dashCooldown = Math.max(0, this.dashCooldown - 1);
        this.jumpCooldown = Math.max(0, this.jumpCooldown - 1);
        this.abilityCooldown = Math.max(0, this.abilityCooldown - 1);
        this.guardTicks = Math.max(0, this.guardTicks - 1);
        this.nenStateSwapTicks = Math.max(0, this.nenStateSwapTicks - 1);
        this.renPulseTicks = Math.max(0, this.renPulseTicks - 1);
        this.enPulseTicks = Math.max(0, this.enPulseTicks - 1);
        this.koStrikeTicks = Math.max(0, this.koStrikeTicks - 1);
        this.ryuBurstTicks = Math.max(0, this.ryuBurstTicks - 1);

        ServerPlayer owner = this.getOwnerPlayer();
        if (owner == null || !owner.isAlive()) {
            this.discard();
            return;
        }

        HunterPlayerData ownerData = HunterDataUtil.getOptional(owner).orElse(null);
        if (ownerData == null || !ownerData.hasDeepPurpleTechnique()) {
            this.discard();
            return;
        }

        if ((this.upkeepTicks % NEN_UPKEEP_INTERVAL) == 0) {
            if (!ownerData.consumeNen(NEN_UPKEEP_COST)) {
                this.discard();
                HunterDataUtil.sync(owner);
                return;
            }
            this.spentNen += NEN_UPKEEP_COST;
        }

        ServerPlayer source = this.getSourcePlayer();
        if (source == null || !source.isAlive()) {
            source = owner;
        }
        if (source != null && (this.tickCount == 1 || (this.tickCount % 20) == 0)) {
            copySourceSnapshot(source);
        }

        spawnBodySmoke((ServerLevel) this.level());

        LivingEntity target = chooseTarget(owner, source);
        if (target == null) {
            this.setTarget(null);
            refreshNenStates();
            followOwner(owner);
            return;
        }

        this.setTarget(target);
        refreshNenStates();
        tryGuard(target);
        tryDash(target);
        tryDoubleJump(target);
        tryUseCopiedAbility(owner, ownerData, target);

        double moveSpeed = isEnActive() ? 1.45D : 1.22D;
        this.getNavigation().moveTo(target, moveSpeed);
        this.lookAt(target, 30.0F, 30.0F);
        if (this.distanceToSqr(target) <= 5.0D) {
            this.doHurtTarget(target);
        }
    }

    private void followOwner(ServerPlayer owner) {
        double distanceSqr = this.distanceToSqr(owner);
        if (distanceSqr <= FOLLOW_OWNER_RANGE_SQR) {
            this.getNavigation().stop();
            this.lookAt(owner, 20.0F, 20.0F);
            return;
        }
        this.getNavigation().moveTo(owner, 1.2D);
        this.lookAt(owner, 20.0F, 20.0F);
    }

    @Nullable
    private LivingEntity chooseTarget(ServerPlayer owner, ServerPlayer source) {
        LivingEntity defendTarget = firstValid(owner, owner.getLastHurtByMob(), source.getLastHurtByMob(), owner.getLastHurtMob(), source.getLastHurtMob());
        if (defendTarget != null && this.distanceToSqr(defendTarget) <= (OWNER_DEFENSE_RANGE * OWNER_DEFENSE_RANGE)) {
            return defendTarget;
        }
        LivingEntity currentTarget = this.getTarget();
        if (isValidCombatTarget(owner, currentTarget)) {
            return currentTarget;
        }
        AABB searchBox = this.getBoundingBox().inflate(SEARCH_RANGE, 10.0D, SEARCH_RANGE);
        List<LivingEntity> targets = this.level().getEntitiesOfClass(LivingEntity.class, searchBox, entity ->
                entity.isAlive()
                        && entity != this
                        && entity != owner
                        && entity != source
                        && isSearchTarget(owner, entity));
        return targets.stream()
                .filter(this::hasLineOfSight)
                .min(Comparator.comparingDouble(this::distanceToSqr))
                .orElse(null);
    }

    private boolean isSearchTarget(ServerPlayer owner, LivingEntity entity) {
        if (entity instanceof Player player) {
            return !(player instanceof net.minecraft.server.level.ServerPlayer serverPlayer && serverPlayer.isSpectator())
                    && !FactionUtil.areFactionMates(owner, player);
        }
        return entity instanceof Monster;
    }

    @SafeVarargs
    private final LivingEntity firstValid(ServerPlayer owner, LivingEntity... targets) {
        for (LivingEntity target : targets) {
            if (isValidCombatTarget(owner, target)) {
                return target;
            }
        }
        return null;
    }

    private boolean isValidCombatTarget(ServerPlayer owner, @Nullable LivingEntity target) {
        if (target == null || !target.isAlive() || target == this || target == owner) {
            return false;
        }
        if (target instanceof Player player && FactionUtil.areFactionMates(owner, player)) {
            return false;
        }
        return true;
    }

    private void tryGuard(LivingEntity target) {
        if (this.guardTicks > 0) {
            this.cloneData.setGuarding(true);
            this.cloneData.tickGuard();
            return;
        }
        this.cloneData.setGuarding(false);
        if (this.distanceToSqr(target) <= 9.0D && this.random.nextFloat() < 0.03F) {
            this.guardTicks = 14;
            this.cloneData.setGuarding(true);
        }
    }

    private void tryDash(LivingEntity target) {
        if (this.dashCooldown > 0 || !this.onGround()) {
            return;
        }
        double distanceSqr = this.distanceToSqr(target);
        if (distanceSqr < DASH_RANGE_MIN_SQR || distanceSqr > DASH_RANGE_MAX_SQR) {
            return;
        }
        Vec3 direction = target.position().subtract(this.position()).normalize();
        if (!Double.isFinite(direction.x) || !Double.isFinite(direction.z)) {
            return;
        }
        this.setDeltaMovement(direction.x * 1.25D, 0.24D, direction.z * 1.25D);
        this.hasImpulse = true;
        this.dashCooldown = 42;
        this.playSound(SoundEvents.PLAYER_ATTACK_SWEEP, 0.55F, 0.8F);
    }

    private void tryDoubleJump(LivingEntity target) {
        if (this.jumpCooldown > 0 || this.onGround()) {
            return;
        }
        if (target.getY() <= this.getY() + 1.2D || this.getDeltaMovement().y > 0.08D) {
            return;
        }
        Vec3 direction = target.position().subtract(this.position()).normalize();
        this.setDeltaMovement(this.getDeltaMovement().x * 0.4D + (direction.x * 0.45D), 0.58D, this.getDeltaMovement().z * 0.4D + (direction.z * 0.45D));
        this.hasImpulse = true;
        this.jumpCooldown = 22;
        this.playSound(SoundEvents.BLAZE_SHOOT, 0.25F, 1.35F);
    }

    private void tryUseCopiedAbility(ServerPlayer owner, HunterPlayerData ownerData, LivingEntity target) {
        if (this.abilityCooldown > 0) {
            return;
        }
        List<String> copiedAbilities = getCopiedCombatAbilities();
        if (copiedAbilities.isEmpty()) {
            return;
        }
        String abilityId = copiedAbilities.get(this.random.nextInt(copiedAbilities.size()));
        if ("smoke_clone".equals(abilityId)) {
            return;
        }
        if ("nen_zetsu".equals(abilityId) || "nen_ken".equals(abilityId)) {
            return;
        }
        if ("smoke_soldier".equals(abilityId)) {
            trySummonSmokeSoldier(owner, ownerData, target);
            return;
        }
        if ("smokey_chain".equals(abilityId)) {
            trySmokeyChain(target);
            return;
        }

        BanditAbilityProfile profile = findBanditProfile(abilityId);
        if (profile == null) {
            return;
        }
        performBanditStyleAbility(target, profile);
    }

    private void trySummonSmokeSoldier(ServerPlayer owner, HunterPlayerData ownerData, LivingEntity target) {
        if (ownerData.getLungCapacity() < SmokeSoldierEntity.SMOKE_COST || !(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        if (!ownerData.consumeLungCapacity(SmokeSoldierEntity.SMOKE_COST)) {
            return;
        }
        SmokeSoldierEntity soldier = HunterEntityTypes.SMOKE_SOLDIER.get().create(serverLevel);
        if (soldier == null) {
            ownerData.addLungCapacity(SmokeSoldierEntity.SMOKE_COST);
            return;
        }
        Vec3 towardTarget = target.position().subtract(this.position()).normalize();
        soldier.setOwner(owner);
        soldier.copyOwnerSnapshot(ownerData);
        soldier.setScoutAngleDegrees((float) Math.toDegrees(Math.atan2(towardTarget.z, towardTarget.x)));
        soldier.moveTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), 0.0F);
        serverLevel.addFreshEntity(soldier);
        serverLevel.sendParticles(HunterParticles.MOREL_SMOKE.get(), this.getX(), this.getY(0.95D), this.getZ(), 12, 0.16D, 0.25D, 0.16D, 0.018D);
        this.abilityCooldown = 70;
        HunterDataUtil.sync(owner);
    }

    private void trySmokeyChain(LivingEntity target) {
        if (!isSmokingPipe(this.getMainHandItem()) && !isSmokingPipe(this.getOffhandItem())) {
            return;
        }
        if (this.distanceToSqr(target) > (14.0D * 14.0D) || !this.hasLineOfSight(target) || !(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        Vec3 anchor = this.position().add(0.0D, this.getBbHeight() * 0.78D, 0.0D);
        Vec3 targetCenter = target.position().add(0.0D, target.getBbHeight() * 0.55D, 0.0D);
        Vec3 towardAnchor = anchor.subtract(targetCenter);
        if (towardAnchor.lengthSqr() > 1.0E-4D) {
            Vec3 pull = towardAnchor.normalize().scale(0.52D);
            target.setDeltaMovement(target.getDeltaMovement().scale(0.55D).add(pull.x, pull.y * 0.2D, pull.z));
            target.hurtMarked = true;
            target.hasImpulse = true;
        }
        spawnChainParticles(serverLevel, anchor, targetCenter);
        this.abilityCooldown = 55;
    }

    private void performBanditStyleAbility(LivingEntity target, BanditAbilityProfile profile) {
        Vec3 direction = target.position().subtract(this.position()).normalize();
        if (!Double.isFinite(direction.x) || !Double.isFinite(direction.z)) {
            return;
        }

        this.lookAt(target, 30.0F, 30.0F);
        this.setDeltaMovement(direction.x * profile.lungeStrength(), Math.max(this.getDeltaMovement().y, profile.launchStrength()), direction.z * profile.lungeStrength());
        this.hasImpulse = true;
        this.abilityCooldown = profile.cooldownTicks();

        if (profile.areaRadius() > 0.0F) {
            AABB box = this.getBoundingBox().inflate(profile.areaRadius());
            List<LivingEntity> targets = this.level().getEntitiesOfClass(LivingEntity.class, box, entity -> entity != this && entity.isAlive());
            ServerPlayer owner = this.getOwnerPlayer();
            for (LivingEntity nearby : targets) {
                if (!this.hasLineOfSight(nearby) || (owner != null && !isValidCombatTarget(owner, nearby))) {
                    continue;
                }
                applyProfileHit(nearby, profile);
            }
        } else {
            applyProfileHit(target, profile);
        }
    }

    private void applyProfileHit(LivingEntity target, BanditAbilityProfile profile) {
        float damage = profile.baseDamage() + (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE);
        damage += NenTechniqueAbility.getOutgoingDamageBonusTotal(this.cloneData);
        if (isKoActive()) {
            damage *= 1.35F;
            this.koStrikeTicks = 0;
            this.cloneData.setKoActive(false);
        }
        if (!target.hurt(this.damageSources().mobAttack(this), damage)) {
            return;
        }
        Vec3 knockbackDir = target.position().subtract(this.position());
        if (knockbackDir.lengthSqr() > 1.0E-4D) {
            knockbackDir = knockbackDir.normalize();
            target.push(knockbackDir.x * profile.knockbackStrength(), profile.launchStrength(), knockbackDir.z * profile.knockbackStrength());
        } else if (profile.launchStrength() > 0.0F) {
            target.push(0.0D, profile.launchStrength(), 0.0D);
        }
        if (isRyuActive()) {
            target.push(0.0D, 0.08D, 0.0D);
        }
        this.playSound(profile.requiresWeapon() ? SoundEvents.PLAYER_ATTACK_SWEEP : SoundEvents.PLAYER_ATTACK_STRONG, 0.65F, 0.9F + (this.random.nextFloat() * 0.08F));
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        if (!(target instanceof LivingEntity living)) {
            return super.doHurtTarget(target);
        }
        float damage = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE) + NenTechniqueAbility.getOutgoingDamageBonusTotal(this.cloneData);
        if (isKoActive()) {
            damage *= 1.4F;
            this.koStrikeTicks = 0;
            this.cloneData.setKoActive(false);
        }
        if (isRyuActive()) {
            damage += 2.0F;
        }
        boolean hurt = living.hurt(this.damageSources().mobAttack(this), damage);
        if (hurt) {
            Vec3 pushDir = living.position().subtract(this.position()).normalize();
            if (Double.isFinite(pushDir.x) && Double.isFinite(pushDir.z)) {
                living.push(pushDir.x * 0.2D, isRyuActive() ? 0.12D : 0.04D, pushDir.z * 0.2D);
            }
            this.playSound(SoundEvents.PLAYER_ATTACK_STRONG, 0.55F, 0.95F);
        }
        return hurt;
    }

    @Override
    public void die(DamageSource damageSource) {
        if (!this.level().isClientSide()) {
            net.minecraft.world.entity.Entity killer = damageSource.getEntity();
            ServerPlayer owner = this.getOwnerPlayer();
            if (owner != null && killer != owner) {
                HunterPlayerData data = HunterDataUtil.getOptional(owner).orElse(null);
                if (data != null) {
                    data.addNen(NEN_UPKEEP_COST / 2);
                    HunterDataUtil.sync(owner);
                }
            }
        }
        super.die(damageSource);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        Entity attacker = source.getEntity();
        ServerPlayer owner = this.getOwnerPlayer();
        if (owner != null && attacker == owner) {
            if (!this.level().isClientSide()) {
                refundNen(owner, this.spentNen);
                this.discard();
            }
            return true;
        }
        if (owner != null && attacker instanceof Player player && FactionUtil.areFactionMates(owner, player)) {
            return false;
        }
        if (this.guardTicks > 0) {
            amount *= 0.25F;
        }
        amount *= (1.0F - NenTechniqueAbility.getIncomingReduction(this.cloneData));
        boolean hurt = super.hurt(source, amount);
        if (hurt && attacker instanceof LivingEntity living && owner != null && isValidCombatTarget(owner, living)) {
            this.setTarget(living);
            this.guardTicks = Math.max(this.guardTicks, 10);
        }
        return hurt;
    }

    @Override
    public boolean canBeAffected(MobEffectInstance effectInstance) {
        return false;
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.FIRE_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.FIRE_EXTINGUISH;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.FIRE_EXTINGUISH;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState block) {
        this.playSound(SoundEvents.WOOL_STEP, 0.1F, 1.0F);
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (this.ownerUuid != null) {
            tag.putUUID("OwnerUuid", this.ownerUuid);
        }
        if (this.sourceUuid != null) {
            tag.putUUID("SourceUuid", this.sourceUuid);
        }
        tag.putInt("UpkeepTicks", this.upkeepTicks);
        tag.putInt("DashCooldown", this.dashCooldown);
        tag.putInt("JumpCooldown", this.jumpCooldown);
        tag.putInt("AbilityCooldown", this.abilityCooldown);
        tag.putInt("GuardTicks", this.guardTicks);
        tag.putInt("SpentNen", this.spentNen);
        tag.putBoolean("RefundedNen", this.refundedNen);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.ownerUuid = tag.hasUUID("OwnerUuid") ? tag.getUUID("OwnerUuid") : null;
        this.sourceUuid = tag.hasUUID("SourceUuid") ? tag.getUUID("SourceUuid") : null;
        this.entityData.set(OWNER_UUID_DATA, this.ownerUuid != null ? this.ownerUuid.toString() : "");
        this.entityData.set(SOURCE_UUID_DATA, this.sourceUuid != null ? this.sourceUuid.toString() : "");
        this.upkeepTicks = tag.getInt("UpkeepTicks");
        this.dashCooldown = tag.getInt("DashCooldown");
        this.jumpCooldown = tag.getInt("JumpCooldown");
        this.abilityCooldown = tag.getInt("AbilityCooldown");
        this.guardTicks = tag.getInt("GuardTicks");
        this.spentNen = tag.getInt("SpentNen");
        this.refundedNen = tag.getBoolean("RefundedNen");
    }

    private void copySourceSnapshot(ServerPlayer source) {
        ServerPlayer owner = this.getOwnerPlayer();
        HunterPlayerData abilitySourceData = owner == null ? HunterDataUtil.getOptional(source).orElse(null) : HunterDataUtil.getOptional(owner).orElse(null);
        if (abilitySourceData == null) {
            return;
        }
        this.cloneData.copyFrom(abilitySourceData);
        this.cloneData.setCurrentNen(this.cloneData.getMaxNen());
        configureDeepPurpleNenStates(this.cloneData);
        this.cloneData.setGuarding(false);
        this.cloneData.clearActiveAbility();
        this.cloneData.clearChargingAbility();
        this.cloneData.clearMartialArtsGrab();
        this.cloneData.clearDeepPurplePing();
        copyEquipment(source);
    }

    private void copyEquipment(ServerPlayer source) {
        this.setItemSlot(EquipmentSlot.MAINHAND, copyOne(source.getMainHandItem()));
        this.setItemSlot(EquipmentSlot.OFFHAND, copyOne(source.getOffhandItem()));
        this.setItemSlot(EquipmentSlot.HEAD, copyOne(source.getItemBySlot(EquipmentSlot.HEAD)));
        this.setItemSlot(EquipmentSlot.CHEST, copyOne(source.getItemBySlot(EquipmentSlot.CHEST)));
        this.setItemSlot(EquipmentSlot.LEGS, copyOne(source.getItemBySlot(EquipmentSlot.LEGS)));
        this.setItemSlot(EquipmentSlot.FEET, copyOne(source.getItemBySlot(EquipmentSlot.FEET)));
    }

    private ItemStack copyOne(ItemStack stack) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack copy = stack.copy();
        copy.setCount(1);
        return copy;
    }

    private void refreshNenStates() {
        this.cloneData.setCurrentNen(this.cloneData.getMaxNen());
        configureDeepPurpleNenStates(this.cloneData);
        boolean hasTarget = this.getTarget() != null && this.getTarget().isAlive();
        this.cloneData.setZetsuActive(!hasTarget && this.cloneData.hasZetsuUnlocked());
        this.cloneData.setKenActive(hasTarget && this.cloneData.hasKenUnlocked());
    }

    private boolean isEnActive() {
        return false;
    }

    private boolean isKoActive() {
        return false;
    }

    private boolean isRyuActive() {
        return false;
    }

    private List<String> getCopiedCombatAbilities() {
        Set<String> ordered = new LinkedHashSet<>();
        for (int bar = 0; bar < HunterPlayerData.COMBAT_BAR_COUNT; bar++) {
            for (int slot = 0; slot < HunterPlayerData.COMBAT_SLOT_COUNT; slot++) {
                String abilityId = this.cloneData.getCombatSlot(bar, slot);
                if (isCopiedCombatAbilityAllowed(abilityId)) {
                    ordered.add(abilityId);
                }
            }
        }
        for (SkillTreeCombatAbility ability : HunterAbilities.SKILL_TREE_COMBAT_ABILITIES) {
            if (ability.isUnlocked(this.cloneData) && isCopiedCombatAbilityAllowed(ability.id())) {
                ordered.add(ability.id());
            }
        }
        return new ArrayList<>(ordered);
    }

    private static boolean isCopiedCombatAbilityAllowed(String abilityId) {
        return abilityId != null
                && !abilityId.isBlank()
                && !"smoke_clone".equals(abilityId)
                && !"nen_zetsu".equals(abilityId)
                && !"nen_ken".equals(abilityId)
                && !"nen_ten".equals(abilityId)
                && !"nen_ren".equals(abilityId)
                && !"nen_en".equals(abilityId)
                && !"nen_ko".equals(abilityId)
                && !"nen_ryu".equals(abilityId);
    }

    private static void configureDeepPurpleNenStates(HunterPlayerData data) {
        boolean zetsuUnlocked = data.hasZetsuUnlocked();
        boolean kenUnlocked = data.hasKenUnlocked();
        data.setTenUnlocked(false);
        data.setRenUnlocked(false);
        data.setEnUnlocked(false);
        data.setKoUnlocked(false);
        data.setRyuUnlocked(false);
        data.disableAllNen();
        data.setZetsuUnlocked(zetsuUnlocked);
        data.setKenUnlocked(kenUnlocked);
    }

    @Nullable
    private BanditAbilityProfile findBanditProfile(String abilityId) {
        for (BanditAbilityProfile profile : BanditAbilityProfile.values()) {
            if (profile.abilityId().equals(abilityId)) {
                return profile;
            }
        }
        return null;
    }

    private void spawnBodySmoke(ServerLevel level) {
        if ((this.tickCount % 5) != 0) {
            return;
        }
        level.sendParticles(HunterParticles.MOREL_SMOKE.get(), this.getX(), this.getY(0.82D), this.getZ(), 1, 0.05D, 0.08D, 0.05D, 0.004D);
        if (this.getDeltaMovement().horizontalDistanceSqr() > 0.02D) {
            level.sendParticles(HunterParticles.MOREL_SMOKE.get(), this.getX(), this.getY(0.22D), this.getZ(), 1, 0.06D, 0.03D, 0.06D, 0.006D);
        }
    }

    private void spawnChainParticles(ServerLevel level, Vec3 start, Vec3 end) {
        Vec3 delta = end.subtract(start);
        int segments = Math.max(10, (int) Math.ceil(delta.length() * 1.2D));
        Vec3 step = delta.scale(1.0D / segments);
        for (int i = 0; i <= segments; i++) {
            Vec3 point = start.add(step.scale(i));
            level.sendParticles(HunterParticles.MOREL_SMOKE.get(), point.x, point.y, point.z, 2, 0.03D, 0.03D, 0.03D, 0.01D);
        }
    }

    private static boolean isSmokingPipe(ItemStack stack) {
        return !stack.isEmpty() && stack.is(HunterItems.SMOKING_PIPE.get());
    }

    private void refundNen(ServerPlayer owner, int amount) {
        if (this.refundedNen || amount <= 0) {
            return;
        }
        HunterPlayerData data = HunterDataUtil.getOptional(owner).orElse(null);
        if (data == null) {
            return;
        }
        data.addNen(amount);
        HunterDataUtil.sync(owner);
        this.refundedNen = true;
    }

    @Nullable
    private static UUID parseUuid(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }
}
