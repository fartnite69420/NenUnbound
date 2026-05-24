package com.huntercraft.huntercraft.entity;

import com.huntercraft.huntercraft.HunterCraftMod;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = HunterCraftMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class HunterEntityEvents {
    private HunterEntityEvents() {
    }

    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(HunterEntityTypes.GREAT_STAMP_PIG.get(), GreatStampPigEntity.createAttributes().build());
        event.put(HunterEntityTypes.BANDIT.get(), BanditEntity.createAttributes().build());
        event.put(HunterEntityTypes.TONPA.get(), TonpaEntity.createAttributes().build());
        event.put(HunterEntityTypes.WING.get(), WingEntity.createAttributes().build());
        event.put(HunterEntityTypes.SMOKE_SOLDIER.get(), SmokeSoldierEntity.createAttributes().build());
        event.put(HunterEntityTypes.SMOKE_CLONE.get(), SmokeCloneEntity.createAttributes().build());
    }

    @SubscribeEvent
    public static void registerSpawnPlacements(SpawnPlacementRegisterEvent event) {
        event.register(HunterEntityTypes.GREAT_STAMP_PIG.get(),
                SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                GreatStampPigEntity::canSpawn,
                SpawnPlacementRegisterEvent.Operation.OR);
        event.register(HunterEntityTypes.BANDIT.get(),
                SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                BanditEntity::canSpawn,
                SpawnPlacementRegisterEvent.Operation.OR);
    }
}
