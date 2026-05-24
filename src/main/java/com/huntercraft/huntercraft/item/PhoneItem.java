package com.huntercraft.huntercraft.item;

import com.huntercraft.huntercraft.client.PhoneQuestScreen;
import com.huntercraft.huntercraft.util.HunterDataUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class PhoneItem extends Item {
    public PhoneItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);
        if (level.isClientSide()) {
            Minecraft.getInstance().setScreen(new PhoneQuestScreen());
        } else if (player instanceof ServerPlayer serverPlayer) {
            HunterDataUtil.getOptional(serverPlayer).ifPresent(data -> {
                if (data.ensurePhoneQuestRefreshStarted(level.getGameTime())) {
                    HunterDataUtil.sync(serverPlayer);
                }
            });
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
