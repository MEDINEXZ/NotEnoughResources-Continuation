package com.medinexz.notenoughresources.integration.forestry;

import com.medinexz.notenoughresources.integration.INERIntegration;
import com.medinexz.notenoughresources.integration.IntegrationHelper;

/**
 * Forestry (1.7.10) world generation resources.
 *
 * <p>Checked against Forestry 4.2.16: its IWorldGenerator places apatite ore
 * (large, rare veins high up), copper ore and tin ore; each can be switched
 * off in its config.  Beehives are generated too.</p>
 *
 * <p>Not registered: the beehives.</p>
 */
public class ForestryIntegration implements INERIntegration {

    private static final String MOD_ID = "Forestry";

    private static final int OVERWORLD = 0;

    // "resources" block variants
    private static final int APATITE = 0;
    private static final int COPPER  = 1;
    private static final int TIN     = 2;

    @Override
    public String getModId() {
        return MOD_ID;
    }

    @Override
    public void registerResources() {
        IntegrationHelper.register(MOD_ID, "resources", APATITE, OVERWORLD);
        IntegrationHelper.register(MOD_ID, "resources", COPPER, OVERWORLD);
        IntegrationHelper.register(MOD_ID, "resources", TIN, OVERWORLD);
    }
}
