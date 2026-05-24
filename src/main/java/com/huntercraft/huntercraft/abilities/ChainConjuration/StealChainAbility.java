package com.huntercraft.huntercraft.abilities.ChainConjuration;

import com.huntercraft.huntercraft.abilities.SkillTreeCombatAbility;
import com.huntercraft.huntercraft.data.HunterPlayerData;
import com.huntercraft.huntercraft.entity.SmokeyChainProjectileEntity;
import com.huntercraft.huntercraft.progression.NenTechniqueSkillNode;
import com.huntercraft.huntercraft.progression.SkillNode;
import com.huntercraft.huntercraft.util.HunterDataUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

public class StealChainAbility extends SkillTreeCombatAbility {
    private static final int COOLDOWN_TICKS = 25 * 20;
    private static final int NEN_COST = 500;

    public StealChainAbility() {
        super("steal_chain", "Steal Chain", "Disable one random ready Hatsu Nen technique on the struck target for 10 seconds.", "textures/gui/abilities/steal_chain.png", SkillNode.MARTIAL_ARTS, 0, com.huntercraft.huntercraft.abilities.AbilitySourceType.NEN, com.huntercraft.huntercraft.abilities.AbilitySourceType.SHARP);
    }

    @Override
    public boolean isUnlocked(HunterPlayerData data) {
        return data.hasUnlockedNenTechniqueNode(NenTechniqueSkillNode.CHAIN_NEN_STEAL);
    }

    @Override
    public int getMaxCooldownTicks() {
        return COOLDOWN_TICKS;
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
            return Component.literal("You need the Chain Nen technique to use Steal Chain.");
        }
        if (!data.hasStaminaForNenCost(NEN_COST)) {
            return this.createNenCostMessage(NEN_COST);
        }
        return null;
    }

    @Override
    public void use(ServerPlayer player, HunterPlayerData data, Vec3 direction) {
        if (!(player.level() instanceof ServerLevel serverLevel) || !data.consumeNen(NEN_COST)) {
            return;
        }
        Vec3 launch = player.getLookAngle().normalize();
        SmokeyChainProjectileEntity projectile = SmokeyChainProjectileEntity.create(serverLevel, player, launch, this.id());
        if (projectile == null) {
            data.addStamina(data.getReducedNenStaminaCost(NEN_COST));
            return;
        }
        serverLevel.addFreshEntity(projectile);
        this.startCooldown(data, COOLDOWN_TICKS);
        HunterDataUtil.sync(player);
    }
}
