package com.medinexz.notenoughresources.gui;

import codechicken.lib.gui.GuiDraw;
import com.medinexz.notenoughresources.core.OreGenerationLayout;
import com.medinexz.notenoughresources.core.OreGenerationProfile;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

public class OreGenerationRenderer {

    private static final int COLOR_GRAPH = 0xFF44AA44;
    private static final int COLOR_AXIS  = 0xFF888888;
    private static final int COLOR_TEXT  = 0xFF333333;

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
            pctStr = String.format("%.2f%%", chance);
        } else {
            pctStr = "<0.01%";
        }

        List<String> tooltip = new ArrayList<String>();
        tooltip.add("Y: " + yValue + " (" + pctStr + ")");
        return tooltip;
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

        String maxPct = String.format("%.2f%%", maxProbability * 100.0);
        drawSmallStringRight(maxPct, xPct, graphY - graphHeight - 7, COLOR_TEXT);

        // X-axis labels: minY, midY, maxY (below baseline)
        int minY = profile.getMinY();
        int maxY = profile.getMaxY();
        int midY = (minY + maxY) / 2;
        int labelY = graphY + 2;

        drawSmallStringCentered(String.valueOf(minY), graphX, labelY, COLOR_TEXT);
        drawSmallStringCentered(String.valueOf(maxY), graphX + graphWidth, labelY, COLOR_TEXT);
        drawSmallStringCentered(String.valueOf(midY), graphX + graphWidth / 2, labelY, COLOR_TEXT);

        // Peak info (below X-axis labels)
        int peakY = profile.getPeakY();
        int peakLabelY = graphY + 10;
        drawSmallString("Peak Y:" + peakY, graphX, peakLabelY, COLOR_TEXT);
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
