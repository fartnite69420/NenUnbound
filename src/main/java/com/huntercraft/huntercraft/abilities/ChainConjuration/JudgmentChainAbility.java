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

public class JudgmentChainAbility extends SkillTreeCombatAbility {
    private static final int COOLDOWN_TICKS = 45 * 20;
    private static final int NEN_COST = 500;

    public JudgmentChainAbility() {
        super("judgment_chain", "Judgment Chain", "Pierce a vowed target and choose one of their Hatsu abilities to seal until death.", "textures/gui/abilities/judgment_chain.png", SkillNode.MARTIAL_ARTS, 0, com.huntercraft.huntercraft.abilities.AbilitySourceType.NEN, com.huntercraft.huntercraft.abilities.AbilitySourceType.SHARP);
    }

    @Override
    public boolean isUnlocked(HunterPlayerData data) {
        return data.hasUnlockedNenTechniqueNode(NenTechniqueSkillNode.CHAIN_NEN_JUDGMENT);
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
            return Component.literal("You need the Chain Nen technique to use Judgment Chain.");
        }
        if (!data.hasStaminaForNenCost(NEN_COST)) {
            return this.createNenCostMessage(NEN_COST);
        }
        return null;
    }

    @Override
    public int getMaxCooldownTicks() {
        return COOLDOWN_TICKS;
    }

    @Override
    public void use(ServerPlayer player, HunterPlayerData data, Vec3 direction) {
        if (!(player.level() instanceof ServerLevel serverLevel) || !data.consumeNen(NEN_COST)) {
            return;
        }
        Vec3 launch = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : player.getLookAngle().normalize();
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
