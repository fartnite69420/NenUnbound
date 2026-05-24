package com.huntercraft.huntercraft.animation;

public enum AnimationType {
    NONE(0),
    MEDITATE(0),
    SMOKING_PIPE(0),
    SMOKE_SOLDIER_SUMMON(10),
    DASH(8),
    DOUBLE_JUMP(10),
    FLASH_CLEAVE_ONE(6),
    FLASH_CLEAVE_TWO(6),
    FLASH_CLEAVE_THREE(6),
    HEAVEN_SPLITTER(8),
    SKYBREAKER_ASCENT(10),
    SKYBREAKER_DROP(10),
    AEGIS_RUSH(14),
    MIRROR_REPRISAL_GUARD(20),
    MIRROR_REPRISAL_STRIKE(8),
    STEEL_TATSUMAKI(24),
    BOXER_JAB(6),
    BOXER_BARRAGE(8),
    BOXER_BARRAGE_FAST(4),
    BOXER_HAMMER_STRIKE(7),
    BOXER_COUNTER_GUARD(10),
    BOXER_COUNTER_STRIKE(8),
    BOXER_REDIRECTION(12),
    BOXER_GRAB_LIFT(10),
    BOXER_GRAB_SLAM(10),
    MARTIAL_METEOR_HEEL(10),
    MARTIAL_METEOR_HEEL_IMPACT(8),
    MARTIAL_ANKLE_SPLITTER(8),
    MARTIAL_FACE_JAB(6),
    MARTIAL_WHIRLWIND_ARC(12),
    MARTIAL_AIR_BARRAGE(12),
    MARTIAL_RISING_SHOT_CHARGE(10),
    MARTIAL_RISING_SHOT(10),
    MARTIAL_TORA_HUNT_CHARGE(15),
    MARTIAL_TORA_HUNT(14),
    LION_FANG_DRAW_CHARGE(15),
    VOID_REND_CHARGE(20),
    VOID_REND(8),
    GUARD(0),
    PARRY(5),
    WEAPON_PARRY(5),
    ELASTIC_REFLECT(0),
    DOWSING_CHAIN_SWING(14);

    private final int durationTicks;

    AnimationType(int durationTicks) {
        this.durationTicks = durationTicks;
    }

    public int durationTicks() {
        return this.durationTicks;
    }

    public static AnimationType byName(String name) {
        for (AnimationType type : values()) {
            if (type.name().equals(name)) {
                return type;
            }
        }
        return NONE;
    }
}
