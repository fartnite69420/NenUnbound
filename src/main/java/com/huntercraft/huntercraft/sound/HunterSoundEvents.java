package com.huntercraft.huntercraft.sound;

import com.huntercraft.huntercraft.HunterCraftMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class HunterSoundEvents {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, HunterCraftMod.MODID);

    public static final RegistryObject<SoundEvent> PARRY = SOUND_EVENTS.register("parry",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(HunterCraftMod.MODID, "parry")));
    public static final RegistryObject<SoundEvent> SLASH = SOUND_EVENTS.register("slash",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(HunterCraftMod.MODID, "slash")));
    public static final RegistryObject<SoundEvent> PUNCH = SOUND_EVENTS.register("punch",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(HunterCraftMod.MODID, "punch")));
    public static final RegistryObject<SoundEvent> DASH = SOUND_EVENTS.register("dash",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(HunterCraftMod.MODID, "dash")));
    public static final RegistryObject<SoundEvent> TELEPORT = SOUND_EVENTS.register("teleport",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(HunterCraftMod.MODID, "teleport")));
    public static final RegistryObject<SoundEvent> GROUND_SMASH = SOUND_EVENTS.register("ground_smash",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(HunterCraftMod.MODID, "ground_smash")));

    private HunterSoundEvents() {
    }

    public static void register(IEventBus modBus) {
        SOUND_EVENTS.register(modBus);
    }
}
