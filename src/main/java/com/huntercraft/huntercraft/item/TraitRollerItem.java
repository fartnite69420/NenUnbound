package com.huntercraft.huntercraft.item;

import com.huntercraft.huntercraft.data.TraitType;
import com.huntercraft.huntercraft.util.HunterDataUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class TraitRollerItem extends Item {
    public TraitRollerItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, net.minecraft.world.entity.player.Player player, InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }
        HunterDataUtil.getOptional(serverPlayer).ifPresent(data -> {
            TraitType rolledTrait = serverPlayer.getRandom().nextFloat() <= 0.10F ? TraitType.SCARLET_EYES : TraitType.NONE;
            data.setTrait(rolledTrait);
            HunterDataUtil.sync(serverPlayer);
            serverPlayer.sendSystemMessage(Component.literal("Trait rolled: " + rolledTrait.displayName()));
        });
        if (!serverPlayer.getAbilities().instabuild) {
            stack.shrink(1);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
