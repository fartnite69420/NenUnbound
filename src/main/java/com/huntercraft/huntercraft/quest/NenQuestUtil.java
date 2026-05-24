package com.huntercraft.huntercraft.quest;

import com.huntercraft.huntercraft.data.HunterPlayerData;
import net.minecraft.network.chat.Component;

public final class NenQuestUtil {
    public static final int FEEL_AURA_TICKS_REQUIRED = 20 * 30;
    public static final int TEN_ENDURE_TICKS_REQUIRED = 20 * 12;
    public static final int ZETSU_HIDE_PREP_TICKS = 20 * 30;
    public static final int ZETSU_HIDE_SEARCH_TICKS = 20 * 120;
    public static final int ZETSU_HIDE_RADIUS = 100;
    public static final float REN_OVERFLOW_DAMAGE_REQUIRED = 80.0F;
    public static final int REN_OVERFLOW_WINDOW_TICKS = 20 * 6;
    public static final int AURA_BURST_KILLS_REQUIRED = 3;
    public static final int AURA_BURST_WINDOW_TICKS = 20 * 8;
    public static final int EN_LOOK_REQUIRED = 20;
    public static final int SHU_REN_WEAPON_TICKS_REQUIRED = 20 * 8;
    public static final int KO_ONE_SHOT_REQUIRED = 10;
    public static final int KEN_BALANCE_TICKS_REQUIRED = 20 * 18;

    private NenQuestUtil() {
    }

    public static Component getTrainerTitle(boolean zushi) {
        return Component.literal("Wing - Nen Training");
    }

    public static Component getStageName(NenQuestStage stage) {
        return switch (stage) {
            case FEEL_THE_AURA -> Component.literal("Feel the Aura");
            case OPEN_THE_NODES -> Component.literal("Open the Nodes");
            case TEN_ENDURE -> Component.literal("Endure");
            case ZETSU_DISAPPEAR -> Component.literal("Disappear");
            case REN_OVERFLOW -> Component.literal("Overflow");
            case REN_AURA_BURST -> Component.literal("Aura Burst");
            case HATSU_CHOOSE_TYPE -> Component.literal("Choose Your Hatsu");
            case EN_LOOK -> Component.literal("Base En");
            case SHU_REN_WEAPON -> Component.literal("Shu");
            case KO_ONE_SHOT -> Component.literal("Ko");
            case KEN_BALANCE -> Component.literal("Balance Power and Defense");
            case RYU_SHIFT_AURA -> Component.literal("Shift Your Aura");
            case COMPLETED -> Component.literal("Nen Training Complete");
            default -> Component.literal("Awakening Nen");
        };
    }

    public static Component getDialogue(boolean zushi, NenQuestStage stage) {
        return switch (stage) {
            case NOT_STARTED -> Component.literal("Wing: Nen is not a party trick. It is the shape of your life spilling into the world. If you want it, begin by proving you can quiet yourself enough to hear it.");
            case FEEL_THE_AURA -> Component.literal("Wing: Sit. Breathe. Let the body disappear. When the pulses come, answer them before thought gets in the way. Aura listens to a quiet mind, not a restless one.");
            case OPEN_THE_NODES -> Component.literal("Wing: Good. Now I force the nodes open. It will hurt. Let it. A hunter who fears pain never learns anything lasting.");
            case TEN_ENDURE -> Component.literal("Wing: Ten is calm under pressure. Bleeding, cornered, breathless... and still holding your aura together. Do not survive by accident. Survive on purpose.");
            case ZETSU_DISAPPEAR -> Component.literal("Wing: Zetsu is disappearance. I will give you thirty seconds to hide. After that I hunt for two full minutes. If I see you even once, the lesson starts over.");
            case REN_OVERFLOW -> Component.literal("Wing: Ren is output. Stop dripping aura like a leak and force it outward like a flood. Commit to the strike before doubt answers back.");
            case REN_AURA_BURST -> Component.literal("Wing: Better. Pressure is only real if you can sustain it through several fights in a row. Keep the surge alive and finish before your nerve collapses.");
            case HATSU_CHOOSE_TYPE -> Component.literal("Wing: Every aura leans somewhere. Strengthening. Emitting. Changing. Conjuring. Controlling. Choose the direction your Hatsu will naturally grow toward.");
            case EN_LOOK -> Component.literal("Wing: En begins with attention. Do not stare through life. Feel where it presses back. Let your awareness touch what breathes around you.");
            case SHU_REN_WEAPON -> Component.literal("Wing: Good. Now carry that aura into what you hold. A weapon coated in intent stops being steel and starts becoming part of you.");
            case KO_ONE_SHOT -> Component.literal("Wing: Ko is all your greed in one point. Tremendous force, terrible defense. If your timing is late, you lose everything for nothing.");
            case KEN_BALANCE -> Component.literal("Wing: Ten and Ren together. Hold defense and pressure at once. Wasteful, difficult, exhausting... and exactly why it matters.");
            case RYU_SHIFT_AURA -> Component.literal("Wing: Enough drills. Ryu only becomes real in motion. Fight me, shift your aura when it matters, and prove you can think while everything hurts.");
            case COMPLETED -> Component.literal("Wing: Good. Now your Nen has structure. Not mastery, not even close... but structure. That is enough to start becoming dangerous.");
        };
    }

