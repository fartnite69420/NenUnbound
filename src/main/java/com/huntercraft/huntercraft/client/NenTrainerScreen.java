package com.huntercraft.huntercraft.client;

import com.huntercraft.huntercraft.data.HunterPlayerData;
import com.huntercraft.huntercraft.data.HunterPlayerDataProvider;
import com.huntercraft.huntercraft.network.HunterNetwork;
import com.huntercraft.huntercraft.network.packet.AdvanceNenQuestPacket;
import com.huntercraft.huntercraft.quest.NenQuestStage;
import com.huntercraft.huntercraft.quest.NenQuestUtil;
import com.huntercraft.huntercraft.quest.NenType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;

public class NenTrainerScreen extends Screen {
    private static final int PANEL_WIDTH = 376;
    private static final int PANEL_HEIGHT = 264;
    private static final int HATSU_NODE_RADIUS = 18;
    private final int trainerEntityId;
    private NenQuestStage lastStage;

    public NenTrainerScreen(int trainerEntityId, boolean zushi) {
        super(NenQuestUtil.getTrainerTitle(false));
        this.trainerEntityId = trainerEntityId;
    }

    @Override
    protected void init() {
        this.clearWidgets();
        int left = this.width / 2 - PANEL_WIDTH / 2;
        int top = this.height / 2 - PANEL_HEIGHT / 2;
        int actionY = top + 232;
        this.addRenderableWidget(Button.builder(Component.literal("Close"), button -> this.onClose())
                .bounds(left + 296, top + 14, 68, 20)
                .build());

        HunterPlayerData data = getLocalData();
        if (data == null) {
            return;
        }

        NenQuestStage stage = data.getNenQuestStage();
        this.lastStage = stage;
        if (stage == NenQuestStage.NOT_STARTED) {
            this.addRenderableWidget(actionButton("Begin Training", left + 18, actionY, 144, "begin", ""));
        } else if (stage == NenQuestStage.FEEL_THE_AURA && !data.isMeditationCountdownActive() && !data.isMeditationActive() && !NenQuestUtil.isStageComplete(data)) {
            this.addRenderableWidget(actionButton("Start Meditation", left + 18, actionY, 144, "start_meditation", ""));
        } else if (stage == NenQuestStage.OPEN_THE_NODES && !data.hasNodeDamageTaken()) {
            this.addRenderableWidget(actionButton("Open the Nodes", left + 18, actionY, 144, "open_nodes", ""));
        } else if (stage == NenQuestStage.ZETSU_DISAPPEAR && !data.isZetsuTrialRunning() && !data.isZetsuTrialComplete()) {
            this.addRenderableWidget(actionButton("Start Hide Trial", left + 18, actionY, 144, "start_zetsu", ""));
        } else if (stage == NenQuestStage.HATSU_CHOOSE_TYPE) {
            addHatsuButtons(left, top);
        } else if (stage == NenQuestStage.RYU_SHIFT_AURA && !data.isRyuFightFinished()) {
            this.addRenderableWidget(actionButton(data.isRyuFightStarted() ? "Trial Running" : "Start Spar", left + 18, actionY, 144, "start_ryu", ""));
        }

        if (NenQuestUtil.isStageComplete(data)) {
            this.addRenderableWidget(actionButton("Advance", left + 220, actionY, 144, "claim", ""));
        }
    }

