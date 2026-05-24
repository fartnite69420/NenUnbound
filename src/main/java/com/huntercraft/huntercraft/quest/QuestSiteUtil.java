package com.huntercraft.huntercraft.quest;

import com.huntercraft.huntercraft.entity.BanditEntity;
import com.huntercraft.huntercraft.entity.GreatStampPigEntity;
import com.huntercraft.huntercraft.entity.HunterEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.registries.ForgeRegistries;

public final class QuestSiteUtil {
    private QuestSiteUtil() {
    }

    public static BlockPos createQuestSite(ServerPlayer player, QuestDefinition quest, int targetCount) {
        ServerLevel level = player.serverLevel();
        BlockPos base = findQuestSite(level, player.blockPosition());
        BlockPos site = base.above();

        switch (quest.id()) {
            case "pig_hunting" -> spawnGreatStampPigs(level, site, targetCount);
            case "bandit_beating" -> spawnBandits(level, site, targetCount);
            case "package_pickup" -> {
                createLootChest(level, site, quest.targetId(), targetCount);
                spawnBandits(level, site.offset(5, 0, 5), Math.max(2, targetCount / 2));
            }
            case "item_retrieval", "item_delivery", "weapon_creation" -> createLootChest(level, site, quest.targetId(), targetCount);
            default -> {
            }
        }

        return site;
    }

    private static BlockPos findQuestSite(ServerLevel level, BlockPos origin) {
        for (int attempt = 0; attempt < 24; attempt++) {
            double angle = level.random.nextDouble() * (Math.PI * 2.0D);
            int distance = 140 + level.random.nextInt(120);
            int x = origin.getX() + (int) Math.round(Math.cos(angle) * distance);
            int z = origin.getZ() + (int) Math.round(Math.sin(angle) * distance);
            BlockPos top = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, new BlockPos(x, origin.getY(), z));
            if (!level.getBlockState(top.below()).isSolid()) {
                continue;
            }
            return top;
        }
        return level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, origin);
    }

    private static void spawnGreatStampPigs(ServerLevel level, BlockPos site, int count) {
        for (int i = 0; i < count; i++) {
            GreatStampPigEntity pig = HunterEntityTypes.GREAT_STAMP_PIG.get().create(level);
            if (pig == null) {
                continue;
            }
            BlockPos spawnPos = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, site.offset(level.random.nextInt(12) - 6, 0, level.random.nextInt(12) - 6));
            pig.moveTo(spawnPos.getX() + 0.5D, spawnPos.getY() + 1.0D, spawnPos.getZ() + 0.5D, level.random.nextFloat() * 360.0F, 0.0F);
            pig.finalizeSpawn(level, level.getCurrentDifficultyAt(spawnPos), MobSpawnType.EVENT, null, null);
            pig.setPersistenceRequired();
            level.addFreshEntity(pig);
        }
    }

    private static void spawnBandits(ServerLevel level, BlockPos site, int count) {
        for (int i = 0; i < count; i++) {
            BanditEntity bandit = HunterEntityTypes.BANDIT.get().create(level);
            if (bandit == null) {
                continue;
            }
            BlockPos spawnPos = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, site.offset(level.random.nextInt(10) - 5, 0, level.random.nextInt(10) - 5));
            bandit.moveTo(spawnPos.getX() + 0.5D, spawnPos.getY() + 1.0D, spawnPos.getZ() + 0.5D, level.random.nextFloat() * 360.0F, 0.0F);
            bandit.finalizeSpawn(level, level.getCurrentDifficultyAt(spawnPos), MobSpawnType.EVENT, null, null);
            bandit.setPersistenceRequired();
            level.addFreshEntity(bandit);
        }
    }

    private static void createLootChest(ServerLevel level, BlockPos site, String targetItemId, int count) {
        if (!level.getBlockState(site).canBeReplaced()) {
            site = site.above();
        }
        level.setBlockAndUpdate(site, Blocks.CHEST.defaultBlockState());
        BlockEntity blockEntity = level.getBlockEntity(site);
        if (!(blockEntity instanceof ChestBlockEntity chest)) {
            return;
        }
        Item item = ForgeRegistries.ITEMS.getValue(ResourceLocation.tryParse(targetItemId));
        if (item == null) {
            return;
        }
        chest.setItem(0, new ItemStack(item, Math.max(1, count)));
        chest.setChanged();
    }
}
