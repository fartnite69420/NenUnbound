package com.huntercraft.huntercraft.particle;

import com.huntercraft.huntercraft.HunterCraftMod;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class HunterParticles {
    public static final DeferredRegister<net.minecraft.core.particles.ParticleType<?>> PARTICLES =
            DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, HunterCraftMod.MODID);

    public static final RegistryObject<SimpleParticleType> MOREL_SMOKE = PARTICLES.register("morel_smoke",
            () -> new SimpleParticleType(true));
    public static final RegistryObject<SimpleParticleType> SMOKY_JAIL_SMOKE = PARTICLES.register("smoky_jail_smoke",
            () -> new SimpleParticleType(true));
    public static final RegistryObject<SimpleParticleType> SMOKY_CHAIN_SMOKE = PARTICLES.register("smoky_chain_smoke",
            () -> new SimpleParticleType(true));
    public static final RegistryObject<SimpleParticleType> ELASTIC_REFLECT = PARTICLES.register("elastic_reflect",
            () -> new SimpleParticleType(true));

    private HunterParticles() {
    }

    public static void register(IEventBus modBus) {
        PARTICLES.register(modBus);
    }
}
