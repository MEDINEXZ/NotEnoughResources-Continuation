package com.medinexz.notenoughresources.nei;

import codechicken.nei.drawable.DrawableResource;
import codechicken.nei.event.NEIRegisterHandlerInfosEvent;

import com.medinexz.notenoughresources.NotEnoughResources;
import com.medinexz.notenoughresources.gui.OreGenerationNEIHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.util.ResourceLocation;

/**
 * Tells NEI which mod owns the World Generation handler (so its tab shows the mod
 * name instead of "Unknown"), which icon the tab uses, and how large one entry is.
 */
public class NEIHandlerInfoRegistrar {

    // Tab icon (16x16): the World Gen globe from Just Enough Resources' tabs.png, used under
    // https://github.com/way2muchnoise/JustEnoughResources/blob/1.12.2/LICENSE.md
    public static final String ICON = "notenoughresources:textures/gui/world_generation_icon.png";
    private static final int ICON_SIZE = 16;

    // NEI lays handler images out as 14x14 sprites: it draws them one pixel inside
    // the 16x16 area an item icon would occupy in the tab.  A 16x16 image therefore
    // has to start that inset earlier to cover the same, centred, 16x16 area.
    private static final int NEI_IMAGE_SIZE = 14;
    private static final int ICON_SHIFT = (NEI_IMAGE_SIZE - ICON_SIZE) / 2;

    private static DrawableResource createIcon() {
        return new DrawableResource(
            new ResourceLocation(ICON), 0, 0, ICON_SIZE, ICON_SIZE, 0, 0, 0, 0, ICON_SIZE, ICON_SIZE) {

            @Override
            public void draw(int x, int y) {
                super.draw(x + ICON_SHIFT, y + ICON_SHIFT);
            }
        };
    }

    @SubscribeEvent
    public void onRegisterHandlerInfos(NEIRegisterHandlerInfosEvent event) {
        event.registerHandlerInfo(
            OreGenerationNEIHandler.class,
            NotEnoughResources.MODNAME,
            NotEnoughResources.MODID,
            builder -> builder
                .setDisplayImage(createIcon())
                .setWidth(OreGenerationNEIHandler.GUI_WIDTH)
                .setHeight(OreGenerationNEIHandler.ENTRY_STEP)
                // NEI then puts as many entries on a page as fit into the recipe GUI height
                .setMultipleWidgetsAllowed(true)
        );
    }
}
