# NenUnbound Addon Guide

NenUnbound abilities are looked up by stable string ids. Addons should avoid editing
`HunterAbilities` directly and register their abilities during mod setup.

## Registering an ability

Use the public registration helpers:

```java
HunterAbilities.registerAddonAbility(new MyPassiveAbility());
HunterAbilities.registerAddonCombatAbility(new MyCombatAbility());
```

`registerAddonCombatAbility` makes the ability available to combat-bar lookups and
the unlocked-combat-ability list. It must extend `SkillTreeCombatAbility`.

## Ability checklist

- Use a lowercase namespaced id style, such as `myaddon_flash_step`.
- Put icons under `textures/gui/abilities/` and return that path from the ability.
- Keep cooldown, charge, active, and stop behavior inside the ability class.
- Use `HunterAbilities.find(id)` when the caller can handle a missing ability.
- Use `HunterAbilities.byId(id)` only when existing nullable behavior is expected.

## Editor checklist

- Display names should be short enough for the HUD and ability cards.
- Descriptions should be one or two direct sentences.
- Prefer existing `SkillTreeCombatAbility` hooks before adding new packet or data
  fields.
- Add new persistent data to `HunterPlayerData` only when it cannot be derived from
  existing cooldown, active, charge, or unlock state.
