package com.huntercraft.huntercraft.abilities;

import com.huntercraft.huntercraft.data.HunterPlayerData;
import com.huntercraft.huntercraft.effect.HunterMobEffects;
import com.huntercraft.huntercraft.progression.SkillNode;
import com.huntercraft.huntercraft.sound.HunterSoundEvents;
import com.google.common.collect.Multimap;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.phys.Vec3;

public abstract class SkillTreeCombatAbility extends SkillTreeAbility {
    public SkillTreeCombatAbility(String id, String displayName, String description, String iconPath, SkillNode skillNode, int requiredPoints) {
        this(id, displayName, description, iconPath, skillNode, requiredPoints, defaultCombatSource(skillNode));
    }

    public SkillTreeCombatAbility(String id, String displayName, String description, String iconPath, SkillNode skillNode, int requiredPoints, AbilitySourceType... sourceTypes) {
        super(id, displayName, description, iconPath, skillNode, requiredPoints, sourceTypes);
    }

    public abstract int getMaxCooldownTicks();

    public int getCurrentCooldown(HunterPlayerData data) {
        return data.getAbilityCooldown(this.id());
    }

    public boolean isContinuous() {
        return false;
    }

    /**
     * Returns true if this ability should remain active when other abilities are used.
     * Passive-while-active abilities (like Smokey Jail) are not interrupted by using
     * other abilities such as Smoke Clone or Smoke Soldier.
     */
    public boolean isPassiveWhileActive() {
        return false;
    }

    public boolean isActive(HunterPlayerData data) {
        return data.isActiveAbility(this.id());
    }

    public boolean isCharging(HunterPlayerData data) {
        return data.isChargingAbility(this.id());
    }

    public int getActiveTicks(HunterPlayerData data) {
        return 0;
    }

    public int getChargeTicks(HunterPlayerData data) {
        return 0;
    }

    protected void startCooldown(HunterPlayerData data, int cooldownTicks) {
        if (this.usesCombatVowModifiers()) {
            cooldownTicks = Math.max(1, Math.round(cooldownTicks * data.getCombatVowCooldownMultiplier()));
        }
        data.setAbilityCooldown(this.id(), cooldownTicks);
    }

    protected boolean usesCombatVowModifiers() {
        return this.hasSourceType(AbilitySourceType.SHARP) || this.hasSourceType(AbilitySourceType.BLUNT);
    }

    public int getStaminaCost(HunterPlayerData data) {
        if (this instanceof com.huntercraft.huntercraft.abilities.nenability.NenTechniqueAbility) {
            return 0;
        }
        int cooldown = Math.max(0, this.getMaxCooldownTicks());
        int baseCost = 40 + Math.min(460, Math.round(cooldown / 4.0F));
        if (this.isContinuous()) {
            baseCost += 90;
        }
        if (this.hasSourceType(AbilitySourceType.NEN)) {
            baseCost = Math.max(45, Math.round(baseCost * 0.60F));
        }
        return data.getReducedStaminaCost(baseCost);
    }

    public boolean canUse(ServerPlayer player, HunterPlayerData data) {
        return (!player.hasEffect(HunterMobEffects.ZETSU.get()) || !this.isSuppressedByZetsu())
                && this.isUnlocked(data)
                && this.getCurrentCooldown(data) <= 0
                && (!requiresWeaponInHand() || hasWeaponInHand(player))
                && (!requiresEmptyMainHand() || hasEmptyMainHand(player));
    }

    public Component getUseFailureMessage(ServerPlayer player, HunterPlayerData data) {
        if (player.hasEffect(HunterMobEffects.ZETSU.get()) && this.isSuppressedByZetsu()) {
            return Component.literal("Zetsu is suppressing your Nen.");
        }
        if (!this.isUnlocked(data)) {
            return Component.literal(this.displayName() + " is not unlocked yet.");
        }
        int cooldownTicks = this.getCurrentCooldown(data);
        if (cooldownTicks > 0) {
            return null;
        }
        if (this.requiresWeaponInHand() && !this.hasWeaponInHand(player)) {
            return Component.literal(this.displayName() + " requires a weapon in your hand.");
        }
        if (this.requiresEmptyMainHand() && !this.hasEmptyMainHand(player)) {
            return Component.literal(this.displayName() + " requires an empty main hand.");
        }
        return null;
    }

