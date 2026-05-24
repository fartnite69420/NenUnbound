package com.huntercraft.huntercraft.quest;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class QuestRegistry {
    private static final Map<String, QuestDefinition> QUESTS = new LinkedHashMap<>();

    public static final QuestDefinition ZOMBIE_HUNT = register(new QuestDefinition(
            "zombie_hunt",
            "quest.huntercraft.zombie_hunt",
            "quest.huntercraft.zombie_hunt.desc",
            QuestObjectiveType.KILL_ENTITY,
            "minecraft:zombie",
            5,
            100
    ));

    public static final QuestDefinition IRON_SUPPLY = register(new QuestDefinition(
            "iron_supply",
            "quest.huntercraft.iron_supply",
            "quest.huntercraft.iron_supply.desc",
            QuestObjectiveType.COLLECT_ITEM,
            "minecraft:iron_ingot",
            3,
            75
    ));
    public static final QuestDefinition PIG_HUNTING = register(new QuestDefinition(
            "pig_hunting",
            "quest.huntercraft.phone.pig_hunting",
            "quest.huntercraft.phone.pig_hunting.desc",
            QuestObjectiveType.KILL_ENTITY,
            "huntercraft:great_stamp_pig",
            5,
            1000
    ));
    public static final QuestDefinition WEAPON_CREATION = register(new QuestDefinition(
            "weapon_creation",
            "quest.huntercraft.phone.weapon_creation",
            "quest.huntercraft.phone.weapon_creation.desc",
            QuestObjectiveType.COLLECT_ITEM,
            "minecraft:iron_sword",
            1,
            500
    ));
    public static final QuestDefinition ITEM_RETRIEVAL = register(new QuestDefinition(
            "item_retrieval",
            "quest.huntercraft.phone.item_retrieval",
            "quest.huntercraft.phone.item_retrieval.desc",
            QuestObjectiveType.COLLECT_ITEM,
            "minecraft:string",
            12,
            650
    ));
    public static final QuestDefinition ITEM_DELIVERY = register(new QuestDefinition(
            "item_delivery",
            "quest.huntercraft.phone.item_delivery",
            "quest.huntercraft.phone.item_delivery.desc",
            QuestObjectiveType.COLLECT_ITEM,
            "minecraft:bread",
            8,
            850
    ));
    public static final QuestDefinition BANDIT_BEATING = register(new QuestDefinition(
            "bandit_beating",
            "quest.huntercraft.phone.bandit_beating",
            "quest.huntercraft.phone.bandit_beating.desc",
            QuestObjectiveType.KILL_ENTITY,
            "huntercraft:bandit",
            6,
            1400
    ));
    public static final QuestDefinition PACKAGE_PICKUP = register(new QuestDefinition(
            "package_pickup",
            "quest.huntercraft.phone.package_pickup",
            "quest.huntercraft.phone.package_pickup.desc",
            QuestObjectiveType.COLLECT_ITEM,
            "minecraft:paper",
            1,
            1700
    ));

    public static final List<QuestDefinition> STARTER_QUESTS = List.of(ZOMBIE_HUNT, IRON_SUPPLY);

    private QuestRegistry() {
    }

    private static QuestDefinition register(QuestDefinition definition) {
        QUESTS.put(definition.id(), definition);
        return definition;
    }

    public static QuestDefinition byId(String id) {
        return QUESTS.get(id);
    }

    public static List<QuestDefinition> all() {
        return List.copyOf(QUESTS.values());
    }

    public static int getScaledTargetCount(QuestDefinition definition, int level) {
        int levelTier = Math.max(0, (level - 1) / 20);
        int scaled = definition.targetCount() + levelTier;
        if (definition.objectiveType() == QuestObjectiveType.COLLECT_ITEM) {
            scaled += Math.max(0, (level - 1) / 35);
        }
        return Math.max(1, scaled);
    }

    public static int getScaledRewardXp(QuestDefinition definition, int level) {
        double multiplier = 1.0D + (Math.max(0, level - 1) * 0.035D);
        return Math.max(1, (int) Math.round(definition.rewardXp() * multiplier));
    }
}
