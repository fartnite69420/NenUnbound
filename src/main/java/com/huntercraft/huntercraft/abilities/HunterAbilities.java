package com.huntercraft.huntercraft.abilities;

import com.huntercraft.huntercraft.abilities.base.BaseTechniqueAbility;
import com.huntercraft.huntercraft.abilities.base.DashAbility;
import com.huntercraft.huntercraft.abilities.base.DoubleJumpAbility;
import com.huntercraft.huntercraft.abilities.base.GuardAbility;
import com.huntercraft.huntercraft.abilities.boxertree.AtlasDropAbility;
import com.huntercraft.huntercraft.abilities.boxertree.BreakerFistsPassiveAbility;
import com.huntercraft.huntercraft.abilities.boxertree.CrossfireBarrageAbility;
import com.huntercraft.huntercraft.abilities.boxertree.HammerJabAbility;
import com.huntercraft.huntercraft.abilities.boxertree.LiverBreakCounterAbility;
import com.huntercraft.huntercraft.abilities.boxertree.RedirectionRushAbility;
import com.huntercraft.huntercraft.abilities.boxertree.RuptureStepAbility;
import com.huntercraft.huntercraft.abilities.boxertree.TitanCannonAbility;
import com.huntercraft.huntercraft.abilities.ChainConjuration.ChainJailAbility;
import com.huntercraft.huntercraft.abilities.ChainConjuration.DowsingChainAbility;
import com.huntercraft.huntercraft.abilities.ChainConjuration.HolyChainAbility;
import com.huntercraft.huntercraft.abilities.ChainConjuration.JudgmentChainAbility;
import com.huntercraft.huntercraft.abilities.ChainConjuration.StealChainAbility;
import com.huntercraft.huntercraft.abilities.deeppurple.SmokeCloneAbility;
import com.huntercraft.huntercraft.abilities.deeppurple.SmokeSoldierAbility;
import com.huntercraft.huntercraft.abilities.deeppurple.SmokeyJailAbility;
import com.huntercraft.huntercraft.abilities.deeppurple.SmokeyChainAbility;
import com.huntercraft.huntercraft.abilities.boxertree.SlipGuardPassiveAbility;
import com.huntercraft.huntercraft.abilities.defensetree.AegisRushAbility;
import com.huntercraft.huntercraft.abilities.defensetree.BulwarkCannonAbility;
import com.huntercraft.huntercraft.abilities.defensetree.HeavenSplitterAbility;
import com.huntercraft.huntercraft.abilities.defensetree.IchimonjiAbility;
import com.huntercraft.huntercraft.abilities.defensetree.IronGuardPassiveAbility;
import com.huntercraft.huntercraft.abilities.defensetree.MirrorReprisalAbility;
import com.huntercraft.huntercraft.abilities.defensetree.PerfectReadPassiveAbility;
import com.huntercraft.huntercraft.abilities.defensetree.SkybreakerDiveAbility;
import com.huntercraft.huntercraft.abilities.defensetree.SteelTatsumakiAbility;
import com.huntercraft.huntercraft.abilities.bungeegum.GumAttachAbility;
import com.huntercraft.huntercraft.abilities.bungeegum.PullAbility;
import com.huntercraft.huntercraft.abilities.bungeegum.ReflectAbility;
import com.huntercraft.huntercraft.abilities.bungeegum.TextureSurpriseAbility;
import com.huntercraft.huntercraft.abilities.bungeegum.TrapAbility;
import com.huntercraft.huntercraft.abilities.martialartstree.AnkleSplitterAbility;
import com.huntercraft.huntercraft.abilities.martialartstree.CrazedBlitzAbility;
import com.huntercraft.huntercraft.abilities.martialartstree.FlowStatePassiveAbility;
import com.huntercraft.huntercraft.abilities.martialartstree.FlowStepPassiveAbility;
import com.huntercraft.huntercraft.abilities.martialartstree.MeteorHeelAbility;
import com.huntercraft.huntercraft.abilities.martialartstree.RisingShotAbility;
import com.huntercraft.huntercraft.abilities.martialartstree.ToraHuntAbility;
import com.huntercraft.huntercraft.abilities.martialartstree.TripleStrikerAbility;
import com.huntercraft.huntercraft.abilities.martialartstree.WhirlwindArcAbility;
import com.huntercraft.huntercraft.abilities.nenability.EnAbility;
import com.huntercraft.huntercraft.abilities.nenability.KenAbility;
import com.huntercraft.huntercraft.abilities.nenability.KoAbility;
import com.huntercraft.huntercraft.abilities.nenability.EmperorTimeAbility;
import com.huntercraft.huntercraft.abilities.nenability.NenTechniqueAbility;
import com.huntercraft.huntercraft.abilities.nenability.RenAbility;
import com.huntercraft.huntercraft.abilities.nenability.RyuAbility;
import com.huntercraft.huntercraft.abilities.nenability.TenAbility;
import com.huntercraft.huntercraft.abilities.nenability.ZetsuAbility;
import com.huntercraft.huntercraft.abilities.speedtree.FlashCleaveAbility;
import com.huntercraft.huntercraft.abilities.speedtree.FlashAccelPassiveAbility;
import com.huntercraft.huntercraft.abilities.speedtree.GhostStepAbility;
import com.huntercraft.huntercraft.abilities.speedtree.LionFangDrawAbility;
import com.huntercraft.huntercraft.abilities.speedtree.PhantomRingAbility;
import com.huntercraft.huntercraft.abilities.speedtree.AcuteAbility;
import com.huntercraft.huntercraft.abilities.speedtree.StepDrivePassiveAbility;
import com.huntercraft.huntercraft.abilities.speedtree.UnseenBladeAbility;
import com.huntercraft.huntercraft.abilities.speedtree.VoidRendAbility;
import com.huntercraft.huntercraft.ability.HunterAbility;
import com.huntercraft.huntercraft.data.HunterPlayerData;
import com.huntercraft.huntercraft.progression.SkillNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class HunterAbilities {
    public static final DoubleJumpAbility DOUBLE_JUMP = new DoubleJumpAbility();
    public static final DashAbility DASH = new DashAbility();
    public static final GuardAbility GUARD = new GuardAbility();
    public static final TenAbility TEN = new TenAbility();
    public static final ZetsuAbility ZETSU = new ZetsuAbility();
    public static final RenAbility REN = new RenAbility();
    public static final EnAbility EN = new EnAbility();
    public static final KoAbility KO = new KoAbility();
    public static final KenAbility KEN = new KenAbility();
    public static final RyuAbility RYU = new RyuAbility();
    public static final EmperorTimeAbility EMPEROR_TIME = new EmperorTimeAbility();
    public static final WeaponMasteryPassiveAbility WEAPON_MASTERY = new WeaponMasteryPassiveAbility();
    public static final PhysicalPowerPassiveAbility PHYSICAL_POWER = new PhysicalPowerPassiveAbility();
    public static final StepDrivePassiveAbility STEP_DRIVE = new StepDrivePassiveAbility();
    public static final FlashCleaveAbility FLASH_CLEAVE = new FlashCleaveAbility();
    public static final GhostStepAbility GHOST_STEP = new GhostStepAbility();
    public static final FlashAccelPassiveAbility FLASH_ACCEL = new FlashAccelPassiveAbility();
    public static final AcuteAbility ACUTE = new AcuteAbility();
    public static final LionFangDrawAbility LION_FANG_DRAW = new LionFangDrawAbility();
    public static final VoidRendAbility VOID_REND = new VoidRendAbility();
    public static final PhantomRingAbility PHANTOM_RING = new PhantomRingAbility();
    public static final UnseenBladeAbility UNSEEN_BLADE = new UnseenBladeAbility();
    public static final IronGuardPassiveAbility IRON_GUARD = new IronGuardPassiveAbility();
    public static final PerfectReadPassiveAbility PERFECT_READ = new PerfectReadPassiveAbility();
    public static final HeavenSplitterAbility HEAVEN_SPLITTER = new HeavenSplitterAbility();
    public static final SkybreakerDiveAbility SKYBREAKER_DIVE = new SkybreakerDiveAbility();
    public static final AegisRushAbility AEGIS_RUSH = new AegisRushAbility();
    public static final MirrorReprisalAbility MIRROR_REPRISAL = new MirrorReprisalAbility();
    public static final SteelTatsumakiAbility STEEL_TATSUMAKI = new SteelTatsumakiAbility();
    public static final BulwarkCannonAbility BULWARK_CANNON = new BulwarkCannonAbility();
    public static final IchimonjiAbility ICHIMONJI = new IchimonjiAbility();
    public static final SlipGuardPassiveAbility SLIP_GUARD = new SlipGuardPassiveAbility();
    public static final BreakerFistsPassiveAbility BREAKER_FISTS = new BreakerFistsPassiveAbility();
    public static final HammerJabAbility HAMMER_JAB = new HammerJabAbility();
    public static final CrossfireBarrageAbility CROSSFIRE_BARRAGE = new CrossfireBarrageAbility();
    public static final LiverBreakCounterAbility LIVER_BREAK_COUNTER = new LiverBreakCounterAbility();
    public static final RedirectionRushAbility REDIRECTION_RUSH = new RedirectionRushAbility();
    public static final AtlasDropAbility ATLAS_DROP = new AtlasDropAbility();
    public static final TitanCannonAbility TITAN_CANNON = new TitanCannonAbility();
    public static final RuptureStepAbility RUPTURE_STEP = new RuptureStepAbility();
    public static final FlowStepPassiveAbility FLOW_STEP = new FlowStepPassiveAbility();
    public static final MeteorHeelAbility METEOR_HEEL = new MeteorHeelAbility();
    public static final AnkleSplitterAbility ANKLE_SPLITTER = new AnkleSplitterAbility();
    public static final FlowStatePassiveAbility FLOW_STATE = new FlowStatePassiveAbility();
    public static final WhirlwindArcAbility WHIRLWIND_ARC = new WhirlwindArcAbility();
    public static final RisingShotAbility RISING_SHOT = new RisingShotAbility();
    public static final ToraHuntAbility TORA_HUNT = new ToraHuntAbility();
    public static final TripleStrikerAbility TRIPLE_STRIKER = new TripleStrikerAbility();
    public static final CrazedBlitzAbility CRAZED_BLITZ = new CrazedBlitzAbility();
    public static final SmokeyChainAbility SMOKEY_CHAIN = new SmokeyChainAbility();
    public static final SmokeSoldierAbility SMOKE_SOLDIER = new SmokeSoldierAbility();
    public static final SmokeCloneAbility SMOKE_CLONE = new SmokeCloneAbility();
    public static final SmokeyJailAbility SMOKY_JAIL = new SmokeyJailAbility();
    public static final DowsingChainAbility DOWSING_CHAIN = new DowsingChainAbility();
    public static final HolyChainAbility HOLY_CHAIN = new HolyChainAbility();
    public static final ChainJailAbility CHAIN_JAIL = new ChainJailAbility();
    public static final JudgmentChainAbility JUDGMENT_CHAIN = new JudgmentChainAbility();
    public static final StealChainAbility STEAL_CHAIN = new StealChainAbility();
    public static final GumAttachAbility GUM_ATTACH = new GumAttachAbility();
    public static final PullAbility ELASTIC_PULL = new PullAbility();
    public static final TextureSurpriseAbility TEXTURE_SURPRISE = new TextureSurpriseAbility();
    public static final ReflectAbility ELASTIC_REFLECT = new ReflectAbility();
    public static final TrapAbility ELASTIC_TRAP = new TrapAbility();

    public static final List<SkillTreeCombatAbility> SKILL_TREE_COMBAT_ABILITIES = List.of(
            FLASH_CLEAVE, GHOST_STEP, ACUTE, LION_FANG_DRAW, VOID_REND, PHANTOM_RING, UNSEEN_BLADE,
            HEAVEN_SPLITTER, SKYBREAKER_DIVE, AEGIS_RUSH, MIRROR_REPRISAL, STEEL_TATSUMAKI, BULWARK_CANNON, ICHIMONJI,
            HAMMER_JAB, CROSSFIRE_BARRAGE, LIVER_BREAK_COUNTER, REDIRECTION_RUSH, ATLAS_DROP, TITAN_CANNON, RUPTURE_STEP,
            METEOR_HEEL, ANKLE_SPLITTER, RISING_SHOT, WHIRLWIND_ARC, TORA_HUNT, TRIPLE_STRIKER, CRAZED_BLITZ,
            SMOKEY_CHAIN, SMOKE_SOLDIER, SMOKE_CLONE, SMOKY_JAIL,
            DOWSING_CHAIN, HOLY_CHAIN, CHAIN_JAIL, JUDGMENT_CHAIN, STEAL_CHAIN,
            GUM_ATTACH, ELASTIC_PULL, TEXTURE_SURPRISE, ELASTIC_REFLECT, ELASTIC_TRAP
    );
    public static final List<NenTechniqueAbility> NEN_TECHNIQUES = List.of(TEN, ZETSU, REN, EN, KO, KEN, RYU, EMPEROR_TIME);
    public static final List<SkillTreeCombatAbility> COMBAT_ABILITIES = java.util.stream.Stream.concat(
            SKILL_TREE_COMBAT_ABILITIES.stream(),
            NEN_TECHNIQUES.stream().map(ability -> (SkillTreeCombatAbility) ability)
    ).toList();
    public static final List<SkillTreePassiveAbility> SKILL_TREE_PASSIVES = List.of(
            STEP_DRIVE, FLASH_ACCEL,
            IRON_GUARD, PERFECT_READ,
            SLIP_GUARD, BREAKER_FISTS,
            FLOW_STEP,
            FLOW_STATE
    );
    public static final Map<SkillNode, List<SkillTreeAbility>> TREE_ABILITIES_BY_NODE = Map.of(
            SkillNode.SPEED, List.of(STEP_DRIVE, FLASH_CLEAVE, GHOST_STEP, FLASH_ACCEL, ACUTE, LION_FANG_DRAW, VOID_REND, PHANTOM_RING, UNSEEN_BLADE),
            SkillNode.DEFENSE, List.of(IRON_GUARD, HEAVEN_SPLITTER, SKYBREAKER_DIVE, PERFECT_READ, AEGIS_RUSH, MIRROR_REPRISAL, STEEL_TATSUMAKI, BULWARK_CANNON, ICHIMONJI),
            SkillNode.BOXING, List.of(SLIP_GUARD, HAMMER_JAB, CROSSFIRE_BARRAGE, BREAKER_FISTS, LIVER_BREAK_COUNTER, REDIRECTION_RUSH, ATLAS_DROP, TITAN_CANNON, RUPTURE_STEP),
            SkillNode.MARTIAL_ARTS, List.of(FLOW_STEP, ANKLE_SPLITTER, METEOR_HEEL, FLOW_STATE, RISING_SHOT, WHIRLWIND_ARC, TORA_HUNT, TRIPLE_STRIKER, CRAZED_BLITZ)
    );
    public static final List<CategoryPassiveAbility> CATEGORY_PASSIVES = List.of(WEAPON_MASTERY, PHYSICAL_POWER);
    public static final List<HunterAbility> ALL = List.of(
            DOUBLE_JUMP, DASH, GUARD,
            TEN, ZETSU, REN, EN, KO, KEN, RYU, EMPEROR_TIME,
            WEAPON_MASTERY, PHYSICAL_POWER,
            STEP_DRIVE, FLASH_CLEAVE, GHOST_STEP, FLASH_ACCEL, ACUTE, LION_FANG_DRAW, VOID_REND, PHANTOM_RING, UNSEEN_BLADE,
            IRON_GUARD, HEAVEN_SPLITTER, SKYBREAKER_DIVE, PERFECT_READ, AEGIS_RUSH, MIRROR_REPRISAL, STEEL_TATSUMAKI, BULWARK_CANNON, ICHIMONJI,
            SLIP_GUARD, HAMMER_JAB, CROSSFIRE_BARRAGE, BREAKER_FISTS, LIVER_BREAK_COUNTER, REDIRECTION_RUSH, ATLAS_DROP, TITAN_CANNON, RUPTURE_STEP,
            FLOW_STEP, METEOR_HEEL, ANKLE_SPLITTER, FLOW_STATE, RISING_SHOT, WHIRLWIND_ARC, TORA_HUNT, TRIPLE_STRIKER, CRAZED_BLITZ,
            SMOKEY_CHAIN, SMOKE_SOLDIER, SMOKE_CLONE, SMOKY_JAIL,
            DOWSING_CHAIN, HOLY_CHAIN, CHAIN_JAIL, JUDGMENT_CHAIN, STEAL_CHAIN,
            GUM_ATTACH, ELASTIC_PULL, TEXTURE_SURPRISE, ELASTIC_REFLECT, ELASTIC_TRAP
    );
    public static final List<BaseTechniqueAbility> BASE_TECHNIQUES = List.of(DOUBLE_JUMP, DASH, GUARD);
    private static final Map<String, HunterAbility> ABILITY_BY_ID = new LinkedHashMap<>();
    private static final List<HunterAbility> ADDON_ABILITIES = new ArrayList<>();
    private static final List<SkillTreeCombatAbility> ADDON_COMBAT_ABILITIES = new ArrayList<>();

    static {
        ALL.forEach(HunterAbilities::indexBuiltinAbility);
    }

    private HunterAbilities() {
    }

    public static HunterAbility byId(String id) {
        return ABILITY_BY_ID.get(id);
    }

    public static Optional<HunterAbility> find(String id) {
        return Optional.ofNullable(byId(id));
    }

    public static boolean isNenAbility(HunterAbility ability) {
        return ability != null && ability.isNenSource();
    }

    public static boolean isNenAbility(String abilityId) {
        return isNenAbility(byId(abilityId));
    }

    public static List<HunterAbility> allAbilities() {
        List<HunterAbility> abilities = new ArrayList<>(ALL);
        abilities.addAll(ADDON_ABILITIES);
        return Collections.unmodifiableList(abilities);
    }

    public static List<SkillTreeCombatAbility> combatAbilities() {
        List<SkillTreeCombatAbility> abilities = new ArrayList<>(COMBAT_ABILITIES);
        abilities.addAll(ADDON_COMBAT_ABILITIES);
        return Collections.unmodifiableList(abilities);
    }

    public static void registerAddonAbility(HunterAbility ability) {
        registerAddonAbility(ability, false);
    }

    public static void registerAddonCombatAbility(SkillTreeCombatAbility ability) {
        registerAddonAbility(ability, true);
    }

    private static void registerAddonAbility(HunterAbility ability, boolean combatAbility) {
        Objects.requireNonNull(ability, "ability");
        if (ability.id() == null || ability.id().isBlank()) {
            throw new IllegalArgumentException("Ability id cannot be blank.");
        }
        if (ABILITY_BY_ID.containsKey(ability.id())) {
            throw new IllegalArgumentException("Duplicate NenUnbound ability id: " + ability.id());
        }
        ABILITY_BY_ID.put(ability.id(), ability);
        ADDON_ABILITIES.add(ability);
        if (combatAbility) {
            ADDON_COMBAT_ABILITIES.add((SkillTreeCombatAbility) ability);
        }
    }

    private static void indexBuiltinAbility(HunterAbility ability) {
        if (ABILITY_BY_ID.putIfAbsent(ability.id(), ability) != null) {
            throw new IllegalStateException("Duplicate built-in NenUnbound ability id: " + ability.id());
        }
    }

    public static boolean canEquipToCombatBar(String id) {
        HunterAbility ability = byId(id);
        return ability != null && !ability.baseTechnique() && ability != RYU;
    }

    public static boolean isCombatAbilityUnlocked(HunterPlayerData data, String id) {
        if (!(byId(id) instanceof SkillTreeCombatAbility ability)) {
            return false;
        }
        return ability.isUnlocked(data);
    }

    public static List<SkillTreeCombatAbility> unlockedCombatAbilities(HunterPlayerData data) {
        return combatAbilities().stream()
                .filter(ability -> canEquipToCombatBar(ability.id()))
                .filter(ability -> ability.isUnlocked(data))
                .toList();
    }
}
