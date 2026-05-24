package com.huntercraft.huntercraft.data;

import java.util.Arrays;

public enum TraitType {
    NONE("none", "None"),
    SCARLET_EYES("scarlet_eyes", "Scarlet Eyes");

    private final String id;
    private final String displayName;

    TraitType(String id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    public String id() {
        return this.id;
    }

    public String displayName() {
        return this.displayName;
    }

    public static TraitType byId(String id) {
        if (id == null || id.isBlank()) {
            return NONE;
        }
        return Arrays.stream(values())
                .filter(value -> value.id.equalsIgnoreCase(id) || value.name().equalsIgnoreCase(id))
                .findFirst()
                .orElse(NONE);
    }
}
