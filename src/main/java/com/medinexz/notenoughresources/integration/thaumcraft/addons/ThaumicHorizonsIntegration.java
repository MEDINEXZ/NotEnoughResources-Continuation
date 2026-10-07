package com.medinexz.notenoughresources.integration.thaumcraft.addons;

import com.medinexz.notenoughresources.integration.INERIntegration;

/**
 * Thaumic Horizons (1.7.10).
 *
 * <p>Checked against Thaumic Horizons 1.8.27: no world generation.  The mod has no
 * IWorldGenerator and no ores; its one dimension holds the pocket planes, which
 * players create themselves with the Planar Vortex, so nothing in it is a
 * world generation resource.</p>
 */
public class ThaumicHorizonsIntegration implements INERIntegration {

    private static final String MOD_ID = "ThaumicHorizons";

    @Override
    public String getModId() {
        return MOD_ID;
    }

    @Override
    public void registerResources() {
        // No world generation in this addon
    }
}
