package com.medinexz.notenoughresources.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class OreGenerationBackground extends Gui {

    private final ResourceLocation texture;
    private final int width;
    private final int height;

    public OreGenerationBackground(ResourceLocation texture, int width, int height) {
        this.texture = texture;
        this.width = width;
        this.height = height;
    }

    public void draw(int x, int y) {
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
        drawTexturedModalRect(x, y, 0, 0, width, height);
    }

    public ResourceLocation getTexture() { return texture; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
}