    @Override
    public void tick() {
        super.tick();
        HunterPlayerData data = getLocalData();
        if (data == null) {
            return;
        }
        NenQuestStage stage = data.getNenQuestStage();
        if (stage != this.lastStage) {
            this.init();
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        this.lastStage = null;
        super.onClose();
    }

    private void addHatsuButtons(int left, int top) {
        for (HatsuButton button : getHatsuButtons(left, top)) {
            if (button.locked()) {
                continue;
            }
            int width = 108;
            int x = button.x() - (width / 2);
            int y = button.y() - 11;
            this.addRenderableWidget(actionButton(button.type().displayName(), x, y, width, "choose_type", button.id()));
        }
    }

    private Button actionButton(String label, int x, int y, int width, String action, String choice) {
        return Button.builder(Component.literal(label), button -> {
                    HunterNetwork.sendToServer(new AdvanceNenQuestPacket(action, this.trainerEntityId, choice));
                    if ("start_meditation".equals(action)) {
                        this.onClose();
                    }
                    button.active = false;
                })
                .bounds(x, y, width, 20)
                .build();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        HunterPlayerData data = getLocalData();
        if (data != null && data.getNenQuestStage() == NenQuestStage.HATSU_CHOOSE_TYPE) {
            renderHatsuSelection(graphics, mouseX, mouseY, data);
            super.render(graphics, mouseX, mouseY, partialTick);
            return;
        }

        renderDefaultTrainer(graphics, mouseX, mouseY, partialTick, data);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private void renderDefaultTrainer(GuiGraphics graphics, int mouseX, int mouseY, float partialTick, HunterPlayerData data) {
        this.renderBackground(graphics);
        int left = this.width / 2 - PANEL_WIDTH / 2;
        int top = this.height / 2 - PANEL_HEIGHT / 2;
        int right = left + PANEL_WIDTH;
        int bottom = top + PANEL_HEIGHT;
        int accent = 0xFFF3C986;

        graphics.fill(left, top, right, bottom, 0xF0091118);
        graphics.fill(left, top, right, top + 30, 0xFF172332);
        graphics.fill(left + 14, top + 44, left + 180, top + 194, 0x9E121A24);
        graphics.fill(left + 196, top + 44, right - 14, top + 194, 0x9E121A24);
        graphics.drawString(this.font, this.title, left + 14, top + 11, 0xFFFFFF, false);

        if (data == null) {
            return;
        }

        NenQuestStage stage = data.getNenQuestStage();
        Component stageName = NenQuestUtil.getStageName(stage);
        Component dialogue = NenQuestUtil.getDialogue(false, stage);
        Component objective = NenQuestUtil.getObjective(data);
        Component progress = NenQuestUtil.getProgress(data);
        Component hint = NenQuestUtil.getStageHint(data);
        Component reward = NenQuestUtil.getRewardLine(data);

        graphics.drawString(this.font, Component.literal("Wing"), left + 28, top + 56, accent, false);
        graphics.drawString(this.font, stageName, left + 28, top + 72, 0xFFFFFF, false);
        graphics.drawWordWrap(this.font, dialogue, left + 28, top + 92, 138, 0xD6E2EE);

        graphics.drawString(this.font, Component.literal("Objective"), left + 210, top + 56, 0x8FC7FF, false);
        graphics.drawWordWrap(this.font, objective, left + 210, top + 72, 138, 0xFFFFFF);
        graphics.drawString(this.font, Component.literal("Hint"), left + 210, top + 108, 0x8FC7FF, false);
        int hintY = top + 124;
        int hintWidth = 138;
        graphics.drawWordWrap(this.font, hint, left + 210, hintY, hintWidth, 0xC5D6E6);
        int hintHeight = this.font.split(hint, hintWidth).size() * this.font.lineHeight;
        int rewardLabelY = hintY + hintHeight + 10;
        int rewardTextY = rewardLabelY + 16;
        graphics.drawString(this.font, Component.literal("Reward"), left + 210, rewardLabelY, 0x8FC7FF, false);
        graphics.drawWordWrap(this.font, reward, left + 210, rewardTextY, hintWidth, 0x7DDCA8);

        int rewardHeight = this.font.split(reward, hintWidth).size() * this.font.lineHeight;
        int progressBarX = left + 18;
        int progressBarY = Math.max(top + 214, rewardTextY + rewardHeight + 14);
        int progressBarWidth = 346;
        float percent = getStageCompletionPercent(data);
        graphics.fill(progressBarX, progressBarY, progressBarX + progressBarWidth, progressBarY + 10, 0xAA121A24);
        graphics.fill(progressBarX, progressBarY, progressBarX + Mth.floor(progressBarWidth * percent), progressBarY + 10, 0xFF5FD39A);
        graphics.drawString(this.font, Component.literal("Progress: " + progress.getString()), progressBarX, progressBarY - 12, NenQuestUtil.isStageComplete(data) ? 0x7DDCA8 : 0xF0D58C, false);
    }

    private void renderHatsuSelection(GuiGraphics graphics, int mouseX, int mouseY, HunterPlayerData data) {
        this.renderBackground(graphics);
        int left = this.width / 2 - PANEL_WIDTH / 2;
        int top = this.height / 2 - PANEL_HEIGHT / 2;
        int right = left + PANEL_WIDTH;
        int bottom = top + PANEL_HEIGHT;
        int centerX = left + PANEL_WIDTH / 2;
        int centerY = top + 132;

        graphics.fill(left, top, right, bottom, 0xF20A1018);
        graphics.fill(left, top, right, top + 30, 0xFF172332);
        graphics.drawString(this.font, this.title, left + 14, top + 11, 0xFFFFFF, false);
        graphics.drawCenteredString(this.font, Component.literal("HATSU AFFINITY"), centerX, top + 36, 0xFFFFFF);
        graphics.drawCenteredString(this.font, Component.literal("Choose the Nen type your Hatsu will follow."), centerX, top + 50, 0xD8E4EF);

        List<HatsuButton> buttons = getHatsuButtons(left, top);
        renderHatsuConnections(graphics, buttons);
        renderHatsuCenter(graphics, centerX, centerY);

        HatsuButton hovered = null;
        for (HatsuButton button : buttons) {
            renderHatsuNode(graphics, button);
            int radius = button.locked() ? 14 : HATSU_NODE_RADIUS;
            if (isPointInsideNode(mouseX, mouseY, button.x(), button.y(), radius)) {
                hovered = button;
            }
        }

        if (hovered == null && data.getNenType() != null) {
            for (HatsuButton button : buttons) {
                if (button.type() == data.getNenType()) {
                    hovered = button;
                    break;
                }
            }
        }

        String description = hovered != null
                ? NenQuestUtil.getHatsuDescription(hovered.type()).getString()
                : "Select the path your aura naturally leans toward.";
        graphics.drawCenteredString(this.font, Component.literal(description), centerX, top + 214, 0xD6E2EE);

        NenType chosenType = data.getNenType();
        String progressText = chosenType == null ? "Progress: No type chosen" : "Progress: " + chosenType.displayName() + " chosen";
        graphics.drawString(this.font, Component.literal(progressText), left + 18, bottom - 18, chosenType == null ? 0xF0D58C : 0x7DDCA8, false);
    }

    private void renderHatsuConnections(GuiGraphics graphics, List<HatsuButton> buttons) {
        HatsuButton enhancement = buttons.get(0);
        HatsuButton emission = buttons.get(1);
        HatsuButton transmutation = buttons.get(2);
        HatsuButton manipulation = buttons.get(3);
        HatsuButton conjuration = buttons.get(4);
        HatsuButton specialization = buttons.get(5);

        drawLine(graphics, enhancement.x(), enhancement.y(), transmutation.x(), transmutation.y(), 0xFF10151B);
        drawLine(graphics, transmutation.x(), transmutation.y(), conjuration.x(), conjuration.y(), 0xFF10151B);
        drawLine(graphics, conjuration.x(), conjuration.y(), specialization.x(), specialization.y(), 0xFF10151B);
        drawLine(graphics, specialization.x(), specialization.y(), manipulation.x(), manipulation.y(), 0xFF10151B);
        drawLine(graphics, manipulation.x(), manipulation.y(), emission.x(), emission.y(), 0xFF10151B);
        drawLine(graphics, emission.x(), emission.y(), enhancement.x(), enhancement.y(), 0xFF10151B);
    }

    private void drawLine(GuiGraphics graphics, int x1, int y1, int x2, int y2, int color) {
        int steps = Math.max(Math.abs(x2 - x1), Math.abs(y2 - y1));
        for (int i = 0; i <= steps; i++) {
            float progress = steps == 0 ? 0.0F : (float) i / (float) steps;
            int x = Mth.floor(Mth.lerp(progress, x1, x2));
            int y = Mth.floor(Mth.lerp(progress, y1, y2));
            graphics.fill(x - 1, y - 1, x + 1, y + 1, color);
        }
    }

    private void renderHatsuCenter(GuiGraphics graphics, int centerX, int centerY) {
        graphics.drawCenteredString(this.font, Component.literal("\u767a"), centerX, centerY - 10, 0xFFFFFFFF);
    }

    private void renderHatsuNode(GuiGraphics graphics, HatsuButton button) {
        if (button.locked()) {
            drawLockIcon(graphics, button.x(), button.y(), 0xFFFFFFFF);
        } else {
            int color = 0xFF000000 | button.type().color();
            graphics.fill(button.x() - HATSU_NODE_RADIUS, button.y() - HATSU_NODE_RADIUS, button.x() + HATSU_NODE_RADIUS, button.y() + HATSU_NODE_RADIUS, color);
            graphics.fill(button.x() - HATSU_NODE_RADIUS + 2, button.y() - HATSU_NODE_RADIUS + 2, button.x() + HATSU_NODE_RADIUS - 2, button.y() + HATSU_NODE_RADIUS - 2, color);
        }
        drawHatsuNodeLabel(graphics, button);
    }

    private void drawHatsuNodeLabel(GuiGraphics graphics, HatsuButton button) {
        int y;
        if (button.type() == NenType.ENHANCEMENT) {
            y = button.y() - 34;
        } else if (button.type() == NenType.SPECIALIZATION) {
            y = button.y() + 22;
        } else {
            y = button.y() - 5;
        }
        graphics.drawCenteredString(this.font, Component.literal(button.type().displayName()), button.x(), y, button.locked() ? 0xA8B7C9 : 0xFFFFFF);
    }

    private void drawLockIcon(GuiGraphics graphics, int centerX, int centerY, int color) {
        graphics.fill(centerX - 10, centerY - 3, centerX + 10, centerY + 13, color);
        graphics.fill(centerX - 7, centerY - 10, centerX - 3, centerY - 3, color);
        graphics.fill(centerX + 3, centerY - 10, centerX + 7, centerY - 3, color);
        graphics.fill(centerX - 7, centerY - 12, centerX + 7, centerY - 8, color);
        graphics.fill(centerX - 2, centerY + 2, centerX + 2, centerY + 9, 0xFF172332);
        graphics.fill(centerX - 1, centerY + 1, centerX + 1, centerY + 3, 0xFF172332);
    }

    private boolean isPointInsideNode(int mouseX, int mouseY, int centerX, int centerY, int radius) {
        int dx = mouseX - centerX;
        int dy = mouseY - centerY;
        return (dx * dx) + (dy * dy) <= (radius * radius);
    }

    private List<HatsuButton> getHatsuButtons(int left, int top) {
        List<HatsuButton> buttons = new ArrayList<>();
        buttons.add(new HatsuButton("ENHANCEMENT", NenType.ENHANCEMENT, left + 188, top + 96, false));
        buttons.add(new HatsuButton("EMISSION", NenType.EMISSION, left + 111, top + 138, false));
        buttons.add(new HatsuButton("TRANSMUTATION", NenType.TRANSMUTATION, left + 265, top + 138, false));
        buttons.add(new HatsuButton("MANIPULATION", NenType.MANIPULATION, left + 111, top + 204, false));
        buttons.add(new HatsuButton("CONJURATION", NenType.CONJURATION, left + 265, top + 204, false));
        buttons.add(new HatsuButton("SPECIALIZATION", NenType.SPECIALIZATION, left + 188, top + 234, true));
        return buttons;
    }

    private float getStageCompletionPercent(HunterPlayerData data) {
        return switch (data.getNenQuestStage()) {
            case FEEL_THE_AURA -> Mth.clamp((float) data.getFeelAuraTicks() / NenQuestUtil.FEEL_AURA_TICKS_REQUIRED, 0.0F, 1.0F);
            case OPEN_THE_NODES -> data.hasNodeDamageTaken() ? 1.0F : 0.0F;
            case TEN_ENDURE -> Mth.clamp((float) data.getEndureLowHpTicks() / NenQuestUtil.TEN_ENDURE_TICKS_REQUIRED, 0.0F, 1.0F);
            case ZETSU_DISAPPEAR -> {
                if (data.isZetsuTrialComplete()) {
                    yield 1.0F;
                }
                if (data.isZetsuTrialSearching()) {
                    yield Mth.clamp(1.0F - ((float) data.getZetsuTrialSearchTicks() / NenQuestUtil.ZETSU_HIDE_SEARCH_TICKS), 0.0F, 1.0F);
                }
                if (data.getZetsuTrialPrepTicks() > 0) {
                    yield Mth.clamp(1.0F - ((float) data.getZetsuTrialPrepTicks() / NenQuestUtil.ZETSU_HIDE_PREP_TICKS), 0.0F, 1.0F);
                }
                yield 0.0F;
            }
            case REN_OVERFLOW -> Mth.clamp(data.getRenOverflowDamageWindow() / NenQuestUtil.REN_OVERFLOW_DAMAGE_REQUIRED, 0.0F, 1.0F);
            case REN_AURA_BURST -> Mth.clamp((float) data.getAuraBurstKills() / NenQuestUtil.AURA_BURST_KILLS_REQUIRED, 0.0F, 1.0F);
            case HATSU_CHOOSE_TYPE -> data.getNenType() == null ? 0.0F : 1.0F;
            case EN_LOOK -> Mth.clamp((float) data.getEnLookedMobCount() / NenQuestUtil.EN_LOOK_REQUIRED, 0.0F, 1.0F);
            case SHU_REN_WEAPON -> Mth.clamp((float) data.getShuWeaponRenTicks() / NenQuestUtil.SHU_REN_WEAPON_TICKS_REQUIRED, 0.0F, 1.0F);
            case KO_ONE_SHOT -> Mth.clamp((float) data.getKoOneShotKills() / NenQuestUtil.KO_ONE_SHOT_REQUIRED, 0.0F, 1.0F);
            case KEN_BALANCE -> Mth.clamp((float) data.getKenBalanceTicks() / NenQuestUtil.KEN_BALANCE_TICKS_REQUIRED, 0.0F, 1.0F);
            case RYU_SHIFT_AURA, COMPLETED -> NenQuestUtil.isStageComplete(data) ? 1.0F : 0.0F;
            default -> 0.0F;
        };
    }

    private HunterPlayerData getLocalData() {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null) {
            return null;
        }
        return player.getCapability(HunterPlayerDataProvider.CAPABILITY).resolve().orElse(null);
    }

    private record HatsuButton(String id, NenType type, int x, int y, boolean locked) {
    }
}
