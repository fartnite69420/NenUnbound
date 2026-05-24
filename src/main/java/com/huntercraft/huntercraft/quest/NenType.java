package com.huntercraft.huntercraft.quest;

public enum NenType {
    ENHANCEMENT("Enhancement", 0x4CA63A),
    EMISSION("Emission", 0xE0C12C),
    TRANSMUTATION("Transmutation", 0xB44CE0),
    CONJURATION("Conjuration", 0xD93A3A),
    MANIPULATION("Manipulation", 0xE6E6E6),
    SPECIALIZATION("Specialization", 0x4A83E0);

    private final String displayName;
    private final int color;

    NenType(String displayName, int color) {
        this.displayName = displayName;
        this.color = color;
    }

    public String displayName() {
        return this.displayName;
    }

    public int color() {
        return this.color;
    }

    public static NenType byName(String name) {
        NenType type = byNameOrNull(name);
        return type == null ? ENHANCEMENT : type;
    }

    public static NenType byNameOrNull(String name) {
        for (NenType type : values()) {
            if (type.name().equalsIgnoreCase(name)) {
                return type;
            }
        }
        return null;
    }
}
