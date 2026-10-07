package com.medinexz.notenoughresources.integration.thaumcraft.addons;

import com.medinexz.notenoughresources.integration.INERIntegration;

/**
 * Witching Gadgets (1.7.10).
 *
 * <p>Checked against Witching Gadgets 1.1.10 (requires Traveller's Gear): no world
 * generation.  The mod contains a tomb generator, but never registers it.</p>
 */
public class WitchingGadgetsIntegration implements INERIntegration {

    private static final String MOD_ID = "WitchingGadgets";

    @Override
    public String getModId() {
        return MOD_ID;
    }

    @Override
    public void registerResources() {
        // No world generation in this addon
    }
}
