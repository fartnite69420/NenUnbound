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
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
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

public class SmokeSoldierEntity extends PathfinderMob {
    public static final int SMOKE_COST = 25;
    public static final int NEN_UPKEEP_COST = 250;
    private static final int NEN_UPKEEP_INTERVAL = 40;
    private static final EntityDataAccessor<String> OWNER_UUID_DATA = SynchedEntityData.defineId(SmokeSoldierEntity.class, EntityDataSerializers.STRING);
    private static final int PARTIAL_REFUND = 13;
    private static final int PING_TICKS = 80;
    private static final double SCOUT_RADIUS_MIN = 18.0D;
    private static final double SCOUT_RADIUS_MAX = 34.0D;
    private static final double SCOUT_PLAYER_RANGE = 22.0D;
    private static final double PROTECT_OWNER_RANGE = 24.0D;
    private static final double SCOUT_BREAKAWAY_RADIUS = 14.0D;

    private final HunterPlayerData soldierNenData = createSoldierNenData();
    private UUID ownerUuid;
    private float scoutAngleDegrees;
    private int scoutStep;
    private int retargetTicks;
    private int abilityCooldown;
    private int nenStateSwapTicks;
    private int renPulseTicks;
    private int enPulseTicks;
    private int koStrikeTicks;
    private int ryuBurstTicks;
    private Vec3 scoutTarget;
    private int spentNen;
    private boolean refundedNen;

