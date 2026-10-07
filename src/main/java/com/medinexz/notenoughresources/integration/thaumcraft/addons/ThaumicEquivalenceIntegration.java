package com.medinexz.notenoughresources.integration.thaumcraft.addons;

import com.medinexz.notenoughresources.integration.INERIntegration;

/**
 * Thaumic Equivalence (1.7.10).
 *
 * <p>Checked against Thaumic Equivalence 1.0.4 (requires ProjectE): no world
 * generation and no blocks at all; the mod maps aspects to EMC values.</p>
 */
public class ThaumicEquivalenceIntegration implements INERIntegration {

    private static final String MOD_ID = "ThaumicEquivalence";

    @Override
    public String getModId() {
        return MOD_ID;
    }

    @Override
    public void registerResources() {
        // No world generation in this addon
    }
}
