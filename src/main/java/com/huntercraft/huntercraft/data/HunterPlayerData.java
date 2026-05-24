package com.huntercraft.huntercraft.data;

import com.huntercraft.huntercraft.animation.AnimationType;
import com.huntercraft.huntercraft.abilities.HunterAbilities;
import com.huntercraft.huntercraft.progression.NenTechniqueSkillNode;
import com.huntercraft.huntercraft.progression.SkillNode;
import com.huntercraft.huntercraft.quest.NenQuestStage;
import com.huntercraft.huntercraft.quest.NenType;
import com.huntercraft.huntercraft.quest.PhoneQuestRegistry;
import com.huntercraft.huntercraft.quest.QuestDefinition;
import com.huntercraft.huntercraft.quest.QuestRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class HunterPlayerData {
    public static final int MAX_LEVEL = 90;
    public static final int MAX_SKILL_POINTS = 65;
    public static final int MAX_NEN_LEVEL = 10;
    public static final int NEN_STORAGE_PER_LEVEL = 10_000;
    public static final int MIN_STAMINA = 1_000;
    public static final int MAX_STAMINA = 10_000;
    public static final double MAX_HEALTH_TOTAL = 200.0D;
    public static final long PHONE_QUEST_REFRESH_TICKS = 36_000L;
    public static final int COMBAT_SLOT_COUNT = 8;
    public static final int COMBAT_BAR_COUNT = 2;
    private int level = 1;
    private int xp;
    private int skillPoints = 1;
    private boolean guarding;
    private int guardTicks;
    private int dashIFrameTicks;
    private int airJumpsUsed;
    private boolean airLaunchFallProtection;
    private String currentAnimation = AnimationType.NONE.name();
    private int animationTicks;
    private int nenLevel;
    private int currentNen = MIN_STAMINA;
    private double nenRegenProgress;
    private int staminaRegenDelayTicks;
    private int nenAuraColor = 0x5BE5FF;
    private String nenType = "";
    private String nenTechnique = "";
    private String trait = TraitType.NONE.id();
    private boolean emperorTimeActive;
    private String vowFaction = "";
    private String combatVowType = "";
    private int combatVowPercent = 25;
    private int scarletEyesOffsetX;
    private int scarletEyesOffsetY;
    private int scarletLeftEyeOffsetX = -6;
    private int scarletLeftEyeOffsetY;
    private int scarletLeftEyeLength = -1;
    private int scarletLeftEyeVerticalLength = 1;
    private int scarletRightEyeOffsetX = 6;
    private int scarletRightEyeOffsetY;
    private int scarletRightEyeLength = 1;
    private int scarletRightEyeVerticalLength = 1;
    private int lungCapacity;
    private double deepPurpleLungGainProgress;
    private double deepPurpleDrowningRecoveryProgress;
    private int deepPurpleSoldierMode;
    private int deepPurpleCloneMode;
    private String deepPurpleSpottedTargetUuid = "";
    private BlockPos deepPurpleSpottedTargetPos;
    private int deepPurpleSpottedTargetTicks;
    private String nenQuestStage = NenQuestStage.NOT_STARTED.name();
    private boolean tenUnlocked;
    private boolean zetsuUnlocked;
    private boolean renUnlocked;
    private boolean gyoUnlocked;
    private boolean enUnlocked;
    private boolean shuUnlocked;
    private boolean koUnlocked;
    private boolean kenUnlocked;
    private boolean ryuUnlocked;
    private int feelAuraTicks;
    private int meditationCountdownTicks;
    private boolean meditationActive;
    private int meditationTicksRemaining;
    private String meditationPromptKey = "";
    private int meditationPromptTicksRemaining;
    private int meditationPromptSeed;
    private boolean nodeDamageTaken;
    private int endureLowHpTicks;
    private int zetsuCrouchTicks;
    private int zetsuTrialPrepTicks;
    private int zetsuTrialSearchTicks;
    private boolean zetsuTrialSearching;
    private boolean zetsuTrialComplete;
    private int zetsuTrialWingEntityId = -1;
    private BlockPos zetsuTrialOrigin;
    private float renOverflowDamageWindow;
    private int renOverflowTicks;
    private int auraBurstKills;
    private int auraBurstTicks;
    private int enLookedMobCount;
    private String lastNenLookTargetUuid = "";
    private int nenLookCooldown;
    private int shuWeaponRenTicks;
    private int koOneShotKills;
    private int kenBalanceTicks;
    private boolean ryuFightStarted;
    private boolean ryuFightFinished;
    private boolean tenActive;
    private boolean zetsuActive;
    private boolean renActive;
    private boolean enActive;
    private boolean koActive;
    private boolean kenActive;
    private boolean ryuActive;
    private boolean zetsuForcedInvisibility;
    private String chargingAbilityId = "";
    private int chargeTicksRemaining;
    private double chargeDirectionX;
    private double chargeDirectionY;
    private double chargeDirectionZ;
    private String chargeTargetUuid = "";
    private String activeAbilityId = "";
    private int activeAbilityTicksRemaining;
    private double activeAbilityDirectionX;
    private double activeAbilityDirectionY;
    private double activeAbilityDirectionZ;
    private String activeAbilityTargetUuid = "";
    private String martialArtsGrabTargetUuid = "";
    private String martialArtsGrabSourceAbilityId = "";
    private int martialArtsGrabTicksRemaining;
    private boolean combatBarVisible = true;
    private int activeCombatBar;
    private boolean emptyHandsPickupEnabled;
    private String selectedSkillCategory = "";
    private final Set<String> unlockedNenTechniqueNodes = new HashSet<>();
    private String factionName = "";
    private String playerDisplayName = "";
    private String factionOwnerName = "";
    private String pendingFactionInviteName = "";
    private String pendingFactionInviterName = "";
    private final String[] combatSlots = new String[COMBAT_SLOT_COUNT * COMBAT_BAR_COUNT];
    private final Set<String> factionMembers = new HashSet<>();
    private final Set<String> invitablePlayers = new HashSet<>();
    private final Map<String, Integer> abilityCooldowns = new HashMap<>();
    private final Map<String, Integer> attackerIFrameTicks = new HashMap<>();
    private final Map<String, String> abilityVows = new HashMap<>();
    private final Set<String> judgmentDisabledAbilities = new HashSet<>();
    private String pendingJudgmentChainTargetUuid = "";
    private final Map<String, Integer> skillTreePoints = new HashMap<>();
    private final Set<String> activeQuests = new HashSet<>();
    private final Set<String> completedQuests = new HashSet<>();
    private final Map<String, Integer> questProgress = new HashMap<>();
    private final Map<String, Integer> questTargetCounts = new HashMap<>();
    private final Map<String, Integer> questRewardXp = new HashMap<>();
    private final Map<String, BlockPos> questLocations = new HashMap<>();
    private final Set<String> enTrackedPlayers = new HashSet<>();
    private long phoneQuestRefreshGameTime;

    public int getLevel() {
        return this.level;
    }

    public void setLevel(int level) {
        this.level = Math.max(1, Math.min(MAX_LEVEL, level));
        this.xp = 0;
        this.currentNen = Math.min(this.currentNen, this.getMaxStamina());
        recalculateAvailableSkillPoints();
    }

    public int getXp() {
        return this.xp;
    }

    public int getSkillPoints() {
        return this.skillPoints;
    }

    public int getXpToNextLevel() {
        return 100 + ((this.level - 1) * 50);
    }

    public boolean addXp(int amount) {
        this.xp = Math.max(0, this.xp + amount);
        return false;
    }

    public double getBonusHealth() {
        return ((MAX_HEALTH_TOTAL - 20.0D) / (MAX_LEVEL - 1)) * (this.level - 1);
    }

    public double getPassiveArmor() {
        return (20.0D / (MAX_LEVEL - 1)) * (this.level - 1);
    }

    public float getPassiveToughnessReduction() {
        return this.nenLevel * 0.035F;
    }

    public boolean isGuarding() {
        return this.guarding;
    }

    public void setGuarding(boolean guarding) {
        this.guarding = guarding;
        if (!guarding) {
            this.guardTicks = 0;
        }
    }

    public int getGuardTicks() {
        return this.guardTicks;
    }

    public int getGuardParryTicks() {
        return HunterAbilities.PERFECT_READ.isUnlocked(this)
                ? HunterAbilities.PERFECT_READ.getGuardParryTicks(this)
                : HunterAbilities.BREAKER_FISTS.getGuardParryTicks(this);
    }

    public boolean isGuardParrying() {
        return this.guarding && this.guardTicks <= this.getGuardParryTicks();
    }

    public float getGuardReduction() {
        if (!this.guarding) {
            return 0.0F;
        }
        if (this.guardTicks <= 10) {
            return 1.0F;
        }
        int guardParryTicks = this.getGuardParryTicks();
        float guardFalloff = HunterAbilities.IRON_GUARD.getGuardFalloffPerSecond(this);
        int ticksAfterInvuln = Math.max(0, this.guardTicks - guardParryTicks - 1);
        float reductionLoss = (ticksAfterInvuln / 20.0F) * guardFalloff;
        return Math.max(0.0F, 0.60F - reductionLoss);
    }

    public void tickGuard() {
        if (this.guarding) {
            this.guardTicks++;
        }
    }

    public int getAbilityCooldown(String abilityId) {
        return this.abilityCooldowns.getOrDefault(abilityId, 0);
    }

    public void setAbilityCooldown(String abilityId, int cooldownTicks) {
        if (cooldownTicks <= 0) {
            this.abilityCooldowns.remove(abilityId);
        } else {
            this.abilityCooldowns.put(abilityId, cooldownTicks);
        }
    }

    public int getDashIFrameTicks() {
        return this.dashIFrameTicks;
    }

    public void setDashIFrameTicks(int dashIFrameTicks) {
        this.dashIFrameTicks = dashIFrameTicks;
    }

    public Map<String, Integer> getAbilityCooldowns() {
        return this.abilityCooldowns;
    }

    public boolean isJudgmentDisabledAbility(String abilityId) {
        return abilityId != null && this.judgmentDisabledAbilities.contains(abilityId);
    }

    public void addJudgmentDisabledAbility(String abilityId) {
        if (abilityId != null && !abilityId.isBlank()) {
            this.judgmentDisabledAbilities.add(abilityId);
        }
    }

    public void clearJudgmentDisabledAbilities() {
        this.judgmentDisabledAbilities.clear();
    }

    public Set<String> getJudgmentDisabledAbilities() {
        return this.judgmentDisabledAbilities;
    }

    public String getPendingJudgmentChainTargetUuid() {
        return this.pendingJudgmentChainTargetUuid;
    }

    public void setPendingJudgmentChainTargetUuid(String targetUuid) {
        this.pendingJudgmentChainTargetUuid = targetUuid == null ? "" : targetUuid;
    }

    public void clearPendingJudgmentChainTarget() {
        this.pendingJudgmentChainTargetUuid = "";
    }

    public int getAirJumpsUsed() {
        return this.airJumpsUsed;
    }

    public void setAirJumpsUsed(int airJumpsUsed) {
        this.airJumpsUsed = airJumpsUsed;
    }

    public boolean hasAirLaunchFallProtection() {
        return this.airLaunchFallProtection;
    }

    public void setAirLaunchFallProtection(boolean airLaunchFallProtection) {
        this.airLaunchFallProtection = airLaunchFallProtection;
    }

    public AnimationType getCurrentAnimation() {
        return AnimationType.byName(this.currentAnimation);
    }

    public int getAnimationTicks() {
        return this.animationTicks;
    }

    public int getNenLevel() {
        return this.nenLevel;
    }

    public void setNenLevel(int nenLevel) {
        this.nenLevel = Math.max(0, Math.min(MAX_NEN_LEVEL, nenLevel));
        this.currentNen = Math.min(this.currentNen, this.getMaxStamina());
        this.lungCapacity = Math.min(this.lungCapacity, this.getMaxLungCapacity());
        if (this.currentNen <= 0) {
            this.currentNen = this.getMaxStamina();
        }
        if (!this.hasNen()) {
            this.disableAllNen();
        }
        // Auto-unlock any technique nodes the player now qualifies for
        this.autoUnlockNenTechniqueNodes();
    }

    public boolean hasNen() {
        return this.nenLevel > 0;
    }

    public int getMaxNen() {
        return this.getMaxStamina();
    }

    public int getCurrentNen() {
        return this.getCurrentStamina();
    }

    public void setCurrentNen(int currentNen) {
        this.setCurrentStamina(currentNen);
    }

    public int getMaxStamina() {
        double progress = MAX_LEVEL <= 1 ? 1.0D : (double) (this.level - 1) / (double) (MAX_LEVEL - 1);
        return MIN_STAMINA + (int) Math.round((MAX_STAMINA - MIN_STAMINA) * Math.max(0.0D, Math.min(1.0D, progress)));
    }

    public int getCurrentStamina() {
        return Math.max(0, Math.min(this.currentNen, this.getMaxStamina()));
    }

    public void setCurrentStamina(int currentStamina) {
        this.currentNen = Math.max(0, Math.min(this.getMaxStamina(), currentStamina));
        if (this.currentNen <= 0) {
            this.disableNenDrains();
        }
    }

    public void addNen(int amount) {
        this.addStamina(amount);
    }

    public void addStamina(int amount) {
        this.setCurrentStamina(this.currentNen + amount);
    }

    public void addNenRegenProgress(double amount) {
        this.nenRegenProgress = Math.max(0.0D, this.nenRegenProgress + amount);
    }

    public int getStaminaRegenDelayTicks() {
        return this.staminaRegenDelayTicks;
    }

    public void tickStaminaRegenDelay() {
        if (this.staminaRegenDelayTicks > 0) {
            this.staminaRegenDelayTicks--;
        }
    }

    public boolean tickAndHasStaminaRegenDelay() {
        if (this.staminaRegenDelayTicks <= 0) {
            return false;
        }
        this.staminaRegenDelayTicks--;
        return true;
    }

    public boolean consumeNenRegenStep() {
        if (this.nenRegenProgress < 1.0D) {
            return false;
        }
        this.nenRegenProgress -= 1.0D;
        return true;
    }

    public void resetNenRegenProgress() {
        this.nenRegenProgress = 0.0D;
    }

    public boolean consumeNen(int amount) {
        return this.consumeStamina(this.getReducedNenStaminaCost(amount));
    }

    public boolean consumeStamina(int amount) {
        if (amount <= 0) {
            return true;
        }
        if (this.currentNen < amount) {
            return false;
        }
        this.currentNen -= amount;
        this.staminaRegenDelayTicks = 20 * 5;
        this.resetNenRegenProgress();
        if (this.currentNen <= 0) {
            this.currentNen = 0;
            this.disableNenDrains();
        }
        return true;
    }

    public float getNenPercent() {
        return this.getStaminaPercent();
    }

    public float getStaminaPercent() {
        return this.getMaxStamina() <= 0 ? 0.0F : (float) this.getCurrentStamina() / (float) this.getMaxStamina();
    }

    public int getReducedStaminaCost(int baseCost) {
        if (baseCost <= 0) {
            return 0;
        }
        float multiplier = Math.max(0.25F, 1.0F - (this.nenLevel * 0.025F));
        return Math.max(1, Math.round(baseCost * multiplier));
    }

    public int getReducedNenStaminaCost(int baseCost) {
        return this.getReducedStaminaCost(Math.max(1, Math.round(baseCost * 0.35F)));
    }

    public boolean hasStaminaFor(int baseCost) {
        return this.getCurrentStamina() >= this.getReducedStaminaCost(baseCost);
    }

    public boolean hasStaminaForNenCost(int baseCost) {
        return this.getCurrentStamina() >= this.getReducedNenStaminaCost(baseCost);
    }

    public int getNenAuraColor() {
        return this.nenAuraColor;
    }

    public NenType getNenType() {
        return this.nenType.isBlank() ? null : NenType.byName(this.nenType);
    }

    public void setNenType(NenType nenType) {
        this.nenType = nenType == null ? "" : nenType.name();
        this.nenTechnique = switch (nenType) {
            case MANIPULATION -> "deep_purple";
            case TRANSMUTATION -> "elastic_aura";
            case CONJURATION -> "chain_nen";
            default -> "";
        };
        syncNenTechniqueNodes();
        this.lungCapacity = Math.min(this.lungCapacity, this.getMaxLungCapacity());
        if (!this.hasDeepPurpleTechnique()) {
            this.deepPurpleSoldierMode = 0;
            this.deepPurpleCloneMode = 0;
            this.clearDeepPurplePing();
        }
        if (!this.hasChainTechnique()) {
            this.emperorTimeActive = false;
            this.vowFaction = "";
        }
    }

    public void setNenTechniqueId(String techniqueId) {
        this.nenTechnique = switch (techniqueId == null ? "" : techniqueId) {
            case "deep_purple" -> "deep_purple";
            case "elastic_aura" -> "elastic_aura";
            case "chain_nen" -> "chain_nen";
            default -> "";
        };
        syncNenTechniqueNodes();
        this.lungCapacity = Math.min(this.lungCapacity, this.getMaxLungCapacity());
        if (!this.hasDeepPurpleTechnique()) {
            this.deepPurpleSoldierMode = 0;
            this.deepPurpleCloneMode = 0;
            this.clearDeepPurplePing();
        }
        if (!this.hasChainTechnique()) {
            this.emperorTimeActive = false;
            this.vowFaction = "";
        }
    }

    public String getNenTechniqueId() {
        return this.nenTechnique;
    }

    public String getNenTechniqueDisplayName() {
        return switch (this.nenTechnique) {
            case "deep_purple" -> "Deep Purple";
            case "elastic_aura" -> "Bungee Gum";
            case "chain_nen" -> "Chain Style";
            default -> "None";
        };
    }

    public boolean hasDeepPurpleTechnique() {
        return "deep_purple".equals(this.nenTechnique);
    }

    public TraitType getTrait() {
        return TraitType.byId(this.trait);
    }

    public String getTraitId() {
        return this.trait;
    }

    public String getTraitDisplayName() {
        return this.getTrait().displayName();
    }

    public void setTrait(TraitType trait) {
        TraitType nextTrait = trait == null ? TraitType.NONE : trait;
        this.trait = nextTrait.id();
        if (nextTrait != TraitType.SCARLET_EYES) {
            this.emperorTimeActive = false;
        }
    }

    public boolean hasScarletEyesTrait() {
        return this.getTrait() == TraitType.SCARLET_EYES;
    }

    public boolean isEmperorTimeActive() {
        return this.emperorTimeActive && this.hasScarletEyesTrait();
    }

    public void setEmperorTimeActive(boolean emperorTimeActive) {
        this.emperorTimeActive = this.hasScarletEyesTrait() && emperorTimeActive;
    }

    public String getVowFaction() {
        return this.getAbilityVowFaction("chain_jail");
    }

    public void setVowFaction(String vowFaction) {
        this.setAbilityVowFaction("chain_jail", vowFaction);
    }

    public String getAbilityVowFaction(String abilityId) {
        return abilityId == null ? "" : this.abilityVows.getOrDefault(abilityId, "");
    }

    public void setAbilityVowFaction(String abilityId, String factionName) {
        if (abilityId == null || abilityId.isBlank()) {
            return;
        }
        String normalized = normalizeVowPlayers(factionName);
        if (normalized.isBlank()) {
            this.abilityVows.remove(abilityId);
        } else {
            this.abilityVows.put(abilityId, normalized);
        }
        if ("chain_jail".equals(abilityId)) {
            this.vowFaction = normalized;
        }
    }

    public boolean isAbilityVowedAgainstPlayer(String abilityId, String playerName) {
        if (abilityId == null || abilityId.isBlank() || playerName == null || playerName.isBlank()) {
            return false;
        }
        String normalizedPlayer = playerName.trim();
        for (String vowedPlayer : this.getAbilityVowFaction(abilityId).split(",")) {
            if (vowedPlayer.trim().equalsIgnoreCase(normalizedPlayer)) {
                return true;
            }
        }
        return false;
    }

    private static String normalizeVowPlayers(String playerNames) {
        if (playerNames == null || playerNames.isBlank()) {
            return "";
        }
        LinkedHashSet<String> names = new LinkedHashSet<>();
        for (String token : playerNames.split("[,;\\s]+")) {
            String name = token.trim();
            if (!name.isBlank()) {
                names.add(name);
            }
            if (names.size() >= 3) {
                break;
            }
        }
        return String.join(", ", names);
    }

    public Map<String, String> getAbilityVows() {
        return this.abilityVows;
    }

    public String getCombatVowType() {
        return this.combatVowType;
    }

    public int getCombatVowPercent() {
        return this.combatVowPercent;
    }

    public boolean hasSpeedVow() {
        return "speed".equals(this.combatVowType);
    }

    public boolean hasStrengthVow() {
        return "strength".equals(this.combatVowType);
    }

    public void setCombatVow(String vowType, int percent) {
        String normalized = vowType == null ? "" : vowType.trim().toLowerCase(java.util.Locale.ROOT);
        if (!"speed".equals(normalized) && !"strength".equals(normalized)) {
            this.clearCombatVow();
            return;
        }
        this.combatVowType = normalized;
        this.combatVowPercent = clampCombatVowPercent(percent);
    }

    public void clearCombatVow() {
        this.combatVowType = "";
        this.combatVowPercent = 25;
    }

    public float getCombatVowDamageMultiplier() {
        float percent = this.combatVowPercent / 100.0F;
        if (this.hasSpeedVow()) {
            return 1.0F - percent;
        }
        if (this.hasStrengthVow()) {
            return 1.0F + percent;
        }
        return 1.0F;
    }

    public float getCombatVowCooldownMultiplier() {
        float percent = this.combatVowPercent / 100.0F;
        if (this.hasSpeedVow()) {
            return 1.0F - percent;
        }
        if (this.hasStrengthVow()) {
            return 1.0F + percent;
        }
        return 1.0F;
    }

    private static int clampCombatVowPercent(int percent) {
        return Math.max(25, Math.min(75, percent));
    }

    public int getScarletEyesOffsetX() {
        return this.scarletEyesOffsetX;
    }

    public int getScarletEyesOffsetY() {
        return this.scarletEyesOffsetY;
    }

    public void setScarletEyesOffset(int offsetX, int offsetY) {
        this.scarletEyesOffsetX = Math.max(-8, Math.min(8, offsetX));
        this.scarletEyesOffsetY = Math.max(-8, Math.min(8, offsetY));
        this.scarletLeftEyeOffsetX = Math.max(-8, Math.min(8, offsetX - 6));
        this.scarletLeftEyeOffsetY = this.scarletEyesOffsetY;
        this.scarletRightEyeOffsetX = Math.max(-8, Math.min(8, offsetX + 6));
        this.scarletRightEyeOffsetY = this.scarletEyesOffsetY;
    }

    public int getScarletLeftEyeOffsetX() {
        return this.scarletLeftEyeOffsetX;
    }

    public int getScarletLeftEyeOffsetY() {
        return this.scarletLeftEyeOffsetY;
    }

    public int getScarletLeftEyeLength() {
        return this.scarletLeftEyeLength;
    }

    public int getScarletLeftEyeVerticalLength() {
        return this.scarletLeftEyeVerticalLength;
    }

    public int getScarletRightEyeOffsetX() {
        return this.scarletRightEyeOffsetX;
    }

    public int getScarletRightEyeOffsetY() {
        return this.scarletRightEyeOffsetY;
    }

    public int getScarletRightEyeLength() {
        return this.scarletRightEyeLength;
    }

    public int getScarletRightEyeVerticalLength() {
        return this.scarletRightEyeVerticalLength;
    }

    public void setScarletEyesLayout(int leftX, int leftY, int leftLength, int rightX, int rightY, int rightLength) {
        this.setScarletEyesLayout(leftX, leftY, leftLength, this.scarletLeftEyeVerticalLength, rightX, rightY, rightLength, this.scarletRightEyeVerticalLength);
    }

    public void setScarletEyesLayout(int leftX, int leftY, int leftLength, int leftVerticalLength, int rightX, int rightY, int rightLength, int rightVerticalLength) {
        this.scarletLeftEyeOffsetX = Math.max(-8, Math.min(8, leftX));
        this.scarletLeftEyeOffsetY = Math.max(-8, Math.min(8, leftY));
        this.scarletLeftEyeLength = normalizeScarletEyeLength(leftLength);
        this.scarletLeftEyeVerticalLength = normalizeScarletEyeLength(leftVerticalLength);
        this.scarletRightEyeOffsetX = Math.max(-8, Math.min(8, rightX));
        this.scarletRightEyeOffsetY = Math.max(-8, Math.min(8, rightY));
        this.scarletRightEyeLength = normalizeScarletEyeLength(rightLength);
        this.scarletRightEyeVerticalLength = normalizeScarletEyeLength(rightVerticalLength);
        this.scarletEyesOffsetX = Math.round((this.scarletLeftEyeOffsetX + this.scarletRightEyeOffsetX) * 0.5F);
        this.scarletEyesOffsetY = Math.round((this.scarletLeftEyeOffsetY + this.scarletRightEyeOffsetY) * 0.5F);
    }

    private static int normalizeScarletEyeLength(int length) {
        int clamped = Math.max(-8, Math.min(8, length));
        if (clamped == 0) {
            return length < 0 ? -1 : 1;
        }
        return clamped;
    }

    public boolean hasChainTechnique() {
        return "chain_nen".equals(this.nenTechnique);
    }

    public int getLungCapacity() {
        return this.lungCapacity;
    }

    public void setLungCapacity(int lungCapacity) {
        this.lungCapacity = Math.max(0, Math.min(this.getMaxLungCapacity(), lungCapacity));
    }

    public int getMaxLungCapacity() {
        if (!this.hasDeepPurpleTechnique() || this.nenLevel < 2) {
            return 0;
        }
        return (this.nenLevel - 1) * 50;
    }

    public double getDeepPurpleLungGainPerTick() {
        if (!this.hasDeepPurpleTechnique() || this.nenLevel < 2) {
            return 0.0D;
        }
        return (3.0D * Math.pow(1.25D, Math.max(0, this.nenLevel - 2))) / 20.0D;
    }

    public void addDeepPurpleLungGainProgress(double amount) {
        this.deepPurpleLungGainProgress = Math.max(0.0D, this.deepPurpleLungGainProgress + amount);
    }

    public boolean consumeDeepPurpleLungGainStep() {
        if (this.deepPurpleLungGainProgress < 1.0D) {
            return false;
        }
        this.deepPurpleLungGainProgress -= 1.0D;
        return true;
    }

    public void resetDeepPurpleLungGainProgress() {
        this.deepPurpleLungGainProgress = 0.0D;
    }

    public double getDeepPurpleDrowningSlowMultiplier() {
        if (!this.hasDeepPurpleTechnique() || this.nenLevel < 2) {
            return 1.0D;
        }
        return 3.0D * Math.pow(1.25D, Math.max(0, this.nenLevel - 2));
    }

    public void addDeepPurpleDrowningRecoveryProgress(double amount) {
        this.deepPurpleDrowningRecoveryProgress = Math.max(0.0D, this.deepPurpleDrowningRecoveryProgress + amount);
    }

    public boolean consumeDeepPurpleDrowningRecoveryStep() {
        if (this.deepPurpleDrowningRecoveryProgress < 1.0D) {
            return false;
        }
        this.deepPurpleDrowningRecoveryProgress -= 1.0D;
        return true;
    }

    public void resetDeepPurpleDrowningRecoveryProgress() {
        this.deepPurpleDrowningRecoveryProgress = 0.0D;
    }

    public boolean addLungCapacity(int amount) {
        if (amount <= 0 || !this.hasDeepPurpleTechnique()) {
            return false;
        }
        int previous = this.lungCapacity;
        this.setLungCapacity(this.lungCapacity + amount);
        return this.lungCapacity != previous;
    }

    public boolean consumeLungCapacity(int amount) {
        if (amount <= 0 || !this.hasDeepPurpleTechnique() || this.lungCapacity < amount) {
            return false;
        }
        this.lungCapacity -= amount;
        return true;
    }

    public int getDeepPurpleSoldierMode() {
        return this.deepPurpleSoldierMode;
    }

    public boolean isDeepPurpleReturnMode() {
        return this.deepPurpleSoldierMode == 1;
    }

    public void cycleDeepPurpleSoldierMode() {
        this.deepPurpleSoldierMode = (this.deepPurpleSoldierMode + 1) % 2;
    }

    public String getDeepPurpleSoldierModeDisplayName() {
        return switch (this.deepPurpleSoldierMode) {
            case 1 -> "Return";
            default -> "Hunt";
        };
    }

    public int getDeepPurpleCloneMode() {
        return this.deepPurpleCloneMode;
    }

    public boolean isDeepPurpleFactionCloneMode() {
        return this.deepPurpleCloneMode == 1;
    }

    public void cycleDeepPurpleCloneMode() {
        this.deepPurpleCloneMode = (this.deepPurpleCloneMode + 1) % 2;
    }

    public String getDeepPurpleCloneModeDisplayName() {
        return this.deepPurpleCloneMode == 1 ? "Faction Copy" : "Self Copy";
    }

    public String getDeepPurpleSpottedTargetUuid() {
        return this.deepPurpleSpottedTargetUuid;
    }

    public BlockPos getDeepPurpleSpottedTargetPos() {
        return this.deepPurpleSpottedTargetPos;
    }

    public int getDeepPurpleSpottedTargetTicks() {
        return this.deepPurpleSpottedTargetTicks;
    }

    public void setDeepPurplePing(String targetUuid, BlockPos pos, int ticks) {
        this.deepPurpleSpottedTargetUuid = targetUuid == null ? "" : targetUuid;
        this.deepPurpleSpottedTargetPos = pos;
        this.deepPurpleSpottedTargetTicks = Math.max(0, ticks);
    }

    public void clearDeepPurplePing() {
        this.deepPurpleSpottedTargetUuid = "";
        this.deepPurpleSpottedTargetPos = null;
        this.deepPurpleSpottedTargetTicks = 0;
    }

    public boolean tickDeepPurplePing() {
        if (this.deepPurpleSpottedTargetTicks <= 0) {
            return false;
        }
        this.deepPurpleSpottedTargetTicks--;
        if (this.deepPurpleSpottedTargetTicks <= 0) {
            this.clearDeepPurplePing();
        }
        return true;
    }

    public int getNenTechniquePointsSpent() {
        return (int) this.unlockedNenTechniqueNodes.stream()
                .map(NenTechniqueSkillNode::byId)
                .filter(node -> node != null && node.order() > 0 && node.techniqueId().equalsIgnoreCase(this.nenTechnique))
                .count();
    }

    public int getAvailableNenTechniquePoints() {
        if (this.nenTechnique.isBlank()) {
            return 0;
        }
        return Math.max(0, Math.max(0, this.nenLevel - 1) - this.getNenTechniquePointsSpent());
    }

    public boolean hasUnlockedNenTechniqueNode(NenTechniqueSkillNode node) {
        return node != null && this.unlockedNenTechniqueNodes.contains(node.id());
    }

    public boolean canUnlockNenTechniqueNode(NenTechniqueSkillNode node) {
        if (node == null || this.nenTechnique.isBlank() || !node.techniqueId().equalsIgnoreCase(this.nenTechnique)) {
            return false;
        }
        if (this.hasUnlockedNenTechniqueNode(node)) {
            return false;
        }
        // Nodes with order 0 are auto-unlocked via syncNenTechniqueNodes, not manually
        if (node.order() <= 0) {
            return false;
        }
        return this.nenLevel >= node.requiredNenLevel();
    }

    public boolean unlockNenTechniqueNode(NenTechniqueSkillNode node) {
        if (!this.canUnlockNenTechniqueNode(node)) {
            return false;
        }
        return this.unlockedNenTechniqueNodes.add(node.id());
    }

    /**
     * Auto-unlocks all nen technique nodes whose required nen level has been reached.
     * Called whenever nenLevel changes.
     */
    public void autoUnlockNenTechniqueNodes() {
        if (this.nenTechnique.isBlank()) {
            return;
        }
        for (NenTechniqueSkillNode node : NenTechniqueSkillNode.forTechnique(this.nenTechnique)) {
            if (!this.hasUnlockedNenTechniqueNode(node) && this.nenLevel >= node.requiredNenLevel()) {
                this.unlockedNenTechniqueNodes.add(node.id());
            }
        }
    }

    private void syncNenTechniqueNodes() {
        this.unlockedNenTechniqueNodes.removeIf(nodeId -> {
            NenTechniqueSkillNode node = NenTechniqueSkillNode.byId(nodeId);
            return node == null || this.nenTechnique.isBlank() || !node.techniqueId().equalsIgnoreCase(this.nenTechnique);
        });
        if (!this.nenTechnique.isBlank()) {
            NenTechniqueSkillNode[] nodes = NenTechniqueSkillNode.forTechnique(this.nenTechnique);
            if (nodes.length > 0 && nodes[0].order() == 0) {
                this.unlockedNenTechniqueNodes.add(nodes[0].id());
            }
        }
        // Auto-unlock any nodes the player already qualifies for by nen level
        this.autoUnlockNenTechniqueNodes();
    }

    public NenQuestStage getNenQuestStage() {
        return NenQuestStage.byName(this.nenQuestStage);
    }

    public void setNenQuestStage(NenQuestStage stage) {
        this.nenQuestStage = stage == null ? NenQuestStage.NOT_STARTED.name() : stage.name();
        if (stage != NenQuestStage.FEEL_THE_AURA) {
            this.stopMeditation();
        }
    }

    public void setNenAuraColor(int nenAuraColor) {
        this.nenAuraColor = nenAuraColor & 0xFFFFFF;
    }

    public boolean hasTenUnlocked() {
        return this.tenUnlocked;
    }

    public boolean hasZetsuUnlocked() {
        return this.zetsuUnlocked;
    }

    public boolean hasRenUnlocked() {
        return this.renUnlocked;
    }

    public boolean hasGyoUnlocked() {
        return this.gyoUnlocked;
    }

    public boolean hasEnUnlocked() {
        return this.enUnlocked;
    }

    public boolean hasShuUnlocked() {
        return this.shuUnlocked;
    }

    public boolean hasKoUnlocked() {
        return this.koUnlocked;
    }

    public boolean hasKenUnlocked() {
        return this.kenUnlocked;
    }

    public boolean hasRyuUnlocked() {
        return this.ryuUnlocked;
    }

    public void setTenUnlocked(boolean tenUnlocked) {
        this.tenUnlocked = tenUnlocked;
    }

    public void setZetsuUnlocked(boolean zetsuUnlocked) {
        this.zetsuUnlocked = zetsuUnlocked;
    }

    public void setRenUnlocked(boolean renUnlocked) {
        this.renUnlocked = renUnlocked;
    }

    public void setGyoUnlocked(boolean gyoUnlocked) {
        this.gyoUnlocked = gyoUnlocked;
    }

    public void setEnUnlocked(boolean enUnlocked) {
        this.enUnlocked = enUnlocked;
    }

    public void setShuUnlocked(boolean shuUnlocked) {
        this.shuUnlocked = shuUnlocked;
    }

    public void setKoUnlocked(boolean koUnlocked) {
        this.koUnlocked = koUnlocked;
    }

    public void setKenUnlocked(boolean kenUnlocked) {
        this.kenUnlocked = kenUnlocked;
    }

    public void setRyuUnlocked(boolean ryuUnlocked) {
        this.ryuUnlocked = ryuUnlocked;
    }

    public int getFeelAuraTicks() {
        return this.feelAuraTicks;
    }

    public void setFeelAuraTicks(int feelAuraTicks) {
        this.feelAuraTicks = Math.max(0, feelAuraTicks);
    }

    public int getMeditationCountdownTicks() {
        return this.meditationCountdownTicks;
    }

    public void setMeditationCountdownTicks(int meditationCountdownTicks) {
        this.meditationCountdownTicks = Math.max(0, meditationCountdownTicks);
    }

    public boolean isMeditationCountdownActive() {
        return this.meditationCountdownTicks > 0;
    }

    public boolean isMeditationActive() {
        return this.meditationActive;
    }

    public void setMeditationActive(boolean meditationActive) {
        this.meditationActive = meditationActive;
    }

    public int getMeditationTicksRemaining() {
        return this.meditationTicksRemaining;
    }

    public void setMeditationTicksRemaining(int meditationTicksRemaining) {
        this.meditationTicksRemaining = Math.max(0, meditationTicksRemaining);
    }

    public String getMeditationPromptKey() {
        return this.meditationPromptKey;
    }

    public void setMeditationPromptKey(String meditationPromptKey) {
        this.meditationPromptKey = meditationPromptKey == null ? "" : meditationPromptKey;
    }

    public int getMeditationPromptTicksRemaining() {
        return this.meditationPromptTicksRemaining;
    }

    public void setMeditationPromptTicksRemaining(int meditationPromptTicksRemaining) {
        this.meditationPromptTicksRemaining = Math.max(0, meditationPromptTicksRemaining);
    }

    public int getMeditationPromptSeed() {
        return this.meditationPromptSeed;
    }

    public void setMeditationPromptSeed(int meditationPromptSeed) {
        this.meditationPromptSeed = Math.max(0, meditationPromptSeed);
    }

    public void stopMeditation() {
        this.meditationCountdownTicks = 0;
        this.meditationActive = false;
        this.meditationTicksRemaining = 0;
        this.meditationPromptKey = "";
        this.meditationPromptTicksRemaining = 0;
        this.meditationPromptSeed = 0;
        if (this.getCurrentAnimation() == AnimationType.MEDITATE) {
            this.currentAnimation = AnimationType.NONE.name();
            this.animationTicks = 0;
        }
    }

    public boolean hasNodeDamageTaken() {
        return this.nodeDamageTaken;
    }

    public void setNodeDamageTaken(boolean nodeDamageTaken) {
        this.nodeDamageTaken = nodeDamageTaken;
    }

    public int getEndureLowHpTicks() {
        return this.endureLowHpTicks;
    }

    public void setEndureLowHpTicks(int endureLowHpTicks) {
        this.endureLowHpTicks = Math.max(0, endureLowHpTicks);
    }

    public int getZetsuCrouchTicks() {
        return this.zetsuCrouchTicks;
    }

    public void setZetsuCrouchTicks(int zetsuCrouchTicks) {
        this.zetsuCrouchTicks = Math.max(0, zetsuCrouchTicks);
    }

    public int getZetsuTrialPrepTicks() {
        return this.zetsuTrialPrepTicks;
    }

    public void setZetsuTrialPrepTicks(int zetsuTrialPrepTicks) {
        this.zetsuTrialPrepTicks = Math.max(0, zetsuTrialPrepTicks);
    }

    public int getZetsuTrialSearchTicks() {
        return this.zetsuTrialSearchTicks;
    }

    public void setZetsuTrialSearchTicks(int zetsuTrialSearchTicks) {
        this.zetsuTrialSearchTicks = Math.max(0, zetsuTrialSearchTicks);
    }

    public boolean isZetsuTrialSearching() {
        return this.zetsuTrialSearching;
    }

    public void setZetsuTrialSearching(boolean zetsuTrialSearching) {
        this.zetsuTrialSearching = zetsuTrialSearching;
    }

    public boolean isZetsuTrialComplete() {
        return this.zetsuTrialComplete;
    }

    public void setZetsuTrialComplete(boolean zetsuTrialComplete) {
        this.zetsuTrialComplete = zetsuTrialComplete;
    }

    public int getZetsuTrialWingEntityId() {
        return this.zetsuTrialWingEntityId;
    }

    public void setZetsuTrialWingEntityId(int zetsuTrialWingEntityId) {
        this.zetsuTrialWingEntityId = zetsuTrialWingEntityId;
    }

    public BlockPos getZetsuTrialOrigin() {
        return this.zetsuTrialOrigin;
    }

    public void setZetsuTrialOrigin(BlockPos zetsuTrialOrigin) {
        this.zetsuTrialOrigin = zetsuTrialOrigin;
    }

    public boolean isZetsuTrialRunning() {
        return this.zetsuTrialPrepTicks > 0 || this.zetsuTrialSearchTicks > 0 || this.zetsuTrialSearching;
    }

    public void resetZetsuTrial() {
        this.zetsuTrialPrepTicks = 0;
        this.zetsuTrialSearchTicks = 0;
        this.zetsuTrialSearching = false;
        this.zetsuTrialComplete = false;
        this.zetsuTrialWingEntityId = -1;
        this.zetsuTrialOrigin = null;
    }

    public float getRenOverflowDamageWindow() {
        return this.renOverflowDamageWindow;
    }

    public void setRenOverflowDamageWindow(float renOverflowDamageWindow) {
        this.renOverflowDamageWindow = Math.max(0.0F, renOverflowDamageWindow);
    }

    public int getRenOverflowTicks() {
        return this.renOverflowTicks;
    }

    public void setRenOverflowTicks(int renOverflowTicks) {
        this.renOverflowTicks = Math.max(0, renOverflowTicks);
    }

    public int getAuraBurstKills() {
        return this.auraBurstKills;
    }

    public void setAuraBurstKills(int auraBurstKills) {
        this.auraBurstKills = Math.max(0, auraBurstKills);
    }

    public int getAuraBurstTicks() {
        return this.auraBurstTicks;
    }

    public void setAuraBurstTicks(int auraBurstTicks) {
        this.auraBurstTicks = Math.max(0, auraBurstTicks);
    }

    public int getEnLookedMobCount() {
        return this.enLookedMobCount;
    }

    public void setEnLookedMobCount(int enLookedMobCount) {
        this.enLookedMobCount = Math.max(0, enLookedMobCount);
    }

    public String getLastNenLookTargetUuid() {
        return this.lastNenLookTargetUuid;
    }

    public void setLastNenLookTargetUuid(String lastNenLookTargetUuid) {
        this.lastNenLookTargetUuid = lastNenLookTargetUuid == null ? "" : lastNenLookTargetUuid;
    }

    public int getNenLookCooldown() {
        return this.nenLookCooldown;
    }

    public void setNenLookCooldown(int nenLookCooldown) {
        this.nenLookCooldown = Math.max(0, nenLookCooldown);
    }

    public int getShuWeaponRenTicks() {
        return this.shuWeaponRenTicks;
    }

    public void setShuWeaponRenTicks(int shuWeaponRenTicks) {
        this.shuWeaponRenTicks = Math.max(0, shuWeaponRenTicks);
    }

    public int getKoOneShotKills() {
        return this.koOneShotKills;
    }

    public void setKoOneShotKills(int koOneShotKills) {
        this.koOneShotKills = Math.max(0, koOneShotKills);
    }

    public int getKenBalanceTicks() {
        return this.kenBalanceTicks;
    }

    public void setKenBalanceTicks(int kenBalanceTicks) {
        this.kenBalanceTicks = Math.max(0, kenBalanceTicks);
    }

    public boolean isRyuFightStarted() {
        return this.ryuFightStarted;
    }

    public void setRyuFightStarted(boolean ryuFightStarted) {
        this.ryuFightStarted = ryuFightStarted;
    }

    public boolean isRyuFightFinished() {
        return this.ryuFightFinished;
    }

    public void setRyuFightFinished(boolean ryuFightFinished) {
        this.ryuFightFinished = ryuFightFinished;
    }

    public boolean isTenActive() {
        return this.tenActive;
    }

    public void setTenActive(boolean active) {
        this.tenActive = this.hasTenUnlocked() && !this.zetsuActive && active;
        if (this.tenActive) {
            this.kenActive = false;
            this.renActive = false;
            this.koActive = false;
        }
    }

    public boolean isZetsuActive() {
        return this.zetsuActive;
    }

    public void setZetsuActive(boolean active) {
        this.zetsuActive = this.hasZetsuUnlocked() && active;
        if (this.zetsuActive) {
            this.tenActive = false;
            this.renActive = false;
            this.enActive = false;
            this.koActive = false;
            this.kenActive = false;
            this.ryuActive = false;
        }
    }

    public boolean isRenActive() {
        return this.renActive;
    }

    public void setRenActive(boolean active) {
        this.renActive = this.hasRenUnlocked() && !this.zetsuActive && active;
        if (this.renActive) {
            this.kenActive = false;
            this.tenActive = false;
            this.koActive = false;
        }
    }

    public boolean isEnActive() {
        return this.enActive;
    }

    public void setEnActive(boolean active) {
        this.enActive = this.hasEnUnlocked() && !this.zetsuActive && active;
    }

    public boolean isKoActive() {
        return this.koActive;
    }

    public void setKoActive(boolean active) {
        this.koActive = this.hasKoUnlocked() && !this.zetsuActive && active;
        if (this.koActive) {
            this.tenActive = false;
            this.renActive = false;
            this.kenActive = false;
        }
    }

    public boolean isKenActive() {
        return this.kenActive;
    }

    public void setKenActive(boolean active) {
        this.kenActive = this.hasKenUnlocked() && !this.zetsuActive && active;
        if (this.kenActive) {
            this.tenActive = false;
            this.renActive = false;
            this.koActive = false;
        }
    }

    public boolean isRyuActive() {
        return false;
    }

    public void setRyuActive(boolean active) {
        this.ryuActive = false;
    }

    public boolean isZetsuForcedInvisibility() {
        return this.zetsuForcedInvisibility;
    }

    public void setZetsuForcedInvisibility(boolean zetsuForcedInvisibility) {
        this.zetsuForcedInvisibility = zetsuForcedInvisibility;
    }

    public void disableNenDrains() {
        this.tenActive = false;
        this.renActive = false;
        this.enActive = false;
        this.koActive = false;
        this.kenActive = false;
        this.ryuActive = false;
        this.emperorTimeActive = false;
    }

    public void disableAllNen() {
        this.disableNenDrains();
        this.zetsuActive = false;
    }

    public void triggerAnimation(AnimationType animationType) {
        this.currentAnimation = animationType.name();
        this.animationTicks = animationType.durationTicks();
    }

    public void startChargingAbility(String abilityId, int chargeTicks, Vec3 direction) {
        this.chargingAbilityId = abilityId;
        this.chargeTicksRemaining = Math.max(0, chargeTicks);
        this.chargeDirectionX = direction.x;
        this.chargeDirectionY = direction.y;
        this.chargeDirectionZ = direction.z;
        this.chargeTargetUuid = "";
    }

    public void setChargeTargetUuid(String chargeTargetUuid) {
        this.chargeTargetUuid = chargeTargetUuid == null ? "" : chargeTargetUuid;
    }

    public String getChargeTargetUuid() {
        return this.chargeTargetUuid;
    }

    public boolean isChargingAbility(String abilityId) {
        return this.chargeTicksRemaining > 0 && abilityId.equals(this.chargingAbilityId);
    }

    public int getChargeTicksRemaining() {
        return this.chargeTicksRemaining;
    }

    public void tickChargingAbility() {
        if (this.chargeTicksRemaining > 0) {
            this.chargeTicksRemaining--;
        }
    }

    public Vec3 getChargeDirection() {
        return new Vec3(this.chargeDirectionX, this.chargeDirectionY, this.chargeDirectionZ);
    }

    public void clearChargingAbility() {
        this.chargingAbilityId = "";
        this.chargeTicksRemaining = 0;
        this.chargeDirectionX = 0.0D;
        this.chargeDirectionY = 0.0D;
        this.chargeDirectionZ = 0.0D;
        this.chargeTargetUuid = "";
    }

    public void startActiveAbility(String abilityId, int activeTicks, Vec3 direction) {
        this.activeAbilityId = abilityId;
        this.activeAbilityTicksRemaining = Math.max(0, activeTicks);
        this.activeAbilityDirectionX = direction.x;
        this.activeAbilityDirectionY = direction.y;
        this.activeAbilityDirectionZ = direction.z;
        this.activeAbilityTargetUuid = "";
    }

    public boolean isActiveAbility(String abilityId) {
        return this.activeAbilityTicksRemaining > 0 && abilityId.equals(this.activeAbilityId);
    }

    public String getActiveAbilityId() {
        return this.activeAbilityId;
    }

    public int getActiveAbilityTicksRemaining() {
        return this.activeAbilityTicksRemaining;
    }

    public void tickActiveAbility() {
        if (this.activeAbilityTicksRemaining > 0) {
            this.activeAbilityTicksRemaining--;
        }
    }

    public Vec3 getActiveAbilityDirection() {
        return new Vec3(this.activeAbilityDirectionX, this.activeAbilityDirectionY, this.activeAbilityDirectionZ);
    }

    public void setActiveAbilityTargetUuid(String activeAbilityTargetUuid) {
        this.activeAbilityTargetUuid = activeAbilityTargetUuid == null ? "" : activeAbilityTargetUuid;
    }

    public String getActiveAbilityTargetUuid() {
        return this.activeAbilityTargetUuid;
    }

    public void clearActiveAbility() {
        this.activeAbilityId = "";
        this.activeAbilityTicksRemaining = 0;
        this.activeAbilityDirectionX = 0.0D;
        this.activeAbilityDirectionY = 0.0D;
        this.activeAbilityDirectionZ = 0.0D;
        this.activeAbilityTargetUuid = "";
    }

    public void startMartialArtsGrab(String sourceAbilityId, String targetUuid, int holdTicks) {
        this.martialArtsGrabSourceAbilityId = sourceAbilityId == null ? "" : sourceAbilityId;
        this.martialArtsGrabTargetUuid = targetUuid == null ? "" : targetUuid;
        this.martialArtsGrabTicksRemaining = Math.max(0, holdTicks);
    }

    public boolean isMartialArtsGrabActive() {
        return this.martialArtsGrabTicksRemaining > 0 && !this.martialArtsGrabTargetUuid.isBlank();
    }

    public int getMartialArtsGrabTicksRemaining() {
        return this.martialArtsGrabTicksRemaining;
    }

    public String getMartialArtsGrabTargetUuid() {
        return this.martialArtsGrabTargetUuid;
    }

    public String getMartialArtsGrabSourceAbilityId() {
        return this.martialArtsGrabSourceAbilityId;
    }

    public void tickMartialArtsGrab() {
        if (this.martialArtsGrabTicksRemaining > 0) {
            this.martialArtsGrabTicksRemaining--;
        }
        if (this.martialArtsGrabTicksRemaining <= 0) {
            this.clearMartialArtsGrab();
        }
    }

    public void clearMartialArtsGrab() {
        this.martialArtsGrabTargetUuid = "";
        this.martialArtsGrabSourceAbilityId = "";
        this.martialArtsGrabTicksRemaining = 0;
    }

    public void tickCooldowns() {
        this.abilityCooldowns.replaceAll((abilityId, ticks) -> Math.max(0, ticks - 1));
        this.abilityCooldowns.entrySet().removeIf(entry -> entry.getValue() <= 0);
        this.attackerIFrameTicks.replaceAll((attackerId, ticks) -> Math.max(0, ticks - 1));
        this.attackerIFrameTicks.entrySet().removeIf(entry -> entry.getValue() <= 0);
        if (this.dashIFrameTicks > 0) {
            this.dashIFrameTicks--;
        }
    }

    public boolean hasAttackerIFrame(String attackerId) {
        return attackerId != null && !attackerId.isBlank() && this.attackerIFrameTicks.getOrDefault(attackerId, 0) > 0;
    }

    public void triggerAttackerIFrame(String attackerId, int ticks) {
        if (attackerId == null || attackerId.isBlank() || ticks <= 0) {
            return;
        }
        this.attackerIFrameTicks.put(attackerId, ticks);
    }

    public void tickVisualAnimations() {
        if (this.animationTicks > 0) {
            this.animationTicks--;
        } else if (this.getCurrentAnimation() != AnimationType.GUARD
                && this.getCurrentAnimation() != AnimationType.MEDITATE
                && this.getCurrentAnimation() != AnimationType.BOXER_BARRAGE_FAST
                && this.getCurrentAnimation() != AnimationType.BOXER_REDIRECTION
                && this.getCurrentAnimation() != AnimationType.ELASTIC_REFLECT) {
            this.currentAnimation = AnimationType.NONE.name();
        }
    }

    public void resetCooldowns() {
        this.guarding = false;
        this.guardTicks = 0;
        this.abilityCooldowns.clear();
        this.attackerIFrameTicks.clear();
        this.dashIFrameTicks = 0;
        this.airJumpsUsed = 0;
        this.airLaunchFallProtection = false;
        this.currentAnimation = AnimationType.NONE.name();
        this.animationTicks = 0;
        this.disableAllNen();
        this.stopMeditation();
        this.clearChargingAbility();
        this.clearActiveAbility();
        this.clearMartialArtsGrab();
        this.clearPendingJudgmentChainTarget();
    }

    public void resetAllProgress() {
        this.level = 1;
        this.xp = 0;
        this.skillPoints = 1;
        this.nenLevel = 0;
        this.currentNen = MIN_STAMINA;
        this.staminaRegenDelayTicks = 0;
        this.nenAuraColor = 0x5BE5FF;
        this.nenType = "";
        this.nenTechnique = "";
        this.trait = TraitType.NONE.id();
        this.emperorTimeActive = false;
        this.vowFaction = "";
        this.clearCombatVow();
        this.scarletEyesOffsetX = 0;
        this.scarletEyesOffsetY = 0;
        this.setScarletEyesLayout(-6, 0, -1, 6, 0, 1);
        this.lungCapacity = 0;
        this.abilityVows.clear();
        this.judgmentDisabledAbilities.clear();
        this.pendingJudgmentChainTargetUuid = "";
        this.deepPurpleSoldierMode = 0;
        this.deepPurpleCloneMode = 0;
        this.clearDeepPurplePing();
        this.nenQuestStage = NenQuestStage.NOT_STARTED.name();
        this.tenUnlocked = false;
        this.zetsuUnlocked = false;
        this.renUnlocked = false;
        this.gyoUnlocked = false;
        this.enUnlocked = false;
        this.shuUnlocked = false;
        this.koUnlocked = false;
        this.kenUnlocked = false;
        this.ryuUnlocked = false;
        this.feelAuraTicks = 0;
        this.stopMeditation();
        this.nodeDamageTaken = false;
        this.endureLowHpTicks = 0;
        this.zetsuCrouchTicks = 0;
        this.resetZetsuTrial();
        this.renOverflowDamageWindow = 0.0F;
        this.renOverflowTicks = 0;
        this.auraBurstKills = 0;
        this.auraBurstTicks = 0;
        this.enLookedMobCount = 0;
        this.lastNenLookTargetUuid = "";
        this.nenLookCooldown = 0;
        this.shuWeaponRenTicks = 0;
        this.koOneShotKills = 0;
        this.kenBalanceTicks = 0;
        this.ryuFightStarted = false;
        this.ryuFightFinished = false;
        this.disableAllNen();
        this.zetsuForcedInvisibility = false;
        this.combatBarVisible = true;
        this.activeCombatBar = 0;
        this.emptyHandsPickupEnabled = false;
        this.selectedSkillCategory = "";
        this.skillTreePoints.clear();
        this.unlockedNenTechniqueNodes.clear();
        for (int i = 0; i < this.combatSlots.length; i++) {
            this.combatSlots[i] = "";
        }
        this.activeQuests.clear();
        this.completedQuests.clear();
        this.questProgress.clear();
        this.questTargetCounts.clear();
        this.questRewardXp.clear();
        this.questLocations.clear();
        this.phoneQuestRefreshGameTime = 0L;
        this.enTrackedPlayers.clear();
        this.resetCooldowns();
        recalculateAvailableSkillPoints();
    }

    public boolean isCombatBarVisible() {
        return this.combatBarVisible;
    }

    public void setCombatBarVisible(boolean combatBarVisible) {
        this.combatBarVisible = combatBarVisible;
    }

    public int getActiveCombatBar() {
        return this.activeCombatBar;
    }

    public void setActiveCombatBar(int activeCombatBar) {
        this.activeCombatBar = Math.max(0, Math.min(COMBAT_BAR_COUNT - 1, activeCombatBar));
    }

    public void cycleActiveCombatBar() {
        this.setActiveCombatBar((this.activeCombatBar + 1) % COMBAT_BAR_COUNT);
    }

    public boolean isEmptyHandsPickupEnabled() {
        return this.emptyHandsPickupEnabled;
    }

    public void setEmptyHandsPickupEnabled(boolean emptyHandsPickupEnabled) {
        this.emptyHandsPickupEnabled = emptyHandsPickupEnabled;
    }

    public String getFactionName() {
        return this.factionName;
    }

    public void setFactionName(String factionName) {
        this.factionName = factionName == null ? "" : factionName;
    }

    public String getPlayerDisplayName() {
        return this.playerDisplayName;
    }

    public void setPlayerDisplayName(String playerDisplayName) {
        this.playerDisplayName = playerDisplayName == null ? "" : playerDisplayName;
    }

    public boolean hasFaction() {
        return !this.factionName.isBlank();
    }

    public String getFactionOwnerName() {
        return this.factionOwnerName;
    }

    public void setFactionOwnerName(String factionOwnerName) {
        this.factionOwnerName = factionOwnerName == null ? "" : factionOwnerName;
    }

    public boolean isFactionOwner() {
        return this.hasFaction() && !this.playerDisplayName.isBlank() && this.factionOwnerName.equalsIgnoreCase(this.playerDisplayName);
    }

    public String getPendingFactionInviteName() {
        return this.pendingFactionInviteName;
    }

    public void setPendingFactionInviteName(String pendingFactionInviteName) {
        this.pendingFactionInviteName = pendingFactionInviteName == null ? "" : pendingFactionInviteName;
    }

    public String getPendingFactionInviterName() {
        return this.pendingFactionInviterName;
    }

    public void setPendingFactionInviterName(String pendingFactionInviterName) {
        this.pendingFactionInviterName = pendingFactionInviterName == null ? "" : pendingFactionInviterName;
    }

    public boolean hasPendingFactionInvite() {
        return !this.pendingFactionInviteName.isBlank();
    }

    public Set<String> getFactionMembers() {
        return this.factionMembers;
    }

    public void setFactionMembers(Set<String> factionMembers) {
        this.factionMembers.clear();
        if (factionMembers != null) {
            this.factionMembers.addAll(factionMembers);
        }
    }

    public Set<String> getInvitablePlayers() {
        return this.invitablePlayers;
    }

    public void setInvitablePlayers(Set<String> invitablePlayers) {
        this.invitablePlayers.clear();
        if (invitablePlayers != null) {
            this.invitablePlayers.addAll(invitablePlayers);
        }
    }

    public SkillNode.Category getSelectedSkillCategory() {
        return SkillNode.Category.byId(this.selectedSkillCategory);
    }

    public void selectSkillCategory(SkillNode.Category category) {
        this.selectedSkillCategory = category == null ? "" : category.id();
    }

    public String[] getCombatSlots() {
        return this.combatSlots;
    }

    public String getCombatSlot(int index) {
        return this.getCombatSlot(this.activeCombatBar, index);
    }

    public String getCombatSlot(int barIndex, int slotIndex) {
        if (barIndex < 0 || barIndex >= COMBAT_BAR_COUNT || slotIndex < 0 || slotIndex >= COMBAT_SLOT_COUNT) {
            return "";
        }
        String abilityId = this.combatSlots[toCombatSlotIndex(barIndex, slotIndex)];
        return HunterAbilities.canEquipToCombatBar(abilityId) && HunterAbilities.isCombatAbilityUnlocked(this, abilityId) ? abilityId : "";
    }

    public void setCombatSlot(int index, String abilityId) {
        this.setCombatSlot(this.activeCombatBar, index, abilityId);
    }

    public void setCombatSlot(int barIndex, int slotIndex, String abilityId) {
        if (barIndex < 0 || barIndex >= COMBAT_BAR_COUNT || slotIndex < 0 || slotIndex >= COMBAT_SLOT_COUNT) {
            return;
        }
        int combatIndex = toCombatSlotIndex(barIndex, slotIndex);
        String sanitized = HunterAbilities.canEquipToCombatBar(abilityId) && HunterAbilities.isCombatAbilityUnlocked(this, abilityId) ? abilityId : "";
        if (!sanitized.isBlank()) {
            clearCombatAbility(sanitized, combatIndex);
        }
        this.combatSlots[combatIndex] = sanitized;
    }

    public int getStylePoints(SkillNode node) {
        return this.skillTreePoints.getOrDefault(node.id(), 0);
    }

    public Map<String, Integer> getSkillTreePoints() {
        return this.skillTreePoints;
    }

    public boolean addSkillPoint(SkillNode node) {
        if (this.skillPoints <= 0) {
            return false;
        }
        SkillNode.Category selectedCategory = this.getSelectedSkillCategory();
        if (selectedCategory != null && selectedCategory != node.category()) {
            return false;
        }
        if (selectedCategory == null) {
            this.selectSkillCategory(node.category());
        }
        this.skillTreePoints.put(node.id(), this.getStylePoints(node) + 1);
        this.skillPoints--;
        this.clearUnavailableCombatAbilities();
        return true;
    }

    public boolean removeSkillPoint(SkillNode node) {
        int current = this.getStylePoints(node);
        if (current <= 0) {
            return false;
        }
        if (current == 1) {
            this.skillTreePoints.remove(node.id());
        } else {
            this.skillTreePoints.put(node.id(), current - 1);
        }
        this.skillPoints++;
        if (this.skillTreePoints.isEmpty()) {
            this.selectSkillCategory(null);
        }
        this.clearUnavailableCombatAbilities();
        return true;
    }

    public Set<String> getActiveQuests() {
        return this.activeQuests;
    }

    public Set<String> getCompletedQuests() {
        return this.completedQuests;
    }

    public Map<String, Integer> getQuestProgress() {
        return this.questProgress;
    }

    public int getQuestTargetCount(String questId) {
        QuestDefinition definition = QuestRegistry.byId(questId);
        int fallback = definition != null ? definition.targetCount() : 0;
        return this.questTargetCounts.getOrDefault(questId, fallback);
    }

    public int getQuestRewardXp(String questId) {
        QuestDefinition definition = QuestRegistry.byId(questId);
        int fallback = definition != null ? definition.rewardXp() : 0;
        return this.questRewardXp.getOrDefault(questId, fallback);
    }

    public BlockPos getQuestLocation(String questId) {
        return this.questLocations.get(questId);
    }

    public void setQuestLocation(String questId, BlockPos pos) {
        if (pos == null) {
            this.questLocations.remove(questId);
            return;
        }
        this.questLocations.put(questId, pos.immutable());
    }

    public boolean isPhoneQuestRefreshing(long gameTime) {
        return this.phoneQuestRefreshGameTime > gameTime;
    }

    public long getPhoneQuestRefreshRemaining(long gameTime) {
        return Math.max(0L, this.phoneQuestRefreshGameTime - gameTime);
    }

    public void startPhoneQuestRefresh(long gameTime) {
        this.phoneQuestRefreshGameTime = Math.max(this.phoneQuestRefreshGameTime, gameTime + PHONE_QUEST_REFRESH_TICKS);
    }

    public boolean ensurePhoneQuestRefreshStarted(long gameTime) {
        if (this.phoneQuestRefreshGameTime > 0L) {
            return false;
        }
        boolean hasCompletedPhoneQuest = this.completedQuests.stream().anyMatch(PhoneQuestRegistry::isPhoneQuest);
        if (!hasCompletedPhoneQuest) {
            return false;
        }
        this.phoneQuestRefreshGameTime = gameTime + PHONE_QUEST_REFRESH_TICKS;
        return true;
    }

    public boolean refreshPhoneQuestsIfReady(long gameTime) {
        if (this.phoneQuestRefreshGameTime <= 0L || gameTime < this.phoneQuestRefreshGameTime) {
            return false;
        }
        this.phoneQuestRefreshGameTime = 0L;
        this.completedQuests.removeIf(PhoneQuestRegistry::isPhoneQuest);
        return true;
    }

    public Set<String> getEnTrackedPlayers() {
        return this.enTrackedPlayers;
    }

    public void clearEnTrackedPlayers() {
        this.enTrackedPlayers.clear();
    }

    public void ensureStarterQuests() {
        if (this.activeQuests.isEmpty() && this.completedQuests.isEmpty()) {
            for (QuestDefinition quest : QuestRegistry.STARTER_QUESTS) {
                this.activeQuests.add(quest.id());
                this.questProgress.putIfAbsent(quest.id(), 0);
            }
        }
    }

    public boolean incrementQuestProgress(String questId, int amount) {
        if (!this.activeQuests.contains(questId)) {
            return false;
        }
        QuestDefinition definition = QuestRegistry.byId(questId);
        if (definition == null) {
            return false;
        }
        int current = this.questProgress.getOrDefault(questId, 0);
        int next = Math.min(this.getQuestTargetCount(questId), current + amount);
        if (next == current) {
            return false;
        }
        this.questProgress.put(questId, next);
        return true;
    }

    public boolean tryCompleteQuest(String questId) {
        if (!this.activeQuests.contains(questId)) {
            return false;
        }
        QuestDefinition definition = QuestRegistry.byId(questId);
        if (definition == null) {
            return false;
        }
        if (this.questProgress.getOrDefault(questId, 0) < this.getQuestTargetCount(questId)) {
            return false;
        }
        this.activeQuests.remove(questId);
        this.completedQuests.add(questId);
        this.questTargetCounts.remove(questId);
        this.questRewardXp.remove(questId);
        this.questLocations.remove(questId);
        return true;
    }

    public boolean addQuest(String questId) {
        QuestDefinition definition = QuestRegistry.byId(questId);
        if (definition == null || this.activeQuests.contains(questId) || this.completedQuests.contains(questId)) {
            return false;
        }
        this.activeQuests.add(questId);
        this.questProgress.putIfAbsent(questId, 0);
        this.questTargetCounts.putIfAbsent(questId, definition.targetCount());
        this.questRewardXp.putIfAbsent(questId, definition.rewardXp());
        return true;
    }

    public boolean addScaledQuest(String questId, int targetCount, int rewardXp) {
        QuestDefinition definition = QuestRegistry.byId(questId);
        if (definition == null || this.activeQuests.contains(questId) || this.completedQuests.contains(questId)) {
            return false;
        }
        this.activeQuests.add(questId);
        this.questProgress.put(questId, 0);
        this.questTargetCounts.put(questId, Math.max(1, targetCount));
        this.questRewardXp.put(questId, Math.max(1, rewardXp));
        return true;
    }

    public boolean cancelQuest(String questId) {
        if (!this.activeQuests.remove(questId)) {
            return false;
        }
        this.questProgress.remove(questId);
        this.questTargetCounts.remove(questId);
        this.questRewardXp.remove(questId);
        this.questLocations.remove(questId);
        return true;
    }

    public void copyFrom(HunterPlayerData other) {
        this.level = other.level;
        this.xp = other.xp;
        this.skillPoints = other.skillPoints;
        this.guarding = false;
        this.guardTicks = 0;
        this.nenLevel = other.nenLevel;
        this.currentNen = other.currentNen;
        this.staminaRegenDelayTicks = other.staminaRegenDelayTicks;
        this.nenAuraColor = other.nenAuraColor;
        this.nenType = other.nenType;
        this.nenTechnique = other.nenTechnique;
        this.trait = other.trait;
        this.emperorTimeActive = other.emperorTimeActive;
        this.vowFaction = other.vowFaction;
        this.combatVowType = other.combatVowType;
        this.combatVowPercent = other.combatVowPercent;
        this.scarletEyesOffsetX = other.scarletEyesOffsetX;
        this.scarletEyesOffsetY = other.scarletEyesOffsetY;
        this.scarletLeftEyeOffsetX = other.scarletLeftEyeOffsetX;
        this.scarletLeftEyeOffsetY = other.scarletLeftEyeOffsetY;
        this.scarletLeftEyeLength = other.scarletLeftEyeLength;
        this.scarletLeftEyeVerticalLength = other.scarletLeftEyeVerticalLength;
        this.scarletRightEyeOffsetX = other.scarletRightEyeOffsetX;
        this.scarletRightEyeOffsetY = other.scarletRightEyeOffsetY;
        this.scarletRightEyeLength = other.scarletRightEyeLength;
        this.scarletRightEyeVerticalLength = other.scarletRightEyeVerticalLength;
        this.lungCapacity = other.lungCapacity;
        this.abilityVows.clear();
        this.abilityVows.putAll(other.abilityVows);
        this.judgmentDisabledAbilities.clear();
        this.judgmentDisabledAbilities.addAll(other.judgmentDisabledAbilities);
        this.pendingJudgmentChainTargetUuid = other.pendingJudgmentChainTargetUuid;
        this.deepPurpleSoldierMode = other.deepPurpleSoldierMode;
        this.deepPurpleCloneMode = other.deepPurpleCloneMode;
        this.deepPurpleSpottedTargetUuid = other.deepPurpleSpottedTargetUuid;
        this.deepPurpleSpottedTargetPos = other.deepPurpleSpottedTargetPos;
        this.deepPurpleSpottedTargetTicks = other.deepPurpleSpottedTargetTicks;
        this.nenQuestStage = other.nenQuestStage;
        this.tenUnlocked = other.tenUnlocked;
        this.zetsuUnlocked = other.zetsuUnlocked;
        this.renUnlocked = other.renUnlocked;
        this.gyoUnlocked = other.gyoUnlocked;
        this.enUnlocked = other.enUnlocked;
        this.shuUnlocked = other.shuUnlocked;
        this.koUnlocked = other.koUnlocked;
        this.kenUnlocked = other.kenUnlocked;
        this.ryuUnlocked = other.ryuUnlocked;
        this.feelAuraTicks = other.feelAuraTicks;
        this.meditationCountdownTicks = other.meditationCountdownTicks;
        this.meditationActive = other.meditationActive;
        this.meditationTicksRemaining = other.meditationTicksRemaining;
        this.meditationPromptKey = other.meditationPromptKey;
        this.meditationPromptTicksRemaining = other.meditationPromptTicksRemaining;
        this.meditationPromptSeed = other.meditationPromptSeed;
        this.nodeDamageTaken = other.nodeDamageTaken;
        this.endureLowHpTicks = other.endureLowHpTicks;
        this.zetsuCrouchTicks = other.zetsuCrouchTicks;
        this.zetsuTrialPrepTicks = other.zetsuTrialPrepTicks;
        this.zetsuTrialSearchTicks = other.zetsuTrialSearchTicks;
        this.zetsuTrialSearching = other.zetsuTrialSearching;
        this.zetsuTrialComplete = other.zetsuTrialComplete;
        this.zetsuTrialWingEntityId = other.zetsuTrialWingEntityId;
        this.zetsuTrialOrigin = other.zetsuTrialOrigin;
        this.renOverflowDamageWindow = other.renOverflowDamageWindow;
        this.renOverflowTicks = other.renOverflowTicks;
        this.auraBurstKills = other.auraBurstKills;
        this.auraBurstTicks = other.auraBurstTicks;
        this.enLookedMobCount = other.enLookedMobCount;
        this.lastNenLookTargetUuid = other.lastNenLookTargetUuid;
        this.nenLookCooldown = other.nenLookCooldown;
        this.shuWeaponRenTicks = other.shuWeaponRenTicks;
        this.koOneShotKills = other.koOneShotKills;
        this.kenBalanceTicks = other.kenBalanceTicks;
        this.ryuFightStarted = other.ryuFightStarted;
        this.ryuFightFinished = other.ryuFightFinished;
        this.tenActive = other.tenActive;
        this.zetsuActive = other.zetsuActive;
        this.renActive = other.renActive;
        this.enActive = other.enActive;
        this.koActive = other.koActive;
        this.kenActive = other.kenActive;
        this.ryuActive = other.ryuActive;
        this.zetsuForcedInvisibility = false;
        this.abilityCooldowns.clear();
        this.abilityCooldowns.putAll(other.abilityCooldowns);
        this.dashIFrameTicks = other.dashIFrameTicks;
        this.airJumpsUsed = 0;
        this.airLaunchFallProtection = other.airLaunchFallProtection;
        this.currentAnimation = other.currentAnimation;
        this.animationTicks = other.animationTicks;
        this.chargingAbilityId = other.chargingAbilityId;
        this.chargeTicksRemaining = other.chargeTicksRemaining;
        this.chargeDirectionX = other.chargeDirectionX;
        this.chargeDirectionY = other.chargeDirectionY;
        this.chargeDirectionZ = other.chargeDirectionZ;
        this.chargeTargetUuid = other.chargeTargetUuid;
        this.activeAbilityId = other.activeAbilityId;
        this.activeAbilityTicksRemaining = other.activeAbilityTicksRemaining;
        this.activeAbilityDirectionX = other.activeAbilityDirectionX;
        this.activeAbilityDirectionY = other.activeAbilityDirectionY;
        this.activeAbilityDirectionZ = other.activeAbilityDirectionZ;
        this.activeAbilityTargetUuid = other.activeAbilityTargetUuid;
        this.martialArtsGrabTargetUuid = other.martialArtsGrabTargetUuid;
        this.martialArtsGrabSourceAbilityId = other.martialArtsGrabSourceAbilityId;
        this.martialArtsGrabTicksRemaining = other.martialArtsGrabTicksRemaining;
        this.combatBarVisible = other.combatBarVisible;
        this.activeCombatBar = other.activeCombatBar;
        this.emptyHandsPickupEnabled = other.emptyHandsPickupEnabled;
        this.selectedSkillCategory = other.selectedSkillCategory;
        this.unlockedNenTechniqueNodes.clear();
        this.unlockedNenTechniqueNodes.addAll(other.unlockedNenTechniqueNodes);
        this.factionName = other.factionName;
        this.playerDisplayName = other.playerDisplayName;
        this.factionOwnerName = other.factionOwnerName;
        this.pendingFactionInviteName = other.pendingFactionInviteName;
        this.pendingFactionInviterName = other.pendingFactionInviterName;
        this.factionMembers.clear();
        this.factionMembers.addAll(other.factionMembers);
        this.invitablePlayers.clear();
        this.invitablePlayers.addAll(other.invitablePlayers);
        System.arraycopy(other.combatSlots, 0, this.combatSlots, 0, this.combatSlots.length);
        this.skillTreePoints.clear();
        this.skillTreePoints.putAll(other.skillTreePoints);
        this.activeQuests.clear();
        this.activeQuests.addAll(other.activeQuests);
        this.completedQuests.clear();
        this.completedQuests.addAll(other.completedQuests);
        this.questProgress.clear();
        this.questProgress.putAll(other.questProgress);
        this.questTargetCounts.clear();
        this.questTargetCounts.putAll(other.questTargetCounts);
        this.questRewardXp.clear();
        this.questRewardXp.putAll(other.questRewardXp);
        this.questLocations.clear();
        this.questLocations.putAll(other.questLocations);
        this.phoneQuestRefreshGameTime = other.phoneQuestRefreshGameTime;
        this.enTrackedPlayers.clear();
        recalculateAvailableSkillPoints();
        this.clearUnavailableCombatAbilities();
    }

    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("Level", this.level);
        tag.putInt("Xp", this.xp);
        tag.putInt("SkillPoints", this.skillPoints);
        tag.putBoolean("Guarding", this.guarding);
        tag.putInt("GuardTicks", this.guardTicks);
        tag.putInt("NenLevel", this.nenLevel);
        tag.putInt("CurrentNen", this.currentNen);
        tag.putInt("StaminaRegenDelayTicks", this.staminaRegenDelayTicks);
        tag.putDouble("NenRegenProgress", this.nenRegenProgress);
        tag.putInt("NenAuraColor", this.nenAuraColor);
        tag.putString("NenType", this.nenType);
        tag.putString("NenTechnique", this.nenTechnique);
        tag.putString("Trait", this.trait);
        tag.putBoolean("EmperorTimeActive", this.emperorTimeActive);
        tag.putString("VowFaction", this.vowFaction);
        tag.putString("CombatVowType", this.combatVowType);
        tag.putInt("CombatVowPercent", this.combatVowPercent);
        tag.putInt("ScarletEyesOffsetX", this.scarletEyesOffsetX);
        tag.putInt("ScarletEyesOffsetY", this.scarletEyesOffsetY);
        tag.putInt("ScarletLeftEyeOffsetX", this.scarletLeftEyeOffsetX);
        tag.putInt("ScarletLeftEyeOffsetY", this.scarletLeftEyeOffsetY);
        tag.putInt("ScarletLeftEyeLength", this.scarletLeftEyeLength);
        tag.putInt("ScarletLeftEyeVerticalLength", this.scarletLeftEyeVerticalLength);
        tag.putInt("ScarletRightEyeOffsetX", this.scarletRightEyeOffsetX);
        tag.putInt("ScarletRightEyeOffsetY", this.scarletRightEyeOffsetY);
        tag.putInt("ScarletRightEyeLength", this.scarletRightEyeLength);
        tag.putInt("ScarletRightEyeVerticalLength", this.scarletRightEyeVerticalLength);
        tag.putInt("LungCapacity", this.lungCapacity);
        CompoundTag vowTag = new CompoundTag();
        this.abilityVows.forEach(vowTag::putString);
        tag.put("AbilityVows", vowTag);
        tag.put("JudgmentDisabledAbilities", writeStringSet(this.judgmentDisabledAbilities));
        tag.putString("PendingJudgmentChainTargetUuid", this.pendingJudgmentChainTargetUuid);
        tag.putInt("DeepPurpleSoldierMode", this.deepPurpleSoldierMode);
        tag.putInt("DeepPurpleCloneMode", this.deepPurpleCloneMode);
        tag.putString("DeepPurpleSpottedTargetUuid", this.deepPurpleSpottedTargetUuid);
        tag.putInt("DeepPurpleSpottedTargetTicks", this.deepPurpleSpottedTargetTicks);
        if (this.deepPurpleSpottedTargetPos != null) {
            CompoundTag deepPurplePingTag = new CompoundTag();
            deepPurplePingTag.putInt("X", this.deepPurpleSpottedTargetPos.getX());
            deepPurplePingTag.putInt("Y", this.deepPurpleSpottedTargetPos.getY());
            deepPurplePingTag.putInt("Z", this.deepPurpleSpottedTargetPos.getZ());
            tag.put("DeepPurpleSpottedTargetPos", deepPurplePingTag);
        }
        tag.putString("NenQuestStage", this.nenQuestStage);
        tag.putBoolean("TenUnlocked", this.tenUnlocked);
        tag.putBoolean("ZetsuUnlocked", this.zetsuUnlocked);
        tag.putBoolean("RenUnlocked", this.renUnlocked);
        tag.putBoolean("GyoUnlocked", this.gyoUnlocked);
        tag.putBoolean("EnUnlocked", this.enUnlocked);
        tag.putBoolean("ShuUnlocked", this.shuUnlocked);
        tag.putBoolean("KoUnlocked", this.koUnlocked);
        tag.putBoolean("KenUnlocked", this.kenUnlocked);
        tag.putBoolean("RyuUnlocked", this.ryuUnlocked);
        tag.putInt("FeelAuraTicks", this.feelAuraTicks);
        tag.putInt("MeditationCountdownTicks", this.meditationCountdownTicks);
        tag.putBoolean("MeditationActive", this.meditationActive);
        tag.putInt("MeditationTicksRemaining", this.meditationTicksRemaining);
        tag.putString("MeditationPromptKey", this.meditationPromptKey);
        tag.putInt("MeditationPromptTicksRemaining", this.meditationPromptTicksRemaining);
        tag.putInt("MeditationPromptSeed", this.meditationPromptSeed);
        tag.putBoolean("NodeDamageTaken", this.nodeDamageTaken);
        tag.putInt("EndureLowHpTicks", this.endureLowHpTicks);
        tag.putInt("ZetsuCrouchTicks", this.zetsuCrouchTicks);
        tag.putInt("ZetsuTrialPrepTicks", this.zetsuTrialPrepTicks);
        tag.putInt("ZetsuTrialSearchTicks", this.zetsuTrialSearchTicks);
        tag.putBoolean("ZetsuTrialSearching", this.zetsuTrialSearching);
        tag.putBoolean("ZetsuTrialComplete", this.zetsuTrialComplete);
        tag.putInt("ZetsuTrialWingEntityId", this.zetsuTrialWingEntityId);
        if (this.zetsuTrialOrigin != null) {
            CompoundTag zetsuOriginTag = new CompoundTag();
            zetsuOriginTag.putInt("X", this.zetsuTrialOrigin.getX());
            zetsuOriginTag.putInt("Y", this.zetsuTrialOrigin.getY());
            zetsuOriginTag.putInt("Z", this.zetsuTrialOrigin.getZ());
            tag.put("ZetsuTrialOrigin", zetsuOriginTag);
        }
        tag.putFloat("RenOverflowDamageWindow", this.renOverflowDamageWindow);
        tag.putInt("RenOverflowTicks", this.renOverflowTicks);
        tag.putInt("AuraBurstKills", this.auraBurstKills);
        tag.putInt("AuraBurstTicks", this.auraBurstTicks);
        tag.putInt("EnLookedMobCount", this.enLookedMobCount);
        tag.putString("LastNenLookTargetUuid", this.lastNenLookTargetUuid);
        tag.putInt("NenLookCooldown", this.nenLookCooldown);
        tag.putInt("ShuWeaponRenTicks", this.shuWeaponRenTicks);
        tag.putInt("KoOneShotKills", this.koOneShotKills);
        tag.putInt("KenBalanceTicks", this.kenBalanceTicks);
        tag.putBoolean("RyuFightStarted", this.ryuFightStarted);
        tag.putBoolean("RyuFightFinished", this.ryuFightFinished);
        tag.putBoolean("TenActive", this.tenActive);
        tag.putBoolean("ZetsuActive", this.zetsuActive);
        tag.putBoolean("RenActive", this.renActive);
        tag.putBoolean("EnActive", this.enActive);
        tag.putBoolean("KoActive", this.koActive);
        tag.putBoolean("KenActive", this.kenActive);
        tag.putBoolean("RyuActive", this.ryuActive);
        tag.putBoolean("ZetsuForcedInvisibility", this.zetsuForcedInvisibility);
        tag.putInt("DashIFrames", this.dashIFrameTicks);
        tag.putInt("AirJumpsUsed", this.airJumpsUsed);
        tag.putBoolean("AirLaunchFallProtection", this.airLaunchFallProtection);
        tag.putString("CurrentAnimation", this.currentAnimation);
        tag.putInt("AnimationTicks", this.animationTicks);
        tag.putString("ChargingAbilityId", this.chargingAbilityId);
        tag.putInt("ChargeTicksRemaining", this.chargeTicksRemaining);
        tag.putDouble("ChargeDirectionX", this.chargeDirectionX);
        tag.putDouble("ChargeDirectionY", this.chargeDirectionY);
        tag.putDouble("ChargeDirectionZ", this.chargeDirectionZ);
        tag.putString("ChargeTargetUuid", this.chargeTargetUuid);
        tag.putString("ActiveAbilityId", this.activeAbilityId);
        tag.putInt("ActiveAbilityTicksRemaining", this.activeAbilityTicksRemaining);
        tag.putDouble("ActiveAbilityDirectionX", this.activeAbilityDirectionX);
        tag.putDouble("ActiveAbilityDirectionY", this.activeAbilityDirectionY);
        tag.putDouble("ActiveAbilityDirectionZ", this.activeAbilityDirectionZ);
        tag.putString("ActiveAbilityTargetUuid", this.activeAbilityTargetUuid);
        tag.putString("MartialArtsGrabTargetUuid", this.martialArtsGrabTargetUuid);
        tag.putString("MartialArtsGrabSourceAbilityId", this.martialArtsGrabSourceAbilityId);
        tag.putInt("MartialArtsGrabTicksRemaining", this.martialArtsGrabTicksRemaining);
        tag.putBoolean("CombatBarVisible", this.combatBarVisible);
        tag.putInt("ActiveCombatBar", this.activeCombatBar);
        tag.putBoolean("EmptyHandsPickupEnabled", this.emptyHandsPickupEnabled);
        tag.putString("SelectedSkillCategory", this.selectedSkillCategory);
        tag.putString("FactionName", this.factionName);
        tag.putString("PlayerDisplayName", this.playerDisplayName);
        tag.putString("FactionOwnerName", this.factionOwnerName);
        tag.putString("PendingFactionInviteName", this.pendingFactionInviteName);
        tag.putString("PendingFactionInviterName", this.pendingFactionInviterName);
        tag.put("FactionMembers", writeStringSet(this.factionMembers));
        tag.put("InvitablePlayers", writeStringSet(this.invitablePlayers));
        tag.put("ActiveQuests", writeStringSet(this.activeQuests));
        tag.put("CompletedQuests", writeStringSet(this.completedQuests));
        CompoundTag combatSlotsTag = new CompoundTag();
        for (int bar = 0; bar < COMBAT_BAR_COUNT; bar++) {
            for (int slot = 0; slot < COMBAT_SLOT_COUNT; slot++) {
                combatSlotsTag.putString("Bar" + bar + "Slot" + slot, this.getCombatSlot(bar, slot));
            }
        }
        tag.put("CombatSlots", combatSlotsTag);

        CompoundTag cooldownTag = new CompoundTag();
        this.abilityCooldowns.forEach(cooldownTag::putInt);
        tag.put("AbilityCooldowns", cooldownTag);

        CompoundTag skillTreeTag = new CompoundTag();
        this.skillTreePoints.forEach(skillTreeTag::putInt);
        tag.put("SkillTreePoints", skillTreeTag);
        tag.put("UnlockedNenTechniqueNodes", writeStringSet(this.unlockedNenTechniqueNodes));

        CompoundTag questTag = new CompoundTag();
        this.questProgress.forEach(questTag::putInt);
        tag.put("QuestProgress", questTag);
        CompoundTag questTargetTag = new CompoundTag();
        this.questTargetCounts.forEach(questTargetTag::putInt);
        tag.put("QuestTargetCounts", questTargetTag);
        CompoundTag questRewardTag = new CompoundTag();
        this.questRewardXp.forEach(questRewardTag::putInt);
        tag.put("QuestRewardXp", questRewardTag);
        CompoundTag questLocationTag = new CompoundTag();
        this.questLocations.forEach((questId, pos) -> {
            CompoundTag posTag = new CompoundTag();
            posTag.putInt("X", pos.getX());
            posTag.putInt("Y", pos.getY());
            posTag.putInt("Z", pos.getZ());
            questLocationTag.put(questId, posTag);
        });
        tag.put("QuestLocations", questLocationTag);
        tag.putLong("PhoneQuestRefreshGameTime", this.phoneQuestRefreshGameTime);
        return tag;
    }

    public void deserializeNBT(CompoundTag tag) {
        this.level = Math.max(1, Math.min(MAX_LEVEL, tag.getInt("Level")));
        this.xp = tag.getInt("Xp");
        this.skillPoints = Math.max(0, Math.min(MAX_SKILL_POINTS, tag.getInt("SkillPoints")));
        this.guarding = tag.getBoolean("Guarding");
        this.guardTicks = tag.getInt("GuardTicks");
        this.nenLevel = Math.max(0, Math.min(MAX_NEN_LEVEL, tag.getInt("NenLevel")));
        this.currentNen = tag.contains("CurrentNen") ? Math.max(0, tag.getInt("CurrentNen")) : this.getMaxStamina();
        this.staminaRegenDelayTicks = tag.getInt("StaminaRegenDelayTicks");
        this.nenRegenProgress = tag.getDouble("NenRegenProgress");
        this.nenAuraColor = tag.contains("NenAuraColor") ? tag.getInt("NenAuraColor") & 0xFFFFFF : 0x5BE5FF;
        this.nenType = tag.contains("NenType") ? tag.getString("NenType") : "";
        this.nenTechnique = tag.contains("NenTechnique") ? tag.getString("NenTechnique") : "";
        this.trait = tag.contains("Trait") ? TraitType.byId(tag.getString("Trait")).id() : TraitType.NONE.id();
        this.emperorTimeActive = tag.getBoolean("EmperorTimeActive");
        this.vowFaction = tag.contains("VowFaction") ? tag.getString("VowFaction") : "";
        this.setCombatVow(tag.contains("CombatVowType") ? tag.getString("CombatVowType") : "", tag.contains("CombatVowPercent") ? tag.getInt("CombatVowPercent") : 25);
        this.scarletEyesOffsetX = tag.getInt("ScarletEyesOffsetX");
        this.scarletEyesOffsetY = tag.getInt("ScarletEyesOffsetY");
        if (tag.contains("ScarletLeftEyeOffsetX")) {
            this.setScarletEyesLayout(
                    tag.getInt("ScarletLeftEyeOffsetX"),
                    tag.getInt("ScarletLeftEyeOffsetY"),
                    tag.contains("ScarletLeftEyeLength") ? migrateScarletEyeLength(tag.getInt("ScarletLeftEyeLength"), true) : -1,
                    tag.contains("ScarletLeftEyeVerticalLength") ? migrateScarletEyeLength(tag.getInt("ScarletLeftEyeVerticalLength"), false) : 1,
                    tag.getInt("ScarletRightEyeOffsetX"),
                    tag.getInt("ScarletRightEyeOffsetY"),
                    tag.contains("ScarletRightEyeLength") ? migrateScarletEyeLength(tag.getInt("ScarletRightEyeLength"), false) : 1,
                    tag.contains("ScarletRightEyeVerticalLength") ? migrateScarletEyeLength(tag.getInt("ScarletRightEyeVerticalLength"), false) : 1);
        } else {
            this.setScarletEyesOffset(this.scarletEyesOffsetX, this.scarletEyesOffsetY);
        }
        this.lungCapacity = tag.getInt("LungCapacity");
        this.abilityVows.clear();
        CompoundTag vowTag = tag.getCompound("AbilityVows");
        for (String key : vowTag.getAllKeys()) {
            this.abilityVows.put(key, vowTag.getString(key));
        }
        if (!this.vowFaction.isBlank() && !this.abilityVows.containsKey("chain_jail")) {
            this.abilityVows.put("chain_jail", this.vowFaction);
        }
        this.judgmentDisabledAbilities.clear();
        this.judgmentDisabledAbilities.addAll(readStringSet(tag.getList("JudgmentDisabledAbilities", Tag.TAG_STRING)));
        this.pendingJudgmentChainTargetUuid = tag.contains("PendingJudgmentChainTargetUuid") ? tag.getString("PendingJudgmentChainTargetUuid") : "";
        this.deepPurpleSoldierMode = Math.max(0, Math.min(1, tag.getInt("DeepPurpleSoldierMode")));
        this.deepPurpleCloneMode = Math.max(0, Math.min(1, tag.getInt("DeepPurpleCloneMode")));
        this.deepPurpleSpottedTargetUuid = tag.contains("DeepPurpleSpottedTargetUuid") ? tag.getString("DeepPurpleSpottedTargetUuid") : "";
        this.deepPurpleSpottedTargetTicks = tag.getInt("DeepPurpleSpottedTargetTicks");
        this.deepPurpleSpottedTargetPos = tag.contains("DeepPurpleSpottedTargetPos")
                ? new BlockPos(tag.getCompound("DeepPurpleSpottedTargetPos").getInt("X"), tag.getCompound("DeepPurpleSpottedTargetPos").getInt("Y"), tag.getCompound("DeepPurpleSpottedTargetPos").getInt("Z"))
                : null;
        if (this.nenTechnique.isBlank() && "MANIPULATION".equalsIgnoreCase(this.nenType)) {
            this.nenTechnique = "deep_purple";
        } else if (this.nenTechnique.isBlank() && "CONJURATION".equalsIgnoreCase(this.nenType)) {
            this.nenTechnique = "chain_nen";
        }
        this.nenQuestStage = tag.contains("NenQuestStage") ? tag.getString("NenQuestStage") : NenQuestStage.NOT_STARTED.name();
        this.tenUnlocked = tag.getBoolean("TenUnlocked");
        this.zetsuUnlocked = tag.getBoolean("ZetsuUnlocked");
        this.renUnlocked = tag.getBoolean("RenUnlocked");
        this.gyoUnlocked = tag.getBoolean("GyoUnlocked");
        this.enUnlocked = tag.getBoolean("EnUnlocked");
        this.shuUnlocked = tag.getBoolean("ShuUnlocked");
        this.koUnlocked = tag.getBoolean("KoUnlocked");
        this.kenUnlocked = tag.getBoolean("KenUnlocked");
        this.ryuUnlocked = tag.getBoolean("RyuUnlocked");
        this.feelAuraTicks = tag.getInt("FeelAuraTicks");
        this.meditationCountdownTicks = tag.getInt("MeditationCountdownTicks");
        this.meditationActive = tag.getBoolean("MeditationActive");
        this.meditationTicksRemaining = tag.getInt("MeditationTicksRemaining");
        this.meditationPromptKey = tag.contains("MeditationPromptKey") ? tag.getString("MeditationPromptKey") : "";
        this.meditationPromptTicksRemaining = tag.getInt("MeditationPromptTicksRemaining");
        this.meditationPromptSeed = tag.getInt("MeditationPromptSeed");
        this.nodeDamageTaken = tag.getBoolean("NodeDamageTaken");
        this.endureLowHpTicks = tag.getInt("EndureLowHpTicks");
        this.zetsuCrouchTicks = tag.getInt("ZetsuCrouchTicks");
        this.zetsuTrialPrepTicks = tag.getInt("ZetsuTrialPrepTicks");
        this.zetsuTrialSearchTicks = tag.getInt("ZetsuTrialSearchTicks");
        this.zetsuTrialSearching = tag.getBoolean("ZetsuTrialSearching");
        this.zetsuTrialComplete = tag.getBoolean("ZetsuTrialComplete");
        this.zetsuTrialWingEntityId = tag.contains("ZetsuTrialWingEntityId") ? tag.getInt("ZetsuTrialWingEntityId") : -1;
        this.zetsuTrialOrigin = tag.contains("ZetsuTrialOrigin") ? new BlockPos(tag.getCompound("ZetsuTrialOrigin").getInt("X"), tag.getCompound("ZetsuTrialOrigin").getInt("Y"), tag.getCompound("ZetsuTrialOrigin").getInt("Z")) : null;
        this.renOverflowDamageWindow = tag.getFloat("RenOverflowDamageWindow");
        this.renOverflowTicks = tag.getInt("RenOverflowTicks");
        this.auraBurstKills = tag.getInt("AuraBurstKills");
        this.auraBurstTicks = tag.getInt("AuraBurstTicks");
        this.enLookedMobCount = tag.getInt("EnLookedMobCount");
        this.lastNenLookTargetUuid = tag.contains("LastNenLookTargetUuid") ? tag.getString("LastNenLookTargetUuid") : "";
        this.nenLookCooldown = tag.getInt("NenLookCooldown");
        this.shuWeaponRenTicks = tag.getInt("ShuWeaponRenTicks");
        this.koOneShotKills = tag.getInt("KoOneShotKills");
        this.kenBalanceTicks = tag.getInt("KenBalanceTicks");
        this.ryuFightStarted = tag.getBoolean("RyuFightStarted");
        this.ryuFightFinished = tag.getBoolean("RyuFightFinished");
        this.tenActive = tag.getBoolean("TenActive");
        this.zetsuActive = tag.getBoolean("ZetsuActive");
        this.renActive = tag.getBoolean("RenActive");
        this.enActive = tag.getBoolean("EnActive");
        this.koActive = tag.getBoolean("KoActive");
        this.kenActive = tag.getBoolean("KenActive");
        this.ryuActive = tag.getBoolean("RyuActive");
        this.zetsuForcedInvisibility = tag.getBoolean("ZetsuForcedInvisibility");
        this.dashIFrameTicks = tag.getInt("DashIFrames");
        this.airJumpsUsed = tag.getInt("AirJumpsUsed");
        this.airLaunchFallProtection = tag.getBoolean("AirLaunchFallProtection");
        this.currentAnimation = tag.contains("CurrentAnimation") ? tag.getString("CurrentAnimation") : AnimationType.NONE.name();
        this.animationTicks = tag.getInt("AnimationTicks");
        this.chargingAbilityId = tag.contains("ChargingAbilityId") ? tag.getString("ChargingAbilityId") : "";
        this.chargeTicksRemaining = tag.getInt("ChargeTicksRemaining");
        this.chargeDirectionX = tag.getDouble("ChargeDirectionX");
        this.chargeDirectionY = tag.getDouble("ChargeDirectionY");
        this.chargeDirectionZ = tag.getDouble("ChargeDirectionZ");
        this.chargeTargetUuid = tag.contains("ChargeTargetUuid") ? tag.getString("ChargeTargetUuid") : "";
        this.activeAbilityId = tag.contains("ActiveAbilityId") ? tag.getString("ActiveAbilityId") : "";
        this.activeAbilityTicksRemaining = tag.getInt("ActiveAbilityTicksRemaining");
        this.activeAbilityDirectionX = tag.getDouble("ActiveAbilityDirectionX");
        this.activeAbilityDirectionY = tag.getDouble("ActiveAbilityDirectionY");
        this.activeAbilityDirectionZ = tag.getDouble("ActiveAbilityDirectionZ");
        this.activeAbilityTargetUuid = tag.contains("ActiveAbilityTargetUuid") ? tag.getString("ActiveAbilityTargetUuid") : "";
        this.martialArtsGrabTargetUuid = tag.contains("MartialArtsGrabTargetUuid") ? tag.getString("MartialArtsGrabTargetUuid") : "";
        this.martialArtsGrabSourceAbilityId = tag.contains("MartialArtsGrabSourceAbilityId") ? tag.getString("MartialArtsGrabSourceAbilityId") : "";
        this.martialArtsGrabTicksRemaining = tag.getInt("MartialArtsGrabTicksRemaining");
        this.combatBarVisible = !tag.contains("CombatBarVisible") || tag.getBoolean("CombatBarVisible");
        this.activeCombatBar = Math.max(0, Math.min(COMBAT_BAR_COUNT - 1, tag.getInt("ActiveCombatBar")));
        this.emptyHandsPickupEnabled = tag.getBoolean("EmptyHandsPickupEnabled");
        this.selectedSkillCategory = tag.contains("SelectedSkillCategory") ? tag.getString("SelectedSkillCategory") : "";
        this.factionName = tag.contains("FactionName") ? tag.getString("FactionName") : "";
        this.playerDisplayName = tag.contains("PlayerDisplayName") ? tag.getString("PlayerDisplayName") : "";
        this.factionOwnerName = tag.contains("FactionOwnerName") ? tag.getString("FactionOwnerName") : "";
        this.pendingFactionInviteName = tag.contains("PendingFactionInviteName") ? tag.getString("PendingFactionInviteName") : "";
        this.pendingFactionInviterName = tag.contains("PendingFactionInviterName") ? tag.getString("PendingFactionInviterName") : "";
        this.factionMembers.clear();
        this.factionMembers.addAll(readStringSet(tag.getList("FactionMembers", Tag.TAG_STRING)));
        this.invitablePlayers.clear();
        this.invitablePlayers.addAll(readStringSet(tag.getList("InvitablePlayers", Tag.TAG_STRING)));
        CompoundTag combatSlotsTag = tag.getCompound("CombatSlots");
        for (int i = 0; i < this.combatSlots.length; i++) {
            this.combatSlots[i] = "";
        }
        boolean hasBarFormat = combatSlotsTag.contains("Bar0Slot0");
        if (hasBarFormat) {
            for (int bar = 0; bar < COMBAT_BAR_COUNT; bar++) {
                for (int slot = 0; slot < COMBAT_SLOT_COUNT; slot++) {
                    this.combatSlots[toCombatSlotIndex(bar, slot)] = combatSlotsTag.getString("Bar" + bar + "Slot" + slot);
                }
            }
        } else {
            for (int slot = 0; slot < COMBAT_SLOT_COUNT; slot++) {
                this.combatSlots[toCombatSlotIndex(0, slot)] = combatSlotsTag.getString("Slot" + slot);
            }
        }

        this.abilityCooldowns.clear();
        CompoundTag cooldownTag = tag.getCompound("AbilityCooldowns");
        for (String key : cooldownTag.getAllKeys()) {
            this.abilityCooldowns.put(key, cooldownTag.getInt(key));
        }

        this.skillTreePoints.clear();
        CompoundTag skillTreeTag = tag.getCompound("SkillTreePoints");
        for (String key : skillTreeTag.getAllKeys()) {
            this.skillTreePoints.put(key, skillTreeTag.getInt(key));
        }
        this.unlockedNenTechniqueNodes.clear();
        this.unlockedNenTechniqueNodes.addAll(readStringSet(tag.getList("UnlockedNenTechniqueNodes", Tag.TAG_STRING)));
        syncNenTechniqueNodes();
        if (this.skillTreePoints.isEmpty()) {
            migrateLegacySkillNodes(tag.getList("UnlockedNodes", Tag.TAG_STRING));
        }
        if (this.skillTreePoints.isEmpty()) {
            this.selectSkillCategory(null);
        } else if (this.getSelectedSkillCategory() == null) {
            for (SkillNode node : SkillNode.values()) {
                if (this.getStylePoints(node) > 0) {
                    this.selectSkillCategory(node.category());
                    break;
                }
            }
        }
        this.activeQuests.clear();
        this.activeQuests.addAll(readStringSet(tag.getList("ActiveQuests", Tag.TAG_STRING)));
        this.completedQuests.clear();
        this.completedQuests.addAll(readStringSet(tag.getList("CompletedQuests", Tag.TAG_STRING)));

        this.questProgress.clear();
        CompoundTag questTag = tag.getCompound("QuestProgress");
        for (String key : questTag.getAllKeys()) {
            this.questProgress.put(key, questTag.getInt(key));
        }
        this.questTargetCounts.clear();
        CompoundTag questTargetTag = tag.getCompound("QuestTargetCounts");
        for (String key : questTargetTag.getAllKeys()) {
            this.questTargetCounts.put(key, questTargetTag.getInt(key));
        }
        this.questRewardXp.clear();
        CompoundTag questRewardTag = tag.getCompound("QuestRewardXp");
        for (String key : questRewardTag.getAllKeys()) {
            this.questRewardXp.put(key, questRewardTag.getInt(key));
        }
        this.questLocations.clear();
        CompoundTag questLocationTag = tag.getCompound("QuestLocations");
        for (String key : questLocationTag.getAllKeys()) {
            CompoundTag posTag = questLocationTag.getCompound(key);
            this.questLocations.put(key, new BlockPos(posTag.getInt("X"), posTag.getInt("Y"), posTag.getInt("Z")));
        }
        this.phoneQuestRefreshGameTime = tag.getLong("PhoneQuestRefreshGameTime");
        this.setNenLevel(this.nenLevel);
        this.setCurrentNen(this.currentNen);
        if (this.zetsuActive) {
            this.setZetsuActive(true);
        } else {
            this.setTenActive(this.tenActive);
            this.setRenActive(this.renActive);
            this.setEnActive(this.enActive);
            this.setKoActive(this.koActive);
            this.setKenActive(this.kenActive);
            this.setRyuActive(this.ryuActive);
        }
        recalculateAvailableSkillPoints();
        this.clearUnavailableCombatAbilities();
    }

    private static int migrateScarletEyeLength(int length, boolean leftEye) {
        if (length == 6) {
            return leftEye ? -1 : 1;
        }
        return length;
    }

    private void recalculateAvailableSkillPoints() {
        int totalAllowed = Math.min(MAX_SKILL_POINTS, this.level);
        trimSkillTreePointsToAllowed(totalAllowed);
        int spentPoints = this.skillTreePoints.values().stream().mapToInt(Integer::intValue).sum();
        this.skillPoints = Math.max(0, totalAllowed - spentPoints);
        this.clearUnavailableCombatAbilities();
    }

    private void trimSkillTreePointsToAllowed(int totalAllowed) {
        int spentPoints = this.skillTreePoints.values().stream().mapToInt(Integer::intValue).sum();
        if (spentPoints <= totalAllowed) {
            return;
        }

        int excess = spentPoints - totalAllowed;
        SkillNode[] nodes = SkillNode.values();
        for (int i = nodes.length - 1; i >= 0 && excess > 0; i--) {
            SkillNode node = nodes[i];
            int current = this.getStylePoints(node);
            if (current <= 0) {
                continue;
            }
            int remove = Math.min(current, excess);
            int next = current - remove;
            if (next <= 0) {
                this.skillTreePoints.remove(node.id());
            } else {
                this.skillTreePoints.put(node.id(), next);
            }
            excess -= remove;
        }

        if (this.skillTreePoints.isEmpty()) {
            this.selectSkillCategory(null);
        }
    }

    private void migrateLegacySkillNodes(ListTag legacyNodes) {
        for (int i = 0; i < legacyNodes.size(); i++) {
            SkillNode migratedNode = switch (legacyNodes.getString(i)) {
                case "weapon_speed" -> SkillNode.SPEED;
                case "weapon_defensive" -> SkillNode.DEFENSE;
                case "boxer" -> SkillNode.BOXING;
                case "martial_arts" -> SkillNode.MARTIAL_ARTS;
                default -> null;
            };
            if (migratedNode != null) {
                this.skillTreePoints.put(migratedNode.id(), Math.max(1, this.getStylePoints(migratedNode)));
            }
        }
    }

    private void clearUnavailableCombatAbilities() {
        for (int i = 0; i < this.combatSlots.length; i++) {
            String abilityId = this.combatSlots[i];
            if (!HunterAbilities.canEquipToCombatBar(abilityId) || !HunterAbilities.isCombatAbilityUnlocked(this, abilityId)) {
                this.combatSlots[i] = "";
            }
        }
    }

    private void clearCombatAbility(String abilityId, int exceptIndex) {
        for (int i = 0; i < this.combatSlots.length; i++) {
            if (i != exceptIndex && abilityId.equals(this.combatSlots[i])) {
                this.combatSlots[i] = "";
            }
        }
    }

    private static int toCombatSlotIndex(int barIndex, int slotIndex) {
        return (barIndex * COMBAT_SLOT_COUNT) + slotIndex;
    }

    private static ListTag writeStringSet(Set<String> values) {
        ListTag list = new ListTag();
        for (String value : values) {
            list.add(StringTag.valueOf(value));
        }
        return list;
    }

    private static Set<String> readStringSet(ListTag list) {
        Set<String> values = new HashSet<>();
        for (int i = 0; i < list.size(); i++) {
            values.add(list.getString(i));
        }
        return values;
    }

}
