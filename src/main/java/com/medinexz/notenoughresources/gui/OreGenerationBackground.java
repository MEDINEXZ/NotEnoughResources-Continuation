package com.medinexz.notenoughresources.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.util.ResourceLocation;

public class OreGenerationBackground extends Gui {

    /*
     * GUI texture
     */

    private final ResourceLocation texture;


    /*
     * Texture size
     */

    private final int width;
    private final int height;


    public OreGenerationBackground(
        ResourceLocation texture,
        int width,
        int height
    ) {

        this.texture = texture;

        this.width = width;
        this.height = height;
    }


    public void draw(
        Minecraft minecraft,
        int x,
        int y
    ) {

        /*
         * Bind GUI texture.
         */

        minecraft
            .getTextureManager()
            .bindTexture(texture);


        /*
         * Draw texture.
         */

        drawTexturedModalRect(
            x,
            y,
            0,
            0,
            width,
            height
        );
    }


    public ResourceLocation getTexture() {
        return texture;
    }


    public int getWidth() {
        return width;
    }


    public int getHeight() {
        return height;
    }
}
