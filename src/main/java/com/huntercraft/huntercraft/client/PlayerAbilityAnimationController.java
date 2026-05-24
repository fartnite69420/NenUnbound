package com.huntercraft.huntercraft.client;

import com.huntercraft.huntercraft.HunterCraftMod;
import com.huntercraft.huntercraft.animation.AnimationType;
import com.huntercraft.huntercraft.data.HunterPlayerData;
import com.huntercraft.huntercraft.data.HunterPlayerDataProvider;
import com.huntercraft.huntercraft.item.HunterItems;
import com.huntercraft.huntercraft.progression.SkillNode;
import dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.api.layered.modifier.AbstractFadeModifier;
import dev.kosmx.playerAnim.core.data.AnimationFormat;
import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import dev.kosmx.playerAnim.core.util.Ease;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationAccess;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TridentItem;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class PlayerAbilityAnimationController {
    private static final ResourceLocation LAYER_ID = new ResourceLocation(HunterCraftMod.MODID, "ability_layer");
    private static final Map<UUID, String> LAST_APPLIED = new HashMap<>();

    private static final KeyframeAnimation DASH_ANIMATION = buildDashAnimation();
    private static final KeyframeAnimation MEDITATE_ANIMATION = buildMeditateAnimation();
    private static final KeyframeAnimation SMOKING_PIPE_ANIMATION = buildSmokingPipeAnimation();
    private static final KeyframeAnimation SMOKE_SOLDIER_SUMMON_ANIMATION = buildSmokeSoldierSummonAnimation();
    private static final KeyframeAnimation DOUBLE_JUMP_ANIMATION = buildDoubleJumpAnimation();
    private static final KeyframeAnimation FLASH_CLEAVE_ONE_ANIMATION = buildFlashCleaveAnimation("huntercraft_flash_cleave_one", -58.0F, 34.0F, 62.0F);
    private static final KeyframeAnimation FLASH_CLEAVE_TWO_ANIMATION = buildFlashCleaveAnimation("huntercraft_flash_cleave_two", 52.0F, -28.0F, -58.0F);
    private static final KeyframeAnimation FLASH_CLEAVE_THREE_ANIMATION = buildFlashCleaveAnimation("huntercraft_flash_cleave_three", -12.0F, 8.0F, 78.0F);
    private static final KeyframeAnimation HEAVEN_SPLITTER_ANIMATION = buildHeavenSplitterAnimation();
    private static final KeyframeAnimation SKYBREAKER_ASCENT_ANIMATION = buildSkybreakerAscentAnimation();
    private static final KeyframeAnimation SKYBREAKER_DROP_ANIMATION = buildSkybreakerDropAnimation();
    private static final KeyframeAnimation AEGIS_RUSH_ANIMATION = buildAegisRushAnimation();
    private static final KeyframeAnimation MIRROR_REPRISAL_GUARD_ANIMATION = buildMirrorReprisalGuardAnimation();
    private static final KeyframeAnimation MIRROR_REPRISAL_STRIKE_ANIMATION = buildMirrorReprisalStrikeAnimation();
    private static final KeyframeAnimation STEEL_TATSUMAKI_ANIMATION = buildSteelTatsumakiAnimation();
    private static final KeyframeAnimation BOXER_JAB_ANIMATION = buildBoxerJabAnimation();
    private static final KeyframeAnimation BOXER_BARRAGE_ANIMATION = buildBoxerBarrageAnimation();
    private static final KeyframeAnimation BOXER_BARRAGE_FAST_ANIMATION = buildBoxerBarrageFastAnimation();
    private static final KeyframeAnimation BOXER_HAMMER_STRIKE_ANIMATION = buildBoxerHammerStrikeAnimation();
    private static final KeyframeAnimation BOXER_COUNTER_GUARD_ANIMATION = buildBoxerCounterGuardAnimation();
    private static final KeyframeAnimation BOXER_COUNTER_STRIKE_ANIMATION = buildBoxerCounterStrikeAnimation();
    private static final KeyframeAnimation BOXER_REDIRECTION_ANIMATION = buildBoxerRedirectionAnimation();
    private static final KeyframeAnimation BOXER_GRAB_LIFT_ANIMATION = buildBoxerGrabLiftAnimation();
    private static final KeyframeAnimation BOXER_GRAB_SLAM_ANIMATION = buildBoxerGrabSlamAnimation();
    private static final KeyframeAnimation MARTIAL_METEOR_HEEL_ANIMATION = buildMartialMeteorHeelAnimation();
    private static final KeyframeAnimation MARTIAL_METEOR_HEEL_IMPACT_ANIMATION = buildMartialMeteorHeelImpactAnimation();
    private static final KeyframeAnimation MARTIAL_ANKLE_SPLITTER_ANIMATION = buildMartialAnkleSplitterAnimation();
    private static final KeyframeAnimation MARTIAL_FACE_JAB_ANIMATION = buildMartialFaceJabAnimation();
    private static final KeyframeAnimation MARTIAL_WHIRLWIND_ARC_ANIMATION = buildMartialWhirlwindArcAnimation();
    private static final KeyframeAnimation MARTIAL_AIR_BARRAGE_ANIMATION = buildMartialAirBarrageAnimation();
    private static final KeyframeAnimation MARTIAL_RISING_SHOT_CHARGE_ANIMATION = buildMartialRisingShotChargeAnimation();
    private static final KeyframeAnimation MARTIAL_RISING_SHOT_ANIMATION = buildMartialRisingShotAnimation();
    private static final KeyframeAnimation MARTIAL_TORA_HUNT_CHARGE_ANIMATION = buildMartialToraHuntChargeAnimation();
    private static final KeyframeAnimation MARTIAL_TORA_HUNT_ANIMATION = buildMartialToraHuntAnimation();
    private static final KeyframeAnimation LION_FANG_DRAW_CHARGE_ANIMATION = buildLionFangDrawChargeAnimation();
    private static final KeyframeAnimation VOID_REND_CHARGE_ANIMATION = buildVoidRendChargeAnimation();
    private static final KeyframeAnimation VOID_REND_ANIMATION = buildVoidRendAnimation();
    private static final KeyframeAnimation GUARD_ANIMATION = buildGuardAnimation();
    private static final KeyframeAnimation WEAPON_GUARD_ANIMATION = buildWeaponGuardAnimation();
    private static final KeyframeAnimation PARRY_ANIMATION = buildParryAnimation();
    private static final KeyframeAnimation WEAPON_PARRY_ANIMATION = buildWeaponParryAnimation();
    private static final KeyframeAnimation ELASTIC_REFLECT_ANIMATION = buildElasticReflectAnimation();
    private static final KeyframeAnimation DOWSING_CHAIN_SWING_ANIMATION = buildDowsingChainSwingAnimation();

    private PlayerAbilityAnimationController() {
    }

    public static void init() {
        PlayerAnimationAccess.REGISTER_ANIMATION_EVENT.register((player, animationStack) -> {
            ModifierLayer<KeyframeAnimationPlayer> layer = new ModifierLayer<>();
            animationStack.addAnimLayer(1000, layer);
            PlayerAnimationAccess.getPlayerAssociatedData(player).set(LAYER_ID, layer);
        });
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            LAST_APPLIED.clear();
            return;
        }
        for (AbstractClientPlayer player : minecraft.level.players()) {
            player.getCapability(HunterPlayerDataProvider.CAPABILITY).ifPresent(data -> apply(player, data));
        }
    }

    @SuppressWarnings("unchecked")
    private static void apply(AbstractClientPlayer player, HunterPlayerData data) {
        ModifierLayer<KeyframeAnimationPlayer> layer = (ModifierLayer<KeyframeAnimationPlayer>) PlayerAnimationAccess.getPlayerAssociatedData(player).get(LAYER_ID);
        if (layer == null) {
            return;
        }

        String desired = resolveActiveAnimation(player, data);
        String current = LAST_APPLIED.getOrDefault(player.getUUID(), AnimationType.NONE.name());
        if (desired.equals(current)) {
            return;
        }

        if (AnimationType.NONE.name().equals(desired)) {
            layer.setAnimation(null);
            LAST_APPLIED.remove(player.getUUID());
            return;
        }

        KeyframeAnimation animation = switch (desired) {
            case "MEDITATE" -> MEDITATE_ANIMATION;
            case "SMOKING_PIPE" -> SMOKING_PIPE_ANIMATION;
            case "SMOKE_SOLDIER_SUMMON" -> SMOKE_SOLDIER_SUMMON_ANIMATION;
            case "DASH" -> DASH_ANIMATION;
            case "DOUBLE_JUMP" -> DOUBLE_JUMP_ANIMATION;
            case "FLASH_CLEAVE_ONE" -> FLASH_CLEAVE_ONE_ANIMATION;
            case "FLASH_CLEAVE_TWO" -> FLASH_CLEAVE_TWO_ANIMATION;
            case "FLASH_CLEAVE_THREE" -> FLASH_CLEAVE_THREE_ANIMATION;
            case "HEAVEN_SPLITTER" -> HEAVEN_SPLITTER_ANIMATION;
            case "SKYBREAKER_ASCENT" -> SKYBREAKER_ASCENT_ANIMATION;
            case "SKYBREAKER_DROP" -> SKYBREAKER_DROP_ANIMATION;
            case "AEGIS_RUSH" -> AEGIS_RUSH_ANIMATION;
            case "MIRROR_REPRISAL_GUARD" -> MIRROR_REPRISAL_GUARD_ANIMATION;
            case "MIRROR_REPRISAL_STRIKE" -> MIRROR_REPRISAL_STRIKE_ANIMATION;
            case "STEEL_TATSUMAKI" -> STEEL_TATSUMAKI_ANIMATION;
            case "BOXER_JAB" -> BOXER_JAB_ANIMATION;
            case "BOXER_BARRAGE" -> BOXER_BARRAGE_ANIMATION;
            case "BOXER_BARRAGE_FAST" -> BOXER_BARRAGE_FAST_ANIMATION;
            case "BOXER_HAMMER_STRIKE" -> BOXER_HAMMER_STRIKE_ANIMATION;
            case "BOXER_COUNTER_GUARD" -> BOXER_COUNTER_GUARD_ANIMATION;
            case "BOXER_COUNTER_STRIKE" -> BOXER_COUNTER_STRIKE_ANIMATION;
            case "BOXER_REDIRECTION" -> BOXER_REDIRECTION_ANIMATION;
            case "BOXER_GRAB_LIFT" -> BOXER_GRAB_LIFT_ANIMATION;
            case "BOXER_GRAB_SLAM" -> BOXER_GRAB_SLAM_ANIMATION;
            case "MARTIAL_METEOR_HEEL" -> MARTIAL_METEOR_HEEL_ANIMATION;
            case "MARTIAL_METEOR_HEEL_IMPACT" -> MARTIAL_METEOR_HEEL_IMPACT_ANIMATION;
            case "MARTIAL_ANKLE_SPLITTER" -> MARTIAL_ANKLE_SPLITTER_ANIMATION;
            case "MARTIAL_FACE_JAB" -> MARTIAL_FACE_JAB_ANIMATION;
            case "MARTIAL_WHIRLWIND_ARC" -> MARTIAL_WHIRLWIND_ARC_ANIMATION;
            case "MARTIAL_AIR_BARRAGE" -> MARTIAL_AIR_BARRAGE_ANIMATION;
            case "MARTIAL_RISING_SHOT_CHARGE" -> MARTIAL_RISING_SHOT_CHARGE_ANIMATION;
            case "MARTIAL_RISING_SHOT" -> MARTIAL_RISING_SHOT_ANIMATION;
            case "MARTIAL_TORA_HUNT_CHARGE" -> MARTIAL_TORA_HUNT_CHARGE_ANIMATION;
            case "MARTIAL_TORA_HUNT" -> MARTIAL_TORA_HUNT_ANIMATION;
            case "LION_FANG_DRAW_CHARGE" -> LION_FANG_DRAW_CHARGE_ANIMATION;
            case "VOID_REND_CHARGE" -> VOID_REND_CHARGE_ANIMATION;
            case "VOID_REND" -> VOID_REND_ANIMATION;
            case "GUARD" -> GUARD_ANIMATION;
            case "weapon_guard" -> WEAPON_GUARD_ANIMATION;
            case "PARRY" -> PARRY_ANIMATION;
            case "WEAPON_PARRY" -> WEAPON_PARRY_ANIMATION;
            case "ELASTIC_REFLECT" -> ELASTIC_REFLECT_ANIMATION;
            case "DOWSING_CHAIN_SWING" -> DOWSING_CHAIN_SWING_ANIMATION;
            default -> null;
        };
        if (animation == null) {
            return;
        }

        layer.replaceAnimationWithFade(AbstractFadeModifier.standardFadeIn(2, Ease.OUTCUBIC), new KeyframeAnimationPlayer(animation));
        LAST_APPLIED.put(player.getUUID(), desired);
    }

    private static String resolveActiveAnimation(AbstractClientPlayer player, HunterPlayerData data) {
        AnimationType currentAnimation = data.getCurrentAnimation();
        if (data.getAnimationTicks() <= 0 && !isContinuousAbilityAnimation(currentAnimation)) {
            return AnimationType.NONE.name();
        }
        return switch (currentAnimation) {
            case MEDITATE -> data.isMeditationActive() ? AnimationType.MEDITATE.name() : AnimationType.NONE.name();
            case SMOKING_PIPE -> player.isUsingItem() && isSmokingPipe(player.getUseItem()) ? AnimationType.SMOKING_PIPE.name() : AnimationType.NONE.name();
            case SMOKE_SOLDIER_SUMMON -> AnimationType.SMOKE_SOLDIER_SUMMON.name();
            case DASH -> (data.getDashIFrameTicks() > 0 || data.getActiveAbilityTicksRemaining() > 0 || data.getChargeTicksRemaining() > 0) ? AnimationType.DASH.name() : AnimationType.NONE.name();
            case DOUBLE_JUMP -> (data.getAirJumpsUsed() > 0 || data.hasAirLaunchFallProtection()) ? AnimationType.DOUBLE_JUMP.name() : AnimationType.NONE.name();
            case FLASH_CLEAVE_ONE, FLASH_CLEAVE_TWO, FLASH_CLEAVE_THREE -> (data.getActiveAbilityTicksRemaining() > 0 || data.getChargeTicksRemaining() > 0) ? currentAnimation.name() : AnimationType.NONE.name();
            case HEAVEN_SPLITTER -> data.getAnimationTicks() > 0 ? AnimationType.HEAVEN_SPLITTER.name() : AnimationType.NONE.name();
            case SKYBREAKER_ASCENT, SKYBREAKER_DROP -> (data.getActiveAbilityTicksRemaining() > 0 || data.getChargeTicksRemaining() > 0) ? currentAnimation.name() : AnimationType.NONE.name();
            case AEGIS_RUSH -> (data.getActiveAbilityTicksRemaining() > 0 || data.getChargeTicksRemaining() > 0) ? AnimationType.AEGIS_RUSH.name() : AnimationType.NONE.name();
            case MIRROR_REPRISAL_GUARD -> data.getActiveAbilityTicksRemaining() > 0 ? AnimationType.MIRROR_REPRISAL_GUARD.name() : AnimationType.NONE.name();
            case MIRROR_REPRISAL_STRIKE -> AnimationType.MIRROR_REPRISAL_STRIKE.name();
            case STEEL_TATSUMAKI -> (data.getActiveAbilityTicksRemaining() > 0 || data.getChargeTicksRemaining() > 0) ? AnimationType.STEEL_TATSUMAKI.name() : AnimationType.NONE.name();
            case BOXER_JAB -> AnimationType.BOXER_JAB.name();
            case BOXER_BARRAGE -> (data.getActiveAbilityTicksRemaining() > 0 || data.getChargeTicksRemaining() > 0) ? AnimationType.BOXER_BARRAGE.name() : AnimationType.NONE.name();
            case BOXER_BARRAGE_FAST -> data.getActiveAbilityTicksRemaining() > 0 ? AnimationType.BOXER_BARRAGE_FAST.name() : AnimationType.NONE.name();
            case BOXER_HAMMER_STRIKE -> AnimationType.BOXER_HAMMER_STRIKE.name();
            case BOXER_COUNTER_GUARD -> data.getActiveAbilityTicksRemaining() > 0 ? AnimationType.BOXER_COUNTER_GUARD.name() : AnimationType.NONE.name();
            case BOXER_COUNTER_STRIKE -> AnimationType.BOXER_COUNTER_STRIKE.name();
            case BOXER_REDIRECTION -> (data.getActiveAbilityTicksRemaining() > 0 || data.getChargeTicksRemaining() > 0) ? AnimationType.BOXER_REDIRECTION.name() : AnimationType.NONE.name();
            case BOXER_GRAB_LIFT, BOXER_GRAB_SLAM -> (data.getActiveAbilityTicksRemaining() > 0 || data.getChargeTicksRemaining() > 0) ? currentAnimation.name() : AnimationType.NONE.name();
            case MARTIAL_METEOR_HEEL, MARTIAL_METEOR_HEEL_IMPACT, MARTIAL_ANKLE_SPLITTER, MARTIAL_FACE_JAB, MARTIAL_WHIRLWIND_ARC, MARTIAL_RISING_SHOT, MARTIAL_TORA_HUNT -> currentAnimation.name();
            case MARTIAL_AIR_BARRAGE -> data.getActiveAbilityTicksRemaining() > 0 ? AnimationType.MARTIAL_AIR_BARRAGE.name() : AnimationType.NONE.name();
            case MARTIAL_RISING_SHOT_CHARGE -> data.getChargeTicksRemaining() > 0 ? AnimationType.MARTIAL_RISING_SHOT_CHARGE.name() : AnimationType.NONE.name();
            case MARTIAL_TORA_HUNT_CHARGE -> data.getChargeTicksRemaining() > 0 ? AnimationType.MARTIAL_TORA_HUNT_CHARGE.name() : AnimationType.NONE.name();
            case LION_FANG_DRAW_CHARGE -> data.getChargeTicksRemaining() > 0 ? AnimationType.LION_FANG_DRAW_CHARGE.name() : AnimationType.NONE.name();
            case VOID_REND_CHARGE -> data.getChargeTicksRemaining() > 0 ? AnimationType.VOID_REND_CHARGE.name() : AnimationType.NONE.name();
            case VOID_REND -> AnimationType.VOID_REND.name();
            case PARRY, WEAPON_PARRY -> AnimationType.NONE.name();
            case ELASTIC_REFLECT -> data.getActiveAbilityTicksRemaining() > 0 ? AnimationType.ELASTIC_REFLECT.name() : AnimationType.NONE.name();
            case DOWSING_CHAIN_SWING -> data.getAnimationTicks() > 0 ? AnimationType.DOWSING_CHAIN_SWING.name() : AnimationType.NONE.name();
            default -> AnimationType.NONE.name();
        };
    }

    private static boolean isContinuousAbilityAnimation(AnimationType type) {
        return type == AnimationType.BOXER_BARRAGE_FAST || type == AnimationType.BOXER_REDIRECTION || type == AnimationType.MEDITATE
                || type == AnimationType.SMOKING_PIPE || type == AnimationType.ELASTIC_REFLECT;
    }

    private static KeyframeAnimation buildMeditateAnimation() {
        KeyframeAnimation.AnimationBuilder builder = new KeyframeAnimation.AnimationBuilder(AnimationFormat.JSON_MC_ANIM);
        builder.beginTick = 0;
        builder.endTick = 12;
        builder.stopTick = 12;
        builder.isLooped = true;
        builder.returnTick = 10;
        builder.setName("huntercraft_meditate");
        builder.fullyEnableParts();

        addLinear(builder.body.pitch, 0, degrees(6.0F));
        addLinear(builder.body.pitch, 6, degrees(8.0F));
        addLinear(builder.body.pitch, 12, degrees(6.0F));
        addLinear(builder.head.pitch, 0, degrees(-4.0F));
        addLinear(builder.head.pitch, 6, degrees(-6.0F));
        addLinear(builder.head.pitch, 12, degrees(-4.0F));

        addLinear(builder.rightArm.pitch, 0, degrees(-66.0F));
        addLinear(builder.rightArm.pitch, 6, degrees(-72.0F));
        addLinear(builder.rightArm.roll, 0, degrees(18.0F));
        addLinear(builder.rightArm.roll, 6, degrees(22.0F));
        addLinear(builder.leftArm.pitch, 0, degrees(-66.0F));
        addLinear(builder.leftArm.pitch, 6, degrees(-72.0F));
        addLinear(builder.leftArm.roll, 0, degrees(-18.0F));
        addLinear(builder.leftArm.roll, 6, degrees(-22.0F));

        addLinear(builder.rightLeg.pitch, 0, degrees(58.0F));
        addLinear(builder.rightLeg.pitch, 6, degrees(62.0F));
        addLinear(builder.rightLeg.roll, 0, degrees(12.0F));
        addLinear(builder.rightLeg.roll, 6, degrees(16.0F));
        addLinear(builder.leftLeg.pitch, 0, degrees(58.0F));
        addLinear(builder.leftLeg.pitch, 6, degrees(62.0F));
        addLinear(builder.leftLeg.roll, 0, degrees(-12.0F));
        addLinear(builder.leftLeg.roll, 6, degrees(-16.0F));

        return builder.build();
    }

    private static KeyframeAnimation buildSmokingPipeAnimation() {
        KeyframeAnimation.AnimationBuilder builder = new KeyframeAnimation.AnimationBuilder(AnimationFormat.JSON_MC_ANIM);
        builder.beginTick = 0;
        builder.endTick = 8;
        builder.stopTick = 8;
        builder.isLooped = true;
        builder.returnTick = 6;
        builder.setName("huntercraft_smoking_pipe");
        builder.fullyEnableParts();

        addLinear(builder.body.pitch, 0, degrees(-3.0F));
        addLinear(builder.body.pitch, 4, degrees(-6.0F));
        addLinear(builder.body.pitch, 8, degrees(-3.0F));
        addLinear(builder.body.yaw, 0, degrees(2.0F));
        addLinear(builder.body.yaw, 4, degrees(5.0F));
        addLinear(builder.body.yaw, 8, degrees(2.0F));

        addLinear(builder.head.pitch, 0, degrees(1.0F));
        addLinear(builder.head.pitch, 4, degrees(3.0F));
        addLinear(builder.head.pitch, 8, degrees(1.0F));
        addLinear(builder.head.yaw, 0, degrees(4.0F));
        addLinear(builder.head.yaw, 4, degrees(7.0F));
        addLinear(builder.head.yaw, 8, degrees(4.0F));

        addLinear(builder.rightArm.pitch, 0, degrees(-112.0F));
        addLinear(builder.rightArm.pitch, 4, degrees(-118.0F));
        addLinear(builder.rightArm.pitch, 8, degrees(-112.0F));
        addLinear(builder.rightArm.yaw, 0, degrees(2.0F));
        addLinear(builder.rightArm.yaw, 4, degrees(-2.0F));
        addLinear(builder.rightArm.yaw, 8, degrees(2.0F));
        addLinear(builder.rightArm.roll, 0, degrees(66.0F));
        addLinear(builder.rightArm.roll, 4, degrees(72.0F));
        addLinear(builder.rightArm.roll, 8, degrees(66.0F));

        addLinear(builder.leftArm.pitch, 0, degrees(-10.0F));
        addLinear(builder.leftArm.pitch, 4, degrees(-14.0F));
        addLinear(builder.leftArm.pitch, 8, degrees(-10.0F));
        addLinear(builder.leftArm.yaw, 0, degrees(4.0F));
        addLinear(builder.leftArm.yaw, 4, degrees(8.0F));
        addLinear(builder.leftArm.yaw, 8, degrees(4.0F));
        addLinear(builder.leftArm.roll, 0, degrees(-6.0F));
        addLinear(builder.leftArm.roll, 4, degrees(-10.0F));
        addLinear(builder.leftArm.roll, 8, degrees(-6.0F));

        return builder.build();
    }

    private static KeyframeAnimation buildSmokeSoldierSummonAnimation() {
        KeyframeAnimation.AnimationBuilder builder = new KeyframeAnimation.AnimationBuilder(AnimationFormat.JSON_MC_ANIM);
        builder.beginTick = 0;
        builder.endTick = 10;
        builder.stopTick = 10;
        builder.setName("huntercraft_smoke_soldier_summon");
        builder.fullyEnableParts();

        addLinear(builder.body.pitch, 0, degrees(-4.0F));
        addLinear(builder.body.pitch, 4, degrees(-12.0F));
        addLinear(builder.body.pitch, 10, degrees(-2.0F));
        addLinear(builder.head.pitch, 0, degrees(2.0F));
        addLinear(builder.head.pitch, 4, degrees(-10.0F));
        addLinear(builder.head.pitch, 10, degrees(0.0F));

        addLinear(builder.rightArm.pitch, 0, degrees(-28.0F));
        addLinear(builder.rightArm.pitch, 4, degrees(-94.0F));
        addLinear(builder.rightArm.pitch, 10, degrees(-12.0F));
        addLinear(builder.rightArm.yaw, 0, degrees(-10.0F));
        addLinear(builder.rightArm.yaw, 4, degrees(-28.0F));
        addLinear(builder.rightArm.yaw, 10, degrees(0.0F));
        addLinear(builder.rightArm.roll, 0, degrees(12.0F));
        addLinear(builder.rightArm.roll, 4, degrees(42.0F));
        addLinear(builder.rightArm.roll, 10, degrees(8.0F));

        addLinear(builder.leftArm.pitch, 0, degrees(-12.0F));
        addLinear(builder.leftArm.pitch, 4, degrees(-42.0F));
        addLinear(builder.leftArm.pitch, 10, degrees(-8.0F));
        addLinear(builder.leftArm.yaw, 0, degrees(8.0F));
        addLinear(builder.leftArm.yaw, 4, degrees(18.0F));
        addLinear(builder.leftArm.yaw, 10, degrees(2.0F));
        addLinear(builder.leftArm.roll, 0, degrees(-6.0F));
        addLinear(builder.leftArm.roll, 4, degrees(-18.0F));
        addLinear(builder.leftArm.roll, 10, degrees(-4.0F));

        return builder.build();
    }

    private static KeyframeAnimation buildDashAnimation() {
        KeyframeAnimation.AnimationBuilder builder = new KeyframeAnimation.AnimationBuilder(AnimationFormat.JSON_MC_ANIM);
        builder.beginTick = 0;
        builder.endTick = 2;
        builder.stopTick = 2;
        builder.setName("huntercraft_dash");
        builder.fullyEnableParts();

        addLinear(builder.head.pitch, 0, 0.0F);
        addLinear(builder.head.pitch, 2, degrees(15.0F));

        addLinear(builder.leftArm.pitch, 0, 0.0F);
        addLinear(builder.leftArm.pitch, 2, degrees(97.5F));
        addLinear(builder.leftArm.roll, 0, 0.0F);
        addLinear(builder.leftArm.roll, 2, degrees(-6.0F));

        addLinear(builder.rightArm.pitch, 0, 0.0F);
        addLinear(builder.rightArm.pitch, 2, degrees(-77.5F));
        addLinear(builder.rightArm.roll, 0, 0.0F);
        addLinear(builder.rightArm.roll, 2, degrees(6.0F));

        addLinear(builder.leftLeg.pitch, 0, 0.0F);
        addLinear(builder.leftLeg.pitch, 2, degrees(-32.5F));

        addLinear(builder.rightLeg.pitch, 0, 0.0F);
        addLinear(builder.rightLeg.pitch, 2, degrees(32.5F));

        addLinear(builder.body.pitch, 0, 0.0F);
        addLinear(builder.body.pitch, 2, degrees(-22.5F));

        return builder.build();
    }

    private static KeyframeAnimation buildWeaponGuardAnimation() {
        KeyframeAnimation.AnimationBuilder builder = new KeyframeAnimation.AnimationBuilder(AnimationFormat.JSON_MC_ANIM);
        builder.beginTick = 0;
        builder.endTick = 8;
        builder.stopTick = 8;
        builder.isLooped = true;
        builder.returnTick = 6;
        builder.setName("huntercraft_weapon_guard");
        builder.fullyEnableParts();

        addLinear(builder.body.pitch, 0, 0.0F);
        addLinear(builder.body.pitch, 4, degrees(-8.0F));
        addLinear(builder.body.pitch, 8, degrees(-6.0F));
        addLinear(builder.body.yaw, 0, 0.0F);
        addLinear(builder.body.yaw, 4, degrees(-10.0F));
        addLinear(builder.body.yaw, 8, degrees(-8.0F));
        addLinear(builder.head.pitch, 0, 0.0F);
        addLinear(builder.head.pitch, 4, degrees(5.0F));
        addLinear(builder.head.pitch, 8, degrees(4.0F));
        addLinear(builder.head.yaw, 0, 0.0F);
        addLinear(builder.head.yaw, 4, degrees(8.0F));
        addLinear(builder.head.yaw, 8, degrees(6.0F));

        addLinear(builder.rightArm.pitch, 0, 0.0F);
        addLinear(builder.rightArm.pitch, 4, degrees(-82.0F));
        addLinear(builder.rightArm.pitch, 8, degrees(-88.0F));
        addLinear(builder.rightArm.yaw, 0, 0.0F);
        addLinear(builder.rightArm.yaw, 4, degrees(-42.0F));
        addLinear(builder.rightArm.yaw, 8, degrees(-38.0F));
        addLinear(builder.rightArm.roll, 0, 0.0F);
        addLinear(builder.rightArm.roll, 4, degrees(36.0F));
        addLinear(builder.rightArm.roll, 8, degrees(32.0F));

        addLinear(builder.leftArm.pitch, 0, 0.0F);
        addLinear(builder.leftArm.pitch, 4, degrees(-72.0F));
        addLinear(builder.leftArm.pitch, 8, degrees(-78.0F));
        addLinear(builder.leftArm.yaw, 0, 0.0F);
        addLinear(builder.leftArm.yaw, 4, degrees(44.0F));
        addLinear(builder.leftArm.yaw, 8, degrees(40.0F));
        addLinear(builder.leftArm.roll, 0, 0.0F);
        addLinear(builder.leftArm.roll, 4, degrees(-52.0F));
        addLinear(builder.leftArm.roll, 8, degrees(-48.0F));

        addLinear(builder.rightLeg.pitch, 0, 0.0F);
        addLinear(builder.rightLeg.pitch, 4, degrees(14.0F));
        addLinear(builder.rightLeg.pitch, 8, degrees(12.0F));
        addLinear(builder.rightLeg.roll, 0, 0.0F);
        addLinear(builder.rightLeg.roll, 4, degrees(5.0F));
        addLinear(builder.leftLeg.pitch, 0, 0.0F);
        addLinear(builder.leftLeg.pitch, 4, degrees(-10.0F));
        addLinear(builder.leftLeg.pitch, 8, degrees(-8.0F));
        addLinear(builder.leftLeg.roll, 0, 0.0F);
        addLinear(builder.leftLeg.roll, 4, degrees(-5.0F));

        return builder.build();
    }

    private static boolean isWeaponGuard(AbstractClientPlayer player) {
        return isGuardWeapon(player.getMainHandItem()) || isGuardWeapon(player.getOffhandItem());
    }

    private static boolean isGuardWeapon(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        return stack.getItem() instanceof SwordItem
                || stack.getItem() instanceof AxeItem
                || stack.getItem() instanceof TridentItem;
    }

    private static boolean isSmokingPipe(ItemStack stack) {
        return !stack.isEmpty() && stack.is(HunterItems.SMOKING_PIPE.get());
    }

    private static KeyframeAnimation buildDoubleJumpAnimation() {
        KeyframeAnimation.AnimationBuilder builder = new KeyframeAnimation.AnimationBuilder(AnimationFormat.JSON_MC_ANIM);
        builder.beginTick = 0;
        builder.endTick = 4;
        builder.stopTick = 4;
        builder.setName("huntercraft_double_jump");
        builder.fullyEnableParts();

        addLinear(builder.body.pitch, 0, 0.0F);
        addLinear(builder.body.pitch, 4, degrees(-12.0F));
        addLinear(builder.leftArm.pitch, 0, 0.0F);
        addLinear(builder.leftArm.pitch, 4, degrees(130.0F));
        addLinear(builder.rightArm.pitch, 0, 0.0F);
        addLinear(builder.rightArm.pitch, 4, degrees(130.0F));
        addLinear(builder.leftArm.roll, 0, 0.0F);
        addLinear(builder.leftArm.roll, 4, degrees(-18.0F));
        addLinear(builder.rightArm.roll, 0, 0.0F);
        addLinear(builder.rightArm.roll, 4, degrees(18.0F));
        addLinear(builder.leftLeg.pitch, 0, 0.0F);
        addLinear(builder.leftLeg.pitch, 4, degrees(28.0F));
        addLinear(builder.rightLeg.pitch, 0, 0.0F);
        addLinear(builder.rightLeg.pitch, 4, degrees(28.0F));

        return builder.build();
    }

    private static KeyframeAnimation buildFlashCleaveAnimation(String name, float bodyYaw, float leftArmYaw, float rightArmYaw) {
        KeyframeAnimation.AnimationBuilder builder = new KeyframeAnimation.AnimationBuilder(AnimationFormat.JSON_MC_ANIM);
        builder.beginTick = 0;
        builder.endTick = 4;
        builder.stopTick = 4;
        builder.setName(name);
        builder.fullyEnableParts();

        addLinear(builder.body.yaw, 0, degrees(bodyYaw * 0.35F));
        addLinear(builder.body.yaw, 2, degrees(bodyYaw));
        addLinear(builder.body.yaw, 4, degrees(bodyYaw * 0.2F));
        addLinear(builder.body.pitch, 0, degrees(-4.0F));
        addLinear(builder.body.pitch, 2, degrees(-12.0F));
        addLinear(builder.body.pitch, 4, degrees(-4.0F));

        addLinear(builder.rightArm.pitch, 0, degrees(-112.0F));
        addLinear(builder.rightArm.pitch, 2, degrees(-46.0F));
        addLinear(builder.rightArm.pitch, 4, degrees(-118.0F));
        addLinear(builder.rightArm.yaw, 0, degrees(rightArmYaw * 0.45F));
        addLinear(builder.rightArm.yaw, 2, degrees(rightArmYaw));
        addLinear(builder.rightArm.yaw, 4, degrees(rightArmYaw * 0.15F));
        addLinear(builder.rightArm.roll, 0, degrees(44.0F));
        addLinear(builder.rightArm.roll, 2, degrees(74.0F));
        addLinear(builder.rightArm.roll, 4, degrees(24.0F));

        addLinear(builder.leftArm.pitch, 0, degrees(-74.0F));
        addLinear(builder.leftArm.pitch, 2, degrees(-118.0F));
        addLinear(builder.leftArm.pitch, 4, degrees(-84.0F));
        addLinear(builder.leftArm.yaw, 0, degrees(leftArmYaw * 0.45F));
        addLinear(builder.leftArm.yaw, 2, degrees(leftArmYaw));
        addLinear(builder.leftArm.yaw, 4, degrees(leftArmYaw * 0.15F));
        addLinear(builder.leftArm.roll, 0, degrees(-20.0F));
        addLinear(builder.leftArm.roll, 2, degrees(-58.0F));
        addLinear(builder.leftArm.roll, 4, degrees(-18.0F));

        addLinear(builder.rightLeg.pitch, 0, degrees(12.0F));
        addLinear(builder.rightLeg.pitch, 2, degrees(-10.0F));
        addLinear(builder.rightLeg.pitch, 4, degrees(8.0F));
        addLinear(builder.leftLeg.pitch, 0, degrees(-8.0F));
        addLinear(builder.leftLeg.pitch, 2, degrees(18.0F));
        addLinear(builder.leftLeg.pitch, 4, degrees(-6.0F));

        return builder.build();
    }

    private static KeyframeAnimation buildHeavenSplitterAnimation() {
        KeyframeAnimation.AnimationBuilder builder = new KeyframeAnimation.AnimationBuilder(AnimationFormat.JSON_MC_ANIM);
        builder.beginTick = 0;
        builder.endTick = 8;
        builder.stopTick = 8;
        builder.setName("huntercraft_heaven_splitter");
        builder.fullyEnableParts();

        addLinear(builder.body.pitch, 0, degrees(-8.0F));
        addLinear(builder.body.pitch, 4, degrees(-22.0F));
        addLinear(builder.body.pitch, 8, degrees(-2.0F));
        addLinear(builder.rightArm.pitch, 0, degrees(-55.0F));
        addLinear(builder.rightArm.pitch, 4, degrees(-166.0F));
        addLinear(builder.rightArm.pitch, 8, degrees(-28.0F));
        addLinear(builder.leftArm.pitch, 0, degrees(-40.0F));
        addLinear(builder.leftArm.pitch, 4, degrees(-132.0F));
        addLinear(builder.leftArm.pitch, 8, degrees(-18.0F));
        addLinear(builder.rightArm.roll, 0, degrees(14.0F));
        addLinear(builder.rightArm.roll, 4, degrees(42.0F));
        addLinear(builder.leftArm.roll, 0, degrees(-10.0F));
        addLinear(builder.leftArm.roll, 4, degrees(-34.0F));
        addLinear(builder.rightLeg.pitch, 0, degrees(10.0F));
        addLinear(builder.rightLeg.pitch, 4, degrees(-14.0F));
        addLinear(builder.leftLeg.pitch, 0, degrees(-8.0F));
        addLinear(builder.leftLeg.pitch, 4, degrees(18.0F));
        return builder.build();
    }

    private static KeyframeAnimation buildSkybreakerAscentAnimation() {
        KeyframeAnimation.AnimationBuilder builder = new KeyframeAnimation.AnimationBuilder(AnimationFormat.JSON_MC_ANIM);
        builder.beginTick = 0;
        builder.endTick = 10;
        builder.stopTick = 10;
        builder.isLooped = true;
        builder.returnTick = 7;
        builder.setName("huntercraft_skybreaker_ascent");
        builder.fullyEnableParts();

        addLinear(builder.body.pitch, 0, degrees(-8.0F));
        addLinear(builder.body.pitch, 4, degrees(-20.0F));
        addLinear(builder.body.pitch, 10, degrees(-20.0F));
        addLinear(builder.rightArm.pitch, 0, degrees(-70.0F));
        addLinear(builder.rightArm.pitch, 4, degrees(-148.0F));
        addLinear(builder.rightArm.pitch, 10, degrees(-148.0F));
        addLinear(builder.leftArm.pitch, 0, degrees(-40.0F));
        addLinear(builder.leftArm.pitch, 4, degrees(-108.0F));
        addLinear(builder.leftArm.pitch, 10, degrees(-108.0F));
        addLinear(builder.rightLeg.pitch, 0, degrees(12.0F));
        addLinear(builder.rightLeg.pitch, 4, degrees(28.0F));
        addLinear(builder.leftLeg.pitch, 0, degrees(-8.0F));
        addLinear(builder.leftLeg.pitch, 4, degrees(-24.0F));
        return builder.build();
    }

    private static KeyframeAnimation buildSkybreakerDropAnimation() {
        KeyframeAnimation.AnimationBuilder builder = new KeyframeAnimation.AnimationBuilder(AnimationFormat.JSON_MC_ANIM);
        builder.beginTick = 0;
        builder.endTick = 10;
        builder.stopTick = 10;
        builder.setName("huntercraft_skybreaker_drop");
        builder.fullyEnableParts();

        addLinear(builder.body.pitch, 0, degrees(6.0F));
        addLinear(builder.body.pitch, 4, degrees(18.0F));
        addLinear(builder.body.pitch, 10, degrees(4.0F));
        addLinear(builder.rightArm.pitch, 0, degrees(-154.0F));
        addLinear(builder.rightArm.pitch, 4, degrees(-28.0F));
        addLinear(builder.leftArm.pitch, 0, degrees(-116.0F));
        addLinear(builder.leftArm.pitch, 4, degrees(-36.0F));
        addLinear(builder.rightArm.yaw, 0, degrees(-24.0F));
        addLinear(builder.leftArm.yaw, 0, degrees(18.0F));
        addLinear(builder.rightLeg.pitch, 0, degrees(22.0F));
        addLinear(builder.leftLeg.pitch, 0, degrees(18.0F));
        addLinear(builder.rightLeg.pitch, 4, degrees(-18.0F));
        addLinear(builder.leftLeg.pitch, 4, degrees(-12.0F));
        return builder.build();
    }

    private static KeyframeAnimation buildAegisRushAnimation() {
        KeyframeAnimation.AnimationBuilder builder = new KeyframeAnimation.AnimationBuilder(AnimationFormat.JSON_MC_ANIM);
        builder.beginTick = 0;
        builder.endTick = 14;
        builder.stopTick = 14;
        builder.isLooped = true;
        builder.returnTick = 8;
        builder.setName("huntercraft_aegis_rush");
        builder.fullyEnableParts();

        addLinear(builder.body.pitch, 0, degrees(-10.0F));
        addLinear(builder.body.pitch, 6, degrees(-18.0F));
        addLinear(builder.body.pitch, 14, degrees(-14.0F));
        addLinear(builder.body.yaw, 0, degrees(-8.0F));
        addLinear(builder.body.yaw, 6, degrees(10.0F));
        addLinear(builder.body.yaw, 14, degrees(-6.0F));
        addLinear(builder.rightArm.pitch, 0, degrees(-122.0F));
        addLinear(builder.rightArm.pitch, 6, degrees(-62.0F));
        addLinear(builder.rightArm.pitch, 14, degrees(-128.0F));
        addLinear(builder.leftArm.pitch, 0, degrees(-78.0F));
        addLinear(builder.leftArm.pitch, 6, degrees(-138.0F));
        addLinear(builder.leftArm.pitch, 14, degrees(-84.0F));
        addLinear(builder.rightLeg.pitch, 0, degrees(18.0F));
        addLinear(builder.rightLeg.pitch, 6, degrees(-18.0F));
        addLinear(builder.leftLeg.pitch, 0, degrees(-12.0F));
        addLinear(builder.leftLeg.pitch, 6, degrees(24.0F));
        return builder.build();
    }

    private static KeyframeAnimation buildMirrorReprisalGuardAnimation() {
        KeyframeAnimation.AnimationBuilder builder = new KeyframeAnimation.AnimationBuilder(AnimationFormat.JSON_MC_ANIM);
        builder.beginTick = 0;
        builder.endTick = 20;
        builder.stopTick = 20;
        builder.isLooped = true;
        builder.returnTick = 12;
        builder.setName("huntercraft_mirror_reprisal_guard");
        builder.fullyEnableParts();

        addLinear(builder.rightArm.pitch, 0, degrees(-106.0F));
        addLinear(builder.rightArm.pitch, 6, degrees(-124.0F));
        addLinear(builder.rightArm.pitch, 20, degrees(-124.0F));
        addLinear(builder.rightArm.yaw, 0, degrees(-16.0F));
        addLinear(builder.rightArm.yaw, 6, degrees(-28.0F));
        addLinear(builder.rightArm.roll, 0, degrees(24.0F));
        addLinear(builder.rightArm.roll, 6, degrees(34.0F));
        addLinear(builder.leftArm.pitch, 0, degrees(-98.0F));
        addLinear(builder.leftArm.pitch, 6, degrees(-120.0F));
        addLinear(builder.leftArm.pitch, 20, degrees(-120.0F));
        addLinear(builder.leftArm.yaw, 0, degrees(14.0F));
        addLinear(builder.leftArm.yaw, 6, degrees(26.0F));
        addLinear(builder.leftArm.roll, 0, degrees(-20.0F));
        addLinear(builder.leftArm.roll, 6, degrees(-30.0F));
        return builder.build();
    }

    private static KeyframeAnimation buildMirrorReprisalStrikeAnimation() {
        KeyframeAnimation.AnimationBuilder builder = new KeyframeAnimation.AnimationBuilder(AnimationFormat.JSON_MC_ANIM);
        builder.beginTick = 0;
        builder.endTick = 8;
        builder.stopTick = 8;
        builder.setName("huntercraft_mirror_reprisal_strike");
        builder.fullyEnableParts();

        addLinear(builder.body.yaw, 0, degrees(-14.0F));
        addLinear(builder.body.yaw, 4, degrees(20.0F));
        addLinear(builder.body.pitch, 0, degrees(-10.0F));
        addLinear(builder.body.pitch, 4, degrees(-22.0F));
        addLinear(builder.rightArm.pitch, 0, degrees(-132.0F));
        addLinear(builder.rightArm.pitch, 4, degrees(-34.0F));
        addLinear(builder.rightArm.yaw, 0, degrees(-24.0F));
        addLinear(builder.rightArm.yaw, 4, degrees(32.0F));
        addLinear(builder.leftArm.pitch, 0, degrees(-92.0F));
        addLinear(builder.leftArm.pitch, 4, degrees(-124.0F));
        addLinear(builder.rightLeg.pitch, 0, degrees(16.0F));
        addLinear(builder.rightLeg.pitch, 4, degrees(-10.0F));
        addLinear(builder.leftLeg.pitch, 0, degrees(-8.0F));
        addLinear(builder.leftLeg.pitch, 4, degrees(16.0F));
        return builder.build();
    }

    private static KeyframeAnimation buildSteelTatsumakiAnimation() {
        KeyframeAnimation.AnimationBuilder builder = new KeyframeAnimation.AnimationBuilder(AnimationFormat.JSON_MC_ANIM);
        builder.beginTick = 0;
        builder.endTick = 12;
        builder.stopTick = 12;
        builder.isLooped = true;
        builder.returnTick = 6;
        builder.setName("huntercraft_steel_tatsumaki");
        builder.fullyEnableParts();

        addLinear(builder.body.pitch, 0, degrees(-10.0F));
        addLinear(builder.body.pitch, 3, degrees(-18.0F));
        addLinear(builder.body.pitch, 6, degrees(-8.0F));
        addLinear(builder.body.pitch, 9, degrees(-18.0F));
        addLinear(builder.body.pitch, 12, degrees(-10.0F));
        addLinear(builder.body.yaw, 0, degrees(-54.0F));
        addLinear(builder.body.yaw, 3, degrees(34.0F));
        addLinear(builder.body.yaw, 6, degrees(58.0F));
        addLinear(builder.body.yaw, 9, degrees(-30.0F));
        addLinear(builder.body.yaw, 12, degrees(-54.0F));
        addLinear(builder.body.roll, 0, degrees(-8.0F));
        addLinear(builder.body.roll, 3, degrees(12.0F));
        addLinear(builder.body.roll, 6, degrees(8.0F));
        addLinear(builder.body.roll, 9, degrees(-12.0F));
        addLinear(builder.body.roll, 12, degrees(-8.0F));

        addLinear(builder.rightArm.pitch, 0, degrees(-150.0F));
        addLinear(builder.rightArm.pitch, 3, degrees(-32.0F));
        addLinear(builder.rightArm.pitch, 6, degrees(-124.0F));
        addLinear(builder.rightArm.pitch, 9, degrees(-46.0F));
        addLinear(builder.rightArm.pitch, 12, degrees(-150.0F));
        addLinear(builder.rightArm.yaw, 0, degrees(54.0F));
        addLinear(builder.rightArm.yaw, 3, degrees(-62.0F));
        addLinear(builder.rightArm.yaw, 6, degrees(-78.0F));
        addLinear(builder.rightArm.yaw, 9, degrees(48.0F));
        addLinear(builder.rightArm.yaw, 12, degrees(54.0F));
        addLinear(builder.rightArm.roll, 0, degrees(96.0F));
        addLinear(builder.rightArm.roll, 3, degrees(18.0F));
        addLinear(builder.rightArm.roll, 6, degrees(-88.0F));
        addLinear(builder.rightArm.roll, 9, degrees(-18.0F));
        addLinear(builder.rightArm.roll, 12, degrees(96.0F));

        addLinear(builder.leftArm.pitch, 0, degrees(-38.0F));
        addLinear(builder.leftArm.pitch, 3, degrees(-118.0F));
        addLinear(builder.leftArm.pitch, 6, degrees(-42.0F));
        addLinear(builder.leftArm.pitch, 9, degrees(-112.0F));
        addLinear(builder.leftArm.pitch, 12, degrees(-38.0F));
        addLinear(builder.leftArm.yaw, 0, degrees(-38.0F));
        addLinear(builder.leftArm.yaw, 3, degrees(48.0F));
        addLinear(builder.leftArm.yaw, 6, degrees(34.0F));
        addLinear(builder.leftArm.yaw, 9, degrees(-46.0F));
        addLinear(builder.leftArm.yaw, 12, degrees(-38.0F));
        addLinear(builder.leftArm.roll, 0, degrees(-34.0F));
        addLinear(builder.leftArm.roll, 3, degrees(-86.0F));
        addLinear(builder.leftArm.roll, 6, degrees(30.0F));
        addLinear(builder.leftArm.roll, 9, degrees(78.0F));
        addLinear(builder.leftArm.roll, 12, degrees(-34.0F));

        addLinear(builder.rightLeg.pitch, 0, degrees(18.0F));
        addLinear(builder.rightLeg.pitch, 6, degrees(4.0F));
        addLinear(builder.rightLeg.pitch, 12, degrees(18.0F));
        addLinear(builder.rightLeg.roll, 0, degrees(10.0F));
        addLinear(builder.rightLeg.roll, 6, degrees(18.0F));
        addLinear(builder.rightLeg.roll, 12, degrees(10.0F));
        addLinear(builder.leftLeg.pitch, 0, degrees(-12.0F));
        addLinear(builder.leftLeg.pitch, 6, degrees(8.0F));
        addLinear(builder.leftLeg.pitch, 12, degrees(-12.0F));
        addLinear(builder.leftLeg.roll, 0, degrees(-10.0F));
        addLinear(builder.leftLeg.roll, 6, degrees(-18.0F));
        addLinear(builder.leftLeg.roll, 12, degrees(-10.0F));
        return builder.build();
    }

    private static KeyframeAnimation buildBoxerJabAnimation() {
        KeyframeAnimation.AnimationBuilder builder = new KeyframeAnimation.AnimationBuilder(AnimationFormat.JSON_MC_ANIM);
        builder.beginTick = 0;
        builder.endTick = 6;
        builder.stopTick = 6;
        builder.setName("huntercraft_boxer_jab");
        builder.fullyEnableParts();
        addLinear(builder.body.pitch, 0, degrees(-4.0F));
        addLinear(builder.body.pitch, 3, degrees(-12.0F));
        addLinear(builder.rightArm.pitch, 0, degrees(-82.0F));
        addLinear(builder.rightArm.pitch, 3, degrees(-18.0F));
        addLinear(builder.leftArm.pitch, 0, degrees(-28.0F));
        addLinear(builder.leftArm.pitch, 3, degrees(-92.0F));
        addLinear(builder.rightLeg.pitch, 0, degrees(10.0F));
        addLinear(builder.rightLeg.pitch, 3, degrees(-6.0F));
        return builder.build();
    }

    private static KeyframeAnimation buildBoxerBarrageAnimation() {
        KeyframeAnimation.AnimationBuilder builder = new KeyframeAnimation.AnimationBuilder(AnimationFormat.JSON_MC_ANIM);
        builder.beginTick = 0;
        builder.endTick = 8;
        builder.stopTick = 8;
        builder.isLooped = true;
        builder.returnTick = 4;
        builder.setName("huntercraft_boxer_barrage");
        builder.fullyEnableParts();
        addLinear(builder.body.pitch, 0, degrees(-8.0F));
        addLinear(builder.body.pitch, 4, degrees(-14.0F));
        addLinear(builder.rightArm.pitch, 0, degrees(-112.0F));
        addLinear(builder.rightArm.pitch, 4, degrees(-24.0F));
        addLinear(builder.leftArm.pitch, 0, degrees(-38.0F));
        addLinear(builder.leftArm.pitch, 4, degrees(-124.0F));
        addLinear(builder.rightLeg.pitch, 0, degrees(12.0F));
        addLinear(builder.rightLeg.pitch, 4, degrees(-8.0F));
        addLinear(builder.leftLeg.pitch, 0, degrees(-8.0F));
        addLinear(builder.leftLeg.pitch, 4, degrees(14.0F));
        return builder.build();
    }

    private static KeyframeAnimation buildBoxerBarrageFastAnimation() {
        KeyframeAnimation.AnimationBuilder builder = new KeyframeAnimation.AnimationBuilder(AnimationFormat.JSON_MC_ANIM);
        builder.beginTick = 0;
        builder.endTick = 8;
        builder.stopTick = 8;
        builder.isLooped = true;
        builder.returnTick = 4;
        builder.setName("huntercraft_boxer_barrage_fast");
        builder.fullyEnableParts();

        addLinear(builder.body.pitch, 0, degrees(-13.0F));
        addLinear(builder.body.pitch, 2, degrees(-19.0F));
        addLinear(builder.body.pitch, 4, degrees(-13.0F));
        addLinear(builder.body.pitch, 6, degrees(-19.0F));
        addLinear(builder.body.pitch, 8, degrees(-13.0F));
        addLinear(builder.body.yaw, 0, degrees(-8.0F));
        addLinear(builder.body.yaw, 2, degrees(9.0F));
        addLinear(builder.body.yaw, 4, degrees(-8.0F));
        addLinear(builder.body.yaw, 6, degrees(9.0F));
        addLinear(builder.body.yaw, 8, degrees(-8.0F));

        addLinear(builder.head.pitch, 0, degrees(4.0F));
        addLinear(builder.head.pitch, 2, degrees(8.0F));
        addLinear(builder.head.pitch, 4, degrees(4.0F));
        addLinear(builder.head.pitch, 6, degrees(8.0F));
        addLinear(builder.head.pitch, 8, degrees(4.0F));
        addLinear(builder.head.yaw, 0, degrees(6.0F));
        addLinear(builder.head.yaw, 2, degrees(-6.0F));
        addLinear(builder.head.yaw, 4, degrees(6.0F));
        addLinear(builder.head.yaw, 6, degrees(-6.0F));
        addLinear(builder.head.yaw, 8, degrees(6.0F));

        addLinear(builder.rightArm.pitch, 0, degrees(-126.0F));
        addLinear(builder.rightArm.pitch, 1, degrees(-36.0F));
        addLinear(builder.rightArm.pitch, 2, degrees(-48.0F));
        addLinear(builder.rightArm.pitch, 3, degrees(-116.0F));
        addLinear(builder.rightArm.pitch, 4, degrees(-126.0F));
        addLinear(builder.rightArm.pitch, 5, degrees(-36.0F));
        addLinear(builder.rightArm.pitch, 6, degrees(-48.0F));
        addLinear(builder.rightArm.pitch, 7, degrees(-116.0F));
        addLinear(builder.rightArm.pitch, 8, degrees(-126.0F));
        addLinear(builder.rightArm.yaw, 0, degrees(-30.0F));
        addLinear(builder.rightArm.yaw, 1, degrees(18.0F));
        addLinear(builder.rightArm.yaw, 2, degrees(24.0F));
        addLinear(builder.rightArm.yaw, 3, degrees(-20.0F));
        addLinear(builder.rightArm.yaw, 4, degrees(-30.0F));
        addLinear(builder.rightArm.yaw, 5, degrees(18.0F));
        addLinear(builder.rightArm.yaw, 6, degrees(24.0F));
        addLinear(builder.rightArm.yaw, 7, degrees(-20.0F));
        addLinear(builder.rightArm.yaw, 8, degrees(-30.0F));
        addLinear(builder.rightArm.roll, 0, degrees(8.0F));
        addLinear(builder.rightArm.roll, 1, degrees(28.0F));
        addLinear(builder.rightArm.roll, 2, degrees(24.0F));
        addLinear(builder.rightArm.roll, 3, degrees(10.0F));
        addLinear(builder.rightArm.roll, 4, degrees(8.0F));
        addLinear(builder.rightArm.roll, 5, degrees(28.0F));
        addLinear(builder.rightArm.roll, 6, degrees(24.0F));
        addLinear(builder.rightArm.roll, 7, degrees(10.0F));
        addLinear(builder.rightArm.roll, 8, degrees(8.0F));

        addLinear(builder.leftArm.pitch, 0, degrees(-48.0F));
        addLinear(builder.leftArm.pitch, 1, degrees(-118.0F));
        addLinear(builder.leftArm.pitch, 2, degrees(-128.0F));
        addLinear(builder.leftArm.pitch, 3, degrees(-38.0F));
        addLinear(builder.leftArm.pitch, 4, degrees(-48.0F));
        addLinear(builder.leftArm.pitch, 5, degrees(-118.0F));
        addLinear(builder.leftArm.pitch, 6, degrees(-128.0F));
        addLinear(builder.leftArm.pitch, 7, degrees(-38.0F));
        addLinear(builder.leftArm.pitch, 8, degrees(-48.0F));
        addLinear(builder.leftArm.yaw, 0, degrees(-24.0F));
        addLinear(builder.leftArm.yaw, 1, degrees(18.0F));
        addLinear(builder.leftArm.yaw, 2, degrees(30.0F));
        addLinear(builder.leftArm.yaw, 3, degrees(-18.0F));
        addLinear(builder.leftArm.yaw, 4, degrees(-24.0F));
        addLinear(builder.leftArm.yaw, 5, degrees(18.0F));
        addLinear(builder.leftArm.yaw, 6, degrees(30.0F));
        addLinear(builder.leftArm.yaw, 7, degrees(-18.0F));
        addLinear(builder.leftArm.yaw, 8, degrees(-24.0F));
        addLinear(builder.leftArm.roll, 0, degrees(-24.0F));
        addLinear(builder.leftArm.roll, 1, degrees(-10.0F));
        addLinear(builder.leftArm.roll, 2, degrees(-8.0F));
        addLinear(builder.leftArm.roll, 3, degrees(-30.0F));
        addLinear(builder.leftArm.roll, 4, degrees(-24.0F));
        addLinear(builder.leftArm.roll, 5, degrees(-10.0F));
        addLinear(builder.leftArm.roll, 6, degrees(-8.0F));
        addLinear(builder.leftArm.roll, 7, degrees(-30.0F));
        addLinear(builder.leftArm.roll, 8, degrees(-24.0F));

        addLinear(builder.rightLeg.pitch, 0, degrees(18.0F));
        addLinear(builder.rightLeg.pitch, 2, degrees(10.0F));
        addLinear(builder.rightLeg.pitch, 4, degrees(18.0F));
        addLinear(builder.rightLeg.pitch, 6, degrees(10.0F));
        addLinear(builder.rightLeg.pitch, 8, degrees(18.0F));
        addLinear(builder.rightLeg.roll, 0, degrees(7.0F));
        addLinear(builder.rightLeg.roll, 4, degrees(9.0F));
        addLinear(builder.rightLeg.roll, 8, degrees(7.0F));
        addLinear(builder.leftLeg.pitch, 0, degrees(-16.0F));
        addLinear(builder.leftLeg.pitch, 2, degrees(-10.0F));
        addLinear(builder.leftLeg.pitch, 4, degrees(-16.0F));
        addLinear(builder.leftLeg.pitch, 6, degrees(-10.0F));
        addLinear(builder.leftLeg.pitch, 8, degrees(-16.0F));
        addLinear(builder.leftLeg.roll, 0, degrees(-7.0F));
        addLinear(builder.leftLeg.roll, 4, degrees(-9.0F));
        addLinear(builder.leftLeg.roll, 8, degrees(-7.0F));
        return builder.build();
    }

    private static KeyframeAnimation buildBoxerHammerStrikeAnimation() {
        KeyframeAnimation.AnimationBuilder builder = new KeyframeAnimation.AnimationBuilder(AnimationFormat.JSON_MC_ANIM);
        builder.beginTick = 0;
        builder.endTick = 7;
        builder.stopTick = 7;
        builder.setName("huntercraft_boxer_hammer_strike");
        builder.fullyEnableParts();
        addLinear(builder.body.pitch, 0, degrees(-4.0F));
        addLinear(builder.body.pitch, 2, degrees(-14.0F));
        addLinear(builder.body.pitch, 5, degrees(-8.0F));
        addLinear(builder.rightArm.pitch, 0, degrees(-88.0F));
        addLinear(builder.rightArm.pitch, 2, degrees(-152.0F));
        addLinear(builder.rightArm.pitch, 5, degrees(-22.0F));
        addLinear(builder.rightArm.yaw, 0, degrees(0.0F));
        addLinear(builder.rightArm.yaw, 2, degrees(-12.0F));
        addLinear(builder.leftArm.pitch, 0, degrees(-32.0F));
        addLinear(builder.leftArm.pitch, 2, degrees(-68.0F));
        addLinear(builder.rightLeg.pitch, 0, degrees(12.0F));
        addLinear(builder.rightLeg.pitch, 2, degrees(-8.0F));
        addLinear(builder.leftLeg.pitch, 0, degrees(-8.0F));
        addLinear(builder.leftLeg.pitch, 2, degrees(14.0F));
        return builder.build();
    }

    private static KeyframeAnimation buildBoxerCounterGuardAnimation() {
        KeyframeAnimation.AnimationBuilder builder = new KeyframeAnimation.AnimationBuilder(AnimationFormat.JSON_MC_ANIM);
        builder.beginTick = 0;
        builder.endTick = 10;
        builder.stopTick = 10;
        builder.isLooped = true;
        builder.returnTick = 6;
        builder.setName("huntercraft_boxer_counter_guard");
        builder.fullyEnableParts();
        addLinear(builder.rightArm.pitch, 0, degrees(-106.0F));
        addLinear(builder.rightArm.pitch, 4, degrees(-122.0F));
        addLinear(builder.leftArm.pitch, 0, degrees(-100.0F));
        addLinear(builder.leftArm.pitch, 4, degrees(-116.0F));
        addLinear(builder.rightArm.yaw, 0, degrees(-8.0F));
        addLinear(builder.leftArm.yaw, 0, degrees(8.0F));
        return builder.build();
    }

    private static KeyframeAnimation buildBoxerCounterStrikeAnimation() {
        KeyframeAnimation.AnimationBuilder builder = new KeyframeAnimation.AnimationBuilder(AnimationFormat.JSON_MC_ANIM);
        builder.beginTick = 0;
        builder.endTick = 8;
        builder.stopTick = 8;
        builder.setName("huntercraft_boxer_counter_strike");
        builder.fullyEnableParts();
        addLinear(builder.body.yaw, 0, degrees(-12.0F));
        addLinear(builder.body.yaw, 4, degrees(18.0F));
        addLinear(builder.rightArm.pitch, 0, degrees(-84.0F));
        addLinear(builder.rightArm.pitch, 4, degrees(-12.0F));
        addLinear(builder.leftArm.pitch, 0, degrees(-42.0F));
        addLinear(builder.leftArm.pitch, 4, degrees(-120.0F));
        return builder.build();
    }

    private static KeyframeAnimation buildBoxerRedirectionAnimation() {
        KeyframeAnimation.AnimationBuilder builder = new KeyframeAnimation.AnimationBuilder(AnimationFormat.JSON_MC_ANIM);
        builder.beginTick = 0;
        builder.endTick = 12;
        builder.stopTick = 12;
        builder.setName("huntercraft_boxer_redirection");
        builder.fullyEnableParts();
        addLinear(builder.rightArm.pitch, 0, degrees(-92.0F));
        addLinear(builder.rightArm.pitch, 3, degrees(-18.0F));
        addLinear(builder.rightArm.pitch, 5, degrees(-92.0F));
        addLinear(builder.rightArm.pitch, 8, degrees(-150.0F));
        addLinear(builder.rightArm.pitch, 10, degrees(-18.0F));
        addLinear(builder.leftArm.pitch, 0, degrees(-48.0F));
        addLinear(builder.leftArm.pitch, 3, degrees(-108.0F));
        addLinear(builder.leftArm.pitch, 7, degrees(-118.0F));
        addLinear(builder.leftArm.pitch, 10, degrees(-32.0F));
        return builder.build();
    }

    private static KeyframeAnimation buildBoxerGrabLiftAnimation() {
        KeyframeAnimation.AnimationBuilder builder = new KeyframeAnimation.AnimationBuilder(AnimationFormat.JSON_MC_ANIM);
        builder.beginTick = 0;
        builder.endTick = 10;
        builder.stopTick = 10;
        builder.isLooped = true;
        builder.returnTick = 6;
        builder.setName("huntercraft_boxer_grab_lift");
        builder.fullyEnableParts();
        addLinear(builder.body.pitch, 0, degrees(-6.0F));
        addLinear(builder.body.pitch, 4, degrees(-14.0F));
        addLinear(builder.rightArm.pitch, 0, degrees(-78.0F));
        addLinear(builder.rightArm.pitch, 4, degrees(-138.0F));
        addLinear(builder.leftArm.pitch, 0, degrees(-54.0F));
        addLinear(builder.leftArm.pitch, 4, degrees(-126.0F));
        return builder.build();
    }

    private static KeyframeAnimation buildBoxerGrabSlamAnimation() {
        KeyframeAnimation.AnimationBuilder builder = new KeyframeAnimation.AnimationBuilder(AnimationFormat.JSON_MC_ANIM);
        builder.beginTick = 0;
        builder.endTick = 10;
        builder.stopTick = 10;
        builder.setName("huntercraft_boxer_grab_slam");
        builder.fullyEnableParts();
        addLinear(builder.body.pitch, 0, degrees(10.0F));
        addLinear(builder.body.pitch, 4, degrees(20.0F));
        addLinear(builder.rightArm.pitch, 0, degrees(-156.0F));
        addLinear(builder.rightArm.pitch, 4, degrees(-24.0F));
        addLinear(builder.leftArm.pitch, 0, degrees(-144.0F));
        addLinear(builder.leftArm.pitch, 4, degrees(-18.0F));
        addLinear(builder.rightLeg.pitch, 0, degrees(20.0F));
        addLinear(builder.leftLeg.pitch, 0, degrees(18.0F));
        return builder.build();
    }

    private static KeyframeAnimation buildMartialMeteorHeelAnimation() {
        KeyframeAnimation.AnimationBuilder builder = new KeyframeAnimation.AnimationBuilder(AnimationFormat.JSON_MC_ANIM);
        builder.beginTick = 0;
        builder.endTick = 14;
        builder.stopTick = 14;
        builder.setName("huntercraft_martial_meteor_heel");
        builder.fullyEnableParts();
        addLinear(builder.body.pitch, 0, degrees(-10.0F));
        addLinear(builder.body.pitch, 5, degrees(-34.0F));
        addLinear(builder.body.pitch, 9, degrees(24.0F));
        addLinear(builder.body.pitch, 14, degrees(36.0F));
        addLinear(builder.body.yaw, 0, degrees(0.0F));
        addLinear(builder.body.yaw, 5, degrees(12.0F));
        addLinear(builder.body.yaw, 14, degrees(-10.0F));

        addLinear(builder.head.pitch, 0, degrees(2.0F));
        addLinear(builder.head.pitch, 5, degrees(10.0F));
        addLinear(builder.head.pitch, 14, degrees(-8.0F));

        addLinear(builder.rightLeg.pitch, 0, degrees(22.0F));
        addLinear(builder.rightLeg.pitch, 5, degrees(96.0F));
        addLinear(builder.rightLeg.pitch, 9, degrees(-54.0F));
        addLinear(builder.rightLeg.pitch, 14, degrees(-86.0F));
        addLinear(builder.rightLeg.roll, 5, degrees(16.0F));
        addLinear(builder.rightLeg.roll, 14, degrees(5.0F));
        addLinear(builder.leftLeg.pitch, 0, degrees(-18.0F));
        addLinear(builder.leftLeg.pitch, 5, degrees(-64.0F));
        addLinear(builder.leftLeg.pitch, 9, degrees(18.0F));
        addLinear(builder.leftLeg.pitch, 14, degrees(34.0F));
        addLinear(builder.leftLeg.roll, 5, degrees(-12.0F));
        addLinear(builder.leftLeg.roll, 14, degrees(-8.0F));

        addLinear(builder.rightArm.pitch, 0, degrees(-42.0F));
        addLinear(builder.rightArm.pitch, 5, degrees(-128.0F));
        addLinear(builder.rightArm.pitch, 14, degrees(20.0F));
        addLinear(builder.rightArm.roll, 5, degrees(20.0F));
        addLinear(builder.rightArm.roll, 14, degrees(34.0F));
        addLinear(builder.leftArm.pitch, 0, degrees(-34.0F));
        addLinear(builder.leftArm.pitch, 5, degrees(-112.0F));
        addLinear(builder.leftArm.pitch, 14, degrees(14.0F));
        addLinear(builder.leftArm.roll, 5, degrees(-18.0F));
        addLinear(builder.leftArm.roll, 14, degrees(-30.0F));
        return builder.build();
    }

    private static KeyframeAnimation buildMartialMeteorHeelImpactAnimation() {
        KeyframeAnimation.AnimationBuilder builder = new KeyframeAnimation.AnimationBuilder(AnimationFormat.JSON_MC_ANIM);
        builder.beginTick = 0;
        builder.endTick = 8;
        builder.stopTick = 8;
        builder.setName("huntercraft_martial_meteor_heel_impact");
        builder.fullyEnableParts();
        addLinear(builder.body.pitch, 0, degrees(34.0F));
        addLinear(builder.body.pitch, 2, degrees(48.0F));
        addLinear(builder.body.pitch, 5, degrees(18.0F));
        addLinear(builder.body.yaw, 0, degrees(-8.0F));
        addLinear(builder.body.yaw, 3, degrees(8.0F));
        addLinear(builder.rightLeg.pitch, 0, degrees(-88.0F));
        addLinear(builder.rightLeg.pitch, 2, degrees(-104.0F));
        addLinear(builder.rightLeg.pitch, 5, degrees(-30.0F));
        addLinear(builder.rightLeg.roll, 0, degrees(6.0F));
        addLinear(builder.rightLeg.roll, 2, degrees(18.0F));
        addLinear(builder.leftLeg.pitch, 0, degrees(28.0F));
        addLinear(builder.leftLeg.pitch, 2, degrees(42.0F));
        addLinear(builder.leftLeg.pitch, 5, degrees(8.0F));
        addLinear(builder.leftLeg.roll, 0, degrees(-10.0F));
        addLinear(builder.leftLeg.roll, 2, degrees(-18.0F));
        addLinear(builder.rightArm.pitch, 0, degrees(10.0F));
        addLinear(builder.rightArm.pitch, 2, degrees(34.0F));
        addLinear(builder.rightArm.pitch, 5, degrees(-18.0F));
        addLinear(builder.rightArm.roll, 2, degrees(42.0F));
        addLinear(builder.leftArm.pitch, 0, degrees(6.0F));
        addLinear(builder.leftArm.pitch, 2, degrees(28.0F));
        addLinear(builder.leftArm.pitch, 5, degrees(-16.0F));
        addLinear(builder.leftArm.roll, 2, degrees(-38.0F));
        return builder.build();
    }

    private static KeyframeAnimation buildMartialAnkleSplitterAnimation() {
        KeyframeAnimation.AnimationBuilder builder = new KeyframeAnimation.AnimationBuilder(AnimationFormat.JSON_MC_ANIM);
        builder.beginTick = 0;
        builder.endTick = 10;
        builder.stopTick = 10;
        builder.setName("huntercraft_martial_ankle_splitter");
        builder.fullyEnableParts();
        addLinear(builder.body.pitch, 0, degrees(-8.0F));
        addLinear(builder.body.pitch, 3, degrees(28.0F));
        addLinear(builder.body.pitch, 7, degrees(18.0F));
        addLinear(builder.body.pitch, 10, degrees(-4.0F));
        addLinear(builder.body.yaw, 0, degrees(-4.0F));
        addLinear(builder.body.yaw, 3, degrees(-28.0F));
        addLinear(builder.body.yaw, 7, degrees(30.0F));
        addLinear(builder.body.yaw, 10, degrees(0.0F));

        addLinear(builder.head.pitch, 0, degrees(2.0F));
        addLinear(builder.head.pitch, 3, degrees(-10.0F));
        addLinear(builder.head.pitch, 7, degrees(-6.0F));
        addLinear(builder.head.pitch, 10, degrees(0.0F));

        addLinear(builder.rightLeg.pitch, 0, degrees(14.0F));
        addLinear(builder.rightLeg.pitch, 3, degrees(-82.0F));
        addLinear(builder.rightLeg.pitch, 7, degrees(-92.0F));
        addLinear(builder.rightLeg.pitch, 10, degrees(8.0F));
        addLinear(builder.rightLeg.roll, 0, degrees(0.0F));
        addLinear(builder.rightLeg.roll, 3, degrees(32.0F));
        addLinear(builder.rightLeg.roll, 7, degrees(42.0F));
        addLinear(builder.rightLeg.roll, 10, degrees(0.0F));
        addLinear(builder.leftLeg.pitch, 0, degrees(-8.0F));
        addLinear(builder.leftLeg.pitch, 3, degrees(58.0F));
        addLinear(builder.leftLeg.pitch, 7, degrees(44.0F));
        addLinear(builder.leftLeg.pitch, 10, degrees(-4.0F));
        addLinear(builder.leftLeg.roll, 0, degrees(0.0F));
        addLinear(builder.leftLeg.roll, 3, degrees(-24.0F));
        addLinear(builder.leftLeg.roll, 7, degrees(-18.0F));
        addLinear(builder.leftLeg.roll, 10, degrees(0.0F));

        addLinear(builder.rightArm.pitch, 0, degrees(-36.0F));
        addLinear(builder.rightArm.pitch, 3, degrees(24.0F));
        addLinear(builder.rightArm.pitch, 7, degrees(-18.0F));
        addLinear(builder.rightArm.pitch, 10, degrees(-32.0F));
        addLinear(builder.rightArm.roll, 3, degrees(46.0F));
        addLinear(builder.rightArm.roll, 7, degrees(22.0F));
        addLinear(builder.leftArm.pitch, 0, degrees(-30.0F));
        addLinear(builder.leftArm.pitch, 3, degrees(18.0F));
        addLinear(builder.leftArm.pitch, 7, degrees(-22.0F));
        addLinear(builder.leftArm.pitch, 10, degrees(-28.0F));
        addLinear(builder.leftArm.roll, 3, degrees(-42.0F));
        addLinear(builder.leftArm.roll, 7, degrees(-20.0F));
        return builder.build();
    }

    private static KeyframeAnimation buildMartialFaceJabAnimation() {
        KeyframeAnimation.AnimationBuilder builder = new KeyframeAnimation.AnimationBuilder(AnimationFormat.JSON_MC_ANIM);
        builder.beginTick = 0;
        builder.endTick = 6;
        builder.stopTick = 6;
        builder.setName("huntercraft_martial_face_jab");
        builder.fullyEnableParts();
        addLinear(builder.body.pitch, 0, degrees(-2.0F));
        addLinear(builder.body.pitch, 2, degrees(-10.0F));
        addLinear(builder.rightArm.pitch, 0, degrees(-58.0F));
        addLinear(builder.rightArm.pitch, 2, degrees(-8.0F));
        addLinear(builder.leftArm.pitch, 0, degrees(-36.0F));
        addLinear(builder.leftArm.pitch, 2, degrees(-94.0F));
        return builder.build();
    }

    private static KeyframeAnimation buildMartialWhirlwindArcAnimation() {
        KeyframeAnimation.AnimationBuilder builder = new KeyframeAnimation.AnimationBuilder(AnimationFormat.JSON_MC_ANIM);
        builder.beginTick = 0;
        builder.endTick = 12;
        builder.stopTick = 12;
        builder.isLooped = true;
        builder.returnTick = 0;
        builder.setName("huntercraft_martial_whirlwind_arc");
        builder.fullyEnableParts();
        addLinear(builder.body.pitch, 0, degrees(-10.0F));
        addLinear(builder.body.pitch, 6, degrees(-18.0F));
        addLinear(builder.body.pitch, 12, degrees(-10.0F));
        addLinear(builder.body.yaw, 0, degrees(-48.0F));
        addLinear(builder.body.yaw, 3, degrees(24.0F));
        addLinear(builder.body.yaw, 6, degrees(58.0F));
        addLinear(builder.body.yaw, 9, degrees(-26.0F));
        addLinear(builder.body.yaw, 12, degrees(-48.0F));
        addLinear(builder.rightLeg.pitch, 0, degrees(-22.0F));
        addLinear(builder.rightLeg.pitch, 3, degrees(48.0F));
        addLinear(builder.rightLeg.pitch, 6, degrees(-16.0F));
        addLinear(builder.rightLeg.pitch, 9, degrees(42.0F));
        addLinear(builder.rightLeg.pitch, 12, degrees(-22.0F));
        addLinear(builder.rightLeg.roll, 0, degrees(18.0F));
        addLinear(builder.rightLeg.roll, 6, degrees(56.0F));
        addLinear(builder.rightLeg.roll, 12, degrees(18.0F));
        addLinear(builder.leftLeg.pitch, 0, degrees(36.0F));
        addLinear(builder.leftLeg.pitch, 3, degrees(-24.0F));
        addLinear(builder.leftLeg.pitch, 6, degrees(40.0F));
        addLinear(builder.leftLeg.pitch, 9, degrees(-20.0F));
        addLinear(builder.leftLeg.pitch, 12, degrees(36.0F));
        addLinear(builder.rightArm.pitch, 0, degrees(-120.0F));
        addLinear(builder.rightArm.pitch, 6, degrees(-56.0F));
        addLinear(builder.rightArm.pitch, 12, degrees(-120.0F));
        addLinear(builder.leftArm.pitch, 0, degrees(-54.0F));
        addLinear(builder.leftArm.pitch, 6, degrees(-128.0F));
        addLinear(builder.leftArm.pitch, 12, degrees(-54.0F));
        return builder.build();
    }

    private static KeyframeAnimation buildMartialAirBarrageAnimation() {
        KeyframeAnimation.AnimationBuilder builder = new KeyframeAnimation.AnimationBuilder(AnimationFormat.JSON_MC_ANIM);
        builder.beginTick = 0;
        builder.endTick = 12;
        builder.stopTick = 12;
        builder.isLooped = true;
        builder.returnTick = 6;
        builder.setName("huntercraft_martial_air_barrage");
        builder.fullyEnableParts();
        addLinear(builder.body.pitch, 0, degrees(-12.0F));
        addLinear(builder.body.pitch, 6, degrees(-18.0F));
        addLinear(builder.rightArm.pitch, 0, degrees(-90.0F));
        addLinear(builder.rightArm.pitch, 2, degrees(-12.0F));
        addLinear(builder.rightArm.pitch, 4, degrees(-90.0F));
        addLinear(builder.rightArm.pitch, 6, degrees(-12.0F));
        addLinear(builder.leftArm.pitch, 0, degrees(-48.0F));
        addLinear(builder.leftArm.pitch, 2, degrees(-102.0F));
        addLinear(builder.leftArm.pitch, 4, degrees(-48.0F));
        addLinear(builder.leftArm.pitch, 6, degrees(-102.0F));
        return builder.build();
    }

    private static KeyframeAnimation buildMartialRisingShotChargeAnimation() {
        KeyframeAnimation.AnimationBuilder builder = new KeyframeAnimation.AnimationBuilder(AnimationFormat.JSON_MC_ANIM);
        builder.beginTick = 0;
        builder.endTick = 10;
        builder.stopTick = 10;
        builder.isLooped = true;
        builder.returnTick = 8;
        builder.setName("huntercraft_martial_rising_shot_charge");
        builder.fullyEnableParts();
        addLinear(builder.body.pitch, 0, degrees(-4.0F));
        addLinear(builder.body.pitch, 4, degrees(-16.0F));
        addLinear(builder.rightLeg.pitch, 0, degrees(8.0F));
        addLinear(builder.rightLeg.pitch, 4, degrees(-24.0F));
        addLinear(builder.leftLeg.pitch, 0, degrees(-12.0F));
        addLinear(builder.leftLeg.pitch, 4, degrees(22.0F));
        addLinear(builder.rightArm.pitch, 0, degrees(-52.0F));
        addLinear(builder.rightArm.pitch, 4, degrees(-126.0F));
        addLinear(builder.leftArm.pitch, 0, degrees(-32.0F));
        addLinear(builder.leftArm.pitch, 4, degrees(-88.0F));
        return builder.build();
    }

    private static KeyframeAnimation buildMartialRisingShotAnimation() {
        KeyframeAnimation.AnimationBuilder builder = new KeyframeAnimation.AnimationBuilder(AnimationFormat.JSON_MC_ANIM);
        builder.beginTick = 0;
        builder.endTick = 9;
        builder.stopTick = 9;
        builder.setName("huntercraft_martial_rising_shot");
        builder.fullyEnableParts();
        addLinear(builder.body.pitch, 0, degrees(-12.0F));
        addLinear(builder.body.pitch, 2, degrees(-26.0F));
        addLinear(builder.body.pitch, 5, degrees(24.0F));
        addLinear(builder.body.pitch, 9, degrees(4.0F));
        addLinear(builder.body.yaw, 0, degrees(-8.0F));
        addLinear(builder.body.yaw, 5, degrees(16.0F));
        addLinear(builder.body.yaw, 9, degrees(0.0F));
        addLinear(builder.rightLeg.pitch, 0, degrees(-22.0F));
        addLinear(builder.rightLeg.pitch, 2, degrees(-54.0F));
        addLinear(builder.rightLeg.pitch, 5, degrees(92.0F));
        addLinear(builder.rightLeg.pitch, 9, degrees(12.0F));
        addLinear(builder.rightLeg.roll, 2, degrees(8.0F));
        addLinear(builder.rightLeg.roll, 5, degrees(22.0F));
        addLinear(builder.leftLeg.pitch, 0, degrees(14.0F));
        addLinear(builder.leftLeg.pitch, 2, degrees(36.0F));
        addLinear(builder.leftLeg.pitch, 5, degrees(-44.0F));
        addLinear(builder.leftLeg.pitch, 9, degrees(0.0F));
        addLinear(builder.leftLeg.roll, 2, degrees(-8.0F));
        addLinear(builder.leftLeg.roll, 5, degrees(-16.0F));
        addLinear(builder.rightArm.pitch, 0, degrees(-92.0F));
        addLinear(builder.rightArm.pitch, 2, degrees(-128.0F));
        addLinear(builder.rightArm.pitch, 5, degrees(-34.0F));
        addLinear(builder.rightArm.pitch, 9, degrees(-44.0F));
        addLinear(builder.leftArm.pitch, 0, degrees(-70.0F));
        addLinear(builder.leftArm.pitch, 2, degrees(-112.0F));
        addLinear(builder.leftArm.pitch, 5, degrees(-132.0F));
        addLinear(builder.leftArm.pitch, 9, degrees(-48.0F));
        return builder.build();
    }

    private static KeyframeAnimation buildMartialToraHuntChargeAnimation() {
        KeyframeAnimation.AnimationBuilder builder = new KeyframeAnimation.AnimationBuilder(AnimationFormat.JSON_MC_ANIM);
        builder.beginTick = 0;
        builder.endTick = 15;
        builder.stopTick = 15;
        builder.isLooped = true;
        builder.returnTick = 12;
        builder.setName("huntercraft_martial_tora_hunt_charge");
        builder.fullyEnableParts();
        addLinear(builder.body.pitch, 0, degrees(-14.0F));
        addLinear(builder.body.pitch, 6, degrees(-31.0F));
        addLinear(builder.body.pitch, 15, degrees(-27.0F));
        addLinear(builder.body.yaw, 0, degrees(8.0F));
        addLinear(builder.body.yaw, 6, degrees(-32.0F));
        addLinear(builder.body.yaw, 15, degrees(-24.0F));
        addLinear(builder.head.pitch, 0, degrees(5.0F));
        addLinear(builder.head.pitch, 6, degrees(18.0F));
        addLinear(builder.head.pitch, 15, degrees(14.0F));
        addLinear(builder.rightLeg.pitch, 0, degrees(14.0F));
        addLinear(builder.rightLeg.pitch, 6, degrees(46.0F));
        addLinear(builder.rightLeg.pitch, 15, degrees(38.0F));
        addLinear(builder.leftLeg.pitch, 0, degrees(-16.0F));
        addLinear(builder.leftLeg.pitch, 6, degrees(-42.0F));
        addLinear(builder.leftLeg.pitch, 15, degrees(-34.0F));
        addLinear(builder.rightArm.pitch, 0, degrees(-58.0F));
        addLinear(builder.rightArm.pitch, 6, degrees(-148.0F));
        addLinear(builder.rightArm.pitch, 15, degrees(-132.0F));
        addLinear(builder.rightArm.yaw, 0, degrees(8.0F));
        addLinear(builder.rightArm.yaw, 6, degrees(-28.0F));
        addLinear(builder.leftArm.pitch, 0, degrees(-48.0F));
        addLinear(builder.leftArm.pitch, 6, degrees(-118.0F));
        addLinear(builder.leftArm.pitch, 15, degrees(-108.0F));
        addLinear(builder.leftArm.yaw, 0, degrees(-6.0F));
        addLinear(builder.leftArm.yaw, 6, degrees(24.0F));
        return builder.build();
    }

    private static KeyframeAnimation buildMartialToraHuntAnimation() {
        KeyframeAnimation.AnimationBuilder builder = new KeyframeAnimation.AnimationBuilder(AnimationFormat.JSON_MC_ANIM);
        builder.beginTick = 0;
        builder.endTick = 14;
        builder.stopTick = 14;
        builder.setName("huntercraft_martial_tora_hunt");
        builder.fullyEnableParts();
        addLinear(builder.body.pitch, 0, degrees(-34.0F));
        addLinear(builder.body.pitch, 4, degrees(-18.0F));
        addLinear(builder.body.pitch, 8, degrees(-25.0F));
        addLinear(builder.body.pitch, 14, degrees(-8.0F));
        addLinear(builder.body.yaw, 0, degrees(-38.0F));
        addLinear(builder.body.yaw, 4, degrees(34.0F));
        addLinear(builder.body.yaw, 8, degrees(-18.0F));
        addLinear(builder.body.yaw, 14, degrees(0.0F));
        addLinear(builder.head.pitch, 0, degrees(16.0F));
        addLinear(builder.head.pitch, 4, degrees(-4.0F));
        addLinear(builder.head.pitch, 14, degrees(0.0F));
        addLinear(builder.rightArm.pitch, 0, degrees(-154.0F));
        addLinear(builder.rightArm.pitch, 4, degrees(-24.0F));
        addLinear(builder.rightArm.pitch, 8, degrees(-110.0F));
        addLinear(builder.rightArm.pitch, 14, degrees(-42.0F));
        addLinear(builder.rightArm.yaw, 0, degrees(-34.0F));
        addLinear(builder.rightArm.yaw, 4, degrees(18.0F));
        addLinear(builder.rightArm.yaw, 8, degrees(-26.0F));
        addLinear(builder.leftArm.pitch, 0, degrees(-106.0F));
        addLinear(builder.leftArm.pitch, 4, degrees(-142.0F));
        addLinear(builder.leftArm.pitch, 8, degrees(-20.0F));
        addLinear(builder.leftArm.pitch, 14, degrees(-36.0F));
        addLinear(builder.leftArm.yaw, 0, degrees(24.0F));
        addLinear(builder.leftArm.yaw, 8, degrees(-18.0F));
        addLinear(builder.rightLeg.pitch, 0, degrees(48.0F));
        addLinear(builder.rightLeg.pitch, 4, degrees(-8.0F));
        addLinear(builder.rightLeg.pitch, 14, degrees(6.0F));
        addLinear(builder.rightLeg.roll, 0, degrees(18.0F));
        addLinear(builder.rightLeg.roll, 4, degrees(46.0F));
        addLinear(builder.rightLeg.roll, 14, degrees(0.0F));
        addLinear(builder.leftLeg.pitch, 0, degrees(-38.0F));
        addLinear(builder.leftLeg.pitch, 4, degrees(26.0F));
        addLinear(builder.leftLeg.pitch, 14, degrees(-4.0F));
        return builder.build();
    }

    private static KeyframeAnimation buildLionFangDrawChargeAnimation() {
        KeyframeAnimation.AnimationBuilder builder = new KeyframeAnimation.AnimationBuilder(AnimationFormat.JSON_MC_ANIM);
        builder.beginTick = 0;
        builder.endTick = 15;
        builder.stopTick = 15;
        builder.isLooped = true;
        builder.returnTick = 12;
        builder.setName("huntercraft_lion_fang_draw_charge");
        builder.fullyEnableParts();

        addLinear(builder.body.pitch, 0, degrees(0.0F));
        addLinear(builder.body.pitch, 4, degrees(-18.0F));
        addLinear(builder.body.pitch, 15, degrees(-18.0F));

        addLinear(builder.head.pitch, 0, degrees(0.0F));
        addLinear(builder.head.pitch, 4, degrees(12.0F));
        addLinear(builder.head.pitch, 15, degrees(12.0F));

        addLinear(builder.rightArm.pitch, 0, degrees(0.0F));
        addLinear(builder.rightArm.pitch, 4, degrees(-138.0F));
        addLinear(builder.rightArm.pitch, 15, degrees(-138.0F));
        addLinear(builder.rightArm.yaw, 0, degrees(0.0F));
        addLinear(builder.rightArm.yaw, 4, degrees(-22.0F));
        addLinear(builder.rightArm.yaw, 15, degrees(-22.0F));

        addLinear(builder.leftArm.pitch, 0, degrees(0.0F));
        addLinear(builder.leftArm.pitch, 4, degrees(-86.0F));
        addLinear(builder.leftArm.pitch, 15, degrees(-86.0F));
        addLinear(builder.leftArm.yaw, 0, degrees(0.0F));
        addLinear(builder.leftArm.yaw, 4, degrees(18.0F));
        addLinear(builder.leftArm.yaw, 15, degrees(18.0F));

        addLinear(builder.leftLeg.pitch, 0, degrees(0.0F));
        addLinear(builder.leftLeg.pitch, 4, degrees(-18.0F));
        addLinear(builder.leftLeg.pitch, 15, degrees(-18.0F));

        addLinear(builder.rightLeg.pitch, 0, degrees(0.0F));
        addLinear(builder.rightLeg.pitch, 4, degrees(24.0F));
        addLinear(builder.rightLeg.pitch, 15, degrees(24.0F));

        return builder.build();
    }

    private static KeyframeAnimation buildVoidRendAnimation() {
        KeyframeAnimation.AnimationBuilder builder = new KeyframeAnimation.AnimationBuilder(AnimationFormat.JSON_MC_ANIM);
        builder.beginTick = 0;
        builder.endTick = 8;
        builder.stopTick = 8;
        builder.setName("huntercraft_void_rend");
        builder.fullyEnableParts();

        addLinear(builder.body.yaw, 0, degrees(-18.0F));
        addLinear(builder.body.yaw, 4, degrees(16.0F));
        addLinear(builder.body.yaw, 8, degrees(4.0F));
        addLinear(builder.body.pitch, 0, degrees(-8.0F));
        addLinear(builder.body.pitch, 4, degrees(-18.0F));
        addLinear(builder.body.pitch, 8, degrees(-6.0F));

        addLinear(builder.head.yaw, 0, degrees(10.0F));
        addLinear(builder.head.yaw, 4, degrees(-12.0F));
        addLinear(builder.head.yaw, 8, degrees(0.0F));
        addLinear(builder.head.pitch, 0, degrees(4.0F));
        addLinear(builder.head.pitch, 4, degrees(10.0F));
        addLinear(builder.head.pitch, 8, degrees(2.0F));

        addLinear(builder.rightArm.pitch, 0, degrees(-130.0F));
        addLinear(builder.rightArm.pitch, 4, degrees(-38.0F));
        addLinear(builder.rightArm.pitch, 8, degrees(-108.0F));
        addLinear(builder.rightArm.yaw, 0, degrees(-48.0F));
        addLinear(builder.rightArm.yaw, 4, degrees(24.0F));
        addLinear(builder.rightArm.yaw, 8, degrees(-18.0F));
        addLinear(builder.rightArm.roll, 0, degrees(72.0F));
        addLinear(builder.rightArm.roll, 4, degrees(18.0F));
        addLinear(builder.rightArm.roll, 8, degrees(58.0F));

        addLinear(builder.leftArm.pitch, 0, degrees(-70.0F));
        addLinear(builder.leftArm.pitch, 4, degrees(-126.0F));
        addLinear(builder.leftArm.pitch, 8, degrees(-84.0F));
        addLinear(builder.leftArm.yaw, 0, degrees(26.0F));
        addLinear(builder.leftArm.yaw, 4, degrees(-42.0F));
        addLinear(builder.leftArm.yaw, 8, degrees(14.0F));
        addLinear(builder.leftArm.roll, 0, degrees(-26.0F));
        addLinear(builder.leftArm.roll, 4, degrees(-68.0F));
        addLinear(builder.leftArm.roll, 8, degrees(-22.0F));

        addLinear(builder.rightLeg.pitch, 0, degrees(22.0F));
        addLinear(builder.rightLeg.pitch, 4, degrees(-12.0F));
        addLinear(builder.rightLeg.pitch, 8, degrees(12.0F));
        addLinear(builder.leftLeg.pitch, 0, degrees(-12.0F));
        addLinear(builder.leftLeg.pitch, 4, degrees(24.0F));
        addLinear(builder.leftLeg.pitch, 8, degrees(-8.0F));

        return builder.build();
    }

    private static KeyframeAnimation buildVoidRendChargeAnimation() {
        KeyframeAnimation.AnimationBuilder builder = new KeyframeAnimation.AnimationBuilder(AnimationFormat.JSON_MC_ANIM);
        builder.beginTick = 0;
        builder.endTick = 20;
        builder.stopTick = 20;
        builder.isLooped = true;
        builder.returnTick = 16;
        builder.setName("huntercraft_void_rend_charge");
        builder.fullyEnableParts();

        addLinear(builder.body.yaw, 0, degrees(-22.0F));
        addLinear(builder.body.yaw, 6, degrees(-36.0F));
        addLinear(builder.body.yaw, 20, degrees(-36.0F));
        addLinear(builder.body.pitch, 0, degrees(-6.0F));
        addLinear(builder.body.pitch, 6, degrees(-16.0F));
        addLinear(builder.body.pitch, 20, degrees(-16.0F));

        addLinear(builder.head.yaw, 0, degrees(18.0F));
        addLinear(builder.head.yaw, 6, degrees(28.0F));
        addLinear(builder.head.yaw, 20, degrees(28.0F));
        addLinear(builder.head.pitch, 0, degrees(4.0F));
        addLinear(builder.head.pitch, 6, degrees(10.0F));
        addLinear(builder.head.pitch, 20, degrees(10.0F));

        addLinear(builder.rightArm.pitch, 0, degrees(-120.0F));
        addLinear(builder.rightArm.pitch, 6, degrees(-152.0F));
        addLinear(builder.rightArm.pitch, 20, degrees(-152.0F));
        addLinear(builder.rightArm.yaw, 0, degrees(-28.0F));
        addLinear(builder.rightArm.yaw, 6, degrees(-52.0F));
        addLinear(builder.rightArm.yaw, 20, degrees(-52.0F));
        addLinear(builder.rightArm.roll, 0, degrees(44.0F));
        addLinear(builder.rightArm.roll, 6, degrees(72.0F));
        addLinear(builder.rightArm.roll, 20, degrees(72.0F));

        addLinear(builder.leftArm.pitch, 0, degrees(-76.0F));
        addLinear(builder.leftArm.pitch, 6, degrees(-118.0F));
        addLinear(builder.leftArm.pitch, 20, degrees(-118.0F));
        addLinear(builder.leftArm.yaw, 0, degrees(24.0F));
        addLinear(builder.leftArm.yaw, 6, degrees(38.0F));
        addLinear(builder.leftArm.yaw, 20, degrees(38.0F));
        addLinear(builder.leftArm.roll, 0, degrees(-26.0F));
        addLinear(builder.leftArm.roll, 6, degrees(-52.0F));
        addLinear(builder.leftArm.roll, 20, degrees(-52.0F));

        addLinear(builder.rightLeg.pitch, 0, degrees(18.0F));
        addLinear(builder.rightLeg.pitch, 6, degrees(28.0F));
        addLinear(builder.rightLeg.pitch, 20, degrees(28.0F));
        addLinear(builder.leftLeg.pitch, 0, degrees(-10.0F));
        addLinear(builder.leftLeg.pitch, 6, degrees(-18.0F));
        addLinear(builder.leftLeg.pitch, 20, degrees(-18.0F));

        return builder.build();
    }

    private static KeyframeAnimation buildGuardAnimation() {
        KeyframeAnimation.AnimationBuilder builder = new KeyframeAnimation.AnimationBuilder(AnimationFormat.JSON_MC_ANIM);
        builder.beginTick = 0;
        builder.endTick = 8;
        builder.stopTick = 8;
        builder.isLooped = true;
        builder.returnTick = 6;
        builder.setName("huntercraft_guard");
        builder.fullyEnableParts();

        addLinear(builder.body.pitch, 0, 0.0F);
        addLinear(builder.body.pitch, 4, degrees(-13.0F));
        addLinear(builder.body.pitch, 8, degrees(-11.0F));
        addLinear(builder.head.pitch, 0, 0.0F);
        addLinear(builder.head.pitch, 4, degrees(6.0F));
        addLinear(builder.head.pitch, 8, degrees(4.0F));

        addLinear(builder.leftArm.pitch, 0, 0.0F);
        addLinear(builder.leftArm.pitch, 4, degrees(-138.0F));
        addLinear(builder.leftArm.pitch, 8, degrees(-134.0F));
        addLinear(builder.leftArm.yaw, 0, 0.0F);
        addLinear(builder.leftArm.yaw, 4, degrees(34.0F));
        addLinear(builder.leftArm.yaw, 8, degrees(30.0F));
        addLinear(builder.leftArm.roll, 0, 0.0F);
        addLinear(builder.leftArm.roll, 4, degrees(-64.0F));
        addLinear(builder.leftArm.roll, 8, degrees(-58.0F));

        addLinear(builder.rightArm.pitch, 0, 0.0F);
        addLinear(builder.rightArm.pitch, 4, degrees(-138.0F));
        addLinear(builder.rightArm.pitch, 8, degrees(-134.0F));
        addLinear(builder.rightArm.yaw, 0, 0.0F);
        addLinear(builder.rightArm.yaw, 4, degrees(-34.0F));
        addLinear(builder.rightArm.yaw, 8, degrees(-30.0F));
        addLinear(builder.rightArm.roll, 0, 0.0F);
        addLinear(builder.rightArm.roll, 4, degrees(64.0F));
        addLinear(builder.rightArm.roll, 8, degrees(58.0F));

        addLinear(builder.leftLeg.pitch, 0, 0.0F);
        addLinear(builder.leftLeg.pitch, 4, degrees(-8.0F));
        addLinear(builder.leftLeg.pitch, 8, degrees(-7.0F));
        addLinear(builder.leftLeg.roll, 0, 0.0F);
        addLinear(builder.leftLeg.roll, 4, degrees(-4.0F));
        addLinear(builder.rightLeg.pitch, 0, 0.0F);
        addLinear(builder.rightLeg.pitch, 4, degrees(12.0F));
        addLinear(builder.rightLeg.pitch, 8, degrees(10.0F));
        addLinear(builder.rightLeg.roll, 0, 0.0F);
        addLinear(builder.rightLeg.roll, 4, degrees(4.0F));

        return builder.build();
    }

    private static KeyframeAnimation buildParryAnimation() {
        KeyframeAnimation.AnimationBuilder builder = new KeyframeAnimation.AnimationBuilder(AnimationFormat.JSON_MC_ANIM);
        builder.beginTick = 0;
        builder.endTick = 5;
        builder.stopTick = 5;
        builder.setName("huntercraft_parry");
        builder.fullyEnableParts();

        addLinear(builder.body.pitch, 0, degrees(3.0F));
        addLinear(builder.body.pitch, 2, degrees(-4.0F));
        addLinear(builder.body.pitch, 5, degrees(3.0F));

        addLinear(builder.leftArm.pitch, 0, degrees(-118.0F));
        addLinear(builder.leftArm.pitch, 2, degrees(-145.0F));
        addLinear(builder.leftArm.pitch, 5, degrees(-118.0F));
        addLinear(builder.leftArm.yaw, 0, degrees(-8.0F));
        addLinear(builder.leftArm.yaw, 2, degrees(-24.0F));
        addLinear(builder.leftArm.yaw, 5, degrees(-8.0F));

        addLinear(builder.rightArm.pitch, 0, degrees(-118.0F));
        addLinear(builder.rightArm.pitch, 2, degrees(-145.0F));
        addLinear(builder.rightArm.pitch, 5, degrees(-118.0F));
        addLinear(builder.rightArm.yaw, 0, degrees(8.0F));
        addLinear(builder.rightArm.yaw, 2, degrees(24.0F));
        addLinear(builder.rightArm.yaw, 5, degrees(8.0F));

        return builder.build();
    }

    private static KeyframeAnimation buildWeaponParryAnimation() {
        KeyframeAnimation.AnimationBuilder builder = new KeyframeAnimation.AnimationBuilder(AnimationFormat.JSON_MC_ANIM);
        builder.beginTick = 0;
        builder.endTick = 5;
        builder.stopTick = 5;
        builder.setName("huntercraft_weapon_parry");
        builder.fullyEnableParts();

        addLinear(builder.body.pitch, 0, degrees(3.0F));
        addLinear(builder.body.pitch, 2, degrees(-6.0F));
        addLinear(builder.body.pitch, 5, degrees(3.0F));
        addLinear(builder.body.yaw, 0, degrees(-4.0F));
        addLinear(builder.body.yaw, 2, degrees(-14.0F));
        addLinear(builder.body.yaw, 5, degrees(-4.0F));

        addLinear(builder.rightArm.pitch, 0, degrees(-155.0F));
        addLinear(builder.rightArm.pitch, 2, degrees(-175.0F));
        addLinear(builder.rightArm.pitch, 5, degrees(-155.0F));
        addLinear(builder.rightArm.yaw, 0, degrees(-8.0F));
        addLinear(builder.rightArm.yaw, 2, degrees(-24.0F));
        addLinear(builder.rightArm.yaw, 5, degrees(-8.0F));

        addLinear(builder.leftArm.pitch, 0, degrees(-100.0F));
        addLinear(builder.leftArm.pitch, 2, degrees(-125.0F));
        addLinear(builder.leftArm.pitch, 5, degrees(-100.0F));
        addLinear(builder.leftArm.roll, 0, degrees(-28.0F));
        addLinear(builder.leftArm.roll, 2, degrees(-40.0F));
        addLinear(builder.leftArm.roll, 5, degrees(-28.0F));

        return builder.build();
    }

    private static void addLinear(KeyframeAnimation.StateCollection.State state, int tick, float value) {
        state.addKeyFrame(tick, value, Ease.LINEAR);
    }

    private static float degrees(float degrees) {
        return (float) Math.toRadians(degrees);
    }

    private static KeyframeAnimation buildElasticReflectAnimation() {
        KeyframeAnimation.AnimationBuilder builder = new KeyframeAnimation.AnimationBuilder(AnimationFormat.JSON_MC_ANIM);
        builder.beginTick = 0;
        builder.endTick = 10;
        builder.stopTick = 10;
        builder.isLooped = true;
        builder.returnTick = 8;
        builder.setName("huntercraft_elastic_reflect");
        builder.fullyEnableParts();

        addLinear(builder.body.pitch, 0, degrees(-8.0F));
        addLinear(builder.body.pitch, 5, degrees(-13.0F));
        addLinear(builder.body.pitch, 10, degrees(-8.0F));
        addLinear(builder.body.yaw, 0, degrees(-10.0F));
        addLinear(builder.body.yaw, 5, degrees(-14.0F));
        addLinear(builder.body.yaw, 10, degrees(-10.0F));

        // Left arm: lifted high to drag the gum veil upward.
        addLinear(builder.leftArm.pitch, 0, degrees(-160.0F));
        addLinear(builder.leftArm.pitch, 5, degrees(-168.0F));
        addLinear(builder.leftArm.pitch, 10, degrees(-160.0F));
        addLinear(builder.leftArm.yaw, 0, degrees(16.0F));
        addLinear(builder.leftArm.yaw, 10, degrees(16.0F));
        addLinear(builder.leftArm.roll, 0, degrees(-42.0F));
        addLinear(builder.leftArm.roll, 10, degrees(-42.0F));

        // Right arm: braced low across the front like the gum is being stretched.
        addLinear(builder.rightArm.pitch, 0, degrees(-68.0F));
        addLinear(builder.rightArm.pitch, 5, degrees(-74.0F));
        addLinear(builder.rightArm.pitch, 10, degrees(-68.0F));
        addLinear(builder.rightArm.yaw, 0, degrees(-34.0F));
        addLinear(builder.rightArm.yaw, 10, degrees(-34.0F));
        addLinear(builder.rightArm.roll, 0, degrees(36.0F));
        addLinear(builder.rightArm.roll, 10, degrees(36.0F));

        return builder.build();
    }

    private static KeyframeAnimation buildDowsingChainSwingAnimation() {
        KeyframeAnimation.AnimationBuilder builder = new KeyframeAnimation.AnimationBuilder(AnimationFormat.JSON_MC_ANIM);
        builder.beginTick = 0;
        builder.endTick = 12;
        builder.stopTick = 12;
        builder.isLooped = true;
        builder.returnTick = 10;
        builder.setName("huntercraft_dowsing_chain_swing");
        builder.fullyEnableParts();

        // Body leans slightly forward and rotates with the swing
        addLinear(builder.body.pitch, 0, degrees(-8.0F));
        addLinear(builder.body.pitch, 6, degrees(-12.0F));
        addLinear(builder.body.pitch, 12, degrees(-8.0F));
        addLinear(builder.body.yaw, 0, degrees(0.0F));
        addLinear(builder.body.yaw, 6, degrees(15.0F));
        addLinear(builder.body.yaw, 12, degrees(0.0F));

        // Right arm: extended outward swinging the chain
        addLinear(builder.rightArm.pitch, 0, degrees(-80.0F));
        addLinear(builder.rightArm.pitch, 6, degrees(-90.0F));
        addLinear(builder.rightArm.pitch, 12, degrees(-80.0F));
        addLinear(builder.rightArm.yaw, 0, degrees(-20.0F));
        addLinear(builder.rightArm.yaw, 6, degrees(20.0F));
        addLinear(builder.rightArm.yaw, 12, degrees(-20.0F));
        addLinear(builder.rightArm.roll, 0, degrees(30.0F));
        addLinear(builder.rightArm.roll, 6, degrees(50.0F));
        addLinear(builder.rightArm.roll, 12, degrees(30.0F));

        // Left arm: counterbalance
        addLinear(builder.leftArm.pitch, 0, degrees(-40.0F));
        addLinear(builder.leftArm.pitch, 6, degrees(-50.0F));
        addLinear(builder.leftArm.pitch, 12, degrees(-40.0F));
        addLinear(builder.leftArm.yaw, 0, degrees(10.0F));
        addLinear(builder.leftArm.yaw, 6, degrees(-10.0F));
        addLinear(builder.leftArm.yaw, 12, degrees(10.0F));
        addLinear(builder.leftArm.roll, 0, degrees(-20.0F));
        addLinear(builder.leftArm.roll, 6, degrees(-30.0F));
        addLinear(builder.leftArm.roll, 12, degrees(-20.0F));

        return builder.build();
    }
}
