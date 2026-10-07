package com.medinexz.notenoughresources.integration.mffs;

import com.medinexz.notenoughresources.integration.INERIntegration;
import com.medinexz.notenoughresources.integration.IntegrationHelper;

/**
 * Modular Forcefield System (1.7.10) world generation resources.
 *
 * <p>Checked against Modular Forcefield System 3.0-ALPHA-7: its IWorldGenerator
 * places monazit ore in stone.</p>
 */
public class ModularForcefieldSystemIntegration implements INERIntegration {

    private static final String MOD_ID = "ModularForcefieldSystem";

    private static final int OVERWORLD = 0;

    @Override
    public String getModId() {
        return MOD_ID;
    }

    @Override
    public void registerResources() {
        IntegrationHelper.register(MOD_ID, "tile.monazitOre", 0, OVERWORLD);
    }
}
