package com.huntercraft.huntercraft.client;

import com.huntercraft.huntercraft.data.HunterPlayerData;
import com.huntercraft.huntercraft.data.HunterPlayerDataProvider;
import com.huntercraft.huntercraft.network.HunterNetwork;
import com.huntercraft.huntercraft.network.packet.AcceptPhoneQuestPacket;
import com.huntercraft.huntercraft.network.packet.CancelPhoneQuestPacket;
import com.huntercraft.huntercraft.quest.PhoneJobDefinition;
import com.huntercraft.huntercraft.quest.QuestDefinition;
import com.huntercraft.huntercraft.quest.QuestObjectiveType;
import com.huntercraft.huntercraft.quest.QuestRegistry;
import com.huntercraft.huntercraft.quest.PhoneQuestRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;

public class PhoneQuestScreen extends Screen {
    private static final int PANEL_PADDING = 16;
    private static final int CARD_HEIGHT = 58;
    private static final int CARD_GAP = 8;

    private final List<PhoneJobDefinition> quests = PhoneQuestRegistry.all();
    private Tab selectedTab;

    public PhoneQuestScreen() {
        this(Tab.JOB_BOARD);
    }

    public PhoneQuestScreen(Tab selectedTab) {
        super(Component.translatable("screen.huntercraft.phone"));
        this.selectedTab = selectedTab;
    }

