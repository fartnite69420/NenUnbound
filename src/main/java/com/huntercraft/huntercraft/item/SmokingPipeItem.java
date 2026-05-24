package com.huntercraft.huntercraft.item;

import com.huntercraft.huntercraft.animation.AnimationType;
import com.huntercraft.huntercraft.particle.HunterParticles;
import com.huntercraft.huntercraft.util.HunterDataUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class SmokingPipeItem extends SwordItem {
    public SmokingPipeItem(Tier tier, int attackDamageModifier, float attackSpeedModifier, Properties properties) {
        super(tier, attackDamageModifier, attackSpeedModifier, properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);
        InteractionHand otherHand = usedHand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        ItemStack otherStack = player.getItemInHand(otherHand);
        if (shouldPrioritizeEating(player, otherStack)) {
            return InteractionResultHolder.pass(stack);
        }
        player.startUsingItem(usedHand);
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            HunterDataUtil.getOptional(serverPlayer).ifPresent(data -> {
                data.triggerAnimation(AnimationType.SMOKING_PIPE);
                HunterDataUtil.sync(serverPlayer);
            });
        }
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.TOOT_HORN;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72_000;
    }

    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int remainingUseDuration) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        if (livingEntity instanceof ServerPlayer serverPlayer) {
            HunterDataUtil.getOptional(serverPlayer).ifPresent(data -> {
                double gainPerTick = data.getDeepPurpleLungGainPerTick();
                if (gainPerTick <= 0.0D) {
                    data.resetDeepPurpleLungGainProgress();
                    return;
                }
                data.addDeepPurpleLungGainProgress(gainPerTick);
                boolean changed = false;
                while (data.consumeDeepPurpleLungGainStep()) {
                    changed |= data.addLungCapacity(1);
                }
                if (changed) {
                    HunterDataUtil.sync(serverPlayer);
                }
            });
        }
        if ((remainingUseDuration % 6) != 0) {
            return;
        }
        Vec3 look = livingEntity.getLookAngle().normalize();
        Vec3 mouth = livingEntity.getEyePosition().add(look.scale(0.25D)).add(0.0D, -0.18D, 0.0D);
        serverLevel.sendParticles(HunterParticles.MOREL_SMOKE.get(), mouth.x, mouth.y, mouth.z, 2, 0.03D, 0.03D, 0.03D, 0.01D);
        serverLevel.sendParticles(HunterParticles.MOREL_SMOKE.get(), mouth.x, mouth.y + 0.03D, mouth.z, 3, 0.05D, 0.05D, 0.05D, 0.015D);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity livingEntity, int timeCharged) {
        stopSmoking(level, livingEntity);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
        stopSmoking(level, livingEntity);
        return stack;
    }

    private static void stopSmoking(Level level, LivingEntity livingEntity) {
        if (!level.isClientSide() && livingEntity instanceof ServerPlayer serverPlayer) {
            HunterDataUtil.getOptional(serverPlayer).ifPresent(data -> {
                data.resetDeepPurpleLungGainProgress();
                data.triggerAnimation(AnimationType.NONE);
                HunterDataUtil.sync(serverPlayer);
            });
        }
    }

    private static boolean shouldPrioritizeEating(Player player, ItemStack otherStack) {
        if (otherStack.isEmpty()) {
            return false;
        }
        FoodProperties foodProperties = otherStack.getFoodProperties(player);
        return foodProperties != null && player.canEat(foodProperties.canAlwaysEat());
    }
}
