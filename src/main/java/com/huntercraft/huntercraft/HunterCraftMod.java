package com.huntercraft.huntercraft;

import com.huntercraft.huntercraft.client.HunterCraftClient;
import com.huntercraft.huntercraft.data.HunterPlayerData;
import com.huntercraft.huntercraft.entity.HunterEntityTypes;
import com.huntercraft.huntercraft.effect.HunterMobEffects;
import com.huntercraft.huntercraft.event.CommonEvents;
import com.huntercraft.huntercraft.item.HunterCreativeTabs;
import com.huntercraft.huntercraft.item.HunterItems;
import com.huntercraft.huntercraft.network.HunterNetwork;
import com.huntercraft.huntercraft.particle.HunterParticles;
import com.huntercraft.huntercraft.sound.HunterSoundEvents;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(HunterCraftMod.MODID)
public class HunterCraftMod {
    public static final String MODID = "huntercraft";
    public static final Logger LOGGER = LogManager.getLogger();

    public HunterCraftMod() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener(this::commonSetup);
        modBus.addListener(this::registerCapabilities);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, HunterConfig.COMMON_SPEC);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, HunterConfig.CLIENT_SPEC);
        HunterEntityTypes.register(modBus);
        HunterCreativeTabs.register(modBus);
        HunterItems.register(modBus);
        HunterMobEffects.register(modBus);
        HunterParticles.register(modBus);
        HunterSoundEvents.register(modBus);

        MinecraftForge.EVENT_BUS.register(new CommonEvents());
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> HunterCraftClient.init(modBus));
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(HunterNetwork::register);
    }

    private void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.register(HunterPlayerData.class);
    }
}
