package com.medinexz.notenoughresources.gui;

import com.medinexz.notenoughresources.core.OreGenerationLayout;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class OreGenerationBackground extends Gui {

    // Where the sprites sit inside world_gen.png
    private static final int SLOT_U = 0;
    private static final int SLOT_V = 16;
    private static final int BAR_U  = 0;
    private static final int BAR_V  = 61;

    // The plain panel colour of world_gen.png
    private static final int COLOR_PANEL = 0xFFC6C6C6;

    private final ResourceLocation texture;
    private final OreGenerationLayout layout;

    public OreGenerationBackground(ResourceLocation texture, OreGenerationLayout layout) {
        this.texture = texture;
        this.layout = layout;
    }

    // Draws one entry: panel, ore slot and drops bar, each where the layout puts it
    public void draw(int x, int y) {
        drawRect(x, y, x + getWidth(), y + getHeight(), COLOR_PANEL);

        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
        drawTexturedModalRect(
            x + layout.getOreSlotX(), y + layout.getOreSlotY(),
            SLOT_U, SLOT_V, layout.getOreSlotWidth(), layout.getOreSlotHeight());
        drawTexturedModalRect(
            x + layout.getDropsBarX(), y + layout.getDropsBarY(),
            BAR_U, BAR_V, layout.getDropsBarWidth(), layout.getDropsBarHeight());
    }

    public ResourceLocation getTexture() { return texture; }
    public int getWidth() { return layout.getEntryWidth(); }
    public int getHeight() { return layout.getEntryHeight(); }
}
