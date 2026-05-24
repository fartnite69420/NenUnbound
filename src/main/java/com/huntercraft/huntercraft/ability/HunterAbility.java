package com.huntercraft.huntercraft.ability;

import com.huntercraft.huntercraft.abilities.AbilitySourceType;

import java.util.EnumSet;
import java.util.Set;

public class HunterAbility {
    private final String id;
    private final String displayName;
    private final String description;
    private final String iconPath;
    private final boolean baseTechnique;
    private final EnumSet<AbilitySourceType> sourceTypes;

    public HunterAbility(String id, String displayName, String description, String iconPath, boolean baseTechnique) {
        this(id, displayName, description, iconPath, baseTechnique, new AbilitySourceType[0]);
    }

    public HunterAbility(String id, String displayName, String description, String iconPath, boolean baseTechnique, AbilitySourceType... sourceTypes) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
        this.iconPath = iconPath;
        this.baseTechnique = baseTechnique;
        this.sourceTypes = sourceTypes.length == 0 ? EnumSet.noneOf(AbilitySourceType.class) : EnumSet.of(sourceTypes[0], sourceTypes);
    }

    public String id() {
        return this.id;
    }

    public String displayName() {
        return this.displayName;
    }

    public String description() {
        return this.description;
    }

    public String iconPath() {
        return this.iconPath;
    }

    public boolean baseTechnique() {
        return this.baseTechnique;
    }

    public Set<AbilitySourceType> sourceTypes() {
        return Set.copyOf(this.sourceTypes);
    }

    public boolean hasSourceType(AbilitySourceType sourceType) {
        return this.sourceTypes.contains(sourceType);
    }

    public boolean isNenSource() {
        return this.hasSourceType(AbilitySourceType.NEN);
    }
}
