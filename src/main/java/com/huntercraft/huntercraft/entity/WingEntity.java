package com.huntercraft.huntercraft.entity;

import com.huntercraft.huntercraft.abilities.HunterAbilities;
import com.huntercraft.huntercraft.abilities.nenability.NenTechniqueAbility;
import com.huntercraft.huntercraft.client.NenTrainerScreen;
import com.huntercraft.huntercraft.data.HunterPlayerData;
import com.huntercraft.huntercraft.quest.NenQuestStage;
import com.huntercraft.huntercraft.quest.NenQuestUtil;
import com.huntercraft.huntercraft.util.HunterDataUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class WingEntity extends PathfinderMob {
    private static final int HIDE_SWEEP_SPACING = 14;
    private static final double SPAR_DASH_RANGE_SQR = 196.0D;
    private final HunterPlayerData trainerNenData = createTrainerNenData();
    private UUID trainerTargetUuid;
    private boolean autoSpawnedTrainer;
    private UUID sparTargetUuid;
    private UUID hideTargetUuid;
    private boolean hideSearchActive;
    private BlockPos hideOrigin;
    private Vec3 hideSearchPoint = Vec3.ZERO;
    private int hideSearchRetargetTicks;
    private final List<Vec3> hideSweepPoints = new ArrayList<>();
    private int hideSweepIndex;
    private int dashCooldown;
    private int punchCooldown;
    private int uppercutCooldown;
    private int barrageCooldown;
    private int renPulseTicks;
    private int enPulseTicks;
    private int zetsuTicks;
    private int koWindupTicks;
    private int ryuBurstTicks;
    private int nenStateSwapTicks;
    private int strafeTicks;
    private boolean strafeClockwise;
    public WingEntity(EntityType<? extends WingEntity> type, Level level) {
        super(type, level);
        this.setCustomName(Component.literal("Wing"));
        this.setCustomNameVisible(true);
        this.setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 260.0D)
                .add(Attributes.ARMOR, 16.0D)
                .add(Attributes.ATTACK_DAMAGE, 14.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.45D)
                .add(Attributes.FOLLOW_RANGE, 160.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.95D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new WaterAvoidingRandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 10.0F));
        this.goalSelector.addGoal(3, new RandomLookAroundGoal(this));
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (this.level().isClientSide()) {
            Minecraft.getInstance().setScreen(new NenTrainerScreen(this.getId(), false));
            return InteractionResult.sidedSuccess(true);
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            return;
        }

        this.dashCooldown = Math.max(0, this.dashCooldown - 1);
        this.punchCooldown = Math.max(0, this.punchCooldown - 1);
        this.uppercutCooldown = Math.max(0, this.uppercutCooldown - 1);
        this.barrageCooldown = Math.max(0, this.barrageCooldown - 1);
        this.renPulseTicks = Math.max(0, this.renPulseTicks - 1);
        this.enPulseTicks = Math.max(0, this.enPulseTicks - 1);
        this.zetsuTicks = Math.max(0, this.zetsuTicks - 1);
        this.koWindupTicks = Math.max(0, this.koWindupTicks - 1);
        this.ryuBurstTicks = Math.max(0, this.ryuBurstTicks - 1);
        this.nenStateSwapTicks = Math.max(0, this.nenStateSwapTicks - 1);
        this.strafeTicks = Math.max(0, this.strafeTicks - 1);
        this.hideSearchRetargetTicks = Math.max(0, this.hideSearchRetargetTicks - 1);

        tickRyuSpar();
        tickZetsuSearch();
        tickTrainerPresence();
    }

    private void tickTrainerPresence() {
        if (!this.autoSpawnedTrainer || this.level().isClientSide()) {
            return;
        }
        ServerPlayer trainerTarget = getTrainerTarget();
        if (trainerTarget == null) {
            this.discard();
            return;
        }
        HunterPlayerData data = HunterDataUtil.getOptional(trainerTarget).orElse(null);
        if (data == null
                || data.getLevel() < HunterPlayerData.MAX_LEVEL
                || data.getNenQuestStage() == NenQuestStage.COMPLETED) {
            this.discard();
            return;
        }
        if (this.distanceToSqr(trainerTarget) > (128.0D * 128.0D)) {
            this.getNavigation().moveTo(trainerTarget, 1.1D);
        }
    }

    private void tickRyuSpar() {
        ServerPlayer target = this.getSparTarget();
        if (target == null || !target.isAlive()) {
            this.clearSpar();
            return;
        }

        HunterPlayerData data = HunterDataUtil.getOptional(target).orElse(null);
        if (data == null || data.getNenQuestStage() != NenQuestStage.RYU_SHIFT_AURA) {
            this.clearSpar();
            return;
        }

        this.trainerNenData.setCurrentNen(this.trainerNenData.getMaxNen());
        NenTechniqueAbility.tickNen(target, this.trainerNenData);
        updateNenCombatStates(target);
        this.lookAt(target, 50.0F, 30.0F);
        double distanceSqr = this.distanceToSqr(target);
        if (this.zetsuTicks > 0) {
            runZetsuReposition(target);
            return;
        }
        if (this.koWindupTicks > 0) {
            this.getNavigation().moveTo(target, 1.55D);
            if (distanceSqr <= 9.0D && this.koWindupTicks <= 2) {
                executeKoStrike(target);
            }
            return;
        }
        if (distanceSqr > 20.25D) {
            this.getNavigation().moveTo(target, getChaseSpeed());
        } else {
            strafeAround(target);
        }

        if (distanceSqr > 36.0D && distanceSqr < SPAR_DASH_RANGE_SQR && this.dashCooldown <= 0 && this.onGround()) {
            Vec3 direction = target.position().subtract(this.position()).normalize();
            double dashStrength = this.enPulseTicks > 0 ? 1.7D : 1.45D;
            this.setDeltaMovement(direction.x * dashStrength, 0.28D, direction.z * dashStrength);
            this.hasImpulse = true;
            this.dashCooldown = this.enPulseTicks > 0 ? 20 : 28;
            this.playSound(SoundEvents.PLAYER_ATTACK_SWEEP, 0.95F, 0.6F);
        }

        if (distanceSqr <= 9.0D && shouldStartKo(distanceSqr)) {
            startKoWindup(target);
            return;
        }
        if (distanceSqr <= 12.25D && this.barrageCooldown <= 0 && this.renPulseTicks > 0) {
            performBarrage(target);
            return;
        }
        if (distanceSqr <= 6.25D && this.uppercutCooldown <= 0 && this.onGround()) {
            attackTarget(target, 18.0F, 0.42D, SoundEvents.PLAYER_ATTACK_KNOCKBACK);
            this.uppercutCooldown = 24;
            this.punchCooldown = 8;
        } else if (distanceSqr <= 9.0D && this.punchCooldown <= 0) {
            float punchDamage = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE) + NenTechniqueAbility.getOutgoingDamageBonusTotal(this.trainerNenData);
            attackTarget(target, punchDamage, 0.1D, SoundEvents.PLAYER_ATTACK_STRONG);
            this.punchCooldown = isRenActive() ? 8 : 11;
        }
    }

    private void updateNenCombatStates(ServerPlayer target) {
        if (this.nenStateSwapTicks <= 0) {
            setTrainerNenState(target, HunterAbilities.TEN, true);
            setTrainerNenState(target, HunterAbilities.REN, this.random.nextBoolean());
            setTrainerNenState(target, HunterAbilities.KEN, this.getHealth() <= this.getMaxHealth() * 0.65F || this.random.nextFloat() < 0.4F);
            setTrainerNenState(target, HunterAbilities.RYU, this.getHealth() <= this.getMaxHealth() * 0.5F || this.random.nextFloat() < 0.35F);
            this.enPulseTicks = 40 + this.random.nextInt(40);
            this.renPulseTicks = isRenActive() ? 70 + this.random.nextInt(50) : 0;
            this.nenStateSwapTicks = 70 + this.random.nextInt(50);
        }
        if (isKenActive()) {
            setTrainerNenState(target, HunterAbilities.REN, false);
        }
        if (this.renPulseTicks <= 0 && !isKenActive()) {
            setTrainerNenState(target, HunterAbilities.REN, false);
        }
        if (this.ryuBurstTicks > 0) {
            setTrainerNenState(target, HunterAbilities.RYU, true);
        }
        setTrainerNenState(target, HunterAbilities.EN, this.enPulseTicks > 0 && !isZetsuActive());
        if (this.enPulseTicks == 1) {
            this.playSound(SoundEvents.BEACON_POWER_SELECT, 0.5F, 1.5F);
        }
        if (this.renPulseTicks == 1) {
            this.playSound(SoundEvents.AMETHYST_BLOCK_CHIME, 0.5F, 0.8F);
        }
    }

    private void runZetsuReposition(ServerPlayer target) {
        Vec3 away = this.position().subtract(target.position());
        if (away.lengthSqr() < 0.01D) {
            away = new Vec3(this.random.nextDouble() - 0.5D, 0.0D, this.random.nextDouble() - 0.5D);
        }
        Vec3 lateral = new Vec3(-away.z, 0.0D, away.x).normalize();
        Vec3 reposition = target.position().add(away.normalize().scale(5.5D)).add(lateral.scale(this.strafeClockwise ? 3.5D : -3.5D));
        this.getNavigation().moveTo(reposition.x, reposition.y, reposition.z, 1.7D);
        this.lookAt(target, 40.0F, 30.0F);
        if (this.zetsuTicks <= 1 && this.dashCooldown <= 0) {
            Vec3 direction = target.position().subtract(this.position()).normalize();
            this.setDeltaMovement(direction.x * 1.8D, 0.24D, direction.z * 1.8D);
            this.hasImpulse = true;
            this.dashCooldown = 14;
            this.ryuBurstTicks = 24;
            this.playSound(SoundEvents.ENDERMAN_TELEPORT, 0.5F, 0.65F);
        }
    }

    private boolean shouldStartKo(double distanceSqr) {
        return this.koWindupTicks <= 0
                && this.random.nextFloat() < 0.08F
                && (isRyuActive() || isRenActive())
                && distanceSqr <= 12.25D;
    }

    private void startKoWindup(ServerPlayer target) {
        this.koWindupTicks = 14;
        setTrainerNenState(target, HunterAbilities.KO, true);
        this.ryuBurstTicks = 18;
        this.getNavigation().stop();
        this.playSound(SoundEvents.RESPAWN_ANCHOR_SET_SPAWN, 0.5F, 0.75F);
    }

    private void executeKoStrike(ServerPlayer target) {
        float damage = (30.0F + (float) ((1.0D - (this.getHealth() / this.getMaxHealth())) * 10.0D))
                * HunterAbilities.KO.getKoDamageMultiplier(this.trainerNenData, target);
        attackTarget(target, damage, 0.55D, SoundEvents.PLAYER_ATTACK_CRIT);
        HunterAbilities.KO.onKoStrikeResolved(this.trainerNenData);
        this.koWindupTicks = 0;
        this.ryuBurstTicks = 32;
        this.dashCooldown = 18;
        this.uppercutCooldown = 18;
    }

    private void performBarrage(ServerPlayer target) {
        this.swing(InteractionHand.MAIN_HAND);
        for (int i = 0; i < 3; i++) {
            float barrageHit = 5.0F + NenTechniqueAbility.getOutgoingDamageBonusTotal(this.trainerNenData) + (isRyuActive() ? 1.5F : 0.0F);
            target.hurt(this.damageSources().mobAttack(this), barrageHit);
        }
        Vec3 pushDir = target.position().subtract(this.position()).normalize();
        target.push(pushDir.x * 0.18D, 0.12D, pushDir.z * 0.18D);
        this.playSound(SoundEvents.PLAYER_ATTACK_SWEEP, 0.9F, 0.9F);
        this.barrageCooldown = 34;
        this.punchCooldown = 10;
        this.ryuBurstTicks = 20;
    }

    private double getChaseSpeed() {
        if (this.zetsuTicks > 0) {
            return 1.7D;
        }
        if (this.enPulseTicks > 0 || isRenActive()) {
            return 1.45D;
        }
        return 1.25D;
    }

    private void tickZetsuSearch() {
        ServerPlayer target = this.getHideTarget();
        if (target == null || !target.isAlive()) {
            return;
        }
        HunterPlayerData data = HunterDataUtil.getOptional(target).orElse(null);
        if (data == null || data.getNenQuestStage() != NenQuestStage.ZETSU_DISAPPEAR) {
            stopHideTrial();
            return;
        }

        if (!this.hideSearchActive) {
            this.getNavigation().stop();
            this.lookAt(target, 12.0F, 12.0F);
            return;
        }

        if (this.hasLineOfSight(target)) {
            this.hideSearchPoint = target.position();
            this.hideSearchRetargetTicks = 30;
        } else if (this.hideSearchRetargetTicks <= 0 || this.position().distanceToSqr(this.hideSearchPoint) < 9.0D) {
            pickNewHideSearchPoint();
        }

        this.lookAt(target, 40.0F, 30.0F);
        this.getNavigation().moveTo(this.hideSearchPoint.x, this.hideSearchPoint.y, this.hideSearchPoint.z, 1.45D);
        tryHideSearchRecovery();
        if (this.dashCooldown <= 0 && this.onGround()) {
            Vec3 direction = this.hideSearchPoint.subtract(this.position());
            if (direction.horizontalDistanceSqr() > 4.0D) {
                direction = direction.normalize();
                this.setDeltaMovement(direction.x * 1.55D, 0.22D, direction.z * 1.55D);
                this.hasImpulse = true;
                this.dashCooldown = 18;
            }
        }
    }

    private void tryHideSearchRecovery() {
        boolean targetAbove = this.hideSearchPoint.y > this.getY() + 1.2D;
        boolean wedgedInHole = this.horizontalCollision && this.onGround();
        boolean stalledBelowTarget = this.onGround()
                && this.position().distanceToSqr(this.hideSearchPoint) > 16.0D
                && this.getDeltaMovement().horizontalDistanceSqr() < 0.01D;
        if (targetAbove || wedgedInHole || stalledBelowTarget) {
            this.setDeltaMovement(this.getDeltaMovement().x, 0.72D, this.getDeltaMovement().z);
            this.hasImpulse = true;
            this.playSound(SoundEvents.PLAYER_ATTACK_SWEEP, 0.35F, 1.15F);
        }
    }

    public boolean isHideTrialTrapped() {
        return this.isPassenger()
                || this.isInWaterOrBubble()
                || this.isInLava()
                || this.isInPowderSnow
                || this.isInWall()
                || this.getFeetBlockState().is(Blocks.COBWEB)
                || this.getBlockStateOn().is(Blocks.COBWEB);
    }

    private void pickNewHideSearchPoint() {
        if (this.hideSweepPoints.isEmpty()) {
            buildHideSweepPoints();
        }
        if (this.hideSweepPoints.isEmpty()) {
            this.hideSearchPoint = this.position();
            return;
        }
        if (this.hideSweepIndex >= this.hideSweepPoints.size()) {
            this.hideSweepIndex = 0;
        }
        this.hideSearchPoint = this.hideSweepPoints.get(this.hideSweepIndex++);
        this.hideSearchRetargetTicks = 18 + this.random.nextInt(12);
    }

    private void buildHideSweepPoints() {
        this.hideSweepPoints.clear();
        this.hideSweepIndex = 0;
        if (this.hideOrigin == null) {
            return;
        }

        Set<Long> visited = new HashSet<>();
        addHideSweepPoint(Vec3.atCenterOf(this.hideOrigin), visited);
        addDoorInteriorPointsAround(this.hideOrigin, visited);

        int radius = NenQuestUtil.ZETSU_HIDE_RADIUS;
        for (int ring = HIDE_SWEEP_SPACING; ring <= radius; ring += HIDE_SWEEP_SPACING) {
            addSweepRing(ring, visited);
        }
    }

    private void addSweepRing(int ring, Set<Long> visited) {
        for (int xOffset = -ring; xOffset <= ring; xOffset += HIDE_SWEEP_SPACING) {
            addSweepAnchor(this.hideOrigin.offset(xOffset, 0, -ring), visited);
            addSweepAnchor(this.hideOrigin.offset(xOffset, 0, ring), visited);
        }
        for (int zOffset = -ring + HIDE_SWEEP_SPACING; zOffset <= ring - HIDE_SWEEP_SPACING; zOffset += HIDE_SWEEP_SPACING) {
            addSweepAnchor(this.hideOrigin.offset(-ring, 0, zOffset), visited);
            addSweepAnchor(this.hideOrigin.offset(ring, 0, zOffset), visited);
        }
    }

    private void addSweepAnchor(BlockPos anchor, Set<Long> visited) {
        if (this.hideOrigin == null || anchor.distSqr(this.hideOrigin) > (NenQuestUtil.ZETSU_HIDE_RADIUS * NenQuestUtil.ZETSU_HIDE_RADIUS)) {
            return;
        }
        BlockPos ground = findSearchGround(anchor);
        addHideSweepPoint(Vec3.atCenterOf(ground), visited);
        addDoorInteriorPointsAround(ground, visited);
    }

    private void addDoorInteriorPointsAround(BlockPos anchor, Set<Long> visited) {
        for (int yOffset = -2; yOffset <= 3; yOffset++) {
            for (int xOffset = -3; xOffset <= 3; xOffset++) {
                for (int zOffset = -3; zOffset <= 3; zOffset++) {
                    BlockPos checkPos = anchor.offset(xOffset, yOffset, zOffset);
                    BlockState state = this.level().getBlockState(checkPos);
                    if (!(state.getBlock() instanceof DoorBlock door)) {
                        continue;
                    }
                    Direction facing = state.getValue(DoorBlock.FACING);
                    addHideSweepPoint(Vec3.atCenterOf(findSearchGround(checkPos.relative(facing))), visited);
                    addHideSweepPoint(Vec3.atCenterOf(findSearchGround(checkPos.relative(facing.getOpposite()))), visited);
                }
            }
        }
    }

    private void addHideSweepPoint(Vec3 point, Set<Long> visited) {
        BlockPos pos = BlockPos.containing(point);
        if (this.hideOrigin != null && pos.distSqr(this.hideOrigin) > (NenQuestUtil.ZETSU_HIDE_RADIUS * NenQuestUtil.ZETSU_HIDE_RADIUS)) {
            return;
        }
        long key = pos.asLong();
        if (!visited.add(key)) {
            return;
        }
        this.hideSweepPoints.add(point);
    }

    private BlockPos findSearchGround(BlockPos anchor) {
        int baseY = this.hideOrigin != null ? this.hideOrigin.getY() : anchor.getY();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos(anchor.getX(), baseY + 4, anchor.getZ());
        for (int y = baseY + 4; y >= Math.max(this.level().getMinBuildHeight() + 1, baseY - 8); y--) {
            cursor.setY(y);
            if (!this.level().getBlockState(cursor).isAir() && this.level().getBlockState(cursor.above()).isAir()) {
                return cursor.above().immutable();
            }
        }
        return new BlockPos(anchor.getX(), baseY, anchor.getZ());
    }

    private void strafeAround(ServerPlayer target) {
        if (this.strafeTicks <= 0) {
            this.strafeTicks = 18 + this.random.nextInt(20);
            this.strafeClockwise = this.random.nextBoolean();
        }

        Vec3 toTarget = target.position().subtract(this.position());
        Vec3 lateral = new Vec3(-toTarget.z, 0.0D, toTarget.x).normalize();
        if (!this.strafeClockwise) {
            lateral = lateral.scale(-1.0D);
        }
        this.setDeltaMovement(lateral.x * 0.17D, this.getDeltaMovement().y, lateral.z * 0.17D);
        this.hasImpulse = true;
    }

    private void attackTarget(ServerPlayer target, float damage, double verticalPush, net.minecraft.sounds.SoundEvent sound) {
        target.hurt(this.damageSources().mobAttack(this), damage);
        Vec3 pushDir = target.position().subtract(this.position()).normalize();
        if (!Double.isFinite(pushDir.x) || !Double.isFinite(pushDir.z)) {
            pushDir = new Vec3(0.0D, 0.0D, 0.0D);
        }
        target.push(pushDir.x * 0.28D, verticalPush, pushDir.z * 0.28D);
        this.swing(InteractionHand.MAIN_HAND);
        this.playSound(sound, 0.9F, 0.8F);
    }

    public void startHideTrial(ServerPlayer player, BlockPos origin) {
        this.hideTargetUuid = player.getUUID();
        this.hideOrigin = origin;
        this.hideSearchActive = false;
        this.hideSearchPoint = Vec3.atCenterOf(origin);
        this.hideSearchRetargetTicks = 0;
        this.buildHideSweepPoints();
        this.getNavigation().moveTo(origin.getX(), origin.getY(), origin.getZ(), 1.15D);
    }

    public void beginHideSearch() {
        this.hideSearchActive = true;
        this.hideSearchRetargetTicks = 0;
        pickNewHideSearchPoint();
        this.playSound(SoundEvents.ENDERMAN_SCREAM, 0.5F, 0.55F);
    }

    public void stopHideTrial() {
        this.hideTargetUuid = null;
        this.hideSearchActive = false;
        this.hideOrigin = null;
        this.hideSearchPoint = Vec3.ZERO;
        this.hideSearchRetargetTicks = 0;
        this.hideSweepPoints.clear();
        this.hideSweepIndex = 0;
        this.getNavigation().stop();
    }

    public boolean isRunningHideTrialFor(Player player) {
        return this.hideTargetUuid != null && this.hideTargetUuid.equals(player.getUUID());
    }

    public boolean isHideSearchActive() {
        return this.hideSearchActive;
    }

    public void setTrainerTarget(ServerPlayer player, boolean autoSpawnedTrainer) {
        this.trainerTargetUuid = player == null ? null : player.getUUID();
        this.autoSpawnedTrainer = autoSpawnedTrainer;
    }

    public ServerPlayer getTrainerTarget() {
        if (!(this.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) || this.trainerTargetUuid == null) {
            return null;
        }
        return serverLevel.getServer().getPlayerList().getPlayer(this.trainerTargetUuid);
    }

    public boolean isTrainerAssignedTo(Player player) {
        return player != null && this.trainerTargetUuid != null && this.trainerTargetUuid.equals(player.getUUID());
    }

    public boolean isAutoSpawnedTrainer() {
        return this.autoSpawnedTrainer;
    }

    public HunterPlayerData getTrainerNenData() {
        return this.trainerNenData;
    }

    public boolean canSpotHideTarget(ServerPlayer player) {
        return this.hideSearchActive
                && this.isRunningHideTrialFor(player)
                && this.distanceToSqr(player) <= (NenQuestUtil.ZETSU_HIDE_RADIUS * NenQuestUtil.ZETSU_HIDE_RADIUS)
                && this.hasLineOfSight(player);
    }

    private static HunterPlayerData createTrainerNenData() {
        HunterPlayerData data = new HunterPlayerData();
        data.setNenLevel(10);
        data.setCurrentNen(data.getMaxNen());
        data.setNenAuraColor(0xFFFFFF);
        data.setTenUnlocked(true);
        data.setZetsuUnlocked(true);
        data.setRenUnlocked(true);
        data.setGyoUnlocked(true);
        data.setEnUnlocked(true);
        data.setShuUnlocked(true);
        data.setKoUnlocked(true);
        data.setKenUnlocked(true);
        data.setRyuUnlocked(true);
        return data;
    }

    private void resetTrainerNenForSpar(ServerPlayer target) {
        this.trainerNenData.disableAllNen();
        this.trainerNenData.setCurrentNen(this.trainerNenData.getMaxNen());
        setTrainerNenState(target, HunterAbilities.TEN, true);
        setTrainerNenState(target, HunterAbilities.REN, true);
        setTrainerNenState(target, HunterAbilities.EN, true);
    }

    private void setTrainerNenState(ServerPlayer target, NenTechniqueAbility ability, boolean active) {
        if (target == null) {
            return;
        }
        if (active) {
            if (!ability.isActive(this.trainerNenData)) {
                ability.use(target, this.trainerNenData, Vec3.ZERO);
            }
        } else if (ability.isActive(this.trainerNenData)) {
            ability.stop(target, this.trainerNenData);
        }
    }

    private boolean isRenActive() {
        return HunterAbilities.REN.isActive(this.trainerNenData) || HunterAbilities.KEN.isActive(this.trainerNenData);
    }

    private boolean isKenActive() {
        return HunterAbilities.KEN.isActive(this.trainerNenData);
    }

    private boolean isRyuActive() {
        return HunterAbilities.RYU.isActive(this.trainerNenData);
    }

    private boolean isZetsuActive() {
        return HunterAbilities.ZETSU.isActive(this.trainerNenData);
    }

    public void startSpar(ServerPlayer player) {
        this.sparTargetUuid = player.getUUID();
        this.dashCooldown = 10;
        this.punchCooldown = 0;
        this.uppercutCooldown = 16;
        this.barrageCooldown = 10;
        this.renPulseTicks = 80;
        this.enPulseTicks = 60;
        this.zetsuTicks = 0;
        this.koWindupTicks = 0;
        this.ryuBurstTicks = 0;
        this.nenStateSwapTicks = 0;
        this.strafeTicks = 0;
        resetTrainerNenForSpar(player);
    }

    public void clearSpar() {
        this.sparTargetUuid = null;
        this.zetsuTicks = 0;
        this.koWindupTicks = 0;
        this.getNavigation().stop();
        this.trainerNenData.disableAllNen();
    }

    public boolean isSparringWith(Player player) {
        return this.sparTargetUuid != null && this.sparTargetUuid.equals(player.getUUID());
    }

    public ServerPlayer getSparTarget() {
        if (!(this.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) || this.sparTargetUuid == null) {
            return null;
        }
        return serverLevel.getServer().getPlayerList().getPlayer(this.sparTargetUuid);
    }

    public ServerPlayer getHideTarget() {
        if (!(this.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) || this.hideTargetUuid == null) {
            return null;
        }
        return serverLevel.getServer().getPlayerList().getPlayer(this.hideTargetUuid);
    }

    public void completeRyuTrial(ServerPlayer player) {
        this.clearSpar();
        this.setHealth(this.getMaxHealth());
        player.setHealth(player.getMaxHealth());
        player.removeAllEffects();
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.getEntity() instanceof ServerPlayer player) {
            HunterPlayerData data = HunterDataUtil.getOptional(player).orElse(null);
            if (data == null || data.getNenQuestStage() != NenQuestStage.RYU_SHIFT_AURA || !this.isSparringWith(player)) {
                return false;
            }
        }
        amount *= (1.0F - NenTechniqueAbility.getIncomingReduction(this.trainerNenData));
        if (this.zetsuTicks > 0) {
            amount *= 1.15F;
        }
        return super.hurt(source, amount);
    }

    @Override
    protected net.minecraft.sounds.SoundEvent getAmbientSound() {
        return SoundEvents.VILLAGER_AMBIENT;
    }

    @Override
    protected net.minecraft.sounds.SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.PLAYER_HURT;
    }

    @Override
    protected net.minecraft.sounds.SoundEvent getDeathSound() {
        return SoundEvents.PLAYER_DEATH;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (this.sparTargetUuid != null) {
            tag.putUUID("SparTarget", this.sparTargetUuid);
        }
        if (this.hideTargetUuid != null) {
            tag.putUUID("HideTarget", this.hideTargetUuid);
        }
        if (this.hideOrigin != null) {
            tag.putInt("HideOriginX", this.hideOrigin.getX());
            tag.putInt("HideOriginY", this.hideOrigin.getY());
            tag.putInt("HideOriginZ", this.hideOrigin.getZ());
        }
        if (this.trainerTargetUuid != null) {
            tag.putUUID("TrainerTarget", this.trainerTargetUuid);
        }
        tag.putBoolean("AutoSpawnedTrainer", this.autoSpawnedTrainer);
        tag.putBoolean("HideSearchActive", this.hideSearchActive);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.setCustomName(Component.literal("Wing"));
        this.setCustomNameVisible(true);
        this.trainerTargetUuid = tag.hasUUID("TrainerTarget") ? tag.getUUID("TrainerTarget") : null;
        this.autoSpawnedTrainer = tag.getBoolean("AutoSpawnedTrainer");
        this.sparTargetUuid = tag.hasUUID("SparTarget") ? tag.getUUID("SparTarget") : null;
        this.hideTargetUuid = tag.hasUUID("HideTarget") ? tag.getUUID("HideTarget") : null;
        this.hideOrigin = tag.contains("HideOriginX") ? new BlockPos(tag.getInt("HideOriginX"), tag.getInt("HideOriginY"), tag.getInt("HideOriginZ")) : null;
        this.hideSearchActive = tag.getBoolean("HideSearchActive");
    }
}
