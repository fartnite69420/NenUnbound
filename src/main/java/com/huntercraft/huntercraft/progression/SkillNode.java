package com.huntercraft.huntercraft.progression;

public enum SkillNode {
    SPEED("speed", Category.WEAPON, "skill.huntercraft.speed", "Mobility-focused techniques and aerial control.", 0x3FB7F5),
    DEFENSE("defense", Category.WEAPON, "skill.huntercraft.defense", "Guard, endurance, and survivability training.", 0xD94B57),
    BOXING("boxing", Category.PHYSICAL, "skill.huntercraft.boxing", "Close-range striking and pressure-based combat.", 0xF5A623),
    MARTIAL_ARTS("martial_arts", Category.PHYSICAL, "skill.huntercraft.martial_arts", "Discipline, counters, and technical forms.", 0x8E5CF6);

    private final String id;
    private final Category category;
    private final String translationKey;
    private final String description;
    private final int color;

    SkillNode(String id, Category category, String translationKey, String description, int color) {
        this.id = id;
        this.category = category;
        this.translationKey = translationKey;
        this.description = description;
        this.color = color;
    }

    public String id() {
        return this.id;
    }

    public String translationKey() {
        return this.translationKey;
    }

    public Category category() {
        return this.category;
    }

    public String description() {
        return this.description;
    }

    public int color() {
        return this.color;
    }

    public static SkillNode byId(String id) {
        for (SkillNode node : values()) {
            if (node.id.equals(id)) {
                return node;
            }
        }
        return null;
    }

    public enum Category {
        WEAPON("weapon", "skill.huntercraft.category.weapon", "Speed and defense-oriented weapon fighting styles."),
        PHYSICAL("physical", "skill.huntercraft.category.physical", "Boxing and martial arts close-combat styles.");

        private final String id;
        private final String translationKey;
        private final String description;

        Category(String id, String translationKey, String description) {
            this.id = id;
            this.translationKey = translationKey;
            this.description = description;
        }

        public String id() {
            return this.id;
        }

        public String translationKey() {
            return this.translationKey;
        }

        public String description() {
            return this.description;
        }

        public static Category byId(String id) {
            for (Category category : values()) {
                if (category.id.equals(id)) {
                    return category;
                }
            }
            return null;
        }
    }
}
