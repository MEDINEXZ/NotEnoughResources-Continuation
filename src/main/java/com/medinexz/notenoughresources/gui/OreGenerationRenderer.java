package com.medinexz.notenoughresources.gui;

import codechicken.lib.gui.GuiDraw;
import com.medinexz.notenoughresources.core.DropStatistics;
import com.medinexz.notenoughresources.core.OreData;
import com.medinexz.notenoughresources.core.OreGenerationLayout;
import com.medinexz.notenoughresources.core.OreGenerationProfile;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.util.EnumChatFormatting;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class OreGenerationRenderer {

    private static final int COLOR_GRAPH = 0xFF000000;
    private static final int COLOR_AXIS  = 0xFF888888;
    private static final int COLOR_TEXT  = 0xFF333333;

    private static final int MAX_BIOME_LINES = 8;

    private static final int DROP_CHANCES_PER_LINE = 3;

    private final OreGenerationLayout layout;

    public OreGenerationRenderer(OreGenerationLayout layout) {
        this.layout = layout;
    }

    // Main entry point: draws axes + graph line + labels
    public void drawGraph(OreGenerationProfile profile, int offsetX, int offsetY) {
        if (profile == null) return;

        int minY = profile.getMinY();
        int maxY = profile.getMaxY();
        int count = maxY - minY + 1;
        if (count <= 0) return;

        int graphX = offsetX + layout.getGraphX();
        int graphY = offsetY + layout.getGraphY();
        int graphWidth  = layout.getGraphWidth();
        int graphHeight = layout.getGraphHeight();

        drawAxes(graphX, graphY, graphWidth, graphHeight);

        double maxProbability = profile.getPeakProbability();
        if (maxProbability <= 0.0) return;

        if (count == 1) {
            // Single Y level — draw horizontal line
            double normalized = profile.getProbability(minY) / maxProbability;
            double py = graphY - normalized * graphHeight;
            drawLine(graphX, py, graphX + graphWidth, py, COLOR_GRAPH);
        } else {
            double space = (double) graphWidth / (count - 1);
            double prevX = graphX;
            double prevY = graphY;

            for (int i = 0; i < count; i++) {
                double prob = profile.getProbability(minY + i);
                double x = graphX + i * space;
                double y = graphY - (prob / maxProbability) * graphHeight;
                if (i > 0) {
                    drawLine(prevX, prevY, x, y, COLOR_GRAPH);
                }
                prevX = x;
                prevY = y;
            }
        }

        drawLabels(profile, maxProbability, graphX, graphY, graphWidth, graphHeight);
    }

    // Returns tooltip strings when mouse is over graph area; null otherwise
    public List<String> getTooltip(int localMouseX, int localMouseY, OreGenerationProfile profile) {
        if (profile == null || !isOnGraph(localMouseX, localMouseY)) return null;

        int count = profile.getMaxY() - profile.getMinY() + 1;
        if (count <= 1) return null;

        int graphX     = layout.getGraphX();
        int graphWidth = layout.getGraphWidth();

        double drawingSpace = (double) graphWidth / (count - 1);
        int index = (int) Math.round((double)(localMouseX - graphX) / drawingSpace);
        if (index < 0) index = 0;
        if (index >= count) index = count - 1;

        int yValue = profile.getMinY() + index;
        double chance = profile.getProbability(yValue) * 100.0;

        String pctStr;
        if (chance == 0.0 || chance > 0.01) {
            pctStr = String.format(Locale.ROOT, "%.2f%%", chance);
        } else {
            pctStr = "<0.01%";
        }

        List<String> tooltip = new ArrayList<String>();
        tooltip.add("Y: " + yValue + " (" + pctStr + ")");
        return tooltip;
    }

    // Extends the ore item's tooltip (which already starts with the item name) with
    // mod name, Silk Touch requirement and spawn biomes taken from the ore data
    public void addOreTooltip(List<String> tooltip, OreData oreData) {
        String modName = oreData.getModName();
        boolean hasModName = false;
        for (String line : tooltip) {
            String plain = EnumChatFormatting.getTextWithoutFormattingCodes(line);
            if (plain != null && plain.trim().equals(modName)) hasModName = true;
        }
        if (!hasModName) {
            tooltip.add(Math.min(1, tooltip.size()),
                EnumChatFormatting.BLUE.toString() + EnumChatFormatting.ITALIC + modName);
        }

        if (oreData.isSilkTouchNeeded()) {
            tooltip.add(EnumChatFormatting.DARK_AQUA + "Silk Touch Needed");
        }

        List<String> biomes = oreData.getSpawnBiomes();
        if (!biomes.isEmpty()) {
            tooltip.add("Spawn Biomes:");
            int shown = biomes.size() > MAX_BIOME_LINES ? MAX_BIOME_LINES - 1 : biomes.size();
            for (int i = 0; i < shown; i++) {
                tooltip.add("  " + biomes.get(i));
            }
            if (shown < biomes.size()) {
                tooltip.add("  ... and " + (biomes.size() - shown) + " more");
            }
        }
    }

    // Extends a drop item's tooltip with its average amount per Fortune level, or
    // (detailed) with the chance of every possible amount.  Every level is listed,
    // also for blocks that Fortune does not affect.
    public void addDropTooltip(List<String> tooltip, DropStatistics drop, boolean detailed) {
        tooltip.add(detailed ? "Drop Chances:" : "Avg. Drops:");

        for (int fortune = 0; fortune <= DropStatistics.MAX_FORTUNE; fortune++) {
            Map<Integer, Double> distribution = drop.getDistribution(fortune);

            String level = fortune == 0 ? "Normal" : Enchantment.fortune.getTranslatedName(fortune);

            if (!detailed) {
                tooltip.add("  " + level + ": " + String.format(Locale.ROOT, "%.2f", drop.getAverage(fortune)));
                continue;
            }

            tooltip.add("  " + level + ":");
            StringBuilder line = new StringBuilder();
            int onLine = 0;
            for (Map.Entry<Integer, Double> chance : distribution.entrySet()) {
                if (onLine == DROP_CHANCES_PER_LINE) {
                    tooltip.add(line.toString());
                    line = new StringBuilder();
                    onLine = 0;
                }
                line.append(onLine == 0 ? "    " : "   ")
                    .append(chance.getKey()).append(": ")
                    .append(String.format(Locale.ROOT, "%.2f%%", chance.getValue() * 100.0));
                onLine++;
            }
            if (onLine > 0) tooltip.add(line.toString());
        }

        if (!detailed) {
            tooltip.add(EnumChatFormatting.DARK_GRAY.toString() + EnumChatFormatting.ITALIC
                + "Hold Shift for drop chances");
        }
    }

    public boolean isOnGraph(int localMouseX, int localMouseY) {
        int graphX      = layout.getGraphX();
        int graphY      = layout.getGraphY();
        int graphWidth  = layout.getGraphWidth();
        int graphHeight = layout.getGraphHeight();
        return localMouseX >= graphX && localMouseX <= graphX + graphWidth
            && localMouseY >= graphY - graphHeight && localMouseY <= graphY;
    }

    // ── Private helpers ─────────────────────────────────────────────────────

    private void drawAxes(int graphX, int graphY, int graphWidth, int graphHeight) {
        // X-axis (horizontal baseline)
        drawLine(graphX, graphY, graphX + graphWidth, graphY, COLOR_AXIS);
        // Y-axis (vertical left edge)
        drawLine(graphX, graphY, graphX, graphY - graphHeight, COLOR_AXIS);
    }

    private void drawLabels(OreGenerationProfile profile, double maxProbability,
                             int graphX, int graphY, int graphWidth, int graphHeight) {
        // Y-axis percent labels (right-aligned, just left of Y-axis)
        int xPct = graphX - 2;
        int yPctBottom = graphY - 7;

        String zeroPct = "0%";
        drawSmallStringRight(zeroPct, xPct, yPctBottom, COLOR_TEXT);

        String maxPct = String.format(Locale.ROOT, "%.2f%%", maxProbability * 100.0);
        drawSmallStringRight(maxPct, xPct, graphY - graphHeight - 7, COLOR_TEXT);

        // X-axis labels: minY, midY, maxY (below baseline)
        int minY = profile.getMinY();
        int maxY = profile.getMaxY();
        int midY = (minY + maxY) / 2;
        int labelY = graphY + 2;

        drawSmallStringCentered(String.valueOf(minY), graphX, labelY, COLOR_TEXT);
        drawSmallStringCentered(String.valueOf(maxY), graphX + graphWidth, labelY, COLOR_TEXT);
        drawSmallStringCentered(String.valueOf(midY), graphX + graphWidth / 2, labelY, COLOR_TEXT);

        // Best Y (two lines directly under the ore slot)
        int peakY = profile.getPeakY();
        int bestX = graphX - layout.getGraphX() + layout.getBestLabelX();
        int bestY = graphY - layout.getGraphY() + layout.getBestLabelY();
        drawSmallString("Best:", bestX, bestY, COLOR_TEXT);
        drawSmallString("Y=" + peakY, bestX, bestY + layout.getBestLineHeight(), COLOR_TEXT);
    }

    private void drawLine(double x1, double y1, double x2, double y2, int colorRGB) {
        float r = ((colorRGB >> 16) & 0xFF) / 255.0f;
        float g = ((colorRGB >> 8)  & 0xFF) / 255.0f;
        float b = (colorRGB         & 0xFF) / 255.0f;

        boolean texEnabled = GL11.glIsEnabled(GL11.GL_TEXTURE_2D);
        boolean litEnabled = GL11.glIsEnabled(GL11.GL_LIGHTING);

        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glLineWidth(1.5f);
        GL11.glColor3f(r, g, b);
        GL11.glBegin(GL11.GL_LINES);
        GL11.glVertex2d(x1, y1);
        GL11.glVertex2d(x2, y2);
        GL11.glEnd();
        GL11.glLineWidth(1.0f);
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);

        if (texEnabled)  GL11.glEnable(GL11.GL_TEXTURE_2D);
        if (litEnabled)  GL11.glEnable(GL11.GL_LIGHTING);
    }

    // Draw small (half-scale) text starting at (screenX, screenY)
    private void drawSmallString(String text, int screenX, int screenY, int color) {
        GL11.glPushMatrix();
        GL11.glTranslatef(screenX, screenY, 0);
        GL11.glScalef(0.5f, 0.5f, 1.0f);
        GuiDraw.drawString(text, 0, 0, color, false);
        GL11.glPopMatrix();
    }

    // Right-aligned: right edge of text at screenX
    private void drawSmallStringRight(String text, int screenX, int screenY, int color) {
        int w = smallWidth(text);
        drawSmallString(text, screenX - w, screenY, color);
    }

    // Centered: center of text at screenX
    private void drawSmallStringCentered(String text, int screenX, int screenY, int color) {
        int w = smallWidth(text);
        drawSmallString(text, screenX - w / 2, screenY, color);
    }

    private int smallWidth(String text) {
        return (GuiDraw.getStringWidth(text) + 1) / 2;
    }
}
