package com.huntercraft.huntercraft.progression;

import java.util.Arrays;

public enum NenTechniqueSkillNode {
    DEEP_PURPLE_CORE("deep_purple_core", "deep_purple", 0, 1, "Deep Purple Physiology", "Passive slot: smoking builds Lung Capacity, and your breath lasts longer underwater. Both passives scale upward with Nen level."),
    DEEP_PURPLE_VEIL("deep_purple_veil", "deep_purple", 1, 3, "Smokey Chain", "Fire a smoke cloud forward. If it catches a target, the smoke binds them to your Smoking Pipe."),
    DEEP_PURPLE_PUPPET("deep_purple_puppet", "deep_purple", 2, 4, "Smoke Soldier", "Summon a smoke soldier from your pipe. Crouch-use swaps between Hunt and Return orders."),
    DEEP_PURPLE_COLUMN("deep_purple_column", "deep_purple", 3, 5, "Smoke Clone", "Shape a low-health smoke clone that mirrors a fighter's Nen flow, combat tree, and movement techniques."),
    DEEP_PURPLE_DOMAIN("deep_purple_domain", "deep_purple", 4, 6, "Smokey Jail", "Create a 20-block smoke barrier that traps movement across its shell, blocks incoming projectiles, and can be canceled early."),
    ELASTIC_AURA_CORE("elastic_aura_core", "elastic_aura", 0, 1, "Bungee Gum", "Your aura is transmuted into sticky, stretchy gum. All projectiles, traps, anchors, and impacts tint themselves to your current Nen color."),
    ELASTIC_AURA_ATTACH("elastic_aura_attach", "elastic_aura", 1, 3, "Gum Attach", "Fire bungee gum that sticks to surfaces or targets. M1 on a tagged target yanks them into the anchor point or binds two tagged targets together."),
    ELASTIC_AURA_PULL("elastic_aura_pull", "elastic_aura", 2, 3, "Pull", "Shoot a fast gum line that drags a target into your grasp, briefly locks them in place, then punches them away."),
    ELASTIC_AURA_TEXTURE_SURPRISE("elastic_aura_texture_surprise", "elastic_aura", 3, 4, "Texture Surprise", "Hide the visual tell of your next Bungee Gum technique, including its projectile, tether, trap shell, or reflective aura."),
    ELASTIC_AURA_REFLECT("elastic_aura_reflect", "elastic_aura", 4, 5, "Reflect", "Wrap yourself in bungee gum for a short window and rebound incoming projectiles back at their source."),
    ELASTIC_AURA_TRAP("elastic_aura_trap", "elastic_aura", 5, 6, "Trap", "Lay down sticky gum traps that stun and pin enemies in place when they touch them."),
    CHAIN_NEN_CORE("chain_nen_core", "chain_nen", 0, 1, "Dowsing Chain", "Your chains take shape as a scalable link-by-link projectile system. Dowsing Chain whips around you without needing Emperor Time."),
    CHAIN_NEN_HEAL("chain_nen_heal", "chain_nen", 1, 3, "Holy Chain", "A healing chain that restores your body over several seconds. Requires Emperor Time."),
    CHAIN_NEN_JAIL("chain_nen_jail", "chain_nen", 2, 4, "Chain Jail", "Launch a restraining chain that only fully works against the faction marked by your vow. Requires Emperor Time."),
    CHAIN_NEN_JUDGMENT("chain_nen_judgment", "chain_nen", 3, 5, "Judgment Chain", "Hit a vowed player and choose one of their Hatsu abilities to bind until death."),
    CHAIN_NEN_STEAL("chain_nen_steal", "chain_nen", 4, 6, "Steal Chain", "Bind the target and suppress one random Nen combat ability for 10 seconds. Requires Emperor Time.");

    private final String id;
    private final String techniqueId;
    private final int order;
    private final int requiredNenLevel;
    private final String displayName;
    private final String description;

    NenTechniqueSkillNode(String id, String techniqueId, int order, int requiredNenLevel, String displayName, String description) {
        this.id = id;
        this.techniqueId = techniqueId;
        this.order = order;
        this.requiredNenLevel = requiredNenLevel;
        this.displayName = displayName;
        this.description = description;
    }

    public String id() {
        return this.id;
    }

    public String techniqueId() {
        return this.techniqueId;
    }

    public int order() {
        return this.order;
    }

    public int requiredNenLevel() {
        return this.requiredNenLevel;
    }

    public String displayName() {
        return this.displayName;
    }

    public String description() {
        return this.description;
    }

    public static NenTechniqueSkillNode byId(String id) {
        return Arrays.stream(values())
                .filter(node -> node.id.equalsIgnoreCase(id))
                .findFirst()
                .orElse(null);
    }

    public static NenTechniqueSkillNode[] forTechnique(String techniqueId) {
        return Arrays.stream(values())
                .filter(node -> node.techniqueId.equalsIgnoreCase(techniqueId))
                .sorted((left, right) -> Integer.compare(left.order, right.order))
                .toArray(NenTechniqueSkillNode[]::new);
    }
}