    public static Component getObjective(HunterPlayerData data) {
        return switch (data.getNenQuestStage()) {
            case NOT_STARTED -> Component.literal("Speak with Wing to begin Nen training.");
            case FEEL_THE_AURA -> Component.literal("Meditate for 30 seconds and answer each aura pulse within 0.75 seconds.");
            case OPEN_THE_NODES -> Component.literal("Let Wing force your aura nodes open.");
            case TEN_ENDURE -> Component.literal("Stay at low health without dying.");
            case ZETSU_DISAPPEAR -> Component.literal("Hide from Wing. You get 30 seconds to vanish, then survive his search for 2 minutes.");
            case REN_OVERFLOW -> Component.literal("Deal heavy damage in a short burst.");
            case REN_AURA_BURST -> Component.literal("Kill multiple enemies in quick succession.");
            case HATSU_CHOOSE_TYPE -> Component.literal("Choose the Nen type you want to follow.");
            case EN_LOOK -> Component.literal("Look at 20 living mobs.");
            case SHU_REN_WEAPON -> Component.literal("Fight with a weapon while Ren is active.");
            case KO_ONE_SHOT -> Component.literal("One-shot 10 mobs in a single hit.");
            case KEN_BALANCE -> Component.literal("Maintain both Ten and Ren under pressure.");
            case RYU_SHIFT_AURA -> Component.literal("Spar with Wing until one of you reaches 25% health.");
            case COMPLETED -> Component.literal("You have completed the current Nen training line.");
        };
    }

    public static Component getProgress(HunterPlayerData data) {
        return switch (data.getNenQuestStage()) {
            case FEEL_THE_AURA -> {
                if (data.isMeditationActive()) {
                    yield Component.literal((data.getFeelAuraTicks() / 20) + " / 30s  |  " + data.getMeditationPromptKey());
                }
                yield Component.literal(data.getFeelAuraTicks() / 20 + " / 30s");
            }
            case OPEN_THE_NODES -> Component.literal(data.hasNodeDamageTaken() ? "Ready" : "Not done");
            case TEN_ENDURE -> Component.literal(data.getEndureLowHpTicks() / 20 + " / " + (TEN_ENDURE_TICKS_REQUIRED / 20) + "s");
            case ZETSU_DISAPPEAR -> {
                if (data.isZetsuTrialComplete()) {
                    yield Component.literal("Trial cleared");
                }
                if (data.isZetsuTrialSearching()) {
                    yield Component.literal("Search time left: " + (data.getZetsuTrialSearchTicks() / 20) + "s");
                }
                if (data.getZetsuTrialPrepTicks() > 0) {
                    yield Component.literal("Hide time left: " + (data.getZetsuTrialPrepTicks() / 20) + "s");
                }
                yield Component.literal("Not started");
            }
            case REN_OVERFLOW -> Component.literal((int) data.getRenOverflowDamageWindow() + " / " + (int) REN_OVERFLOW_DAMAGE_REQUIRED + " dmg");
            case REN_AURA_BURST -> Component.literal(data.getAuraBurstKills() + " / " + AURA_BURST_KILLS_REQUIRED + " kills");
            case HATSU_CHOOSE_TYPE -> Component.literal(data.getNenType() == null ? "No type chosen" : data.getNenType().displayName());
            case EN_LOOK -> Component.literal(data.getEnLookedMobCount() + " / " + EN_LOOK_REQUIRED + " targets");
            case SHU_REN_WEAPON -> Component.literal(data.getShuWeaponRenTicks() / 20 + " / " + (SHU_REN_WEAPON_TICKS_REQUIRED / 20) + "s");
            case KO_ONE_SHOT -> Component.literal(data.getKoOneShotKills() + " / " + KO_ONE_SHOT_REQUIRED + " kills");
            case KEN_BALANCE -> Component.literal(data.getKenBalanceTicks() / 20 + " / " + (KEN_BALANCE_TICKS_REQUIRED / 20) + "s");
            case RYU_SHIFT_AURA -> Component.literal(data.isRyuFightFinished() ? "Trial cleared" : data.isRyuFightStarted() ? "Fight in progress" : "Not started");
            case COMPLETED -> Component.literal("Complete");
            default -> Component.literal("");
        };
    }

