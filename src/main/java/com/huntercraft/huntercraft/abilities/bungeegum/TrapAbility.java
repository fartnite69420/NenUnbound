package com.huntercraft.huntercraft.abilities.bungeegum;

import com.huntercraft.huntercraft.abilities.SkillTreeCombatAbility;
import com.huntercraft.huntercraft.data.HunterPlayerData;
import com.huntercraft.huntercraft.entity.ElasticAuraProjectileEntity;
import com.huntercraft.huntercraft.entity.HunterEntityTypes;
import com.huntercraft.huntercraft.progression.NenTechniqueSkillNode;
import com.huntercraft.huntercraft.progression.SkillNode;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

public class TrapAbility extends SkillTreeCombatAbility {
    public static final int NEN_COST = 350;
    private static final int COOLDOWN_TICKS = 12 * 20;

    public TrapAbility() {
        super("elastic_trap", "Trap", "Set a sticky gum snare on the ground. Anyone who touches it gets locked in place in your Nen-colored elastic aura.", "textures/gui/abilities/bungee_gum_trap.png", SkillNode.MARTIAL_ARTS, 0, com.huntercraft.huntercraft.abilities.AbilitySourceType.NEN, com.huntercraft.huntercraft.abilities.AbilitySourceType.BLUNT);
    }

    @Override
    public boolean isUnlocked(HunterPlayerData data) {
        return data.hasUnlockedNenTechniqueNode(NenTechniqueSkillNode.ELASTIC_AURA_TRAP);
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
                && ElasticAuraManager.TECHNIQUE_ID.equals(data.getNenTechniqueId())
                && data.hasStaminaForNenCost(NEN_COST);
    }

    @Override
    public void use(ServerPlayer player, HunterPlayerData data, Vec3 direction) {
        if (!(player.level() instanceof ServerLevel level) || !data.consumeNen(NEN_COST)) {
            return;
        }
        ElasticAuraProjectileEntity projectile = HunterEntityTypes.ELASTIC_AURA_PROJECTILE.get().create(level);
        if (projectile == null) {
            data.addStamina(data.getReducedNenStaminaCost(NEN_COST));
            return;
        }
        boolean hidden = ElasticAuraManager.consumeTextureSurprise(player);
        Vec3 launch = direction.lengthSqr() > 1.0E-4D ? direction.normalize() : player.getLookAngle().normalize();
        projectile.configure(player, ElasticAuraProjectileEntity.MODE_TRAP, hidden);
        projectile.moveTo(player.getX(), player.getEyeY() - 0.08D, player.getZ(), player.getYRot(), player.getXRot());
        projectile.setDeltaMovement(launch.scale(4.05D));
        level.addFreshEntity(projectile);
        this.startCooldown(data, COOLDOWN_TICKS);
    }
}
