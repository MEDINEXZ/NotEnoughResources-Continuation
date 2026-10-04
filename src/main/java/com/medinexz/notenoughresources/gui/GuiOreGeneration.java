package com.medinexz.notenoughresources.gui;

import codechicken.lib.gui.GuiDraw;
import com.medinexz.notenoughresources.core.OreData;
import com.medinexz.notenoughresources.core.OreGenerationLayout;
import com.medinexz.notenoughresources.core.OreGenerationProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.util.List;

public class GuiOreGeneration extends GuiScreen {

    private static final ResourceLocation TEXTURE =
        new ResourceLocation("notenoughresources", "textures/gui/world_gen.png");

    private final OreData oreData;
    private final OreGenerationLayout layout;
    private final OreGenerationBackground background;
    private final OreGenerationRenderer renderer;

    public GuiOreGeneration(OreData oreData) {
        this.oreData    = oreData;
        this.layout     = new OreGenerationLayout();
        this.background = new OreGenerationBackground(TEXTURE, layout);
        this.renderer   = new OreGenerationRenderer(layout);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();

        int guiLeft = (width  - layout.getEntryWidth())  / 2;
        int guiTop  = (height - layout.getEntryHeight()) / 2;

        background.draw(guiLeft, guiTop);

        if (oreData != null) {
            // Ore name title
            String name = oreData.getName();
            int nameW = GuiDraw.getStringWidth(name);
            GuiDraw.drawString(name, guiLeft + (layout.getEntryWidth() - nameW) / 2, guiTop + layout.getTitleY(), 0xFF222222, false);

            OreGenerationProfile profile = oreData.getGenerationProfile();
            renderer.drawGraph(profile, guiLeft, guiTop);

            // Tooltip on graph hover
            int localX = mouseX - guiLeft;
            int localY = mouseY - guiTop;
            if (renderer.isOnGraph(localX, localY)) {
                List<String> tooltip = renderer.getTooltip(localX, localY, profile);
                if (tooltip != null && !tooltip.isEmpty()) {
                    drawHoveringText(tooltip, mouseX, mouseY, Minecraft.getMinecraft().fontRenderer);
                }
            }
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    public OreData getOreData()               { return oreData; }
    public OreGenerationLayout getLayout()    { return layout; }
    public OreGenerationBackground getBackgroundRenderer() { return background; }
    public OreGenerationRenderer getRenderer() { return renderer; }
}