    @Override
    protected void init() {
        this.clearWidgets();
        this.addRenderableWidget(Button.builder(Component.literal("Close"), button -> this.onClose())
                .bounds(this.width - 92, 16, 76, 20)
                .build());
        this.addRenderableWidget(Button.builder(Component.translatable("screen.huntercraft.phone.tab.jobs"), button -> switchTab(Tab.JOB_BOARD))
                .bounds(24, 16, 92, 20)
                .build());
        this.addRenderableWidget(Button.builder(Component.translatable("screen.huntercraft.phone.tab.active"), button -> switchTab(Tab.ACTIVE_QUESTS))
                .bounds(122, 16, 112, 20)
                .build());

        int panelX = 18;
        int panelY = 18;
        int panelWidth = this.width - 36;
        int cardWidth = panelWidth - (PANEL_PADDING * 2);
        int cardX = panelX + PANEL_PADDING;
        if (this.selectedTab == Tab.JOB_BOARD) {
            int cardY = panelY + 64;
            boolean refreshing = isPhoneQuestRefreshing();
            for (PhoneJobDefinition quest : this.quests) {
                Button button = Button.builder(getQuestButtonLabel(quest), pressed -> acceptQuest(quest))
                        .bounds(cardX + cardWidth - 86, cardY + 17, 74, 20)
                        .build();
                if (refreshing && !isQuestActive(quest.id())) {
                    button.active = false;
                }
                this.addRenderableWidget(button);
                cardY += CARD_HEIGHT + CARD_GAP;
            }
        } else {
            int activeCardY = panelY + 64;
            for (ActiveQuestEntry entry : buildActiveQuestEntries(getLocalData())) {
                this.addRenderableWidget(Button.builder(Component.literal("Cancel"), button -> cancelQuest(entry.id()))
                        .bounds(cardX + cardWidth - 86, activeCardY + 36, 74, 20)
                        .build());
                activeCardY += 76;
            }
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        int panelX = 18;
        int panelY = 18;
        int panelWidth = this.width - 36;
        int panelHeight = this.height - 36;
        graphics.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, 0xEE0A1119);
        graphics.fill(panelX, panelY, panelX + panelWidth, panelY + 24, 0xFF1B2733);
        graphics.drawString(this.font, Component.translatable("screen.huntercraft.phone.title"), panelX + PANEL_PADDING, panelY + 8, 0xFFFFFF, false);
        if (this.selectedTab == Tab.JOB_BOARD) {
            graphics.drawString(this.font, Component.translatable("screen.huntercraft.phone.subtitle"), panelX + PANEL_PADDING, panelY + 32, 0xA9C8E4, false);
            if (isPhoneQuestRefreshing()) {
                graphics.drawString(this.font, Component.translatable("screen.huntercraft.phone.note_refresh", formatRefreshRemaining()), panelX + PANEL_PADDING, panelY + 45, 0xF3C986, false);
            } else {
                graphics.drawString(this.font, Component.translatable("screen.huntercraft.phone.note"), panelX + PANEL_PADDING, panelY + 45, 0xF3C986, false);
            }

            int cardX = panelX + PANEL_PADDING;
            int cardY = panelY + 64;
            int cardWidth = panelWidth - (PANEL_PADDING * 2);
            for (PhoneJobDefinition quest : this.quests) {
                renderQuestCard(graphics, quest, cardX, cardY, cardWidth, mouseX, mouseY);
                cardY += CARD_HEIGHT + CARD_GAP;
            }
        } else {
            renderActiveQuestTab(graphics, panelX, panelY, panelWidth, panelHeight);
        }

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private void renderQuestCard(GuiGraphics graphics, PhoneJobDefinition quest, int x, int y, int width, int mouseX, int mouseY) {
        int accent = 0xFF000000 | quest.accentColor();
        graphics.fill(x, y, x + width, y + CARD_HEIGHT, 0xBB101722);
        graphics.fill(x, y, x + 4, y + CARD_HEIGHT, accent);
        graphics.fill(x + 4, y, x + width, y + 2, 0x1EFFFFFF);

        Component title = Component.translatable(quest.titleKey());
        Component difficulty = Component.translatable("screen.huntercraft.phone.difficulty", quest.difficultyLabel());
        HunterPlayerData data = getLocalData();
        int scaledMinReward = getScaledMinReward(quest, data);
        int scaledMaxReward = getScaledMaxReward(quest, data);
        Component reward = Component.translatable("screen.huntercraft.phone.reward", scaledMinReward, scaledMaxReward);
        Component description = Component.translatable(quest.descriptionKey());
        Component status = getQuestStatus(quest);

        graphics.drawString(this.font, title, x + 12, y + 8, 0xFFFFFF, false);
        graphics.drawString(this.font, difficulty, x + width - 170, y + 8, accent, false);
        graphics.drawString(this.font, reward, x + 12, y + 21, 0xF3C986, false);
        graphics.drawString(this.font, status, x + width - 170, y + 21, getQuestStatusColor(quest), false);

        List<FormattedCharSequence> wrapped = this.font.split(description, width - 116);
        int lineY = y + 34;
        for (int i = 0; i < Math.min(2, wrapped.size()); i++) {
            graphics.drawString(this.font, wrapped.get(i), x + 12, lineY, 0xC9D8E8, false);
            lineY += 10;
        }

        if (mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + CARD_HEIGHT) {
            graphics.fill(x, y, x + width, y + CARD_HEIGHT, 0x14FFFFFF);
        }
    }

    private void acceptQuest(PhoneJobDefinition quest) {
        HunterNetwork.sendToServer(new AcceptPhoneQuestPacket(quest.id()));
        this.minecraft.setScreen(new PhoneQuestScreen(Tab.JOB_BOARD));
    }

    private void switchTab(Tab tab) {
        this.minecraft.setScreen(new PhoneQuestScreen(tab));
    }

    private void cancelQuest(String questId) {
        HunterNetwork.sendToServer(new CancelPhoneQuestPacket(questId));
        this.minecraft.setScreen(new PhoneQuestScreen(Tab.ACTIVE_QUESTS));
    }

    private boolean isPhoneQuestRefreshing() {
        HunterPlayerData data = getLocalData();
        Minecraft minecraft = Minecraft.getInstance();
        if (data == null || minecraft.level == null) {
            return false;
        }
        return data.isPhoneQuestRefreshing(minecraft.level.getGameTime());
    }

    private boolean isQuestActive(String questId) {
        HunterPlayerData data = getLocalData();
        return data != null && data.getActiveQuests().contains(questId);
    }

    private String formatRefreshRemaining() {
        HunterPlayerData data = getLocalData();
        Minecraft minecraft = Minecraft.getInstance();
        if (data == null || minecraft.level == null) {
            return "1 day";
        }
        long ticks = data.getPhoneQuestRefreshRemaining(minecraft.level.getGameTime());
        long totalMinutes = Math.max(1L, Math.round((ticks / 1000.0D) * 60.0D));
        long hours = totalMinutes / 60L;
        long minutes = totalMinutes % 60L;
        if (hours <= 0L) {
            return minutes + "m";
        }
        return hours + "h " + minutes + "m";
    }

    private Component getQuestButtonLabel(PhoneJobDefinition quest) {
        HunterPlayerData data = getLocalData();
        if (data == null) {
            return Component.literal("Accept");
        }
        if (isPhoneQuestRefreshing() && !data.getActiveQuests().contains(quest.id())) {
            return Component.literal("Refresh");
        }
        if (PhoneQuestRegistry.isPhoneQuest(quest.id()) && data.getActiveQuests().stream().anyMatch(PhoneQuestRegistry::isPhoneQuest) && !data.getActiveQuests().contains(quest.id())) {
            return Component.literal("Locked");
        }
        if (data.getCompletedQuests().contains(quest.id())) {
            return Component.literal("Done");
        }
        if (data.getActiveQuests().contains(quest.id())) {
            return Component.literal("Active");
        }
        return Component.literal("Accept");
    }

    private Component getQuestStatus(PhoneJobDefinition quest) {
        HunterPlayerData data = getLocalData();
        if (data == null) {
            return Component.translatable("screen.huntercraft.phone.status.available");
        }
        if (data.getCompletedQuests().contains(quest.id())) {
            return Component.translatable("screen.huntercraft.phone.status.completed");
        }
        if (data.getActiveQuests().contains(quest.id())) {
            return Component.translatable("screen.huntercraft.phone.status.active");
        }
        return Component.translatable("screen.huntercraft.phone.status.available");
    }

    private int getQuestStatusColor(PhoneJobDefinition quest) {
        HunterPlayerData data = getLocalData();
        if (data == null) {
            return 0x7FD7A1;
        }
        if (data.getCompletedQuests().contains(quest.id())) {
            return 0x7FD7A1;
        }
        if (data.getActiveQuests().contains(quest.id())) {
            return 0xF1C97E;
        }
        return 0x9DC4E6;
    }

    private HunterPlayerData getLocalData() {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null) {
            return null;
        }
        return player.getCapability(HunterPlayerDataProvider.CAPABILITY).resolve().orElse(null);
    }

