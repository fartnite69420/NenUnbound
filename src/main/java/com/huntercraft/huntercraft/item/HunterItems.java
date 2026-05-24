package com.huntercraft.huntercraft.item;

import com.huntercraft.huntercraft.HunterCraftMod;
import com.huntercraft.huntercraft.entity.HunterEntityTypes;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tiers;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(modid = HunterCraftMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class HunterItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, HunterCraftMod.MODID);

    public static final RegistryObject<Item> GREAT_STAMP_PIG_SPAWN_EGG = ITEMS.register("great_stamp_pig_spawn_egg",
            () -> new ForgeSpawnEggItem(HunterEntityTypes.GREAT_STAMP_PIG, 0xF0A5A2, 0xDB635F, new Item.Properties()));
    public static final RegistryObject<Item> BANDIT_SPAWN_EGG = ITEMS.register("bandit_spawn_egg",
            () -> new ForgeSpawnEggItem(HunterEntityTypes.BANDIT, 0x2C201D, 0xC9B8A1, new Item.Properties()));
    public static final RegistryObject<Item> TONPA_SPAWN_EGG = ITEMS.register("tonpa_spawn_egg",
            () -> new ForgeSpawnEggItem(HunterEntityTypes.TONPA, 0xE6A86F, 0x2D4763, new Item.Properties()));
    public static final RegistryObject<Item> WING_SPAWN_EGG = ITEMS.register("wing_spawn_egg",
            () -> new ForgeSpawnEggItem(HunterEntityTypes.WING, 0xD8D9D9, 0x2F2F2F, new Item.Properties()));
    public static final RegistryObject<Item> SMOKING_PIPE = ITEMS.register("smoking_pipe",
            () -> new SmokingPipeItem(Tiers.IRON, 3, -2.7F, new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> PHONE = ITEMS.register("phone",
            () -> new PhoneItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> TRAIT_ROLLER = ITEMS.register("trait_roller",
            () -> new TraitRollerItem(new Item.Properties().stacksTo(16)));
    public static final RegistryObject<Item> CHAIN = ITEMS.register("chain",
            () -> new ChainItem(new Item.Properties().stacksTo(1)));

    private HunterItems() {
    }

    public static void register(IEventBus modBus) {
        ITEMS.register(modBus);
    }

    @SubscribeEvent
    public static void buildCreativeTabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.SPAWN_EGGS) {
            event.accept(GREAT_STAMP_PIG_SPAWN_EGG);
            event.accept(BANDIT_SPAWN_EGG);
            event.accept(TONPA_SPAWN_EGG);
            event.accept(WING_SPAWN_EGG);
        } else if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(TRAIT_ROLLER);
        }
    }
}
