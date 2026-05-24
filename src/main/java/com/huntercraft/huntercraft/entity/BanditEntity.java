package com.huntercraft.huntercraft.entity;

import com.huntercraft.huntercraft.entity.ability.BanditStyleEntityAbility;
import com.huntercraft.huntercraft.entity.ability.EntityAbilityController;
import com.huntercraft.huntercraft.entity.ability.EntityAbilityMob;
import com.huntercraft.huntercraft.progression.SkillNode;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class BanditEntity extends Monster implements EntityAbilityMob {
    private static final EntityDataAccessor<Integer> SKIN_VARIANT = SynchedEntityData.defineId(BanditEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<String> ABILITY_ID = SynchedEntityData.defineId(BanditEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Integer> ABILITY_WINDUP = SynchedEntityData.defineId(BanditEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> GROUP_TARGET_SIZE = SynchedEntityData.defineId(BanditEntity.class, EntityDataSerializers.INT);
    private final EntityAbilityController abilityController = new EntityAbilityController();
    private int dashCooldown;

    public BanditEntity(EntityType<? extends BanditEntity> type, Level level) {
        super(type, level);
        this.xpReward = 10;
        this.abilityController.add(new BanditStyleEntityAbility(this::getAbilityProfile));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 34.0D)
                .add(Attributes.ARMOR, 3.0D)
                .add(Attributes.ATTACK_DAMAGE, 4.0D)
                .add(Attributes.ATTACK_SPEED, 1.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.28D)
                .add(Attributes.FOLLOW_RANGE, 32.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.08D);
    }

    public static boolean canSpawn(EntityType<BanditEntity> type, ServerLevelAccessor level, MobSpawnType spawnType, net.minecraft.core.BlockPos pos, RandomSource random) {
        return level.getDifficulty() != Difficulty.PEACEFUL
                && Monster.checkMonsterSpawnRules(type, level, spawnType, pos, random)
                && level.getBrightness(net.minecraft.world.level.LightLayer.SKY, pos) < 13;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.15D, false));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 0.95D));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 12.0F));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(SKIN_VARIANT, 0);
        this.entityData.define(ABILITY_ID, BanditAbilityProfile.FLASH_CLEAVE.abilityId());
        this.entityData.define(ABILITY_WINDUP, 0);
        this.entityData.define(GROUP_TARGET_SIZE, 2);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            return;
        }

        if (this.dashCooldown > 0) {
            this.dashCooldown--;
        }

        LivingEntity target = this.getTarget();
        if (target == null || !target.isAlive()) {
            this.abilityController.cancelWindup();
            this.entityData.set(ABILITY_WINDUP, 0);
            return;
        }

        tryDashToCatchTarget(target);

        this.abilityController.tick(this, target);
        this.entityData.set(ABILITY_WINDUP, this.abilityController.getWindupTicks());
        if (this.abilityController.isWindingUp()) {
            return;
        }

        if (this.abilityController.tryUseAny(this, target)) {
            this.entityData.set(ABILITY_WINDUP, this.abilityController.getWindupTicks());
        }
    }

    private void tryDashToCatchTarget(LivingEntity target) {
        if (this.dashCooldown > 0 || this.abilityController.isWindingUp() || !this.onGround()) {
            return;
        }
        double distanceSqr = this.distanceToSqr(target);
        if (distanceSqr < 49.0D || distanceSqr > 256.0D) {
            return;
        }
        Vec3 dashDirection = target.position().subtract(this.position()).normalize();
        this.setDeltaMovement(dashDirection.x * 1.2D, 0.24D, dashDirection.z * 1.2D);
        this.hasImpulse = true;
        this.swing(this.getMainHandItem().isEmpty() ? net.minecraft.world.InteractionHand.MAIN_HAND : net.minecraft.world.InteractionHand.MAIN_HAND);
        this.playSound(SoundEvents.PLAYER_ATTACK_SWEEP, 0.7F, 0.85F + (this.random.nextFloat() * 0.2F));
        this.dashCooldown = 50;
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        if (this.abilityController.isWindingUp()) {
            return false;
        }
        return super.doHurtTarget(target);
    }

    @Override
    public EntityAbilityController getEntityAbilityController() {
        return this.abilityController;
    }

    public BanditAbilityProfile getAbilityProfile() {
        return BanditAbilityProfile.byId(this.entityData.get(ABILITY_ID));
    }

    public int getSkinVariant() {
        return Mth.clamp(this.entityData.get(SKIN_VARIANT), 0, 3);
    }

    public boolean usesSlimModel() {
        return switch (this.getSkinVariant()) {
            case 1, 2 -> true;
            default -> false;
        };
    }

    public Component getBanditAbilityName() {
        return Component.literal(this.getAbilityProfile().displayName());
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.PILLAGER_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(net.minecraft.world.damagesource.DamageSource damageSource) {
        return SoundEvents.PLAYER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.PLAYER_DEATH;
    }

    @Override
    protected void playStepSound(net.minecraft.core.BlockPos pos, BlockState block) {
        this.playSound(SoundEvents.PLAYER_ATTACK_WEAK, 0.08F, 0.65F);
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType, @Nullable SpawnGroupData spawnGroupData, @Nullable CompoundTag tag) {
        SpawnGroupData result = super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData, tag);
        BanditSpawnGroupData groupData = spawnGroupData instanceof BanditSpawnGroupData existing ? existing : new BanditSpawnGroupData(selectGroupSize(this.random));
        groupData.spawnedCount++;
        if (groupData.spawnedCount > groupData.targetSize) {
            this.discard();
            return groupData;
        }

        this.entityData.set(GROUP_TARGET_SIZE, groupData.targetSize);
        this.entityData.set(SKIN_VARIANT, this.random.nextInt(4));
        this.entityData.set(ABILITY_ID, BanditAbilityProfile.random(this.random).abilityId());
        equipForAssignedAbility();
        return groupData;
    }

    private void equipForAssignedAbility() {
        SkillNode.Category category = this.getAbilityProfile().skillNode().category();
        if (category == SkillNode.Category.WEAPON) {
            ItemStack weapon = switch (this.random.nextInt(3)) {
                case 0 -> new ItemStack(Items.IRON_SWORD);
                case 1 -> new ItemStack(Items.STONE_SWORD);
                default -> new ItemStack(Items.IRON_AXE);
            };
            this.setItemSlot(EquipmentSlot.MAINHAND, weapon);
        } else {
            this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        }
        this.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
    }

    private static int selectGroupSize(RandomSource random) {
        int roll = random.nextInt(100);
        if (roll < 55) {
            return 2;
        }
        if (roll < 85) {
            return 3;
        }
        return 4;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("SkinVariant", this.getSkinVariant());
        tag.putString("AssignedAbilityId", this.entityData.get(ABILITY_ID));
        tag.putInt("DashCooldown", this.dashCooldown);
        tag.put("EntityAbilities", this.abilityController.save());
        tag.putInt("AbilityWindup", this.abilityController.getWindupTicks());
        tag.putInt("GroupTargetSize", this.entityData.get(GROUP_TARGET_SIZE));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.entityData.set(SKIN_VARIANT, Mth.clamp(tag.getInt("SkinVariant"), 0, 3));
        this.entityData.set(ABILITY_ID, tag.contains("AssignedAbilityId") ? tag.getString("AssignedAbilityId") : BanditAbilityProfile.FLASH_CLEAVE.abilityId());
        this.dashCooldown = tag.getInt("DashCooldown");
        if (tag.contains("EntityAbilities")) {
            this.abilityController.load(tag.getCompound("EntityAbilities"));
        }
        this.entityData.set(ABILITY_WINDUP, this.abilityController.getWindupTicks());
        this.entityData.set(GROUP_TARGET_SIZE, Math.max(2, tag.getInt("GroupTargetSize")));
        equipForAssignedAbility();
    }

    private static final class BanditSpawnGroupData implements SpawnGroupData {
        private final int targetSize;
        private int spawnedCount;

        private BanditSpawnGroupData(int targetSize) {
            this.targetSize = targetSize;
        }
    }
}
