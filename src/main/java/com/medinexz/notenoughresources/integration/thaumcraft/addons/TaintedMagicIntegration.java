package com.medinexz.notenoughresources.integration.thaumcraft.addons;

import com.medinexz.notenoughresources.integration.INERIntegration;

/**
 * Tainted Magic (1.7.10).
 *
 * <p>Checked against Tainted Magic 8.1.1: no world generation.  This version
 * has no ores; its warpwood trees only grow from the sapling and the nightshade
 * bush is planted by the player.</p>
 */
public class TaintedMagicIntegration implements INERIntegration {

    private static final String MOD_ID = "TaintedMagic";

    @Override
    public String getModId() {
        return MOD_ID;
    }

    @Override
    public void registerResources() {
        // No world generation in this addon
    }
}
