package com.huntercraft.huntercraft.entity;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import org.jetbrains.annotations.Nullable;

public class GreatStampPigEntity extends Monster {
    private static final EntityDataAccessor<Integer> STOMP_TICKS = SynchedEntityData.defineId(GreatStampPigEntity.class, EntityDataSerializers.INT);
    private static final int MAX_STOMP_TICKS = 10;

    public GreatStampPigEntity(EntityType<? extends GreatStampPigEntity> type, Level level) {
        super(type, level);
        this.xpReward = 14;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 90.0D)
                .add(Attributes.ARMOR, 10.0D)
                .add(Attributes.ATTACK_DAMAGE, 6.0D)
                .add(Attributes.ATTACK_KNOCKBACK, 0.75D)
                .add(Attributes.MOVEMENT_SPEED, 0.24D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.2D)
                .add(Attributes.FOLLOW_RANGE, 24.0D);
    }

    public static boolean canSpawn(EntityType<GreatStampPigEntity> type, ServerLevelAccessor level, MobSpawnType spawnType, net.minecraft.core.BlockPos pos, net.minecraft.util.RandomSource random) {
        return level.getDifficulty() != net.minecraft.world.Difficulty.PEACEFUL
                && Monster.checkMonsterSpawnRules(type, level, spawnType, pos, random)
                && level.getBrightness(net.minecraft.world.level.LightLayer.SKY, pos) < 12;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.3D, false));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 0.85D));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 12.0F));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(STOMP_TICKS, 0);
    }

    @Override
    public void tick() {
        super.tick();
        int stompTicks = this.entityData.get(STOMP_TICKS);
        if (stompTicks > 0) {
            this.entityData.set(STOMP_TICKS, stompTicks - 1);
        }
    }

    @Override
    public boolean doHurtTarget(net.minecraft.world.entity.Entity target) {
        this.entityData.set(STOMP_TICKS, MAX_STOMP_TICKS);
        this.playSound(SoundEvents.HOGLIN_ATTACK, 1.0F, 0.55F + (this.random.nextFloat() * 0.08F));
        return super.doHurtTarget(target);
    }

    public float getStompProgress(float partialTick) {
        int stompTicks = this.entityData.get(STOMP_TICKS);
        int previous = Math.max(0, stompTicks - 1);
        return Mth.lerp(partialTick, previous, stompTicks) / (float) MAX_STOMP_TICKS;
    }

    public boolean isRunningAtPlayer() {
        return this.getTarget() instanceof Player && this.getDeltaMovement().horizontalDistanceSqr() > 0.01D;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.PIG_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(net.minecraft.world.damagesource.DamageSource damageSource) {
        return SoundEvents.PIG_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.PIG_DEATH;
    }

    @Override
    protected float getSoundVolume() {
        return 1.15F;
    }

    @Override
    public float getVoicePitch() {
        return 0.55F;
    }

    @Override
    protected void playStepSound(net.minecraft.core.BlockPos pos, BlockState block) {
        this.playSound(SoundEvents.PIG_STEP, 0.45F, 0.5F);
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType, @Nullable SpawnGroupData spawnGroupData, @Nullable net.minecraft.nbt.CompoundTag tag) {
        return super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData, tag);
    }
}