    private void renderActiveQuestTab(GuiGraphics graphics, int panelX, int panelY, int panelWidth, int panelHeight) {
        graphics.drawString(this.font, Component.translatable("screen.huntercraft.phone.active_subtitle"), panelX + PANEL_PADDING, panelY + 32, 0xA9C8E4, false);
        graphics.drawString(this.font, Component.translatable("screen.huntercraft.phone.active_note"), panelX + PANEL_PADDING, panelY + 45, 0xF3C986, false);

        HunterPlayerData data = getLocalData();
        int cardX = panelX + PANEL_PADDING;
        int cardY = panelY + 64;
        int cardWidth = panelWidth - (PANEL_PADDING * 2);

        if (data == null || data.getActiveQuests().isEmpty()) {
            graphics.fill(cardX, cardY, cardX + cardWidth, cardY + 64, 0xAA101722);
            graphics.drawString(this.font, Component.translatable("screen.huntercraft.phone.active_empty"), cardX + 12, cardY + 12, 0xFFFFFF, false);
            graphics.drawWordWrap(this.font, Component.translatable("screen.huntercraft.phone.active_empty.desc"), cardX + 12, cardY + 28, cardWidth - 24, 0xC9D8E8);
            return;
        }

        for (ActiveQuestEntry entry : buildActiveQuestEntries(data)) {
            graphics.fill(cardX, cardY, cardX + cardWidth, cardY + 68, 0xBB101722);
            graphics.fill(cardX, cardY, cardX + 4, cardY + 68, 0xFF4C8FB7);
            graphics.drawString(this.font, entry.title(), cardX + 12, cardY + 8, 0xFFFFFF, false);
            graphics.drawString(this.font, entry.progress(), cardX + cardWidth - 96, cardY + 8, 0xF1C97E, false);
            graphics.drawString(this.font, entry.objective(), cardX + 12, cardY + 22, 0xA9C8E4, false);
            graphics.drawString(this.font, entry.target(), cardX + 12, cardY + 34, 0xFFFFFF, false);
            graphics.drawString(this.font, entry.location(), cardX + 12, cardY + 48, 0xC9D8E8, false);
            graphics.drawString(this.font, Component.translatable("screen.huntercraft.phone.active_reward", entry.rewardXp()), cardX + cardWidth - 96, cardY + 22, 0x7FD7A1, false);
            cardY += 76;
            if (cardY + 68 > panelY + panelHeight) {
                break;
            }
        }
    }

