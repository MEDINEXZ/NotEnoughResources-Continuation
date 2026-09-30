package com.medinexz.notenoughresources.gui;

import com.medinexz.notenoughresources.core.OreData;
import com.medinexz.notenoughresources.core.OreGenerationLayout;
import com.medinexz.notenoughresources.core.OreGenerationProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;

public class GuiOreGeneration extends GuiScreen {
    private OreGenerationBackground background;

    private OreGenerationLayout layout;

    private OreGenerationRenderer renderer;
    private OreData oreData;

    private static final ResourceLocation TEXTURE =
        new ResourceLocation(
            "notenoughresources",
            "textures/gui/world_gen.png"
        );

    private static final int GUI_WIDTH = 160;
    private static final int GUI_HEIGHT = 90;


    public GuiOreGeneration(
        OreData oreData
    ) {

        this.oreData = oreData;


        layout =
            new OreGenerationLayout();

        background =
            new OreGenerationBackground(
                TEXTURE,
                GUI_WIDTH,
                GUI_HEIGHT
            );

        renderer =
            new OreGenerationRenderer(
                layout
            );
    }


    @Override
    public void initGui() {

        super.initGui();
    }


    @Override
    public void drawScreen(
        int mouseX,
        int mouseY,
        float partialTicks
    ) {

        drawDefaultBackground();

        int guiLeft =
            (width - GUI_WIDTH) / 2;

        int guiTop =
            (height - GUI_HEIGHT) / 2;


        background.draw(
            Minecraft.getMinecraft(),
            guiLeft,
            guiTop
        );


        if (oreData != null) {

            OreGenerationProfile profile =
                oreData.getGenerationProfile();

            renderer.drawGraph(
                profile,
                guiLeft,
                guiTop
            );
        }

        super.drawScreen(
            mouseX,
            mouseY,
            partialTicks
        );
    }


    public OreData getOreData() {
        return oreData;
    }


    public OreGenerationLayout getLayout() {
        return layout;
    }


    public OreGenerationBackground getBackgroundRenderer() {
        return background;
    }


    public OreGenerationRenderer getRenderer() {
        return renderer;
    }
}
