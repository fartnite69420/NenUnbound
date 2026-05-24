package com.huntercraft.huntercraft.effect;

import com.huntercraft.huntercraft.HunterCraftMod;
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class HunterMobEffects {
    public static final DeferredRegister<MobEffect> MOB_EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, HunterCraftMod.MODID);

    public static final RegistryObject<MobEffect> STUNNED = MOB_EFFECTS.register("stunned", StunnedEffect::new);
    public static final RegistryObject<MobEffect> PARRY_STUNNED = MOB_EFFECTS.register("parry_stunned", ParryStunnedEffect::new);
    public static final RegistryObject<MobEffect> CHAIN_JAIL = MOB_EFFECTS.register("chain_jail", ChainJailEffect::new);
    public static final RegistryObject<MobEffect> DOWSING_CHAINED = MOB_EFFECTS.register("dowsing_chained", DowsingChainedEffect::new);
    public static final RegistryObject<MobEffect> ZETSU = MOB_EFFECTS.register("zetsu", ZetsuEffect::new);

    private HunterMobEffects() {
    }

    public static void register(IEventBus modBus) {
        MOB_EFFECTS.register(modBus);
    }
}
