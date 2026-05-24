package com.huntercraft.huntercraft.entity;

import com.huntercraft.huntercraft.abilities.HunterAbilities;
import com.huntercraft.huntercraft.abilities.SkillTreeCombatAbility;
import com.huntercraft.huntercraft.progression.SkillNode;
import net.minecraft.util.RandomSource;

import java.util.Arrays;
import java.util.List;

public enum BanditAbilityProfile {
    FLASH_CLEAVE(HunterAbilities.FLASH_CLEAVE, 28, 12.0F, 1.65F, 0.0F, 0, 0.0F, 0.0F),
    GHOST_STEP(HunterAbilities.GHOST_STEP, 34, 10.0F, 1.55F, 2.5F, 0, 0.0F, 0.0F),
    LION_FANG_DRAW(HunterAbilities.LION_FANG_DRAW, 36, 14.0F, 1.8F, 0.0F, 0, 0.0F, 0.0F),
    VOID_REND(HunterAbilities.VOID_REND, 40, 13.0F, 1.7F, 1.8F, 0, 0.0F, 0.0F),
    PHANTOM_RING(HunterAbilities.PHANTOM_RING, 44, 11.0F, 1.45F, 3.0F, 0, 0.0F, 0.0F),
    HEAVEN_SPLITTER(HunterAbilities.HEAVEN_SPLITTER, 34, 13.0F, 1.5F, 0.0F, 8, 0.35F, 0.0F),
    SKYBREAKER_DIVE(HunterAbilities.SKYBREAKER_DIVE, 46, 15.0F, 1.8F, 0.0F, 8, 0.25F, 0.25F),
    AEGIS_RUSH(HunterAbilities.AEGIS_RUSH, 40, 12.0F, 1.55F, 2.4F, 0, 0.0F, 0.0F),
    MIRROR_REPRISAL(HunterAbilities.MIRROR_REPRISAL, 52, 14.0F, 1.6F, 0.0F, 10, 0.45F, 0.0F),
    STEEL_TATSUMAKI(HunterAbilities.STEEL_TATSUMAKI, 60, 12.5F, 1.35F, 4.0F, 6, 0.28F, 0.0F),
    HAMMER_JAB(HunterAbilities.HAMMER_JAB, 24, 9.0F, 1.45F, 0.0F, 10, 0.18F, 0.0F),
    CROSSFIRE_BARRAGE(HunterAbilities.CROSSFIRE_BARRAGE, 46, 12.0F, 1.1F, 3.2F, 4, 0.0F, 0.0F),
    LIVER_BREAK_COUNTER(HunterAbilities.LIVER_BREAK_COUNTER, 48, 13.0F, 1.65F, 0.0F, 12, 0.2F, 0.0F),
    REDIRECTION_RUSH(HunterAbilities.REDIRECTION_RUSH, 40, 12.5F, 1.6F, 0.0F, 8, 0.32F, 0.0F),
    ATLAS_DROP(HunterAbilities.ATLAS_DROP, 62, 16.0F, 1.9F, 0.0F, 12, 0.5F, 0.0F),
    METEOR_HEEL(HunterAbilities.METEOR_HEEL, 42, 14.0F, 1.7F, 0.0F, 8, 0.3F, 0.55F),
    ANKLE_SPLITTER(HunterAbilities.ANKLE_SPLITTER, 34, 11.0F, 1.45F, 0.0F, 14, 0.2F, 0.0F),
    RISING_SHOT(HunterAbilities.RISING_SHOT, 50, 13.5F, 1.55F, 0.0F, 12, 0.24F, 0.6F),
    WHIRLWIND_ARC(HunterAbilities.WHIRLWIND_ARC, 46, 12.0F, 1.25F, 3.0F, 8, 0.2F, 0.0F),
    FLINGING_GRAB(HunterAbilities.TORA_HUNT, 58, 15.0F, 1.75F, 0.0F, 16, 0.5F, 0.2F);

    private static final List<BanditAbilityProfile> VALUES = Arrays.asList(values());

    private final String abilityId;
    private final String displayName;
    private final SkillNode skillNode;
    private final int cooldownTicks;
    private final float baseDamage;
    private final float lungeStrength;
    private final float areaRadius;
    private final int stunTicks;
    private final float knockbackStrength;
    private final float launchStrength;

    BanditAbilityProfile(SkillTreeCombatAbility ability, int cooldownTicks, float baseDamage, float lungeStrength, float areaRadius, int stunTicks, float knockbackStrength, float launchStrength) {
        this.abilityId = ability.id();
        this.displayName = ability.displayName();
        this.skillNode = ability.skillNode();
        this.cooldownTicks = cooldownTicks;
        this.baseDamage = baseDamage;
        this.lungeStrength = lungeStrength;
        this.areaRadius = areaRadius;
        this.stunTicks = stunTicks;
        this.knockbackStrength = knockbackStrength;
        this.launchStrength = launchStrength;
    }

    public String abilityId() {
        return this.abilityId;
    }

    public String displayName() {
        return this.displayName;
    }

    public SkillNode skillNode() {
        return this.skillNode;
    }

    public int cooldownTicks() {
        return this.cooldownTicks;
    }

    public float baseDamage() {
        return this.baseDamage;
    }

    public float lungeStrength() {
        return this.lungeStrength;
    }

    public float areaRadius() {
        return this.areaRadius;
    }

    public int stunTicks() {
        return this.stunTicks;
    }

    public float knockbackStrength() {
        return this.knockbackStrength;
    }

    public float launchStrength() {
        return this.launchStrength;
    }

    public boolean requiresWeapon() {
        return this.skillNode.category() == SkillNode.Category.WEAPON;
    }

    public static BanditAbilityProfile byId(String id) {
        for (BanditAbilityProfile value : VALUES) {
            if (value.abilityId.equals(id)) {
                return value;
            }
        }
        return FLASH_CLEAVE;
    }

    public static BanditAbilityProfile random(RandomSource random) {
        return VALUES.get(random.nextInt(VALUES.size()));
    }
}