    public SmokeSoldierEntity(EntityType<? extends SmokeSoldierEntity> type, Level level) {
        super(type, level);
        this.xpReward = 0;
        this.setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 12.0D)
                .add(Attributes.ATTACK_DAMAGE, 3.5D)
                .add(Attributes.MOVEMENT_SPEED, 0.26D)
                .add(Attributes.FOLLOW_RANGE, 36.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.15D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.0D, true));
        this.goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 18.0F));
        this.goalSelector.addGoal(3, new RandomLookAroundGoal(this));
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(OWNER_UUID_DATA, "");
    }

    public void setOwner(ServerPlayer owner) {
        this.ownerUuid = owner.getUUID();
        this.entityData.set(OWNER_UUID_DATA, owner.getStringUUID());
    }

    public void setScoutAngleDegrees(float scoutAngleDegrees) {
        this.scoutAngleDegrees = scoutAngleDegrees;
    }

    @Nullable
    public ServerPlayer getOwnerPlayer() {
        if (this.ownerUuid == null || !(this.level() instanceof ServerLevel serverLevel)) {
            return null;
        }
        return serverLevel.getServer().getPlayerList().getPlayer(this.ownerUuid);
    }

    @Nullable
    public UUID getOwnerUuid() {
        if (this.ownerUuid != null) {
            return this.ownerUuid;
        }
        String syncedOwnerUuid = this.entityData.get(OWNER_UUID_DATA);
        if (syncedOwnerUuid == null || syncedOwnerUuid.isBlank()) {
            return null;
        }
        try {
            this.ownerUuid = UUID.fromString(syncedOwnerUuid);
            return this.ownerUuid;
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    @Override
    public void tick() {
        super.tick();
        this.fallDistance = 0.0F;
        if (this.level().isClientSide()) {
            return;
        }

        this.nenStateSwapTicks = Math.max(0, this.nenStateSwapTicks - 1);
        this.renPulseTicks = Math.max(0, this.renPulseTicks - 1);
        this.enPulseTicks = Math.max(0, this.enPulseTicks - 1);
        this.koStrikeTicks = Math.max(0, this.koStrikeTicks - 1);
        this.ryuBurstTicks = Math.max(0, this.ryuBurstTicks - 1);
        this.abilityCooldown = Math.max(0, this.abilityCooldown - 1);

        ServerPlayer owner = this.getOwnerPlayer();
        if (owner == null || !owner.isAlive()) {
            this.discard();
            return;
        }

        spawnBodySmoke((ServerLevel) this.level());
        HunterPlayerData ownerData = HunterDataUtil.getOptional(owner).orElse(null);
        if (ownerData == null || !ownerData.hasDeepPurpleTechnique()) {
            this.discard();
            return;
        }
        if (this.tickCount == 1 || (this.tickCount % 20) == 0) {
            copyOwnerSnapshot(ownerData);
        }
        if ((this.tickCount % NEN_UPKEEP_INTERVAL) == 0 && !ownerData.consumeNen(NEN_UPKEEP_COST)) {
            this.discard();
            HunterDataUtil.sync(owner);
            return;
        }
        if ((this.tickCount % NEN_UPKEEP_INTERVAL) == 0) {
            this.spentNen += NEN_UPKEEP_COST;
        }

        if (this.retargetTicks > 0) {
            this.retargetTicks--;
        }

        LivingEntity protectTarget = getOwnerAggressor(owner);
        if (protectTarget != null) {
            this.setTarget(protectTarget);
        } else if (ownerData.getDeepPurpleSpottedTargetUuid().isBlank()) {
            this.setTarget(null);
        }

        if (this.getTarget() != null && !isValidCombatTarget(owner, this.getTarget())) {
            this.setTarget(null);
        }
        refreshNenStates(owner);

        if (ownerData.isDeepPurpleReturnMode()) {
            returnToOwner(owner);
            return;
        }

        if (protectTarget != null) {
            tryUseCopiedAbility(ownerData, protectTarget);
            moveTowardEntity(protectTarget, 1.55D);
            return;
        }

        LivingEntity spottedTarget = resolveSpottedTarget(owner, ownerData);
        if (spottedTarget != null) {
            this.setTarget(spottedTarget);
            tryUseCopiedAbility(ownerData, spottedTarget);
            moveTowardEntity(spottedTarget, 1.6D);
            return;
        }

        Player foundPlayer = findScoutTarget(owner);
        if (foundPlayer != null) {
            ownerData.setDeepPurplePing(foundPlayer.getStringUUID(), foundPlayer.blockPosition(), PING_TICKS);
            HunterDataUtil.sync(owner);
            this.setTarget(foundPlayer);
            tryUseCopiedAbility(ownerData, foundPlayer);
            moveTowardEntity(foundPlayer, 1.6D);
            return;
        }

        scoutAroundOwner(owner);
    }

    private void scoutAroundOwner(ServerPlayer owner) {
        double ownerDistanceSqr = this.distanceToSqr(owner);
        if (ownerDistanceSqr < (SCOUT_BREAKAWAY_RADIUS * SCOUT_BREAKAWAY_RADIUS)) {
            Vec3 outwardTarget = getScoutPoint(owner, this.scoutAngleDegrees, SCOUT_RADIUS_MAX);
            this.scoutTarget = outwardTarget;
            this.retargetTicks = 20;
            this.getNavigation().moveTo(outwardTarget.x, outwardTarget.y, outwardTarget.z, 1.65D);
            return;
        }

        if (this.retargetTicks > 0 && this.scoutTarget != null && this.position().distanceToSqr(this.scoutTarget) > 5.0D) {
            this.getNavigation().moveTo(this.scoutTarget.x, this.scoutTarget.y, this.scoutTarget.z, 1.55D);
            return;
        }

        this.retargetTicks = 40;
        float angleDegrees = this.scoutAngleDegrees + (this.scoutStep * 58.0F);
        this.scoutStep++;
        double radius = SCOUT_RADIUS_MIN + this.random.nextDouble() * (SCOUT_RADIUS_MAX - SCOUT_RADIUS_MIN);
        this.scoutTarget = getScoutPoint(owner, angleDegrees, radius);
        this.getNavigation().moveTo(this.scoutTarget.x, this.scoutTarget.y, this.scoutTarget.z, 1.55D);
    }

    private Vec3 getScoutPoint(ServerPlayer owner, float angleDegrees, double radius) {
        double angle = Math.toRadians(angleDegrees);
        int targetX = Mth.floor(owner.getX() + (Math.cos(angle) * radius));
        int targetZ = Mth.floor(owner.getZ() + (Math.sin(angle) * radius));
        BlockPos surface = this.level().getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, new BlockPos(targetX, owner.blockPosition().getY(), targetZ));
        return Vec3.atBottomCenterOf(surface.above());
    }

    private void returnToOwner(ServerPlayer owner) {
        this.setTarget(null);
        double distanceSqr = this.distanceToSqr(owner);
        if (distanceSqr <= 9.0D) {
            this.getNavigation().stop();
            this.lookAt(owner, 30.0F, 30.0F);
            return;
        }
        this.getNavigation().moveTo(owner, 1.75D);
        this.lookAt(owner, 30.0F, 30.0F);
    }

    private void moveTowardEntity(LivingEntity target, double speed) {
        this.getNavigation().moveTo(target, speed);
        this.lookAt(target, 30.0F, 30.0F);
    }

    @Nullable
    private LivingEntity resolveSpottedTarget(ServerPlayer owner, HunterPlayerData ownerData) {
        if (ownerData.getDeepPurpleSpottedTargetUuid().isBlank() || !(this.level() instanceof ServerLevel serverLevel)) {
            return null;
        }
        Entity entity;
        try {
            entity = serverLevel.getEntity(UUID.fromString(ownerData.getDeepPurpleSpottedTargetUuid()));
        } catch (IllegalArgumentException exception) {
            ownerData.clearDeepPurplePing();
            HunterDataUtil.sync(owner);
            return null;
        }
        if (!(entity instanceof LivingEntity living) || !isValidCombatTarget(owner, living)) {
            ownerData.clearDeepPurplePing();
            HunterDataUtil.sync(owner);
            return null;
        }
        ownerData.setDeepPurplePing(living.getStringUUID(), living.blockPosition(), PING_TICKS);
        return living;
    }

    @Nullable
    private Player findScoutTarget(ServerPlayer owner) {
        AABB searchBox = this.getBoundingBox().inflate(SCOUT_PLAYER_RANGE, 8.0D, SCOUT_PLAYER_RANGE);
        List<Player> players = this.level().getEntitiesOfClass(Player.class, searchBox, player ->
                player != owner
                        && player.isAlive()
                        && !player.isSpectator()
                        && !FactionUtil.areFactionMates(owner, player)
                        && this.hasLineOfSight(player));
        return players.stream()
                .min(Comparator.comparingDouble(this::distanceToSqr))
                .orElse(null);
    }

    @Nullable
    private LivingEntity getOwnerAggressor(ServerPlayer owner) {
        LivingEntity aggressor = owner.getLastHurtByMob();
        if (aggressor == null || !isValidCombatTarget(owner, aggressor) || this.distanceToSqr(aggressor) > (PROTECT_OWNER_RANGE * PROTECT_OWNER_RANGE)) {
            return null;
        }
        return aggressor;
    }

    private boolean isValidCombatTarget(ServerPlayer owner, LivingEntity target) {
        if (target == null || !target.isAlive() || target == this || target == owner) {
            return false;
        }
        if (target instanceof Player player && FactionUtil.areFactionMates(owner, player)) {
            return false;
        }
        return true;
    }

    private void spawnBodySmoke(ServerLevel level) {
        if ((this.tickCount % 4) != 0) {
            return;
        }
        level.sendParticles(HunterParticles.MOREL_SMOKE.get(), this.getX(), this.getY(0.85D), this.getZ(), 1, 0.07D, 0.12D, 0.07D, 0.006D);
        if (this.getDeltaMovement().horizontalDistanceSqr() > 0.01D) {
            level.sendParticles(HunterParticles.MOREL_SMOKE.get(), this.getX(), this.getY(0.28D), this.getZ(), 1, 0.08D, 0.03D, 0.08D, 0.008D);
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        Entity attacker = source.getEntity();
        Entity directAttacker = source.getDirectEntity();
        ServerPlayer owner = this.getOwnerPlayer();
        if (owner != null && (attacker == owner || directAttacker == owner)) {
            if (!this.level().isClientSide()) {
                refundNen(owner, this.spentNen);
                refundSmoke(owner, SMOKE_COST);
                this.discard();
            }
            return true;
        }
        if (attacker instanceof Player player && !canPlayerUseNenToHit(player)) {
            return false;
        }
        amount *= (1.0F - NenTechniqueAbility.getIncomingReduction(this.soldierNenData));
        return super.hurt(source, amount);
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        if (!(target instanceof LivingEntity living)) {
            return super.doHurtTarget(target);
        }

        float damage = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE) + NenTechniqueAbility.getOutgoingDamageBonusTotal(this.soldierNenData);
        if (isKoActive()) {
            damage *= 1.35F;
            this.koStrikeTicks = 0;
            this.soldierNenData.setKoActive(false);
        }
        if (isRyuActive()) {
            damage += 1.5F;
        }

        boolean hurt = living.hurt(this.damageSources().mobAttack(this), damage);
        if (hurt) {
            Vec3 pushDir = living.position().subtract(this.position()).normalize();
            if (!Double.isFinite(pushDir.x) || !Double.isFinite(pushDir.z)) {
                pushDir = Vec3.ZERO;
            }
            living.push(pushDir.x * 0.18D, isRyuActive() ? 0.12D : 0.05D, pushDir.z * 0.18D);
            this.playSound(isKoActive() ? SoundEvents.PLAYER_ATTACK_CRIT : SoundEvents.PLAYER_ATTACK_STRONG, 0.7F, isRenActive() ? 0.9F : 1.05F);
        }
        return hurt;
    }

    private boolean canPlayerUseNenToHit(Player player) {
        HunterPlayerData data = HunterDataUtil.getOptional(player).orElse(null);
        return data != null && data.hasNen()
                && (data.isTenActive() || data.isZetsuActive() || data.isRenActive() || data.isEnActive() || data.isKoActive() || data.isKenActive() || data.isRyuActive());
    }

    @Override
    public void die(DamageSource damageSource) {
        ServerPlayer owner = this.getOwnerPlayer();
        if (!this.level().isClientSide() && owner != null) {
            refundSmoke(owner, PARTIAL_REFUND);
            refundNen(owner, NEN_UPKEEP_COST / 2);
        }
        super.die(damageSource);
    }

    private void refundSmoke(ServerPlayer owner, int amount) {
        if (this.refundedNen || amount <= 0) {
            return;
        }
        HunterPlayerData data = HunterDataUtil.getOptional(owner).orElse(null);
        if (data == null) {
            return;
        }
        data.addLungCapacity(amount);
        HunterDataUtil.sync(owner);
        this.refundedNen = true;
    }

    private void refundNen(ServerPlayer owner, int amount) {
        if (amount <= 0) {
            return;
        }
        HunterPlayerData data = HunterDataUtil.getOptional(owner).orElse(null);
        if (data == null) {
            return;
        }
        data.addNen(amount);
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
        return SoundEvents.CAMPFIRE_CRACKLE;
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
        this.playSound(SoundEvents.WOOL_STEP, 0.18F, 1.2F);
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
        tag.putFloat("ScoutAngleDegrees", this.scoutAngleDegrees);
        tag.putInt("ScoutStep", this.scoutStep);
        tag.putInt("RetargetTicks", this.retargetTicks);
        tag.putInt("AbilityCooldown", this.abilityCooldown);
        tag.putInt("SpentNen", this.spentNen);
        tag.putBoolean("RefundedNen", this.refundedNen);
        if (this.scoutTarget != null) {
            tag.putDouble("ScoutTargetX", this.scoutTarget.x);
            tag.putDouble("ScoutTargetY", this.scoutTarget.y);
            tag.putDouble("ScoutTargetZ", this.scoutTarget.z);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.ownerUuid = tag.hasUUID("OwnerUuid") ? tag.getUUID("OwnerUuid") : null;
        this.entityData.set(OWNER_UUID_DATA, this.ownerUuid != null ? this.ownerUuid.toString() : "");
        this.scoutAngleDegrees = tag.getFloat("ScoutAngleDegrees");
        this.scoutStep = tag.getInt("ScoutStep");
        this.retargetTicks = tag.getInt("RetargetTicks");
        this.abilityCooldown = tag.getInt("AbilityCooldown");
        this.spentNen = tag.getInt("SpentNen");
        this.refundedNen = tag.getBoolean("RefundedNen");
        if (tag.contains("ScoutTargetX")) {
            this.scoutTarget = new Vec3(tag.getDouble("ScoutTargetX"), tag.getDouble("ScoutTargetY"), tag.getDouble("ScoutTargetZ"));
        }
    }

    public HunterPlayerData getSoldierNenData() {
        return this.soldierNenData;
    }

    public void copyOwnerSnapshot(HunterPlayerData ownerData) {
        if (ownerData == null) {
            return;
        }
        this.soldierNenData.copyFrom(ownerData);
        this.soldierNenData.setCurrentNen(this.soldierNenData.getMaxNen());
        configureDeepPurpleNenStates(this.soldierNenData);
        this.soldierNenData.setGuarding(false);
        this.soldierNenData.clearActiveAbility();
        this.soldierNenData.clearChargingAbility();
        this.soldierNenData.clearMartialArtsGrab();
        this.soldierNenData.clearDeepPurplePing();
    }

    private void refreshNenStates(ServerPlayer owner) {
        this.soldierNenData.setCurrentNen(this.soldierNenData.getMaxNen());
        configureDeepPurpleNenStates(this.soldierNenData);
        boolean hasTarget = this.getTarget() != null && this.getTarget().isAlive();
        this.soldierNenData.setZetsuActive(!hasTarget && this.soldierNenData.hasZetsuUnlocked());
        this.soldierNenData.setKenActive(hasTarget && this.soldierNenData.hasKenUnlocked());
    }

    private boolean isRenActive() {
        return this.soldierNenData.isKenActive();
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

    private static HunterPlayerData createSoldierNenData() {
        HunterPlayerData data = new HunterPlayerData();
        data.setNenLevel(6);
        data.setCurrentNen(data.getMaxNen());
        data.setNenAuraColor(0xE2E2E2);
        data.setZetsuUnlocked(true);
        data.setKenUnlocked(true);
        return data;
    }

    private void tryUseCopiedAbility(HunterPlayerData ownerData, LivingEntity target) {
        if (this.abilityCooldown > 0 || target == null || !target.isAlive()) {
            return;
        }
        List<String> copiedAbilities = getCopiedCombatAbilities();
        if (copiedAbilities.isEmpty()) {
            return;
        }

        String abilityId = chooseCopiedAbility(copiedAbilities, target);
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

    private String chooseCopiedAbility(List<String> copiedAbilities, LivingEntity target) {
        double distanceSqr = this.distanceToSqr(target);
        List<String> preferred = new ArrayList<>();
        for (String abilityId : copiedAbilities) {
            BanditAbilityProfile profile = findBanditProfile(abilityId);
            if (profile == null) {
                if ("smokey_chain".equals(abilityId) && distanceSqr > 16.0D && distanceSqr < 196.0D) {
                    preferred.add(abilityId);
                }
                continue;
            }
            if (profile.areaRadius() > 0.0F || distanceSqr <= 36.0D || profile.lungeStrength() >= 1.45F) {
                preferred.add(abilityId);
            }
        }
        List<String> pool = preferred.isEmpty() ? copiedAbilities : preferred;
        return pool.get(this.random.nextInt(pool.size()));
    }

    private void trySmokeyChain(LivingEntity target) {
        if (this.distanceToSqr(target) > (14.0D * 14.0D) || !this.hasLineOfSight(target) || !(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        Vec3 anchor = this.position().add(0.0D, this.getBbHeight() * 0.78D, 0.0D);
        Vec3 targetCenter = target.position().add(0.0D, target.getBbHeight() * 0.55D, 0.0D);
        Vec3 towardAnchor = anchor.subtract(targetCenter);
        if (towardAnchor.lengthSqr() > 1.0E-4D) {
            Vec3 pull = towardAnchor.normalize().scale(0.42D);
            target.setDeltaMovement(target.getDeltaMovement().scale(0.62D).add(pull.x, pull.y * 0.16D, pull.z));
            target.hurtMarked = true;
            target.hasImpulse = true;
        }
        spawnChainParticles(serverLevel, anchor, targetCenter);
        this.abilityCooldown = 58;
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
        damage += NenTechniqueAbility.getOutgoingDamageBonusTotal(this.soldierNenData);
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
        this.playSound(profile.requiresWeapon() ? SoundEvents.PLAYER_ATTACK_SWEEP : SoundEvents.PLAYER_ATTACK_STRONG, 0.65F, 0.9F + (this.random.nextFloat() * 0.08F));
    }

    private List<String> getCopiedCombatAbilities() {
        Set<String> ordered = new LinkedHashSet<>();
        for (int bar = 0; bar < HunterPlayerData.COMBAT_BAR_COUNT; bar++) {
            for (int slot = 0; slot < HunterPlayerData.COMBAT_SLOT_COUNT; slot++) {
                String abilityId = this.soldierNenData.getCombatSlot(bar, slot);
                if (isCopiedCombatAbilityAllowed(abilityId)) {
                    ordered.add(abilityId);
                }
            }
        }
        for (SkillTreeCombatAbility ability : HunterAbilities.SKILL_TREE_COMBAT_ABILITIES) {
            if (ability.isUnlocked(this.soldierNenData) && isCopiedCombatAbilityAllowed(ability.id())) {
                ordered.add(ability.id());
            }
        }
        return new ArrayList<>(ordered);
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

    private static boolean isCopiedCombatAbilityAllowed(String abilityId) {
        return abilityId != null
                && !abilityId.isBlank()
                && !"smoke_clone".equals(abilityId)
                && !"smoke_soldier".equals(abilityId)
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

    private void spawnChainParticles(ServerLevel level, Vec3 start, Vec3 end) {
        Vec3 delta = end.subtract(start);
        int segments = Math.max(10, (int) Math.ceil(delta.length() * 1.2D));
        Vec3 step = delta.scale(1.0D / segments);
        for (int i = 0; i <= segments; i++) {
            Vec3 point = start.add(step.scale(i));
            level.sendParticles(HunterParticles.SMOKY_CHAIN_SMOKE.get(), point.x, point.y, point.z, 2, 0.03D, 0.03D, 0.03D, 0.01D);
        }
    }

}
