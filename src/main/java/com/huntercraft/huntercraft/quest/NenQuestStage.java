package com.huntercraft.huntercraft.quest;

public enum NenQuestStage {
    NOT_STARTED,
    FEEL_THE_AURA,
    OPEN_THE_NODES,
    TEN_ENDURE,
    ZETSU_DISAPPEAR,
    REN_OVERFLOW,
    REN_AURA_BURST,
    HATSU_CHOOSE_TYPE,
    EN_LOOK,
    SHU_REN_WEAPON,
    KO_ONE_SHOT,
    KEN_BALANCE,
    RYU_SHIFT_AURA,
    COMPLETED;

    public static NenQuestStage byName(String name) {
        for (NenQuestStage stage : values()) {
            if (stage.name().equalsIgnoreCase(name)) {
                return stage;
            }
        }
        return NOT_STARTED;
    }
}