    private List<ActiveQuestEntry> buildActiveQuestEntries(HunterPlayerData data) {
        List<ActiveQuestEntry> entries = new ArrayList<>();
        if (data == null) {
            return entries;
        }
        for (String questId : data.getActiveQuests()) {
            QuestDefinition definition = QuestRegistry.byId(questId);
            if (definition == null) {
                continue;
            }
            int progress = data.getQuestProgress().getOrDefault(questId, 0);
            entries.add(new ActiveQuestEntry(
                    questId,
                    Component.translatable(definition.titleKey()),
                    Component.translatable("screen.huntercraft.phone.active_progress", progress, data.getQuestTargetCount(questId)),
                    getObjectiveLine(definition),
                    getTargetLine(definition, data.getQuestTargetCount(questId)),
                    getLocationLine(data.getQuestLocation(questId)),
                    data.getQuestRewardXp(questId)
            ));
        }
        return entries;
    }

    private Component getObjectiveLine(QuestDefinition definition) {
        if (definition.objectiveType() == QuestObjectiveType.KILL_ENTITY) {
            return Component.translatable("screen.huntercraft.phone.objective.kill");
        }
        return Component.translatable("screen.huntercraft.phone.objective.collect");
    }

    private Component getTargetLine(QuestDefinition definition, int targetCount) {
        return Component.translatable("screen.huntercraft.phone.target", resolveTargetName(definition), targetCount);
    }

    private Component getLocationLine(BlockPos pos) {
        if (pos == null) {
            return Component.translatable("screen.huntercraft.phone.location.none");
        }
        return Component.translatable("screen.huntercraft.phone.location.coords", pos.getX(), pos.getY(), pos.getZ());
    }

    private Component resolveTargetName(QuestDefinition definition) {
        ResourceLocation id = ResourceLocation.tryParse(definition.targetId());
        if (id == null) {
            return Component.literal(definition.targetId());
        }
        if (definition.objectiveType() == QuestObjectiveType.KILL_ENTITY) {
            if (ForgeRegistries.ENTITY_TYPES.containsKey(id)) {
                return ForgeRegistries.ENTITY_TYPES.getValue(id).getDescription();
            }
        } else if (ForgeRegistries.ITEMS.containsKey(id)) {
            Item item = ForgeRegistries.ITEMS.getValue(id);
            if (item != null) {
                return item.getDescription();
            }
        }
        return Component.literal(id.toString());
    }

    public enum Tab {
        JOB_BOARD,
        ACTIVE_QUESTS
    }

    private int getScaledMinReward(PhoneJobDefinition quest, HunterPlayerData data) {
        if (data == null) {
            return quest.minRewardXp();
        }
        return QuestRegistry.getScaledRewardXp(QuestRegistry.byId(quest.id()), data.getLevel());
    }

    private int getScaledMaxReward(PhoneJobDefinition quest, HunterPlayerData data) {
        if (data == null) {
            return quest.maxRewardXp();
        }
        return Math.max(getScaledMinReward(quest, data), (int) Math.round(quest.maxRewardXp() * (1.0D + (Math.max(0, data.getLevel() - 1) * 0.035D))));
    }

    private record ActiveQuestEntry(String id, Component title, Component progress, Component objective, Component target, Component location, int rewardXp) {
    }
}