    public boolean isSuppressedByZetsu() {
        return this.isNenSource();
    }

    public void tick(ServerPlayer player, HunterPlayerData data) {
    }

    public abstract void use(ServerPlayer player, HunterPlayerData data, Vec3 direction);

    public void stop(ServerPlayer player, HunterPlayerData data) {
    }

    protected boolean requiresWeaponInHand() {
        return this.skillNode().category() == SkillNode.Category.WEAPON;
    }

    protected boolean requiresEmptyMainHand() {
        return this.skillNode().category() == SkillNode.Category.PHYSICAL;
    }

    protected boolean hasWeaponInHand(ServerPlayer player) {
        return isWeapon(player.getMainHandItem()) || isWeapon(player.getOffhandItem());
    }

    protected boolean hasEmptyMainHand(ServerPlayer player) {
        return player.getMainHandItem().isEmpty();
    }

    protected float getWeaponScaledDamage(ServerPlayer player, float baseDamage) {
        return baseDamage + getHeldWeaponAttackDamage(player);
    }

    protected float getHeldWeaponAttackDamage(ServerPlayer player) {
        ItemStack stack = isWeapon(player.getMainHandItem()) ? player.getMainHandItem() : player.getOffhandItem();
        if (stack.isEmpty()) {
            return 0.0F;
        }

        EquipmentSlot slot = stack == player.getMainHandItem() ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
        Multimap<Attribute, AttributeModifier> modifiers = stack.getAttributeModifiers(slot);
        double amount = 0.0D;
        for (AttributeModifier modifier : modifiers.get(Attributes.ATTACK_DAMAGE)) {
            amount += switch (modifier.getOperation()) {
                case ADDITION -> modifier.getAmount();
                case MULTIPLY_BASE, MULTIPLY_TOTAL -> 0.0D;
            };
        }
        return (float) Math.max(0.0D, amount);
    }

    protected Component createNenCostMessage(int requiredNen) {
        return Component.literal("You need more stamina to use " + this.displayName() + ".");
    }

    protected void playSlashReleaseSound(ServerPlayer player, float pitch) {
        playAbilitySound(player, HunterSoundEvents.SLASH.get(), 0.95F, pitch);
    }

    protected void playPunchReleaseSound(ServerPlayer player, float pitch) {
        playAbilitySound(player, HunterSoundEvents.PUNCH.get(), 0.95F, pitch);
    }

    protected void playDashReleaseSound(ServerPlayer player, float pitch) {
        playAbilitySound(player, HunterSoundEvents.DASH.get(), 0.85F, pitch);
    }

    protected void playTeleportReleaseSound(ServerPlayer player, float pitch) {
        playAbilitySound(player, HunterSoundEvents.TELEPORT.get(), 0.85F, pitch);
    }

    protected void playGroundSmashReleaseSound(ServerPlayer player, float pitch) {
        playAbilitySound(player, HunterSoundEvents.GROUND_SMASH.get(), 1.0F, pitch);
    }

    protected void playAbilitySound(ServerPlayer player, SoundEvent sound, float volume, float pitch) {
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), sound, SoundSource.PLAYERS, volume, pitch);
    }

    protected static String formatCooldownSeconds(int cooldownTicks) {
        float seconds = cooldownTicks / 20.0F;
        if (Math.abs(seconds - Math.round(seconds)) < 0.05F) {
            return Integer.toString(Math.round(seconds));
        }
        return String.format(java.util.Locale.ROOT, "%.1f", seconds);
    }

    private static boolean isWeapon(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        return stack.getItem() instanceof SwordItem
                || stack.getItem() instanceof AxeItem
                || stack.getItem() instanceof TridentItem;
    }

    private static AbilitySourceType defaultCombatSource(SkillNode skillNode) {
        return skillNode.category() == SkillNode.Category.WEAPON ? AbilitySourceType.SHARP : AbilitySourceType.BLUNT;
    }
}
