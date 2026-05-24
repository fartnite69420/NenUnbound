package com.huntercraft.huntercraft.client;

import com.huntercraft.huntercraft.HunterCraftMod;
import com.huntercraft.huntercraft.HunterConfig;
import com.huntercraft.huntercraft.abilities.GrabAbility;
import com.huntercraft.huntercraft.abilities.HunterAbilities;
import com.huntercraft.huntercraft.abilities.SkillTreeCombatAbility;
import com.huntercraft.huntercraft.abilities.ChainConjuration.ChainJailAbility;
import com.huntercraft.huntercraft.abilities.martialartstree.MartialArtsGrabHelper;
import com.huntercraft.huntercraft.abilities.base.BaseTechniqueAbility;
import com.huntercraft.huntercraft.abilities.nenability.NenTechniqueAbility;
import com.huntercraft.huntercraft.ability.HunterAbility;
import com.huntercraft.huntercraft.data.HunterPlayerData;
import com.huntercraft.huntercraft.data.HunterPlayerDataProvider;
import com.huntercraft.huntercraft.effect.HunterMobEffects;
import com.huntercraft.huntercraft.network.packet.MeditationPromptInputPacket;
import com.huntercraft.huntercraft.quest.NenQuestStage;
import com.huntercraft.huntercraft.quest.NenQuestUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = HunterCraftMod.MODID)
public class HunterHudOverlay implements IGuiOverlay {
    public static final HunterHudOverlay INSTANCE = new HunterHudOverlay();
    private static final ResourceLocation LUNG_CAPACITY_ICON = new ResourceLocation(HunterCraftMod.MODID, "textures/gui/lung_capacity_passive.png");
    private static final int COMBAT_SLOT_SIZE = 23;
    private static final int COMBAT_SLOT_GAP = 4;
    private static final int COMBAT_ICON_SIZE = 16;
    private static final int COMBAT_ICON_TEXTURE_SIZE = 64;
    private static final int PASSIVE_ICON_TEXTURE_SIZE = 64;
    private static final float COMBAT_KEY_LABEL_SCALE = 0.6F;
    private static final int BASE_SLOT_SIZE = 22;
    private static final int BASE_SLOT_GAP = 4;
    private static final int BASE_ICON_SIZE = 14;
    private static final int INVENTORY_VITALS_Y_OFFSET = 56;
    private static final int COMBAT_VITALS_Y_OFFSET = 44;
    private static final int COMBAT_BAR_Y_OFFSET = 28;
    private static final int COMBAT_BAR_STACK_GAP = 4;
    private static final int DUAL_BAR_HUD_Y_SHIFT = COMBAT_SLOT_SIZE + COMBAT_BAR_STACK_GAP;
    private static final int VANILLA_COMBAT_HUD_HEIGHT = 56;
    private static final int HEALTH_BAR_WIDTH = 84;
    private static final int HEALTH_BAR_HEIGHT = 14;
    private static final int NEN_BAR_WIDTH = 84;
    private static final int NEN_BAR_HEIGHT = 14;
    private static final int FOOD_BAR_COMBAT_X_OFFSET = -11;
    private static boolean foodOverlayTranslated;

