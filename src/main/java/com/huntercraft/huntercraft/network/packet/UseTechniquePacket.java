package com.huntercraft.huntercraft.network.packet;

import com.huntercraft.huntercraft.abilities.HunterAbilities;
import com.huntercraft.huntercraft.abilities.base.BaseTechniqueAbility;
import com.huntercraft.huntercraft.abilities.nenability.NenTechniqueAbility;
import com.huntercraft.huntercraft.data.HunterPlayerData;
import com.huntercraft.huntercraft.effect.HunterMobEffects;
import com.huntercraft.huntercraft.sound.HunterSoundEvents;
import com.huntercraft.huntercraft.util.HunterDataUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class UseTechniquePacket {
    public enum Technique {
        DASH,
        DOUBLE_JUMP,
        GUARD_TOGGLE,
        TEN,
        ZETSU,
        REN,
        EN,
        KO,
        KEN,
        RYU
    }

    private final Technique technique;
    private final Vec3 direction;

    public UseTechniquePacket(Technique technique, Vec3 direction) {
        this.technique = technique;
        this.direction = direction;
    }

    public static void encode(UseTechniquePacket packet, FriendlyByteBuf buffer) {
        buffer.writeEnum(packet.technique);
        buffer.writeDouble(packet.direction.x);
        buffer.writeDouble(packet.direction.y);
        buffer.writeDouble(packet.direction.z);
    }

    public static UseTechniquePacket decode(FriendlyByteBuf buffer) {
        return new UseTechniquePacket(
                buffer.readEnum(Technique.class),
                new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble())
        );
    }

    public static void handle(UseTechniquePacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        ServerPlayer player = context.getSender();
        if (player == null) {
            context.setPacketHandled(true);
            return;
        }

        context.enqueueWork(() -> {
            if (player.isSpectator()) {
                return;
            }
            boolean stunned = player.hasEffect(HunterMobEffects.STUNNED.get());
            if (stunned && !isAllowedWhileStunned(packet.technique)) {
                return;
            }
            boolean parryStunned = player.hasEffect(HunterMobEffects.PARRY_STUNNED.get());
            if (parryStunned && !isAllowedWhileParryStunned(packet.technique)) {
                return;
            }
            HunterPlayerData data = HunterDataUtil.get(player);
            if (player.hasEffect(HunterMobEffects.ZETSU.get()) && isNenTechnique(packet.technique) && !(packet.technique == Technique.ZETSU && data.isZetsuActive())) {
                return;
            }
            if (isBaseTechnique(packet.technique) && !data.isCombatBarVisible()) {
                return;
            }
            if (!canActivateTechnique(packet.technique, player, data)) {
                return;
            }
            int staminaCost = getActivationStaminaCost(packet.technique, data);
            if (staminaCost > 0 && !data.consumeStamina(staminaCost)) {
                return;
            }
            switch (packet.technique) {
                case DASH -> HunterAbilities.DASH.use(player, data, packet.direction);
                case DOUBLE_JUMP -> HunterAbilities.DOUBLE_JUMP.use(player, data, packet.direction);
                case GUARD_TOGGLE -> {
                    if (data.isGuarding()) {
                        HunterAbilities.GUARD.stop(player, data);
                    } else {
                        HunterAbilities.GUARD.use(player, data, packet.direction);
                    }
                }
                case TEN -> HunterAbilities.TEN.use(player, data, packet.direction);
                case ZETSU -> HunterAbilities.ZETSU.use(player, data, packet.direction);
                case REN -> HunterAbilities.REN.use(player, data, packet.direction);
                case EN -> HunterAbilities.EN.use(player, data, packet.direction);
                case KO -> HunterAbilities.KO.use(player, data, packet.direction);
                case KEN -> HunterAbilities.KEN.use(player, data, packet.direction);
                case RYU -> data.setRyuActive(false);
            }
            playTechniqueSound(player, packet.technique);
            HunterDataUtil.sync(player);
        });

        context.setPacketHandled(true);
    }

    private static boolean isAllowedWhileStunned(Technique technique) {
        return switch (technique) {
            case GUARD_TOGGLE, TEN, ZETSU, REN, EN, KO, KEN -> true;
            default -> false;
        };
    }

    private static boolean isAllowedWhileParryStunned(Technique technique) {
        return technique == Technique.GUARD_TOGGLE;
    }

    private static boolean isBaseTechnique(Technique technique) {
        return switch (technique) {
            case DASH, DOUBLE_JUMP, GUARD_TOGGLE -> true;
            default -> false;
        };
    }

    private static boolean isNenTechnique(Technique technique) {
        return switch (technique) {
            case TEN, ZETSU, REN, EN, KO, KEN -> true;
            default -> false;
        };
    }

    private static boolean canActivateTechnique(Technique technique, ServerPlayer player, HunterPlayerData data) {
        return switch (technique) {
            case DASH -> HunterAbilities.DASH.getCurrentCooldown(data) <= 0;
            case DOUBLE_JUMP -> !player.onGround()
                    && data.getAirJumpsUsed() < 1
                    && HunterAbilities.DOUBLE_JUMP.getCurrentCooldown(data) <= 0;
            case GUARD_TOGGLE -> data.isGuarding() || HunterAbilities.GUARD.getCurrentCooldown(data) <= 0;
            case TEN -> HunterAbilities.TEN.canUse(player, data);
            case ZETSU -> HunterAbilities.ZETSU.canUse(player, data);
            case REN -> HunterAbilities.REN.canUse(player, data);
            case EN -> HunterAbilities.EN.canUse(player, data);
            case KO -> HunterAbilities.KO.canUse(player, data);
            case KEN -> HunterAbilities.KEN.canUse(player, data);
            case RYU -> true;
        };
    }

    private static int getActivationStaminaCost(Technique technique, HunterPlayerData data) {
        return switch (technique) {
            case DASH -> HunterAbilities.DASH.getStaminaCost(data);
            case DOUBLE_JUMP -> HunterAbilities.DOUBLE_JUMP.getStaminaCost(data);
            case GUARD_TOGGLE -> data.isGuarding() ? 0 : HunterAbilities.GUARD.getStaminaCost(data);
            case TEN -> getNenToggleCost(HunterAbilities.TEN, data);
            case ZETSU -> data.isZetsuActive() ? 0 : 0;
            case REN -> getNenToggleCost(HunterAbilities.REN, data);
            case EN -> getNenToggleCost(HunterAbilities.EN, data);
            case KO -> getNenToggleCost(HunterAbilities.KO, data);
            case KEN -> getNenToggleCost(HunterAbilities.KEN, data);
            case RYU -> 0;
        };
    }

    private static int getNenToggleCost(NenTechniqueAbility ability, HunterPlayerData data) {
        return ability.isActive(data) ? 0 : data.getReducedNenStaminaCost(8);
    }

    private static void playTechniqueSound(ServerPlayer player, Technique technique) {
        switch (technique) {
            case DASH, DOUBLE_JUMP -> player.level().playSound(null, player.getX(), player.getY(), player.getZ(), HunterSoundEvents.DASH.get(), SoundSource.PLAYERS, 0.75F, technique == Technique.DOUBLE_JUMP ? 1.25F : 1.0F);
            default -> {
            }
        }
    }
}
