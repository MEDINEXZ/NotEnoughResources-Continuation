package com.medinexz.notenoughresources.integration.thaumcraft.addons;

import com.medinexz.notenoughresources.integration.INERIntegration;

/**
 * Automagy (1.7.10).
 *
 * <p>Checked against Automagy 0.28.3: no world generation resources.  Its
 * IWorldGenerator builds one structure, the Nether spire over large lava pools
 * (1 in 140 suitable chunks by default): obsidian columns with a fire node, a
 * wisp spawner and a chest.  The only block of its own in it is runed
 * obsidian, which is a crafted block otherwise, and the spire is too rare to
 * show up in the chunks NER profiles.</p>
 */
public class AutomagyIntegration implements INERIntegration {

    private static final String MOD_ID = "Automagy";

    @Override
    public String getModId() {
        return MOD_ID;
    }

    @Override
    public void registerResources() {
        // No world generation resources in this addon
    }
}
