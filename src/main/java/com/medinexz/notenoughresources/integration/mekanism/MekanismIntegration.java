package com.medinexz.notenoughresources.integration.mekanism;

import com.medinexz.notenoughresources.integration.INERIntegration;
import com.medinexz.notenoughresources.integration.IntegrationHelper;

/**
 * Mekanism (1.7.10) world generation resources.
 *
 * <p>Checked against Mekanism Community Edition 9.10.51: its IWorldGenerator places
 * osmium, copper and tin ore in stone below Y 60 and salt blocks on the beds
 * of water, everywhere except the Nether and the End; the amounts per chunk
 * come from its config.</p>
 */
public class MekanismIntegration implements INERIntegration {

    private static final String MOD_ID = "Mekanism";

    private static final int OVERWORLD = 0;

    // OreBlock variants
    private static final int OSMIUM = 0;
    private static final int COPPER = 1;
    private static final int TIN    = 2;

    @Override
    public String getModId() {
        return MOD_ID;
    }

    @Override
    public void registerResources() {
        IntegrationHelper.register(MOD_ID, "OreBlock", OSMIUM, OVERWORLD);
        IntegrationHelper.register(MOD_ID, "OreBlock", COPPER, OVERWORLD);
        IntegrationHelper.register(MOD_ID, "OreBlock", TIN, OVERWORLD);
        IntegrationHelper.register(MOD_ID, "SaltBlock", 0, OVERWORLD);
    }
}
