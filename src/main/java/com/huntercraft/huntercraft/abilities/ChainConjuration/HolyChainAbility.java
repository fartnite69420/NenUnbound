package com.huntercraft.huntercraft.abilities.ChainConjuration;

import com.huntercraft.huntercraft.abilities.SkillTreeCombatAbility;
import com.huntercraft.huntercraft.data.HunterPlayerData;
import com.huntercraft.huntercraft.progression.NenTechniqueSkillNode;
import com.huntercraft.huntercraft.progression.SkillNode;
import com.huntercraft.huntercraft.util.HunterDataUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

public class HolyChainAbility extends SkillTreeCombatAbility {
    private static final int COOLDOWN_TICKS = 20 * 20;
    private static final int ACTIVE_TICKS = 20 * 4;
    private static final int NEN_COST = 220;

    public HolyChainAbility() {
        super("holy_chain", "Holy Chain", "Wrap yourself in a healing chain for a few seconds.", "textures/gui/abilities/holy_chain.png", SkillNode.MARTIAL_ARTS, 0, com.huntercraft.huntercraft.abilities.AbilitySourceType.NEN);
    }

    @Override
    public boolean isUnlocked(HunterPlayerData data) {
        return data.hasUnlockedNenTechniqueNode(NenTechniqueSkillNode.CHAIN_NEN_HEAL);
    }

    @Override
    public int getMaxCooldownTicks() {
        return COOLDOWN_TICKS;
    }

    @Override
    public boolean isContinuous() {
        return true;
    }

    @Override
    protected boolean requiresWeaponInHand() {
        return false;
    }

    @Override
    protected boolean requiresEmptyMainHand() {
        return false;
    }

    @Override
    public boolean canUse(ServerPlayer player, HunterPlayerData data) {
        return super.canUse(player, data)
                && data.hasChainTechnique()
                && data.hasStaminaForNenCost(NEN_COST);
    }

    @Override
    public Component getUseFailureMessage(ServerPlayer player, HunterPlayerData data) {
        Component baseMessage = super.getUseFailureMessage(player, data);
        if (baseMessage != null) {
            return baseMessage;
        }
        if (!data.hasChainTechnique()) {
            return Component.literal("You need the Chain Nen technique to use Holy Chain.");
        }
        if (!data.hasStaminaForNenCost(NEN_COST)) {
            return this.createNenCostMessage(NEN_COST);
        }
        return null;
    }

    @Override
    public int getActiveTicks(HunterPlayerData data) {
        return data.isActiveAbility(this.id()) ? data.getActiveAbilityTicksRemaining() : 0;
    }

    @Override
    public void use(ServerPlayer player, HunterPlayerData data, Vec3 direction) {
        if (!data.consumeNen(NEN_COST)) {
            return;
        }
        data.startActiveAbility(this.id(), ACTIVE_TICKS, Vec3.ZERO);
        this.startCooldown(data, COOLDOWN_TICKS);
        HunterDataUtil.sync(player);
    }

    @Override
    public void tick(ServerPlayer player, HunterPlayerData data) {
        if (!data.isActiveAbility(this.id())) {
            return;
        }
        if ((data.getActiveAbilityTicksRemaining() % 20) == 0) {
            player.heal(data.isEmperorTimeActive() ? 2.0F : 1.0F);
        }
        data.tickActiveAbility();
    }
}
