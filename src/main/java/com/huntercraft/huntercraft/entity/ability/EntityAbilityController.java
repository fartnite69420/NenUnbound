package com.huntercraft.huntercraft.entity.ability;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EntityAbilityController {
    private final List<EntityAbility> abilities = new ArrayList<>();
    private final Map<String, Integer> cooldowns = new HashMap<>();
    @Nullable
    private EntityAbility windingAbility;
    private int windupTicks;

    public EntityAbilityController add(EntityAbility ability) {
        this.abilities.add(ability);
        return this;
    }

    public void tick(Mob mob, @Nullable LivingEntity target) {
        cooldowns.replaceAll((id, ticks) -> Math.max(0, ticks - 1));
        if (this.windingAbility == null || this.windupTicks <= 0) {
            return;
        }

        if (target == null || !target.isAlive()) {
            this.cancelWindup();
            return;
        }

        this.windupTicks--;
        mob.getNavigation().stop();
        mob.lookAt(target, 30.0F, 30.0F);
        if (this.windupTicks <= 0) {
            EntityAbility ability = this.windingAbility;
            this.windingAbility = null;
            if (ability.canUse(mob, target)) {
                ability.use(mob, target);
                this.setCooldown(ability.id(), ability.cooldownTicks(mob));
            }
        }
    }

    public boolean tryUseAny(Mob mob, LivingEntity target) {
        if (this.isWindingUp()) {
            return false;
        }

        for (EntityAbility ability : this.abilities) {
            if (this.getCooldown(ability.id()) > 0 || !ability.canUse(mob, target)) {
                continue;
            }
            this.startWindup(mob, target, ability);
            return true;
        }
        return false;
    }

    public boolean isWindingUp() {
        return this.windingAbility != null && this.windupTicks > 0;
    }

    public int getWindupTicks() {
        return this.windupTicks;
    }

    public int getCooldown(String abilityId) {
        return this.cooldowns.getOrDefault(abilityId, 0);
    }

    public void setCooldown(String abilityId, int ticks) {
        this.cooldowns.put(abilityId, Math.max(0, ticks));
    }

    public void cancelWindup() {
        this.windingAbility = null;
        this.windupTicks = 0;
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        ListTag cooldownList = new ListTag();
        for (Map.Entry<String, Integer> entry : this.cooldowns.entrySet()) {
            if (entry.getValue() <= 0) {
                continue;
            }
            CompoundTag cooldownTag = new CompoundTag();
            cooldownTag.putString("Id", entry.getKey());
            cooldownTag.putInt("Ticks", entry.getValue());
            cooldownList.add(cooldownTag);
        }
        tag.put("Cooldowns", cooldownList);
        if (this.windingAbility != null) {
            tag.putString("WindupAbility", this.windingAbility.id());
            tag.putInt("WindupTicks", this.windupTicks);
        }
        return tag;
    }

    public void load(CompoundTag tag) {
        this.cooldowns.clear();
        ListTag cooldownList = tag.getList("Cooldowns", StringTag.TAG_COMPOUND);
        for (int i = 0; i < cooldownList.size(); i++) {
            CompoundTag cooldownTag = cooldownList.getCompound(i);
            String id = cooldownTag.getString("Id");
            if (!id.isBlank()) {
                this.cooldowns.put(id, Math.max(0, cooldownTag.getInt("Ticks")));
            }
        }

        this.cancelWindup();
        String windupAbilityId = tag.getString("WindupAbility");
        if (!windupAbilityId.isBlank()) {
            for (EntityAbility ability : this.abilities) {
                if (ability.id().equals(windupAbilityId)) {
                    this.windingAbility = ability;
                    this.windupTicks = Mth.clamp(tag.getInt("WindupTicks"), 0, 200);
                    break;
                }
            }
        }
    }

    private void startWindup(Mob mob, LivingEntity target, EntityAbility ability) {
        this.windingAbility = ability;
        this.windupTicks = Math.max(0, ability.windupTicks(mob));
        ability.onWindupStart(mob, target);
        if (this.windupTicks <= 0) {
            this.windingAbility = null;
            ability.use(mob, target);
            this.setCooldown(ability.id(), ability.cooldownTicks(mob));
        }
    }
}
