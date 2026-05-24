package com.huntercraft.huntercraft.client;

import com.huntercraft.huntercraft.HunterCraftMod;
import com.huntercraft.huntercraft.abilities.HunterAbilities;
import com.huntercraft.huntercraft.abilities.SkillTreeAbility;
import com.huntercraft.huntercraft.abilities.SkillTreeCombatAbility;
import com.huntercraft.huntercraft.abilities.nenability.NenTechniqueAbility;
import com.huntercraft.huntercraft.ability.HunterAbility;
import com.huntercraft.huntercraft.data.HunterPlayerData;
import com.huntercraft.huntercraft.data.HunterPlayerDataProvider;
import com.huntercraft.huntercraft.network.HunterNetwork;
import com.huntercraft.huntercraft.network.packet.CreateFactionPacket;
import com.huntercraft.huntercraft.network.packet.DisbandFactionPacket;
import com.huntercraft.huntercraft.network.packet.InviteFactionPlayerPacket;
import com.huntercraft.huntercraft.network.packet.LeaveFactionPacket;
import com.huntercraft.huntercraft.network.packet.RequestFactionViewPacket;
import com.huntercraft.huntercraft.network.packet.RespondFactionInvitePacket;
import com.huntercraft.huntercraft.network.packet.SetNenAuraColorPacket;
import com.huntercraft.huntercraft.network.packet.SetScarletEyesOffsetPacket;
import com.huntercraft.huntercraft.network.packet.SetCombatVowPacket;
import com.huntercraft.huntercraft.network.packet.SetVowFactionPacket;
import com.huntercraft.huntercraft.network.packet.SelectJudgmentAbilityPacket;
import com.huntercraft.huntercraft.network.packet.SwitchCombatBarPacket;
import com.huntercraft.huntercraft.network.packet.ToggleEmptyHandsPickupPacket;
import com.huntercraft.huntercraft.network.packet.UpdateCombatSlotPacket;
import com.huntercraft.huntercraft.progression.SkillNode;
import com.huntercraft.huntercraft.progression.NenTechniqueSkillNode;
import com.huntercraft.huntercraft.quest.QuestDefinition;
import com.huntercraft.huntercraft.quest.QuestRegistry;
import com.huntercraft.huntercraft.quest.NenType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class HunterMenuScreen extends Screen {
    private static final String[] JUDGMENT_CHAIN_CHOICES = {
            "gum_attach", "elastic_pull", "texture_surprise", "elastic_reflect", "elastic_trap",
            "dowsing_chain", "holy_chain", "chain_jail", "steal_chain", "judgment_chain",
            "smokey_chain", "smoke_soldier", "smoke_clone", "smokey_jail"
    };
    private static final int PANEL_MARGIN = 20;
    private static final int SLOT_SIZE = 28;
    private static final int SLOT_GAP = 4;
    private static final int CLEAR_ZONE_WIDTH = 88;
    private static final int CLEAR_ZONE_HEIGHT = 28;
    private static final int ABILITY_CARD_WIDTH = 136;
    private static final int ABILITY_CARD_HEIGHT = 40;
    private static final int ABILITY_CARD_GAP_X = 12;
    private static final int ABILITY_CARD_GAP_Y = 10;
    private static final int SKILL_ENTRY_HEIGHT = 42;
    private static final int SKILL_ENTRY_ICON_SIZE = 16;
    private static final int SKILL_ENTRY_ICON_TEXTURE_SIZE = 64;
    private static final int SLOT_DROP_PADDING = 8;
    private static final int ABILITIES_SWITCH_BUTTON_Y = PANEL_MARGIN + 68;
    private static final int ABILITIES_CONTENT_TOP = PANEL_MARGIN + 88;

    private Tab activeTab = Tab.SKILL_TREE;
    private SkillNode.Category selectedCategory = SkillNode.Category.WEAPON;
    private SkillNode selectedStyle = SkillNode.SPEED;
    private SkillTreeAbility selectedTreeAbility;
    private String draggingAbilityId = "";
    private int draggingFromSlot = -1;
    private int dragMouseX;
    private int dragMouseY;
    private EditBox factionNameBox;
    private ColorSlider redSlider;
    private ColorSlider greenSlider;
    private ColorSlider blueSlider;
    private NenTechniqueSkillNode selectedNenTechniqueNode;
    private EditBox vowFactionBox;
    private EditBox combatVowPercentBox;
    private Integer previousGuiScale;
    private boolean applyingFixedGuiScale;
    private boolean draggingScarletEyes;
    private int draggingScarletEye = -1;

    public HunterMenuScreen() {
        super(Component.empty());
    }

    @Override
    protected void init() {
        applyFixedGuiScale();
        this.clearWidgets();
        this.factionNameBox = null;
        this.redSlider = null;
        this.greenSlider = null;
        this.blueSlider = null;
        this.vowFactionBox = null;
        this.combatVowPercentBox = null;
        if (this.activeTab == Tab.NEN_TECHNIQUE && getData().getNenTechniqueId().isBlank()) {
            this.activeTab = Tab.NEN;
        }
        if (this.activeTab == Tab.NEN_VOWS && !getData().hasNen()) {
            this.activeTab = Tab.NEN;
        }
        addTabButtons();
        if (this.activeTab == Tab.SKILL_TREE) {
            addSkillTreeButtons();
        } else if (this.activeTab == Tab.ABILITIES) {
            addAbilitiesButtons();
        } else if (this.activeTab == Tab.PLAYER_INFO) {
            addPlayerInfoWidgets();
        } else if (this.activeTab == Tab.NEN) {
            addNenWidgets();
        } else if (this.activeTab == Tab.NEN_TECHNIQUE) {
            addNenTechniqueWidgets();
        } else if (this.activeTab == Tab.NEN_VOWS) {
            addNenVowWidgets();
        } else if (this.activeTab == Tab.FACTION) {
            addFactionWidgets();
            HunterNetwork.sendToServer(new RequestFactionViewPacket());
        }
    }

    @Override
    public void removed() {
        restoreGuiScale();
        super.removed();
    }

    private void applyFixedGuiScale() {
        if (this.minecraft == null || this.applyingFixedGuiScale || this.previousGuiScale != null) {
            return;
        }
        int currentGuiScale = this.minecraft.options.guiScale().get();
        if (currentGuiScale == 2) {
            return;
        }
        this.previousGuiScale = currentGuiScale;
        this.applyingFixedGuiScale = true;
        this.minecraft.options.guiScale().set(2);
        this.minecraft.resizeDisplay();
        this.applyingFixedGuiScale = false;
    }

    private void restoreGuiScale() {
        if (this.minecraft == null || this.previousGuiScale == null || this.applyingFixedGuiScale) {
            return;
        }
        this.applyingFixedGuiScale = true;
        this.minecraft.options.guiScale().set(this.previousGuiScale);
        this.previousGuiScale = null;
        this.minecraft.resizeDisplay();
        this.applyingFixedGuiScale = false;
    }

    private void addTabButtons() {
        int tabY = PANEL_MARGIN;
        int buttonWidth = 96;
        int gap = 10;
        List<Tab> visibleTabs = getVisibleTabs();
        int totalWidth = (buttonWidth * visibleTabs.size()) + (gap * (visibleTabs.size() - 1));
        int startX = (this.width - totalWidth) / 2;
        for (int i = 0; i < visibleTabs.size(); i++) {
            Tab tab = visibleTabs.get(i);
            Button button = Button.builder(Component.literal(tab.label), value -> {
                        this.activeTab = tab;
                        this.init();
                    })
                    .bounds(startX + i * (buttonWidth + gap), tabY, buttonWidth, 20)
                    .build();
            button.active = this.activeTab != tab;
            this.addRenderableWidget(button);
        }
    }

    private void addSkillTreeButtons() {
        HunterPlayerData data = getData();
        SkillNode.Category lockedCategory = data.getSelectedSkillCategory();
        if (lockedCategory != null) {
            this.selectedCategory = lockedCategory;
            if (this.selectedStyle.category() != lockedCategory) {
                this.selectedStyle = getNodesForCategory(lockedCategory)[0];
            }
        }

        int contentTop = PANEL_MARGIN + 42;
        int leftWidth = 164;
        int columnsLeft = PANEL_MARGIN + 10 + leftWidth + 12;
        SkillNode[] nodes = getNodesForCategory(this.selectedCategory);
        int columnWidth = getColumnWidth(columnsLeft, nodes.length);
        int categoryButtonY = contentTop - 28;
        int categoryButtonWidth = 90;
        int categoryGap = 10;
        for (int i = 0; i < SkillNode.Category.values().length; i++) {
            SkillNode.Category category = SkillNode.Category.values()[i];
            Button button = Button.builder(Component.translatable(category.translationKey()), value -> {
                        this.selectedCategory = category;
                        SkillNode[] categoryNodes = getNodesForCategory(category);
                        if (this.selectedStyle.category() != category && categoryNodes.length > 0) {
                            this.selectedStyle = categoryNodes[0];
                            this.selectedTreeAbility = null;
                        }
                        this.init();
                    })
                    .bounds(columnsLeft + i * (categoryButtonWidth + categoryGap), categoryButtonY, categoryButtonWidth, 20)
                    .build();
            button.active = lockedCategory == null || lockedCategory == category;
            this.addRenderableWidget(button);
        }

        for (int i = 0; i < nodes.length; i++) {
            SkillNode node = nodes[i];
            int columnX = columnsLeft + i * (columnWidth + 8);
            int buttonY = contentTop + 8;
            this.addRenderableWidget(Button.builder(Component.literal("+"), value -> {
                        this.selectedStyle = node;
                        ClientHooks.adjustSkillPoints(node, 1);
                    }).bounds(columnX + columnWidth - 24, buttonY, 18, 18).build());
            this.addRenderableWidget(Button.builder(Component.literal("-"), value -> {
                        this.selectedStyle = node;
                        ClientHooks.adjustSkillPoints(node, -1);
                    }).bounds(columnX + columnWidth - 46, buttonY, 18, 18).build());
        }
    }

    private void addFactionWidgets() {
        HunterPlayerData data = getData();
        int panelX = PANEL_MARGIN + 24;
        int panelY = PANEL_MARGIN + 58;
        int panelWidth = this.width - (PANEL_MARGIN * 2) - 48;
        if (!data.hasFaction()) {
            this.factionNameBox = new EditBox(this.font, panelX + 14, panelY + 74, 180, 20, Component.literal("Faction Name"));
            this.factionNameBox.setMaxLength(20);
            this.addRenderableWidget(this.factionNameBox);
            this.addRenderableWidget(Button.builder(Component.literal("Create Faction"), value -> {
                        if (this.factionNameBox != null && !this.factionNameBox.getValue().isBlank()) {
                            HunterNetwork.sendToServer(new CreateFactionPacket(this.factionNameBox.getValue()));
                        }
                    }).bounds(panelX + 204, panelY + 74, 110, 20).build());
            if (data.hasPendingFactionInvite()) {
                this.addRenderableWidget(Button.builder(Component.literal("Accept"), value ->
                                HunterNetwork.sendToServer(new RespondFactionInvitePacket(true)))
                        .bounds(panelX + 14, panelY + 182, 90, 20).build());
                this.addRenderableWidget(Button.builder(Component.literal("Decline"), value ->
                                HunterNetwork.sendToServer(new RespondFactionInvitePacket(false)))
                        .bounds(panelX + 112, panelY + 182, 90, 20).build());
            }
            return;
        }
        this.addRenderableWidget(Button.builder(Component.literal(data.isFactionOwner() ? "Disband Faction" : "Leave Faction"), value ->
                        HunterNetwork.sendToServer(data.isFactionOwner() ? new DisbandFactionPacket() : new LeaveFactionPacket()))
                .bounds(panelX + 14, panelY + 84, 120, 20)
                .build());
        if (!data.isFactionOwner()) {
            return;
        }
        List<String> invitablePlayers = new ArrayList<>(data.getInvitablePlayers());
        invitablePlayers.sort(String::compareToIgnoreCase);
        int startY = panelY + 132;
        int buttonWidth = Math.min(220, panelWidth - 28);
        for (int i = 0; i < Math.min(8, invitablePlayers.size()); i++) {
            String playerName = invitablePlayers.get(i);
            this.addRenderableWidget(Button.builder(Component.literal(playerName), value ->
                            HunterNetwork.sendToServer(new InviteFactionPlayerPacket(playerName)))
                    .bounds(panelX + 14, startY + (i * 24), buttonWidth, 20).build());
        }
    }

    private void addAbilitiesButtons() {
        HunterPlayerData data = getData();
        int buttonY = ABILITIES_SWITCH_BUTTON_Y;
        this.addRenderableWidget(Button.builder(Component.literal("Bar 1"), value -> switchCombatBar(0))
                .bounds(PANEL_MARGIN + 10, buttonY, 54, 20)
                .build());
        this.addRenderableWidget(Button.builder(Component.literal("Bar 2"), value -> switchCombatBar(1))
                .bounds(PANEL_MARGIN + 70, buttonY, 54, 20)
                .build());
        this.addRenderableWidget(Button.builder(Component.literal("Swap"), value ->
                        switchCombatBar((data.getActiveCombatBar() + 1) % HunterPlayerData.COMBAT_BAR_COUNT))
                .bounds(PANEL_MARGIN + 130, buttonY, 54, 20)
                .build());
        this.addRenderableWidget(Button.builder(Component.literal("Empty Hands: " + (data.isEmptyHandsPickupEnabled() ? "ON" : "OFF")), value ->
                        toggleEmptyHandsPickup())
                .bounds(PANEL_MARGIN + 194, buttonY, 132, 20)
                .build());
    }

    private void addPlayerInfoWidgets() {
        HunterPlayerData data = getData();
        if (!data.hasScarletEyesTrait()) {
            return;
        }
    }

    private void addNenWidgets() {
        HunterPlayerData data = getData();
        int x = PANEL_MARGIN + 38;
        int y = PANEL_MARGIN + 288;
        int red = (data.getNenAuraColor() >> 16) & 0xFF;
        int green = (data.getNenAuraColor() >> 8) & 0xFF;
        int blue = data.getNenAuraColor() & 0xFF;
        this.redSlider = this.addRenderableWidget(new ColorSlider(x, y, 180, 20, "Red", red));
        this.greenSlider = this.addRenderableWidget(new ColorSlider(x, y + 24, 180, 20, "Green", green));
        this.blueSlider = this.addRenderableWidget(new ColorSlider(x, y + 48, 180, 20, "Blue", blue));
        this.redSlider.active = data.hasNen();
        this.greenSlider.active = data.hasNen();
        this.blueSlider.active = data.hasNen();
    }

    private void addNenVowWidgets() {
        HunterPlayerData data = getData();
        int panelX = PANEL_MARGIN + 24;
        int panelY = PANEL_MARGIN + 58;
        this.combatVowPercentBox = new EditBox(this.font, panelX + 14, panelY + 122, 54, 20, Component.literal("Percent"));
        this.combatVowPercentBox.setMaxLength(2);
        this.combatVowPercentBox.setValue(Integer.toString(data.getCombatVowPercent()));
        this.addRenderableWidget(this.combatVowPercentBox);
        this.addRenderableWidget(Button.builder(Component.literal("Vow of Speed"), value ->
                        HunterNetwork.sendToServer(new SetCombatVowPacket("speed", readCombatVowPercent())))
                .bounds(panelX + 78, panelY + 122, 104, 20)
                .build());
        this.addRenderableWidget(Button.builder(Component.literal("Vow of Strength"), value ->
                        HunterNetwork.sendToServer(new SetCombatVowPacket("strength", readCombatVowPercent())))
                .bounds(panelX + 190, panelY + 122, 118, 20)
                .build());
        this.addRenderableWidget(Button.builder(Component.literal("Clear"), value ->
                        HunterNetwork.sendToServer(new SetCombatVowPacket("", 25)))
                .bounds(panelX + 316, panelY + 122, 62, 20)
                .build());
        if (data.hasChainTechnique()) {
            this.vowFactionBox = new EditBox(this.font, panelX + 14, panelY + 250, 210, 20, Component.literal("Vowed Players"));
            this.vowFactionBox.setMaxLength(64);
            this.vowFactionBox.setValue(data.getAbilityVowFaction("chain_jail"));
            this.addRenderableWidget(this.vowFactionBox);
            this.addRenderableWidget(Button.builder(Component.literal("Save Chain Hatsu Vow"), value ->
                            HunterNetwork.sendToServer(new SetVowFactionPacket("chain_hatsu", this.vowFactionBox == null ? "" : this.vowFactionBox.getValue())))
                    .bounds(panelX + 236, panelY + 250, 142, 20)
                    .build());
        }
        if (!data.getPendingJudgmentChainTargetUuid().isBlank()) {
            int startY = panelY + 306;
            for (int i = 0; i < JUDGMENT_CHAIN_CHOICES.length; i++) {
                HunterAbility ability = HunterAbilities.byId(JUDGMENT_CHAIN_CHOICES[i]);
                if (!(ability instanceof SkillTreeCombatAbility combatAbility)) {
                    continue;
                }
                int column = i % 2;
                int row = i / 2;
                this.addRenderableWidget(Button.builder(Component.literal(combatAbility.displayName()), value ->
                                HunterNetwork.sendToServer(new SelectJudgmentAbilityPacket(combatAbility.id())))
                        .bounds(panelX + 14 + (column * 170), startY + (row * 23), 156, 20)
                        .build());
            }
        }
    }

    private void addNenTechniqueWidgets() {
        HunterPlayerData data = getData();
        if (data.getNenTechniqueId().isBlank()) {
            return;
        }
        NenTechniqueSkillNode[] nodes = NenTechniqueSkillNode.forTechnique(data.getNenTechniqueId());
        if (this.selectedNenTechniqueNode == null || !this.selectedNenTechniqueNode.techniqueId().equalsIgnoreCase(data.getNenTechniqueId())) {
            this.selectedNenTechniqueNode = nodes.length > 0 ? nodes[0] : null;
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        HunterHudStyle.panel(graphics, PANEL_MARGIN, PANEL_MARGIN + 28, this.width - (PANEL_MARGIN * 2), this.height - (PANEL_MARGIN * 2) - 28);
        switch (this.activeTab) {
            case SKILL_TREE -> renderSkillTree(graphics, mouseX, mouseY);
            case ABILITIES -> renderAbilities(graphics, mouseX, mouseY);
            case PLAYER_INFO -> renderPlayerInfo(graphics);
            case NEN -> renderNen(graphics);
            case NEN_TECHNIQUE -> renderNenTechnique(graphics, mouseX, mouseY);
            case NEN_VOWS -> renderNenVows(graphics);
            case FACTION -> renderFaction(graphics);
        }
        super.render(graphics, mouseX, mouseY, partialTick);
        renderHoveredAbilityTooltip(graphics, mouseX, mouseY);
    }

    private void renderSkillTree(GuiGraphics graphics, int mouseX, int mouseY) {
        HunterPlayerData data = getData();
        int contentTop = PANEL_MARGIN + 42;
        int contentBottom = this.height - PANEL_MARGIN - 12;
        int leftWidth = 164;
        int leftX = PANEL_MARGIN + 10;
        int leftY = contentTop;
        SkillNode.Category lockedCategory = data.getSelectedSkillCategory();
        if (lockedCategory != null) {
            this.selectedCategory = lockedCategory;
            if (this.selectedStyle.category() != lockedCategory) {
                this.selectedStyle = getNodesForCategory(lockedCategory)[0];
                this.selectedTreeAbility = null;
            }
        }

        HunterHudStyle.recessedPanel(graphics, leftX, leftY, leftWidth, contentBottom - leftY);
        graphics.drawString(this.font, Component.literal("DESCRIPTION"), leftX + 12, leftY + 12, 0xFFFFFF, false);
        graphics.drawString(this.font, Component.literal("Skill Points: " + data.getSkillPoints()), leftX + 12, leftY + 34, 0xB6D7F2, false);
        graphics.drawString(this.font, Component.translatable(this.selectedCategory.translationKey()), leftX + 12, leftY + 52, 0xF3B86F, false);
        graphics.drawWordWrap(this.font, Component.literal(this.selectedCategory.description()), leftX + 12, leftY + 68, leftWidth - 24, 0xD3D8DE);
        String categoryPassive = this.selectedCategory == SkillNode.Category.WEAPON
                ? HunterAbilities.WEAPON_MASTERY.displayName()
                : HunterAbilities.PHYSICAL_POWER.displayName();
        graphics.drawString(this.font, Component.literal(categoryPassive), leftX + 12, leftY + 102, 0xF3B86F, false);

        String selectedTitle = this.selectedTreeAbility != null
                ? this.selectedTreeAbility.displayName()
                : Component.translatable(this.selectedStyle.translationKey()).getString();
        String selectedDescription = this.selectedTreeAbility != null
                ? this.selectedTreeAbility.description()
                : this.selectedStyle.description();
        int detailTextWidth = leftWidth - 24;
        int selectedDescriptionY = leftY + 148;
        Component selectedDescriptionComponent = Component.literal(selectedDescription);
        graphics.drawString(this.font, Component.literal(selectedTitle), leftX + 12, leftY + 132, this.selectedStyle.color(), false);
        graphics.drawWordWrap(this.font, selectedDescriptionComponent, leftX + 12, selectedDescriptionY, detailTextWidth, 0xD3D8DE);
        int requirementsLabelY = Math.max(leftY + 200, selectedDescriptionY + getWrappedTextHeight(selectedDescriptionComponent, detailTextWidth) + 10);
        graphics.drawString(this.font, Component.literal("REQUIREMENTS"), leftX + 12, requirementsLabelY, 0xFFFFFF, false);
        String requirementText = this.selectedTreeAbility != null
                ? this.selectedTreeAbility.requiredPoints() + " points in " + Component.translatable(this.selectedStyle.translationKey()).getString()
                : "Choose one category path";
        int requirementTextY = requirementsLabelY + 16;
        Component requirementComponent = Component.literal(requirementText);
        graphics.drawWordWrap(this.font, requirementComponent, leftX + 12, requirementTextY, detailTextWidth, 0xC8D7E6);
        int footerY = requirementTextY + getWrappedTextHeight(requirementComponent, detailTextWidth) + 8;
        graphics.drawString(this.font, Component.literal("Points can be refunded"), leftX + 12, footerY, 0xC8D7E6, false);
        graphics.drawString(this.font, Component.literal("Current: " + data.getStylePoints(this.selectedStyle)), leftX + 12, footerY + 12, this.selectedStyle.color(), false);
        graphics.drawString(this.font, Component.literal(lockedCategory == null ? "No category locked yet" : "Locked: " + Component.translatable(lockedCategory.translationKey()).getString()), leftX + 12, footerY + 24, lockedCategory == null ? 0xB6D7F2 : 0xF5A623, false);

        int columnsLeft = leftX + leftWidth + 12;
        SkillNode[] nodes = getNodesForCategory(this.selectedCategory);
        int columnWidth = getColumnWidth(columnsLeft, nodes.length);
        for (int i = 0; i < nodes.length; i++) {
            SkillNode node = nodes[i];
            int columnX = columnsLeft + i * (columnWidth + 8);
            int headerColor = 0xFF000000 | node.color();
            HunterHudStyle.recessedPanel(graphics, columnX, contentTop, columnWidth, contentBottom - contentTop);
            if (node == this.selectedStyle) {
                graphics.fill(columnX + 2, contentTop + 26, columnX + columnWidth - 2, contentBottom - 2, 0x301E91B9);
            }
            graphics.fill(columnX, contentTop, columnX + columnWidth, contentTop + 24, headerColor);
            graphics.drawCenteredString(this.font, Component.translatable(node.translationKey()).append(" - " + data.getStylePoints(node)), columnX + (columnWidth / 2), contentTop + 8, 0xFFFFFF);
            renderSkillEntries(graphics, node, columnX + 12, contentTop + 38, columnWidth - 24);
        }
    }

    private void renderSkillEntries(GuiGraphics graphics, SkillNode node, int x, int y, int width) {
        HunterPlayerData data = getData();
        for (SkillTreeAbility entry : HunterAbilities.TREE_ABILITIES_BY_NODE.getOrDefault(node, List.of())) {
            boolean unlocked = entry.isUnlocked(data);
            boolean selected = entry == this.selectedTreeAbility;
            int tileColor = selected ? 0x663A4652 : unlocked ? 0x331B2733 : 0x22080D13;
            int titleColor = unlocked ? node.color() : 0xC8D7E6;
            int iconX = x + (width - SKILL_ENTRY_ICON_SIZE) / 2;
            graphics.fill(x, y, x + width, y + SKILL_ENTRY_HEIGHT - 4, tileColor);
            HunterHudStyle.thinFrame(graphics, x, y, width, SKILL_ENTRY_HEIGHT - 4, 0x8805080D, selected ? 0xAAF3B86F : 0x55384552);
            renderSkillEntryIcon(graphics, new ResourceLocation(HunterCraftMod.MODID, entry.iconPath()), iconX, y + 3);
            graphics.drawCenteredString(this.font, Component.literal(entry.displayName()), x + (width / 2), y + 21, titleColor);
            graphics.drawCenteredString(this.font, Component.literal(entry.requiredPoints() + " pts"), x + (width / 2), y + 31, 0x8D98A4);
            y += SKILL_ENTRY_HEIGHT;
        }
    }

    private void renderSkillEntryIcon(GuiGraphics graphics, ResourceLocation icon, int x, int y) {
        com.mojang.blaze3d.vertex.PoseStack pose = graphics.pose();
        float scale = (float) SKILL_ENTRY_ICON_SIZE / SKILL_ENTRY_ICON_TEXTURE_SIZE;
        pose.pushPose();
        pose.translate(x, y, 0.0F);
        pose.scale(scale, scale, 1.0F);
        graphics.blit(icon, 0, 0, 0, 0, SKILL_ENTRY_ICON_TEXTURE_SIZE, SKILL_ENTRY_ICON_TEXTURE_SIZE, SKILL_ENTRY_ICON_TEXTURE_SIZE, SKILL_ENTRY_ICON_TEXTURE_SIZE);
        pose.popPose();
    }

    private void renderAbilities(GuiGraphics graphics, int mouseX, int mouseY) {
        HunterPlayerData data = getData();
        int contentTop = ABILITIES_CONTENT_TOP;
        int poolX = PANEL_MARGIN + 10;
        int poolY = contentTop;
        int poolWidth = this.width - 2 * PANEL_MARGIN - 20;
        int poolHeight = this.height - contentTop - PANEL_MARGIN - 72;
        HunterHudStyle.recessedPanel(graphics, poolX, poolY, poolWidth, poolHeight);
        graphics.drawString(this.font, Component.literal("Drag abilities onto the combat bar."), poolX + 12, poolY + 12, 0xF3B86F, false);
        graphics.drawString(this.font, Component.literal("Base techniques stay on the left bar only."), poolX + 12, poolY + 26, 0xB6D7F2, false);
        graphics.drawString(this.font, Component.literal("Empty Hands keeps ground pickups out of your hotbar."), poolX + 12, poolY + 40, 0xC8D7E6, false);

        List<SkillTreeCombatAbility> abilities = HunterAbilities.unlockedCombatAbilities(data);
        int cardsY = poolY + 66;
        graphics.drawString(this.font, Component.literal("Editing Bar " + (data.getActiveCombatBar() + 1)), poolX + 12, poolY + 52, 0xB6D7F2, false);
        if (abilities.isEmpty()) {
            graphics.drawString(this.font, Component.literal("Unlock combat or Nen abilities to show them here."), poolX + 12, cardsY, 0xC8D7E6, false);
        }
        for (int i = 0; i < abilities.size(); i++) {
            SkillTreeCombatAbility ability = abilities.get(i);
            int cardX = getAbilityCardX(i);
            int cardY = getAbilityCardY(i);
            renderAbilityCard(graphics, ability, cardX, cardY, isInside(mouseX, mouseY, cardX, cardY, ABILITY_CARD_WIDTH, ABILITY_CARD_HEIGHT), ability.isUnlocked(data));
        }
        renderSlotBar(graphics, data, mouseX, mouseY, false);
        renderClearZone(graphics, mouseX, mouseY);
        if (!this.draggingAbilityId.isBlank()) {
            HunterAbility dragged = HunterAbilities.byId(this.draggingAbilityId);
            if (dragged != null) {
                renderAbilityCard(graphics, dragged, mouseX - (ABILITY_CARD_WIDTH / 2), mouseY - (ABILITY_CARD_HEIGHT / 2), true);
            }
        }
    }

    private void renderPlayerInfo(GuiGraphics graphics) {
        HunterPlayerData data = getData();
        int panelX = PANEL_MARGIN + 24;
        int panelY = PANEL_MARGIN + 58;
        int panelWidth = this.width - (PANEL_MARGIN * 2) - 48;
        int panelHeight = this.height - panelY - PANEL_MARGIN - 12;
        HunterHudStyle.recessedPanel(graphics, panelX, panelY, panelWidth, panelHeight);
        graphics.drawString(this.font, Component.literal("PLAYER INFO"), panelX + 14, panelY + 14, 0xFFFFFF, false);
        drawInfoCard(graphics, panelX + 14, panelY + 42, 156, 58, "Level", String.valueOf(data.getLevel()), 0xF3B86F);
        drawInfoCard(graphics, panelX + 182, panelY + 42, 156, 58, "XP", data.getXp() + " / " + data.getXpToNextLevel(), 0x88E0C2);
        drawInfoCard(graphics, panelX + 350, panelY + 42, 156, 58, "Skill Points", String.valueOf(data.getSkillPoints()), 0xB6D7F2);
        drawInfoCard(graphics, panelX + 14, panelY + 112, 156, 70, "Bonus Health", String.format(Locale.ROOT, "%.0f", data.getBonusHealth()), 0xF7A9B4);
        drawInfoCard(graphics, panelX + 182, panelY + 112, 156, 70, "Passive Armor", String.format(Locale.ROOT, "%.1f", data.getPassiveArmor()), 0xD6D9E0);
        drawInfoCard(graphics, panelX + 350, panelY + 112, 156, 70, "Nen Toughness", String.format(Locale.ROOT, "%.0f%%", data.getPassiveToughnessReduction() * 100.0F), 0x88E0C2);
        drawInfoCard(graphics, panelX + 14, panelY + 194, 156, 58, "Trait", data.getTraitDisplayName(), 0xD93A3A);
        drawInfoCard(graphics, panelX + 182, panelY + 194, 156, 58, "Emperor Time", data.isEmperorTimeActive() ? "Active" : "Inactive", data.isEmperorTimeActive() ? 0xB11226 : 0x8D98A4);
        drawInfoCard(graphics, panelX + 14, panelY + 264, 240, 58, "Faction", getFactionInfo(data), data.hasFaction() ? 0xF3B86F : 0x8D98A4);
        renderQuestProgressSummary(graphics, data, panelX + 350, panelY + 194, Math.max(180, panelWidth - 364), 96);
        if (data.hasScarletEyesTrait()) {
            graphics.drawString(this.font, Component.literal("Scarlet Eyes Editor"), panelX + 14, panelY + 336, 0xFFFFFF, false);
            graphics.drawString(this.font, Component.literal("Drag on the face preview to place the red eye overlay."), panelX + 14, panelY + 352, 0xC8D7E6, false);
            renderScarletEyesEditor(graphics, data, panelX + 14, panelY + 374);
        }
    }

    private void renderNen(GuiGraphics graphics) {
        HunterPlayerData data = getData();
        NenType nenType = data.getNenType();
        int panelX = PANEL_MARGIN + 24;
        int panelY = PANEL_MARGIN + 58;
        int panelWidth = this.width - (PANEL_MARGIN * 2) - 48;
        int panelHeight = this.height - panelY - PANEL_MARGIN - 12;
        HunterHudStyle.recessedPanel(graphics, panelX, panelY, panelWidth, panelHeight);
        graphics.drawString(this.font, Component.literal("NEN"), panelX + 14, panelY + 14, 0xFFFFFF, false);
        drawInfoCard(graphics, panelX + 14, panelY + 42, 156, 58, "Nen Level", String.valueOf(data.getNenLevel()), 0xC98BFF);
        drawInfoCard(graphics, panelX + 182, panelY + 42, 156, 58, "Stamina", data.getCurrentStamina() + " / " + data.getMaxStamina(), 0xF2B13E);
        drawInfoCard(graphics, panelX + 350, panelY + 42, 156, 58, "Aura Color", String.format("#%06X", data.getNenAuraColor()), 0xFF000000 | data.getNenAuraColor());
        drawInfoCard(graphics, panelX + 14, panelY + 112, 156, 58, "Nen Type", nenType == null ? "Unchosen" : nenType.displayName(), nenType == null ? 0x8D98A4 : 0xFF000000 | nenType.color());
        drawInfoCard(graphics, panelX + 182, panelY + 112, 156, 58, "Hatsu Technique", data.getNenTechniqueDisplayName(), data.hasDeepPurpleTechnique() ? 0xA1B7C7 : data.hasChainTechnique() ? 0xD93A3A : 0x8D98A4);
        graphics.drawString(this.font, Component.literal("Nen Progression"), panelX + 14, panelY + 184, 0xFFFFFF, false);
        graphics.drawWordWrap(this.font, Component.literal("Hatsu techniques are now unlocked through Wing's training instead of directly from Nen level. Zetsu still suppresses the rest while active."), panelX + 14, panelY + 200, panelWidth - 28, 0xC8D7E6);
        graphics.drawString(this.font, Component.literal("Aura Color"), panelX + 14, panelY + 262, 0xFFFFFF, false);
        int previewX = PANEL_MARGIN + 248;
        int previewY = PANEL_MARGIN + 288;
        graphics.drawString(this.font, Component.literal("Preview"), previewX, previewY - 16, 0xFFFFFF, false);
        HunterHudStyle.abilitySlot(graphics, previewX, previewY, 72, 0xFF000000 | data.getNenAuraColor(), false);
        graphics.fill(previewX + 8, previewY + 8, previewX + 64, previewY + 64, 0xFF000000 | data.getNenAuraColor());
    }

    private void renderNenTechnique(GuiGraphics graphics, int mouseX, int mouseY) {
        HunterPlayerData data = getData();
        int panelX = PANEL_MARGIN + 24;
        int panelY = PANEL_MARGIN + 58;
        int panelWidth = this.width - (PANEL_MARGIN * 2) - 48;
        int panelHeight = this.height - panelY - PANEL_MARGIN - 12;
        HunterHudStyle.recessedPanel(graphics, panelX, panelY, panelWidth, panelHeight);
        graphics.drawString(this.font, Component.literal("HATSU TECHNIQUE"), panelX + 14, panelY + 14, 0xFFFFFF, false);

        if (data.getNenTechniqueId().isBlank()) {
            graphics.drawWordWrap(this.font, Component.literal("You do not have a Hatsu technique yet. Once a technique is chosen, its personal skill path will appear here."), panelX + 14, panelY + 42, panelWidth - 28, 0xC8D7E6);
            return;
        }

        graphics.drawString(this.font, Component.literal(data.getNenTechniqueDisplayName()), panelX + 14, panelY + 40, 0xA1B7C7, false);
        graphics.drawString(this.font, Component.literal("Technique Points: " + data.getAvailableNenTechniquePoints()), panelX + 14, panelY + 56, 0xF3B86F, false);

        int infoX = panelX + 14;
        int infoY = panelY + 82;
        int infoWidth = 232;
        int infoHeight = panelHeight - 96;
        HunterHudStyle.recessedPanel(graphics, infoX, infoY, infoWidth, infoHeight);
        NenTechniqueSkillNode selectedNode = this.selectedNenTechniqueNode;
        if (selectedNode == null) {
            NenTechniqueSkillNode[] nodes = NenTechniqueSkillNode.forTechnique(data.getNenTechniqueId());
            selectedNode = nodes.length > 0 ? nodes[0] : null;
            this.selectedNenTechniqueNode = selectedNode;
        }
        if (selectedNode != null) {
            boolean unlocked = data.hasUnlockedNenTechniqueNode(selectedNode);
            boolean unlockable = data.canUnlockNenTechniqueNode(selectedNode);
            graphics.drawString(this.font, Component.literal(selectedNode.displayName().toUpperCase(Locale.ROOT)), infoX + 12, infoY + 12, unlocked ? 0xFFFFFF : 0xD3D8DE, false);
            String statusText = unlocked ? "Unlocked"
                    : unlockable ? "Ready to unlock"
                    : "Requires Nen Level " + selectedNode.requiredNenLevel();
            int statusColor = unlocked ? 0x88E0C2 : unlockable ? 0xF3B86F : 0x8D98A4;
            graphics.drawString(this.font, Component.literal(statusText), infoX + 12, infoY + 30, statusColor, false);
            graphics.drawWordWrap(this.font, Component.literal(selectedNode.description()), infoX + 12, infoY + 52, infoWidth - 24, 0xC8D7E6);
            String requirementText = selectedNode.order() == 0
                    ? "Unlocked automatically when you choose " + data.getNenTechniqueDisplayName() + "."
                    : "Unlocks automatically at Nen Level " + selectedNode.requiredNenLevel() + ".";
            graphics.drawWordWrap(this.font, Component.literal(requirementText), infoX + 12, infoY + 134, infoWidth - 24, 0x8D98A4);
        }

        int treeLeft = panelX + 286;
        int treeRight = panelX + panelWidth - 42;
        int nodeY = panelY + 190;
        NenTechniqueSkillNode[] nodes = NenTechniqueSkillNode.forTechnique(data.getNenTechniqueId());
        if (nodes.length == 0) {
            return;
        }
        int spacing = nodes.length == 1 ? 0 : (treeRight - treeLeft) / (nodes.length - 1);
        for (int i = 0; i < nodes.length - 1; i++) {
            int startX = treeLeft + (i * spacing);
            int endX = treeLeft + ((i + 1) * spacing);
            graphics.fill(startX + 24, nodeY + 15, endX - 24, nodeY + 18, 0x55D5DDE5);
        }
        for (int i = 0; i < nodes.length; i++) {
            NenTechniqueSkillNode node = nodes[i];
            int nodeX = treeLeft + (i * spacing);
            int size = 42;
            boolean hovered = isInside(mouseX, mouseY, nodeX, nodeY, size, size);
            boolean unlocked = data.hasUnlockedNenTechniqueNode(node);
            boolean unlockable = data.canUnlockNenTechniqueNode(node);
            int frameColor = unlocked ? 0xFFDDE4EC : unlockable ? 0xFFF3B86F : 0xFF4A5561;
            int fillColor = unlocked ? 0xCCDAE2EA : unlockable ? 0xCC2A3138 : 0xCC151A20;
            if (hovered) {
                fillColor = unlocked ? 0xFFE8EDF3 : unlockable ? 0xCC38424C : 0xCC222A33;
            }
            HunterHudStyle.abilitySlot(graphics, nodeX, nodeY, size, frameColor, hovered);
            graphics.fill(nodeX + 6, nodeY + 6, nodeX + size - 6, nodeY + size - 6, fillColor);
            graphics.drawCenteredString(this.font, Component.literal("Lv." + node.requiredNenLevel()), nodeX + (size / 2), nodeY + 8, unlocked ? 0x10161C : 0xFFFFFF);
            graphics.drawCenteredString(this.font, Component.literal(node.displayName()), nodeX + (size / 2), nodeY + 54, hovered || selectedNode == node ? 0xFFFFFF : 0xC8D7E6);
        }
    }

    private void renderNenVows(GuiGraphics graphics) {
        HunterPlayerData data = getData();
        int panelX = PANEL_MARGIN + 24;
        int panelY = PANEL_MARGIN + 58;
        int panelWidth = this.width - (PANEL_MARGIN * 2) - 48;
        int panelHeight = this.height - panelY - PANEL_MARGIN - 12;
        HunterHudStyle.recessedPanel(graphics, panelX, panelY, panelWidth, panelHeight);
        graphics.drawString(this.font, Component.literal("NEN VOWS"), panelX + 14, panelY + 14, 0xFFFFFF, false);
        String combatVowName = data.hasSpeedVow() ? "Vow of Speed" : data.hasStrengthVow() ? "Vow of Strength" : "Unset";
        int combatColor = data.hasSpeedVow() ? 0x88E0C2 : data.hasStrengthVow() ? 0xF3B86F : 0x8D98A4;
        drawInfoCard(graphics, panelX + 14, panelY + 42, 200, 58, "Combat Vow", combatVowName, combatColor);
        drawInfoCard(graphics, panelX + 226, panelY + 42, 200, 58, "Vow Percent", data.hasSpeedVow() || data.hasStrengthVow() ? data.getCombatVowPercent() + "%" : "25-75%", combatColor);
        graphics.drawString(this.font, Component.literal("Percent"), panelX + 14, panelY + 104, 0xFFFFFF, false);
        graphics.drawWordWrap(this.font, Component.literal("Speed shortens cooldowns and weakens damage by the chosen percent. Strength increases damage and lengthens cooldowns by the chosen percent. Only one can be active."), panelX + 14, panelY + 156, panelWidth - 28, 0xC8D7E6);
        if (!data.hasChainTechnique()) {
            graphics.drawWordWrap(this.font, Component.literal("Chain Hatsu vows appear here when Chain Style is your Hatsu technique."), panelX + 14, panelY + 202, panelWidth - 28, 0x8D98A4);
            return;
        }
        String vow = data.getAbilityVowFaction("chain_jail");
        drawInfoCard(graphics, panelX + 14, panelY + 190, 200, 48, "Chain Hatsu Vow", vow.isBlank() ? "Unset" : vow, 0xF3B86F);
        graphics.drawString(this.font, Component.literal("Vowed Players"), panelX + 14, panelY + 232, 0xFFFFFF, false);
        graphics.drawWordWrap(this.font, Component.literal("Type up to 3 player names separated by commas. Chain Hatsu vow effects only fully apply against those players."), panelX + 14, panelY + 278, panelWidth - 28, 0xC8D7E6);
        if (!data.getPendingJudgmentChainTargetUuid().isBlank()) {
            graphics.drawString(this.font, Component.literal("Judgment Chain Target"), panelX + 14, panelY + 288, 0xF3B86F, false);
            graphics.drawString(this.font, Component.literal("Choose one Hatsu ability to bind until death."), panelX + 14, panelY + 300, 0xC8D7E6, false);
        }
    }

    private int readCombatVowPercent() {
        if (this.combatVowPercentBox == null) {
            return 25;
        }
        try {
            return Integer.parseInt(this.combatVowPercentBox.getValue().trim());
        } catch (NumberFormatException ignored) {
            return 25;
        }
    }

    private void renderFaction(GuiGraphics graphics) {
        HunterPlayerData data = getData();
        int panelX = PANEL_MARGIN + 24;
        int panelY = PANEL_MARGIN + 58;
        int panelWidth = this.width - (PANEL_MARGIN * 2) - 48;
        int panelHeight = this.height - panelY - PANEL_MARGIN - 12;
        HunterHudStyle.recessedPanel(graphics, panelX, panelY, panelWidth, panelHeight);
        graphics.drawString(this.font, Component.literal("FACTION"), panelX + 14, panelY + 14, 0xFFFFFF, false);
        if (!data.hasFaction()) {
            graphics.drawString(this.font, Component.literal("No faction yet"), panelX + 14, panelY + 40, 0xF3B86F, false);
            graphics.drawWordWrap(this.font, Component.literal("Create a faction or accept an invitation. Joining only works through invites from an existing faction leader."), panelX + 14, panelY + 54, panelWidth - 28, 0xC8D7E6);
            if (data.hasPendingFactionInvite()) {
                HunterHudStyle.recessedPanel(graphics, panelX + 14, panelY + 154, panelWidth - 28, 60);
                graphics.drawString(this.font, Component.literal("Pending Invite"), panelX + 26, panelY + 166, 0xF3B86F, false);
                graphics.drawWordWrap(this.font, Component.literal("Invitation to join " + data.getPendingFactionInviteName() + " from " + data.getPendingFactionInviterName() + "."), panelX + 26, panelY + 182, panelWidth - 52, 0xD3D8DE);
            }
            return;
        }
        graphics.drawString(this.font, Component.literal(data.getFactionName()), panelX + 14, panelY + 40, 0xF3B86F, false);
        graphics.drawString(this.font, Component.literal("Leader: " + data.getFactionOwnerName()), panelX + 14, panelY + 58, 0xB6D7F2, false);

        int membersX = panelX + 14;
        int membersY = panelY + 116;
        int membersWidth = (panelWidth / 2) - 20;
        HunterHudStyle.recessedPanel(graphics, membersX, membersY, membersWidth, panelY + panelHeight - 14 - membersY);
        graphics.drawString(this.font, Component.literal("Members"), membersX + 12, membersY + 10, 0xFFFFFF, false);
        List<String> members = new ArrayList<>(data.getFactionMembers());
        members.sort(String::compareToIgnoreCase);
        int lineY = membersY + 30;
        for (String member : members) {
            graphics.drawString(this.font, Component.literal(member), membersX + 12, lineY, member.equalsIgnoreCase(data.getFactionOwnerName()) ? 0xF3B86F : 0xD3D8DE, false);
            lineY += 12;
        }

        int inviteX = membersX + membersWidth + 12;
        int inviteWidth = panelWidth - (inviteX - panelX) - 14;
        HunterHudStyle.recessedPanel(graphics, inviteX, membersY, inviteWidth, panelY + panelHeight - 14 - membersY);
        graphics.drawString(this.font, Component.literal("Invite Players"), inviteX + 12, membersY + 10, 0xFFFFFF, false);
        if (!data.isFactionOwner()) {
            graphics.drawWordWrap(this.font, Component.literal("Only the faction leader can send invites."), inviteX + 12, membersY + 30, inviteWidth - 24, 0x8D98A4);
            return;
        }
        if (data.getInvitablePlayers().isEmpty()) {
            graphics.drawWordWrap(this.font, Component.literal("No eligible online players are available to invite right now."), inviteX + 12, membersY + 30, inviteWidth - 24, 0x8D98A4);
        } else {
            graphics.drawWordWrap(this.font, Component.literal("Click a player below to send them a faction invite."), inviteX + 12, membersY + 30, inviteWidth - 24, 0xC8D7E6);
        }
    }

    private void drawInfoCard(GuiGraphics graphics, int x, int y, int width, int height, String title, String value, int accentColor) {
        HunterHudStyle.recessedPanel(graphics, x, y, width, height);
        graphics.fill(x, y, x + width, y + 4, accentColor);
        graphics.drawString(this.font, Component.literal(title.toUpperCase(Locale.ROOT)), x + 12, y + 12, 0xFFFFFF, false);
        graphics.drawString(this.font, Component.literal(value), x + 12, y + 32, accentColor, false);
    }

    private void renderSlotBar(GuiGraphics graphics, HunterPlayerData data, int mouseX, int mouseY, boolean hudStyle) {
        int barX = getBarX();
        int barY = getBarY();
        for (int i = 0; i < HunterPlayerData.COMBAT_SLOT_COUNT; i++) {
            int slotX = barX + (i * (SLOT_SIZE + SLOT_GAP));
            boolean hovered = isInside(mouseX, mouseY, slotX, barY, SLOT_SIZE, SLOT_SIZE);
            HunterHudStyle.abilitySlot(graphics, slotX, barY, SLOT_SIZE, hovered ? 0xFF506070 : 0xFF28333D, hovered);
            HunterAbility assigned = HunterAbilities.byId(data.getCombatSlot(i));
            if (assigned != null) {
                graphics.blit(new ResourceLocation(HunterCraftMod.MODID, assigned.iconPath()), slotX + 5, barY + 4, 0, 0, 16, 16, 24, 24);
                graphics.drawCenteredString(this.font, ClientHooks.getCombatKeyLabel(i), slotX + (SLOT_SIZE / 2), barY + 18, 0xD8DDE5);
            } else {
                graphics.drawCenteredString(this.font, ClientHooks.getCombatKeyLabel(i), slotX + (SLOT_SIZE / 2), barY + 10, 0x8D98A4);
            }
        }
        if (!hudStyle) {
            graphics.drawString(this.font, Component.literal("Combat Bar " + (data.getActiveCombatBar() + 1)), barX, barY - 12, 0xF3B86F, false);
        }
    }

    private void renderClearZone(GuiGraphics graphics, int mouseX, int mouseY) {
        int clearX = this.width - PANEL_MARGIN - CLEAR_ZONE_WIDTH - 10;
        int clearY = getBarY() + 2;
        boolean hovered = isInside(mouseX, mouseY, clearX, clearY, CLEAR_ZONE_WIDTH, CLEAR_ZONE_HEIGHT);
        HunterHudStyle.recessedPanel(graphics, clearX, clearY, CLEAR_ZONE_WIDTH, CLEAR_ZONE_HEIGHT);
        if (hovered) {
            graphics.fill(clearX + 2, clearY + 2, clearX + CLEAR_ZONE_WIDTH - 2, clearY + CLEAR_ZONE_HEIGHT - 2, 0x66441818);
        }
        graphics.drawCenteredString(this.font, Component.literal("Clear Slot"), clearX + (CLEAR_ZONE_WIDTH / 2), clearY + 10, 0xFFFFFF);
    }

    private void renderAbilityCard(GuiGraphics graphics, HunterAbility ability, int x, int y, boolean hovered) {
        renderAbilityCard(graphics, ability, x, y, hovered, true);
    }

    private void renderAbilityCard(GuiGraphics graphics, HunterAbility ability, int x, int y, boolean hovered, boolean unlocked) {
        int outerColor = hovered ? 0xCC314052 : 0xCC1A232C;
        int innerColor = unlocked ? 0xAA11161D : 0xAA0B0F14;
        int titleColor = unlocked ? 0xFFFFFF : 0x8D98A4;
        HunterHudStyle.recessedPanel(graphics, x, y, ABILITY_CARD_WIDTH, ABILITY_CARD_HEIGHT);
        HunterHudStyle.ornamentalFrame(graphics, x, y, ABILITY_CARD_WIDTH, ABILITY_CARD_HEIGHT, outerColor, hovered ? 0xFF6F7F8D : 0xFF3F4B56, 0xFF05080D);
        graphics.fill(x + 4, y + 4, x + ABILITY_CARD_WIDTH - 4, y + ABILITY_CARD_HEIGHT - 4, innerColor);
        graphics.blit(new ResourceLocation(HunterCraftMod.MODID, ability.iconPath()), x + 6, y + 5, 0, 0, 24, 24, 24, 24);
        graphics.drawString(this.font, Component.literal(ability.displayName()), x + 36, y + 13, titleColor, false);
    }

    private void renderHoveredAbilityTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        if (this.activeTab != Tab.ABILITIES || !this.draggingAbilityId.isBlank()) {
            return;
        }
        HunterAbility hovered = getHoveredAbility(mouseX, mouseY);
        if (hovered == null) {
            return;
        }
        List<Component> lines = new ArrayList<>();
        lines.add(Component.literal(hovered.displayName()));
        lines.add(Component.literal(hovered.description()));
        graphics.renderComponentTooltip(this.font, lines, mouseX, mouseY);
    }

    private HunterAbility getHoveredAbility(int mouseX, int mouseY) {
        HunterPlayerData data = getData();
        List<SkillTreeCombatAbility> abilities = HunterAbilities.unlockedCombatAbilities(data);
        for (int i = 0; i < abilities.size(); i++) {
            int cardX = getAbilityCardX(i);
            int cardY = getAbilityCardY(i);
            if (isInside(mouseX, mouseY, cardX, cardY, ABILITY_CARD_WIDTH, ABILITY_CARD_HEIGHT)) {
                return abilities.get(i);
            }
        }
        for (int i = 0; i < HunterPlayerData.COMBAT_SLOT_COUNT; i++) {
            int slotX = getBarX() + (i * (SLOT_SIZE + SLOT_GAP));
            int slotY = getBarY();
            if (isInside(mouseX, mouseY, slotX, slotY, SLOT_SIZE, SLOT_SIZE)) {
                return HunterAbilities.byId(data.getCombatSlot(i));
            }
        }
        return null;
    }

    private void renderQuestProgressSummary(GuiGraphics graphics, HunterPlayerData data, int x, int y, int width, int height) {
        HunterHudStyle.recessedPanel(graphics, x, y, width, height);
        graphics.drawString(this.font, Component.literal("Current Quest"), x + 12, y + 12, 0xFFFFFF, false);
        if (data.getActiveQuests().isEmpty()) {
            graphics.drawString(this.font, Component.literal("No active quest"), x + 12, y + 32, 0x8D98A4, false);
            return;
        }
        String questId = data.getActiveQuests().iterator().next();
        QuestDefinition definition = QuestRegistry.byId(questId);
        Component title = definition == null ? Component.literal(questId) : Component.translatable(definition.titleKey());
        int progress = data.getQuestProgress().getOrDefault(questId, 0);
        int target = data.getQuestTargetCount(questId);
        graphics.drawString(this.font, title, x + 12, y + 32, 0xF3B86F, false);
        graphics.drawString(this.font, Component.literal(progress + " / " + target), x + 12, y + 48, 0xB6D7F2, false);
        HunterHudStyle.bar(graphics, x + 12, y + 66, width - 24, 10, target <= 0 ? 0.0F : (float) progress / target, 0xFF4C8FB7);
    }

    private void renderScarletEyesEditor(GuiGraphics graphics, HunterPlayerData data, int x, int y) {
        int faceSize = 96;
        HunterHudStyle.recessedPanel(graphics, x, y, faceSize + 20, faceSize + 20);
        ResourceLocation skin = Minecraft.getInstance().player == null
                ? new ResourceLocation("textures/entity/steve.png")
                : Minecraft.getInstance().player.getSkinTextureLocation();
        graphics.blit(skin, x + 10, y + 10, faceSize, faceSize, 8.0F, 8.0F, 8, 8, 64, 64);
        graphics.blit(skin, x + 10, y + 10, faceSize, faceSize, 40.0F, 8.0F, 8, 8, 64, 64);
        drawScarletEditorEye(graphics, data, x, y, 0);
        drawScarletEditorEye(graphics, data, x, y, 1);
        graphics.drawString(this.font, Component.literal("Left  X " + data.getScarletLeftEyeOffsetX() + " Y " + data.getScarletLeftEyeOffsetY() + " W " + data.getScarletLeftEyeLength() + " H " + data.getScarletLeftEyeVerticalLength()), x + faceSize + 28, y + 10, 0xC8D7E6, false);
        graphics.drawString(this.font, Component.literal("Right X " + data.getScarletRightEyeOffsetX() + " Y " + data.getScarletRightEyeOffsetY() + " W " + data.getScarletRightEyeLength() + " H " + data.getScarletRightEyeVerticalLength()), x + faceSize + 28, y + 24, 0xC8D7E6, false);
        graphics.drawString(this.font, Component.literal("Wheel = width. Shift + wheel = height."), x + faceSize + 28, y + 42, 0x8D98A4, false);
    }

    private void drawScarletEditorEye(GuiGraphics graphics, HunterPlayerData data, int editorX, int editorY, int eye) {
        int cx = getScarletEyeEditorX(data, editorX, eye);
        int cy = getScarletEyeEditorY(data, editorY, eye);
        int endX = cx + (getScarletEyeLength(data, eye) * 12);
        int endY = cy + (getScarletEyeVerticalLength(data, eye) * 12);
        int minX = Math.min(cx, endX);
        int maxX = Math.max(cx, endX);
        int minY = Math.min(cy, endY);
        int maxY = Math.max(cy, endY);
        graphics.fill(minX, minY, maxX, maxY, 0xFFE21E35);
        graphics.fill(minX - 1, minY - 2, maxX + 1, minY, 0x88FF6B7A);
        graphics.fill(cx - 1, cy - 7, cx + 2, cy + 7, 0xFFFFFFFF);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        if (this.activeTab == Tab.ABILITIES && button == 0) {
            HunterPlayerData data = getData();
            List<SkillTreeCombatAbility> abilities = HunterAbilities.unlockedCombatAbilities(data);
            for (int i = 0; i < abilities.size(); i++) {
                SkillTreeCombatAbility ability = abilities.get(i);
                int cardX = getAbilityCardX(i);
                int cardY = getAbilityCardY(i);
                if (isInside(mouseX, mouseY, cardX, cardY, ABILITY_CARD_WIDTH, ABILITY_CARD_HEIGHT)) {
                    this.draggingAbilityId = ability.id();
                    this.draggingFromSlot = -1;
                    this.dragMouseX = (int) mouseX;
                    this.dragMouseY = (int) mouseY;
                    return true;
                }
            }
            for (int i = 0; i < HunterPlayerData.COMBAT_SLOT_COUNT; i++) {
                int slotX = getBarX() + (i * (SLOT_SIZE + SLOT_GAP));
                int slotY = getBarY();
                String slotAbility = data.getCombatSlot(i);
                if (isInside(mouseX, mouseY, slotX - SLOT_DROP_PADDING, slotY - SLOT_DROP_PADDING, SLOT_SIZE + (SLOT_DROP_PADDING * 2), SLOT_SIZE + (SLOT_DROP_PADDING * 2)) && !slotAbility.isBlank()) {
                    this.draggingAbilityId = slotAbility;
                    this.draggingFromSlot = i;
                    this.dragMouseX = (int) mouseX;
                    this.dragMouseY = (int) mouseY;
                    return true;
                }
            }
        } else if (this.activeTab == Tab.PLAYER_INFO && button == 0 && getData().hasScarletEyesTrait() && isInsideScarletEditor(mouseX, mouseY)) {
            this.draggingScarletEyes = true;
            this.draggingScarletEye = getNearestScarletEye(mouseX, mouseY);
            updateScarletEyesOffsetFromMouse(mouseX, mouseY);
            return true;
        } else if (this.activeTab == Tab.SKILL_TREE && button == 0) {
            SkillTreeAbility clickedTreeAbility = getTreeAbilityAt(mouseX, mouseY);
            if (clickedTreeAbility != null) {
                this.selectedTreeAbility = clickedTreeAbility;
                return true;
            }
            SkillNode clickedStyle = getStyleAt(mouseX, mouseY);
            if (clickedStyle != null) {
                this.selectedStyle = clickedStyle;
                this.selectedTreeAbility = null;
                return true;
            }
        } else if (this.activeTab == Tab.NEN_TECHNIQUE && button == 0) {
            NenTechniqueSkillNode clickedNode = getNenTechniqueNodeAt(mouseX, mouseY);
            if (clickedNode != null) {
                this.selectedNenTechniqueNode = clickedNode;
                if (getData().canUnlockNenTechniqueNode(clickedNode)) {
                    HunterNetwork.sendToServer(new com.huntercraft.huntercraft.network.packet.UnlockNenTechniqueNodePacket(clickedNode.id()));
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (!this.draggingAbilityId.isBlank()) {
            this.dragMouseX = (int) mouseX;
            this.dragMouseY = (int) mouseY;
            return true;
        }
        if (this.draggingScarletEyes && button == 0) {
            updateScarletEyesOffsetFromMouse(mouseX, mouseY);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (this.activeTab == Tab.ABILITIES && button == 0 && !this.draggingAbilityId.isBlank()) {
            HunterPlayerData data = getData();
            int targetSlot = getSlotIndexAt(mouseX, mouseY);
            if (targetSlot >= 0) {
                if (this.draggingFromSlot >= 0 && this.draggingFromSlot != targetSlot) {
                    String targetAbility = data.getCombatSlot(targetSlot);
                    HunterNetwork.sendToServer(new UpdateCombatSlotPacket(data.getActiveCombatBar(), targetSlot, this.draggingAbilityId));
                    HunterNetwork.sendToServer(new UpdateCombatSlotPacket(data.getActiveCombatBar(), this.draggingFromSlot, targetAbility));
                } else {
                    HunterNetwork.sendToServer(new UpdateCombatSlotPacket(data.getActiveCombatBar(), targetSlot, this.draggingAbilityId));
                }
            } else if (this.draggingFromSlot >= 0 && isInside(mouseX, mouseY, this.width - PANEL_MARGIN - CLEAR_ZONE_WIDTH - 10, getBarY() + 2, CLEAR_ZONE_WIDTH, CLEAR_ZONE_HEIGHT)) {
                HunterNetwork.sendToServer(new UpdateCombatSlotPacket(data.getActiveCombatBar(), this.draggingFromSlot, ""));
            }
            this.draggingAbilityId = "";
            this.draggingFromSlot = -1;
            return true;
        }
        if (this.draggingScarletEyes && button == 0) {
            updateScarletEyesOffsetFromMouse(mouseX, mouseY);
            this.draggingScarletEyes = false;
            this.draggingScarletEye = -1;
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (this.activeTab == Tab.PLAYER_INFO && getData().hasScarletEyesTrait() && isInsideScarletEditor(mouseX, mouseY)) {
            int eye = getNearestScarletEye(mouseX, mouseY);
            HunterPlayerData data = getData();
            int step = delta > 0.0D ? 1 : -1;
            int leftLength = data.getScarletLeftEyeLength();
            int rightLength = data.getScarletRightEyeLength();
            int leftVerticalLength = data.getScarletLeftEyeVerticalLength();
            int rightVerticalLength = data.getScarletRightEyeVerticalLength();
            if (eye == 0) {
                if (hasShiftDown()) {
                    leftVerticalLength = nextScarletLength(leftVerticalLength, step);
                } else {
                    leftLength = nextScarletLength(leftLength, step);
                }
            } else {
                if (hasShiftDown()) {
                    rightVerticalLength = nextScarletLength(rightVerticalLength, step);
                } else {
                    rightLength = nextScarletLength(rightLength, step);
                }
            }
            setScarletEyesLayout(data.getScarletLeftEyeOffsetX(), data.getScarletLeftEyeOffsetY(), leftLength, leftVerticalLength,
                    data.getScarletRightEyeOffsetX(), data.getScarletRightEyeOffsetY(), rightLength, rightVerticalLength);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    private int getSlotIndexAt(double mouseX, double mouseY) {
        int barX = getBarX();
        int barY = getBarY();
        for (int i = 0; i < HunterPlayerData.COMBAT_SLOT_COUNT; i++) {
            int slotX = barX + (i * (SLOT_SIZE + SLOT_GAP));
            if (isInside(mouseX, mouseY, slotX - SLOT_DROP_PADDING, barY - SLOT_DROP_PADDING, SLOT_SIZE + (SLOT_DROP_PADDING * 2), SLOT_SIZE + (SLOT_DROP_PADDING * 2))) {
                return i;
            }
        }
        return -1;
    }

    private int getAbilityCardX(int index) {
        int poolX = PANEL_MARGIN + 10;
        return poolX + 12 + ((index % 3) * (ABILITY_CARD_WIDTH + ABILITY_CARD_GAP_X));
    }

    private int getAbilityCardY(int index) {
        int poolY = ABILITIES_CONTENT_TOP;
        int cardsY = poolY + 66;
        return cardsY + ((index / 3) * (ABILITY_CARD_HEIGHT + ABILITY_CARD_GAP_Y));
    }

    private SkillNode getStyleAt(double mouseX, double mouseY) {
        int contentTop = PANEL_MARGIN + 42;
        int contentBottom = this.height - PANEL_MARGIN - 12;
        int leftWidth = 164;
        int columnsLeft = PANEL_MARGIN + 10 + leftWidth + 12;
        SkillNode[] nodes = getNodesForCategory(this.selectedCategory);
        int columnWidth = getColumnWidth(columnsLeft, nodes.length);
        for (int i = 0; i < nodes.length; i++) {
            int columnX = columnsLeft + i * (columnWidth + 8);
            if (isInside(mouseX, mouseY, columnX, contentTop, columnWidth, contentBottom - contentTop)) {
                return nodes[i];
            }
        }
        return null;
    }

    private SkillTreeAbility getTreeAbilityAt(double mouseX, double mouseY) {
        int contentTop = PANEL_MARGIN + 42;
        int leftWidth = 164;
        int columnsLeft = PANEL_MARGIN + 10 + leftWidth + 12;
        SkillNode[] nodes = getNodesForCategory(this.selectedCategory);
        int columnWidth = getColumnWidth(columnsLeft, nodes.length);
        for (int i = 0; i < nodes.length; i++) {
            SkillNode node = nodes[i];
            int columnX = columnsLeft + i * (columnWidth + 8);
            int entryY = contentTop + 38;
            for (SkillTreeAbility entry : HunterAbilities.TREE_ABILITIES_BY_NODE.getOrDefault(node, List.of())) {
                if (isInside(mouseX, mouseY, columnX + 12, entryY, columnWidth - 24, SKILL_ENTRY_HEIGHT - 4)) {
                    this.selectedStyle = node;
                    return entry;
                }
                entryY += SKILL_ENTRY_HEIGHT;
            }
        }
        return null;
    }

    private int getColumnWidth(int columnsLeft, int count) {
        int availableWidth = this.width - columnsLeft - PANEL_MARGIN;
        return Math.max(110, (availableWidth - Math.max(0, count - 1) * 8) / Math.max(1, count));
    }

    private int getWrappedTextHeight(Component text, int width) {
        return this.font.split(text, width).size() * this.font.lineHeight;
    }

    private static SkillNode[] getNodesForCategory(SkillNode.Category category) {
        return Arrays.stream(SkillNode.values())
                .filter(node -> node.category() == category)
                .toArray(SkillNode[]::new);
    }

    private int getBarX() {
        int totalWidth = (HunterPlayerData.COMBAT_SLOT_COUNT * (SLOT_SIZE + SLOT_GAP)) - SLOT_GAP;
        return (this.width - totalWidth) / 2;
    }

    private int getBarY() {
        return this.height - 64;
    }

    private void switchCombatBar(int barIndex) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return;
        }
        minecraft.player.getCapability(HunterPlayerDataProvider.CAPABILITY).ifPresent(data -> {
            data.setActiveCombatBar(barIndex);
            HunterNetwork.sendToServer(new SwitchCombatBarPacket(data.getActiveCombatBar()));
            this.init();
        });
    }

    private void toggleEmptyHandsPickup() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return;
        }
        minecraft.player.getCapability(HunterPlayerDataProvider.CAPABILITY).ifPresent(data -> {
            data.setEmptyHandsPickupEnabled(!data.isEmptyHandsPickupEnabled());
            HunterNetwork.sendToServer(new ToggleEmptyHandsPickupPacket(data.isEmptyHandsPickupEnabled()));
            this.init();
        });
    }

    private void setNenAuraColor(int color) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return;
        }
        minecraft.player.getCapability(HunterPlayerDataProvider.CAPABILITY).ifPresent(data -> {
            data.setNenAuraColor(color);
            HunterNetwork.sendToServer(new SetNenAuraColorPacket(color));
            this.init();
        });
    }

    private void updateNenAuraColorFromSliders() {
        if (this.redSlider == null || this.greenSlider == null || this.blueSlider == null) {
            return;
        }
        int color = (this.redSlider.getChannelValue() << 16)
                | (this.greenSlider.getChannelValue() << 8)
                | this.blueSlider.getChannelValue();
        setNenAuraColor(color);
    }

    private static boolean isInside(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    private boolean isInsideScarletEditor(double mouseX, double mouseY) {
        int panelX = PANEL_MARGIN + 24;
        int panelY = PANEL_MARGIN + 58;
        int x = panelX + 14;
        int y = panelY + 374;
        return isInside(mouseX, mouseY, x + 10, y + 10, 96, 96);
    }

    private void updateScarletEyesOffsetFromMouse(double mouseX, double mouseY) {
        HunterPlayerData data = getData();
        int panelX = PANEL_MARGIN + 24;
        int panelY = PANEL_MARGIN + 58;
        int faceX = panelX + 24;
        int faceY = panelY + 384;
        int offsetX = Math.max(-8, Math.min(8, (int) Math.round(((mouseX - faceX) / 96.0D) * 16.0D) - 8));
        int offsetY = Math.max(-8, Math.min(8, (int) Math.round(((mouseY - faceY) / 96.0D) * 16.0D) - 8));
        int eye = this.draggingScarletEye < 0 ? getNearestScarletEye(mouseX, mouseY) : this.draggingScarletEye;
        if (eye == 0) {
            setScarletEyesLayout(offsetX, offsetY, data.getScarletLeftEyeLength(),
                    data.getScarletLeftEyeVerticalLength(), data.getScarletRightEyeOffsetX(), data.getScarletRightEyeOffsetY(), data.getScarletRightEyeLength(), data.getScarletRightEyeVerticalLength());
        } else {
            setScarletEyesLayout(data.getScarletLeftEyeOffsetX(), data.getScarletLeftEyeOffsetY(), data.getScarletLeftEyeLength(),
                    data.getScarletLeftEyeVerticalLength(), offsetX, offsetY, data.getScarletRightEyeLength(), data.getScarletRightEyeVerticalLength());
        }
    }

    private void setScarletEyesLayout(int leftX, int leftY, int leftLength, int leftVerticalLength, int rightX, int rightY, int rightLength, int rightVerticalLength) {
        HunterPlayerData data = getData();
        data.setScarletEyesLayout(leftX, leftY, leftLength, leftVerticalLength, rightX, rightY, rightLength, rightVerticalLength);
        HunterNetwork.sendToServer(new SetScarletEyesOffsetPacket(data.getScarletLeftEyeOffsetX(), data.getScarletLeftEyeOffsetY(), data.getScarletLeftEyeLength(), data.getScarletLeftEyeVerticalLength(),
                data.getScarletRightEyeOffsetX(), data.getScarletRightEyeOffsetY(), data.getScarletRightEyeLength(), data.getScarletRightEyeVerticalLength()));
    }

    private static int nextScarletLength(int current, int step) {
        int next = current + step;
        if (next == 0) {
            next += step;
        }
        return Math.max(-8, Math.min(8, next));
    }

    private int getNearestScarletEye(double mouseX, double mouseY) {
        HunterPlayerData data = getData();
        int panelX = PANEL_MARGIN + 24;
        int panelY = PANEL_MARGIN + 58;
        int editorX = panelX + 14;
        int editorY = panelY + 374;
        double leftDistance = Math.pow(mouseX - getScarletEyeEditorX(data, editorX, 0), 2.0D) + Math.pow(mouseY - getScarletEyeEditorY(data, editorY, 0), 2.0D);
        double rightDistance = Math.pow(mouseX - getScarletEyeEditorX(data, editorX, 1), 2.0D) + Math.pow(mouseY - getScarletEyeEditorY(data, editorY, 1), 2.0D);
        return leftDistance <= rightDistance ? 0 : 1;
    }

    private static int getScarletEyeEditorX(HunterPlayerData data, int editorX, int eye) {
        int offset = eye == 0 ? data.getScarletLeftEyeOffsetX() : data.getScarletRightEyeOffsetX();
        return editorX + 10 + 48 + Math.round(offset * 6.0F);
    }

    private static int getScarletEyeEditorY(HunterPlayerData data, int editorY, int eye) {
        int offset = eye == 0 ? data.getScarletLeftEyeOffsetY() : data.getScarletRightEyeOffsetY();
        return editorY + 10 + 48 + Math.round(offset * 6.0F);
    }

    private static int getScarletEyeLength(HunterPlayerData data, int eye) {
        return eye == 0 ? data.getScarletLeftEyeLength() : data.getScarletRightEyeLength();
    }

    private static int getScarletEyeVerticalLength(HunterPlayerData data, int eye) {
        return eye == 0 ? data.getScarletLeftEyeVerticalLength() : data.getScarletRightEyeVerticalLength();
    }

    private static String getFactionInfo(HunterPlayerData data) {
        if (!data.hasFaction()) {
            return data.hasPendingFactionInvite() ? "Invite: " + data.getPendingFactionInviteName() : "None";
        }
        return data.getFactionName() + (data.isFactionOwner() ? " (Leader)" : "");
    }

    private HunterPlayerData getData() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return new HunterPlayerData();
        }
        return minecraft.player.getCapability(HunterPlayerDataProvider.CAPABILITY).orElseGet(HunterPlayerData::new);
    }

    private List<Tab> getVisibleTabs() {
        List<Tab> tabs = new ArrayList<>(List.of(Tab.SKILL_TREE, Tab.ABILITIES, Tab.PLAYER_INFO, Tab.NEN));
        if (!getData().getNenTechniqueId().isBlank()) {
            tabs.add(Tab.NEN_TECHNIQUE);
        }
        if (getData().hasNen()) {
            tabs.add(Tab.NEN_VOWS);
        }
        tabs.add(Tab.FACTION);
        return tabs;
    }

    private NenTechniqueSkillNode getNenTechniqueNodeAt(double mouseX, double mouseY) {
        HunterPlayerData data = getData();
        NenTechniqueSkillNode[] nodes = NenTechniqueSkillNode.forTechnique(data.getNenTechniqueId());
        if (nodes.length == 0) {
            return null;
        }
        int panelX = PANEL_MARGIN + 24;
        int panelWidth = this.width - (PANEL_MARGIN * 2) - 48;
        int treeLeft = panelX + 286;
        int treeRight = panelX + panelWidth - 42;
        int nodeY = PANEL_MARGIN + 58 + 190;
        int spacing = nodes.length == 1 ? 0 : (treeRight - treeLeft) / (nodes.length - 1);
        for (int i = 0; i < nodes.length; i++) {
            int nodeX = treeLeft + (i * spacing);
            if (isInside(mouseX, mouseY, nodeX, nodeY, 42, 42)) {
                return nodes[i];
            }
        }
        return null;
    }

    private enum Tab {
        SKILL_TREE("Skill Tree"),
        ABILITIES("Abilities"),
        PLAYER_INFO("Player Info"),
        NEN("Nen"),
        NEN_TECHNIQUE("Hatsu Technique"),
        NEN_VOWS("Nen Vows"),
        FACTION("Faction");

        private final String label;

        Tab(String label) {
            this.label = label;
        }
    }

    private final class ColorSlider extends AbstractSliderButton {
        private final String channel;

        private ColorSlider(int x, int y, int width, int height, String channel, int value) {
            super(x, y, width, height, Component.empty(), value / 255.0D);
            this.channel = channel;
            this.updateMessage();
        }

        private int getChannelValue() {
            return Math.max(0, Math.min(255, (int) Math.round(this.value * 255.0D)));
        }

        @Override
        protected void updateMessage() {
            this.setMessage(Component.literal(this.channel + ": " + this.getChannelValue()));
        }

        @Override
        protected void applyValue() {
            this.updateMessage();
            updateNenAuraColorFromSliders();
        }
    }
}
