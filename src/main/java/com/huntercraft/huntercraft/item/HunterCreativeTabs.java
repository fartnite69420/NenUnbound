package com.huntercraft.huntercraft.item;

import com.huntercraft.huntercraft.HunterCraftMod;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class HunterCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(net.minecraft.core.registries.Registries.CREATIVE_MODE_TAB, HunterCraftMod.MODID);

    public static final RegistryObject<CreativeModeTab> HUNTERCRAFT_TAB = CREATIVE_MODE_TABS.register("huntercraft",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.huntercraft"))
                    .icon(() -> new ItemStack(HunterItems.SMOKING_PIPE.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(HunterItems.SMOKING_PIPE.get());
                        output.accept(HunterItems.PHONE.get());
                        output.accept(HunterItems.GREAT_STAMP_PIG_SPAWN_EGG.get());
                        output.accept(HunterItems.BANDIT_SPAWN_EGG.get());
                        output.accept(HunterItems.TONPA_SPAWN_EGG.get());
                        output.accept(HunterItems.WING_SPAWN_EGG.get());
                    })
                    .build());

    private HunterCreativeTabs() {
    }

    public static void register(IEventBus modBus) {
        CREATIVE_MODE_TABS.register(modBus);
    }
}
