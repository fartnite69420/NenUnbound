package com.huntercraft.huntercraft.entity;

import com.huntercraft.huntercraft.item.HunterItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class TonpaEntity extends PathfinderMob {
    public TonpaEntity(EntityType<? extends TonpaEntity> type, Level level) {
        super(type, level);
        this.setCustomName(Component.literal("Tonpa"));
        this.setCustomNameVisible(true);
        this.setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.22D)
                .add(Attributes.FOLLOW_RANGE, 16.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.1D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new WaterAvoidingRandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(3, new RandomLookAroundGoal(this));
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (player.level().isClientSide()) {
            return InteractionResult.sidedSuccess(true);
        }
        if (player.getInventory().countItem(HunterItems.PHONE.get()) > 0) {
            player.sendSystemMessage(Component.translatable("message.huntercraft.tonpa.has_phone"));
            return InteractionResult.CONSUME;
        }
        if (held.is(Items.EMERALD)) {
            if (!player.getAbilities().instabuild) {
                held.shrink(1);
            }
            ItemStack phone = new ItemStack(HunterItems.PHONE.get());
            if (!player.addItem(phone)) {
                player.drop(phone, false);
            }
            this.playAmbientSound();
            player.sendSystemMessage(Component.translatable("message.huntercraft.tonpa.sold_phone"));
            return InteractionResult.CONSUME;
        }
        player.sendSystemMessage(Component.translatable("message.huntercraft.tonpa.offer"));
        return InteractionResult.CONSUME;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.setCustomName(Component.literal("Tonpa"));
        this.setCustomNameVisible(true);
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        return super.isInvulnerableTo(source);
    }

    @Override
    protected net.minecraft.sounds.SoundEvent getAmbientSound() {
        return net.minecraft.sounds.SoundEvents.VILLAGER_AMBIENT;
    }

    @Override
    protected net.minecraft.sounds.SoundEvent getHurtSound(DamageSource damageSource) {
        return net.minecraft.sounds.SoundEvents.VILLAGER_HURT;
    }

    @Override
    protected net.minecraft.sounds.SoundEvent getDeathSound() {
        return net.minecraft.sounds.SoundEvents.VILLAGER_DEATH;
    }
}