    @Override
    public void render(ForgeGui gui, GuiGraphics graphics, float partialTick, int screenWidth, int screenHeight) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.options.hideGui) {
            return;
        }

        minecraft.player.getCapability(HunterPlayerDataProvider.CAPABILITY).ifPresent(data ->
                renderData(graphics, minecraft.font, data, minecraft, screenWidth, screenHeight, partialTick));
    }

    private void renderData(GuiGraphics graphics, Font font, HunterPlayerData data, Minecraft minecraft, int screenWidth, int screenHeight, float partialTick) {
        boolean spectator = minecraft.player != null && minecraft.player.isSpectator();
        boolean creative = minecraft.player != null && minecraft.player.isCreative();
        if (!creative && !spectator) {
            renderNumericHealth(graphics, font, data, minecraft, screenWidth, screenHeight);
        }

        if (!spectator) {
            if (data.isCombatBarVisible()) {
                renderStaminaBar(graphics, font, data, screenWidth, screenHeight);
            }
            if (data.getNenQuestStage() == NenQuestStage.FEEL_THE_AURA && (data.isMeditationCountdownActive() || data.isMeditationActive())) {
                renderMeditationHud(graphics, font, data, screenWidth, screenHeight);
            }
            if (data.isCombatBarVisible()) {
                renderCombatBar(graphics, font, data, screenWidth, screenHeight, partialTick);
                renderBaseAbilities(graphics, font, data, screenWidth, screenHeight, partialTick);
                renderNenPassiveHud(graphics, font, data, screenWidth, screenHeight);
            }
            renderChainJailMinigame(graphics, font, minecraft, screenWidth, screenHeight);
        }
    }

    private void renderNenPassiveHud(GuiGraphics graphics, Font font, HunterPlayerData data, int screenWidth, int screenHeight) {
        if (!data.hasDeepPurpleTechnique() || data.getMaxLungCapacity() <= 0) {
            return;
        }
        int healthX = getHealthBarX(screenWidth);
        int healthY = getVitalsY(data, screenHeight);
        int iconSize = 16;
        int iconX = healthX - 84;
        int iconY = healthY - 2;
        int barX = iconX + iconSize + 4;
        int barY = iconY + 5;
        int barWidth = 32;
        int barHeight = 6;
        int textX = barX + barWidth + 6;
        float percent = data.getMaxLungCapacity() <= 0 ? 0.0F : (float) data.getLungCapacity() / data.getMaxLungCapacity();
        int fillWidth = Math.max(0, Math.min(barWidth, Math.round(barWidth * percent)));

        renderScaledHudIcon(graphics, LUNG_CAPACITY_ICON, iconX, iconY, iconSize);
        HunterHudStyle.bar(graphics, barX, barY, barWidth, barHeight, percent, 0xCCB74444);
        graphics.drawString(font, String.valueOf(data.getLungCapacity()), textX, iconY + 4, 0xFFFFFF, false);
    }

    private void renderScaledHudIcon(GuiGraphics graphics, ResourceLocation icon, int x, int y, int size) {
        PoseStack pose = graphics.pose();
        float scale = (float) size / PASSIVE_ICON_TEXTURE_SIZE;
        pose.pushPose();
        pose.translate(x, y, 0.0F);
        pose.scale(scale, scale, 1.0F);
        graphics.blit(icon, 0, 0, 0, 0, PASSIVE_ICON_TEXTURE_SIZE, PASSIVE_ICON_TEXTURE_SIZE, PASSIVE_ICON_TEXTURE_SIZE, PASSIVE_ICON_TEXTURE_SIZE);
        pose.popPose();
    }

    private void renderBaseAbilities(GuiGraphics graphics, Font font, HunterPlayerData data, int screenWidth, int screenHeight, float partialTick) {
        int count = HunterAbilities.BASE_TECHNIQUES.size();
        int totalWidth = (count * (BASE_SLOT_SIZE + BASE_SLOT_GAP)) - BASE_SLOT_GAP;
        int x = (screenWidth - totalWidth) / 2;
        int y = getVitalsY(data, screenHeight) - BASE_SLOT_SIZE - 13;
        for (int i = 0; i < count; i++) {
            renderBaseAbility(graphics, font, HunterAbilities.BASE_TECHNIQUES.get(i), x + i * (BASE_SLOT_SIZE + BASE_SLOT_GAP), y, data, partialTick);
        }
    }

    private void renderBaseAbility(GuiGraphics graphics, Font font, BaseTechniqueAbility ability, int x, int y, HunterPlayerData data, float partialTick) {
        int cooldownTicks = ability.getCurrentCooldown(data);
        int maxCooldownTicks = ability.getMaxCooldownTicks();
        boolean active = ability.isActive(data);
        boolean charging = ability.isCharging(data);
        int outlineColor;
        if (charging) {
            outlineColor = 0xCCD0A63A;
        } else if (active && ability.isContinuous()) {
            outlineColor = 0xCC3B6EA8;
        } else if (cooldownTicks > 0) {
            outlineColor = 0xCCB83B4B;
        } else {
            outlineColor = 0xCC28333D;
        }
        HunterHudStyle.compactTechniqueSlot(graphics, x, y, BASE_SLOT_SIZE, outlineColor);
        renderScaledBaseIcon(graphics, new ResourceLocation(HunterCraftMod.MODID, ability.iconPath()), x + 4, y + 4);
        if (cooldownTicks > 0) {
            float displayCooldown = getDisplayCooldown(cooldownTicks, partialTick);
            int innerHeight = BASE_SLOT_SIZE - 4;
            int overlayHeight = Math.max(1, (int) Math.ceil((displayCooldown / maxCooldownTicks) * innerHeight));
            graphics.fill(x + 2, y + (BASE_SLOT_SIZE - 2 - overlayHeight), x + BASE_SLOT_SIZE - 2, y + BASE_SLOT_SIZE - 2, 0xBB05080D);
            drawBaseCooldownNumber(graphics, font, x, y, displayCooldown);
        } else if (charging) {
            drawActiveDuration(graphics, font, x, y, ability.getChargeTicks(data), BASE_SLOT_SIZE / 2, 7, 0xF3DA78);
        } else if (active && ability.isContinuous()) {
            drawActiveDuration(graphics, font, x, y, ability.getActiveTicks(data), BASE_SLOT_SIZE / 2, 7, 0x8EEBFF);
        }
    }

    private void renderCombatBar(GuiGraphics graphics, Font font, HunterPlayerData data, int screenWidth, int screenHeight, float partialTick) {
        int barWidth = (HunterPlayerData.COMBAT_SLOT_COUNT * (COMBAT_SLOT_SIZE + COMBAT_SLOT_GAP)) - COMBAT_SLOT_GAP;
        int x = (screenWidth - barWidth) / 2;
        int y = screenHeight - COMBAT_BAR_Y_OFFSET;
        if (hasTwoCombatBarsOnScreen()) {
            renderCombatBarRow(graphics, font, data, x, y - COMBAT_SLOT_SIZE - COMBAT_BAR_STACK_GAP, 1, partialTick);
            renderCombatBarRow(graphics, font, data, x, y, 0, partialTick);
        } else {
            renderCombatBarRow(graphics, font, data, x, y, data.getActiveCombatBar(), partialTick);
        }
    }

    private void renderCombatBarRow(GuiGraphics graphics, Font font, HunterPlayerData data, int x, int y, int barIndex, float partialTick) {
        for (int i = 0; i < HunterPlayerData.COMBAT_SLOT_COUNT; i++) {
            int slotX = x + (i * (COMBAT_SLOT_SIZE + COMBAT_SLOT_GAP));
            HunterAbility ability = HunterAbilities.byId(data.getCombatSlot(barIndex, i));
            int outlineColor = getAbilityOutlineColor(data, ability);
            HunterHudStyle.abilitySlot(graphics, slotX, y, COMBAT_SLOT_SIZE, outlineColor, false);
            if (ability != null) {
                ResourceLocation icon = new ResourceLocation(HunterCraftMod.MODID, ability.iconPath());
                int iconX = slotX + ((COMBAT_SLOT_SIZE - COMBAT_ICON_SIZE) / 2);
                int iconY = y + 3;
                renderScaledCombatIcon(graphics, icon, iconX, iconY);
                if (isAbilityBlockedByZetsu(data, ability)) {
                    drawBlockedOverlay(graphics, iconX, iconY, COMBAT_ICON_SIZE);
                }
                if (data.isJudgmentDisabledAbility(ability.id())) {
                    drawChainDisabledOverlay(graphics, iconX, iconY, COMBAT_ICON_SIZE);
                }
                int cooldown = getAbilityCooldown(data, ability);
                int maxCooldown = getAbilityMaxCooldown(ability);
                if (cooldown > 0 && maxCooldown > 0) {
                    float displayCooldown = getDisplayCooldown(cooldown, partialTick);
                    int overlayHeight = Math.max(1, (int) Math.ceil((displayCooldown / maxCooldown) * COMBAT_ICON_SIZE));
                    int overlayTop = (y + 3 + COMBAT_ICON_SIZE) - overlayHeight;
                    graphics.fill(slotX + ((COMBAT_SLOT_SIZE - COMBAT_ICON_SIZE) / 2), overlayTop,
                            slotX + ((COMBAT_SLOT_SIZE - COMBAT_ICON_SIZE) / 2) + COMBAT_ICON_SIZE, y + 3 + COMBAT_ICON_SIZE, 0xAA05080D);
                    drawCooldownNumber(graphics, font, slotX, y, displayCooldown);
                } else if (isAbilityCharging(data, ability)) {
                    drawActiveDuration(graphics, font, slotX, y, getAbilityChargeTicks(data, ability), COMBAT_SLOT_SIZE / 2, 12, 0xF3DA78);
                } else if (isAbilityContinuousActive(data, ability)) {
                    drawActiveDuration(graphics, font, slotX, y, getAbilityActiveTicks(data, ability), COMBAT_SLOT_SIZE / 2, 12, 0x8EEBFF);
                }
            }
            int labelBarIndex = hasTwoCombatBarsOnScreen() ? barIndex : 0;
            drawBindLabel(graphics, font, slotX, y, ClientHooks.getCombatKeyLabel(labelBarIndex, i));
        }
    }

    private void renderScaledCombatIcon(GuiGraphics graphics, ResourceLocation icon, int x, int y) {
        PoseStack pose = graphics.pose();
        float scale = (float) COMBAT_ICON_SIZE / COMBAT_ICON_TEXTURE_SIZE;
        pose.pushPose();
        pose.translate(x, y, 0.0F);
        pose.scale(scale, scale, 1.0F);
        graphics.blit(icon, 0, 0, 0, 0, COMBAT_ICON_TEXTURE_SIZE, COMBAT_ICON_TEXTURE_SIZE, COMBAT_ICON_TEXTURE_SIZE, COMBAT_ICON_TEXTURE_SIZE);
        pose.popPose();
    }

    private void renderScaledBaseIcon(GuiGraphics graphics, ResourceLocation icon, int x, int y) {
        PoseStack pose = graphics.pose();
        float scale = (float) BASE_ICON_SIZE / COMBAT_ICON_TEXTURE_SIZE;
        pose.pushPose();
        pose.translate(x, y, 0.0F);
        pose.scale(scale, scale, 1.0F);
        graphics.blit(icon, 0, 0, 0, 0, COMBAT_ICON_TEXTURE_SIZE, COMBAT_ICON_TEXTURE_SIZE, COMBAT_ICON_TEXTURE_SIZE, COMBAT_ICON_TEXTURE_SIZE);
        pose.popPose();
    }

    private boolean isAbilityBlockedByZetsu(HunterPlayerData data, HunterAbility ability) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || !minecraft.player.hasEffect(HunterMobEffects.ZETSU.get())) {
            return false;
        }
        if (!(ability instanceof SkillTreeCombatAbility skillTreeCombatAbility)) {
            return false;
        }
        return skillTreeCombatAbility.isSuppressedByZetsu()
                && !(skillTreeCombatAbility instanceof NenTechniqueAbility && skillTreeCombatAbility.isActive(data));
    }

    private void drawBlockedOverlay(GuiGraphics graphics, int x, int y, int size) {
        int color = 0xFFFF1010;
        int centerX = x + (size / 2);
        int centerY = y + (size / 2);
        int radius = (size / 2) - 1;
        int thickness = 2;
        for (int i = 0; i < 40; i++) {
            double angle = (Math.PI * 2.0D * i) / 40.0D;
            int px = centerX + Mth.floor(Math.cos(angle) * radius);
            int py = centerY + Mth.floor(Math.sin(angle) * radius);
            graphics.fill(px - 1, py - 1, px + thickness, py + thickness, color);
        }
        for (int i = 1; i < size - 1; i++) {
            int px = x + i;
            int py = y + i;
            graphics.fill(px - 1, py - 1, px + 2, py + 2, color);
        }
    }

    private void drawChainDisabledOverlay(GuiGraphics graphics, int x, int y, int size) {
        int chainColor = 0xFFE4D0A1;
        int shadowColor = 0xAA3B3428;
        for (int i = 0; i < 4; i++) {
            int linkX = x + 1 + (i * 4);
            graphics.fill(linkX, y + 1, linkX + 6, y + 3, shadowColor);
            graphics.fill(linkX + 1, y + 2, linkX + 5, y + 4, chainColor);
            graphics.fill(linkX, y + size - 4, linkX + 6, y + size - 2, shadowColor);
            graphics.fill(linkX + 1, y + size - 5, linkX + 5, y + size - 3, chainColor);
        }
        graphics.fill(x + 1, y + 1, x + 3, y + size - 1, chainColor);
        graphics.fill(x + size - 3, y + 1, x + size - 1, y + size - 1, chainColor);
    }

    private void renderChainJailMinigame(GuiGraphics graphics, Font font, Minecraft minecraft, int screenWidth, int screenHeight) {
        if (minecraft.player == null) {
            return;
        }
        MobEffectInstance effect = minecraft.player.getEffect(HunterMobEffects.CHAIN_JAIL.get());
        if (effect == null) {
            return;
        }
        int width = 156;
        int height = 40;
        int x = (screenWidth - width) / 2;
        int y = Math.max(18, (screenHeight / 2) + 34);
        int barX = x + 14;
        int barY = y + 17;
        int barWidth = width - 28;
        int barHeight = 10;
        HunterHudStyle.panel(graphics, x, y, width, height);
        String title = effect.getAmplifier() == 2 ? "CHAIN JAIL  VOW LOCKED" : "CHAIN JAIL";
        graphics.drawString(font, title, x + 12, y + 6, effect.getAmplifier() == 2 ? 0xFFFF5454 : HunterHudStyle.GOLD, false);
        HunterHudStyle.recessedPanel(graphics, barX - 2, barY - 2, barWidth + 4, barHeight + 4);
        int greenStart = barX + Math.round(barWidth * ChainJailAbility.ESCAPE_GREEN_START);
        int greenEnd = barX + Math.round(barWidth * ChainJailAbility.ESCAPE_GREEN_END);
        graphics.fill(barX, barY, greenStart, barY + barHeight, 0xFF8E2532);
        graphics.fill(greenStart, barY, greenEnd, barY + barHeight, 0xFF54D76E);
        graphics.fill(greenEnd, barY, barX + barWidth, barY + barHeight, 0xFF8E2532);
        graphics.fill(greenStart, barY, greenEnd, barY + 1, 0xAAE8FFF0);
        graphics.fill(barX, barY, barX + barWidth, barY + 1, 0x22FFFFFF);
        float progress = ChainJailAbility.getEscapeCursorProgress(minecraft.player.tickCount);
        int cursorX = barX + Mth.floor(progress * barWidth);
        graphics.fill(cursorX - 1, barY - 4, cursorX + 2, barY + barHeight + 4, 0xFFFFD866);
        graphics.fill(cursorX - 5, barY + barHeight + 4, cursorX + 6, barY + barHeight + 7, 0xFFFFD866);
        if (effect.getAmplifier() == 1) {
            String text = "2 BREAKS";
            graphics.drawString(font, text, x + width - font.width(text) - 12, y + 6, HunterHudStyle.TEXT_SOFT, false);
        }
    }

    private int getAbilityOutlineColor(HunterPlayerData data, HunterAbility ability) {
        if (ability instanceof BaseTechniqueAbility baseTechniqueAbility) {
            if (baseTechniqueAbility.isCharging(data)) {
                return 0xCCD0A63A;
            }
            if (baseTechniqueAbility.isContinuous() && baseTechniqueAbility.isActive(data)) {
                return 0xCC3B6EA8;
            }
            if (baseTechniqueAbility.getCurrentCooldown(data) > 0) {
                return 0xCCB83B4B;
            }
        }
        if (ability instanceof SkillTreeCombatAbility skillTreeCombatAbility) {
            if (skillTreeCombatAbility.isCharging(data)) {
                return 0xCCD0A63A;
            }
            if (ability instanceof GrabAbility && MartialArtsGrabHelper.isGrabSourceActive(data, skillTreeCombatAbility.id())) {
                return 0xCC3BBF5E;
            }
            if (skillTreeCombatAbility.isContinuous() && skillTreeCombatAbility.isActive(data)) {
                return 0xCC3B6EA8;
            }
            if (skillTreeCombatAbility.getCurrentCooldown(data) > 0) {
                return 0xCCB83B4B;
            }
        }
        if (ability instanceof GrabAbility && ability instanceof SkillTreeCombatAbility skillTreeCombatAbility && skillTreeCombatAbility.isActive(data)) {
            return 0xCC3BBF5E;
        }
        return 0xCC28333D;
    }

    private int getAbilityCooldown(HunterPlayerData data, HunterAbility ability) {
        if (ability instanceof BaseTechniqueAbility baseTechniqueAbility) {
            return baseTechniqueAbility.getCurrentCooldown(data);
        }
        if (ability instanceof SkillTreeCombatAbility skillTreeCombatAbility) {
            return skillTreeCombatAbility.getCurrentCooldown(data);
        }
        return 0;
    }

    private int getAbilityMaxCooldown(HunterAbility ability) {
        if (ability instanceof BaseTechniqueAbility baseTechniqueAbility) {
            return baseTechniqueAbility.getMaxCooldownTicks();
        }
        if (ability instanceof SkillTreeCombatAbility skillTreeCombatAbility) {
            return skillTreeCombatAbility.getMaxCooldownTicks();
        }
        return 0;
    }

    private boolean isAbilityContinuousActive(HunterPlayerData data, HunterAbility ability) {
        if (ability instanceof BaseTechniqueAbility baseTechniqueAbility) {
            return baseTechniqueAbility.isContinuous() && baseTechniqueAbility.isActive(data);
        }
        if (ability instanceof SkillTreeCombatAbility skillTreeCombatAbility) {
            return (skillTreeCombatAbility.isContinuous() && skillTreeCombatAbility.isActive(data))
                    || (ability instanceof GrabAbility && MartialArtsGrabHelper.isGrabSourceActive(data, skillTreeCombatAbility.id()));
        }
        return false;
    }

    private boolean isAbilityCharging(HunterPlayerData data, HunterAbility ability) {
        if (ability instanceof BaseTechniqueAbility baseTechniqueAbility) {
            return baseTechniqueAbility.isCharging(data);
        }
        if (ability instanceof SkillTreeCombatAbility skillTreeCombatAbility) {
            return skillTreeCombatAbility.isCharging(data);
        }
        return false;
    }

    private int getAbilityActiveTicks(HunterPlayerData data, HunterAbility ability) {
        if (ability instanceof BaseTechniqueAbility baseTechniqueAbility) {
            return baseTechniqueAbility.getActiveTicks(data);
        }
        if (ability instanceof SkillTreeCombatAbility skillTreeCombatAbility) {
            if (ability instanceof GrabAbility && MartialArtsGrabHelper.isGrabSourceActive(data, skillTreeCombatAbility.id())) {
                return data.getMartialArtsGrabTicksRemaining();
            }
            return skillTreeCombatAbility.getActiveTicks(data);
        }
        return 0;
    }

    private int getAbilityChargeTicks(HunterPlayerData data, HunterAbility ability) {
        if (ability instanceof BaseTechniqueAbility baseTechniqueAbility) {
            return baseTechniqueAbility.getChargeTicks(data);
        }
        if (ability instanceof SkillTreeCombatAbility skillTreeCombatAbility) {
            return skillTreeCombatAbility.getChargeTicks(data);
        }
        return 0;
    }

    private void drawCooldownNumber(GuiGraphics graphics, Font font, int x, int y, float cooldownTicks) {
        String text = formatCooldown(cooldownTicks);
        graphics.drawCenteredString(font, text, x + (COMBAT_SLOT_SIZE / 2), y + 12, 0xFFFFFF);
    }

    private void drawBaseCooldownNumber(GuiGraphics graphics, Font font, int x, int y, float cooldownTicks) {
        String text = formatCooldown(cooldownTicks);
        graphics.drawCenteredString(font, text, x + (BASE_SLOT_SIZE / 2), y + 7, 0xFFFFFF);
    }

    private void drawActiveDuration(GuiGraphics graphics, Font font, int x, int y, int heldTicks, int centerX, int centerY, int color) {
        String text = String.format(java.util.Locale.ROOT, "%.1f", heldTicks / 20.0F);
        graphics.drawCenteredString(font, text, x + centerX, y + centerY, color);
    }

    private void drawBindLabel(GuiGraphics graphics, Font font, int x, int y, String label) {
        if (label.isEmpty()) {
            return;
        }
        PoseStack pose = graphics.pose();
        float scaledTextWidth = font.width(label) * COMBAT_KEY_LABEL_SCALE;
        float textX = x + ((COMBAT_SLOT_SIZE - scaledTextWidth) / 2.0F);
        float textY = y + COMBAT_SLOT_SIZE - 6.0F;
        pose.pushPose();
        pose.translate(textX, textY, 0.0F);
        pose.scale(COMBAT_KEY_LABEL_SCALE, COMBAT_KEY_LABEL_SCALE, 1.0F);
        graphics.drawString(font, label, 0, 0, 0xD8DDE5, false);
        pose.popPose();
    }

    private float getDisplayCooldown(int cooldownTicks, float partialTick) {
        return Math.max(0.0F, cooldownTicks - partialTick);
    }

    private String formatCooldown(float cooldownTicks) {
        float seconds = cooldownTicks / 20.0F;
        if (seconds <= 0.0F) {
            return "";
        }
        return String.format(java.util.Locale.ROOT, "%.1f", seconds);
    }

    private void renderNumericHealth(GuiGraphics graphics, Font font, HunterPlayerData data, Minecraft minecraft, int screenWidth, int screenHeight) {
        if (minecraft.player == null) {
            return;
        }
        int panelWidth = HEALTH_BAR_WIDTH;
        int panelHeight = HEALTH_BAR_HEIGHT;
        int x = getHealthBarX(screenWidth);
        int y = getVitalsY(data, screenHeight);
        float currentHealth = minecraft.player.getHealth();
        float maxHealth = minecraft.player.getMaxHealth();
        float percent = maxHealth <= 0.0F ? 0.0F : currentHealth / maxHealth;

        HunterHudStyle.bar(graphics, x, y, panelWidth, panelHeight, percent, 0xCCB83B4B);
        graphics.drawString(font, "\u2665", x + 4, y + 3, 0xFFF3B0B8, false);
        String text = String.format(java.util.Locale.ROOT, "%d / %d", Math.round(currentHealth), Math.round(maxHealth));
        graphics.drawCenteredString(font, text, x + (panelWidth / 2) + 6, y + 3, 0xFFFFFF);
    }

    private void renderStaminaBar(GuiGraphics graphics, Font font, HunterPlayerData data, int screenWidth, int screenHeight) {
        int width = NEN_BAR_WIDTH;
        int height = NEN_BAR_HEIGHT;
        int x = getHealthBarX(screenWidth) + HEALTH_BAR_WIDTH + 6;
        int y = getVitalsY(data, screenHeight);
        HunterHudStyle.bar(graphics, x, y, width, height, data.getStaminaPercent(), 0xCCF2B13E);
        drawScaledCenteredString(graphics, font, data.getCurrentStamina() + " / " + data.getMaxStamina(), x + (width / 2), y + 4, 0x101010, 0.75F);
    }

    private boolean shouldUseDarkNenText(int nenColor) {
        int red = (nenColor >> 16) & 0xFF;
        int green = (nenColor >> 8) & 0xFF;
        int blue = nenColor & 0xFF;
        return red >= 235 && green >= 235 && blue >= 235;
    }

    private void drawScaledCenteredString(GuiGraphics graphics, Font font, String text, int centerX, int y, int color, float scale) {
        PoseStack pose = graphics.pose();
        float textWidth = font.width(text) * scale;
        pose.pushPose();
        pose.translate(centerX - (textWidth / 2.0F), y, 0.0F);
        pose.scale(scale, scale, 1.0F);
        graphics.drawString(font, text, 0, 0, color, false);
        pose.popPose();
    }

    private int getHealthBarX(int screenWidth) {
        return (screenWidth / 2) - 91;
    }

    private int getVitalsY(HunterPlayerData data, int screenHeight) {
        int offset = data.isCombatBarVisible() ? COMBAT_VITALS_Y_OFFSET : INVENTORY_VITALS_Y_OFFSET;
        if (data.isCombatBarVisible() && hasTwoCombatBarsOnScreen()) {
            offset += DUAL_BAR_HUD_Y_SHIFT;
        }
        return screenHeight - offset;
    }

    private static boolean hasTwoCombatBarsOnScreen() {
        return HunterConfig.ABILITY_BARS_ON_SCREEN.get() >= HunterPlayerData.COMBAT_BAR_COUNT;
    }

    private void renderMeditationHud(GuiGraphics graphics, Font font, HunterPlayerData data, int screenWidth, int screenHeight) {
        int centerX = screenWidth / 2;
        int panelWidth = 176;
        int panelLeft = centerX - (panelWidth / 2);
        int panelTop = screenHeight / 2 - 76;
        int panelBottom = panelTop + 106;
        graphics.fill(panelLeft, panelTop, panelLeft + panelWidth, panelBottom, 0x9A0A1016);
        graphics.fill(panelLeft, panelTop, panelLeft + panelWidth, panelTop + 18, 0xCC172332);
        graphics.drawCenteredString(font, Component.literal("MEDITATION"), centerX, panelTop + 5, 0xFFFFFF);

        if (data.isMeditationCountdownActive()) {
            int seconds = Math.max(1, Mth.ceil(data.getMeditationCountdownTicks() / 20.0F));
            graphics.drawCenteredString(font, Component.literal("Prepare yourself"), centerX, panelTop + 30, 0xD8E4EF);
            graphics.drawCenteredString(font, Component.literal(Integer.toString(seconds)), centerX, panelTop + 48, 0xF3C986);
            graphics.drawCenteredString(font, Component.literal("The pulses begin soon."), centerX, panelTop + 70, 0xC8D7E6);
            return;
        }

        String prompt = data.getMeditationPromptKey().isBlank() ? "-" : data.getMeditationPromptKey();
        float reactionPercent = data.getMeditationPromptTicksRemaining() <= 0
                ? 0.0F
                : (float) data.getMeditationPromptTicksRemaining() / MeditationPromptInputPacket.RESPONSE_WINDOW_TICKS;
        int reactionBarWidth = panelWidth - 20;
        int reactionLeft = panelLeft + 10;
        int reactionTop = panelTop + 26;
        graphics.fill(reactionLeft, reactionTop, reactionLeft + reactionBarWidth, reactionTop + 8, 0xAA121A24);
        graphics.fill(reactionLeft, reactionTop, reactionLeft + Math.max(1, Mth.floor(reactionBarWidth * reactionPercent)), reactionTop + 8, 0xFF5FD39A);
        graphics.drawCenteredString(font, Component.literal(prompt), centerX, panelTop + 42, 0xFFFFFF);
        graphics.drawCenteredString(font, Component.literal("Press " + prompt), centerX, panelTop + 58, 0xF3C986);

        float meditationPercent = Mth.clamp((float) data.getFeelAuraTicks() / NenQuestUtil.FEEL_AURA_TICKS_REQUIRED, 0.0F, 1.0F);
        int totalBarTop = panelTop + 82;
        graphics.fill(reactionLeft, totalBarTop, reactionLeft + reactionBarWidth, totalBarTop + 8, 0xAA121A24);
        graphics.fill(reactionLeft, totalBarTop, reactionLeft + Mth.floor(reactionBarWidth * meditationPercent), totalBarTop + 8, 0xFF5FD39A);
        graphics.drawCenteredString(font, Component.literal((data.getFeelAuraTicks() / 20) + " / 30s"), centerX, totalBarTop + 12, 0xD8E4EF);
    }

    @SubscribeEvent
    public static void onRenderOverlayPre(RenderGuiOverlayEvent.Pre event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return;
        }
        if (event.getOverlay().id().equals(VanillaGuiOverlay.HOTBAR.id())) {
            minecraft.player.getCapability(HunterPlayerDataProvider.CAPABILITY).ifPresent(data -> {
                if (data.isCombatBarVisible() && !minecraft.player.isSpectator()) {
                    event.setCanceled(true);
                }
            });
        } else if (event.getOverlay().id().equals(VanillaGuiOverlay.PLAYER_HEALTH.id())) {
            if (!minecraft.player.isCreative() && !minecraft.player.isSpectator()) {
                event.setCanceled(true);
            }
        } else if (event.getOverlay().id().equals(VanillaGuiOverlay.EXPERIENCE_BAR.id())) {
            minecraft.player.getCapability(HunterPlayerDataProvider.CAPABILITY).ifPresent(data -> {
                if (data.isCombatBarVisible() && !minecraft.player.isSpectator()) {
                    event.setCanceled(true);
                }
            });
        } else if (event.getOverlay().id().equals(VanillaGuiOverlay.ARMOR_LEVEL.id())
                || event.getOverlay().id().equals(VanillaGuiOverlay.FOOD_LEVEL.id())) {
            minecraft.player.getCapability(HunterPlayerDataProvider.CAPABILITY).ifPresent(data -> {
                if (data.isCombatBarVisible() && !minecraft.player.isSpectator()) {
                    if (minecraft.gui instanceof ForgeGui forgeGui) {
                        int combatHudHeight = VANILLA_COMBAT_HUD_HEIGHT + (hasTwoCombatBarsOnScreen() ? DUAL_BAR_HUD_Y_SHIFT : 0);
                        forgeGui.leftHeight = Math.max(forgeGui.leftHeight, combatHudHeight);
                        forgeGui.rightHeight = Math.max(forgeGui.rightHeight, combatHudHeight);
                    }
                    if (event.getOverlay().id().equals(VanillaGuiOverlay.FOOD_LEVEL.id())) {
                        event.getGuiGraphics().pose().pushPose();
                        event.getGuiGraphics().pose().translate(FOOD_BAR_COMBAT_X_OFFSET, 0.0F, 0.0F);
                        foodOverlayTranslated = true;
                    }
                }
            });
        }
    }

    @SubscribeEvent
    public static void onRenderOverlayFoodPost(RenderGuiOverlayEvent.Post event) {
        if (foodOverlayTranslated && event.getOverlay().id().equals(VanillaGuiOverlay.FOOD_LEVEL.id())) {
            event.getGuiGraphics().pose().popPose();
            foodOverlayTranslated = false;
        }
    }
}
