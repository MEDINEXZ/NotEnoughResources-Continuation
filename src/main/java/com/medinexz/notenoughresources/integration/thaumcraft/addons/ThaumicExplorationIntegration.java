package com.medinexz.notenoughresources.integration.thaumcraft.addons;

import com.medinexz.notenoughresources.integration.INERIntegration;

/**
 * Thaumic Exploration (1.7.10).
 *
 * <p>Checked against Thaumic Exploration 1.1-53: no world generation.  The mod
 * adds no ores, biomes, dimensions or structures; all of its blocks are crafted
 * devices.</p>
 */
public class ThaumicExplorationIntegration implements INERIntegration {

    private static final String MOD_ID = "ThaumicExploration";

    @Override
    public String getModId() {
        return MOD_ID;
    }

    @Override
    public void registerResources() {
        // No world generation in this addon
    }
}
