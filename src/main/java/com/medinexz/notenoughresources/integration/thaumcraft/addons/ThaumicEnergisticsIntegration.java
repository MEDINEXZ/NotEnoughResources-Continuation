package com.medinexz.notenoughresources.integration.thaumcraft.addons;

import com.medinexz.notenoughresources.integration.INERIntegration;

/**
 * Thaumic Energistics (1.7.10).
 *
 * <p>Checked against Thaumic Energistics 1.1.3.0 (requires Applied Energistics 2):
 * no world generation.  The mod only connects essentia to ME networks; all of
 * its blocks are crafted devices.</p>
 */
public class ThaumicEnergisticsIntegration implements INERIntegration {

    private static final String MOD_ID = "thaumicenergistics";

    @Override
    public String getModId() {
        return MOD_ID;
    }

    @Override
    public void registerResources() {
        // No world generation in this addon
    }
}