    public static Component getStageHint(HunterPlayerData data) {
        return switch (data.getNenQuestStage()) {
            case FEEL_THE_AURA -> Component.literal("Press the shown W, A, S, or D prompt before the 0.75 second window closes. Missing even one pulse restarts the meditation.");
            case OPEN_THE_NODES -> Component.literal("Talk to Wing and let him strike you once to force the aura nodes open.");
            case TEN_ENDURE -> Component.literal("Drop into the danger zone and stay alive without panic.");
            case ZETSU_DISAPPEAR -> Component.literal("Stay inside the 100 block trial zone. Wing only needs line of sight once to catch you.");
            case REN_OVERFLOW -> Component.literal("You need one strong burst, not scattered damage over a long fight.");
            case REN_AURA_BURST -> Component.literal("Chain kills together before the burst timer expires.");
            case HATSU_CHOOSE_TYPE -> Component.literal("Specialization stays locked for now. Pick one of the five standard paths.");
            case EN_LOOK -> Component.literal("Center mobs in your view and keep scanning. New targets count best.");
            case SHU_REN_WEAPON -> Component.literal("Keep Ren active while attacking with a weapon in hand.");
            case KO_ONE_SHOT -> Component.literal("This only counts when a target dies in one hit.");
            case KEN_BALANCE -> Component.literal("Keep both Ten and Ren flowing long enough to stabilize the strain.");
            case RYU_SHIFT_AURA -> Component.literal("The trial ends when either fighter reaches quarter health. Both of you heal after.");
            case COMPLETED -> Component.literal("Wing has no more foundational drills for you right now.");
            default -> Component.literal("Speak with Wing when you're ready to begin.");
        };
    }

    public static Component getRewardLine(HunterPlayerData data) {
        return switch (data.getNenQuestStage()) {
            case FEEL_THE_AURA -> Component.literal("Reward on clear: access to node opening.");
            case OPEN_THE_NODES -> Component.literal("Reward on clear: Nen Level 1 and Gyo.");
            case TEN_ENDURE -> Component.literal("Reward on clear: Ten unlocked.");
            case ZETSU_DISAPPEAR -> Component.literal("Reward on clear: Zetsu unlocked.");
            case REN_OVERFLOW -> Component.literal("Reward on clear: access to Aura Burst.");
            case REN_AURA_BURST -> Component.literal("Reward on clear: Ren unlocked.");
            case HATSU_CHOOSE_TYPE -> Component.literal("Reward on clear: your Nen type is set.");
            case EN_LOOK -> Component.literal("Reward on clear: En unlocked.");
            case SHU_REN_WEAPON -> Component.literal("Reward on clear: Shu unlocked.");
            case KO_ONE_SHOT -> Component.literal("Reward on clear: Ko unlocked.");
            case KEN_BALANCE -> Component.literal("Reward on clear: Ken unlocked.");
            case RYU_SHIFT_AURA -> Component.literal("Reward on clear: Ryu unlocked and Nen Level 2.");
            case COMPLETED -> Component.literal("Reward earned: foundational Nen training complete.");
            default -> Component.literal("Reward on clear: the first Nen lesson begins.");
        };
    }

    public static Component getHatsuDescription(NenType type) {
        return switch (type) {
            case ENHANCEMENT -> Component.literal("Strengthens the self or what you hold.");
            case EMISSION -> Component.literal("Projects aura away from the body cleanly.");
            case TRANSMUTATION -> Component.literal("Changes aura so it behaves like something else.");
            case CONJURATION -> Component.literal("Forms aura into created tools or objects.");
            case MANIPULATION -> Component.literal("Controls and guides people or things.");
            case SPECIALIZATION -> Component.literal("A rare path outside the normal pattern.");
        };
    }

    public static boolean isStageComplete(HunterPlayerData data) {
        return switch (data.getNenQuestStage()) {
            case NOT_STARTED -> false;
            case FEEL_THE_AURA -> data.getFeelAuraTicks() >= FEEL_AURA_TICKS_REQUIRED;
            case OPEN_THE_NODES -> data.hasNodeDamageTaken();
            case TEN_ENDURE -> data.getEndureLowHpTicks() >= TEN_ENDURE_TICKS_REQUIRED;
            case ZETSU_DISAPPEAR -> data.isZetsuTrialComplete();
            case REN_OVERFLOW -> data.getRenOverflowDamageWindow() >= REN_OVERFLOW_DAMAGE_REQUIRED;
            case REN_AURA_BURST -> data.getAuraBurstKills() >= AURA_BURST_KILLS_REQUIRED;
            case HATSU_CHOOSE_TYPE -> data.getNenType() != null;
            case EN_LOOK -> data.getEnLookedMobCount() >= EN_LOOK_REQUIRED;
            case SHU_REN_WEAPON -> data.getShuWeaponRenTicks() >= SHU_REN_WEAPON_TICKS_REQUIRED;
            case KO_ONE_SHOT -> data.getKoOneShotKills() >= KO_ONE_SHOT_REQUIRED;
            case KEN_BALANCE -> data.getKenBalanceTicks() >= KEN_BALANCE_TICKS_REQUIRED;
            case RYU_SHIFT_AURA -> data.isRyuFightFinished();
            case COMPLETED -> true;
        };
    }
}
