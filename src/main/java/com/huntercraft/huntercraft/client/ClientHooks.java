package com.huntercraft.huntercraft.client;

import com.huntercraft.huntercraft.ability.HunterAbility;
import com.huntercraft.huntercraft.HunterConfig;
import com.huntercraft.huntercraft.abilities.HunterAbilities;
import com.huntercraft.huntercraft.abilities.SkillTreeCombatAbility;
import com.huntercraft.huntercraft.animation.AnimationType;
import com.huntercraft.huntercraft.data.HunterPlayerData;
import com.huntercraft.huntercraft.data.HunterPlayerDataProvider;
import com.huntercraft.huntercraft.effect.HunterMobEffects;
import com.huntercraft.huntercraft.item.HunterItems;
import com.huntercraft.huntercraft.network.HunterNetwork;
import com.huntercraft.huntercraft.network.packet.AdjustSkillPointsPacket;
import com.huntercraft.huntercraft.network.packet.ChainJailMinigameInputPacket;
import com.huntercraft.huntercraft.network.packet.SwitchCombatBarPacket;
import com.huntercraft.huntercraft.network.packet.ToggleCombatBarPacket;
import com.huntercraft.huntercraft.network.packet.MeditationPromptInputPacket;
import com.huntercraft.huntercraft.network.packet.UseCombatAbilityPacket;
import com.huntercraft.huntercraft.network.packet.UseTechniquePacket;
import com.huntercraft.huntercraft.quest.NenQuestStage;
import com.huntercraft.huntercraft.progression.SkillNode;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderNameTagEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import com.mojang.math.Axis;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public final class ClientHooks {
    public static final KeyMapping OPEN_MENU = new KeyMapping("key.huntercraft.open_menu", GLFW.GLFW_KEY_M, "key.categories.huntercraft");
    public static final KeyMapping TOGGLE_COMBAT_BAR = new KeyMapping("key.huntercraft.toggle_combat_bar", GLFW.GLFW_KEY_LEFT_ALT, "key.categories.huntercraft");
    public static final KeyMapping SWITCH_COMBAT_BAR = new KeyMapping("key.huntercraft.switch_combat_bar", GLFW.GLFW_KEY_R, "key.categories.huntercraft");
    public static final KeyMapping DASH = new KeyMapping("key.huntercraft.dash", GLFW.GLFW_KEY_I, "key.categories.huntercraft");
    public static final KeyMapping GUARD = new KeyMapping("key.huntercraft.guard", GLFW.GLFW_KEY_U, "key.categories.huntercraft");
    public static final KeyMapping[] COMBAT_KEYS = new KeyMapping[] {
            new KeyMapping("key.huntercraft.slot_1", GLFW.GLFW_KEY_1, "key.categories.huntercraft"),
            new KeyMapping("key.huntercraft.slot_2", GLFW.GLFW_KEY_2, "key.categories.huntercraft"),
            new KeyMapping("key.huntercraft.slot_3", GLFW.GLFW_KEY_3, "key.categories.huntercraft"),
            new KeyMapping("key.huntercraft.slot_4", GLFW.GLFW_KEY_4, "key.categories.huntercraft"),
            new KeyMapping("key.huntercraft.slot_5", GLFW.GLFW_KEY_5, "key.categories.huntercraft"),
            new KeyMapping("key.huntercraft.slot_6", GLFW.GLFW_KEY_6, "key.categories.huntercraft"),
            new KeyMapping("key.huntercraft.slot_7", GLFW.GLFW_KEY_7, "key.categories.huntercraft"),
            new KeyMapping("key.huntercraft.slot_8", GLFW.GLFW_KEY_8, "key.categories.huntercraft")
    };
    public static final KeyMapping[] COMBAT_BAR_2_KEYS = new KeyMapping[] {
            new KeyMapping("key.huntercraft.bar2_slot_1", GLFW.GLFW_KEY_UNKNOWN, "key.categories.huntercraft"),
            new KeyMapping("key.huntercraft.bar2_slot_2", GLFW.GLFW_KEY_UNKNOWN, "key.categories.huntercraft"),
            new KeyMapping("key.huntercraft.bar2_slot_3", GLFW.GLFW_KEY_UNKNOWN, "key.categories.huntercraft"),
            new KeyMapping("key.huntercraft.bar2_slot_4", GLFW.GLFW_KEY_UNKNOWN, "key.categories.huntercraft"),
            new KeyMapping("key.huntercraft.bar2_slot_5", GLFW.GLFW_KEY_UNKNOWN, "key.categories.huntercraft"),
            new KeyMapping("key.huntercraft.bar2_slot_6", GLFW.GLFW_KEY_UNKNOWN, "key.categories.huntercraft"),
            new KeyMapping("key.huntercraft.bar2_slot_7", GLFW.GLFW_KEY_UNKNOWN, "key.categories.huntercraft"),
            new KeyMapping("key.huntercraft.bar2_slot_8", GLFW.GLFW_KEY_UNKNOWN, "key.categories.huntercraft")
    };

    private static boolean previousJumpDown;
    private static int lastJumpPressedTick = -100;
    private static boolean previousCombatBarToggle;
    private static int cachedHotbarSlot;
    private static int protectedHotbarSlot;
    private static boolean previousMeditationUp;
    private static boolean previousMeditationLeft;
    private static boolean previousMeditationDown;
    private static boolean previousMeditationRight;
    private static final boolean[] previousCombatKeyDown = new boolean[COMBAT_KEYS.length];
    private static final boolean[] previousCombatBar2KeyDown = new boolean[COMBAT_BAR_2_KEYS.length];

    private ClientHooks() {
    }

    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(OPEN_MENU);
        event.register(TOGGLE_COMBAT_BAR);
        event.register(SWITCH_COMBAT_BAR);
        event.register(DASH);
        event.register(GUARD);
        for (KeyMapping combatKey : COMBAT_KEYS) {
            event.register(combatKey);
        }
        for (KeyMapping combatKey : COMBAT_BAR_2_KEYS) {
            event.register(combatKey);
        }
    }

    public static void registerOverlays(RegisterGuiOverlaysEvent event) {
        event.registerBelow(net.minecraftforge.client.gui.overlay.VanillaGuiOverlay.CHAT_PANEL.id(), "hunter_status", HunterHudOverlay.INSTANCE);
    }

    @SubscribeEvent
    public static void onRenderNameTag(RenderNameTagEvent event) {
        if (event.getEntity() instanceof LivingEntity living && living.hasEffect(HunterMobEffects.ZETSU.get())) {
            event.setResult(Event.Result.DENY);
        }
    }

    @SubscribeEvent
    public static void onRenderPlayer(RenderPlayerEvent.Pre event) {
        event.getEntity().getCapability(HunterPlayerDataProvider.CAPABILITY).ifPresent(data -> {
            if (data.isChargingAbility("ghost_step")) {
                event.setCanceled(true);
            }
        });
    }

    @SubscribeEvent
    public static void onInteractionKeyMapping(InputEvent.InteractionKeyMappingTriggered event) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || !event.isAttack()) {
            return;
        }
        if (isStunnedForInput(player)) {
            event.setCanceled(true);
            event.setSwingHand(false);
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null) {
            return;
        }

        if (event.phase == TickEvent.Phase.START) {
            if (!isAnyCombatBindDown()) {
                cachedHotbarSlot = player.getInventory().selected;
                protectedHotbarSlot = cachedHotbarSlot;
            } else if (protectedHotbarSlot >= 0) {
                cachedHotbarSlot = protectedHotbarSlot;
            }
            return;
        }

        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        player.getCapability(HunterPlayerDataProvider.CAPABILITY).ifPresent(data -> {
            data.tickCooldowns();
            if (player.onGround()) {
                data.setAirJumpsUsed(0);
            }
            if (data.isGuarding()) {
                data.tickGuard();
            }
            data.tickVisualAnimations();
        });
        if (minecraft.level != null) {
            minecraft.level.players().forEach(otherPlayer -> {
                if (otherPlayer == player) {
                    return;
                }
                otherPlayer.getCapability(HunterPlayerDataProvider.CAPABILITY).ifPresent(HunterPlayerData::tickVisualAnimations);
            });
        }

        while (OPEN_MENU.consumeClick()) {
            minecraft.setScreen(new HunterMenuScreen());
        }

        boolean combatBarToggleDown = TOGGLE_COMBAT_BAR.isDown();
        if (combatBarToggleDown && !previousCombatBarToggle) {
            player.getCapability(HunterPlayerDataProvider.CAPABILITY).ifPresent(data -> {
                boolean nextVisible = !data.isCombatBarVisible();
                data.setCombatBarVisible(nextVisible);
                HunterNetwork.sendToServer(new ToggleCombatBarPacket(nextVisible));
            });
        }
        previousCombatBarToggle = combatBarToggleDown;

        while (SWITCH_COMBAT_BAR.consumeClick()) {
            if (!hasTwoCombatBarsOnScreen()) {
                player.getCapability(HunterPlayerDataProvider.CAPABILITY).ifPresent(data -> {
                    data.cycleActiveCombatBar();
                    HunterNetwork.sendToServer(new SwitchCombatBarPacket(data.getActiveCombatBar()));
                });
            }
        }

        player.getCapability(HunterPlayerDataProvider.CAPABILITY).ifPresent(data -> {
            boolean upDown = minecraft.options.keyUp.isDown();
            boolean leftDown = minecraft.options.keyLeft.isDown();
            boolean downDown = minecraft.options.keyDown.isDown();
            boolean rightDown = minecraft.options.keyRight.isDown();
            boolean meditationPrompting = data.getNenQuestStage() == NenQuestStage.FEEL_THE_AURA && data.isMeditationActive();
            if (meditationPrompting) {
                if (upDown && !previousMeditationUp) {
                    HunterNetwork.sendToServer(new MeditationPromptInputPacket("W"));
                }
                if (leftDown && !previousMeditationLeft) {
                    HunterNetwork.sendToServer(new MeditationPromptInputPacket("A"));
                }
                if (downDown && !previousMeditationDown) {
                    HunterNetwork.sendToServer(new MeditationPromptInputPacket("S"));
                }
                if (rightDown && !previousMeditationRight) {
                    HunterNetwork.sendToServer(new MeditationPromptInputPacket("D"));
                }
            }
            previousMeditationUp = upDown;
            previousMeditationLeft = leftDown;
            previousMeditationDown = downDown;
            previousMeditationRight = rightDown;
        });

        if (player.isSpectator()) {
            previousJumpDown = minecraft.options.keyJump.isDown();
            return;
        }

        boolean jumpDown = minecraft.options.keyJump.isDown();
        if (player.hasEffect(HunterMobEffects.CHAIN_JAIL.get()) && jumpDown && !previousJumpDown) {
            HunterNetwork.sendToServer(new ChainJailMinigameInputPacket(player.tickCount));
        }

        if (isStunnedForInput(player)) {
            previousJumpDown = jumpDown;
            return;
        }

        boolean combatBarVisible = player.getCapability(HunterPlayerDataProvider.CAPABILITY)
                .map(HunterPlayerData::isCombatBarVisible)
                .orElse(false);

        while (DASH.consumeClick()) {
            if (combatBarVisible) {
                HunterNetwork.sendToServer(new UseTechniquePacket(UseTechniquePacket.Technique.DASH, computeDashDirection(player)));
            }
        }

        while (GUARD.consumeClick()) {
            if (combatBarVisible) {
                HunterNetwork.sendToServer(new UseTechniquePacket(UseTechniquePacket.Technique.GUARD_TOGGLE, Vec3.ZERO));
            }
        }

        if (jumpDown && !previousJumpDown) {
            int currentTick = player.tickCount;
            if (combatBarVisible && !player.onGround() && currentTick - lastJumpPressedTick <= 7) {
                HunterNetwork.sendToServer(new UseTechniquePacket(UseTechniquePacket.Technique.DOUBLE_JUMP, computeAirJumpDirection(player)));
            }
            lastJumpPressedTick = currentTick;
        }
        previousJumpDown = jumpDown;

        player.getCapability(HunterPlayerDataProvider.CAPABILITY).ifPresent(data -> {
            boolean usedCombatBind = false;
            boolean combatKeyHeld = false;
            if (data.isCombatBarVisible()) {
                boolean twoBars = hasTwoCombatBarsOnScreen();
                for (int i = 0; i < COMBAT_KEYS.length; i++) {
                    boolean combatKeyDown = COMBAT_KEYS[i].isDown();
                    if (combatKeyDown) {
                        combatKeyHeld = true;
                    }
                    if (combatKeyDown && !previousCombatKeyDown[i]) {
                        triggerAbility(twoBars ? data.getCombatSlot(0, i) : data.getCombatSlot(i), player);
                        usedCombatBind = true;
                    }
                    previousCombatKeyDown[i] = combatKeyDown;
                }
                for (int i = 0; i < COMBAT_BAR_2_KEYS.length; i++) {
                    boolean combatKeyDown = COMBAT_BAR_2_KEYS[i].isDown();
                    if (combatKeyDown) {
                        combatKeyHeld = true;
                    }
                    if (twoBars && combatKeyDown && !previousCombatBar2KeyDown[i]) {
                        triggerAbility(data.getCombatSlot(1, i), player);
                        usedCombatBind = true;
                    }
                    previousCombatBar2KeyDown[i] = combatKeyDown;
                    if (!twoBars) {
                        while (COMBAT_BAR_2_KEYS[i].consumeClick()) {
                            // Drop queued second-bar presses while dual-bar mode is disabled.
                        }
                    }
                }
            } else {
                for (int i = 0; i < COMBAT_KEYS.length; i++) {
                    previousCombatKeyDown[i] = COMBAT_KEYS[i].isDown();
                    while (COMBAT_KEYS[i].consumeClick()) {
                        // Drop queued presses while the combat bar is hidden so they don't fire on reopen.
                    }
                }
                for (int i = 0; i < COMBAT_BAR_2_KEYS.length; i++) {
                    previousCombatBar2KeyDown[i] = COMBAT_BAR_2_KEYS[i].isDown();
                    while (COMBAT_BAR_2_KEYS[i].consumeClick()) {
                        // Drop queued presses while the combat bar is hidden so they don't fire on reopen.
                    }
                }
            }
            if ((usedCombatBind || combatKeyHeld) && data.isCombatBarVisible() && player.getInventory().selected != cachedHotbarSlot) {
                player.getInventory().selected = cachedHotbarSlot;
                if (player.connection != null) {
                    player.connection.send(new ServerboundSetCarriedItemPacket(cachedHotbarSlot));
                }
            } else if (!combatKeyHeld) {
                protectedHotbarSlot = player.getInventory().selected;
            }
        });
    }

    private static boolean isAnyCombatBindDown() {
        for (KeyMapping combatKey : COMBAT_KEYS) {
            if (combatKey.isDown()) {
                return true;
            }
        }
        for (KeyMapping combatKey : COMBAT_BAR_2_KEYS) {
            if (combatKey.isDown()) {
                return true;
            }
        }
        return false;
    }

    private static boolean isStunnedForInput(LocalPlayer player) {
        return player.hasEffect(HunterMobEffects.STUNNED.get()) || player.hasEffect(HunterMobEffects.PARRY_STUNNED.get());
    }

    @SubscribeEvent
    public static void onRenderHand(RenderHandEvent event) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null) {
            return;
        }
        player.getCapability(HunterPlayerDataProvider.CAPABILITY).ifPresent(data -> applyFirstPersonAbilityAnimation(event, player, data));

        ItemStack stack = event.getItemStack();
        if (stack.isEmpty() || !stack.is(HunterItems.SMOKING_PIPE.get())) {
            return;
        }
        if (!player.isUsingItem() || !player.getUseItem().is(HunterItems.SMOKING_PIPE.get()) || player.getUsedItemHand() != event.getHand()) {
            return;
        }

        boolean mainHand = event.getHand() == InteractionHand.MAIN_HAND;
        float side = mainHand ? 1.0F : -1.0F;
        event.getPoseStack().translate(0.14F * side, 0.18F, -0.22F);
        event.getPoseStack().mulPose(Axis.XP.rotationDegrees(-45.0F));
        event.getPoseStack().mulPose(Axis.YP.rotationDegrees(10.0F * side));
        event.getPoseStack().mulPose(Axis.ZP.rotationDegrees(-18.0F * side));
    }

    private static void applyFirstPersonAbilityAnimation(RenderHandEvent event, LocalPlayer player, HunterPlayerData data) {
        AnimationType animation = resolveFirstPersonAnimation(player, data);
        if (animation == AnimationType.NONE) {
            return;
        }

        boolean mainHand = event.getHand() == InteractionHand.MAIN_HAND;
        float side = mainHand ? 1.0F : -1.0F;
        float pulse = player.tickCount + event.getPartialTick();
        float bob = (float) Math.sin(pulse * 0.45F);

        switch (animation) {
            case GUARD -> transformFistGuard(event, side);
            case PARRY -> transformParry(event, side, false);
            case WEAPON_PARRY -> transformParry(event, side, true);
            case DASH, DOUBLE_JUMP, SKYBREAKER_ASCENT, SKYBREAKER_DROP -> transformRush(event, side);
            case FLASH_CLEAVE_ONE, FLASH_CLEAVE_TWO, FLASH_CLEAVE_THREE, HEAVEN_SPLITTER, VOID_REND, MIRROR_REPRISAL_STRIKE,
                    LION_FANG_DRAW_CHARGE, MARTIAL_WHIRLWIND_ARC, MARTIAL_TORA_HUNT -> transformSlash(event, side, mainHand);
            case AEGIS_RUSH, MIRROR_REPRISAL_GUARD, STEEL_TATSUMAKI, ELASTIC_REFLECT -> transformDefensive(event, side, bob);
            case BOXER_JAB, BOXER_HAMMER_STRIKE, BOXER_COUNTER_STRIKE, BOXER_REDIRECTION, BOXER_GRAB_SLAM,
                    MARTIAL_FACE_JAB, MARTIAL_RISING_SHOT, MARTIAL_METEOR_HEEL_IMPACT -> transformHeavyPunch(event, side, mainHand);
            case BOXER_BARRAGE, BOXER_BARRAGE_FAST, MARTIAL_AIR_BARRAGE -> transformBarrage(event, side, mainHand, pulse);
            case BOXER_COUNTER_GUARD, BOXER_GRAB_LIFT -> transformBoxingGuard(event, side);
            case MARTIAL_METEOR_HEEL, MARTIAL_ANKLE_SPLITTER -> transformMartialStrike(event, side, mainHand);
            case SMOKING_PIPE -> transformSmokingPose(event, side);
            case SMOKE_SOLDIER_SUMMON, DOWSING_CHAIN_SWING -> transformSummon(event, side, bob);
            default -> {
                if (animation == AnimationType.WEAPON_PARRY || isGuardWeapon(event.getItemStack())) {
                    transformWeaponGuard(event, side);
                }
            }
        }
    }

    private static AnimationType resolveFirstPersonAnimation(LocalPlayer player, HunterPlayerData data) {
        return data.getCurrentAnimation();
    }

    private static void transformFistGuard(RenderHandEvent event, float side) {
        transformBoxingGuard(event, side);
    }

    private static void transformBoxingGuard(RenderHandEvent event, float side) {
        event.getPoseStack().translate(-0.18F * side, 0.18F, -0.38F);
        event.getPoseStack().mulPose(Axis.XP.rotationDegrees(-58.0F));
        event.getPoseStack().mulPose(Axis.YP.rotationDegrees(24.0F * side));
        event.getPoseStack().mulPose(Axis.ZP.rotationDegrees(-42.0F * side));
    }

    private static void transformWeaponGuard(RenderHandEvent event, float side) {
        event.getPoseStack().translate(-0.08F * side, 0.08F, -0.28F);
        event.getPoseStack().mulPose(Axis.XP.rotationDegrees(-36.0F));
        event.getPoseStack().mulPose(Axis.YP.rotationDegrees(34.0F * side));
        event.getPoseStack().mulPose(Axis.ZP.rotationDegrees(-22.0F * side));
    }

    private static void transformParry(RenderHandEvent event, float side, boolean weapon) {
        event.getPoseStack().translate(-0.22F * side, 0.02F, -0.44F);
        event.getPoseStack().mulPose(Axis.XP.rotationDegrees(weapon ? -52.0F : -70.0F));
        event.getPoseStack().mulPose(Axis.YP.rotationDegrees((weapon ? 48.0F : 30.0F) * side));
        event.getPoseStack().mulPose(Axis.ZP.rotationDegrees((weapon ? -52.0F : -70.0F) * side));
    }

    private static void transformRush(RenderHandEvent event, float side) {
        event.getPoseStack().translate(0.08F * side, -0.1F, -0.42F);
        event.getPoseStack().mulPose(Axis.XP.rotationDegrees(-28.0F));
        event.getPoseStack().mulPose(Axis.YP.rotationDegrees(-14.0F * side));
        event.getPoseStack().mulPose(Axis.ZP.rotationDegrees(12.0F * side));
    }

    private static void transformSlash(RenderHandEvent event, float side, boolean mainHand) {
        float power = mainHand ? 1.0F : 0.55F;
        event.getPoseStack().translate(0.12F * side * power, -0.04F, -0.5F * power);
        event.getPoseStack().mulPose(Axis.XP.rotationDegrees(-76.0F * power));
        event.getPoseStack().mulPose(Axis.YP.rotationDegrees(42.0F * side * power));
        event.getPoseStack().mulPose(Axis.ZP.rotationDegrees(-58.0F * side * power));
    }

    private static void transformDefensive(RenderHandEvent event, float side, float bob) {
        event.getPoseStack().translate(-0.1F * side, 0.04F + bob * 0.015F, -0.34F);
        event.getPoseStack().mulPose(Axis.XP.rotationDegrees(-42.0F));
        event.getPoseStack().mulPose(Axis.YP.rotationDegrees(24.0F * side));
        event.getPoseStack().mulPose(Axis.ZP.rotationDegrees(-28.0F * side));
    }

    private static void transformHeavyPunch(RenderHandEvent event, float side, boolean mainHand) {
        float lead = mainHand ? 1.0F : 0.45F;
        event.getPoseStack().translate(-0.22F * side * lead, -0.08F, -0.62F * lead);
        event.getPoseStack().mulPose(Axis.XP.rotationDegrees(-82.0F * lead));
        event.getPoseStack().mulPose(Axis.YP.rotationDegrees(18.0F * side * lead));
        event.getPoseStack().mulPose(Axis.ZP.rotationDegrees(-24.0F * side * lead));
    }

    private static void transformBarrage(RenderHandEvent event, float side, boolean mainHand, float pulse) {
        float alternate = ((int) (pulse * 1.5F) & 1) == 0 ? 1.0F : -1.0F;
        float lead = (mainHand ? alternate : -alternate) > 0.0F ? 1.0F : 0.35F;
        event.getPoseStack().translate(-0.2F * side * lead, -0.08F, -0.48F - 0.22F * lead);
        event.getPoseStack().mulPose(Axis.XP.rotationDegrees(-74.0F * lead));
        event.getPoseStack().mulPose(Axis.YP.rotationDegrees(22.0F * side * lead));
        event.getPoseStack().mulPose(Axis.ZP.rotationDegrees((-18.0F - 28.0F * lead) * side));
    }

    private static void transformMartialStrike(RenderHandEvent event, float side, boolean mainHand) {
        float lead = mainHand ? 1.0F : 0.5F;
        event.getPoseStack().translate(0.06F * side, -0.18F * lead, -0.52F * lead);
        event.getPoseStack().mulPose(Axis.XP.rotationDegrees(-48.0F * lead));
        event.getPoseStack().mulPose(Axis.YP.rotationDegrees(-28.0F * side * lead));
        event.getPoseStack().mulPose(Axis.ZP.rotationDegrees(36.0F * side * lead));
    }

    private static void transformSmokingPose(RenderHandEvent event, float side) {
        event.getPoseStack().translate(0.04F * side, 0.08F, -0.08F);
        event.getPoseStack().mulPose(Axis.XP.rotationDegrees(-18.0F));
        event.getPoseStack().mulPose(Axis.ZP.rotationDegrees(-8.0F * side));
    }

    private static void transformSummon(RenderHandEvent event, float side, float bob) {
        event.getPoseStack().translate(0.0F, 0.05F + bob * 0.02F, -0.24F);
        event.getPoseStack().mulPose(Axis.XP.rotationDegrees(-34.0F));
        event.getPoseStack().mulPose(Axis.YP.rotationDegrees(16.0F * side));
        event.getPoseStack().mulPose(Axis.ZP.rotationDegrees(-14.0F * side));
    }

    private static boolean isWeaponGuard(LocalPlayer player, HunterPlayerData data) {
        return data.getSelectedSkillCategory() == SkillNode.Category.WEAPON
                && (isGuardWeapon(player.getMainHandItem()) || isGuardWeapon(player.getOffhandItem()));
    }

    private static boolean isGuardWeapon(ItemStack stack) {
        return !stack.isEmpty() && (stack.getItem() instanceof SwordItem || stack.getItem() instanceof AxeItem || stack.getItem() instanceof TridentItem);
    }

    private static Vec3 computeDashDirection(LocalPlayer player) {
        Vec3 look = player.getLookAngle();
        Vec3 forward = new Vec3(look.x, 0.0D, look.z);
        if (forward.lengthSqr() < 1.0E-4D) {
            forward = new Vec3(0.0D, 0.0D, 1.0D);
        }
        forward = forward.normalize();
        Vec3 right = new Vec3(-forward.z, 0.0D, forward.x);

        Minecraft minecraft = Minecraft.getInstance();
        Vec3 direction = Vec3.ZERO;
        if (minecraft.options.keyUp.isDown()) {
            direction = direction.add(forward);
        }
        if (minecraft.options.keyDown.isDown()) {
            direction = direction.subtract(forward);
        }
        if (minecraft.options.keyLeft.isDown()) {
            direction = direction.subtract(right);
        }
        if (minecraft.options.keyRight.isDown()) {
            direction = direction.add(right);
        }
        if (minecraft.options.keyJump.isDown()) {
            direction = direction.add(0.0D, 1.0D, 0.0D);
        }
        if (minecraft.options.keyShift.isDown()) {
            direction = direction.add(0.0D, -1.0D, 0.0D);
        }
        if (direction.lengthSqr() < 1.0E-4D) {
            direction = player.getLookAngle();
        }
        return direction.normalize();
    }

    private static Vec3 computeAirJumpDirection(LocalPlayer player) {
        Minecraft minecraft = Minecraft.getInstance();
        Vec3 look = player.getLookAngle();
        Vec3 forward = new Vec3(look.x, 0.0D, look.z);
        if (forward.lengthSqr() < 1.0E-4D) {
            forward = new Vec3(0.0D, 0.0D, 1.0D);
        }
        forward = forward.normalize();
        Vec3 right = new Vec3(-forward.z, 0.0D, forward.x);
        Vec3 direction = Vec3.ZERO;
        if (minecraft.options.keyUp.isDown()) {
            direction = direction.add(forward);
        }
        if (minecraft.options.keyDown.isDown()) {
            direction = direction.subtract(forward);
        }
        if (minecraft.options.keyLeft.isDown()) {
            direction = direction.subtract(right);
        }
        if (minecraft.options.keyRight.isDown()) {
            direction = direction.add(right);
        }
        if (direction.lengthSqr() < 1.0E-4D) {
            direction = forward;
        }
        return direction.normalize();
    }

    public static void adjustSkillPoints(SkillNode node, int delta) {
        HunterNetwork.sendToServer(new AdjustSkillPointsPacket(node.id(), delta));
    }

    public static String getCombatKeyLabel(int slot) {
        return getCombatKeyLabel(0, slot);
    }

    public static String getCombatKeyLabel(int barIndex, int slot) {
        KeyMapping[] keys = barIndex == 1 ? COMBAT_BAR_2_KEYS : COMBAT_KEYS;
        if (slot < 0 || slot >= keys.length) {
            return "";
        }
        String label = keys[slot].getTranslatedKeyMessage().getString().trim();
        if (label.length() <= 3) {
            return label;
        }
        return label.substring(0, 3).toUpperCase(java.util.Locale.ROOT);
    }

    public static boolean hasTwoCombatBarsOnScreen() {
        return HunterConfig.ABILITY_BARS_ON_SCREEN.get() >= HunterPlayerData.COMBAT_BAR_COUNT;
    }

    private static void triggerAbility(String abilityId, LocalPlayer player) {
        HunterAbility ability = HunterAbilities.byId(abilityId);
        if (ability == null) {
            return;
        }
        if (HunterAbilities.DASH.id().equals(ability.id())) {
            HunterNetwork.sendToServer(new UseTechniquePacket(UseTechniquePacket.Technique.DASH, computeDashDirection(player)));
        } else if (HunterAbilities.DOUBLE_JUMP.id().equals(ability.id())) {
            HunterNetwork.sendToServer(new UseTechniquePacket(UseTechniquePacket.Technique.DOUBLE_JUMP, computeAirJumpDirection(player)));
        } else if (HunterAbilities.GUARD.id().equals(ability.id())) {
            HunterNetwork.sendToServer(new UseTechniquePacket(UseTechniquePacket.Technique.GUARD_TOGGLE, Vec3.ZERO));
        } else if (ability instanceof SkillTreeCombatAbility) {
            HunterNetwork.sendToServer(new UseCombatAbilityPacket(ability.id(), player.getLookAngle()));
        }
    }

}
