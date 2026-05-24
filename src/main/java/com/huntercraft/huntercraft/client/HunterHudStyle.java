package com.huntercraft.huntercraft.client;

import net.minecraft.client.gui.GuiGraphics;

public final class HunterHudStyle {
    public static final int PANEL = 0xD0091118;
    public static final int PANEL_SOFT = 0xB30C1219;
    public static final int PANEL_INNER = 0xAA11161D;
    public static final int INK = 0xFF05080D;
    public static final int STEEL = 0xFF2D3844;
    public static final int STEEL_DARK = 0xFF151D25;
    public static final int STEEL_LIGHT = 0xFF526170;
    public static final int AURA_BLUE = 0xFF5BE5FF;
    public static final int AURA_GREEN = 0xFF88E0C2;
    public static final int BLOOD = 0xFFB83B4B;
    public static final int GOLD = 0xFFF3B86F;
    public static final int TEXT = 0xFFFFFFFF;
    public static final int TEXT_SOFT = 0xFFC8D7E6;
    public static final int TEXT_MUTED = 0xFF8D98A4;

    private HunterHudStyle() {
    }

    public static void panel(GuiGraphics graphics, int x, int y, int width, int height) {
        graphics.fill(x, y, x + width, y + height, PANEL);
        graphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, 0xC00B1118);
        graphics.fill(x + 2, y + 2, x + width - 2, y + height - 2, PANEL_SOFT);
        ornamentalFrame(graphics, x, y, width, height, STEEL, STEEL_LIGHT, INK);
    }

    public static void recessedPanel(GuiGraphics graphics, int x, int y, int width, int height) {
        graphics.fill(x, y, x + width, y + height, 0xAA080D13);
        graphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, PANEL_INNER);
        graphics.fill(x + 2, y + 2, x + width - 2, y + height - 2, 0x7710161D);
        thinFrame(graphics, x, y, width, height, STEEL_DARK, 0x8836424E);
    }

    public static void abilitySlot(GuiGraphics graphics, int x, int y, int size, int stateColor, boolean hovered) {
        int edge = 0xFF000000 | (hovered ? brighten(stateColor, 24) : stateColor) & 0x00FFFFFF;
        int highlight = hovered ? 0xFF8EEBFF : 0xFF657687;
        graphics.fill(x, y, x + size, y + size, 0xEE05080D);
        graphics.fill(x + 1, y + 1, x + size - 1, y + size - 1, 0xDD111820);
        graphics.fill(x + 3, y + 3, x + size - 3, y + size - 3, 0xAA0D1218);
        ornamentalFrame(graphics, x, y, size, size, edge, highlight, 0xFF05080D);
        graphics.fill(x + 5, y + 5, x + size - 5, y + size - 5, hovered ? 0x80303A44 : 0x55182129);
        graphics.fill(x + 2, y + 2, x + 5, y + 3, 0xFF76D9FF);
        graphics.fill(x + 2, y + 2, x + 3, y + 5, 0xFF76D9FF);
        graphics.fill(x + size - 5, y + size - 3, x + size - 2, y + size - 2, 0xFF8E5CFF);
        graphics.fill(x + size - 3, y + size - 5, x + size - 2, y + size - 2, 0xFF8E5CFF);
    }

    public static void compactTechniqueSlot(GuiGraphics graphics, int x, int y, int size, int stateColor) {
        int edge = 0xFF000000 | stateColor & 0x00FFFFFF;
        graphics.fill(x, y, x + size, y + size, 0xEE05080D);
        graphics.fill(x + 1, y + 1, x + size - 1, y + size - 1, 0xCC10161D);
        graphics.fill(x + 3, y + 3, x + size - 3, y + size - 3, 0x8811161D);
        ornamentalFrame(graphics, x, y, size, size, edge, 0xFF657687, 0xFF05080D);
    }

    public static void bar(GuiGraphics graphics, int x, int y, int width, int height, float percent, int fillColor) {
        int clampedFill = Math.max(0, Math.min(width - 4, Math.round((width - 4) * percent)));
        graphics.fill(x, y, x + width, y + height, 0xDD05080D);
        graphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, 0xCC1A232C);
        graphics.fill(x + 2, y + 2, x + 2 + clampedFill, y + height - 2, fillColor);
        graphics.fill(x + 2, y + 2, x + width - 2, y + 3, 0x22FFFFFF);
        thinFrame(graphics, x, y, width, height, 0xAA000000, 0x88384552);
    }

    public static void ornamentalFrame(GuiGraphics graphics, int x, int y, int width, int height, int edgeColor, int highlightColor, int shadowColor) {
        thinFrame(graphics, x, y, width, height, shadowColor, edgeColor);
        graphics.fill(x + 1, y + 2, x + width - 1, y + 3, highlightColor);
        graphics.fill(x + 1, y + height - 3, x + width - 1, y + height - 2, edgeColor);
        graphics.fill(x + 2, y + 1, x + 3, y + height - 1, highlightColor);
        graphics.fill(x + width - 3, y + 1, x + width - 2, y + height - 1, edgeColor);
        corner(graphics, x, y, edgeColor, highlightColor);
        corner(graphics, x + width - 7, y, edgeColor, highlightColor);
        corner(graphics, x, y + height - 7, edgeColor, highlightColor);
        corner(graphics, x + width - 7, y + height - 7, edgeColor, highlightColor);
    }

    public static void thinFrame(GuiGraphics graphics, int x, int y, int width, int height, int shadowColor, int edgeColor) {
        graphics.fill(x, y, x + width, y + 1, edgeColor);
        graphics.fill(x, y + height - 1, x + width, y + height, shadowColor);
        graphics.fill(x, y, x + 1, y + height, edgeColor);
        graphics.fill(x + width - 1, y, x + width, y + height, shadowColor);
    }

    private static void corner(GuiGraphics graphics, int x, int y, int edgeColor, int highlightColor) {
        graphics.fill(x, y + 1, x + 7, y + 3, edgeColor);
        graphics.fill(x + 1, y, x + 3, y + 7, edgeColor);
        graphics.fill(x + 1, y + 1, x + 4, y + 2, highlightColor);
        graphics.fill(x + 1, y + 1, x + 2, y + 4, highlightColor);
        graphics.fill(x + 5, y + 3, x + 6, y + 5, 0xAA05080D);
        graphics.fill(x + 3, y + 5, x + 5, y + 6, 0xAA05080D);
    }

    private static int brighten(int color, int amount) {
        int alpha = color & 0xFF000000;
        int red = Math.min(255, ((color >> 16) & 0xFF) + amount);
        int green = Math.min(255, ((color >> 8) & 0xFF) + amount);
        int blue = Math.min(255, (color & 0xFF) + amount);
        return alpha | (red << 16) | (green << 8) | blue;
    }
}
